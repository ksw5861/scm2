<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import InputText from 'primevue/inputtext';
import InputGroup from 'primevue/inputgroup';
import Calendar from 'primevue/calendar';
import Button from 'primevue/button';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';

/* 상태 */
const search = ref({ fromDate: null, toDate: null, vendorName: '', orderId: '' });
const orderList = ref([]); // 주문 헤더 목록 (orderId 단위)
const selectedOrders = ref([]); // 선택된 주문 헤더들
const detailRows = ref([]); // 누적된 상세
const selectedDetailRows = ref([]); // 상세에서 선택된 행들

/* 유틸 */
function fmtDate(d) {
  if (!d) return '';
  const dt = typeof d === 'string' ? new Date(d) : d;
  return `${dt.getFullYear()}-${String(dt.getMonth() + 1).padStart(2, '0')}-${String(dt.getDate()).padStart(2, '0')}`;
}
function fmtCurrency(n) {
  return new Intl.NumberFormat('ko-KR').format(Number(n) || 0);
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
    size: 7
  };

  const { data } = await axios.get('/api/approval-list', { params });
  orderList.value = Array.isArray(data) ? data : (data?.data ?? []);

  detailRows.value = [];
  selectedOrders.value = [];
  selectedDetailRows.value = [];
}
function resetSearch() {
  search.value = { fromDate: null, toDate: null, vendorName: '', orderId: '' };
  applySearch();
}

/* 행 클릭 */
async function onRowClick(e) {
  const row = e.data;
  const exists = selectedOrders.value.find((r) => r.orderId === row.orderId);

  if (exists) {
    // 해제
    selectedOrders.value = selectedOrders.value.filter((r) => r.orderId !== row.orderId);
    detailRows.value = detailRows.value.filter((r) => r.orderId !== row.orderId);
  } else {
    // 선택 → 상세 불러오기
    selectedOrders.value.push(row);
    await selectOrder(row);
  }
}

/* 상세 조회 */
async function selectOrder(row) {
  const { data } = await axios.get('/order/approval/details', {
    params: { orderId: row.orderId }
  });
  const raw = Array.isArray(data) ? data : (data?.data ?? []);
  raw.forEach((x) => {
    if (!detailRows.value.find((r) => r.odetailId === x.odetailId)) {
      const price = Number(x.prodUnitPrice || 0);
      const qty = Number(x.orderQty || 0);
      detailRows.value.push({
        odetailId: x.odetailId,
        orderId: x.orderId,
        prodId: x.prodId,
        prodName: x.prodName,
        spec: x.spec,
        unit: x.unit,
        prodUnitPrice: price,
        orderQty: qty,
        amount: price * qty
      });
    }
  });
}

/* 승인 */
async function approveSelected() {
  if (!selectedDetailRows.value.length) {
    alert('승인할 상세를 선택하세요');
    return;
  }
  const odetailIds = selectedDetailRows.value.map((r) => r.odetailId);
  const body = { odetailIds };

  const res = await axios.post('/order/approval/approve', body);
  if (res?.status === 200) {
    alert('승인 완료');
    await applySearch();
  } else {
    alert('승인 실패');
  }
}

/* 페이지 시작 */
onMounted(() => applySearch());
</script>

<template>
  <div class="page-wrap">
    <div class="page-title">주문 승인</div>

    <!-- ✅ 검색폼 -->
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

    <!-- 목록 + 상세 -->
    <div class="split">
      <!-- 목록 -->
      <div class="list-box">
        <div class="sub-title">승인 대기 주문 목록</div>
        <DataTable :value="orderList" dataKey="orderId" selectionMode="multiple" v-model:selection="selectedOrders" :metaKeySelection="false" @row-click="onRowClick" paginator :rows="7">
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
          </div>
        </div>
        <DataTable :value="detailRows" dataKey="odetailId" :selection="selectedDetailRows" @update:selection="(val) => (selectedDetailRows.value = val)" selectionMode="multiple" paginator :rows="8">
          <Column selectionMode="multiple" />
          <Column field="prodId" header="제품 번호" />
          <Column field="prodName" header="제품명" />
          <Column field="spec" header="규격" />
          <Column field="unit" header="단위" />
          <Column field="prodUnitPrice" header="단가" :body="(d) => fmtCurrency(d.prodUnitPrice)" />
          <Column field="orderQty" header="수량" />
          <Column field="amount" header="금액" :body="(d) => fmtCurrency(d.amount)" />
        </DataTable>
      </div>
    </div>
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
</style>
