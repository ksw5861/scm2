<script setup>
import { ref, onMounted, watch } from 'vue';
import axios from 'axios';
import { useAppToast } from '@/composables/useAppToast';
import InputText from 'primevue/inputtext';
import InputGroup from 'primevue/inputgroup';
import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Paginator from 'primevue/paginator';
import Chart from 'chart.js/auto';

const { toast } = useAppToast();

/* ------------------ 검색 상태 ------------------ */
const search = ref({
  tradeType: '전체',
  keyword: ''
});

/* ------------------ 데이터 상태 ------------------ */
const summary = ref({
  totalSales: 0,
  totalReturn: 0,
  totalPayment: 0,
  totalAr: 0
});
const list = ref([]);
const totalRecords = ref(0);
const page = ref(1);
const rows = ref(10);

/* ------------------ 금액 포맷 ------------------ */
function fmt(value) {
  const num = Number(value);
  if (isNaN(num) || value === null || value === undefined) return '₩0';
  return '₩' + num.toLocaleString('ko-KR');
}

/* ------------------ 차트 참조 ------------------ */
const chartRefDonut = ref(null);
const chartRefBar = ref(null);
let donutChart = null;
let barChart = null;

/* ------------------ 데이터 로드 ------------------ */
async function loadData() {
  try {
    const params = {
      tradeType: search.value.tradeType,
      keyword: search.value.keyword,
      page: page.value,
      size: rows.value
    };
    const { data } = await axios.get('/api/account-ledger', { params });

    summary.value = data.summary || {
      totalSales: 0,
      totalReturn: 0,
      totalPayment: 0,
      totalAr: 0
    };
    list.value = data.items || [];
    totalRecords.value = data.total || 0;

    renderDonutChart();
    renderBarChart();
  } catch (err) {
    toast('error', '조회 실패', err.message);
  }
}

/* ------------------ 도넛 그래프 ------------------ */
function renderDonutChart() {
  if (!chartRefDonut.value) return;
  const ctx = chartRefDonut.value.getContext('2d');
  if (donutChart) donutChart.destroy();

  const labels = ['총매출', '총반품', '총입금', '총미수금'];
  const values = [
    summary.value.totalSales,
    summary.value.totalReturn,
    summary.value.totalPayment,
    summary.value.totalAr
  ];
  const colors = ['#1976d2', '#d32f2f', '#388e3c', '#ff9800'];

  donutChart = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels,
      datasets: [
        { data: values, backgroundColor: colors, borderWidth: 0, hoverOffset: 10 }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '58%',
      plugins: {
        legend: { position: 'bottom' },
        title: {
          display: true,
          text: '본사 전체 거래 항목 비중',
          font: { size: 14, weight: 'bold' }
        },
        tooltip: { callbacks: { label: (ctx) => `${ctx.label}: ${fmt(ctx.parsed)}` } }
      }
    }
  });
}

/* ------------------ 거래처별 Top5 Bar ------------------ */
function renderBarChart() {
  if (!chartRefBar.value) return;
  const ctx = chartRefBar.value.getContext('2d');
  if (barChart) barChart.destroy();

  const top5 = [...list.value]
    .sort((a, b) => b.totalSales - a.totalSales)
    .slice(0, 5);

  const labels = top5.map((v) => v.companyName);
  const sales = top5.map((v) => v.totalSales);
  const arrears = top5.map((v) => v.totalAr);

  barChart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels,
      datasets: [
        { label: '총매출', data: sales, backgroundColor: '#1976d2' },
        { label: '미수금', data: arrears, backgroundColor: '#ff9800' }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { position: 'bottom' },
        title: {
          display: true,
          text: '거래처별 Top5 매출 / 미수금 비교',
          font: { size: 14, weight: 'bold' }
        },
        tooltip: { callbacks: { label: (ctx) => `${ctx.dataset.label}: ${fmt(ctx.parsed.y)}` } }
      },
      scales: {
        y: {
          beginAtZero: true,
          ticks: { callback: (v) => '₩' + v.toLocaleString() }
        }
      }
    }
  });
}

/* ------------------ 상세 데이터 ------------------ */
const selectedVendor = ref({
  companyName: '-',
  totalSales: 0,
  totalReturn: 0,
  totalPayment: 0,
  totalAr: 0,
  lastTradeDate: null // ✅ 최근거래일자 추가
});
const detailChartRef = ref(null);
let detailChart = null;

function handleRowSelect(event) {
  selectedVendor.value = event.data;
  renderDetailChart();
}

/* ------------------ 상세 그래프 ------------------ */
function renderDetailChart() {
  if (!detailChartRef.value || !selectedVendor.value) return;
  const ctx = detailChartRef.value.getContext('2d');
  if (detailChart) detailChart.destroy();

  const labels = ['총매출', '총반품', '총입금', '미수금'];
  const values = [
    selectedVendor.value.totalSales,
    selectedVendor.value.totalReturn,
    selectedVendor.value.totalPayment,
    selectedVendor.value.totalAr
  ];
  const colors = ['#1976d2', '#d32f2f', '#388e3c', '#ff9800'];

  detailChart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels,
      datasets: [{ data: values, backgroundColor: colors }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        title: {
          display: true,
          text: `${selectedVendor.value.companyName} 거래내역 요약`
        },
        tooltip: { callbacks: { label: (ctx) => fmt(ctx.parsed.y) } }
      },
      scales: {
        y: { beginAtZero: true, ticks: { callback: (v) => '₩' + v.toLocaleString() } }
      }
    }
  });
}

/* ------------------ 초기화 ------------------ */
function resetForm() {
  search.value = { tradeType: '전체', keyword: '' };
  loadData();
}

onMounted(() => loadData());
</script>

<template>
  <div class="page-wrap">
    <h2 class="page-title">거래처원장 (판매처)</h2>

    <!-- 검색폼 -->
    <div class="box">
      <div class="form-grid-4">
        <div class="field">
          <label>거래유형</label>
          <Dropdown v-model="search.tradeType" :options="['전체', '매출', '매입']" class="w-full" />
        </div>
        <div class="field">
          <label>거래처명</label>
          <InputGroup>
            <InputText v-model="search.keyword" placeholder="거래처명" />
          </InputGroup>
        </div>
      </div>
      <div class="actions">
        <Button label="초기화" icon="pi pi-refresh" @click="resetForm" />
        <Button label="조회" icon="pi pi-search" @click="loadData" />
      </div>
    </div>

    <!-- 요약 카드 -->
    <div class="summary-grid">
      <div class="card"><p>총매출</p><h3 class="blue">{{ fmt(summary.totalSales) }}</h3></div>
      <div class="card"><p>총반품</p><h3 class="red">{{ fmt(summary.totalReturn) }}</h3></div>
      <div class="card"><p>총입금</p><h3 class="green">{{ fmt(summary.totalPayment) }}</h3></div>
      <div class="card"><p>총미수금</p><h3 class="orange">{{ fmt(summary.totalAr) }}</h3></div>
    </div>

    <!-- 그래프 -->
    <div class="chart-grid">
      <div class="chart-box"><canvas ref="chartRefDonut"></canvas></div>
      <div class="chart-box"><canvas ref="chartRefBar"></canvas></div>
    </div>

    <!-- ✅ 하단 -->
    <div class="box table-wrap">
      <div class="w-2/5">
        <DataTable
          :value="list"
          dataKey="vendorId"
          size="small"
          rowHover
          selectionMode="single"
          v-model:selection="selectedVendor"
          @rowSelect="handleRowSelect"
        >
          <Column field="companyName" header="거래처명" />
          <Column field="totalSales" header="총금액">
            <template #body="{ data }">{{ fmt(data.totalSales) }}</template>
          </Column>
          <Column field="totalAr" header="미수금">
            <template #body="{ data }">{{ fmt(data.totalAr) }}</template>
          </Column>
        </DataTable>
        <Paginator
          :rows="rows"
          :totalRecords="totalRecords"
          @page="(e) => { page.value = e.page + 1; loadData(); }"
        />
      </div>

      <!-- 우측 상세 -->
      <div class="w-3/5">
        <div class="detail-card">
          <h3 class="text-lg font-bold mb-3">{{ selectedVendor.companyName }}</h3>
          <div class="chart-small mb-4"><canvas ref="detailChartRef"></canvas></div>
          <div class="grid grid-cols-3 gap-4">
            <div class="p-3 bg-gray-50 rounded-md">
              <p class="text-gray-500 text-sm">총매출</p>
              <p class="font-semibold text-blue-600">{{ fmt(selectedVendor.totalSales) }}</p>
            </div>
            <div class="p-3 bg-gray-50 rounded-md">
              <p class="text-gray-500 text-sm">총반품</p>
              <p class="font-semibold text-red-500">{{ fmt(selectedVendor.totalReturn) }}</p>
            </div>
            <div class="p-3 bg-gray-50 rounded-md">
              <p class="text-gray-500 text-sm">총입금</p>
              <p class="font-semibold text-green-600">{{ fmt(selectedVendor.totalPayment) }}</p>
            </div>
            <div class="p-3 bg-gray-50 rounded-md">
              <p class="text-gray-500 text-sm">미수금</p>
              <p class="font-semibold text-orange-500">{{ fmt(selectedVendor.totalAr) }}</p>
            </div>
            <!-- ✅ 단가/수량 제거, 최근거래일자 추가 -->
            <div class="p-3 bg-gray-50 rounded-md">
              <p class="text-gray-500 text-sm">최근거래일자</p>
              <p class="font-semibold text-indigo-500">
                {{ selectedVendor.lastTradeDate ? selectedVendor.lastTradeDate : '-' }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-wrap { padding: 16px; background: #f5f7fb; }
.page-title { font-weight: 700; font-size: 22px; margin-bottom: 20px; }
.box { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; padding: 16px; margin-bottom: 24px; }
.form-grid-4 { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.field { display: flex; flex-direction: column; gap: 6px; }
.actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 10px; }

.summary-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
.card { background: #fff; text-align: center; border-radius: 10px; padding: 14px 0; box-shadow: 0 1px 3px rgba(0,0,0,0.08); display: flex; flex-direction: column; justify-content: center; align-items: center; height: 110px; }
.card p { color: #777; font-size: 13px; margin-bottom: 4px; }
.card h3 { font-size: 20px; font-weight: 700; margin: 0; }
.blue { color: #1976d2; } .red { color: #d32f2f; } .green { color: #388e3c; } .orange { color: #ff9800; }

.chart-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 24px; align-items: stretch; }
.chart-box { background: #fff; border-radius: 10px; border: 1px solid #e5e7eb; padding: 10px; height: 330px; display: flex; align-items: center; justify-content: center; overflow: hidden; box-shadow: 0 2px 6px rgba(0,0,0,0.08); }
.chart-box canvas { max-width: 95%; max-height: 95%; }

.box.table-wrap {
  display: flex;
  align-items: stretch;
  gap: 1rem;
  height: 420px;
}
.table-wrap .w-2\/5 {
  width: 40%;
  display: flex;
  flex-direction: column;
  height: 100%;
}
.table-wrap .w-3\/5 {
  width: 60%;
  display: flex;
  flex-direction: column;
  height: 100%;
}
.detail-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
}
.chart-small { flex: 1; max-height: 200px; overflow: hidden; }
.chart-small canvas { width: 100%; height: 100%; }
</style>
