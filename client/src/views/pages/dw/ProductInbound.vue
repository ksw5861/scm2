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
const searchForm = ref({ prodId: '', prodName: '', endDate: null });

/* ------------------ 목록/상세 ------------------ */
const inboundList = ref([]);
const selectedRow = ref(null);

const detail = ref({
  prodId: '',
  prodName: '',
  prodNo: '',
  proQty: null,
  proDate: '',
  endDate: null,
  spec: '',
  unit: '',
  manager: '',
  whCode: ''
});

/* ------------------ 초기화 ------------------ */
function clearDetail() {
  detail.value = {
    prodId: '',
    prodName: '',
    prodNo: '',
    proQty: null,
    proDate: '',
    endDate: null,
    spec: '',
    unit: '',
    manager: '',
    whCode: ''
  };
}

/* ------------------ 바인딩 ------------------ */
function bindDetail(row) {
  selectedRow.value = row;
  detail.value = {
    prodId: row?.prodId ?? '',
    prodName: row?.prodName ?? '',
    prodNo: row?.prodNo ?? '',
    proQty: row?.proQty ?? null,
    endDate: row?.endDate ?? null,
    spec: row?.spec ?? '',
    manager: row?.manager ?? '',
    whCode: row?.whCode ?? '',
    proDate: row?.proDate ?? ''
  };
}

/* ------------------ 이벤트 ------------------ */
function onRowClick(e) {
  bindDetail(e.data);
}

async function doSearch() {
  try {
    const params = {
      prodCode: searchForm.value.prodId.trim(),
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
  searchForm.value = { prodId: '', prodName: '', proDate: null };
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
  searchForm.value.prodId = code;
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
      prodNo: detail.value.prodNo,
      employeeId: detail.value.manager,
      totalQty: Number(detail.value.proQty || 0),
      whId: detail.value.whCode
    };
    // const result = await axios.post('/api/inbound', body);
    // console.log(result.data.retCode); // success, fail
    // console.log(result.status); // 200, 404, 405, 400

    // if (result.data.retCode === 'success') {
    //   toast('success', '등록 성공', '성공적으로 입고처리되었습니다.');
    // } else if (result.data.retCode === 'fail') {
    //   toast('error', '실패', '실패 하였습니다');
    // }

    // if (result.status === 200) {
    //   toast('success', '등록 성공', '성공적으로 입고처리되었습니다.');
    // } else {
    //   toast('error', '실패', '실패 하였습니다');
    // }

    // const { data } = await axios.post('/api/inbound', body);
    const { status } = await axios.post('/api/inbound', body); // 200

    if (status === 200) return toast('success', '등록 성공', '성공적으로 입고처리되었습니다.');
    if (status === 302) return toast('error', '에러', '사용자 정의 에러');

    await doSearch();
  } catch (err) {
    toast('error', '실패', '실패 하였습니다');
    console.error(err);
  }
}

/* ------------------ 삭제 ------------------ */
async function remove() {
  try {
    const prodNo = selectedRow.value?.prodNo;
    if (!prodNo) {
      toast.add({
        severity: 'warn',
        summary: '경고',
        detail: '삭제할 LOT을 선택하세요',
        life: 3000
      });
      return;
    }

    await axios.delete(`/api/inbound/${prodNo}`);
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
            <InputText v-model="searchForm.prodId" placeholder="PRD001" @click="openProdModal" />
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
          <Calendar v-model="searchForm.proDate" dateFormat="yy-mm-dd" showIcon class="w-full" />
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

          <Column field="prodNo" header="LOT번호" />
          <Column field="prodId" header="제품코드" />
          <Column field="prodName" header="제품명" />
          <Column field="proQty" header="수량" />
          <Column field="matStatus" header="상태" />
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
          <div class="field"><label>제품코드</label><InputText v-model="detail.prodId" /></div>
          <div class="field"><label>제품명</label><InputText v-model="detail.prodName" /></div>
          <div class="field"><label>LOT번호</label><InputText v-model="detail.prodNo" /></div>
          <div class="field"><label>입고일자</label><Calendar v-model="detail.proDate" dateFormat="yy-mm-dd" showIcon /></div>
          <div class="field"><label>입고수량</label><InputText v-model="detail.proQty" /></div>
          <div class="field"><label>유통기한</label><Calendar v-model="detail.endDate" dateFormat="yy-mm-dd" showIcon /></div>
          <div class="field"><label>규격/단위</label><InputText v-model="detail.spec" /></div>
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
