<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import InputText from 'primevue/inputtext';
import InputGroup from 'primevue/inputgroup';
import Calendar from 'primevue/calendar';
import Button from 'primevue/button';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Dialog from 'primevue/dialog';
import Textarea from 'primevue/textarea';

/* 상태 */
const search = ref({ fromDate: null, toDate: null, vendorName: '', orderId: '' });
const orderList = ref([]);
const selectedOrders = ref([]);         // 항상 1개만 유지
const detailRows = ref([]);
const selectedDetailRows = ref([]);
const currentOrderId = ref(null);

/* 반려 모달 */
const rejectDialog = ref(false);
const rejectReason = ref('');

/* 유틸 */
function fmtDate(d) {
  if (!d) return '';
  const dt = typeof d === 'string' ? new Date(d) : d;
  return `${dt.getFullYear()}-${String(dt.getMonth() + 1).padStart(2, '0')}-${String(dt.getDate()).padStart(2, '0')}`;
}
function toNumber(n) {
  if (n === null || n === undefined) return 0;
  if (typeof n === 'number') return isFinite(n) ? n : 0;
  const cleaned = String(n).replace(/[^\d.-]/g, '');
  const num = Number(cleaned);
  return isFinite(num) ? num : 0;
}
function fmtCurrency(n) {
  const num = toNumber(n);
  try {
    return new Intl.NumberFormat('ko-KR', { useGrouping: true, maximumFractionDigits: 0 }).format(num) + '원';
  } catch {
    return String(num).replace(/\B(?=(\d{3})+(?!\d))/g, ',') + '원';
  }
}

/* 상세 로딩 */
async function loadDetails(row) {
  if (!row) {
    detailRows.value = [];
    selectedDetailRows.value = [];
    currentOrderId.value = null;
    return;
  }
  if (currentOrderId.value === row.orderId) {
    detailRows.value = [];
    selectedDetailRows.value = [];
    currentOrderId.value = null;
    return;
  }
  selectedDetailRows.value = [];
  const { data } = await axios.get('/api/approval/details', { params: { orderId: row.orderId } });
  const rows = Array.isArray(data?.data) ? data.data : [];
  detailRows.value = rows.map(d => {
    const price = toNumber(d.prodUnitPrice);
    const qty = toNumber(d.orderQty);
    return { ...d, prodUnitPrice: price, orderQty: qty, amount: price * qty };
  });
  currentOrderId.value = row.orderId;
}

/* 목록 조회 */
async function applySearch() {
  const params = {
    fromDate: search.value.fromDate ? fmtDate(search.value.fromDate) : '',
    toDate: search.value.toDate ? fmtDate(search.value.toDate) : '',
    companyName: search.value.vendorName || '',
    orderId: search.value.orderId || '',
    prodStatus: '대기',
    page: 1,
    size: 10
  };
  const { data } = await axios.get('/api/approval-list', { params });
  orderList.value = Array.isArray(data) ? data : (data?.data ?? []);
  detailRows.value = [];
  selectedOrders.value = [];
  selectedDetailRows.value = [];
  currentOrderId.value = null;
}
function resetSearch() {
  search.value = { fromDate: null, toDate: null, vendorName: '', orderId: '' };
  applySearch();
}

/* 선택 변경 */
async function onOrderSelectionChange(e) {
  const arr = Array.isArray(e?.value) ? e.value : [];
  if (!arr.length) {
    selectedOrders.value = [];
    await loadDetails(null);
    return;
  }
  const last = arr[arr.length - 1];
  selectedOrders.value = [last];
  await loadDetails(last);
}

/* 행 클릭 */
async function onOrderRowClick(e) {
  const row = e.data;
  selectedOrders.value = [row];
  await loadDetails(row);
}

/* 상세 체크박스 */
function toggleDetailSelection(row) {
  const idx = selectedDetailRows.value.findIndex(r => r.odetailId === row.odetailId);
  if (idx >= 0) selectedDetailRows.value.splice(idx, 1);
  else selectedDetailRows.value.push(row);
  return selectedDetailRows.value;
}

/* 승인 */
async function approveSelected() {
  if (!selectedDetailRows.value.length) {
    alert('승인할 상세를 선택하세요');
    return;
  }
  const odetailIds = selectedDetailRows.value.map(r => r.odetailId);
  const res = await axios.post('/api/approval/approve', { odetailIds });
  if (res?.status === 200 && res.data.retCode === 'success') {
    alert('승인 완료');
    await applySearch();
  } else {
    alert('승인 실패');
  }
}

/* 반려 모달 열기 */
function openRejectDialog() {
  if (!selectedDetailRows.value.length) {
    alert('반려할 상세를 선택하세요');
    return;
  }
  rejectReason.value = '';
  rejectDialog.value = true;
}

/* 반려 실행 */
async function confirmReject() {
  if (!rejectReason.value.trim()) {
    alert('반려 사유를 입력하세요');
    return;
  }
  const odetailIds = selectedDetailRows.value.map(r => r.odetailId);
  const res = await axios.post('/api/approval/reject', { odetailIds, reason: rejectReason.value });
  if (res?.status === 200 && res.data.retCode === 'success') {
    alert('반려 완료');
    rejectDialog.value = false;
    await applySearch();
  } else {
    alert('반려 실패');
  }
}

onMounted(() => applySearch());
</script>

<template>
  <div class="page-wrap">
    <div class="page-title">주문 승인</div>

    <!-- 검색폼 -->
    <div class="box">
      <div class="box-title">주문 승인 검색</div>
      <div class="form-grid-4">
        <div class="field">
          <label>주문 일자</label>
          <div class="flex gap-2">
            <Calendar v-model="search.fromDate" dateFormat="yy-mm-dd" showIcon class="w-full" />
            <Calendar v-model="search.toDate" dateFormat="yy-mm-dd" showIcon class="w-full" />
          </div>
        </div>
        <div class="field">
          <label>판매처명</label>
          <InputGroup>
            <InputText v-model="search.vendorName" placeholder="판매처명" />
          </InputGroup>
        </div>
        <div class="field">
          <label>주문번호</label>
          <InputGroup>
            <InputText v-model="search.orderId" placeholder="주문번호" />
          </InputGroup>
        </div>
      </div>
      <div class="actions">
        <Button label="초기화" icon="pi pi-refresh" @click="resetSearch" />
        <Button label="조회" icon="pi pi-search" @click="applySearch" />
      </div>
    </div>

    <div class="split">
      <!-- 목록 -->
      <div class="list-box">
        <div class="sub-title">승인 대기 주문 목록</div>
        <DataTable
          :value="orderList"
          dataKey="orderId"
          v-model:selection="selectedOrders"
          :metaKeySelection="false"
          @selection-change="onOrderSelectionChange"
          @row-click="onOrderRowClick"
          paginator :rows="10"
        >
          <Column selectionMode="multiple" />
          <Column field="orderDate" header="주문 일자" :body="(r) => fmtDate(r.orderDate)" />
          <Column field="companyName" header="판매처명" />
          <Column field="orderId" header="주문번호" />
          <Column field="prodStatus" header="상태" />
        </DataTable>
      </div>

      <!-- 상세 -->
      <div class="detail-box">
        <div class="detail-head">
          <div class="detail-title">주문 상세</div>
          <div class="head-actions">
            <Button label="선택 승인" icon="pi pi-check" @click="approveSelected" :disabled="!selectedDetailRows.length" />
            <Button label="부분 반려" icon="pi pi-times" class="p-button-danger"
              @click="openRejectDialog" :disabled="!selectedDetailRows.length" />
          </div>
        </div>
        <DataTable
          :value="detailRows"
          dataKey="odetailId"
          v-model:selection="selectedDetailRows"
          selectionMode="multiple"
          :metaKeySelection="false"
          @row-click="toggleDetailSelection($event.data)"
          paginator :rows="10"
        >
          <Column selectionMode="multiple" />
          <Column field="prodId" header="제품 번호" />
          <Column field="prodName" header="제품명" />
          <Column field="spec" header="규격" />
          <Column field="unit" header="단위" />
          <Column field="prodUnitPrice" header="단가">
            <template #body="{ data }">{{ fmtCurrency(data.prodUnitPrice) }}</template>
          </Column>
          <Column field="orderQty" header="수량" />
          <Column field="amount" header="금액">
            <template #body="{ data }">{{ fmtCurrency(data.amount) }}</template>
          </Column>
          <Column field="prodStatus" header="상태"/>

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
.form-grid-4 { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.field { display: flex; flex-direction: column; gap: 6px; }
.actions { display: flex; gap: 8px; justify-content: flex-end; margin-top: 8px; }
.split { display: grid; grid-template-columns: 1.1fr 1.4fr; gap: 14px; }
.list-box, .detail-box { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; padding: 12px; }
.sub-title { font-weight: 700; margin-bottom: 8px; }
.detail-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.detail-title { font-weight: 700; display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.head-actions { display: flex; gap: 8px; }
/* 목록/상세 둘 다 hover시 클릭 느낌 */
:deep(.list-box .p-datatable-tbody > tr:hover),
:deep(.detail-box .p-datatable-tbody > tr:hover) {
  background: #f9fafb;   /* 아주 옅은 하이라이트 */
  cursor: pointer;        /* 손가락 커서 */
  transition: background 120ms ease-in-out;
}
</style>
