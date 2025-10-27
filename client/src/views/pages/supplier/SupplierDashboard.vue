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

const icons = {
    hourglass: useIcon('hourglass'),
    truck: useIcon('truck'),
    cart: useIcon('cart'),
    box: useIcon('box'),
    dollar: useIcon('dollar'),
    history: useIcon('history'),
    wrench: useIcon('wrench'), 
    list: useIcon('list')
};

// breadcrumb
const breadcrumbHome = { icon: useIcon('home'), to: '/' };
const breadcrumbItems = computed(() => {
  const matched = route.matched.filter((r) => r.meta);
  if (!matched.length) return [];
  const current = matched[matched.length - 1];
  const parentLabel = current.meta?.breadcrumb?.parent || '주문 조회';
  const currentLabel = current.name || '';
  return [{ label: "대시보드", to: route.fullPath }];
});

//데이터
const summaryCards = ref([]);
const recentList = ref();
const defectList = ref();
const chartData = ref({});
const chartOptions = ref({});
const loading = ref(true);

function fmt(v) {
  const n = Number(v);
  return isNaN(n) ? '₩0' : '₩' + n.toLocaleString('ko-KR');
}

onMounted(async () => {
  try {
    const { data } = await axios.get(`/api/sdashboard/${vendorId}`);

    //summaryDTO 기반
    summaryCards.value = [
        { label: '총 주문 금액 (6개월)', value: data.summary.totalAmount, color: 'red', icon: icons.dollar },
        { label: '승인 대기건', value: data.summary.beforeApprove, color: 'yellow', icon: icons.hourglass },
        { label: '출고 대기건', value: data.summary.beforeShipOrder, color: 'blue', icon: icons.truck },
        { label: '총 주문건 (6개월)', value: data.summary.totalOrder, color: 'green', icon: icons.cart },
        { label: '총 출고건 (6개월)', value: data.summary.totalInbound, color: 'purple', icon: icons.box }
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
  } finally {
    loading.value = false;
  }
});

const defectColumns = [
  { field: 'matName', label: '자재명', style: 'width: 100px' },
  { field: 'qty', label: '수량', style: 'width: 50px; text-align: right' },
  { field: 'unit', label: '단위', style: 'width: 25px;' },
  { field: 'regDate', label: '사유', style: 'width: 100px' }
];

const tableColumns = [
  { field: 'type', label: '구분', style: 'width: 25px' },
  { field: 'refNo', label: '번호', style: 'width: 90px' },
  { field: 'matName', label: '자재명', style: 'width: 150px' },
  { field: 'qty', label: '수량', style: 'width: 60px; text-align: right' },
  { field: 'unit', label: '단위', style: 'width: 25px' }
];
</script>

<template>

    <Fluid>
        <Breadcrumb class="rounded-lg block w-full" :home="breadcrumbHome" :model="breadcrumbItems" />

        <div class="grid grid-cols-2 2xl:grid-cols-5 gap-4 mt-4">

            <template v-if="loading">

                <div v-for="n in 5" :key="n" class="card h-30" height="7rem" style="margin-bottom: 0;">
                    
                    <div class="flex justify-between mb-4">
                        <Skeleton width="40%" height="1.5rem" />
                        <Skeleton width="2.5rem" height="2.5rem" class="rounded-full" />
                    </div>

                    <Skeleton height="2rem" />
                </div>

            </template>

            <template v-else>

                <div v-for="(item, i) in summaryCards" :key="i" class="card h-30" style="margin-bottom: 0;">
                    <div class="flex justify-between mb-4">

                        <div>
                            <span class="block text-muted-color font-medium mb-4">{{ item.label }}</span>
                            <div
                                class="dark:text-surface-0 font-bold text-xl"
                                :class="`text-${item.color}-500`"
                            >

                                {{ item.type === 'money' ? fmt(item.value) : useNumberFormat(item.value).value }}<span class="font-medium text-gray-700">{{ item.type === 'money' ? '원' : '건' }}</span>
                            
                            </div>
                        </div>

                        <div
                            class="flex items-center justify-center rounded-border"
                            :class="'bg-' + item.color + '-100 dark:bg-' + item.color + '-100/10'"
                            style="width:2.5rem;height:2.5rem;"
                        >
                            <i class="!text-xl" :class="[item.icon, 'text-' + item.color + '-500']"></i>
                        </div>

                    </div>
                </div>

            </template>

        </div>

        <div class="flex w-full gap-4 mt-4 items-stretch">

            <div class="card flex flex-col w-1/4 h-full">

                <template v-if="loading">
                    <Skeleton width="60%" height="1.5rem" class="mb-2" />
                    <Skeleton height="200px" />
                </template>

                <template v-else>
                    <div class="flex justify-between items-center mb-2">
                        <div class="flex items-center gap-2 font-semibold text-lg">
                            <span :class=icons.history></span> 최근 3개월 출고 품목 비율
                        </div>
                    </div>

                    <Divider />

                    <Chart type="pie" :data="chartData" :options="chartOptions" class="w-full" />
                </template>

            </div>

            <div class="card flex flex-col w-1/2 h-full">

                <template v-if="loading">
                    <Skeleton width="60%" height="1.5rem" class="mb-2" />
                    <Skeleton v-for="n in 4" :key="n" height="2rem" class="mb-2" />
                </template>

                <template v-else>

                    <div class="flex justify-between items-center mb-2">
                        <div class="flex items-center gap-2 font-semibold text-lg">
                            <span :class=icons.list></span> 최근 주문 및 출고 내역
                        </div>
                    </div>

                    <Divider />

                    <selectTable
                        v-model:selection="selectedDetail"
                        :selectionMode="'single'"
                        :columns="tableColumns"
                        :data="recentList"
                        :paginator="false"
                        :showCheckbox="false"
                    />

                </template>
            </div>

            <div class="card flex flex-col w-1/4 h-full">

                <template v-if="loading">
                    <Skeleton width="60%" height="1.5rem" class="mb-2" />
                    <Skeleton v-for="n in 4" :key="n" height="2rem" class="mb-2" />
                </template>

                <template v-else>
                    <div class="flex justify-between items-center mb-2">
                        <div class="flex items-center gap-2 font-semibold text-lg">
                            <span :class=icons.wrench></span> 불량 내역
                        </div>
                    </div>

                    <Divider />

                    <selectTable
                        v-model:selection="selectedDetails"
                        :selectionMode="'single'"
                        :columns="defectColumns"
                        :data="defectList" 
                        :paginator="false" 
                        :showCheckbox="false"
                    />
                </template>
            </div>
            
        </div>

    </Fluid>

</template>
