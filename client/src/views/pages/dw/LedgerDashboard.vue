<!-- ======================================================
📄 Dashboard.vue
- 거래처원장 요약 및 그래프 페이지
====================================================== -->
<script setup>
import { ref, onMounted, computed, nextTick } from 'vue';
import axios from 'axios';
import Chart from 'chart.js/auto';
import { useAppToast } from '@/composables/useAppToast';
import { useRoute } from 'vue-router';
import { useIcon } from '@/composables/useIcon';

const { toast } = useAppToast();
const route = useRoute();

const loading = ref(true);

const icons = {
  home: useIcon('home'),
  cart: useIcon('cart'),
  hourglass: useIcon('hourglass'),
  forward: useIcon('forward'),
  gauge: useIcon('gauge'),
  bill: useIcon('bill')
};

/* breadcrumb */
const breadcrumbHome = { icon: icons.home, to: '/' };
const breadcrumbItems = computed(() => {
  const matched = route.matched.filter((r) => r.meta);
  if (!matched.length) return [];
  return [{ label: '대시보드' }];
});

/* 상태 */
const summary = ref({
  totalPrice: 0,
  returnPrice: 0,
  totalPayment: 0,
  totalAr: 0
});
const list = ref([]);

/* 차트 참조 */
const chartRefDonut = ref(null);
const chartRefBar = ref(null);
let donutChart, barChart;

function fmt(v) {
  const n = Number(v);
  return isNaN(n) ? '0' : n.toLocaleString('ko-KR');
}

/* 데이터 로드 */
async function loadDashboard() {
  try {
    const { data } = await axios.get('/api/account-dash');
    summary.value = data.summary;
    list.value = data.items;

    loading.value = false;
    await nextTick();

    renderDonutChart();
    renderBarChart();
  } catch (e) {
    toast('error', '대시보드 조회 실패', e.message);
    loading.value = false;
  }
}

/* 도넛 차트 */
function renderDonutChart() {
  const ctx = chartRefDonut.value.getContext('2d');
  if (donutChart) donutChart.destroy();
  donutChart = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: ['총 입금', '총 미수금'],
      datasets: [
        {
          data: [summary.value.totalPayment, summary.value.totalAr],
          backgroundColor: ['#4BAF7D', '#D84C4C']
        }
      ]
    },
    options: {
      plugins: { legend: { position: 'bottom' } }
    }
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
      labels: top5.map((v) => v.companyName),
      datasets: [
        {
          label: '매출',
          data: top5.map((v) => v.totalPrice),
          backgroundColor: '#1976d2'
        },
        {
          label: '미수금',
          data: top5.map((v) => v.totalAr),
          backgroundColor: '#ff9800'
        }
      ]
    },
    options: {
      plugins: { legend: { position: 'bottom' } }
    }
  });
}

onMounted(() => loadDashboard());
</script>

<template>
  <Fluid>
    <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />

        <div class="grid grid-cols-2 2xl:grid-cols-4 gap-4 mt-4">

            <template v-if="loading">

                <div v-for="n in 4" :key="n" class="card h-30" height="7rem" style="margin-bottom: 0;">
                    
                    <div class="flex justify-between mb-4">
                        <Skeleton width="40%" height="1.5rem" />
                        <Skeleton width="2.5rem" height="2.5rem" class="rounded-full" />
                    </div>

                    <Skeleton height="2rem" />
                </div>

            </template>

            <template v-else>

                <div
                  v-for="(v, i) in [
                    { label: '총 매출', val: summary.totalPrice, color: 'blue', icon: icons.cart },
                    { label: '총 반품', val: summary.returnPrice, color: 'red', icon: icons.hourglass },
                    { label: '총 입금', val: summary.totalPayment, color: 'green', icon: icons.forward },
                    { label: '총 미수금', val: summary.totalAr, color: 'orange', icon: icons.gauge }
                  ]"
                  :key="i" class="card h-30" style="margin-bottom: 0;"
                >
                  <div class="flex justify-between mb-4">

                        <div>
                            <span class="block text-muted-color font-medium mb-4">{{ v.label }}</span>
                            <div
                                class="dark:text-surface-0 font-bold text-xl"
                                :class="`text-${v.color}-500`"
                            >

                                {{ fmt(v.val) }}<span class="font-medium text-gray-700">원</span>
                            
                            </div>
                        </div>

                        <div
                            class="flex items-center justify-center rounded-border"
                            :class="'bg-' + v.color + '-100 dark:bg-' + v.color + '-100/10'"
                            style="width:2.5rem;height:2.5rem;"
                        >
                            <i class="!text-xl" :class="[v.icon, 'text-' + v.color + '-500']"></i>
                        </div>

                    </div>
                </div>

            </template>

        </div>

        <div class="flex w-full gap-4 mt-4 items-stretch">

            <div class="card flex flex-col w-full 2xl:w-1/3 h-full">

                <template v-if="loading">
                    <Skeleton width="60%" height="1.5rem" class="mb-2" />
                    <Skeleton height="200px" />
                </template>

                <template v-else>
                    <div class="flex justify-between items-center mb-2">
                        <div class="flex items-center gap-2 font-semibold text-lg">
                            <span :class=icons.bill></span> 입금 및 미수금 현황
                        </div>
                    </div>

                    <Divider />
                    
                    <canvas ref="chartRefDonut"></canvas>

                </template>

            </div>

            <div class="card flex flex-col w-full 2xl:w-2/3 h-full">

                <template v-if="loading">
                    <Skeleton width="60%" height="1.5rem" class="mb-2" />
                    <Skeleton height="200px" />
                </template>

                <template v-else>
                    <div class="flex justify-between items-center mb-2">
                        <div class="flex items-center gap-2 font-semibold text-lg">
                            <span :class=icons.bill></span> 매출 상위 5개 판매처
                        </div>
                    </div>

                    <Divider />
                    
                    <canvas ref="chartRefBar"></canvas>

                </template>

            </div>

        </div>

  </Fluid>
</template>