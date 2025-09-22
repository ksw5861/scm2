<template>
  <div class="order-list">
    <!-- 헤더 -->
    <div class="header">
      <h2>주문 조회 (판매처)</h2>
      <div class="header-actions">
        <Button label="초기화" icon="pi pi-refresh" class="p-button-outlined" @click="resetFilters" />
        <Button label="조회" icon="pi pi-search" class="p-button-primary" @click="fetchOrders" />
        <!-- 엑셀 다운로드 버튼 -->
        <Button label="엑셀 다운로드" icon="pi pi-file-excel" class="p-button-success" @click="exportExcel" />

        <!-- PDF 다운로드 버튼 -->
        <Button label="PDF 출력" icon="pi pi-file-pdf" class="p-button-danger" @click="exportPDF" />
      </div>
    </div>
    
    
    
    <Modal
    :visible="isShowModal"
    title="창고 검색"
    idField="whId"
    :columns="[
      { key: 'whId', label: '창고번호' },
      { key: 'whName', label: '창고명' }
    ]"
    :fetchData="fetchWarehouseData"
    :page-size="5"
    @select="handleSelect"
    @close="isShowModal = false"
    />


    <!-- 검색 영역 -->
    <div class="filter-section">
      <div class="filter-group">
        <span class="filter-label">주문일자</span>
        <DatePicker v-model="filters.startDate" placeholder="시작일" dateFormat="yy-mm-dd" showIcon />
        <span>~</span>
        <DatePicker v-model="filters.endDate" placeholder="종료일" dateFormat="yy-mm-dd" showIcon />
      </div>

      <div class="filter-group">
        <span class="filter-label">제품명</span>
        <InputText v-model="filters.prodName" placeholder="제품명 검색" />
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
          <Column field="prodSpec" header="규격" style="width:100px; text-align:center;" />
          <Column field="prodUnit" header="단위" style="width:80px; text-align:center;" />
          <Column field="prodPrice" header="제품가격" style="width:120px; text-align:right;">
            <template #body="slotProps">
              {{ formatCurrency(slotProps.data.prodPrice) }}
            </template>
          </Column>
          <Column field="orderQty" header="주문수량" style="width:80px; text-align:center;" />
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
import { ref, computed, watch, onMounted } from 'vue'
import axios from 'axios'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import DatePicker from 'primevue/datepicker'
// import * as XLSX from 'xlsx'
// import * as FileSaver from 'file-saver'  // ✅ 핵심
// import jsPDF from 'jspdf'
// import 'jspdf-autotable'

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

// ===== 선택된 주문 (좌측 목록 → 우측 상세) =====
const selectedOrder = ref(null)

// ===== 주문 상세 =====
const orderDetails = ref([])

// ===== 합계 =====
const detailTotal = computed(() => {
  return orderDetails.value.reduce((acc, item) => acc + (item.total || 0), 0)
})

// ===== 금액 포맷 =====
const formatCurrency = (value) => {
  if (!value) return '0 원'
  return value.toLocaleString('ko-KR') + ' 원'
}

// ===== 날짜 포맷 (YYYY-MM-DD로 변환) =====
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// ===== API 전달용 날짜 포맷 함수 =====
const formatDateForAPI = (date) => {
  if (!date) return null
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}` // YYYY-MM-DD
}

// ===== 주문 목록 조회 =====
const fetchOrders = async () => {
  try {
    const { data } = await axios.get('/api/orderlist', {
      params: {
        startDate: formatDateForAPI(filters.value.startDate),
        endDate: formatDateForAPI(filters.value.endDate),
        prodName: filters.value.prodName,
        status: filters.value.status,
        orderId: filters.value.orderId
      }
    })

    console.log('API 요청 파라미터:', {
      startDate: formatDateForAPI(filters.value.startDate),
      endDate: formatDateForAPI(filters.value.endDate)
    })

    // 서버 데이터를 화면 출력용으로 가공
    orders.value = data.map(item => ({
      orderId: item.orderId,
      orderDate: formatDate(item.orderDate),
      prodName: item.prodName || '-',
      totalPrice: item.totalPrice || 0,
      deliveryDate: formatDate(item.deliveryDate),
      status: item.status || '-'
    }))

    console.log('주문 목록:', data)
  } catch (error) {
    console.error('주문 목록 조회 오류:', error)
    alert('주문 목록 조회 중 오류가 발생했습니다.')
  }
}

// ===== 주문 상세 조회 =====
const fetchOrderDetail = async (orderId) => {
  console.log("요청한 주문번호:", orderId)

  try {
    const { data } = await axios.get('/api/orderdetail', {
      params: { orderId }
    })

    console.log('서버에서 받은 주문 상세 데이터:', data)

    orderDetails.value = data.map(item => ({
      ...item,
      total: item.prodPrice * item.orderQty
    }))

    console.log('주문 상세:', orderDetails.value)
  } catch (error) {
    console.error('주문 상세 조회 오류:', error)
    alert('주문 상세 조회 중 오류가 발생했습니다.')
  }
}

// ===== 선택된 주문 감시 =====
watch(selectedOrder, (newOrder) => {
  if (newOrder && newOrder.orderId) {
    fetchOrderDetail(newOrder.orderId)
  } else {
    orderDetails.value = []
  }
})

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


// ===== 엑셀 다운로드 =====
// const exportExcel = () => {
//   if (!orders.value.length) {
//     alert('다운로드할 데이터가 없습니다.')
//     return
//   }

//   // JSON → 시트 변환
//   const ws = XLSX.utils.json_to_sheet(orders.value)

//   // 워크북 생성
//   const wb = XLSX.utils.book_new()
//   XLSX.utils.book_append_sheet(wb, ws, "주문목록")

//   // 엑셀 파일로 변환
//   const excelBuffer = XLSX.write(wb, { bookType: 'xlsx', type: 'array' })
//   const data = new Blob([excelBuffer], { type: 'application/octet-stream' })

//   // ✅ FileSaver.saveAs 로 호출
//   FileSaver.saveAs(data, `주문목록_${new Date().toISOString().slice(0, 10)}.xlsx`)
// }


// // ===== PDF 다운로드 =====
// const exportPDF = () => {
//   if (!orders.value.length) {
//     alert('출력할 데이터가 없습니다.')
//     return
//   }

//   const doc = new jsPDF()

//   // 타이틀
//   doc.setFontSize(18)
//   doc.text('주문 목록 보고서', 14, 20)

//   // 테이블 생성
//   const tableData = orders.value.map(item => [
//     item.orderId,
//     item.orderDate,
//     item.prodName,
//     formatCurrency(item.totalPrice),
//     item.status
//   ])

//   doc.autoTable({
//     head: [['주문번호', '주문일자', '대표 제품명', '총액', '상태']],
//     body: tableData,
//     startY: 30
//   })

//   // PDF 저장
//   doc.save(`주문목록_${new Date().toISOString().slice(0, 10)}.pdf`)
// }

// ===== 페이지 로드 시 전체 주문 자동 조회 =====
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
  background: #007ad9;
  color: white;
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
