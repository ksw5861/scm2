<template>
  <div class="main">
    <!-- 헤더 -->
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

    <!-- 공지 배너 -->
    <div class="notice-banners">
      <div class="banner danger">
        <div class="banner-head"><span class="dot danger"></span><strong>긴급 공지</strong></div>
        <ul><li>가격 정책 변경 예정</li><li>시스템 점검: 2025-10-05 02:00~03:00</li></ul>
        <div class="banner-actions">
          <Button label="자세히" size="small" class="p-button-text" />
          <Button label="닫기" size="small" class="p-button-text" />
        </div>
      </div>
      <div class="banner warn">
        <div class="banner-head"><span class="dot warn"></span><strong>알림</strong></div>
        <ul><li>일부 품목 납기 지연 가능</li></ul>
        <div class="banner-actions">
          <Button label="자세히" size="small" class="p-button-text" />
          <Button label="닫기" size="small" class="p-button-text" />
        </div>
      </div>
      <div class="banner info">
        <div class="banner-head"><span class="dot info"></span><strong>업데이트</strong></div>
        <ul><li>반품 신청 UX 개선</li></ul>
      </div>
    </div>

    <!-- 빠른 작업 -->
    <div class="card-grid-4">
      <Card class="quick-card"><template #title><i class="pi pi-shopping-cart mr-2"></i>발주 요청</template><template #content>필요한 원부자재를 발주하세요.</template></Card>
      <Card class="quick-card"><template #title><i class="pi pi-refresh mr-2"></i>반품 신청</template><template #content>불량/오배송 반품 진행.</template></Card>
      <Card class="quick-card"><template #title><i class="pi pi-wallet mr-2"></i>납부 등록</template><template #content>이번 달 납부 내역 등록.</template></Card>
      <Card class="quick-card"><template #title><i class="pi pi-upload mr-2"></i>재고조사 업로드</template><template #content>실사 파일 업로드.</template></Card>
    </div>

<!-- KPI -->
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
    <template #title>미배송 주문</template>
    <template #content>
      <div class="kpi-value">{{ kpi.unshipped }}건</div>
      <div class="kpi-sub"><i class="pi pi-truck"></i> 미배송 수량</div>
    </template>
  </Card>

  <Card class="kpi">
    <template #title>다음 결제기한 금액</template>
    <template #content>
      <div class="kpi-value">{{ asKRW(kpi.nextDueAmount) }}</div>
      <div class="kpi-sub"><i class="pi pi-clock"></i> 기준일: {{ formatDate(kpi.nextDueDate) }}</div>
    </template>
  </Card>
</div>

    <!-- 매출 분석 (탭 3개) -->
    <Card class="chart-card">
      <template #title>
        매출 분석
        <Dropdown
          v-model="selectedRange"
          :options="rangeOptions"
          optionLabel="label"
          optionValue="value"
          class="ml-2"
          size="small"
          @change="fetchAllCharts"
        />
      </template>

      <template #content>
        <TabView>
          <!-- ① 매출 추이 (본사 주문금액 vs 실제 매출) -->
          <TabPanel header="매출 추이">
            <Chart type="line" :data="trendData" :options="trendOptions" />
          </TabPanel>

          <!-- ② 작년 vs 올해 매출 비교 -->
          <TabPanel header="작년 vs 올해">
            <Chart type="bar" :data="compareData" :options="compareOptions" />
          </TabPanel>

<!-- ③ 원두 판매 랭킹 (Progress Bar) -->
<TabPanel header="원두 판매 랭킹">
  <div v-if="beanRank.length" class="bean-rank-wrap">
    <div v-for="bean in beanRank" :key="bean.LABEL ?? bean.name" class="bean-rank">
      <div class="bean-row">
        <div class="bean-info">
          <strong>{{ bean.LABEL ?? bean.name }}</strong>
          <small v-if="bean.category">{{ bean.category }}</small>
        </div>
        <!-- ✅ 안전 반올림 (문자/undefined/기호 모두 처리) -->
        <div class="bean-percent">{{ roundRate(bean.RATE ?? bean.rate) }}%</div>
      </div>
      <ProgressBar
        :value="roundRate(bean.RATE ?? bean.rate)"
        :showValue="false"
        style="height:10px"
      />
    </div>
  </div>
  <Message v-else severity="info" class="mt-2">표시할 랭킹 데이터가 없습니다.</Message>
</TabPanel>



        </TabView>
      </template>
    </Card>

    <!-- 미결제 주문/반품 요약 -->
    <Card class="table-card">
      <template #title>미결제 주문/반품 요약</template>
      <template #content>
        <div class="table-toolbar">
          <span class="p-input-icon-left"><i class="pi pi-search" /><InputText v-model="orderQuery" placeholder="주문번호 또는 제품명 검색" /></span>
        </div>
        <DataTable :value="filteredOrders" paginator :rows="7" dataKey="orderId" :rowHover="true" class="p-datatable-sm">
          <Column field="orderId" header="주문코드" style="width:160px" />
          <Column field="prodName" header="제품명" />
          <Column header="총합계금액" style="width:140px; text-align:right"><template #body="{ data }">{{ asKRW(data.totalPrice) }}</template></Column>
          <Column header="출고일자" style="width:130px"><template #body="{ data }">{{ formatDate(data.sendDate) }}</template></Column>
          <Column header="주문상태" style="width:120px"><template #body="{ data }"><Tag :value="data.status" :severity="statusColor(data.status)" /></template></Column>
          <Column header="결제기한" style="width:130px"><template #body="{ data }">{{ formatDate(data.paydueDate) }}</template></Column>
        </DataTable>
      </template>
    </Card>

    <!-- 재고 / 공지 -->
    <div class="two-col">
      <Card class="table-card">
        <template #title>재고 현황 <span class="muted">(안전재고 대비)</span></template>
        <template #content>
          <DataTable :value="stock" paginator :rows="8" class="p-datatable-sm">
            <Column field="sku" header="SKU" style="width:120px" />
            <Column field="name" header="품목명" />
            <Column field="qty" header="현재고" style="width:90px" />
            <Column field="safety" header="안전재고" style="width:100px" />
            <Column header="발주 필요" style="width:120px">
              <template #body="{ data }"><Tag :value="data.qty < data.safety ? '필요' : '정상'" :severity="data.qty < data.safety ? 'danger' : 'success'" /></template>
            </Column>
            <Column field="price" header="단가" style="width:120px"><template #body="{ data }">{{ asKRW(data.price) }}</template></Column>
          </DataTable>
          <Message v-if="lowItems.length" severity="warn" class="mt-3"><i class="pi pi-exclamation-triangle mr-2"></i>안전재고 미만 품목: {{ lowItems.map(i => i.name).join(', ') }}</Message>
        </template>
      </Card>

      <Card class="notice-card">
        <template #title>공지사항</template>
        <template #content>
          <ul class="notice-list">
            <li v-for="n in notices" :key="n.id">
              <div class="row"><Tag :value="n.category" :severity="noticeColor(n.category)" /><span class="date">{{ formatDate(n.date) }}</span></div>
              <div class="title">{{ n.title }}</div>
              <p class="desc">{{ n.desc }}</p>
            </li>
          </ul>
          <div class="more"><Button label="모든 공지 보기" class="p-button-text" /></div>
        </template>
      </Card>
    </div>

    <!-- 하단 CTA -->
    <div class="footer-cta">
      <Message severity="info" icon="pi pi-info-circle">수기 입력보다 <b>발주 템플릿</b>을 사용하면 편합니다.</Message>
      <Button label="발주 템플릿 다운로드" icon="pi pi-download" />
    </div>
  </div>
</template>


<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import { useUserStore } from '@/stores/user'

import {
  Chart as ChartJS,
  Title, Tooltip, Legend, LineElement, BarElement, PointElement,
  CategoryScale, LinearScale, TimeScale, Filler
} from 'chart.js'
import 'chartjs-adapter-date-fns'

// PrimeVue tab/progress components (로컬 임포트로 전역등록 없이 사용)
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import ProgressBar from 'primevue/progressbar'

ChartJS.register(
  Title, Tooltip, Legend, LineElement, BarElement, PointElement,
  CategoryScale, LinearScale, TimeScale, Filler
)

// Pinia Store
const userStore = useUserStore();
const vendorId = userStore.code;

// TODO: 필요시 실데이터 연결
const selectedStore = ref(null)
const stores = ref([])

// 날짜 포맷
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr
  return date.toLocaleDateString('ko-KR')
}
const today = new Date().toISOString().substring(0, 10)

// 금액 포맷
const asKRW = (value) => {
  if (value === null || value === undefined) return '0원'
  return value.toLocaleString('ko-KR') + '원'
}

// 주문 상태 색상
const statusColor = (status) => {
  switch (status) {
    case '대기': return 'warning'
    case '완료': return 'success'
    case '취소': return 'danger'
    case '배송중': return 'info'
    case '배송완료': return 'success'
    default: return 'secondary'
  }
}

// KPI
const kpi = ref({
  todaySales: 0,
  monthSales: 0,
  unshipped: 0,
  nextDueAmount: 0,
  nextDueDate: '',
  dailyRate: 0,
  monthRate: 0
})

const fetchKpi = async () => {
  try {
    const { data } = await axios.get('/api/branch/sales-growth', { params: { vendorId } })
    kpi.value = {
      todaySales: data.TODAY ?? 0,
      monthSales: data.CURR_MONTH ?? 0,
      unshipped: data.UNSHIPPED ?? 0,
      nextDueAmount: data.NEXT_DUE_AMT ?? 0,
      nextDueDate: data.NEXT_DUE_DATE ?? '',
      dailyRate: data.DAILY_RATE ?? 0,
      monthRate: data.MONTHLY_RATE ?? 0
    }
  } catch (err) {
    console.error('KPI 불러오기 오류:', err)
  }
}


// 주문
const orders = ref([]);
const orderQuery = ref('');
const fetchOrders = async () => {
  try {
    const { data } = await axios.get('/api/pendingorders', { params: { vendorId, limit: 10 } });
    orders.value = data;
  } catch (err) {
    console.error('미결제 주문 조회 오류:', err);
  }
};
const filteredOrders = computed(() =>
  orders.value.filter(o =>
    !orderQuery.value ||
    o.orderId?.includes(orderQuery.value) ||
    (o.prodName && o.prodName.includes(orderQuery.value))
  )
);

// 재고
const stock = ref([]);
const lowItems = computed(() => stock.value.filter(s => s.qty < s.safety));
const fetchStock = async () => {
  try {
    const { data } = await axios.get('/api/dashboard/stock', { params: { vendorId } });
    stock.value = data;
  } catch (err) {
    console.error('재고 조회 오류:', err);
  }
};

// 공지사항
const notices = ref([]);
const fetchNotices = async () => {
  try {
    const { data } = await axios.get('/api/dashboard/notices');
    notices.value = data;
  } catch (err) {
    console.error('공지사항 조회 오류:', err);
  }
};

// ========= 매출 분석(탭) =========

// 공통 드롭다운: 일별/월별
const selectedRange = ref('daily');
const rangeOptions = [
  { label: '일별', value: 'daily' },
  { label: '월별', value: 'monthly' }
];

// ① 매출 추이 (본사 주문금액 vs 실제 매출)
const trendData = ref({ datasets: [] });
const trendOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { position: 'bottom' } },
  scales: {
    x: {
      type: selectedRange.value === 'daily' ? 'time' : 'category',
      time: selectedRange.value === 'daily' ? { unit: 'day' } : undefined
    },
    y: {
      beginAtZero: true,
      ticks: { callback: (value) => (value / 10000).toLocaleString() + '만 원' }
    }
  },
  elements: { line: { tension: 0.35 }, point: { radius: 2 } }
}));

const fetchTrend = async () => {
  try {
    const { data } = await axios.get('/api/branch/salestrend', {
      params: { vendorId, range: selectedRange.value } // 기대 필드: LABEL, ORDER_AMT, SALES_AMT
    });

    const labels = data.map(d => d.LABEL);
    const orderAmt = data.map(d => d.ORDER_AMT ?? 0);
    const salesAmt = data.map(d => d.SALES_AMT ?? 0);

    trendData.value = {
      labels,
      datasets: [
        {
          label: '본사 주문금액',
          data: orderAmt,
          borderColor: '#93c5fd',
          backgroundColor: 'rgba(147,197,253,0.20)',
          fill: true
        },
        {
          label: '실제 매출금액',
          data: salesAmt,
          borderColor: '#4F46E5',
          backgroundColor: 'rgba(79,70,229,0.10)',
          fill: true
        }
      ]
    };
  } catch (err) {
    console.error('매출 추이 조회 오류:', err);
  }
};

// ② 작년 vs 올해 매출 비교
const compareData = ref({ datasets: [] });
const compareOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { position: 'bottom' } },
  scales: {
    x: { 
      type: 'category'   // ✅ 무조건 카테고리로 강제 (월 라벨용)
    },
    y: { 
      beginAtZero: true, 
      ticks: { callback: (v) => (v / 10000).toLocaleString() + '만 원' } 
    }
  }
}));


const fetchCompare = async () => {
  try {
    const { data } = await axios.get('/api/branch/salescompare', {
      params: { vendorId, range: selectedRange.value }
    });

    // ✅ 실제 데이터 위치: data.compareData
    const list = data.compareData;

    // ✅ LABEL이 이미 월(Label "05", "09" 이런 형식)
    const labels = list.map(d => d.LABEL);

    compareData.value = {
      labels,
      datasets: [
        { label: '작년', data: list.map(d => d.LAST_YEAR ?? 0), backgroundColor: '#93c5fd' },
        { label: '올해', data: list.map(d => d.THIS_YEAR ?? 0), backgroundColor: '#4F46E5' }
      ]
    };
  } catch (err) {
    console.error('작년 vs 올해 매출 비교 조회 오류:', err);
  }
};


// 랭킹 데이터
const beanRank = ref([]);

// ✅ RATE 값 안전 파싱 + 반올림 (문자열/공백/%/콤마 모두 처리)
const roundRate = (v) => {
  if (v === null || v === undefined) return 0;
  // 문자열일 수 있으므로 숫자/점/부호만 남기고 변환
  const n = parseFloat(String(v).replace(/[^\d.\-]/g, ''));
  return Number.isFinite(n) ? Math.round(n) : 0;
};

// ✅ API 호출 시 응답을 안전하게 매핑 (필드 불일치 대비)
const fetchCoffeeRank = async () => {
  try {
    const { data } = await axios.get('/api/branch/coffeerank', {
      params: { vendorId, range: selectedRange.value }
    });

    // 배열로 정규화 (직접 배열/객체 래핑 모두 대응)
    const list = Array.isArray(data) ? data : (data?.list ?? data?.items ?? []);
    // 필드 이름 가드 + RATE 숫자화
    beanRank.value = list.map(r => ({
      LABEL: r.LABEL ?? r.name ?? '',
      RATE: roundRate(r.RATE ?? r.rate ?? 0)
    }));
  } catch (e) {
    console.error('원두 랭킹 조회 오류:', e);
    beanRank.value = [];
  }
};


// 공통 fetch
const fetchAllCharts = () => {
  fetchTrend();
  fetchCompare();
  fetchCoffeeRank();
};

// 최초 로드
onMounted(() => {
  fetchKpi();
  fetchOrders();
  fetchStock();
  fetchNotices();
  fetchAllCharts();

});

// range 실시간 반영(드롭다운 변경 시 자동 반영을 원하면 아래 watch 유지)
// watch(selectedRange, fetchAllCharts);
</script>



<style scoped>
.main { min-height: 100vh; background:#f7f8fa; padding:20px 24px; }

/* 헤더 */
.header { display:flex; justify-content:space-between; align-items:center; margin-bottom:16px; }
.title { margin:0; font-size:20px; font-weight:700; }
.subtitle { margin:0; color:#6b7280; }
.header-ctrl { display:flex; align-items:center; }

/* 공지 배너 */
.notice-banners { display:grid; grid-template-columns: 1fr; gap:12px; margin-bottom:14px; }
.banner { border-radius:12px; padding:14px; background:#fff; border:1px solid #f1f5f9; }
.banner.danger { background:#fff5f5; border-color:#fde2e2; }
.banner.warn { background:#fffaf0; border-color:#feecdc; }
.banner.info { background:#f7fbff; border-color:#dbeafe; }
.banner-head { display:flex; align-items:center; gap:8px; margin-bottom:6px; }
.dot { width:8px; height:8px; border-radius:50%; display:inline-block; }
.dot.danger { background:#ef4444; } .dot.warn { background:#f59e0b; } .dot.info { background:#3b82f6; }
.banner-actions { display:flex; gap:6px; margin-top:6px; }

/* 빠른 작업 / KPI */
.card-grid-4, .kpi-row { display:grid; grid-template-columns: repeat(4, 1fr); gap:12px; margin-bottom:16px; }
.quick-card :deep(.p-card-title){ font-weight:700; font-size:14px; display:flex; align-items:center; }
.kpi :deep(.p-card-title){ color:#6b7280; font-weight:600; }
.kpi-value { font-size:22px; font-weight:800; margin-top:2px; }
.kpi-sub { color:#6b7280; font-size:12px; margin-top:4px; display:flex; align-items:center; gap:6px; }

/* 차트 영역 */
.chart-card { margin-bottom:16px; }
.chart-card :deep(.p-card-content){ height:260px; }
.chart-card :deep(canvas){ max-height: 220px !important; } /* 탭 내 차트 높이 안정화 */

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
.two-col { display:grid; grid-template-columns: 1.3fr 1fr; gap:12px; }

/* 공지 리스트 */
.notice-card .notice-list{ list-style:none; padding:0; margin:0; display:flex; flex-direction:column; gap:12px; }
.notice-card .row{ display:flex; align-items:center; gap:8px; }
.notice-card .date{ color:#9ca3af; font-size:12px; margin-left:auto; }
.notice-card .title{ font-weight:700; margin-top:4px; }
.notice-card .desc{ color:#6b7280; margin:.25rem 0 0; font-size:14px; }
.more{ text-align:right; margin-top:8px; }

/* 하단 CTA */
.footer-cta { margin-top:8px; display:flex; align-items:center; justify-content:space-between; gap:12px; }

/* 반응형 */
@media (max-width: 1200px){
  .card-grid-4, .kpi-row { grid-template-columns: repeat(2, 1fr); }
  .two-col { grid-template-columns: 1fr; }
}
@media (max-width: 640px){
  .card-grid-4, .kpi-row { grid-template-columns: 1fr; }
}
.kpi-rate {
  margin-left: 6px;
  font-size: 14px;
  font-weight: 600;
}

</style>
