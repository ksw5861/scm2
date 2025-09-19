<template>
  <div class="pay-register">
    <!-- ===== 상단 요약 카드 ===== -->
    <div class="summary-cards">
      <div class="summary-card sales">
        <p class="title">이번달 매출 금액</p>
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

    <!-- ===== 주문 내역 테이블 ===== -->
    <div class="order-table-section">
      <DataTable
        :value="orders"
        v-model:selection="selectedOrders"
        dataKey="order_id"
        selectionMode="checkbox"
        paginator
        :rows="10"
        responsiveLayout="scroll"
        resizableColumns
        columnResizeMode="fit"
        class="order-table"
      >
        <Column selectionMode="multiple" headerStyle="width: 50px"></Column>
        <Column field="order_id" header="주문번호" style="width:120px" />
        <Column field="order_date" header="주문일자" style="width:120px; text-align:center;" />
        <Column field="ship_date" header="출고일자" style="width:120px; text-align:center;" />
        <Column field="product_name" header="제품명" style="width:180px;" />
        <Column field="order_amount" header="주문총액(원)" style="width:130px; text-align:right;">
          <template #body="slotProps">
            {{ formatCurrency(slotProps.data.order_amount) }}
          </template>
        </Column>
        <Column field="pay_due" header="결제기한" style="width:120px; text-align:center;" />
        <Column field="pay_status" header="결제유무" style="width:100px; text-align:center;" />
      </DataTable>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'

// ===== 샘플 데이터 =====
const orders = ref([
  { order_id: 'AB-001', order_date: '2025-08-30', ship_date: '2025-08-30', product_name: '아라비카 원두 외 6건', order_amount: 2160000, pay_due: '2026-03-02', pay_status: '대기' },
  { order_id: 'AB-002', order_date: '2025-08-28', ship_date: '2025-08-26', product_name: '리베리카 원두', order_amount: 500000, pay_due: '2026-02-28', pay_status: '대기' },
  { order_id: 'AB-003', order_date: '2025-08-24', ship_date: '2025-08-24', product_name: '로부스타 원두', order_amount: 1000000, pay_due: '2026-02-24', pay_status: '완료' },
  { order_id: 'AB-004', order_date: '2025-08-20', ship_date: '2025-08-19', product_name: '로부스타 원두', order_amount: 1000000, pay_due: '2026-02-20', pay_status: '대기' }
])

// 체크된 주문 목록
const selectedOrders = ref([])

// 상단 카드용 데이터
const monthSales = ref(4999999)
const remainingCredit = ref(40250000)

// 선택된 주문의 합계
const selectedTotal = computed(() => {
  return selectedOrders.value.reduce((sum, item) => sum + item.order_amount, 0)
})

// 금액 포맷
const formatCurrency = (value) => {
  return value.toLocaleString('ko-KR') + ' 원'
}

// 납부하기
const submitPayment = () => {
  if (selectedOrders.value.length === 0) {
    alert('납부할 주문을 선택하세요.')
    return
  }
  console.log('납부 데이터 전송:', selectedOrders.value)
  alert(`총 ${formatCurrency(selectedTotal.value)} 납부가 진행됩니다.`)
}
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
