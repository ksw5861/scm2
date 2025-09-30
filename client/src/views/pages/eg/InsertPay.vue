<template>
  <div class="payment-dashboard">
    <!-- ===== 헤더 ===== -->
    <header class="header">
      <div>
        <h1 class="title">본사 거래 관리</h1>
        <p class="subtitle">주문, 반품 및 납부 현황 관리</p>
      </div>
      <Badge class="date-badge">{{ today }}</Badge>
    </header>

    <!-- ===== 상단 카드 4개 ===== -->
    <section class="summary-cards-formula">
      <!-- 총 미결제 금액 -->
      <div class="card red">
        <p class="label">총 미결제 금액</p>
        <p class="amount">₩{{ formatCurrency(summary.totalUnpaid) }}</p>
      </div>

      <div class="formula-icon">➖</div>

      <!-- 승인된 반품 총액 -->
      <div class="card green">
        <p class="label">승인된 반품 총액</p>
        <p class="amount">₩{{ formatCurrency(summary.thisMonthReturnAmount) }}</p>
      </div>

      <div class="formula-icon">=</div>

      <!-- 최종 납부 예정 금액 -->
      <div class="card blue">
        <p class="label">이번 납부 예정 금액</p>
        <p class="amount">
          ₩{{ formatCurrency(summary.totalUnpaid - summary.thisMonthReturnAmount) }}
        </p>
      </div>

      <!-- 다음 납부 예정 금액 -->
      <div class="card orange">
        <p class="label">다음 납부 예정 금액</p>
        <p class="amount">₩{{ formatCurrency(summary.upcomingAmount) }}</p>
      </div>
    </section>

    <!-- ===== 탭 ===== -->
    <div class="tabs">
      <button
        class="tab-btn"
        :class="{ active: activeTab === 'pending' }"
        @click="activeTab = 'pending'"
      >
        미결제 내역
      </button>
      <button
        class="tab-btn"
        :class="{ active: activeTab === 'pay' }"
        @click="activeTab = 'pay'"
      >
        납부 처리
      </button>
    </div>

    <!-- ===== [탭] 미결제 내역 ===== -->
    <div v-if="activeTab === 'pending'" class="tab-content">
      <h4 class="section-title">미결제 주문내역</h4>

      <div class="table-toolbar">
        <div class="search-container">
          <i class="pi pi-search search-icon"></i>
          <InputText
            v-model="searchQuery"
            placeholder="주문번호 또는 제품명 검색..."
            class="search-input"
          />
        </div>
      </div>

      <DataTable
        :value="filteredOrders"
        selectionMode="checkbox"
        dataKey="orderId"
        v-model:selection="selectedOrders"
        paginator
        :rows="20"
        class="custom-table"
      >
        <template #header>
          <div v-if="selectedOrders.length > 0" class="selected-summary">
            <span>{{ selectedOrders.length }}개 항목 선택됨</span>
            <span>총 선택 금액: <strong>₩{{ formatCurrency(selectedTotal) }}</strong></span>
          </div>
        </template>

        <Column selectionMode="multiple" headerStyle="width:50px" />
        <Column field="orderId" header="주문번호" style="width:140px;" />
        <Column field="prodName" header="제품명" style="width:240px;" />
        <Column field="totalPrice" header="주문금액" style="width:150px; text-align:right;">
          <template #body="{ data }">
            ₩{{ formatCurrency(data.totalPrice) }}
          </template>
        </Column>
        <Column field="sendDate" header="출고일자" style="width:140px;">
          <template #body="{ data }">
            {{ formatDate(data.sendDate) }}
          </template>
        </Column>
        <Column field="status" header="주문상태" style="width:140px;" />
        <Column field="paydueDate" header="결제기한" style="width:140px;">
          <template #body="{ data }">
            {{ formatDate(data.paydueDate) }}
          </template>
        </Column>
      </DataTable>
    </div>

    
    <!-- ===== [탭] 납부 처리 ===== -->
    <div v-else-if="activeTab === 'pay'" class="tab-content pay-section">
      <!-- 좌측 납부 요약 -->
      <div class="pay-summary">
        <h3 class="section-title"><i class="pi pi-check-circle"></i> 납부 요약</h3>
        <div class="summary-list">
          <div v-for="order in selectedOrders" :key="order.orderId" class="summary-item">
            <span>{{ order.prodName }} + 외 {{ order.extraCount }}건</span>
            <span>₩{{ formatCurrency(order.totalPrice) }}</span>
          </div>
        </div>
        <div class="summary-total">
          <div class="row">
            <span>총 납부금액</span>
            <span>₩{{ formatCurrency(selectedTotal) }}</span>
          </div>
        </div>
      </div>

      <!-- 우측 카카오페이 결제 버튼 -->
      <div class="pay-form">
        <h3 class="section-title"><i class="pi pi-wallet"></i> 카카오페이 결제</h3>
        <Button
          :label="'₩' + formatCurrency(selectedTotal) + ' 결제하기'"
          class="w-full pay-btn"
          @click="requestPay"
        />
      </div>
    </div>

    <Toast />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import Badge from 'primevue/badge'
import Toast from 'primevue/toast'
import InputText from 'primevue/inputtext'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import { useToast } from 'primevue/usetoast'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const vendorId = ref(null);
const toast = useToast()
const today = new Date().toLocaleDateString('ko-KR')

// ===== 탭 상태 =====
const activeTab = ref('pending')

// ===== 상단 카드 데이터 =====
const summary = ref({
  totalUnpaid: 0,
  thisMonthReturnAmount: 0,
  upcomingAmount: 0
})

const fetchSummary = async () => {
  try {
    const res = await axios.get('/api/paymentsummary')
    if (res.data && res.data.status === 'success') {
      summary.value = res.data.data || {}
    }
  } catch (error) {
    console.error('상단 카드 데이터 불러오기 실패:', error)
  }
}

// ===== 미결제 내역 =====
const orders = ref([])
const selectedOrders = ref([])
const searchQuery = ref('')

const fetchOrders = async () => {
  try {
    const res = await axios.get('/api/pendingorders')
    orders.value = res.data || []
  } catch (error) {
    console.error('미결제 목록 불러오기 실패:', error)
  }
}

const filteredOrders = computed(() => {
  if (!searchQuery.value) return orders.value
  return orders.value.filter(order =>
    order.orderId.includes(searchQuery.value) ||
    order.prodName.includes(searchQuery.value)
  )
})

const selectedTotal = computed(() => selectedOrders.value.reduce((sum, order) => sum + (order.totalPrice || 0), 0))

// ===== 카카오페이 결제 요청 =====
onMounted(() => {
  const IMP = window.IMP
  IMP.init('imp62556076') // 아임포트 발급 가맹점 코드
})

const requestPay = () => {
  if (selectedOrders.value.length === 0) {
    toast.add({ severity: 'warn', summary: '알림', detail: '납부할 주문을 선택하세요.', life: 3000 })
    return
  }

  const IMP = window.IMP
  const orderIds = selectedOrders.value.map(o => o.orderId).join(',')

  IMP.request_pay(
    {
      pg: 'kakaopay.TC0ONETIME',
      pay_method: 'card',
      merchant_uid: `ORDER_${new Date().getTime()}`,
      name: `주문 ${selectedOrders.value.length}건`,
      amount: selectedTotal.value,
      buyer_email: userStore.email || 'test@example.com',
      buyer_name: userStore.name || '홍길동',
      buyer_tel: '010-1234-5678',
      buyer_addr: '서울특별시 강남구 역삼동',
      buyer_postcode: '123-456'
    },
    async (rsp) => {
      if (rsp.success) {
        try {
          const payload = {
            impUid: rsp.imp_uid,
            merchantUid: rsp.merchant_uid,
            payAmount: selectedTotal.value,
            vendorId: 'VEN001', // 수정해야함
            payType: '카카오페이',
            paymentDetails: selectedOrders.value.map(order => ({
              dataType: 'ORDER',
              orderId: order.orderId,
              totalPrice: order.totalPrice
            }))
          }

          console.log(payload)
          await axios.post('/api/verify-payment', payload)

          toast.add({
            severity: 'success',
            summary: '성공',
            detail: '결제가 완료되었습니다.',
            life: 3000
          })

          // 화면 초기화
          fetchOrders()
          selectedOrders.value = []
        } catch (err) {
          toast.add({
            severity: 'error',
            summary: '서버 오류',
            detail: '백엔드 저장 중 오류가 발생했습니다.',
            life: 3000
          })
        }
      } else {
        toast.add({
          severity: 'error',
          summary: '결제 실패',
          detail: rsp.error_msg,
          life: 3000
        })
      }
    }
  )
}

// ===== 날짜 & 금액 포맷 =====
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const formatCurrency = (value) => {
  return value ? value.toLocaleString('ko-KR') : '0'
}

onMounted(() => {
  fetchSummary()
  fetchOrders()
  vendorId.value = userStore.code
  console.log(vendorId.value)
})
</script>




<style scoped>
/* ===== 공통 스타일 ===== */

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.title {
  font-size: 1.6rem;
  font-weight: bold;
}
.subtitle {
  font-size: 0.9rem;
  color: #777;
}
.date-badge {
  background: #eee;
  color: #333;
}


/* ===== 상단 카드 - 균등 + 아이콘 작게 ===== */
.summary-cards-formula {
  display: grid;
  grid-template-columns: 2fr 0.3fr 2fr 0.3fr 2fr 2fr; /* 카드 넓게, 아이콘 작게 */
  align-items: stretch;
  gap: 12px;
  margin: 20px 0;
  width: 100%;
}

/* ===== 카드 공통 ===== */
.summary-cards-formula .card {
  min-width: 160px;
  min-height: 140px;
  padding: 20px;
  border-radius: 12px;
  background: #fff;
  text-align: center;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  transition: transform 0.2s ease, background 0.2s ease;
  height: 100%;
}

.summary-cards-formula .card:hover {
  transform: translateY(-4px);
}

.card .label {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 10px;
}
.card .amount {
  font-size: 1.5rem;
  font-weight: bold;
}

/* ===== 색상 ===== */
.red .amount {
  color: #e74c3c;
}
.green .amount {
  color: #27ae60;
}
.blue .amount {
  color: #2980b9;
}
.orange .amount {
  color: #f39c12;
}

/* ===== ➖, = 아이콘 ===== */
.formula-icon {
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 1.4rem; /* 작게 */
  font-weight: bold;
  color: #888;
  height: 100%;
  min-height: 140px;
  background: transparent; /* 배경 제거 */
  box-shadow: none;
}

/* ===== 반응형 ===== */

/* 태블릿 이하: 2열 */
@media (max-width: 1024px) {
  .summary-cards-formula {
    grid-template-columns: repeat(2, 1fr);
  }
  .formula-icon {
    display: none; /* 태블릿부터 아이콘 숨김 */
  }
}

/* 모바일 이하: 1열 */
@media (max-width: 480px) {
  .summary-cards-formula {
    grid-template-columns: 1fr;
  }
}


/* ===== 거래 요약 3개 카드 ===== */
.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin: 20px 0;
}

/* ===== 탭 ===== */
.tabs {
  display: flex;
  border-bottom: 1px solid #ddd;
  margin-bottom: 15px;
}
.tab-btn {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  font-size: 1rem;
  background: none;
  border: none;
  cursor: pointer;
}
.tab-btn.active {
  border-bottom: 3px solid #2980b9;
  font-weight: bold;
  color: #2980b9;
}

/* ===== 검색창 ===== */
.table-toolbar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 10px;
}

.table-toolbar.between {
  justify-content: space-between;
}

.search-container {
  position: relative;
  width: 280px;
}

.search-icon {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #aaa;
}
.search-input {
  padding-left: 30px;
  border-radius: 20px;
  height: 34px;
  width: 100%;
  border: 1px solid #ccc;
}

.filters {
  display: flex;
  gap: 10px;
}
.export-btn {
  background: #f4f4f4;
  border: none;
  color: #333;
}

/* ===== 상태 뱃지 ===== */
.status-badge {
  padding: 3px 8px;
  border-radius: 6px;
  color: white;
  font-size: 0.8rem;
}
.status-badge.완료 {
  background: #27ae60;
}
.status-badge.대기중 {
  background: #f39c12;
}
.status-badge.실패 {
  background: #e74c3c;
}

/* ===== 납부 처리 ===== */
.pay-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}
.pay-form,
.pay-summary {
  background: #fff;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}
.section-title {
  font-weight: bold;
  margin-bottom: 12px;
}

.radio-group {
  display: flex;
  gap: 10px;
  align-items: center;
}

.pay-btn {
  background-color: #000;
  color: #fff;
  font-weight: bold;
  border: none;
  height: 40px;
  border-radius: 6px;
  cursor: pointer;
}
.pay-btn:hover {
  background-color: #333;
}

/* ===== 결제 요약 ===== */
.summary-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.summary-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
  font-size: 0.9rem;
}

.summary-total {
  margin-top: 12px;
  padding-top: 8px;
  border-top: 1px solid #ccc;
}
.summary-total .row {
  display: flex;
  justify-content: space-between;
  font-weight: bold;
  font-size: 1.1rem;
}

.selected-summary {
  display: flex;
  justify-content: flex-end;
  gap: 20px;
  font-size: 1rem;
  padding: 4px 10px;
}

</style>
