<template>
  <div class="dashboard">
    <!-- ===== 상단 카드 3개 ===== -->
    <div class="top-cards">
      <!-- 이번달 납부대금 -->
      <Card class="card purple" @click="goToInsertPay">
        <template #title>
          이번달 납부대금
        </template>
        <template #content>
          <div class="card-value">{{ formatCurrency(thisMonthPayment) }}</div>
        </template>
      </Card>

      <!-- 주문합계 -->
      <Card class="card blue" @click="goToOrderList">
        <template #title>
          주문합계
        </template>
        <template #content>
          <div class="card-value">{{ formatCurrency(totalOrders) }}</div>
          <div class="chart-mini">
            <Chart type="line" :data="miniChartData" :options="miniChartOptions" />
          </div>
        </template>
      </Card>

      <!-- 여신 / 잔여 한도 -->
      <Card class="card limit-card">
        <template #title>
          여신/잔여 한도
        </template>
        <template #content>
          <div class="limit-section">
            <div class="limit-item">
              <label>여신한도:</label>
              <span>{{ formatCurrency(creditLimit) }}</span>
            </div>
            <div class="limit-item">
              <label>잔여한도:</label>
              <span>{{ formatCurrency(remainingLimit) }}</span>
            </div>
          </div>
        </template>
      </Card>
    </div>

    <!-- ===== 하단 그리드 영역 ===== -->
    <div class="bottom-section">
      <!-- 전년도 대비 올해 주문금액 비교 (막대 그래프) -->
      <div class="chart-container">
        <h3>전년도 대비 올해 주문금액 비교</h3>
        <Chart type="bar" :data="barChartData" :options="barChartOptions" />
      </div>

      <!-- 오른쪽 진행중인 주문 & 반품 -->
      <div class="progress-container">
        <!-- 진행중인 주문 -->
        <div class="progress-list">
          <div class="progress-header">
            <h4>진행중인 주문건</h4>
            <span class="view-all" @click="goToOrderList">View All →</span>
          </div>
          <ul>
            <li v-for="order in ongoingOrders" :key="order.date">
              <span class="date">{{ order.date }}</span>
              <span class="status">{{ order.status }}</span>
            </li>
          </ul>
        </div>

        <!-- 진행중인 반품 -->
        <div class="progress-list">
          <div class="progress-header">
            <h4>진행중인 반품건</h4>
            <span class="view-all" @click="goToReturnList">View All →</span>
          </div>
          <ul>
            <li v-for="ret in ongoingReturns" :key="ret.date">
              <span class="date">{{ ret.date }}</span>
              <span class="status">{{ ret.status }}</span>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Card from 'primevue/card'
import Chart from 'primevue/chart'
import { useRouter } from 'vue-router'

const router = useRouter()

/* ===== 샘플 데이터 ===== */
const thisMonthPayment = ref(5000000)
const totalOrders = ref(999999999)
const creditLimit = ref(50000000)
const remainingLimit = ref(42000000)

const ongoingOrders = ref([
  { date: '2025-09-15', status: '발송준비' },
  { date: '2025-09-14', status: '발송준비' },
  { date: '2025-09-13', status: '배송중' },
  { date: '2025-09-12', status: '배송중' }
])

const ongoingReturns = ref([
  { date: '2025-09-14', status: '배송중' },
  { date: '2025-09-11', status: '승인대기' }
])

/* ===== 미니 라인 차트 ===== */
const miniChartData = {
  labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
  datasets: [
    {
      data: [100, 200, 150, 250, 300, 200, 100],
      fill: false,
      borderColor: '#4bc0c0'
    }
  ]
}

const miniChartOptions = {
  plugins: { legend: { display: false } },
  scales: { x: { display: false }, y: { display: false } },
  elements: { point: { radius: 0 } }
}

/* ===== 막대 그래프 ===== */
const barChartData = {
  labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
  datasets: [
    {
      label: '2024',
      backgroundColor: '#93c5fd',
      data: [270, 150, 200, 300, 350, 450]
    },
    {
      label: '2025',
      backgroundColor: '#3b82f6',
      data: [130, 180, 210, 320, 420, 490]
    }
  ]
}

const barChartOptions = {
  responsive: true,
  plugins: {
    legend: {
      position: 'bottom'
    }
  }
}

/* ===== 네비게이션 ===== */
const goToInsertPay = () => {
  router.push('/insertpay')
}

const goToOrderList = () => {
  router.push('/orderlist')
}

const goToReturnList = () => {
  router.push('/returnlist')
}

/* ===== 금액 포맷 ===== */
const formatCurrency = (value) => {
  return value.toLocaleString('ko-KR') + ' 원'
}
</script>

<style scoped>
.dashboard {
  padding: 20px;
  background-color: #f8f9fa;
}

/* ===== 상단 카드 3개 ===== */
.top-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.card {
  cursor: pointer;
  text-align: center;
  padding: 15px;
  border-radius: 8px;
}

.purple {
  background: linear-gradient(135deg, #7e57c2, #9575cd);
  color: #fff;
}

.blue {
  background: linear-gradient(135deg, #2196f3, #64b5f6);
  color: #fff;
}

.limit-card {
  background: #fff;
  border: 1px solid #dcdcdc;
  text-align: left;
}

.card-value {
  font-size: 1.8rem;
  font-weight: bold;
  margin-top: 10px;
}

.limit-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.limit-item {
  display: flex;
  justify-content: space-between;
  font-size: 1rem;
  color: #333;
}

/* ===== 하단 그리드 ===== */
.bottom-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

/* 막대 그래프 영역 */
.chart-container {
  background: #fff;
  padding: 20px;
  border: 1px solid #dcdcdc;
  border-radius: 8px;
}

.chart-container h3 {
  margin-bottom: 10px;
}

/* 진행중인 리스트 */
.progress-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.progress-list {
  background: #fff;
  border: 1px solid #dcdcdc;
  border-radius: 8px;
  padding: 15px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.view-all {
  font-size: 0.85rem;
  color: #007ad9;
  cursor: pointer;
}

ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

li {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 0.9rem;
  border-bottom: 1px solid #f0f0f0;
}

li:last-child {
  border-bottom: none;
}

/* 반응형 */
@media (max-width: 768px) {
  .bottom-section {
    grid-template-columns: 1fr;
  }
}
</style>
