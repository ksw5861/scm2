<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useAppToast } from '@/composables/useAppToast';
import Modal from '@/components/common/Modal.vue';
import Checkbox from 'primevue/checkbox';

import InputText from 'primevue/inputtext';
import InputGroup from 'primevue/inputgroup';
import InputGroupAddon from 'primevue/inputgroupaddon';
import Calendar from 'primevue/calendar';
import Button from 'primevue/button';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import { useToast } from 'primevue/usetoast';

const { toast } = useAppToast();

/* ------------------ 유틸 ------------------ */
function fmtDate(d) {
  if (!d) return '';
  try {
    const dt = typeof d === 'string' ? new Date(d) : d;
    return `${dt.getFullYear()}-${String(dt.getMonth() + 1).padStart(2, '0')}-${String(dt.getDate()).padStart(2, '0')}`;
  } catch {
    return '';
  }
}

/* ------------------ 검색 폼 ------------------ */
const searchForm = ref({ prodCode: '', prodName: '', endDate: null });

/* ------------------ 목록/상세 ------------------ */
const inboundList = ref([]);
const selectedRow = ref(null);

const detail = ref({
  prodCode: '',
  prodName: '',
  lotNo: '',
  qty: null,
  inDate: '',
  expireDate: null,
  unitSpec: '',
  manager: ''
  // whCode: ''
});

/* ------------------ 초기화 ------------------ */
function clearDetail() {
  detail.value = {
    prodCode: '',
    prodName: '',
    lotNo: '',
    qty: null,
    inDate: '',
    expireDate: null,
    unitSpec: '',
    manager: '',
    whCode: ''
  };
}

/* ------------------ 바인딩 ------------------ */
function bindDetail(row) {
  selectedRow.value = row;
  detail.value = {
    prodCode: row?.prodId ?? '',
    prodName: row?.prodName ?? '',
    lotNo: row?.prdLot ?? '',
    qty: row?.totalQty ?? null,
    inDate: row?.inDate ?? '',
    expireDate: row?.expireDate ?? null,
    unitSpec: row?.unitSpec ?? '',
    manager: row?.manager ?? '',
    whCode: row?.whCode ?? ''
  };
}

/* ------------------ 이벤트 ------------------ */
function onRowClick(e) {
  bindDetail(e.data);
}

async function doSearch() {
  try {
    const params = {
      prodCode: searchForm.value.prodCode.trim(),
      prodName: searchForm.value.prodName.trim(),
      endDate: searchForm.value.endDate ? fmtDate(searchForm.value.endDate) : ''
    };
    const { data } = await axios.get('/api/lots', { params });
    inboundList.value = data ?? [];
    clearDetail();
  } catch (err) {
    toast.error('목록 조회 오류');
    console.error(err);
  }
}

function resetSearch() {
  searchForm.value = { prodCode: '', prodName: '', endDate: null };
  doSearch();
}

/* ------------------ 제품 모달 ------------------ */
const isShowProdModal = ref(false);
function openProdModal() {
  isShowProdModal.value = true;
}
function closeProdModal() {
  isShowProdModal.value = false;
}

const fetchProductData = async ({ q = '', page = 1, size = 10 } = {}) => {
  const { data } = await axios.get('/api/products', { params: { q, page, size } });
  return data;
};

function handleSelectProduct(item) {
  const code = item?.prodId || '';
  const name = item?.prodName || '';
  searchForm.value.prodCode = code;
  searchForm.value.prodName = name;
  closeProdModal();
  toast.info(`제품 선택: ${code} / ${name}`);
  doSearch();
}

/* ------------------ 창고 모달 ------------------ */
const isShowWhModal = ref(false);
function openWhModal() {
  isShowWhModal.value = true;
}
function closeWhModal() {
  isShowWhModal.value = false;
}

const fetchWarehouseData = async ({ q = '', page = 1, size = 10 } = {}) => {
  const { data } = await axios.get('/api/warehouses', { params: { q, page, size } });
  return data;
};

function handleSelectWarehouse(item) {
  detail.value.whCode = item?.whId || '';
  closeWhModal();
  toast.info(`창고 선택: ${detail.value.whCode}`);
}

/* ------------------ 저장 ------------------ */
async function save() {
  try {
    const body = {
      prdLot: detail.value.lotNo,
      prodId: detail.value.prodCode,
      totalQty: Number(detail.value.qty || 0), // ✅ qty → totalQty 맞춤
      whId: detail.value.whCode,
      transferDate: detail.value.inDate
    };

    await axios.post('/api/inbound', body);
    toast.add({
      severity: 'success',
      summary: '성공',
      detail: '입고 등록 완료',
      life: 3000
    });
    await doSearch();
  } catch (err) {
    toast.add({
      severity: 'error',
      summary: '에러',
      detail: '입고 등록 실패',
      life: 3000
    });
    console.error(err);
  }
}

/* ------------------ 삭제 ------------------ */
async function remove() {
  try {
    const prdLot = selectedRow.value?.prdLot;
    if (!prdLot) {
      toast.add({
        severity: 'warn',
        summary: '경고',
        detail: '삭제할 LOT을 선택하세요',
        life: 3000
      });
      return;
    }

    await axios.delete(`/api/inbound/${prdLot}`);
    toast.add({
      severity: 'success',
      summary: '성공',
      detail: '삭제 완료',
      life: 3000
    });
    await doSearch();
  } catch (err) {
    toast.add({
      severity: 'error',
      summary: '에러',
      detail: '삭제 실패',
      life: 3000
    });
    console.error(err);
  }
}

// ✅ 페이지 진입 시 자동 조회
onMounted(() => {
  doSearch();
});
</script>

<template>
  <div class="page-wrap">
    <!-- 검색 -->
    <div class="box">
      <div class="form-grid">
        <div class="field">
          <label>제품코드</label>
          <InputGroup>
            <InputText v-model="searchForm.prodCode" placeholder="PRD001" @click="openProdModal" />
            <InputGroupAddon>
              <Button icon="pi pi-search" text @click="openProdModal" />
            </InputGroupAddon>
          </InputGroup>
        </div>
        <div class="field">
          <label>제품명</label>
          <InputGroup>
            <InputText v-model="searchForm.prodName" placeholder="원두" @click="openProdModal" />
            <InputGroupAddon>
              <Button icon="pi pi-search" text @click="openProdModal" />
            </InputGroupAddon>
          </InputGroup>
        </div>
        <div class="field">
          <label>생산일자</label>
          <Calendar v-model="searchForm.endDate" dateFormat="yy-mm-dd" showIcon class="w-full" />
        </div>
      </div>
      <div class="actions">
        <Button label="초기화" icon="pi pi-refresh" outlined @click="resetSearch" />
        <Button label="조회" icon="pi pi-search" @click="doSearch" />
      </div>
    </div>

    <!-- 목록 + 상세 (좌우 분할 유지) -->
    <div class="split">
      <!-- 목록 -->
      <div class="list-box">
        <DataTable :value="inboundList" dataKey="prdLot" v-model:selection="selectedRow" selectionMode="single" @rowClick="onRowClick" :rowClass="(row) => (selectedRow?.prdLot === row.prdLot ? 'selected-row' : '')" highlightOnHover>
          <!-- ✅ 행 체크박스, 전체선택 헤더 제거 -->
          <Column headerStyle="width:3em" :header="''">
            <template #body="slotProps">
              <Checkbox :binary="true" :modelValue="selectedRow?.prdLot === slotProps.data.prdLot" @change="bindDetail(slotProps.data)" />
            </template>
          </Column>

          <Column field="prdLot" header="LOT번호" />
          <Column field="prodId" header="제품코드" />
          <Column field="prodName" header="제품명" />
          <Column field="totalQty" header="수량" />
          <Column field="status" header="상태" />
        </DataTable>
      </div>

      <!-- 상세 -->
      <div class="detail-box">
        <div class="detail-head">
          <div class="detail-title">상세</div>
          <div class="head-actions">
            <Button label="삭제" icon="pi pi-trash" severity="danger" @click="remove" />
            <Button label="등록" icon="pi pi-save" severity="primary" @click="save" />
          </div>
        </div>

        <div class="detail-grid">
          <div class="field"><label>제품코드</label><InputText v-model="detail.prodCode" /></div>
          <div class="field"><label>제품명</label><InputText v-model="detail.prodName" /></div>
          <div class="field"><label>LOT번호</label><InputText v-model="detail.lotNo" /></div>
          <div class="field"><label>입고일자</label><Calendar v-model="detail.inDate" dateFormat="yy-mm-dd" showIcon /></div>
          <div class="field"><label>입고수량</label><InputText v-model="detail.qty" /></div>
          <div class="field"><label>유통기한</label><Calendar v-model="detail.expireDate" dateFormat="yy-mm-dd" showIcon /></div>
          <div class="field"><label>규격/단위</label><InputText v-model="detail.unitSpec" /></div>
          <div class="field"><label>담당자</label><InputText v-model="detail.manager" /></div>
          <div class="field">
            <label>창고코드</label>
            <InputGroup>
              <InputText v-model="detail.whCode" placeholder="WH001" @click="openWhModal" />
              <InputGroupAddon>
                <Button icon="pi pi-search" text @click="openWhModal" />
              </InputGroupAddon>
            </InputGroup>
          </div>
        </div>
      </div>
    </div>

    <!-- 제품검색 모달 -->
    <Modal
      :visible="isShowProdModal"
      title="제품 검색"
      idField="prodId"
      :columns="[
        { key: 'prodId', label: '제품코드' },
        { key: 'prodName', label: '제품명' }
      ]"
      :fetchData="fetchProductData"
      :page-size="5"
      @select="handleSelectProduct"
      @close="closeProdModal"
    />

    <!-- 창고검색 모달 -->
    <Modal
      :visible="isShowWhModal"
      title="창고 검색"
      idField="whId"
      :columns="[
        { key: 'whId', label: '창고코드' },
        { key: 'whName', label: '창고명' }
      ]"
      :fetchData="fetchWarehouseData"
      :page-size="5"
      @select="handleSelectWarehouse"
      @close="closeWhModal"
    />
  </div>
</template>

<style>
.selected-row {
  background: #dbeafe !important; /* 선택된 행 강조 */
}
</style>

<style scoped>
.page-wrap {
  padding: 16px;
  background: #f5f7fb;
}
.box {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 12px;
}
.form-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 8px;
}
.split {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 14px;
}
.list-box,
.detail-box {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 12px;
}
.detail-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.detail-title {
  font-weight: 700;
}
.head-actions {
  display: flex;
  gap: 8px;
}
.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}
</style>
