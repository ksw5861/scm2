<script setup>
import { ref, onMounted, watch, computed } from 'vue';
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
import { useIcon } from '@/composables/useIcon';
import { useRoute } from 'vue-router';

const route = useRoute();
const { toast } = useAppToast();

const icons = {
  home: useIcon('home'),
  vendor: useIcon('vendor'),
  list: useIcon('list')
};


/* ───────────────────────────────
 *  Breadcrumb 구성
 * ─────────────────────────────── */
const breadcrumbHome = { icon: icons.home, to: '/' };
const breadcrumbItems = computed(() => {
  const matched = route.matched.filter(r => r.meta);
  if (!matched.length) return [];
  const current = matched[matched.length - 1];
  return [
    { label: current.meta.breadcrumb?.parent || '' },
    { label: current.name || '', to: route.fullPath }
  ];
});

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

    console.log(data);

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
    summary.value.totalPrice,
    summary.value.returnPrice,
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
  totalPrice: 0,
  returnPrice: 0,
  totalPayment: 0,
  totalAr: 0,
  lastOrderDate: null // ✅ 최근거래일자 추가
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
    selectedVendor.value.totalPrice,
    selectedVendor.value.returnPrice,
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
    <Fluid>
        <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />

        <div class="mt-4 flex flex-row gap-4">
            <div class="card" style="width: 25%; height: 156px; margin-bottom: 0;">
                <p>총 매출</p>
                <h4>{{ fmt(summary.totalPrice) }}</h4>
            </div>
            <div class="card" style="width: 25%; height: 156px; margin-bottom: 0;">
                <p>총 반품</p>
                <h4>{{ fmt(summary.returnPrice) }}</h4>
            </div>
            <div class="card" style="width: 25%; height: 156px; margin-bottom: 0;">
                <p>총 입금</p>
                <h4>{{ fmt(summary.totalPayment) }}</h4>
            </div>
            <div class="card" style="width: 25%; height: 156px; margin-bottom: 0;">
                <p>총 미수금</p>
                <h4>{{ fmt(summary.totalAr) }}</h4>
            </div>
        </div>

        <!-- 그래프 -->
        <div class="mt-4 flex flex-row gap-4 overflow-hidden">
            <div class="card chart-box w-1/3 min-w-0" style="margin-bottom: 0;">
                <canvas ref="chartRefDonut" class="w-full h-auto"></canvas>
            </div>
            <div class="card chart-box w-2/3 min-w-0">
                <canvas ref="chartRefBar" class="w-full h-auto"></canvas>
            </div>
        </div>


        <!-- 검색 -->
        <SearchCard title="거래처 검색" @search="loadData" @reset="resetForm">
            <div class="flex flex-wrap w-full">

                <div class="flex flex-col gap-2 p-2 w-full">
                <InputGroup>
                    <InputGroupAddon><i :class="icons.vendor" /></InputGroupAddon>
                    <IftaLabel>
                    <InputText v-model="search.keyword" inputId="searchName" />
                    <label for="searchName">거래처명</label>
                    </IftaLabel>
                </InputGroup>
                </div>
            </div>
        </SearchCard>

        <div class="flex flex-col md:flex-row w-full gap-4 mt-4">
            <div class="w-full xl:w-5/12 lg:w-1/2">
                <div class="card flex flex-col">
                    <div class="font-semibold text-lg sm:text-xl flex items-center justify-between gap-4 h-10">
                        <div class="flex items-center gap-4">
                            <span :class="icons.list"></span>
                            거래처 목록
                        </div>
                        <div class="text-sm text-gray-400">
                            총 <span class="font-semibold text-sm text-gray-700">
                                <span v-if="page.totalElements > 0">
                                    <CountUp :end-val="page.totalElements" />
                                </span>
                                <span v-else>0</span>
                            </span>건
                        </div>
                    </div>
                    <Divider />
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
                            <template #body="{ data }">{{ fmt(data.totalPrice) }}</template>
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
            </div>
        </div>


      <!-- 우측 상세 -->
      <div class="w-3/5">
        <div class="detail-card">
          <h3 class="text-lg font-bold mb-3">{{ selectedVendor.companyName }}</h3>
          <div class="chart-small mb-4"><canvas ref="detailChartRef"></canvas></div>
          <div class="grid grid-cols-3 gap-4">
            <div class="p-3 bg-gray-50 rounded-md">
              <p class="text-gray-500 text-sm">총매출</p>
              <p class="font-semibold text-blue-600">{{ fmt(selectedVendor.totalPrice) }}</p>
            </div>
            <div class="p-3 bg-gray-50 rounded-md">
              <p class="text-gray-500 text-sm">총반품</p>
              <p class="font-semibold text-red-500">{{ fmt(selectedVendor.returnPrice) }}</p>
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
                {{ selectedVendor.lastOrderDate }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </Fluid>
</template>
