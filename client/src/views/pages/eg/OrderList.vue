<template>
  <div class="order-list">
    <!-- 헤더 -->
    <div class="header">
      <h2>주문 조회 (판매처)</h2>
      <div class="header-actions">
        <Button label="초기화" icon="pi pi-refresh" class="p-button-outlined" @click="resetFilters" />
        <Button label="조회" icon="pi pi-search" class="p-button-primary" @click="fetchOrders" />
        <Button label="엑셀 다운로드" icon="pi pi-file-excel" class="p-button-success" @click="exportExcel" />
        <Button label="PDF 출력" icon="pi pi-file-pdf" class="p-button-danger" @click="exportPDF" />
      </div>
    </div>

    <!-- 검색 영역 -->
    <div class="filter-section">
      <!-- 주문일자 -->
      <div class="filter-group">
        <span class="filter-label">주문일자</span>
        <DatePicker v-model="filters.startDate" placeholder="시작일" dateFormat="yy-mm-dd" showIcon />
        <span>~</span>
        <DatePicker v-model="filters.endDate" placeholder="종료일" dateFormat="yy-mm-dd" showIcon />
      </div>

      <!-- 제품명 -->
      <div class="filter-group">
        <span class="filter-label">제품명</span>
        <InputText v-model="filters.prodName" placeholder="제품명 검색" />
      </div>

      <!-- 주문 상태 -->
      <div class="filter-group">
        <span class="filter-label">주문상태</span>
        <div class="status-buttons">
          <Button label="대기" :class="{selected: filters.status === '대기'}" @click="selectStatus('대기')" />
          <Button label="승인" :class="{selected: filters.status === '승인'}" @click="selectStatus('승인')" />
          <Button label="배송중" :class="{selected: filters.status === '배송중'}" @click="selectStatus('배송중')" />
          <Button label="배송완료" :class="{selected: filters.status === '배송완료'}" @click="selectStatus('배송완료')" />
        </div>
      </div>

      <!-- 주문번호 -->
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
          dataKey="orderId"
          paginator
          :rows="10"
          responsiveLayout="scroll"
          resizableColumns
          columnResizeMode="fit"
          class="custom-table"
        >
          <Column field="orderId" header="주문번호" style="width:140px; text-align:center;" />
          <Column field="orderDate" header="주문일자" style="width:140px; text-align:center;" />
          <Column field="prodName" header="대표 제품명" style="width:200px;" />

          <Column field="totalPrice" header="주문총액(원)" style="width:150px; text-align:right;">
            <template #body="slotProps">
              {{ formatCurrency(slotProps.data.totalPrice) }}
            </template>
          </Column>

          <Column field="deliveryDate" header="배송예정일" style="width:140px; text-align:center;" />
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
          <Column field="prodId" header="제품코드" style="width:120px; text-align:center;" />
          <Column field="prodName" header="제품명" style="width:150px;" />
          <Column field="spec" header="규격" style="width:100px; text-align:center;" />
          <Column field="unit" header="단위" style="width:80px; text-align:center;" />

          <Column field="prodUnitPrice" header="제품단가" style="width:120px; text-align:right;">
            <template #body="slotProps">
              {{ formatCurrency(slotProps.data.prodUnitPrice) }}
            </template>
          </Column>

          <Column field="orderQty" header="주문수량" style="width:80px; text-align:center;" />

          <Column field="totalUnitPrice" header="합계" style="width:120px; text-align:right;">
            <template #body="slotProps">
              {{ formatCurrency(slotProps.data.totalUnitPrice) }}
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
import { ref, computed, watch, onMounted } from 'vue'
import axios from 'axios'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import DatePicker from 'primevue/datepicker'
import { useUserStore } from '@/stores/user';

const userStore = useUserStore()

// ===== 검색 조건 =====
const filters = ref({
  startDate: null,
  endDate: null,
  prodName: '',
  status: '',
  orderId: ''
})

// ===== 주문 목록 =====
const orders = ref([])

// ===== 선택된 주문 =====
const selectedOrder = ref(null)

// ===== 주문 상세 =====
const orderDetails = ref([])

// ===== 합계 계산 =====
const detailTotal = computed(() => {
  return orderDetails.value.reduce((acc, item) => acc + (item.totalUnitPrice || 0), 0)
})

// ===== 금액 포맷 =====
const formatCurrency = (value) => {
  if (value === null || value === undefined) return '0 원'
  return value.toLocaleString('ko-KR') + ' 원'
}

// ===== 날짜 포맷 =====
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// ===== 배송 예정일 계산 =====
const addDays = (dateString, days) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  date.setDate(date.getDate() + days)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// ===== API 조회용 날짜 포맷 =====
const formatDateForAPI = (date) => {
  if (!date) return null
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// ===== 주문 상태 선택 =====
const selectStatus = (status) => {
  filters.value.status = status
  fetchOrders()
}

// ===== 주문 목록 조회 =====
const fetchOrders = async () => {
  try {
    const params = {
      ...filters.value,
      startDate: formatDateForAPI(filters.value.startDate),
      endDate: formatDateForAPI(filters.value.endDate)
    }

    const { data } = await axios.get('/api/orderlist', { params })

    if (data.status === 'success') {
      orders.value = data.orders.map(item => ({
        orderId: item.orderId,
        orderDate: formatDate(item.orderDate),
        prodName: item.prodName || '-',
        totalPrice: item.totalPrice || 0,
        deliveryDate: addDays(item.orderDate, 2),
        status: item.status || '-'
      }))
    } else {
      alert(data.message || '주문 목록을 불러오지 못했습니다.')
    }
  } catch (error) {
    console.error('❌ 주문 목록 조회 오류:', error)
    alert('서버 오류로 주문 목록을 불러올 수 없습니다.')
  }
}

// ===== 주문 상세 조회 =====
const fetchOrderDetail = async (orderId) => {
  try {
    const { data } = await axios.get(`/api/orders/${orderId}/details`)

    if (data.status === 'success') {
      orderDetails.value = data.details.map(item => ({
        ...item,
        totalUnitPrice: item.prodUnitPrice * item.orderQty
      }))
    } else {
      alert(data.message || '주문 상세 데이터를 불러오지 못했습니다.')
    }
  } catch (error) {
    console.error('❌ 주문 상세 조회 오류:', error)
    alert('서버 오류로 상세 데이터를 불러올 수 없습니다.')
  }
}

// ===== 선택된 주문 변경 감시 =====
watch(
  () => selectedOrder.value,
  (newOrder) => {
    if (newOrder && newOrder.orderId) {
      fetchOrderDetail(newOrder.orderId)
    } else {
      orderDetails.value = []
    }
  }
)



// ===== PDF 출력 =====
const exportPDF = () => {
  alert('PDF 출력 기능은 아직 구현되지 않았습니다.')
}


// ===== 초기화 =====
const resetFilters = () => {
  filters.value = {
    startDate: null,
    endDate: null,
    prodName: '',
    status: '',
    orderId: ''
  }
  fetchOrders()
}

onMounted(() => {
  fetchOrders()
})
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
  background: #007ad9 !important;
  color: white !important;
}

.content-section {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.order-list-table,
.order-detail-table {
  flex: 1;
  min-width: 400px;
}

.total-footer {
  margin-top: 10px;
  text-align: right;
  font-size: 1.1rem;
  font-weight: bold;
  color: #333;
}

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

.header-actions .p-button-success {
  background-color: #28a745;
  border: none;
}

.header-actions .p-button-danger {
  background-color: #dc3545;
  border: none;
}
</style>
