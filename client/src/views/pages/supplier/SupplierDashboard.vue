<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import { useAppToast } from '@/composables/useAppToast';
import { useRoute } from 'vue-router';
import { useIcon } from '@/composables/useIcon';
import { useDateFormat, useNumberFormat } from '@/composables/useFormat';
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

//데이터
const summaryCards = ref([]);
const recentList = ref();
const defectList = ref();
const chartData = ref({});
const chartOptions = ref({});

function fmt(v) {
  const n = Number(v);
  return isNaN(n) ? '₩0' : '₩' + n.toLocaleString('ko-KR');
}

onMounted(async () => {
  try {
    const { data } = await axios.get(`/api/sdashboard/${vendorId}`);

    //summaryDTO 기반
    summaryCards.value = [
      { label: '승인 대기 건', value: data.summary.beforeApprove },
      { label: '출고지시 대기 건', value: data.summary.beforeShipOrder },
      { label: '총 주문건 수(6개월)', value: data.summary.totalOrder },
      { label: '총 출고건 수(6개월)', value: data.summary.totalInbound },
      { label: '총 주문금액(6개월)', value: data.summary.totalAmount }
    ];

    //최근주문출고테이블
    recentList.value = data.recentList.map((item) => ({
      type: item.type,
      refNo: item.refNo,
      matId: item.matId,
      matName: item.matName,
      qty: item.qty,
      unit: 'EA',
      regDate: useDateFormat(item.regDate)
    }));

    //불량테이블
    defectList.value = data.defectList.map((item) => ({
      matName: item.matName,
      qty: item.logRejQty,
      unit: 'EA',
      regDate: item.logMemo
    }));

    //파이차트
    chartData.value = {
      labels: data.pieList.map((v) => v.matName),
      datasets: [
        {
          label: '출고량 비율',
          data: data.pieList.map((v) => v.qty),
          backgroundColor: ['#60A5FA', '#4ADE80', '#FACC15', '#F87171', '#A78BFA']
        }
      ]
    };

    chartOptions.value = {
      plugins: {
        legend: { position: 'bottom' },
        tooltip: {
          callbacks: {
            label: (ctx) => `${ctx.label}: ${ctx.formattedValue} EA`
          }
        }
      }
    };
  } catch (err) {
    toast('error', '대시보드 데이터를 불러오지 못했습니다.', err.message);
  }
});

const defectColumns = [
  { field: 'matName', label: '자재명', style: 'width: 140px' },
  { field: 'qty', label: '수량', style: 'width: 50px; text-align: right' },
  { field: 'unit', label: '단위', style: 'width: 25px; text-align: right' },
  { field: 'regDate', label: '사유', style: 'width: 100px' }
];

const tableColumns = [
  { field: 'type', label: '구분', style: 'width: 25px' },
  { field: 'refNo', label: '번호', style: 'width: 90px' },
  //{ field: 'matId', label: '자재코드', style: 'width: 30px; text-align: center' },
  { field: 'matName', label: '자재명', style: 'width: 150px' },
  { field: 'qty', label: '수량', style: 'width: 50px; text-align: right' },
  { field: 'unit', label: '단위', style: 'width: 25px' }
  //{ field: 'regDate', label: '일자', style: 'width: 100px; text-align: center' }
];
</script>

<template>
  <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
  <div class="p-6 space-y-6 w-full">
    <!-- 요약 카드 -->
    <div class="grid grid-cols-5 gap-4">
      <div v-for="(item, i) in summaryCards" :key="i" class="card flex flex-col justify-center text-center p-6" style="height: 128px">
        <p class="text-gray-500 text-l mb-1 font-large">{{ item.label }}</p>
        <!-- 금액(₩)인 경우만 fmt 적용 -->
        <h4 class="text-3xl font-bold text-gray-900">
          {{ item.label.includes('금액') ? fmt(item.value) : useNumberFormat(item.value).value }}
        </h4>
      </div>
    </div>
    <div class="flex flex-col md:flex-row gap-8">
      <!-- 상태별 현황 차트 -->
      <div class="md:w-1/5">
        <div class="card p-4" style="height: 450px">
          <div class="flex items-center justify-between gap-4 h-10">
            <h3 class="text-lg font-semibold flex items-center gap-4"><span :class="useIcon('history')"></span> 최근 3개월 출고 품목 비율</h3>
          </div>
          <Divider />
          <Chart type="pie" :data="chartData" :options="chartOptions" class="w-full md:w-[20rem]" />
        </div>
      </div>
      <div class="md:w-2/5">
        <div class="card p-4" style="height: 450px">
          <div class="flex items-center justify-between gap-4 h-10">
            <h3 class="text-lg font-semibold flex items-center gap-4"><span :class="useIcon('list')"></span>불량 내역</h3>
          </div>
          <Divider />
          <selectTable v-model:selection="selectedDetails" :selectionMode="'single'" :columns="defectColumns" :data="defectList" :paginator="false" :showCheckbox="false" />
        </div>
      </div>
      <!-- 최근 주문 / 출고 내역 -->
      <div class="md:w-3/5">
        <div class="card p-4" style="height: 450px">
          <div class="flex items-center justify-between gap-4 h-10">
            <h3 class="text-lg font-semibold flex items-center gap-4"><span :class="useIcon('list')"></span>최근 주문 / 출고 내역</h3>
          </div>
          <Divider />
          <selectTable v-model:selection="selectedDetail" :selectionMode="'single'" :columns="tableColumns" :data="recentList" :paginator="false" :showCheckbox="false" />
        </div>
      </div>
    </div>
  </div>
</template>
