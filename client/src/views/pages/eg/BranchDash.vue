<template>
  <div class="main">
    <!-- 헤더 -->
    <header class="header">
      <div>
        <h2 class="title">대시보드</h2>
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
      <template #title>최근 30일 매출 추이</template>
      <template #content><Chart type="line" :data="chartData" :options="chartOptions" /></template>
    </Card>

    <!-- 미결제 주문/반품 요약 (카멜케이스) -->
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

    <!-- 재고 / 공지 2열 -->
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
import { ref, computed } from 'vue'
import Badge from 'primevue/badge'
import Button from 'primevue/button'
import Dropdown from 'primevue/dropdown'
import Card from 'primevue/card'
import InputText from 'primevue/inputtext'
import Tag from 'primevue/tag'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Chart from 'primevue/chart'
import Message from 'primevue/message'

const today = new Date().toLocaleDateString('ko-KR')

/* 매장 */
const stores = ref([{ label: '강남점' }, { label: '홍대점' }, { label: '부산서면점' }])
const selectedStore = ref(stores.value[0])

/* KPI */
const kpi = ref({
  todaySales: 1280000,
  monthSales: 28450000,
  unshipped: 12,
  nextDueAmount: 650000,
  nextDueDate: '2025-09-30'
})

/* 주문/반품 요약 (카멜케이스) */
const orders = ref([
  { orderId: 'O2025-09-0012', prodName: '에스프레소 원두 1kg', totalPrice: 150000, sendDate: '2025-09-23', status: '대기', paydueDate: '2025-09-30' },
  { orderId: 'O2025-09-0013', prodName: '우유 1L (20팩)', totalPrice: 94000, sendDate: '2025-09-22', status: '연체', paydueDate: '2025-09-22' },
  { orderId: 'O2025-09-0014', prodName: '시럽 6종 (바닐라, 카라멜)', totalPrice: 120000, sendDate: '2025-09-21', status: '승인', paydueDate: '2025-09-30' },
  // 반품 행(표시용): return → orderId 매핑, totalPrice 음수, status 적절히 표현
  { orderId: 'R2025-09-0003', prodName: '머그컵 흠집 반품', totalPrice: -22000, sendDate: null, status: '반품', paydueDate: null },
  { orderId: 'O2025-09-0015', prodName: '디카페인 원두 1kg', totalPrice: 110000, sendDate: '2025-09-20', status: '완료', paydueDate: '2025-09-27' }
])
const orderQuery = ref('')
const filteredOrders = computed(() =>
  orders.value.filter(o =>
    !orderQuery.value ||
    o.orderId.includes(orderQuery.value) ||
    (o.prodName && o.prodName.includes(orderQuery.value))
  )
)

/* 재고 */
const stock = ref([
  { sku: 'ESP-001', name: '에스프레소 원두 1kg', qty: 45, safety: 30, price: 18000 },
  { sku: 'MLK-20P', name: '우유 1L (20팩)', qty: 20, safety: 40, price: 24000 },
  { sku: 'SYR-006', name: '시럽 6종', qty: 8, safety: 12, price: 32000 },
  { sku: 'CUP-12', name: '12oz 종이컵', qty: 500, safety: 300, price: 75 },
  { sku: 'LID-12', name: '12oz 컵뚜껑', qty: 150, safety: 200, price: 55 },
])
const lowItems = computed(() => stock.value.filter(s => s.qty < s.safety))

/* 공지 */
const notices = ref([
  { id: 1, title: '원두 단가 조정 안내', category: '가격', date: '2025-09-25', desc: '국제 원두 시세 상승으로 일부 품목 단가가 조정됩니다.' },
  { id: 2, title: '시스템 점검 공지', category: '시스템', date: '2025-09-20', desc: '점검 시간: 02:00~03:00, 주문/납부 일시 제한.' },
  { id: 3, title: '반품 정책 업데이트', category: '정책', date: '2025-09-18', desc: '수령 후 7일 이내 반품 신청 가능.' }
])

/* 차트 */
const days = Array.from({ length: 30 }, (_, i) => {
  const d = new Date(); d.setDate(d.getDate() - (29 - i))
  return `${(d.getMonth()+1).toString().padStart(2,'0')}/${d.getDate().toString().padStart(2,'0')}`
})
const chartData = ref({
  labels: days,
  datasets: [{
    label: '매출',
    data: days.map(() => Math.floor(700000 + Math.random() * 400000)),
    fill: true,
    borderColor: '#4F46E5',
    backgroundColor: 'rgba(79,70,229,0.08)',
    tension: 0.35,
    pointRadius: 0
  }]
})
const chartOptions = ref({
  maintainAspectRatio: false,
  plugins: { legend: { display: false }, tooltip: { callbacks: { label: ctx => ` ${asKRW(ctx.parsed.y)}` } } },
  scales: { x: { grid: { display: false } }, y: { grid: { color: 'rgba(0,0,0,0.06)' }, ticks: { callback: v => asKRW(v) } } }
})

/* utils */
const asKRW = v => (v ?? 0).toLocaleString('ko-KR', { style: 'currency', currency: 'KRW', maximumFractionDigits: 0 })
const formatDate = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}
const statusColor = (s) => {
  switch (s) {
    case '완료': return 'success'
    case '대기': return 'warning'
    case '승인': return 'info'
    case '연체': return 'danger'
    case '실패': return 'danger'
    case '반품': return 'secondary'
    default: return 'secondary'
  }
}
const noticeColor = (c) => (c === '가격' ? 'danger' : c === '정책' ? 'info' : 'warning')
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
