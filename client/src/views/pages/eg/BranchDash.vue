<template>
  <div class="main">
    <header class="header">
      <div>
        <h2 class="title">대시보드 {{ userStore.name }}님 환영합니다!</h2>
        <p class="subtitle">{{ today }} | 가맹점 운영 현황</p>
      </div>
      <div class="header-ctrl">
        <Button icon="pi pi-search" class="p-button-text" />
        <Badge :value="3" severity="danger" class="mr-2" />
        <Dropdown v-model="selectedStore" :options="stores" optionLabel="label" class="mr-2" />
        <Button label="로그아웃" size="small" />
      </div>
    </header>

    <div class="card-grid-4">
      <Card class="quick-card" @click="$router.push('/insertorder')" style="cursor:pointer;">
        <template #title><i class="pi pi-shopping-cart mr-2"></i>발주 요청</template>
        <template #content>필요한 원부자재를 발주하세요.</template>
      </Card>

      <Card class="quick-card" @click="$router.push('/insertreturn')" style="cursor:pointer;">
        <template #title><i class="pi pi-refresh mr-2"></i>반품 신청</template>
        <template #content>불량/오배송 반품 진행.</template>
      </Card>

      <Card class="quick-card" @click="$router.push('/insertpay')" style="cursor:pointer;">
        <template #title><i class="pi pi-wallet mr-2"></i>납부 등록</template>
        <template #content>이번 달 납부 내역 등록.</template>
      </Card>

      <Card>
        <template #title><i class="pi pi-exclamation-triangle mr-2"></i>연체 주문</template>
        <template #content>
          {{ overdue.count }}건 | {{ asKRW(overdue.amount) }}
        </template>
      </Card>
    </div>

    <div class="kpi-row">
      <Card class="kpi">
        <template #title>오늘 매출</template>
        <template #content>
          <div class="kpi-value">
            {{ asKRW(kpi.todaySales) }}
            <span
              v-if="kpi.dailyRate !== null"
              :style="{ color: kpi.dailyRate > 0 ? '#22c55e' : (kpi.dailyRate < 0 ? '#ef4444' : '#6b7280') }"
              class="kpi-rate"
            >
              {{ kpi.dailyRate > 0 ? '▲' : (kpi.dailyRate < 0 ? '▼' : '') }}
              {{ Math.abs(kpi.dailyRate).toFixed(1) }}%
            </span>
          </div>
        </template>
      </Card>

      <Card class="kpi">
        <template #title>이번 달 매출</template>
        <template #content>
          <div class="kpi-value">
            {{ asKRW(kpi.monthSales) }}
            <span
              v-if="kpi.monthRate !== null"
              :style="{ color: kpi.monthRate > 0 ? '#22c55e' : (kpi.monthRate < 0 ? '#ef4444' : '#6b7280') }"
              class="kpi-rate"
            >
              {{ kpi.monthRate > 0 ? '▲' : (kpi.monthRate < 0 ? '▼' : '') }}
              {{ Math.abs(kpi.monthRate).toFixed(1) }}%
            </span>
          </div>
        </template>
      </Card>

      <Card class="kpi">
        <template #title>다음 결제기한 금액</template>
        <template #content>
          <div class="kpi-value">{{ asKRW(kpi.nextDueAmount) }}</div>
          <div class="kpi-sub"><i class="pi pi-clock"></i> 기준일: {{ formatDate(kpi.nextDueDate) }}</div>
        </template>
      </Card>

      <Card class="kpi">
      <template #title>여신한도 잔액</template>
      <template #content>
        <div class="kpi-value">{{ asKRW(kpi.remainCredit) }}</div>  
        <div class="kpi-sub">
          <i class="pi pi-truck"></i>
          여신한도: {{ asKRW(kpi.creditLimit) }}
        </div> 
      </template>
    </Card>
  </div>

    <Card class="chart-card">
      <template #title>
        매출 분석
        <div class="flex items-center gap-2 ml-2">
          <Dropdown
            v-model="selectedRange"
            :options="rangeOptions"
            optionLabel="label"
            optionValue="value"
            size="small"
            @change="onRangeChange"
          />

          <template v-if="selectedRange === 'daily'">
            <div class="flex gap-1 ml-2">
              <Button
                v-for="d in dayOptions"
                :key="d"
                :label="`최근 ${d}일`"
                size="small"
                :severity="selectedDays === d ? 'primary' : 'secondary'"
                @click="updateDays(d)"
              />
            </div>
          </template>

          <template v-else-if="selectedRange === 'monthly'">
            <div class="flex items-center gap-2 ml-2">
              <Dropdown
                v-model="selectedYear"
                :options="yearOptions"
                size="small"
                @change="updateMonth"
              />
              <Dropdown
                v-model="selectedMonth"
                :options="monthOptions"
                size="small"
                @change="updateMonth"
              >
                <template #option="slotProps">
                  <span>{{ slotProps.option }}월</span>
                </template>
                <template #value="slotProps">
                  <span>{{ slotProps.value }}월</span>
                </template>
              </Dropdown>
            </div>
          </template>
        </div>
      </template>

      <template #content>
        <TabView>
          <TabPanel header="매출 추이">
            <Chart type="line" :data="trendData" :options="trendOptions" />
          </TabPanel>

          <TabPanel header="매출-주문">
            <Chart type="bar" :data="profitData" :options="profitOptions" />
          </TabPanel>

          <TabPanel header="작년 vs 올해">
            <Chart type="bar" :data="compareData" :options="compareOptions" />
          </TabPanel>

          <TabPanel header="원두 판매 랭킹">
            <div v-if="beanRank.length" class="bean-rank-wrap">
              <div v-for="bean in beanRank" :key="bean.LABEL ?? bean.name" class="bean-rank">
                <div class="bean-row">
                  <div class="bean-info">
                    <strong>{{ bean.LABEL ?? bean.name }}</strong>
                    <small v-if="bean.category">{{ bean.category }}</small>
                  </div>
                  <div class="bean-percent">{{ roundRate(bean.RATE ?? bean.rate) }}%</div>
                </div>
                <ProgressBar :value="roundRate(bean.RATE ?? bean.rate)" :showValue="false" style="height:10px" />
              </div>
            </div>
            <Message v-else severity="info" class="mt-2">표시할 랭킹 데이터가 없습니다.</Message>
          </TabPanel>

          <TabPanel header="결제수단별">
            <Chart type="bar" :data="payMethodData" :options="payMethodOptions" />
          </TabPanel>
        </TabView>
      </template>
    </Card>

    <Card class="table-card">
      <template #title>진행중인 주문건</template>
      <template #content>
        <div class="table-toolbar">
          <span class="p-input-icon-left">
            <i class="pi pi-search" />
            <InputText v-model="orderQuery" placeholder="주문번호 또는 제품명 검색" />
          </span>
        </div>
        <DataTable :value="filteredOrders" paginator :rows="7" dataKey="orderId" :rowHover="true" class="p-datatable-sm">
          <Column field="orderId" header="주문코드" style="width:160px" />
          <Column field="prodName" header="제품명" />
          <Column header="총합계금액" style="width:140px; text-align:right">
            <template #body="{ data }">{{ asKRW(data.totalPrice) }}</template>
          </Column>
          <Column header="출고일자" style="width:130px">
            <template #body="{ data }">{{ formatDate(data.sendDate) }}</template>
          </Column>
          <Column header="주문상태" style="width:120px">
            <template #body="{ data }"><Tag :value="data.status" :severity="statusColor(data.status)" /></template>
          </Column>
          <Column header="결제기한" style="width:130px">
            <template #body="{ data }">{{ formatDate(data.paydueDate) }}</template>
          </Column>
        </DataTable>
      </template>
    </Card>

    <div class="two-col">
      <Card class="notice-card">
        <template #title>공지사항</template>
        <template #content>
          <div class="table-toolbar">
            <span class="muted">최근 등록된 공지사항입니다.</span>
          </div>

          <DataTable
            :value="notices"
            dataKey="noticeNo"
            class="p-datatable-sm clickable-rows" 
            paginator
            :rows="5"
            :rowHover="true"
            style="min-height: 250px"
            @row-click="openNoticeModal" >
            <Column field="noticeNo" header="공지번호" style="width:100px; text-align:center" />
            <Column field="title" header="제목" />
            <Column field="author" header="작성자" style="width:140px; text-align:center" />
            <Column field="createdAt" header="작성일자" style="width:180px; text-align:center" />
          </DataTable>

          <div class="more">
            <Button
              label="모든 공지 보기"
              class="p-button-text"
              @click="$router.push('/notice')"
            />
          </div>
        </template>
      </Card>
    </div>

    <Dialog 
        v-model:visible="noticeModalVisible" 
        modal 
        :closable="false"  
        :style="{ width: '50vw' }"
        :breakpoints="{ '1199px': '75vw', '575px': '90vw' }"
    >
        <template #header>
            <div class="flex flex-column gap-1">
                <h3 style="margin: 0; padding: 0;">{{ selectedNotice.title }}</h3>
                <div class="flex align-items-center text-sm text-500 notice-header-info">
                    <span class="mr-3 text-700"><i class="pi pi-user mr-1"></i> {{ selectedNotice.author }}</span>
                    <span><i class="pi pi-calendar mr-1"></i> {{ formatDate(selectedNotice.createdAt) }}</span>
                </div>
            </div>
        </template>
        
        <div v-html="selectedNotice.content" class="notice-content-body"></div>
        
        <template #footer>
            <Button label="닫기" icon="pi pi-times" @click="noticeModalVisible = false" text />
        </template>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { useUserStore } from '@/stores/user'

import {
  Chart as ChartJS,
  Title, Tooltip, Legend, LineElement, BarElement, PointElement,
  CategoryScale, LinearScale, TimeScale, Filler
} from 'chart.js'

// ✅ 타임스케일 어댑터 반드시 필요
import 'chartjs-adapter-date-fns'

ChartJS.register(
  Title, Tooltip, Legend, LineElement, BarElement, PointElement,
  CategoryScale, LinearScale, TimeScale, Filler
)

import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import ProgressBar from 'primevue/progressbar'
import Dialog from 'primevue/dialog'


ChartJS.register(
  Title, Tooltip, Legend, LineElement, BarElement, PointElement,
  CategoryScale, LinearScale, TimeScale, Filler
)

const userStore = useUserStore();
const vendorId = userStore.code;

// 선택 매장
const selectedStore = ref(null)
const stores = ref([])

// ✅ 공지 모달 관련 상태
const noticeModalVisible = ref(false)
const selectedNotice = ref({
    noticeNo: null,
    title: '공지 상세 정보 로딩 중...',
    content: '잠시만 기다려주세요...',
    author: '',
    createdAt: ''
})

// 유틸
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr
  return date.toLocaleDateString('ko-KR')
}
const today = new Date().toISOString().substring(0, 10)
const asKRW = (value) => (value ?? 0).toLocaleString('ko-KR') + '원'
const statusColor = (status) => {
  switch (status) {
    case '대기': return 'warning'
    case '처리중': return 'success'
    case '처리완료': return 'danger'
    case '배송준비중': return 'info'
    case '출고완료': return 'danger'
    default: return 'secondary'
  }
}

// ===== KPI (로직 유지) =====
const kpi = ref({
  todaySales: 0,
  monthSales: 0,
  creditLimit: 0,
  nextDueAmount: 0,
  nextDueDate: '',
  dailyRate: 0,
  monthRate: 0
})
const overdue = ref({
  count: 0,
  amount: 0
})

const fetchKpi = async () => {
  try {
    const { data: daily } = await axios.get('/api/sales/daily-summary', { params: { vendorId } })
    const todaySales = Number(daily.today || 0)
    const yesterdaySales = Number(daily.yesterday || 0)
    const dailyRate = yesterdaySales > 0 ? (((todaySales - yesterdaySales) / yesterdaySales) * 100).toFixed(1) : 0

    const now = new Date()
    const { data: monthly } = await axios.get('/api/sales/monthly-summary', {
      params: { vendorId, year: now.getFullYear(), month: now.getMonth() + 1 }
    })
    const monthSales = Number(monthly.total || 0)
    const lastMonthSales = Number(monthly.lastMonthTotal || 0)
    const monthRate = lastMonthSales > 0 ? (((monthSales - lastMonthSales) / lastMonthSales) * 100).toFixed(1) : 0

    const { data: finance } = await axios.get('/api/sales/finance-summary', { params: { vendorId } })

    kpi.value = {
      todaySales,
      monthSales,
      dailyRate,
      monthRate,
      creditLimit: Number(finance.creditLimit || 0), 
      nextDueAmount: Number(finance.nextDueAmount || 0), 
      nextDueDate: finance.nextDueDate || '', 
      remainCredit: Number(finance.remainCredit || 0) 
    }
  } catch (err) {
    console.error('❌ KPI 불러오기 오류:', err)
  }
}

// 연체중인 주문건
const fetchOverdue = async () => {
  try {
    const { data } = await axios.get('/api/orders/overdue-summary', { params: { vendorId } })
    
    // ✅ 배열로 오면 첫 번째 객체만 사용
    const obj = Array.isArray(data) ? data[0] : data
    
    overdue.value = {
      count: obj.COUNT || 0,
      amount: obj.AMOUNT || 0
    }
  } catch (err) {
    console.error('연체 요약 조회 오류:', err)
  }
}


// ===== 진행중인 주문 목록 (수정된 Vue 로직) =====
const orders = ref([])
const orderQuery = ref('')

const fetchOrders = async () => {
  try {
    console.log("✅ vendorId:", vendorId)
    const { data } = await axios.get('/api/orders/ongoing', { params: { vendorId } }) 
    
    // 백엔드에서 이미 '배송완료'를 제외하고 리스트를 반환하므로, 
    // data.data 대신 바로 data를 사용합니다. (컨트롤러 응답 형태에 따라)
    orders.value = data 
    
  } catch (err) {
    // 204 No Content도 여기서 처리될 수 있습니다.
    if (err.response && err.response.status === 204) {
      orders.value = [];
    } else {
      console.error('진행중 주문 조회 오류:', err)
    }
  }
}

// ✅ 백엔드에서 이미 필터링했으므로, 여기서는 검색어 필터링만 유지하면 됩니다.
const filteredOrders = computed(() => {
  return orders.value.filter(o =>
    !orderQuery.value ||
    o.orderId?.includes(orderQuery.value) ||
    (o.prodName && o.prodName.includes(orderQuery.value))
  );
});

// ===== 공지 (로직 수정) =====
const notices = ref([])

const fetchNotices = async () => {
  try {
    const { data, status } = await axios.get('/api/notice', {
      params: { page: 1, size: 5, title: '' } 
    });

    if (status === 204) {
      notices.value = []
      return
    }

    notices.value = data.data 
  } catch (err) {
    console.error('대시보드 공지 조회 오류:', err)
  }
}

// ✅ 공지사항 상세 내용을 가져오는 함수 (모달용)
const fetchNoticeDetail = async (noticeNo) => {
    try {
        const { data } = await axios.get(`/api/notice/${noticeNo}`); 
        
        // 컨트롤러 응답 구조 ({ "data": NoticeVO 객체 })에 맞춰 data.data 반환
        return data.data; 
        
    } catch (err) {
        console.error(`공지 ${noticeNo} 상세 조회 오류:`, err);
        return { 
            title: '오류', 
            content: '공지 내용을 불러오는 데 실패했습니다.', 
            author: '시스템', 
            createdAt: new Date().toISOString() 
        };
    }
}

// ✅ 모달을 열고 상세 내용을 불러오는 함수
const openNoticeModal = async (event) => {
  // PrimeVue DataTable의 @row-click 이벤트 객체에서 data (행 데이터)를 가져옴
  const noticeNo = event.data?.noticeNo;
  
  // 1. 모달을 열기 전에 로딩 상태 데이터 설정 (사용자에게 반응성을 보여줌)
  selectedNotice.value = {
    noticeNo,
    title: '공지 상세 정보 로딩 중...',
    content: '<p>잠시만 기다려주세요...</p>',
    author: '로딩',
    createdAt: ''
  };
  
  // 2. 모달을 띄움
  noticeModalVisible.value = true;
  
  // 3. 상세 API 호출 및 데이터 업데이트
  if (noticeNo) {
    const detail = await fetchNoticeDetail(noticeNo);
    
    // 상세 정보로 selectedNotice 업데이트
    selectedNotice.value = {
        noticeNo: detail.noticeNo || noticeNo,
        title: detail.title || '제목 없음',
        content: detail.content || '내용 없음',
        author: detail.author || '관리자',
        createdAt: detail.createdAt || new Date().toISOString()
    };
  }
}


// ===== Range 공통 상태 및 차트 (로직 유지) =====
const selectedRange = ref('daily')
const rangeOptions = [
  { label: '일별', value: 'daily' },
  { label: '월별', value: 'monthly' }
]

const selectedDays = ref(7)
const dayOptions = [7, 15, 21]

const selectedYear = ref(new Date().getFullYear())
const selectedMonth = ref(new Date().getMonth() + 1)
const yearOptions = [new Date().getFullYear() - 2, new Date().getFullYear() - 1, new Date().getFullYear(), new Date().getFullYear() + 1]
const monthOptions = Array.from({ length: 12 }, (_, i) => i + 1)

// ===== 차트: 매출 추이 =====
const trendData = ref({ labels: [], datasets: [] })
const trendOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { position: 'bottom' } },
  scales: {
    x: { type: selectedRange.value === 'daily' ? 'time' : 'category', time: selectedRange.value === 'daily' ? { unit: 'day' } : undefined, ticks: { autoSkip: true, maxTicksLimit: 8 } },
    y: { beginAtZero: true, ticks: { callback: (v) => (v / 10000).toLocaleString() + '만 원' } }
  },
  elements: { line: { tension: 0.35 }, point: { radius: 2 } }
}))

const fetchTrend = async () => {
  try {
    const { data } = await axios.get('/api/branch/salestrend', { params: { vendorId, range: selectedRange.value } })
    let labels = (data || []).map(d => d.LABEL)
    let orderAmt = (data || []).map(d => Number(d.ORDER_AMT ?? 0))
    let salesAmt = (data || []).map(d => Number(d.SALES_AMT ?? 0))

    ;({ labels, datasets: [orderAmt, salesAmt] } = applyTimeFilter(labels, [orderAmt, salesAmt]))

    trendData.value = {
      labels,
      datasets: [
        { label: '본사 주문금액', data: orderAmt, borderColor: '#93c5fd', backgroundColor: 'rgba(147,197,253,0.20)', fill: true },
        { label: '실제 매출금액', data: salesAmt, borderColor: '#4F46E5', backgroundColor: 'rgba(79,70,229,0.10)', fill: true }
      ]
    }
  } catch (err) {
    console.error('매출 추이 조회 오류:', err)
    trendData.value = { labels: [], datasets: [] }
  }
}

// ===== 차트: 순이익 =====
const profitData = ref({ labels: [], datasets: [] })
const profitOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { display: false } },
  scales: {
    x: { type: selectedRange.value === 'daily' ? 'time' : 'category', time: selectedRange.value === 'daily' ? { unit: 'day' } : undefined, ticks: { autoSkip: true, maxTicksLimit: 8 } },
    y: { beginAtZero: true, ticks: { callback: (v) => (v / 10000).toLocaleString() + '만 원' } }
  }
}))
const fetchProfit = async () => {
  try {
    const { data } = await axios.get('/api/branch/salestrend', { params: { vendorId, range: selectedRange.value } })
    let labels = (data || []).map(d => d.LABEL)
    let orderAmt = (data || []).map(d => Number(d.ORDER_AMT ?? 0))
    let salesAmt = (data || []).map(d => Number(d.SALES_AMT ?? 0))
    let profit = salesAmt.map((v, i) => v - (orderAmt[i] ?? 0))

    ;({ labels, datasets: [profit] } = applyTimeFilter(labels, [profit]))

    profitData.value = {
      labels,
      datasets: [{ label: '순이익', data: profit, backgroundColor: profit.map(v => v >= 0 ? '#22c55e' : '#ef4444') }]
    }
  } catch (err) {
    console.error('순이익 조회 오류:', err)
    profitData.value = { labels: [], datasets: [] }
  }
}

// ===== 차트: 작년 vs 올해 =====
const compareData = ref({ labels: [], datasets: [] })
const compareOptions = computed(() => ({
  responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom' } },
  scales: { x: { type: 'category', ticks: { autoSkip: true, maxTicksLimit: 8 } }, y: { beginAtZero: true, ticks: { callback: (v) => (v / 10000).toLocaleString() + '만 원' } } }
}))
const fetchCompare = async () => {
  try {
    const { data } = await axios.get('/api/branch/salescompare', { params: { vendorId, range: selectedRange.value } })
    let list = data?.compareData ?? []
    let labels = list.map(d => d.LABEL)
    let lastYear = list.map(d => Number(d.LAST_YEAR ?? 0))
    let thisYear = list.map(d => Number(d.THIS_YEAR ?? 0))

    ;({ labels, datasets: [lastYear, thisYear] } = applyTimeFilter(labels, [lastYear, thisYear]))

    compareData.value = {
      labels,
      datasets: [
        { label: '작년', data: lastYear, backgroundColor: '#93c5fd' },
        { label: '올해', data: thisYear, backgroundColor: '#4F46E5' }
      ]
    }
  } catch (err) {
    console.error('작년 vs 올해 조회 오류:', err)
    compareData.value = { labels: [], datasets: [] }
  }
}

// ===== 원두 랭킹 =====
const beanRank = ref([])
const roundRate = (v) => {
  if (v === null || v === undefined) return 0
  const n = parseFloat(String(v).replace(/[^\d.\-]/g, ''))
  return Number.isFinite(n) ? Math.round(n) : 0
}
const fetchCoffeeRank = async () => {
  try {
    const { data } = await axios.get('/api/branch/coffeerank', { params: { vendorId, range: selectedRange.value } })
    const list = Array.isArray(data) ? data : (data?.list ?? data?.items ?? [])
    beanRank.value = list.map(r => ({ LABEL: r.LABEL ?? r.name ?? '', RATE: roundRate(r.RATE ?? r.rate ?? 0) }))
  } catch (e) {
    console.error('원두 랭킹 조회 오류:', e)
    beanRank.value = []
  }
}

// ===== 결제수단별 =====
const payMethodData = ref({ labels: [], datasets: [] })
const payMethodOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { position: 'bottom' },
    tooltip: { callbacks: { label: (ctx) => `${ctx.dataset.label}: ${ctx.parsed.y.toLocaleString('ko-KR')}원` } }
  },
  scales: {
    x: { stacked: true, type: selectedRange.value === 'daily' ? 'time' : 'category', time: selectedRange.value === 'daily' ? { unit: 'day' } : undefined, ticks: { autoSkip: true, maxTicksLimit: 8 } },
    y: { stacked: true, beginAtZero: true, ticks: { callback: (v) => (v / 10000).toLocaleString() + '만 원' } }
  }
}))
const fetchPayMethod = async () => {
  try {
    const { data } = await axios.get('/api/branch/paymethod', { params: { vendorId, range: selectedRange.value } })
    let labels = (data || []).map(d => d.LABEL)
    let cardAmt = (data || []).map(d => Number(d.CARD ?? 0))
    let cashAmt = (data || []).map(d => Number(d.CASH ?? 0))

    ;({ labels, datasets: [cardAmt, cashAmt] } = applyTimeFilter(labels, [cardAmt, cashAmt]))

    payMethodData.value = {
      labels,
      datasets: [
        { label: '카드', data: cardAmt, backgroundColor: '#93c5fd' },
        { label: '현금', data: cashAmt, backgroundColor: '#c7d2fe' }
      ]
    }
  } catch (err) {
    console.error('결제수단별 매출 조회 오류:', err)
    payMethodData.value = { labels: [], datasets: [] }
  }
}

// ===== 공통 슬라이싱 유틸 (로직 유지) =====
const applyTimeFilter = (labels, datasets) => {
  if (selectedRange.value === 'daily') {
    const n = selectedDays.value
    const start = Math.max(labels.length - n, 0)
    return { labels: labels.slice(start), datasets: datasets.map(d => d.slice(start)) }
  } else {
    const target = `${selectedYear.value}-${String(selectedMonth.value).padStart(2, '0')}`
    const idx = labels.findIndex(l => l === target)
    if (idx >= 0) {
      return { labels: [labels[idx]], datasets: datasets.map(d => [d[idx]]) }
    } else {
      return { labels: [target], datasets: datasets.map(() => [0]) }
    }
  }
}

// ===== 이벤트 핸들러 (로직 유지) =====
const onRangeChange = () => {
  if (selectedRange.value === 'daily') selectedDays.value = 7
  fetchAllCharts()
}
const updateDays = (days) => { selectedDays.value = days; fetchAllCharts() }
const updateMonth = () => { fetchAllCharts() }

// ===== 공통 fetch (로직 유지) =====
const fetchAllCharts = async () => {
    await Promise.all([
      fetchTrend(),
      fetchProfit(),
      fetchCompare(),
      fetchCoffeeRank(),
      fetchPayMethod()
    ])
  }

  // 최초 로드
onMounted(() => {
  fetchKpi()
  fetchOrders()
  fetchNotices()
  fetchAllCharts()
  fetchOverdue()
})
</script>

<style scoped>
.main { min-height: 100vh; background:#f7f8fa; padding:20px 24px; }

/* 헤더 */
.header { display:flex; justify-content:space-between; align-items:center; margin-bottom:16px; }
.title { margin:0; font-size:15px; font-weight:300; }
.subtitle { margin:0; color:#6b7280; }
.header-ctrl { display:flex; align-items:center; }

/* 빠른 작업 / KPI */
.card-grid-4, .kpi-row { display:grid; grid-template-columns: repeat(4, 1fr); gap:12px; margin-bottom:16px; }
.quick-card :deep(.p-card-title){ font-weight:700; font-size:14px; display:flex; align-items:center; }
.kpi :deep(.p-card-title){ color:#6b7280; font-weight:600; }
.kpi-value { font-size:22px; font-weight:800; margin-top:2px; }
.kpi-sub { color:#6b7280; font-size:12px; margin-top:4px; display:flex; align-items:center; gap:6px; }

.quick-card { transition: all 0.2s ease; cursor: pointer; }
.quick-card:hover { transform: translateY(-4px); box-shadow: 0 6px 15px rgba(0, 0, 0, 0.1); }
.quick-card:active { transform: scale(0.97); box-shadow: 0 3px 8px rgba(0, 0, 0, 0.08); }

/* 차트 카드 */
.chart-card { margin-bottom: 16px; }
.chart-card :deep(.p-card-content) {
  position: relative; height: 380px; padding: 24px 16px 16px;
  overflow: hidden; display: flex; flex-direction: column; justify-content: center;
}
.chart-card :deep(canvas) {
  flex: 1; width: 100% !important; height: auto !important; max-height: 340px !important; display: block;
}
.chart-card :deep(.p-chart) canvas { padding: 0 !important; margin: 0 !important; }

/* 원두 랭킹 */
.bean-rank-wrap { display:flex; flex-direction:column; gap:10px; }
.bean-rank { display:block; }
.bean-row { display:flex; justify-content:space-between; align-items:center; margin-bottom:4px; }
.bean-info { display:flex; flex-direction:column; }
.bean-info small { color:#9ca3af; font-size:12px; }
.bean-percent { font-weight:700; color:#4F46E5; width:52px; text-align:right; }

/* 테이블/공통 */
.table-card { margin-bottom:16px; }
.table-toolbar { display:flex; justify-content:flex-end; margin-bottom:10px; }
.muted { color:#9ca3af; font-weight:400; }

/* 2열 */
.two-col { display:grid; grid-template-columns: 1fr; gap:12px; }

/* 공지 리스트 */
.notice-card .notice-list{ list-style:none; padding:0; margin:0; display:flex; flex-direction:column; gap:12px; }
.notice-card .row{ display:flex; align-items:center; gap:8px; }
.notice-card .date{ color:#9ca3af; font-size:12px; margin-left:auto; }
.notice-card .title{ font-weight:700; margin-top:4px; }
.notice-card .desc{ color:#6b7280; margin:.25rem 0 0; font-size:14px; }
.more{ text-align:right; margin-top:8px; }
.notice-no {
  font-weight: 600;
  color: #4F46E5; /* 보라톤 강조 */
  margin-right: 6px;
}

.author {
  color: #6b7280;
  font-size: 13px;
  margin-right: 8px;
}

.date {
  color: #9ca3af;
  font-size: 13px;
  margin-left: auto; /* 오른쪽 끝 정렬 */
}

/* ✅ 클릭 가능한 행에 커서 스타일 추가 */
.notice-card :deep(.p-datatable-sm.clickable-rows .p-datatable-tbody > tr:hover) {
  cursor: pointer;
  background: #f0f4f8; /* 마우스 오버 시 배경색 변경 (선택 사항) */
}

/* ✅ 공지 모달 내용 스타일 */
.notice-content-body {
    max-height: 500px; /* 모달 내용 최대 높이 지정 */
    overflow-y: auto; /* 내용이 길면 스크롤 생성 */
    padding: 10px 0;
    line-height: 1.6;
    color: #374151;
}

.notice-header-info {
  margin-top: 5px;
  font-size: 13px;
  color: #6b7280 !important;
}
.notice-header-info i {
  font-size: 12px;
}

/* 반응형 */
@media (max-width: 1200px){
  .card-grid-4, .kpi-row { grid-template-columns: repeat(2, 1fr); }
  .two-col { grid-template-columns: 1fr; }
}
@media (max-width: 640px){
  .card-grid-4, .kpi-row { grid-template-columns: 1fr; }
}
.kpi-rate { margin-left: 6px; font-size: 14px; font-weight: 600; }
</style>