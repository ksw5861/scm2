<template>
  <div class="pos-dashboard">
    <!-- 탭 -->
<div class="tab-container">
  <Button
    v-for="tab in tabs"
    :key="tab"
    :label="tab"
    class="tab-btn"
    :class="{ active: activeTab === tab }"
    @click="activeTab = tab"
  />

  <!-- ✅ 여기! 닫기 전에 -->
  <div class="pos-info">
    <strong>{{ userStore.name }}</strong> | {{ currentTime }}
  </div>
</div>


    <!-- ================== 탭1: 매출 발생 ================== -->
    <div v-if="activeTab === '매출 발생'" class="tab-content">
      <div class="pos-body">
        <!-- 상품 리스트 -->
        <div class="product-list">
          <div
            v-for="(item, index) in filteredProducts"
            :key="item.id"
            class="product-card"
            :class="{ empty: item.posShowYn === 'N' }"
            @click="addToOrder(item)"
          >
            <template v-if="item.posShowYn === 'Y'">
              <div class="product-index">{{ index + 1 }}</div>
              <div class="product-name">{{ item.name }}</div>
              <div class="product-price">{{ item.price.toLocaleString() }}원</div>
  
            </template>
            <template v-else>
              <div class="empty-cell">—</div>
            </template>
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

    <!-- ================== 탭2: 매출 내역 ================== -->
    <div v-else-if="activeTab === '매출 내역'" class="tab-content">
      <div class="sales-header">
        <div class="stat-card">
          <h3>오늘 총 매출</h3>
          <p class="value">{{ totalSales.toLocaleString() }}원</p>
          <p class="diff" :class="{ up: salesChange > 0, down: salesChange < 0 }">
            {{ salesChange > 0 ? '+' : '' }}{{ salesChange }}% vs 어제
          </p>
        </div>

        <div class="stat-card">
          <h3>주문 건수</h3>
          <p class="value">{{ todayCount }}건</p>
          <p class="diff" :class="{ up: countChange > 0, down: countChange < 0 }">
            {{ countChange > 0 ? '+' : '' }}{{ countChange }}건 vs 어제
          </p>
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
        <Column field="saleId" header="주문번호" />
        <Column field="saleDate" header="일시" />
        <Column field="saleTotalAmount" header="금액">
          <template #body="{ data }">
            <span class="price">{{ data.saleTotalAmount.toLocaleString() }}원</span>
          </template>
        </Column>
        <Column field="salePayType" header="결제방법">
  <template #body="{ data }">
    <span
      class="method"
      :style="{
        backgroundColor: data.salePayType === 'CARD' ? '#93c5fd' : '#c7d2fe',
        color: '#1e293b',                // 글씨는 가독성 좋은 딥그레이
        padding: '4px 8px',
        borderRadius: '8px',
        fontWeight: '400'
      }"
    >
      {{ data.salePayType === 'CARD' ? '카드' : '현금' }}
    </span>
  </template>
</Column>

      </DataTable>
    </div>

    <!-- ================== 탭3: 월별 매출 ================== -->
    <div v-else-if="activeTab === '월별 매출'" class="tab-content">
      <div class="month-header">
        <div class="stat-card">
          <h3>월 총 매출</h3>
          <p class="value">{{ monthlySummary.total.toLocaleString() }}원</p>
          <p class="diff" :class="{ up: monthlyChange > 0, down: monthlyChange < 0 }">
            {{ monthlyChange > 0 ? '+' : '' }}{{ monthlyChange }}% vs 전월
          </p>
        </div>
        <div class="stat-card">
          <h3>일평균 매출</h3>
          <p class="value">{{ dailyAvg.toLocaleString() }}원</p>
          <p class="diff">영업일 기준</p>
        </div>
        <div class="stat-card">
          <h3>영업일수</h3>
          <p class="value">{{ monthlySummary.workingDays }}일</p>
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
              <div class="day-sales">{{ day.sales.toLocaleString() }}원</div>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from "vue"
import axios from "axios"
import Button from "primevue/button"
import InputText from "primevue/inputtext"
import DataTable from "primevue/datatable"
import Column from "primevue/column"
import { useUserStore } from "@/stores/user"


/* =============================
 🇰🇷 공휴일 자동 생성 (양력 + 명절 하드코딩)
============================= */

// ✅ 1. 고정 양력 공휴일
function generateFixedHolidays(startYear = 2000, endYear = 2100) {
  const fixedDays = [
    { month: 1, day: 1, name: "신정" },
    { month: 3, day: 1, name: "삼일절" },
    { month: 5, day: 5, name: "어린이날" },
    { month: 6, day: 6, name: "현충일" },
    { month: 8, day: 15, name: "광복절" },
    { month: 10, day: 3, name: "개천절" },
    { month: 10, day: 9, name: "한글날" },
    { month: 12, day: 25, name: "성탄절" },
  ]

  const holidays = []
  for (let year = startYear; year <= endYear; year++) {
    for (const f of fixedDays) {
      holidays.push({
        date: `${year}-${String(f.month).padStart(2, "0")}-${String(f.day).padStart(2, "0")}`,
        name: f.name,
      })
    }
  }
  return holidays
}

// ✅ 2. 명절 하드코딩 (매년 변하는 음력 명절 → 양력 변환값 반영)
const fixedLunarHolidays = [
  // 🧧 2025년 기준 (참고: 천문연구원 공휴일 데이터)
  { date: "2025-01-28", name: "설날 연휴" },
  { date: "2025-01-29", name: "설날" },
  { date: "2025-01-30", name: "설날 연휴" },
  { date: "2025-05-05", name: "석가탄신일" }, // 어린이날과 동일날
  { date: "2025-10-06", name: "추석" },
  { date: "2025-10-07", name: "추석 연휴" },
  { date: "2025-10-08", name: "추석 연휴" },

  // 2026년
  { date: "2026-02-16", name: "설날 연휴" },
  { date: "2026-02-17", name: "설날" },
  { date: "2026-02-18", name: "설날 연휴" },
  { date: "2026-05-24", name: "석가탄신일" },
  { date: "2026-09-24", name: "추석 연휴" },
  { date: "2026-09-25", name: "추석" },
  { date: "2026-09-26", name: "추석 연휴" },
]

// ✅ 3. 전체 병합
const fixedHolidays = [...generateFixedHolidays(2000, 2100), ...fixedLunarHolidays]

console.log("✅ 총 공휴일:", fixedHolidays.length)

/* =============================
  💳 매출 발생
============================= */
const tabs = ["매출 발생", "매출 내역", "월별 매출"]
const activeTab = ref("매출 발생")
const currentTime = ref('')

const userStore = useUserStore()
const vendorId = userStore.code

const productList = ref([])
const orderList = ref([])
const paymentMethod = ref("")

const fetchPosProducts = async () => {
  const { data } = await axios.get("/api/sales/margin/list", { params: { vendorId } })
  productList.value = data.sort((a, b) => a.sortNo - b.sortNo).map((p) => ({
    id: p.saleProdId,
    name: p.saleProdName,
    price: p.saleProdPrice,
    vendorId: vendorId,
    posShowYn: p.posShowYn,
  }))
}


onMounted(() => {
  fetchPosProducts(),
  setInterval(() => {
    const now = new Date()
    currentTime.value = now.toLocaleString('ko-KR', { 
      year: 'numeric', month: '2-digit', day: '2-digit',
      hour: '2-digit', minute: '2-digit', second: '2-digit'
    })
  }, 1000)
})

const filteredProducts = computed(() => productList.value)
const addToOrder = (item) => {
  if (item.posShowYn === "N") return
  const found = orderList.value.find((o) => o.id === item.id)
  found ? found.qty++ : orderList.value.push({ ...item, qty: 1 })
}
const increaseQty = (o) => o.qty++
const decreaseQty = (o) => (o.qty > 1 ? o.qty-- : removeOrder(o.id))
const removeOrder = (id) => (orderList.value = orderList.value.filter((o) => o.id !== id))
const subTotal = computed(() => orderList.value.reduce((sum, o) => sum + o.price * o.qty, 0))
const tax = computed(() => Math.round(subTotal.value * 0.1))
const total = computed(() => subTotal.value + tax.value)
const handlePayment = async () => {
  if (!paymentMethod.value) return alert("결제 방식을 선택해주세요")
  const payload = {
    salesDetails: orderList.value.map((o) => ({
      saleProdId: o.id,
      saleProdName: o.name,
      saleQty: o.qty,
      saleProdPrice: o.price,
      prodUnitPrice: o.price,
      saleMargin: 0,
    })),
    salePayType: paymentMethod.value === "card" ? "CARD" : "CASH",
    saleTotalAmount: total.value,
    vendorId:vendorId,
  }
  await axios.post("/api/sales/register", payload)
  alert("✅ 결제가 완료되었습니다!")
  orderList.value = []
  paymentMethod.value = ""
}

/* =============================
  📊 매출 내역
============================= */

// ✅ 날짜 변수 분리 (문자열용 + Date 객체용)
const todayDate = new Date()
const todayStr = todayDate.toISOString().split("T")[0]

const salesList = ref([])
const search = ref("")
const dailySummary = ref({})

const fetchDailySummary = async () => {
  try {
    const { data } = await axios.get("/api/sales/daily-summary", { params: { vendorId } })
    dailySummary.value = data
  } catch (err) {
    console.error("❌ 일별 요약 조회 실패:", err)
  }
}

// ✅ 어제 대비 증감률 계산
const salesChange = computed(() => {
  const todaySales = Number(dailySummary.value.today || 0)
  const yesterdaySales = Number(dailySummary.value.yesterday || 0)
  if (!yesterdaySales) return 0
  return (((todaySales - yesterdaySales) / yesterdaySales) * 100).toFixed(1)
})

const countChange = computed(() => {
  const todayCount = Number(dailySummary.value.todayCount || 0)
  const yesterdayCount = Number(dailySummary.value.yesterdayCount || 0)
  return todayCount - yesterdayCount
})

// ✅ 오늘 매출만 필터링해서 합계
const totalSales = computed(() =>
  salesList.value
    .filter((s) => s.saleDate?.startsWith(todayStr))
    .reduce((sum, s) => sum + (s.saleTotalAmount || 0), 0)
)

// ✅ 오늘 주문 건수
const todayCount = computed(() =>
  salesList.value.filter((s) => s.saleDate?.startsWith(todayStr)).length
)

const fetchSalesHistory = async () => {
  try {
    const { data } = await axios.get("/api/sales/history", { params: { vendorId } })
    salesList.value = data
  } catch (err) {
    console.error("❌ 매출내역 조회 실패:", err)
  }
}

const filteredSales = computed(() => {
  if (!search.value) return salesList.value
  return salesList.value.filter((s) =>
    s.saleId?.toLowerCase().includes(search.value.toLowerCase())
  )
})

// ✅ 평균 주문 금액 (오늘 기준)
const avgOrder = computed(() => {
  const todaySalesList = salesList.value.filter((s) => s.saleDate?.startsWith(todayStr))
  return todaySalesList.length
    ? Math.round(totalSales.value / todaySalesList.length)
    : 0
})


/* =============================
  📅 월별 매출
============================= */
const monthlySummary = ref({
  total: 0,
  workingDays: 0,
  lastMonthTotal: 0,
  dailySales: [],
})

const monthlyChange = computed(() => {
  const { total, lastMonthTotal } = monthlySummary.value
  if (!lastMonthTotal || lastMonthTotal === 0) return 0
  return (((total - lastMonthTotal) / lastMonthTotal) * 100).toFixed(1)
})

const dailyAvg = computed(() =>
  monthlySummary.value.workingDays
    ? Math.round(monthlySummary.value.total / monthlySummary.value.workingDays)
    : 0
)


const year = ref(todayDate.getFullYear())
const month = ref(todayDate.getMonth())

const daysInMonth = ref([])

const fetchMonthlySummary = async () => {
  try {
    const { data } = await axios.get("/api/sales/monthly-summary", {
      params: { vendorId, year: year.value, month: month.value + 1 },
    })
    monthlySummary.value = data
    generateCalendar()
  } catch (err) {
    console.error("❌ 월별 매출 요약 조회 실패:", err)
  }
}

function generateCalendar() {
  const firstDay = new Date(year.value, month.value, 1).getDay()
  const lastDate = new Date(year.value, month.value + 1, 0).getDate()
  const salesData = monthlySummary.value.dailySales || []
  const newDays = []

  for (let i = 0; i < firstDay; i++) newDays.push({ empty: true })

  const normalizeDate = (str) => {
    const [y, m, d] = str.split("-").map((v) => String(Number(v)))
    return `${y}-${m.padStart(2, "0")}-${d.padStart(2, "0")}`
  }

  for (let d = 1; d <= lastDate; d++) {
    const dateObj = new Date(year.value, month.value, d)
    const dateStr = `${year.value}-${String(month.value + 1).padStart(2, "0")}-${String(d).padStart(2, "0")}`
    const weekday = dateObj.getDay()

    const holidayObj = fixedHolidays.find(
      (h) => normalizeDate(h.date) === normalizeDate(dateStr)
    )
    const found = salesData.find(
      (s) => s.SALE_DATE_STR === dateStr || s.sale_date_str === dateStr
    )
    const sales = found ? found.AMOUNT || found.amount || 0 : 0

    newDays.push({
      day: d,
      weekday,
      date: dateStr,
      sales,
      holiday: holidayObj ? holidayObj.name : null,
    })
  }
  daysInMonth.value = newDays
}

const prevMonth = () => {
  if (month.value === 0) {
    month.value = 11
    year.value--
  } else month.value--
  fetchMonthlySummary()
}

const nextMonth = () => {
  if (month.value === 11) {
    month.value = 0
    year.value++
  } else month.value++
  fetchMonthlySummary()
}

watch(activeTab, (tab) => {
  if (tab === "매출 내역") {
    fetchSalesHistory()
    fetchDailySummary()
  }
  if (tab === "월별 매출") {
    fetchMonthlySummary()
  }
})
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
.tab-container {
  display: flex;
  justify-content: space-between; /* 왼쪽 버튼들 + 오른쪽 정보 분리 */
  align-items: center;
}

.pos-info {
  margin-left: auto; 
  font-size: 1.0rem;
  color: #555;
}

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
.product-card.empty {
  background-color: #f8f8f8;
  border: 1px dashed #ccc;
  cursor: default;
}
.product-card.empty:hover {
  border-color: #ccc;
}
.empty-cell {
  text-align: center;
  color: #bbb;
  font-size: 1.2rem;
  margin-top: 1.8rem;
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
.diff.up {
  color: #2ecc71; /* 상승: 초록색 */
}
.diff.down {
  color: #e74c3c; /* 하락: 빨간색 */
}
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
  font-size: 1.3rem;
  color: #222;
  font-weight: 400;
}
</style>
