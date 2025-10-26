<!-- ======================================================
📄 주문승인.vue (모달 포함 + 브레드크럼 추가 완전체)
====================================================== -->
<script setup>
import axios from 'axios';
import Button from 'primevue/button';
import Calendar from 'primevue/calendar';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import Dialog from 'primevue/dialog';
import InputGroup from 'primevue/inputgroup';
import InputText from 'primevue/inputtext';
import Textarea from 'primevue/textarea';
import { onMounted, ref, computed } from 'vue';
import Breadcrumb from 'primevue/breadcrumb';
import { useRoute } from 'vue-router';
import { useIcon } from '@/composables/useIcon';

/* ------------------ 상태 ------------------ */
const search = ref({ fromDate: null, toDate: null, vendorId: '', orderId: '' });
const orderList = ref([]);
const selectedOrders = ref([]);
const detailRows = ref([]);
const selectedDetailRows = ref([]);
const currentOrderId = ref(null);

/* ------------------ 반려 모달 ------------------ */
const rejectDialog = ref(false);
const rejectReason = ref('');

/* ------------------ 판매처 모달 ------------------ */
const vendorDialog = ref(false);
const vendorKeyword = ref('');
const vendorList = ref([]);
const vendorPage = ref(1);
const vendorRows = ref(10);
const vendorTotal = ref(0);

/* ------------------ 주문번호 모달 ------------------ */
const orderDialog = ref(false);
const orderKeyword = ref('');
const orderListModal = ref([]);
const orderPage = ref(1);
const orderRows = ref(10);
const orderTotal = ref(0);

/* ------------------ 날짜 모달 ------------------ */
const dateDialog = ref(false);
const dateList = ref([]);
const datePage = ref(1);
const dateRows = ref(10);
const dateTotal = ref(0);

async function openDateModal() {
  dateDialog.value = true;
  datePage.value = 1;
  await loadDateList();
}
async function loadDateList() {
  const { data } = await axios.get('/api/approval/modal/searchByDate', {
    params: {
      startDate: search.value.fromDate ? fmtDate(search.value.fromDate) : '',
      endDate: search.value.toDate ? fmtDate(search.value.toDate) : '',
      page: datePage.value,
      size: dateRows.value
    }
  });
  dateList.value = data.items ?? data.data ?? [];
  dateTotal.value = data.totalCount ?? 0;
}
function onDatePageChange(e) {
  datePage.value = e.page + 1;
  dateRows.value = e.rows;
  loadDateList();
}
function selectDateRow(e) {
  const row = e.data;
  search.value.orderId = row.orderId;
  dateDialog.value = false;
}

/* ------------------ 유틸 ------------------ */
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
  return new Intl.NumberFormat('ko-KR', { maximumFractionDigits: 0 }).format(num) + '원';
}

/* ------------------ 판매처 모달 ------------------ */
async function loadVendorList() {
  try {
    const { data } = await axios.get('/api/approval-modal/vendor', {
      params: { condition: vendorKeyword.value, page: vendorPage.value, size: vendorRows.value }
    });
    vendorList.value = data.items ?? data.data ?? [];
    vendorTotal.value = data.totalCount ?? 0;
  } catch {
    vendorList.value = [];
  }
}
async function openVendorModal() {
  vendorDialog.value = true;
  vendorPage.value = 1;
  await loadVendorList();
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

/* ------------------ 주문번호 모달 ------------------ */
async function loadOrderList() {
  try {
    const { data } = await axios.get('/api/approval-modal/order', {
      params: { condition: orderKeyword.value, page: orderPage.value, size: orderRows.value }
    });
    orderListModal.value = data.items ?? data.data ?? [];
    orderTotal.value = data.totalCount ?? 0;
  } catch {
    orderListModal.value = [];
  }
}
async function openOrderModal() {
  orderDialog.value = true;
  orderPage.value = 1;
  await loadOrderList();
}
function onOrderPageChange(e) {
  orderPage.value = e.page + 1;
  orderRows.value = e.rows;
  loadOrderList();
}
function selectOrder(e) {
  const o = e.data;
  search.value.orderId = o.orderId;
  orderDialog.value = false;
}

/* ------------------ 상세 로딩 ------------------ */
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
  detailRows.value = rows.map((d) => {
    const price = toNumber(d.prodUnitPrice);
    const qty = toNumber(d.orderQty);
    return { ...d, prodUnitPrice: price, orderQty: qty, amount: price * qty };
  });
  currentOrderId.value = row.orderId;
}

/* ------------------ 목록 조회 ------------------ */
async function applySearch() {
  const params = {
    startDate: search.value.fromDate ? fmtDate(search.value.fromDate) : '',
    endDate: search.value.toDate ? fmtDate(search.value.toDate) : '',
    vendorId: search.value.vendorId || '',
    orderId: search.value.orderId || ''
  };
  try {
    const { data } = await axios.get('/api/approval-list', { params });
    orderList.value = Array.isArray(data) ? data : (data?.data ?? []);
  } catch {
    orderList.value = [];
  }
  detailRows.value = [];
  selectedOrders.value = [];
  selectedDetailRows.value = [];
  currentOrderId.value = null;
}
function resetSearch() {
  search.value = { fromDate: null, toDate: null, vendorId: '', vendorName: '', orderId: '' };
  applySearch();
}

/* ------------------ 체크박스 단일 선택 ------------------ */
function toggleSingleSelect(row) {
  if (selectedOrders.value.length && selectedOrders.value[0].orderId === row.orderId) {
    selectedOrders.value = [];
    loadDetails(null);
    return;
  }
  selectedOrders.value = [row];
  loadDetails(row);
}

/* ------------------ 승인 / 반려 ------------------ */
async function approveSelected() {
  if (!selectedDetailRows.value.length) return alert('승인할 상세를 선택하세요');
  const odetailIds = selectedDetailRows.value.map((r) => r.odetailId);
  const res = await axios.post('/api/approval/approve', { odetailIds });
  if (res?.status === 200 && res.data.retCode === 'success') {
    alert('승인 완료');
    await applySearch();
  } else alert('승인 실패');
}
function openRejectDialog() {
  if (!selectedDetailRows.value.length) return alert('반려할 상세를 선택하세요');
  rejectReason.value = '';
  rejectDialog.value = true;
}
async function confirmReject() {
  if (!rejectReason.value.trim()) return alert('반려 사유를 입력하세요');
  const odetailIds = selectedDetailRows.value.map((r) => r.odetailId);
  const res = await axios.post('/api/approval/reject', { odetailIds, reason: rejectReason.value });
  if (res?.status === 200 && res.data.retCode === 'success') {
    alert('반려 완료');
    rejectDialog.value = false;
    await applySearch();
  } else alert('반려 실패');
}
onMounted(() => applySearch());

/* ------------------ 브레드크럼 ------------------ */
const route = useRoute();
const breadcrumbHome = { icon: useIcon('home'), to: '/' };
const breadcrumbItems = computed(() => {
  const matched = route.matched.filter((r) => r.meta);
  if (!matched.length) return [];
  const current = matched[matched.length - 1];
  const parentLabel = current.meta?.breadcrumb?.parent || '';
  const currentLabel = current.name || '';
  return [{ label: parentLabel }, { label: currentLabel, to: route.fullPath }];
});
</script>

<template>
  <div class="page-wrap">
    <!-- ✅ 브레드크럼 -->
    <Breadcrumb class="rounded-lg mb-3" :home="breadcrumbHome" :model="breadcrumbItems" />

    <div class="page-title"></div>

    <!-- 검색폼 -->
    <div class="box">
      <div class="box-title">주문 승인 검색</div>
      <div class="form-grid-4">
        <div class="field">
          <label>주문 일자</label>
          <div class="flex items-center gap-2">
            <Calendar v-model="search.fromDate" dateFormat="yy-mm-dd" showIcon class="w-full" />
            <span>~</span>
            <Calendar v-model="search.toDate" dateFormat="yy-mm-dd" showIcon class="w-full" />
            <!-- <Button icon="pi pi-search" class="p-button-text" @click="openDateModal" /> -->
          </div>
        </div>
        <div class="field">
          <label>판매처명</label>
          <InputGroup>
            <InputText v-model="search.vendorName" placeholder="판매처명" readonly @click="openVendorModal" />
            <Button icon="pi pi-search" @click.stop="openVendorModal" />
          </InputGroup>
        </div>
        <div class="field">
          <label>주문번호</label>
          <InputGroup>
            <InputText v-model="search.orderId" placeholder="주문번호" readonly @click="openOrderModal" />
            <Button icon="pi pi-search" @click.stop="openOrderModal" />
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
        <DataTable :value="orderList" dataKey="orderId" paginator :rows="10" @row-click="(e) => toggleSingleSelect(e.data)">
          <Column headerStyle="width:3rem; text-align:center;">
            <template #body="{ data }">
              <div class="p-checkbox p-component custom-checkbox">
                <input type="checkbox" class="p-checkbox-box" :checked="selectedOrders.some((o) => o.orderId === data.orderId)" @change="() => toggleSingleSelect(data)" />
              </div>
            </template>
          </Column>
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
            <Button label="부분 반려" icon="pi pi-times" class="p-button-danger" @click="openRejectDialog" :disabled="!selectedDetailRows.length" />
          </div>
        </div>
        <DataTable :value="detailRows" dataKey="odetailId" v-model:selection="selectedDetailRows" selectionMode="multiple" :metaKeySelection="false" @row-click="toggleDetailSelection($event.data)" paginator :rows="10">
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
          <Column field="prodStatus" header="상태" />
        </DataTable>
      </div>
    </div>

    <!-- 날짜 모달 -->

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
        <Button icon="pi pi-search" @click.stop="loadVendorList" />
      </div>
      <DataTable :value="vendorList" dataKey="vendorId" selectionMode="single" @row-dblclick="selectVendor" paginator :rows="vendorRows" :totalRecords="vendorTotal" @page="onVendorPageChange">
        <Column field="companyName" header="판매처명" />
      </DataTable>
    </Dialog>

    <!-- 주문번호 모달 -->
    <Dialog v-model:visible="orderDialog" header="주문번호 검색" modal closable :style="{ width: '600px' }">
      <div class="p-inputgroup mb-3">
        <InputText v-model="orderKeyword" placeholder="주문번호 검색" @keyup.enter="loadOrderList" />
        <Button icon="pi pi-search" @click.stop="loadOrderList" />
      </div>
      <DataTable :value="orderListModal" dataKey="orderId" selectionMode="single" @row-dblclick="selectOrder" paginator :rows="orderRows" :totalRecords="orderTotal" @page="onOrderPageChange">
        <Column field="orderId" header="주문번호" />
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

/* ✅ 상세와 동일한 초록 체크박스 */
:deep(.custom-checkbox) {
  display: flex;
  justify-content: center;
  align-items: center;
}
:deep(.custom-checkbox .p-checkbox-box) {
  width: 18px;
  height: 18px;
  border: 1px solid #ced4da;
  border-radius: 3px;
  background: #fff;
  cursor: pointer;
  transition:
    background 0.15s,
    border-color 0.15s;
  appearance: none;
  outline: none;
}
:deep(.custom-checkbox .p-checkbox-box:checked) {
  background: #16a34a;
  border-color: #16a34a;
}
:deep(.custom-checkbox .p-checkbox-box:checked::after) {
  content: '';
  position: absolute;
  width: 4px;
  height: 9px;
  border: solid #fff;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
  top: 2px;
  left: 6px;
}
</style>
