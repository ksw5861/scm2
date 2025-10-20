<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import { useAppToast } from '@/composables/useAppToast';
import { useRoute } from 'vue-router';
import { useIcon } from '@/composables/useIcon';
import { useNumberFormat } from '@/composables/useFormat';
import { useUserStore } from '@/stores/user';
import SelectTable from '@/components/common/checkBoxTable.vue';

const route = useRoute();
const { toast } = useAppToast();
const userStore = useUserStore();
const vendorId = userStore.code;

// breadcrumb
const breadcrumbHome = { icon: useIcon('home'), to: '/' };
const breadcrumbItems = computed(() => {
  const matched = route.matched.filter((r) => r.meta);
  if (!matched.length) return [];
  const current = matched[matched.length - 1];
  const parentLabel = current.meta?.breadcrumb?.parent || '주문 조회';
  const currentLabel = current.name || '';
  return [{ label: parentLabel }, { label: currentLabel, to: route.fullPath }];
});

// reactive state
const summaryCards = ref([])
const recentList = ref()

function fmt(v) {
  const n = Number(v);
  return isNaN(n) ? '₩0' : '₩' + n.toLocaleString('ko-KR');
}

onMounted(async () => {
  try {
    const { data } = await axios.get(`/api/supplier/dashboard/${vendorId}`);

    console.log(data);
    console.log(data.summary.beforeApprove)
    console.log(data.recentList[0].type)

    //summaryDTO 기반
    summaryCards.value = [
      { label: '승인 대기 건', value: data.summary.beforeApprove },
      { label: '출고지시 대기 건', value: data.summary.beforeShipOrder },
      { label: '총 주문건 수(6개월)', value: data.summary.totalOrder },
      { label: '총 출고건 수(6개월)', value: data.summary.totalInbound },
      { label: '총 주문금액(6개월)', value: data.summary.totalAmount }
    ];

    //테이블
    recentList.value = data.recentList.map((item) => ({
      type: item.type,
      refNo: item.refNo,
      matId: item.matId,
      qty: item.qty,
      unit: 'EA',
      regDate: item.regDate
    }));

    //차트 데이터 (임시 예시 — chartDTO 생기면 교체)
    chartData.value = {
      labels: ['발주', '출고', '배송중', '입고완료', '반품'],
      datasets: [
        {
          label: '건수',
          data: [12, 8, 5, 10, 2], // chartDTO 오면 여기에 바인딩
          backgroundColor: '#60A5FA',
        },
      ],
    };

    // chartOptions.value = {
    //   plugins: {
    //     legend: { display: false },
    //   },
    //   scales: {
    //     y: { beginAtZero: true, ticks: { stepSize: 1 } },
    //   },
    // };

    // ecentList.value = data.recentList || []

    // ✅ 동적 컬럼 정의
  } catch (err) {
    toast('error', '대시보드 데이터를 불러오지 못했습니다.', err.message);
  }
});

const tableColumns = [
      { field: 'type', label: '구분', style: 'width:10px; text-align: center' },
      { field: 'refNo', label: '번호', style: 'width: 100px; text-align: center' },
      { field: 'matId', label: '자재코드', style: 'width: 80px; text-align: center' },
      { field: 'qty', label: '수량', style: 'width: 50px; text-align: right' },
      { field: 'unit', label: '단위', style: 'width: 25px; text-align: right' },
      { field: 'regDate', label: '일자', style: 'width: 140px; text-align: center' }
    ]
</script>

<template>
  <Fluid>
    <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    <div class="flex gap-4 mt-4">
        <div class="p-6 space-y-6 w-full">
        <!-- 요약 카드 -->
        <div class="grid grid-cols-5 gap-4">
            <div v-for="(item, i) in summaryCards" :key="i" class="card flex flex-col justify-center text-center p-6 shadow-md" style="height: 128px;">
                <p class="text-gray-500 text-l mb-2 font-large">{{ item.label }}</p>
                <!-- 금액(₩)인 경우만 fmt 적용 -->
                <h4 class="text-3xl font-bold text-gray-900">
                    {{ item.label.includes('금액') ? fmt(item.value) : useNumberFormat(item.value).value }}
                </h4>
            </div>
        </div>


    <!-- 상태별 현황 차트 -->
    <div class="card shadow-md p-4">
      <!-- <h3 class="text-lg font-semibold mb-4">공급 진행 현황</h3>
      <Chart type="bar" :data="chartData" :options="chartOptions" class="h-80" /> -->
    </div>

    <!-- 최근 주문 / 출고 내역 -->
    <div class="card shadow-md p-4">
      <h3 class="text-lg font-semibold mb-4">최근 주문 / 출고 내역</h3>
      <Divider />
      <selectTable v-model:selection="selectedDetail" :selectionMode="'single'" :columns="tableColumns" :data="recentList" :paginator="false" :showCheckbox="false" />
    </div>
  </div>
</div>

</Fluid>
</template>
