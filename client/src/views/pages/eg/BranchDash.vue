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
      <Card class="kpi"><template #title>오늘 매출</template><template #content><div class="kpi-value">{{ asKRW(kpi.todaySales) }}</div><div class="kpi-sub"><i class="pi pi-arrow-up-right"></i> 전일 대비 +2.8%</div></template></Card>
      <Card class="kpi"><template #title>이번 달 매출</template><template #content><div class="kpi-value">{{ asKRW(kpi.monthSales) }}</div><div class="kpi-sub"><i class="pi pi-arrow-up-right"></i> +4.2%</div></template></Card>
      <Card class="kpi"><template #title>미배송 주문</template><template #content><div class="kpi-value">{{ kpi.unshipped }}건</div><div class="kpi-sub"><i class="pi pi-arrow-down-right"></i> -2건</div></template></Card>
      <Card class="kpi"><template #title>다음 결제기한 금액</template><template #content><div class="kpi-value">{{ asKRW(kpi.nextDueAmount) }}</div><div class="kpi-sub"><i class="pi pi-clock"></i> 기준일: {{ formatDate(kpi.nextDueDate) }}</div></template></Card>
    </div>

    <!-- 매출 차트 -->
    <Card class="chart-card">
      <template #title>
        매출 추이
        <Dropdown v-model="selectedRange" :options="rangeOptions" optionLabel="label" 
                  optionValue="value" class="ml-2" size="small" @change="fetchSalesTrend" />
      </template>
      <template #content>
        <Chart type="line" :data="chartData" :options="chartOptions" />
      </template>
    </Card>

    <!-- 미결제 주문/반품 요약 -->
    <Card class="table-card">
      <template #title>미결제 주문/반품 요약</template>
      <template #content>
        <div class="table-toolbar">
          <span class="p-input-icon-left"><i class="pi pi-search" /><InputText v-model="orderQuery" placeholder="주문번호(orderId) 또는 제품명(prodName) 검색" /></span>
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
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { useUserStore } from '@/stores/user'

import {
  Chart as ChartJS,
  Title, Tooltip, Legend, LineElement, PointElement, CategoryScale, LinearScale
} from 'chart.js'

import 'chartjs-adapter-date-fns';

ChartJS.register(Title, Tooltip, Legend, LineElement, PointElement, CategoryScale, LinearScale)

// Pinia Store
const userStore = useUserStore();
const vendorId = userStore.code;

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
const kpi = ref({ todaySales: 0, monthSales: 0, unshipped: 0, nextDueAmount: 0, nextDueDate: '' });
const fetchKpi = async () => {
  try {
    const { data } = await axios.get('/api/branchdashboard', { params: { vendorId } });
    kpi.value = data;
  } catch (err) {
    console.error('KPI 불러오기 오류:', err);
  }
};

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
    o.orderId.includes(orderQuery.value) ||
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

// ========= 매출 차트 =========
const selectedRange = ref('daily'); 
const rangeOptions = [
  { label: '일별', value: 'daily' },
  { label: '주별', value: 'weekly' },
  { label: '월별', value: 'monthly' },
  { label: '연별', value: 'yearly' }
];

const chartData = ref({ datasets: [] });

const fetchSalesTrend = async () => {
  try {
    const { data } = await axios.get('/api/salestrend', {
      params: { vendorId, range: selectedRange.value }
    });

    chartData.value = {
      datasets: [{
        label: '매출',
        data: data.map(d => ({
          x: d.LABEL,  // API에서 YYYY-MM-DD 내려줌
          y: d.AMOUNT
        })),
        borderColor: '#4F46E5',
        backgroundColor: 'rgba(79,70,229,0.08)',
        fill: true,
        tension: 0.35,
        pointRadius: 2
      }]
    };

  } catch (err) {
    console.error('매출 추이 조회 오류:', err);
  }
};

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  scales: {
    x: {
      type: 'time',
      time: { unit: 'day' },
      min: new Date(new Date().setMonth(new Date().getMonth() - 1)), // 1개월 전
      max: new Date() // 오늘까지
    },
    y: {
      beginAtZero: true,
      ticks: {
        callback: (value) => (value / 10000).toLocaleString() + '만 원'
      }
    }
  }
}

onMounted(() => {
  fetchKpi();
  fetchOrders();
  fetchStock();
  fetchNotices();
  fetchSalesTrend();
});
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

/* 차트 */
.chart-card { margin-bottom:16px; }
.chart-card :deep(.p-card-content){ height:260px; }

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
</style>
