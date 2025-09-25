<template>
  <div class="pay-register">
    <!-- ===== 상단 요약 카드 ===== -->
    <div class="summary-cards">
      <!-- 미수금 -->
      <div class="summary-card sales">
        <p class="title">미수금</p>
        <p class="amount">{{ formatCurrency(monthSales) }}</p>
      </div>

      <!-- 남은 여신한도 -->
      <div class="summary-card credit">
        <p class="title">남은 여신한도</p>
        <p class="amount highlight">{{ formatCurrency(remainingCredit) }}</p>
      </div>

      <!-- 납부금액 -->
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
      >
        <Column selectionMode="multiple" headerStyle="width: 50px"></Column>
        <Column field="orderId" header="주문번호" style="width:120px" />
        <Column field="orderDate" header="주문일자" style="width:120px; text-align:center;" />
        <Column field="sendDate" header="출고일자" style="width:120px; text-align:center;" />
        <Column field="prodName" header="제품명" style="width:180px;" />
        <Column field="totalPrice" header="주문총액(원)" style="width:130px; text-align:right;">
          <template #body="slotProps">
            {{ formatCurrency(slotProps.data.totalPrice) }}
          </template>
        </Column>
        <Column field="paydueDate" header="결제기한" style="width:120px; text-align:center;" />
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

const orders = ref([]) // ✅ 결제대기 주문 목록
const selectedOrders = ref([])

// 상단 카드 데이터
       // 미수금
const remainingCredit = ref(0)    // 여신한도 (추후 API 연동)




// ===== 주문 목록 조회 =====
const fetchOrders = async () => {
  try {
    const res = await axios.get('/api/pendingorders') // 결제대기 주문 목록 API
    orders.value = Array.isArray(res.data) ? res.data : res.data.items
  } catch (error) {
    console.error('주문 목록 조회 실패:', error)
    orders.value = []
  }
}



// 미수금 합계 (결제대기 총액)
const monthSales = computed(() =>
  orders.value.reduce((sum, order) => sum + Number(order.totalPrice || 0), 0)
)


// ===== 선택된 주문 총합 =====
const selectedTotal = computed(() =>
  selectedOrders.value.reduce((sum, item) => sum + (item.totalPrice || 0), 0)
)

// ===== 금액 포맷팅 =====
const formatCurrency = (value) => {
  if (!value && value !== 0) return '0 원'
  return value.toLocaleString('ko-KR') + ' 원'
}

// ===== 납부 처리 =====
const submitPayment = async () => {
  if (selectedOrders.value.length === 0) {
    alert('납부할 주문을 선택하세요.')
    return
  }

  const payload = {
  payRmk: '납부 처리',
  payAmount: selectedTotal.value,
  vendorId: 'V-001',
  payType: 'CASH',
  paymentDetails: selectedOrders.value.map(o => ({
    orderId: o.orderId,
    totalPrice: o.totalPrice
  }))
}

// 반드시 '/insertpayment' 사용
await axios.post('/api/insertpayment', payload)
  .then(() => {
    alert('납부가 완료되었습니다.')
    selectedOrders.value = []
    fetchOrders()
  })
  .catch(error => {
    console.error('납부 등록 오류:', error)
    alert('납부 등록 중 오류가 발생했습니다.')
  })
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

/* 헤더 중앙 정렬 + 구분선 */
::v-deep(.p-datatable-thead > tr > th) {
  border-right: 1px solid #e5e7eb;
  background: #f9fafb;
  font-weight: 600;
  text-align: center;
  padding: 10px;
}

/* 본문 셀 스타일 */
::v-deep(.p-datatable-tbody > tr > td) {
  border-right: 1px solid #f0f0f0;
  padding: 8px 10px;
  text-align: center;
}

/* 반응형 대응 */
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
