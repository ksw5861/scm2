<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useAppToast } from '@/composables/useAppToast';
import InputText from 'primevue/inputtext';
import InputGroup from 'primevue/inputgroup';
import InputGroupAddon from 'primevue/inputgroupaddon';
import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Paginator from 'primevue/paginator';
import Chart from 'chart.js/auto';
import Calendar from 'primevue/calendar';

const { toast } = useAppToast();

/* ------------------ 검색 상태 ------------------ */
const search = ref({
  vendorType: '전체',
  tradeType: '전체',
  keyword: '',
  dateFrom: null,
  dateTo: null
});

/* ------------------ 데이터 상태 ------------------ */
const summary = ref({
  totalSales: 0,
  totalReturn: 0,
  totalPayment: 0,
  totalAr: 0,
  totalAp: 0
});
const list = ref([]);
const totalRecords = ref(0);
const page = ref(1);
const rows = ref(10);

/* ------------------ 상세 관련 상태 ------------------ */
const showDetail = ref(false);
const selectedVendor = ref(null);
const detailSummary = ref({
  totalSales: 0,
  totalReturn: 0,
  totalPayment: 0,
  totalAr: 0
});
const detailList = ref([]);
const detailChartRef = ref(null);
let detailChart = null;

/* ------------------ 목록 차트 ------------------ */
const chartRef = ref(null);
let chartInstance = null;

function renderChart(data = []) {
  const ctx = chartRef.value.getContext('2d');
  if (chartInstance) chartInstance.destroy();

  const sliced = data.slice(0, 10);
  chartInstance = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: sliced.map((d) => d.companyName),
      datasets: [
        { label: '총매출', data: sliced.map((d) => d.totalSales), backgroundColor: '#1976d2' },
        { label: '총반품', data: sliced.map((d) => Math.abs(d.totalReturn)), backgroundColor: '#d32f2f' },
        { label: '총입금', data: sliced.map((d) => d.totalPayment), backgroundColor: '#388e3c' }
      ]
    },
    options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom' } } }
  });
}

/* ------------------ 상세 차트 ------------------ */
function renderDetailChart(data) {
  if (!detailChartRef.value) return;
  const ctx = detailChartRef.value.getContext('2d');
  if (detailChart) detailChart.destroy();

  const labels = data.map((r) => r.tradeDate);
  const sales = data.map((r) => r.salesAmount);
  const returns = data.map((r) => r.returnAmount);
  const payments = data.map((r) => r.paymentAmount);

  detailChart = new Chart(ctx, {
    type: 'line',
    data: {
      labels,
      datasets: [
        { label: '매출', data: sales, borderColor: '#1976d2', fill: false },
        { label: '반품', data: returns, borderColor: '#d32f2f', fill: false },
        { label: '입금', data: payments, borderColor: '#388e3c', fill: false }
      ]
    },
    options: { responsive: true, plugins: { legend: { position: 'bottom' } } }
  });
}

/* ------------------ 조회 ------------------ */
async function loadData() {
  try {
    const params = {
      vendorType: search.value.vendorType,
      tradeType: search.value.tradeType,
      keyword: search.value.keyword,
      dateFrom: search.value.dateFrom ? fmtDate(search.value.dateFrom) : '',
      dateTo: search.value.dateTo ? fmtDate(search.value.dateTo) : '',
      page: page.value,
      size: rows.value
    };
    const { data } = await axios.get('/api/account-ledger', { params });

    list.value = data.items || [];
    totalRecords.value = data.total || 0;

    summary.value = {
      totalSales: data.summary?.totalSales || 0,
      totalReturn: data.summary?.totalReturn || 0,
      totalPayment: data.summary?.totalPayment || 0,
      totalAr: data.summary?.totalAr || 0,
      totalAp: data.summary?.totalAp || 0
    };

    renderChart(list.value);
  } catch (err) {
    toast('error', '조회 실패', err.message);
  }
}

/* ------------------ 상세 보기 ------------------ */
async function goDetail(vendor) {
  try {
    selectedVendor.value = vendor;
    showDetail.value = true;

    const { data } = await axios.get('/api/account-ledger/detail', {
      params: { vendorId: vendor.vendorId }
    });

    detailSummary.value = data.summary;
    detailList.value = data.list;
    renderDetailChart(detailList.value);
  } catch (err) {
    toast('error', '상세조회 실패', err.message);
  }
}

/* ------------------ 초기화 ------------------ */
function resetForm() {
  search.value = { vendorType: '전체', tradeType: '전체', keyword: '', dateFrom: null, dateTo: null };
  loadData();
}

/* ------------------ 유틸 ------------------ */
function fmtDate(d) {
  if (!d) return '';
  const dt = typeof d === 'string' ? new Date(d) : d;
  return `${dt.getFullYear()}-${String(dt.getMonth() + 1).padStart(2, '0')}-${String(dt.getDate()).padStart(2, '0')}`;
}

function fmt(val) {
  return val ? '₩' + Number(val).toLocaleString() : '-';
}

/* ------------------ Mounted ------------------ */
onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="page-wrap">
    <h2 class="page-title">거래처원장</h2>

    <!-- 검색 -->
    <div class="box">
      <div class="form-grid">
        <Dropdown v-model="search.vendorType" :options="['전체', '판매처', '공급처']" placeholder="거래처구분" />
        <Dropdown v-model="search.tradeType" :options="['전체', '매출', '매입']" placeholder="거래유형" />
        <InputGroup>
          <InputText v-model="search.keyword" placeholder="거래처명" />
          <InputGroupAddon>
            <Button icon="pi pi-search" text @click="loadData" />
          </InputGroupAddon>
        </InputGroup>
        <div class="flex gap-2">
          <Calendar v-model="search.dateFrom" dateFormat="yy-mm-dd" showIcon class="w-full" />
          <Calendar v-model="search.dateTo" dateFormat="yy-mm-dd" showIcon class="w-full" />
        </div>
      </div>
      <div class="actions">
        <Button label="초기화" icon="pi pi-refresh" outlined @click="resetForm" />
        <Button label="조회" icon="pi pi-search" @click="loadData" />
      </div>
    </div>

    <!-- 요약 카드 -->
    <div class="summary-grid">
      <div class="card">
        <p>총매출</p>
        <h3 class="blue">₩{{ summary.totalSales.toLocaleString() }}</h3>
      </div>
      <div class="card">
        <p>총반품</p>
        <h3 class="red">₩{{ summary.totalReturn.toLocaleString() }}</h3>
      </div>
      <div class="card">
        <p>총입금</p>
        <h3 class="green">₩{{ summary.totalPayment.toLocaleString() }}</h3>
      </div>
      <div class="card">
        <p>총미수금</p>
        <h3 class="blue">₩{{ summary.totalAr.toLocaleString() }}</h3>
      </div>
      <div class="card">
        <p>총미지급금</p>
        <h3 class="red">₩{{ summary.totalAp.toLocaleString() }}</h3>
      </div>
    </div>

    <!-- 그래프 -->
    <div class="chart-box">
      <canvas ref="chartRef"></canvas>
    </div>

    <!-- 거래처별 현황 -->
    <div class="box">
      <div class="box-title">거래처별 현황</div>
      <DataTable :value="list" dataKey="vendorId" @rowClick="goDetail($event.data)" size="small" rowHover class="clickable-table">
        <Column field="companyName" header="거래처명" />
        <Column field="vendorType" header="구분" />
        <Column field="totalSales" header="총금액" :body="(d) => fmt(d.totalSales)" />
        <Column field="totalAr" header="미수금" :body="(d) => fmt(d.totalAr)" />
        <Column field="totalAp" header="미지급금" :body="(d) => fmt(d.totalAp)" />
      </DataTable>

      <Paginator
        :rows="rows"
        :totalRecords="totalRecords"
        @page="
          (e) => {
            page.value = e.page + 1;
            loadData();
          }
        "
      />
    </div>

    <!-- 상세 섹션 -->
    <div v-if="showDetail" class="detail-section">
      <h3 class="detail-title">📊 {{ selectedVendor.companyName }} 거래 상세내역</h3>

      <!-- 요약 -->
      <div class="summary-grid">
        <div class="card blue">
          <p>총매출</p>
          <h3>₩{{ detailSummary.totalSales.toLocaleString() }}</h3>
        </div>
        <div class="card red">
          <p>총반품</p>
          <h3>₩{{ detailSummary.totalReturn.toLocaleString() }}</h3>
        </div>
        <div class="card green">
          <p>총입금</p>
          <h3>₩{{ detailSummary.totalPayment.toLocaleString() }}</h3>
        </div>
        <div class="card red">
          <p>미수금</p>
          <h3>₩{{ detailSummary.totalAr.toLocaleString() }}</h3>
        </div>
      </div>

      <!-- 그래프 -->
      <div class="chart-box">
        <canvas ref="detailChartRef"></canvas>
      </div>

      <!-- 테이블 -->
      <div class="box">
        <DataTable :value="detailList" stripedRows size="small">
          <Column field="tradeDate" header="거래일자" />
          <Column field="tradeType" header="유형" />
          <Column field="item" header="내역" />
          <Column field="salesAmount" header="매출" :body="(r) => fmt(r.salesAmount)" />
          <Column field="returnAmount" header="반품" :body="(r) => fmt(r.returnAmount)" />
          <Column field="paymentAmount" header="입금" :body="(r) => fmt(r.paymentAmount)" />
        </DataTable>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-wrap {
  padding: 16px;
  background: #f5f7fb;
}
.page-title {
  font-weight: 700;
  font-size: 22px;
  margin-bottom: 20px;
}
.box {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 18px;
}
.form-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  align-items: end;
}
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 10px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
  margin-bottom: 20px;
}
.card {
  background: #fff;
  text-align: center;
  border-radius: 10px;
  padding: 14px 0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}
.card p {
  color: #777;
  font-size: 13px;
  margin-bottom: 4px;
}
.card h3 {
  font-size: 20px;
  font-weight: 700;
  margin: 0;
}
.blue {
  color: #1976d2;
}
.red {
  color: #d32f2f;
}
.green {
  color: #388e3c;
}

.chart-box {
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  padding: 20px;
  height: 280px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.box-title {
  font-weight: 700;
  font-size: 15px;
  margin-bottom: 10px;
}
.p-paginator {
  margin-top: 8px;
  justify-content: center;
}
.clickable-table .p-datatable-tbody > tr {
  cursor: pointer;
  transition: background-color 0.15s ease;
}
.clickable-table .p-datatable-tbody > tr:hover {
  background-color: #f1f5ff !important;
}

.detail-section {
  margin-top: 30px;
  animation: fadeIn 0.3s ease;
}
.detail-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 12px;
}
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
