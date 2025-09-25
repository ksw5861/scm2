<template>
  <div class="pay-register">
    <!-- ===== 상단 요약 카드 ===== -->
    <div class="summary-cards">
      <div class="summary-card sales">
        <p class="title">미수금</p>
        <p class="amount">{{ formatCurrency(monthSales) }}</p>
      </div>

      <div class="summary-card credit">
        <p class="title">남은 여신한도</p>
        <p class="amount highlight">{{ formatCurrency(remainingCredit) }}</p>
      </div>

      <div class="summary-card total">
        <p class="title">납부금액</p>
        <p class="amount">{{ formatCurrency(selectedTotal) }}</p>
      </div>

      <Button 
        label="납부하기"
        icon="pi pi-wallet"
        class="p-button-lg p-button-primary pay-btn"
        @click="submitPayment"
      />
    </div>

    <!-- Toast 알림 -->
    <Toast />

    <!-- ===== 주문 내역 테이블 ===== -->
    <div class="order-table-section">
      <DataTable
        :value="orders"
        v-model:selection="selectedOrders"
        dataKey="orderId"
        selectionMode="checkbox"
        paginator
        :rows="10"
        responsiveLayout="scroll"
        resizableColumns
        columnResizeMode="fit"
        class="order-table"
        :rowClass="rowClass"
      >
        <Column selectionMode="multiple" headerStyle="width: 50px"></Column>
        <Column field="orderId" header="주문번호" style="width:120px" />

        <!-- 주문일자 yyyy-MM-dd -->
        <Column field="orderDate" header="주문일자" style="width:120px; text-align:center;">
          <template #body="slotProps">
            {{ formatDate(slotProps.data.orderDate) }}
          </template>
        </Column>

        <!-- 출고일자 yyyy-MM-dd -->
        <Column field="sendDate" header="출고일자" style="width:120px; text-align:center;">
          <template #body="slotProps">
            {{ formatDate(slotProps.data.sendDate) }}
          </template>
        </Column>

        <Column field="prodName" header="제품명" style="width:180px;" />

        <!-- 금액 표시: 반품이면 returnUnitPrice 사용 -->
        <Column field="prodUnitPrice" header="주문총액(원)" style="width:130px; text-align:right;">
          <template #body="slotProps">
            <span v-if="slotProps.data.payStatus === 'RETURN'" class="refund-amount">
              -{{ formatCurrency(slotProps.data.returnUnitPrice) }}
            </span>
            <span v-else>
              {{ formatCurrency(slotProps.data.prodUnitPrice) }}
            </span>
          </template>
        </Column>

        <!-- 결제기한 = 주문일 + 180일 -->
        <Column field="paydueDate" header="결제기한" style="width:120px; text-align:center;">
          <template #body="slotProps">
            {{ formatPayDueDate(slotProps.data.orderDate) }}
          </template>
        </Column>

        <Column field="payStatus" header="결제상태" style="width:100px; text-align:center;" />
      </DataTable>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Toast from 'primevue/toast'
import { useToast } from 'primevue/usetoast'

const orders = ref([])
const selectedOrders = ref([])
const toast = useToast()

// 상단 카드 데이터
const remainingCredit = ref(0)

// ===== 주문 목록 조회 =====
const fetchOrders = async () => {
  try {
    const res = await axios.get('/api/pendingorders') // 🔹 반품 포함된 전체 주문 조회
    orders.value = res.data || []

    // 주문번호 + 상태 기준 정렬
    orders.value.sort((a, b) => {
      if (a.orderId === b.orderId) {
        return a.payStatus.localeCompare(b.payStatus) // RETURN이 아래쪽으로
      }
      return a.orderId.localeCompare(b.orderId)
    })

    console.log('전체 주문 목록 (납부대기+반품):', orders.value)
  } catch (error) {
    console.error('주문 목록 조회 실패:', error)
    orders.value = []
  }
}

// ===== 미수금 합계 (반품금액은 자동 차감) =====
const monthSales = computed(() =>
  orders.value.reduce((sum, order) => {
    const normal = order.payStatus === 'PENDING' ? Number(order.prodUnitPrice || 0) : 0
    const refund = order.payStatus === 'RETURN' ? Number(order.returnUnitPrice || 0) : 0
    return sum + (normal - refund)
  }, 0)
)

// ===== 선택된 주문 총합 (반품금액은 자동 차감) =====
const selectedTotal = computed(() =>
  selectedOrders.value.reduce((sum, item) => {
    const normal = item.payStatus === 'PENDING' ? Number(item.prodUnitPrice || 0) : 0
    const refund = item.payStatus === 'RETURN' ? Number(item.returnUnitPrice || 0) : 0
    return sum + (normal - refund)
  }, 0)
)

// ===== 금액 포맷팅 =====
const formatCurrency = (value) => {
  if (!value && value !== 0) return '0 원'
  return value.toLocaleString('ko-KR') + ' 원'
}

// ===== 날짜 포맷팅 (yyyy-MM-dd) =====
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  if (isNaN(date)) return ''
  return date.toISOString().split('T')[0]
}

// ===== 결제기한 계산 (주문일 + 180일) =====
const formatPayDueDate = (orderDateStr) => {
  if (!orderDateStr) return ''
  const orderDate = new Date(orderDateStr)
  if (isNaN(orderDate)) return ''

  orderDate.setDate(orderDate.getDate() + 180)
  return orderDate.toISOString().split('T')[0]
}

// ===== 행 스타일 (반품건은 전체 빨간 표시) =====
const rowClass = (data) => {
  return {
    'refund-row': data.payStatus === 'RETURN'
  }
}

// ===== 납부 처리 =====
const submitPayment = async () => {
  if (selectedOrders.value.length === 0) {
    toast.add({ severity: 'warn', summary: '알림', detail: '납부할 주문을 선택하세요.', life: 3000 })
    return
  }

  // ✅ prodUnitPrice / returnUnitPrice 컬럼을 서버에 구분해서 전송
  const payload = {
    payRmk: '납부 처리',
    payAmount: selectedTotal.value,
    vendorId: 'V-001',
    payType: 'CASH',
    paymentDetails: selectedOrders.value.map(o => ({
      orderId: o.orderId,
      prodUnitPrice: o.payStatus === 'PENDING' ? o.prodUnitPrice : 0,
      returnUnitPrice: o.payStatus === 'RETURN' ? o.returnUnitPrice : 0
    }))
  }

  try {
    await axios.post('/api/insertpayment', payload)
    toast.add({ severity: 'success', summary: '성공', detail: '납부가 완료되었습니다.', life: 3000 })
    selectedOrders.value = []
    fetchOrders()
  } catch (error) {
    console.error('납부 등록 오류:', error)
    toast.add({ severity: 'error', summary: '오류', detail: '납부 등록 중 오류가 발생했습니다.', life: 3000 })
  }
}

// 페이지 로드시 자동 조회
onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
/* ===== 전체 레이아웃 ===== */
.pay-register {
  padding: 20px;
  background: #f6f8fa;
}

/* ===== 상단 카드 ===== */
.summary-cards {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.summary-card {
  flex: 1;
  min-width: 200px;
  background: white;
  border: 1px solid #dcdcdc;
  border-radius: 8px;
  padding: 15px;
  text-align: center;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
}

.summary-card .title {
  font-size: 1rem;
  color: #555;
  margin-bottom: 8px;
}

.summary-card .amount {
  font-size: 1.5rem;
  font-weight: bold;
  color: #333;
}

.summary-card .highlight {
  color: #007ad9;
}

.pay-btn {
  height: 70px;
  font-size: 1.2rem;
  font-weight: bold;
  background-color: #004080;
  border: none;
  padding: 0 25px;
  border-radius: 8px;
  transition: 0.3s;
}

.pay-btn:hover {
  background-color: #0059b3;
}

/* ===== 테이블 섹션 ===== */
.order-table-section {
  background: white;
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #dcdcdc;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
}

.order-table {
  font-size: 0.95rem;
  width: 100%;
}

::v-deep(.p-datatable-thead > tr > th) {
  border-right: 1px solid #e5e7eb;
  background: #f9fafb;
  font-weight: 600;
  text-align: center;
  padding: 10px;
}

::v-deep(.p-datatable-tbody > tr > td) {
  border-right: 1px solid #f0f0f0;
  padding: 8px 10px;
  text-align: center;
}

/* 반품 행 전체 빨간색 배경 */
.refund-row {
  background-color: #fff5f5 !important;
}

/* 반품 금액만 빨간색 */
.refund-amount {
  color: #d60000;
  font-weight: bold;
}

@media (max-width: 768px) {
  .summary-cards {
    flex-direction: column;
  }
  
  .pay-btn {
    width: 100%;
    margin-top: 10px;
  }

  .order-table {
    font-size: 0.85rem;
  }

  .p-datatable-wrapper {
    overflow-x: auto;
  }
}
</style>
