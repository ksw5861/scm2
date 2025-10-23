<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

import InputText from 'primevue/inputtext';
import InputGroup from 'primevue/inputgroup';
import InputGroupAddon from 'primevue/inputgroupaddon';
import Calendar from 'primevue/calendar';
import Button from 'primevue/button';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Dialog from 'primevue/dialog';
import Textarea from 'primevue/textarea';

/* ------------------ 상태 ------------------ */
// 검색폼
const search = ref({ prodId: '', prodName: '', vendorName: '', fromDate: null, toDate: null, returnId: '' });

// 목록/상세
const returnList = ref([]);
const selectedReturns = ref([]);
const detailRows = ref([]);
const selectedDetailRows = ref([]);
const currentReturnId = ref(null);

// 반려 모달
const rejectDialog = ref(false);
const rejectReason = ref('');

/* ------------------ 모달 상태 ------------------ */
// 판매처 모달
const vendorDialog = ref(false);
const vendorKeyword = ref('');
const vendorList = ref([]);
const vendorPage = ref(1);
const vendorRows = ref(10);
const vendorTotal = ref(0);

// 반품코드 모달
const returnDialog = ref(false);
const returnKeyword = ref('');
const returnListModal = ref([]);
const returnPage = ref(1);
const returnRows = ref(10);
const returnTotal = ref(0);

/* ------------------ 유틸 ------------------ */
function fmtDate(d) {
  if (!d) return '';
  const dt = typeof d === 'string' ? new Date(d) : d;
  return `${dt.getFullYear()}-${String(dt.getMonth() + 1).padStart(2, '0')}-${String(dt.getDate()).padStart(2, '0')}`;
}
function toNumber(n) {
  if (!n) return 0;
  const num = Number(String(n).replace(/[^\d.-]/g, ''));
  return isFinite(num) ? num : 0;
}
function fmtWon(n) {
  return new Intl.NumberFormat('ko-KR').format(toNumber(n)) + '원';
}

/* ------------------ 상세 조회 ------------------ */
async function loadDetails(row) {
  if (!row) {
    detailRows.value = [];
    selectedDetailRows.value = [];
    currentReturnId.value = null;
    return;
  }
  if (currentReturnId.value === row.returnId) {
    detailRows.value = [];
    selectedDetailRows.value = [];
    currentReturnId.value = null;
    return;
  }
  selectedDetailRows.value = [];
  const res = await axios.get('/api/return-details', { params: { returnId: row.returnId } });
  detailRows.value = res.data?.data ?? [];
  currentReturnId.value = row.returnId;
}

/* ------------------ 목록 조회 ------------------ */
async function applySearch() {
  const params = {
    prodId: search.value.prodId || '',
    prodName: search.value.prodName || '',
    vendorName: search.value.vendorName || '',
    fromDate: search.value.fromDate ? fmtDate(search.value.fromDate) : '',
    toDate: search.value.toDate ? fmtDate(search.value.toDate) : '',
    returnId: search.value.returnId || '',
    page: 1,
    size: 10
  };
  const res = await axios.get('/api/return-list', { params });
  returnList.value = res.data?.data ?? res.data ?? [];
  detailRows.value = [];
  selectedReturns.value = [];
  selectedDetailRows.value = [];
  currentReturnId.value = null;
}
function resetSearch() {
  search.value = { prodId: '', prodName: '', vendorName: '', fromDate: null, toDate: null, returnId: '' };
  applySearch();
}

/* ------------------ 선택 변경 ------------------ */
async function onReturnSelectionChange(e) {
  const arr = Array.isArray(e?.value) ? e.value : [];
  if (!arr.length) {
    selectedReturns.value = [];
    await loadDetails(null);
    return;
  }
  const last = arr[arr.length - 1];
  selectedReturns.value = [last];
  await loadDetails(last);
}

/* ------------------ 행 클릭 ------------------ */
async function onReturnRowClick(e) {
  const row = e.data;
  selectedReturns.value = [row];
  await loadDetails(row);
}

/* ------------------ 상세 체크박스 ------------------ */
function toggleDetailSelection(row) {
  const idx = selectedDetailRows.value.findIndex((r) => r.rdetailId === row.rdetailId);
  if (idx >= 0) selectedDetailRows.value.splice(idx, 1);
  else selectedDetailRows.value.push(row);
  return selectedDetailRows.value;
}

/* ------------------ 승인 ------------------ */
async function approveSelected() {
  if (!selectedDetailRows.value.length) {
    alert('승인할 상세를 선택하세요');
    return;
  }
  const ids = selectedDetailRows.value.map((r) => r.rdetailId);
  const res = await axios.post('/api/return/approve', { ids });
  if (res?.status === 200 && res.data.retCode === 'success') {
    alert('승인 완료');
    await applySearch();
  } else alert('승인 실패');
}

/* ------------------ 반려 ------------------ */
function openRejectDialog() {
  if (!selectedDetailRows.value.length) {
    alert('반려할 상세를 선택하세요');
    return;
  }
  rejectReason.value = '';
  rejectDialog.value = true;
}
async function confirmReject() {
  if (!rejectReason.value.trim()) {
    alert('반려 사유를 입력하세요');
    return;
  }
  const ids = selectedDetailRows.value.map((r) => r.rdetailId);
  const res = await axios.post('/api/return/reject', { ids, reason: rejectReason.value });
  if (res?.status === 200 && res.data.retCode === 'success') {
    alert('반려 완료');
    rejectDialog.value = false;
    await applySearch();
  } else alert('반려 실패');
}

/* ------------------ 모달 ------------------ */
// 판매처 모달
async function openVendorModal() {
  vendorDialog.value = true;
  vendorPage.value = 1;
  await loadVendorList();
}
async function loadVendorList() {
  const { data } = await axios.get('/api/returnApproval/modal/searchByVendor', {
    params: { condition: vendorKeyword.value, page: vendorPage.value, size: vendorRows.value }
  });
  vendorList.value = data.items ?? data.data ?? [];
  vendorTotal.value = data.totalCount ?? 0;
}
function onVendorPageChange(e) {
  vendorPage.value = e.page + 1;
  vendorRows.value = e.rows;
  loadVendorList();
}
function selectVendor(e) {
  const v = e.data;
  search.value.vendorName = v.companyName;
  search.value.vendorId = v.vendorId;
  vendorDialog.value = false;
}

// 반품코드 모달
async function openReturnModal() {
  returnDialog.value = true;
  returnPage.value = 1;
  await loadReturnList();
}
async function loadReturnList() {
  const { data } = await axios.get('/api/returnApproval/modal/searchByCode', {
    params: { condition: returnKeyword.value, page: returnPage.value, size: returnRows.value }
  });
  returnListModal.value = data.items ?? data.data ?? [];
  returnTotal.value = data.totalCount ?? 0;
}
function onReturnPageChange(e) {
  returnPage.value = e.page + 1;
  returnRows.value = e.rows;
  loadReturnList();
}
function selectReturn(e) {
  const r = e.data;
  search.value.returnId = r.returnId;
  returnDialog.value = false;
}

onMounted(() => applySearch());
</script>

<template>
  <div class="page-wrap">
    <div class="page-title">반품 승인 처리</div>

    <!-- 검색폼 -->
    <div class="box">
      <div class="box-title">반품 검색</div>
      <div class="form-grid-4">
        <!-- 반품일자 -->
        <div class="field">
          <label>반품 일자</label>
          <div class="flex gap-2">
            <Calendar v-model="search.fromDate" dateFormat="yy-mm-dd" showIcon class="w-full" />
            <Calendar v-model="search.toDate" dateFormat="yy-mm-dd" showIcon class="w-full" />
          </div>
        </div>

       
        <!-- 판매처명 -->
        <div class="field">
          <label>판매처명</label>
          <InputGroup>
            <InputText v-model="search.vendorName" placeholder="판매처명" readonly @click="openVendorModal" />
            <InputGroupAddon>
              <Button icon="pi pi-search" class="p-button-text p-button-plain" @click="openVendorModal" />
            </InputGroupAddon>
          </InputGroup>
        </div>

        <!-- 반품코드 -->
        <div class="field">
          <label>반품코드</label>
          <InputGroup>
            <InputText v-model="search.returnId" placeholder="반품코드" readonly @click="openReturnModal" />
            <InputGroupAddon>
              <Button icon="pi pi-search" class="p-button-text p-button-plain" @click="openReturnModal" />
            </InputGroupAddon>
          </InputGroup>
        </div>
      </div>
      <div class="actions">
        <Button label="초기화" icon="pi pi-refresh" @click="resetSearch" />
        <Button label="조회" icon="pi pi-search" @click="applySearch" />
      </div>
    </div>

    <!-- 목록 + 상세 -->
    <div class="split">
      <div class="list-box">
        <div class="sub-title">반품 목록</div>
        <DataTable
          :value="returnList"
          dataKey="returnId"
          v-model:selection="selectedReturns"
          :metaKeySelection="false"
          @selection-change="onReturnSelectionChange"
          @row-click="onReturnRowClick"
          paginator
          :rows="10"
        >
          <Column selectionMode="multiple" :headerCheckbox="false" />
          <Column field="returnDate" header="반품일자" :body="(r) => fmtDate(r.returnDate)" />
          <Column field="companyName" header="판매처명" />
          <Column field="returnId" header="반품코드" />
          <Column field="returnStatus" header="상태" />
        </DataTable>
      </div>

      <div class="detail-box">
        <div class="detail-head">
          <div class="detail-title">반품 상세</div>
          <div class="head-actions">
            <Button label="승인" icon="pi pi-check" @click="approveSelected" :disabled="!selectedDetailRows.length" />
            <Button label="반려" icon="pi pi-times" class="p-button-danger" @click="openRejectDialog" :disabled="!selectedDetailRows.length" />
          </div>
        </div>
        <DataTable
          :value="detailRows"
          dataKey="rdetailId"
          v-model:selection="selectedDetailRows"
          selectionMode="multiple"
          :metaKeySelection="false"
          @row-click="toggleDetailSelection($event.data)"
          paginator
          :rows="10"
        >
          <Column selectionMode="multiple" :headerCheckbox="false" />
          <Column field="prodId" header="제품코드" />
          
          <Column field="companyName" header="판매처명" />
          <Column field="returnQty" header="수량" />
          <Column field="prodUnitPrice" header="단가">
            <template #body="{ data }">{{ fmtWon(data.prodUnitPrice) }}</template>
          </Column>
          <Column field="amt" header="반품금액">
            <template #body="{ data }">{{ fmtWon(data.amt) }}</template>
          </Column>
          <Column field="rdetailStatus" header="상태" />
        </DataTable>
      </div>
    </div>

    <!-- 반려 모달 -->
    <Dialog v-model:visible="rejectDialog" modal header="반려 사유 입력" :style="{ width: '400px' }">
      <Textarea v-model="rejectReason" rows="5" class="w-full" placeholder="반려 사유를 입력해 주세요" />
      <template #footer>
        <Button label="취소" icon="pi pi-times" class="p-button-text" @click="rejectDialog = false" />
        <Button label="반려" icon="pi pi-check" class="p-button-danger" @click="confirmReject" />
      </template>
    </Dialog>

    <!-- 판매처 모달 -->
    <Dialog v-model:visible="vendorDialog" header="판매처 검색" modal closable :style="{ width: '600px' }">
      <div class="p-inputgroup mb-3">
        <InputText v-model="vendorKeyword" placeholder="판매처명 검색" @keyup.enter="loadVendorList" />
        <Button icon="pi pi-search" @click="loadVendorList" />
      </div>
      <DataTable
        :value="vendorList"
        dataKey="vendorId"
        selectionMode="single"
        @row-dblclick="selectVendor"
        paginator
        :rows="vendorRows"
        :totalRecords="vendorTotal"
        @page="onVendorPageChange"
      >
        <Column field="companyName" header="판매처명" />
      </DataTable>
    </Dialog>

    <!-- 반품코드 모달 -->
    <Dialog v-model:visible="returnDialog" header="반품코드 검색" modal closable :style="{ width: '600px' }">
      <div class="p-inputgroup mb-3">
        <InputText v-model="returnKeyword" placeholder="반품코드 검색" @keyup.enter="loadReturnList" />
        <Button icon="pi pi-search" @click="loadReturnList" />
      </div>
      <DataTable
        :value="returnListModal"
        dataKey="returnId"
        selectionMode="single"
        @row-dblclick="selectReturn"
        paginator
        :rows="returnRows"
        :totalRecords="returnTotal"
        @page="onReturnPageChange"
      >
        <Column field="returnId" header="반품코드" />
      </DataTable>
    </Dialog>
  </div>
</template>

<style scoped>
.page-wrap {
  padding: 16px;
  background: #f5f7fb;
}
.page-title {
  font-weight: 700;
  font-size: 18px;
  margin-bottom: 12px;
}
.box {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 12px;
}
.box-title {
  font-weight: 700;
  margin-bottom: 12px;
}
.form-grid-4 {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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
  grid-template-columns: 1.1fr 1.4fr;
  gap: 14px;
}
.list-box,
.detail-box {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 12px;
}
.sub-title {
  font-weight: 700;
  margin-bottom: 8px;
}
.detail-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.detail-title {
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.head-actions {
  display: flex;
  gap: 8px;
}
:deep(.list-box .p-datatable-tbody > tr:hover),
:deep(.detail-box .p-datatable-tbody > tr:hover) {
  background: #f9fafb;
  cursor: pointer;
  transition: background 120ms ease-in-out;
}
</style>
