<!-- ======================================================
📄 Dashboard.vue
- 거래처원장 요약 및 그래프 페이지
====================================================== -->
<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import Chart from 'chart.js/auto';
import { useAppToast } from '@/composables/useAppToast';

const { toast } = useAppToast();

/* 상태 */
const summary = ref({ totalPrice: 0, returnPrice: 0, totalPayment: 0, totalAr: 0 });
const list = ref([]);

/* 차트 참조 */
const chartRefDonut = ref(null);
const chartRefBar = ref(null);
let donutChart, barChart;

function fmt(v) {
  const n = Number(v);
  return isNaN(n) ? '₩0' : '₩' + n.toLocaleString('ko-KR');
}

/* 데이터 로드 */
async function loadDashboard() {
  try {
    const { data } = await axios.get('/api/dashboard');
    summary.value = data.summary;
    list.value = data.items;
    renderDonutChart();
    renderBarChart();
  } catch (e) {
    toast('error', '대시보드 조회 실패', e.message);
  }
}

/* 도넛 차트 */
function renderDonutChart() {
  const ctx = chartRefDonut.value.getContext('2d');
  if (donutChart) donutChart.destroy();
  donutChart = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: ['총매출', '총반품', '총입금', '총미수금'],
      datasets: [{ data: [summary.value.totalPrice, summary.value.returnPrice, summary.value.totalPayment, summary.value.totalAr], backgroundColor: ['#1976d2', '#d32f2f', '#388e3c', '#ff9800'] }]
    },
    options: { plugins: { legend: { position: 'bottom' } } }
  });
}

/* 막대 차트 */
function renderBarChart() {
  const ctx = chartRefBar.value.getContext('2d');
  if (barChart) barChart.destroy();
  const top5 = [...list.value].sort((a, b) => b.totalPrice - a.totalPrice).slice(0, 5);
  barChart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: top5.map(v => v.companyName),
      datasets: [
        { label: '총매출', data: top5.map(v => v.totalPrice), backgroundColor: '#1976d2' },
        { label: '미수금', data: top5.map(v => v.totalAr), backgroundColor: '#ff9800' }
      ]
    },
    options: { plugins: { legend: { position: 'bottom' } } }
  });
}

onMounted(() => loadDashboard());
</script>

<template>
  <Fluid>
    <div class="flex gap-4">
      <div class="card" v-for="(v, i) in [
        {label:'총매출', val:summary.totalPrice},
        {label:'총반품', val:summary.returnPrice},
        {label:'총입금', val:summary.totalPayment},
        {label:'총미수금', val:summary.totalAr}
      ]" :key="i" style="width:25%;">
        <p>{{ v.label }}</p><h4>{{ fmt(v.val) }}</h4>
      </div>
    </div>

    <div class="flex gap-4 mt-4">
      <div class="card w-1/3"><canvas ref="chartRefDonut"></canvas></div>
      <div class="card w-2/3"><canvas ref="chartRefBar"></canvas></div>
    </div>
  </Fluid>
</template>
