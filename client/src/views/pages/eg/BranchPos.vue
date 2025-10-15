<template>
  <div class="pos-dashboard">
    <!-- 상단 탭 -->
    <div class="tab-container">
      <Button
        v-for="tab in tabs"
        :key="tab"
        :label="tab"
        class="tab-btn"
        :class="{ active: activeTab === tab }"
        @click="activeTab = tab"
      />
    </div>

    <!-- ====================== 탭1 : 매출 발생 ====================== -->
    <div v-if="activeTab === '매출 발생'" class="tab-content">
      <div class="pos-body">
        <!-- 상품 리스트 -->
        <div class="product-section">
          <div class="product-list">
            <div
              v-for="item in filteredProducts"
              :key="item.id"
              class="product-card"
              @click="addToOrder(item)"
            >
              <div class="product-name">{{ item.name }}</div>
              <div class="product-price">{{ item.price.toLocaleString() }}원</div>
              <div class="product-stock">재고: {{ item.stock }}</div>
            </div>
          </div>
        </div>

        <!-- 주문내역 -->
        <div class="order-panel">
          <h4 class="order-title">
            <i class="pi pi-receipt mr-2"></i> 주문 내역
            <span class="text-secondary">({{ orderList.length }}개)</span>
          </h4>

          <div v-if="orderList.length === 0" class="empty-order">
            상품을 선택해주세요
          </div>

          <ul v-else class="order-list">
            <li v-for="order in orderList" :key="order.id" class="order-item">
              <div>
                <strong>{{ order.name }}</strong>
                <div class="order-sub">{{ order.price.toLocaleString() }}원</div>
              </div>
              <div class="order-actions">
                <Button icon="pi pi-minus" text @click.stop="decreaseQty(order)" />
                <span class="qty">{{ order.qty }}</span>
                <Button icon="pi pi-plus" text @click.stop="increaseQty(order)" />
                <Button icon="pi pi-trash" text severity="danger" @click.stop="removeOrder(order.id)" />
              </div>
            </li>
          </ul>

          <!-- 결제 요약 -->
          <div v-if="orderList.length > 0" class="payment-summary">
            <div class="summary-row">
              <span>소계</span>
              <span>{{ subTotal.toLocaleString() }}원</span>
            </div>
            <div class="summary-row">
              <span>부가세 (10%)</span>
              <span>{{ tax.toLocaleString() }}원</span>
            </div>
            <div class="summary-row total">
              <strong>총 결제금액</strong>
              <strong>{{ total.toLocaleString() }}원</strong>
            </div>

            <div class="payment-methods">
              <Button
                label="💳 카드 결제"
                class="method-btn"
                :outlined="paymentMethod !== 'card'"
                @click="paymentMethod = 'card'"
              />
              <Button
                label="💵 현금 결제"
                class="method-btn"
                :outlined="paymentMethod !== 'cash'"
                @click="paymentMethod = 'cash'"
              />
            </div>

            <Button label="결제하기" class="pay-btn" @click="handlePayment" />
          </div>
        </div>
      </div>
    </div>

    <!-- ====================== 탭2 : 매출 내역 ====================== -->
    <div v-else-if="activeTab === '매출 내역'" class="tab-content">
      <div class="sales-header">
        <div class="stat-card">
          <h3>오늘 총 매출</h3>
          <p class="value">{{ totalSales.toLocaleString() }}원</p>
          <p class="diff">+12.5% vs 어제</p>
        </div>
        <div class="stat-card">
          <h3>주문 건수</h3>
          <p class="value">{{ salesList.length }}건</p>
          <p class="diff">+3건 vs 어제</p>
        </div>
        <div class="stat-card">
          <h3>평균 주문금액</h3>
          <p class="value">{{ avgOrder.toLocaleString() }}원</p>
          <p class="diff">건당 평균</p>
        </div>
      </div>

      <div class="sales-search">
        <InputText v-model="search" placeholder="주문번호 검색" class="search-input" />
        <Button label="엑셀 다운로드" icon="pi pi-download" outlined />
      </div>

      <DataTable :value="filteredSales" class="sales-table" responsiveLayout="scroll">
        <Column field="orderNo" header="주문번호" />
        <Column field="date" header="일시" />
        <Column field="count" header="상품수" />
        <Column field="amount" header="금액">
          <template #body="{ data }">
            <span class="price">{{ data.amount.toLocaleString() }}원</span>
          </template>
        </Column>
        <Column field="method" header="결제방법">
          <template #body="{ data }">
            <span class="method">{{ data.method }}</span>
          </template>
        </Column>
        <Column field="status" header="상태">
          <template #body>
            <Tag severity="success" value="완료" />
          </template>
        </Column>
      </DataTable>
    </div>

    <!-- ====================== 탭3 : 월별 매출 ====================== -->
    <div v-if="activeTab === '월별 매출'" class="tab-content">
      <div class="month-header">
        <div class="stat-card">
          <h3>월 총 매출</h3>
          <p class="value">{{ monthlyTotal.toLocaleString() }}원</p>
          <p class="diff">+12.5% vs 전월</p>
        </div>
        <div class="stat-card">
          <h3>일평균 매출</h3>
          <p class="value">{{ dailyAvg.toLocaleString() }}원</p>
          <p class="diff">영업일 기준</p>
        </div>
        <div class="stat-card">
          <h3>영업일수</h3>
          <p class="value">{{ workingDays }}일</p>
          <p class="diff">이번 달</p>
        </div>
      </div>

      <!-- 달력 헤더 -->
      <div class="calendar-nav">
        <Button icon="pi pi-chevron-left" text @click="prevMonth" />
        <h3>{{ year }}년 {{ month + 1 }}월</h3>
        <Button icon="pi pi-chevron-right" text @click="nextMonth" />
      </div>

      <!-- 달력 -->
      <div class="calendar">
        <div class="calendar-grid">
          <div
            v-for="(day, i) in daysInMonth"
            :key="i"
            class="day-card"
            :class="{ empty: day.empty }"
          >
            <template v-if="!day.empty">
              <div
                class="day-number"
                :class="{
                  sunday: day.weekday === 0 || day.holiday,
                  saturday: day.weekday === 6
                }"
              >
                {{ day.day }}
                <span v-if="day.holiday" class="holiday-text">({{ day.holiday }})</span>
              </div>

              <!-- 매출 표시 -->
              <div class="day-sales">
                {{ day.sales.toLocaleString() }}원
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from "vue"
import Button from "primevue/button"
import InputText from "primevue/inputtext"
import DataTable from "primevue/datatable"
import Column from "primevue/column"
import Tag from "primevue/tag"

const tabs = ["매출 발생", "매출 내역", "월별 매출"]
const activeTab = ref("매출 발생")

// 매출 발생 탭 -----------------
const filter = ref("전체")
const productList = ref(
  Array.from({ length: 24 }).map((_, i) => ({
    id: i + 1,
    name: `상품 ${i + 1}`,
    category: i % 2 === 0 ? "원두" : "부자재",
    price: 20000 + (i % 5) * 2000,
    stock: 50 + i * 2
  }))
)
const orderList = ref([])
const paymentMethod = ref("")

const filteredProducts = computed(() =>
  filter.value === "전체" ? productList.value : productList.value.filter((p) => p.category === filter.value)
)
const addToOrder = (item) => {
  const found = orderList.value.find((o) => o.id === item.id)
  if (found) found.qty++
  else orderList.value.push({ ...item, qty: 1 })
}
const increaseQty = (order) => order.qty++
const decreaseQty = (order) => (order.qty > 1 ? order.qty-- : removeOrder(order.id))
const removeOrder = (id) => (orderList.value = orderList.value.filter((o) => o.id !== id))
const subTotal = computed(() => orderList.value.reduce((sum, o) => sum + o.price * o.qty, 0))
const tax = computed(() => Math.round(subTotal.value * 0.1))
const total = computed(() => subTotal.value + tax.value)
const handlePayment = () => {
  if (!paymentMethod.value) return alert("결제 방식을 선택해주세요 💳💵")
  alert(`✅ ${paymentMethod.value === "card" ? "카드" : "현금"} 결제가 완료되었습니다!\n총 금액: ${total.value.toLocaleString()}원`)
  orderList.value = []
  paymentMethod.value = ""
}

// 매출 내역 탭 -----------------
const salesList = ref([
  { orderNo: "ORD-20231015-001", date: "2025-10-15 14:23", count: 5, amount: 125000, method: "카드" },
  { orderNo: "ORD-20231015-002", date: "2025-10-15 15:45", count: 3, amount: 89000, method: "현금" },
  { orderNo: "ORD-20231015-003", date: "2025-10-15 16:12", count: 8, amount: 234000, method: "카드" },
  { orderNo: "ORD-20231014-045", date: "2025-10-14 18:30", count: 4, amount: 102000, method: "카드" }
])
const totalSales = computed(() => salesList.value.reduce((sum, s) => sum + s.amount, 0))
const avgOrder = computed(() => Math.round(totalSales.value / salesList.value.length))
const search = ref("")
const filteredSales = computed(() =>
  salesList.value.filter((s) => s.orderNo.toLowerCase().includes(search.value.toLowerCase()))
)

// 월별 매출 탭 -----------------
const today = new Date()
const year = ref(today.getFullYear())
const month = ref(today.getMonth())

const holidays = [
  { date: "2025-01-01", name: "신정" },
  { date: "2025-01-28", name: "설날 연휴" },
  { date: "2025-01-29", name: "설날" },
  { date: "2025-01-30", name: "설날 연휴" },
  { date: "2025-03-01", name: "삼일절" },
  { date: "2025-05-05", name: "어린이날" },
  { date: "2025-06-06", name: "현충일" },
  { date: "2025-08-15", name: "광복절" },
  { date: "2025-09-07", name: "추석 연휴" },
  { date: "2025-09-08", name: "추석" },
  { date: "2025-09-09", name: "추석 연휴" },
  { date: "2025-10-03", name: "개천절" },
  { date: "2025-12-25", name: "성탄절" }
]


const monthlyTotal = ref(7925000)
const workingDays = ref(15)
const dailyAvg = computed(() => Math.round(monthlyTotal.value / workingDays.value))

const daysInMonth = ref([])

function generateCalendar() {
  const firstDay = new Date(year.value, month.value, 1).getDay()
  const lastDate = new Date(year.value, month.value + 1, 0).getDate()

  const newDays = []
  for (let i = 0; i < firstDay; i++) newDays.push({ empty: true })

  for (let d = 1; d <= lastDate; d++) {
    const dateObj = new Date(year.value, month.value, d)
    const dateStr = dateObj.toISOString().split("T")[0]
    const weekday = dateObj.getDay()
    const holidayObj = holidays.find((h) => h.date === dateStr)
    const sales = Math.floor(Math.random() * 400000) + 300000

    newDays.push({
      day: d,
      weekday,
      date: dateStr,
      sales,
      holiday: holidayObj ? holidayObj.name : null
    })
  }

  daysInMonth.value = newDays
}

watch([year, month], generateCalendar, { immediate: true })

const prevMonth = () => {
  if (month.value === 0) {
    month.value = 11
    year.value--
  } else month.value--
}
const nextMonth = () => {
  if (month.value === 11) {
    month.value = 0
    year.value++
  } else month.value++
}
</script>


<style scoped>
.pos-dashboard {
  padding: 1.5rem;
  background: #fff;
}

/* 탭 */
.tab-container {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}
.tab-btn {
  border-radius: 20px;
}
.tab-btn.active {
  background: #007ad9;
  color: #fff;
}

/* POS (탭1) */
.pos-body {
  display: grid;
  grid-template-columns: 3fr 1fr;
  gap: 2rem;
}
.product-section {
  height: 75vh;
  overflow: hidden;
}
.product-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  grid-auto-rows: 95px;
  gap: 0.8rem;
  height: 100%;
  overflow-y: auto;
}
.product-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 0.6rem;
  cursor: pointer;
  transition: 0.2s;
}
.product-card:hover {
  border-color: #007ad9;
}
.order-panel {
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 1rem;
  background: #fafafa;
  display: flex;
  flex-direction: column;
  height: 75vh;
}
.order-list {
  flex: 1;
  overflow-y: auto;
  list-style: none;
  padding: 0;
  margin: 0;
}
.order-item {
  display: flex;
  justify-content: space-between;
  border-bottom: 1px solid #eee;
  padding: 0.4rem 0;
}
.payment-summary {
  border-top: 1px solid #ddd;
  margin-top: 1rem;
  padding-top: 1rem;
}
.payment-methods {
  display: flex;
  justify-content: space-between;
  margin-top: 0.8rem;
  gap: 0.6rem;
}
.method-btn {
  flex: 1;
}
.pay-btn {
  width: 100%;
  background-color: #0f172a;
  color: #fff;
  margin-top: 0.8rem;
  border-radius: 8px;
}

/* 매출 내역 (탭2) */
.sales-header {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
}
.stat-card {
  flex: 1;
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 1rem;
  background: #fafafa;
}
.value {
  font-size: 1.3rem;
  color: #007ad9;
  font-weight: bold;
}
.diff {
  color: #2ecc71;
  font-size: 0.85rem;
}
.sales-search {
  display: flex;
  justify-content: space-between;
  margin-bottom: 1rem;
}
.search-input {
  width: 250px;
}
.price {
  color: #f39c12;
  font-weight: bold;
}
.method {
  background: #eef;
  padding: 0.3rem 0.6rem;
  border-radius: 6px;
}

/* 월별 매출 (탭3) */
.month-header {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
}
.stat-card {
  flex: 1;
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 1rem;
  background: #fafafa;
}
.value {
  font-size: 1.3rem;
  color: #007ad9;
  font-weight: bold;
}
.diff {
  color: #2ecc71;
  font-size: 0.85rem;
}

.calendar-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 0.8rem;
}
.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 0.5rem;
}
.day-card {
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 0.8rem;
  background: #fff;
  height: 110px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.day-card.empty {
  background: #fafafa;
  border: none;
}

.day-number {
  font-size: 1.1rem;
  font-weight: bold;
  color: #333;
}
.day-number.sunday {
  color: #e74c3c; /* 빨간색 */
}
.day-number.saturday {
  color: #007ad9; /* 파란색 */
}
.holiday-text {
  display: block;
  font-size: 0.9rem;
  color: #e74c3c;
  margin-top: 0.2rem;
}
.day-sales {
  text-align: right;
  font-size: 1rem;
  color: #222;
  font-weight: 400;
}
</style>
