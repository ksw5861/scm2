<script setup>
import { ref } from 'vue';
import axios from 'axios';
import { useAppToast } from '@/composables/useAppToast';
import Modal from '@/components/common/Modal.vue';

import InputText from 'primevue/inputtext';
import InputGroup from 'primevue/inputgroup';
import InputGroupAddon from 'primevue/inputgroupaddon';

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
const searchForm = ref({ prodCode: '', prodName: '', makerDate: null });

/* ------------------ 목록/상세 ------------------ */
const inboundList = ref([]);
const selectedRow = ref(null);

const detail = ref({
  prodCode: '',
  prodName: '',
  lotNo: '',
  qty: null,
  stockNow: null,
  stockAfter: null,
  prodDate: '',
  expireDate: null,
  unitSpec: '',
  whCode: '',
  manager: '',
  inDate: ''
});

/* ------------------ 초기화 ------------------ */
function clearDetail() {
  detail.value = {
    prodCode: '',
    prodName: '',
    lotNo: '',
    qty: null,
    stockNow: null,
    stockAfter: null,
    prodDate: '',
    expireDate: null,
    unitSpec: '',
    whCode: '',
    manager: '',
    inDate: ''
  };
}

/* ------------------ 바인딩 ------------------ */
function bindDetail(row) {
  selectedRow.value = row;
  detail.value = {
    prodCode: row?.prodCode ?? '',
    prodName: row?.prodName ?? '',
    lotNo: row?.lotNo ?? '',
    qty: row?.qty ?? null,
    prodDate: row?.prodDate ?? '',
    expireDate: null,
    unitSpec: row?.unitSpec ?? '',
    whCode: row?.whCode ?? '',
    manager: row?.manager ?? '',
    inDate: row?.inDate ?? '',
    stockNow: Number(row?.stockNow) || 0,
    stockAfter: (Number(row?.stockNow) || 0) + (Number(row?.qty) || 0)
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
      makerDate: searchForm.value.makerDate ? fmtDate(searchForm.value.makerDate) : ''
    };
    const { data } = await axios.get('/api/inbound/lots', { params });
    inboundList.value = Array.isArray(data) ? data : [];
    clearDetail();
  } catch (err) {
    toast.error('목록 조회 오류');
    console.error(err);
  }
}

function resetSearch() {
  searchForm.value = { prodCode: '', prodName: '', makerDate: null };
  doSearch();
}

/* ------------------ 모달 ------------------ */
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

/* ------------------ 저장/삭제 ------------------ */
async function save() {
  try {
    const body = {
      prdLot: detail.value.lotNo,
      prodNo: detail.value.prodCode,
      prodId: detail.value.prodCode,
      inQty: Number(detail.value.qty || 0),
      inDate: detail.value.inDate,
      employeeId: detail.value.manager,
      whId: detail.value.whCode,
      prodName: detail.value.prodName
    };
    await axios.post('/api/inbound', body);
    toast.success('입고 등록 완료');
    await doSearch();
  } catch (err) {
    toast.error('입고 등록 오류');
    console.error(err);
  }
}

async function remove() {
  try {
    const inboundId = selectedRow.value?.inboundId;
    if (!inboundId) {
      toast.warn('삭제할 ID 없음');
      return;
    }
    await axios.delete(`/api/inbound/${inboundId}`);
    toast.success('삭제 완료');
    await doSearch();
  } catch (err) {
    toast.error('삭제 오류');
    console.error(err);
  }
}
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
          <Calendar v-model="searchForm.makerDate" dateFormat="yy-mm-dd" showIcon class="w-full" />
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
        <DataTable :value="inboundList" dataKey="lotNo" class="datatable" @rowClick="onRowClick">
          <Column field="lotNo" header="LOT번호" />
          <Column field="prodCode" header="제품코드" />
          <Column field="prodName" header="제품명" />
          <Column field="qty" header="수량" />
          <Column field="status" header="상태" />
          <!-- ✅ 목록에만 상태 -->
        </DataTable>
      </div>

      <!-- 상세 -->
      <div class="detail-box">
        <div class="detail-head">
          <div class="detail-title">상세</div>
          <div class="head-actions">
            <Button label="등록" icon="pi pi-save" severity="primary" @click="save" />
          </div>
        </div>

        <div class="detail-grid">
          <div class="field"><label>제품코드</label><InputText v-model="detail.prodCode" /></div>
          <div class="field"><label>제품명</label><InputText v-model="detail.prodName" /></div>
          <div class="field"><label>LOT번호</label><InputText v-model="detail.lotNo" /></div>
          <div class="field"><label>수량</label><InputText v-model="detail.qty" /></div>
          <div class="field"><label>현재고</label><InputText v-model="detail.stockNow" /></div>
          <div class="field"><label>입고 후 재고</label><InputText v-model="detail.stockAfter" /></div>
          <div class="field"><label>생산일자</label><InputText v-model="detail.prodDate" disabled /></div>
          <div class="field"><label>유통기한</label><Calendar v-model="detail.expireDate" dateFormat="yy-mm-dd" showIcon /></div>
          <div class="field"><label>규격/단위</label><InputText v-model="detail.unitSpec" /></div>
          <div class="field"><label>담당자</label><InputText v-model="detail.manager" /></div>
          <div class="field"><label>입고일자</label><InputText v-model="detail.inDate" /></div>
          <!-- ❌ 상태는 상세에서 제거 -->
        </div>
      </div>
    </div>

    <!-- 모달 -->
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
  </div>
</template>

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
