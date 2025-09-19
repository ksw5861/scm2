<template>
  <div class="order-list">
    <!-- 헤더 -->
    <div class="header">
      <h2>주문 조회 (판매처)</h2>
      <div class="header-actions">
        <Button label="초기화" icon="pi pi-refresh" class="p-button-outlined" @click="resetFilters" />
        <Button label="조회" icon="pi pi-search" class="p-button-primary" @click="fetchOrders" />
      </div>
    </div>

    <!-- 검색 영역 -->
    <div class="filter-section">
      <div class="filter-group">
        <span class="filter-label">주문일자</span>
        <Calendar v-model="filters.startDate" placeholder="시작일" dateFormat="yy-mm-dd" showIcon />
        <span>~</span>
        <Calendar v-model="filters.endDate" placeholder="종료일" dateFormat="yy-mm-dd" showIcon />
      </div>

      <div class="filter-group">
        <span class="filter-label">제품명</span>
        <InputText v-model="filters.productName" placeholder="제품명 검색" />
      </div>

      <div class="filter-group">
        <span class="filter-label">주문상태</span>
        <div class="status-buttons">
          <Button label="대기" :class="{'selected': filters.status === '대기'}" @click="filters.status = '대기'" />
          <Button label="승인" :class="{'selected': filters.status === '승인'}" @click="filters.status = '승인'" />
          <Button label="배송중" :class="{'selected': filters.status === '배송중'}" @click="filters.status = '배송중'" />
          <Button label="배송완료" :class="{'selected': filters.status === '배송완료'}" @click="filters.status = '배송완료'" />
        </div>
      </div>

      <div class="filter-group">
        <span class="filter-label">주문번호</span>
        <InputText v-model="filters.orderId" placeholder="주문번호 검색" />
      </div>
    </div>

    <!-- 목록 & 상세 -->
    <div class="content-section">
      <!-- 좌측 주문 목록 -->
      <div class="order-list-table">
        <DataTable
          :value="orders"
          selectionMode="single"
          v-model:selection="selectedOrder"
          @rowSelect="onOrderSelect"
          paginator
          :rows="10"
          responsiveLayout="scroll"
          resizableColumns
          columnResizeMode="fit"
          class="custom-table"
        >
          <Column field="order_id" header="주문번호" style="width:120px; text-align:center;" />
          <Column field="order_date" header="주문일자" style="width:140px; text-align:center;" />
          <Column field="prod_name" header="제품명" style="width:200px;" />
          <Column field="total_price" header="주문총액(원)" style="width:150px; text-align:right;">
            <template #body="slotProps">
              {{ formatCurrency(slotProps.data.total_price) }}
            </template>
          </Column>
          <Column field="delivery_date" header="배송예정일" style="width:140px; text-align:center;" />
          <Column field="status" header="상태" style="width:120px; text-align:center;" />
        </DataTable>
      </div>

      <!-- 우측 주문 상세 -->
      <div class="order-detail-table">
        <DataTable
          :value="orderDetails"
          responsiveLayout="scroll"
          resizableColumns
          columnResizeMode="fit"
          class="custom-table"
        >
          <Column field="prod_id" header="제품코드" style="width:120px; text-align:center;" />
          <Column field="prod_name" header="제품명" style="width:150px;" />
          <Column field="prod_spec" header="규격" style="width:100px; text-align:center;" />
          <Column field="prod_unit" header="단위" style="width:80px; text-align:center;" />
          <Column field="prod_price" header="제품가격" style="width:120px; text-align:right;">
            <template #body="slotProps">
              {{ formatCurrency(slotProps.data.prod_price) }}
            </template>
          </Column>
          <Column field="order_qty" header="주문수량" style="width:80px; text-align:center;" />
          <Column field="total" header="합계" style="width:120px; text-align:right;">
            <template #body="slotProps">
              {{ formatCurrency(slotProps.data.total) }}
            </template>
          </Column>
        </DataTable>

        <!-- 합계 금액 -->
        <div class="total-footer">
          합계금액 : <strong>{{ formatCurrency(detailTotal) }}</strong>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Calendar from 'primevue/calendar'

// 검색 필터
const filters = ref({
  startDate: null,
  endDate: null,
  productName: '',
  status: '',
  orderId: ''
})

// 주문 목록 데이터
const orders = ref([
  { order_id: 'AB-001', order_date: '2025-09-11', prod_name: '아라비카원두 더블', total_price: 2160000, delivery_date: '2025-09-20', status: '대기' },
  { order_id: 'AB-002', order_date: '2025-09-10', prod_name: '리베리카 원두', total_price: 500000, delivery_date: '2025-09-19', status: '대기' },
  { order_id: 'AB-003', order_date: '2025-09-10', prod_name: '로부스타 원두', total_price: 1000000, delivery_date: '2025-09-19', status: '승인' },
  { order_id: 'AB-004', order_date: '2025-09-05', prod_name: '로부스타 원두', total_price: 1000000, delivery_date: '2025-09-19', status: '배송중' },
  { order_id: 'AB-005', order_date: '2025-09-04', prod_name: '로부스타 원두', total_price: 1000000, delivery_date: '2025-09-19', status: '배송완료' }
])

// 선택된 주문
const selectedOrder = ref(null)

// 주문 상세 데이터
const orderDetails = ref([])

// 합계 계산
const detailTotal = computed(() => {
  return orderDetails.value.reduce((acc, item) => acc + item.total, 0)
})

// 금액 포맷팅
const formatCurrency = (value) => {
  return value.toLocaleString('ko-KR') + ' 원'
}

// 주문 선택 시 상세 조회
const onOrderSelect = () => {
  if (!selectedOrder.value) return

  // 실제 API 호출을 통해 상세내역 로딩 예정
  orderDetails.value = [
    { prod_id: 'AB-012', prod_name: '아라비카원두', prod_spec: '1 kg 팩', prod_unit: 'Box(24팩)', prod_price: 480000, order_qty: 2, total: 960000 },
    { prod_id: 'AB-013', prod_name: '온컵300미리', prod_spec: '100ea 줄', prod_unit: 'Box(30줄)', prod_price: 50000, order_qty: 4, total: 200000 },
    { prod_id: 'AB-014', prod_name: '냉컵300미리', prod_spec: '100ea 줄', prod_unit: 'Box(30줄)', prod_price: 50000, order_qty: 4, total: 200000 },
    { prod_id: 'AB-015', prod_name: '냉컵500미리', prod_spec: '100ea 줄', prod_unit: 'Box(30줄)', prod_price: 50000, order_qty: 4, total: 200000 },
    { prod_id: 'AB-016', prod_name: '냅킨', prod_spec: '100ea 팩', prod_unit: 'Box(60팩)', prod_price: 30000, order_qty: 5, total: 150000 },
    { prod_id: 'AB-017', prod_name: '설탕시럽', prod_spec: '500ml 병', prod_unit: 'Box(12병)', prod_price: 120000, order_qty: 1, total: 120000 },
    { prod_id: 'AB-018', prod_name: '녹차 파우더', prod_spec: '1 kg 팩', prod_unit: '팩(1kg)', prod_price: 30000, order_qty: 1, total: 30000 }
  ]
}

// 초기화
const resetFilters = () => {
  filters.value = {
    startDate: null,
    endDate: null,
    productName: '',
    status: '',
    orderId: ''
  }
}

// 조회
const fetchOrders = () => {
  console.log('조회 조건:', filters.value)
  // 실제 API 연동 예정
}
</script>

<style scoped>
.order-list {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.filter-section {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 15px;
  padding: 10px;
  border: 1px solid #dcdcdc;
  border-radius: 8px;
  background: #f9f9f9;
  margin-bottom: 20px;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-label {
  font-weight: 600;
  min-width: 70px;
  text-align: right;
}

.status-buttons button {
  margin-right: 5px;
}

.status-buttons .selected {
  background: #007ad9;
  color: white;
}

.content-section {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

/* 좌측/우측 테이블 */
.order-list-table,
.order-detail-table {
  flex: 1;
  min-width: 400px;
}

/* 합계 */
.total-footer {
  margin-top: 10px;
  text-align: right;
  font-size: 1.1rem;
  font-weight: bold;
  color: #333;
}

/* ===== 공통 테이블 스타일 ===== */
.custom-table {
  width: 100%;
  font-size: 0.95rem;
}

::v-deep(.p-datatable-thead > tr > th) {
  border-right: 1px solid #e5e7eb;
  background: #f9fafb;
  font-weight: 600;
  text-align: center !important;
  padding: 10px 0;
}

::v-deep(.p-datatable-tbody > tr > td) {
  border-right: 1px solid #f0f0f0;
  text-align: center;
  padding: 8px 10px;
}

::v-deep(.p-datatable-wrapper) {
  overflow-x: auto;
}

/* ===== 반응형 ===== */
@media (max-width: 768px) {
  .filter-section {
    flex-direction: column;
    align-items: flex-start;
  }

  .order-list-table,
  .order-detail-table {
    min-width: 100%;
  }
}
</style>
