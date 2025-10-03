<template>
  <div class="go-del">
    <!-- 헤더 -->
    <div class="header">
      <h2>출하지시 등록 (goDel)</h2>
      <div class="header-actions">
        <Button 
          label="출하지시 등록" 
          icon="pi pi-truck" 
          class="p-button-warning"
          :disabled="!selectedOrders.length"
          @click="createDeliveryInstruction" 
        />
      </div>
    </div>

    <!-- 주문 목록 -->
    <div class="content-section">
      <!-- 좌측 주문 목록 -->
      <div class="order-list-table">
        <DataTable
          :value="orders"
          selectionMode="multiple"
          v-model:selection="selectedOrders"
          dataKey="orderId"
          paginator
          :rows="10"
          responsiveLayout="scroll"
          class="custom-table"
        >
          <Column selectionMode="multiple" headerStyle="width: 3em" />
          <Column field="orderId" header="주문번호" style="width:140px; text-align:center;" />
          <Column field="orderDate" header="주문일자" style="width:140px; text-align:center;" />
          <Column field="vendorId" header="가맹점" style="width:120px; text-align:center;" />
          <Column field="totalPrice" header="총액" style="width:120px; text-align:right;">
            <template #body="slotProps">
              {{ formatCurrency(slotProps.data.totalPrice) }}
            </template>
          </Column>
          <Column field="status" header="상태" style="width:120px; text-align:center;" />
        </DataTable>
      </div>

      <!-- 우측 주문 상세 -->
      <div class="order-detail-table">
        <DataTable
          :value="orderDetails"
          dataKey="odetailId"
          responsiveLayout="scroll"
          class="custom-table"
        >
          <Column field="prodId" header="제품코드" style="width:120px; text-align:center;" />
          <Column field="prodName" header="제품명" style="width:150px;" />
          <Column field="spec" header="규격" style="width:100px; text-align:center;" />
          <Column field="unit" header="단위" style="width:80px; text-align:center;" />
          <Column field="orderQty" header="주문수량" style="width:80px; text-align:center;" />
          <Column field="prodUnitPrice" header="단가" style="width:100px; text-align:right;">
            <template #body="slotProps">
              {{ formatCurrency(slotProps.data.prodUnitPrice) }}
            </template>
          </Column>
          <Column field="totalUnitPrice" header="합계" style="width:120px; text-align:right;">
            <template #body="slotProps">
              {{ formatCurrency(slotProps.data.totalUnitPrice) }}
            </template>
          </Column>
          <Column field="prodStatus" header="상태" style="width:120px; text-align:center;" />
        </DataTable>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import axios from 'axios'

// PrimeVue
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'

const orders = ref([])
const selectedOrders = ref([])
const orderDetails = ref([])

// 주문 목록 조회
const fetchOrders = async () => {
  try {
    const { data } = await axios.get('/api/orders/pending-delivery')
    if (data.status === 'success') {
      orders.value = data.orders
    } else {
      orders.value = []
    }
  } catch (err) {
    console.error('주문 목록 조회 실패:', err)
    orders.value = []
  }
}

// 주문 상세 조회
const fetchOrderDetails = async (orderIds) => {
  let details = []
  for (const orderId of orderIds) {
    try {
      const { data } = await axios.get(`/api/goDel/orders/${orderId}/details`)
      if (data.status === 'success') {
        details = [
          ...details, 
          ...data.details.map(d => ({
            ...d,
            totalUnitPrice: d.prodUnitPrice * d.orderQty
          }))
        ]
      }
    } catch (err) {
      console.error(`주문 상세 조회 실패 [${orderId}]:`, err)
    }
  }
  orderDetails.value = details
}

// 출하지시 등록
const createDeliveryInstruction = async () => {
  if (!selectedOrders.value.length) {
    alert('출하지시에 포함할 주문을 선택하세요.')
    return
  }
  try {
    const ids = selectedOrders.value.map(o => o.orderId)
    const { data } = await axios.post('/api/goDel/create', { orders: ids })
    if (data.status === 'success') {
      alert(`출하지시 등록 완료! (출하번호: ${data.shipId})`)
      fetchOrders()
      selectedOrders.value = []
      orderDetails.value = []
    } else {
      alert(data.message || '출하지시 등록 실패')
    }
  } catch (err) {
    console.error('출하지시 등록 실패:', err)
    alert('출하지시 등록 실패')
  }
}

// 선택된 주문 변경 감지 → 상세조회
watch(
  () => selectedOrders.value,
  (newOrders) => {
    if (newOrders.length) {
      fetchOrderDetails(newOrders.map(o => o.orderId))
    } else {
      orderDetails.value = []
    }
  }
)

onMounted(fetchOrders)

const formatCurrency = (v) => (v ? v.toLocaleString() + ' 원' : '0 원')
</script>

<style scoped>
.go-del {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
.header-actions {
  display: flex;
  gap: 10px;
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

/* 테이블 공통 스타일 */
.custom-table {
  width: 100%;
  font-size: 0.95rem;
}
::v-deep(.p-datatable-thead > tr > th) {
  background: #f4f6f8;
  font-weight: 600;
  text-align: center !important;
  padding: 10px 0;
}
::v-deep(.p-datatable-tbody > tr > td) {
  text-align: center;
  padding: 8px 10px;
}
::v-deep(.p-datatable-wrapper) {
  overflow-x: auto;
}

/* 버튼 색상 */
.p-button-primary {
  background-color: #007bff !important;
  border: none;
}
.p-button-warning {
  background-color: #ffc107 !important;
  border: none;
  color: #000 !important;
}
</style>
