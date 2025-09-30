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
const searchForm = ref({
  prodId: '',
  prodName: '',
  custName: '',
  expDate: null
});

// 목록/상세
const returnList = ref([]);          // 반품 목록
const selectedReturns = ref([]);     // 선택된 반품 (좌측)
const detailRows = ref([]);          // 상세 데이터 (우측)
const selectedDetailRows = ref([]);  // 상세 선택
const currentReturnId = ref(null);

// 반려 모달
const rejectDialog = ref(false);
const rejectReason = ref('');

/* ------------------ 유틸 ------------------ */
function fmtDate(d) {
  if (!d) return '';
  const dt = new Date(d);
  return `${dt.getFullYear()}-${String(dt.getMonth() + 1).padStart(2, '0')}-${String(dt.getDate()).padStart(2, '0')}`;
}
const nf = (n) => new Intl.NumberFormat('ko-KR').format(Number(n) || 0);

/* ------------------ 목록 조회 ------------------ */
async function applySearch() {
  const params = {
    productCode: searchForm.value.prodId || '',
    productName: searchForm.value.prodName || '',
    customerName: searchForm.value.custName || '',
    expDate: searchForm.value.expDate ? fmtDate(searchForm.value.expDate) : '',
    page: 1, size: 10
  };
  const { data } = await axios.get('/api/return-list', { params });
  returnList.value = Array.isArray(data) ? data : (data?.data ?? []);
  detailRows.value = [];
  selectedReturns.value = [];
  selectedDetailRows.value = [];
  currentReturnId.value = null;
}
function resetSearch() {
  searchForm.value = { prodId: '', prodName: '', custName: '', expDate: null };
  applySearch();
}

/* ------------------ 상세 조회 ------------------ */
async function loadDetails(row) {
  if (!row) {
    detailRows.value = [];
    selectedDetailRows.value = [];
    currentReturnId.value = null;
    return;
  }
  if (currentReturnId.value === row.rtnId) {
    detailRows.value = [];
    selectedDetailRows.value = [];
    currentReturnId.value = null;
    return;
  }
  const { data } = await axios.get('/api/return-details', { params: { rtnId: row.rtnId } });
  detailRows.value = Array.isArray(data) ? data : (data?.data ?? []);
  currentReturnId.value = row.rtnId;
}

/* ------------------ 목록 선택 ------------------ */
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
async function onReturnRowClick(e) {
  const row = e.data;
  selectedReturns.value = [row];
  await loadDetails(row);
}

/* ------------------ 상세 선택 ------------------ */
function toggleDetailSelection(row) {
  const idx = selectedDetailRows.value.findIndex(r => r.detailId === row.detailId);
  if (idx >= 0) selectedDetailRows.value.splice(idx, 1);
  else selectedDetailRows.value.push(row);
}

/* ------------------ 승인/반려 ------------------ */
async function approveSelected() {
  if (!selectedDetailRows.value.length) {
    alert('승인할 항목을 선택하세요');
    return;
  }
  const ids = selectedDetailRows.value.map(r => r.detailId);
  const res = await axios.post('/api/return/approve', { ids });
  if (res?.status === 200 && res.data.retCode === 'success') {
    alert('승인 완료');
    await applySearch();
  } else alert('승인 실패');
}

function openRejectDialog() {
  if (!selectedDetailRows.value.length) {
    alert('반려할 항목을 선택하세요');
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
  const ids = selectedDetailRows.value.map(r => r.detailId);
  const res = await axios.post('/api/return/reject', { ids, reason: rejectReason.value });
  if (res?.status === 200 && res.data.retCode === 'success') {
    alert('반려 완료');
    rejectDialog.value = false;
    await applySearch();
  } else alert('반려 실패');
}

/* ------------------ 모달 열기 ------------------ */
function openProdModal() {
  alert('제품 검색 모달 오픈');
}
function openCustModal() {
  alert('거래처 검색 모달 오픈');
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
        <!-- 제품명 -->
        <div class="field">
          <label>제품명</label>
          <InputGroup>
            <InputText
              v-model="searchForm.prodName"
              placeholder="제품명"
              readonly
              @click="openProdModal"
            />
            <InputGroupAddon>
              <Button icon="pi pi-search" class="p-button-text p-button-plain" @click="openProdModal" />
            </InputGroupAddon>
          </InputGroup>
        </div>

        <!-- 제품코드 -->
        <div class="field">
          <label>제품코드</label>
          <InputGroup>
            <InputText
              v-model="searchForm.prodId"
              placeholder="제품코드"
              readonly
              @click="openProdModal"
            />
            <InputGroupAddon>
              <Button icon="pi pi-search" class="p-button-text p-button-plain" @click="openProdModal" />
            </InputGroupAddon>
          </InputGroup>
        </div>

        <!-- 거래처명 -->
        <div class="field">
          <label>거래처명</label>
          <InputGroup>
            <InputText
              v-model="searchForm.custName"
              placeholder="거래처명"
              readonly
              @click="openCustModal"
            />
            <InputGroupAddon>
              <Button icon="pi pi-search" class="p-button-text p-button-plain" @click="openCustModal" />
            </InputGroupAddon>
          </InputGroup>
        </div>

        <!-- 유통기한 -->
        <div class="field">
          <label>유통기한</label>
          <Calendar v-model="searchForm.expDate" dateFormat="yy-mm-dd" showIcon class="w-full" />
        </div>
      </div>
      <div class="actions">
        <Button label="초기화" icon="pi pi-refresh" @click="resetSearch" />
        <Button label="조회" icon="pi pi-search" @click="applySearch" />
      </div>
    </div>

    <!-- 목록 + 상세 -->
    <div class="split">
      <!-- 목록 -->
      <div class="list-box">
        <div class="sub-title">반품 목록</div>
        <DataTable
          :value="returnList"
          dataKey="rtnId"
          v-model:selection="selectedReturns"
          :metaKeySelection="false"
          @selection-change="onReturnSelectionChange"
          @row-click="onReturnRowClick"
          paginator :rows="10"
        >
          <Column selectionMode="multiple" />
          <Column field="returnDate" header="반품일자" :body="(r)=>fmtDate(r.returnDate)" />
          <Column field="customerName" header="판매처명" />
          <Column field="returnCode" header="반품코드" />
          <Column field="status" header="상태" />
        </DataTable>
      </div>

      <!-- 상세 -->
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
          dataKey="detailId"
          v-model:selection="selectedDetailRows"
          selectionMode="multiple"
          :metaKeySelection="false"
          @row-click="toggleDetailSelection($event.data)"
          paginator :rows="10"
        >
          <Column selectionMode="multiple" />
          <Column field="productCode" header="제품코드" />
          <Column field="productName" header="제품명" />
          <Column field="expDate" header="유통기한" :body="(r)=>fmtDate(r.expDate)" />
          <Column field="manager" header="담당자" />
          <Column field="qty" header="반품수량" />
          <Column field="reason" header="사유" />
          <Column field="processedAt" header="처리일자" :body="(r)=>fmtDate(r.processedAt)" />
          <Column field="returnAmount" header="반품금액">
            <template #body="{data}">{{ nf(data.returnAmount) }}</template>
          </Column>
          <Column field="status" header="상태" />
        </DataTable>
      </div>
    </div>

    <!-- 반려 모달 -->
    <Dialog v-model:visible="rejectDialog" modal header="반려 사유 입력" :style="{ width: '400px' }">
      <Textarea v-model="rejectReason" rows="5" class="w-full" placeholder="반려 사유를 입력해 주세요" />
      <template #footer>
        <Button label="취소" icon="pi pi-times" class="p-button-text" @click="rejectDialog=false" />
        <Button label="반려" icon="pi pi-check" class="p-button-danger" @click="confirmReject" />
      </template>
    </Dialog>
  </div>
</template>

<style scoped>
.page-wrap { padding: 16px; background: #f5f7fb; }
.page-title { font-weight: 700; font-size: 18px; margin-bottom: 12px; }
.box { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; padding: 16px; margin-bottom: 12px; }
.box-title { font-weight: 700; margin-bottom: 12px; }
.form-grid-4 { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.field { display: flex; flex-direction: column; gap: 6px; }
.actions { display: flex; gap: 8px; justify-content: flex-end; margin-top: 8px; }
.split { display: grid; grid-template-columns: 1.1fr 1.4fr; gap: 14px; }
.list-box, .detail-box { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; padding: 12px; }
.sub-title { font-weight: 700; margin-bottom: 8px; }
.detail-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.detail-title { font-weight: 700; display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.head-actions { display: flex; gap: 8px; }
:deep(.list-box .p-datatable-tbody > tr:hover),
:deep(.detail-box .p-datatable-tbody > tr:hover) {
  background: #f9fafb;
  cursor: pointer;
  transition: background 120ms ease-in-out;
}
</style>
