<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import Accordion from 'primevue/accordion';
import AccordionPanel from 'primevue/accordionpanel';
import AccordionHeader from 'primevue/accordionheader';
import AccordionContent from 'primevue/accordioncontent';
import { useAppToast } from '@/composables/useAppToast';
import { useRoute } from 'vue-router';
import { useIcon } from '@/composables/useIcon';
import { useDateFormat, useNumberFormat } from '@/composables/useFormat';
import { useUserStore } from '@/stores/user';
import SearchCard from '@/components/card/SearchCard.vue';
import { DataTable } from 'primevue';

// Pinia Store
// (userStore.name)이름
// (userStore.code)코드 - 계정기준으로
const userStore = useUserStore();
const empName = userStore.name;
const empId = userStore.code;

const route = useRoute();
const { toast } = useAppToast();

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

const page = ref({ page: 1, size: 10, totalElements: 0 });
//리스트
const planList = ref([]);
// const prdList = ref([]);
// const mrpList = ref([]);


//공통코드
const mrpStatusMap = ref({});
const purStatusMap = ref({});
const planTypeMap = ref({});
const monthMap = ref({});
const weekMap = ref({});

const monthOptions = ref();
const weekOptions = ref();
const statusOptions = ref();


//검색필드
const searchFilter = ref({
  startDate: '',
  endDate: '',
  status: ''
});

//datePicker날짜변환
const formatDate = (date) => {
  if (!date) return '';
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

//주문목록(페이지로드시)
const fetchList = async () => {
  //검색필터 기간 유효성검사
  if (searchFilter.value.startDate && searchFilter.value.endDate) {
    if (new Date(searchFilter.value.startDate) > new Date(searchFilter.value.endDate)) {
      toast('warn', '기간 오류', '시작일은 종료일보다 이전이어야 합니다.', '3000');
      return;
    }
  }

  const params = {
    startDate: searchFilter.value.startDate,
    endDate: searchFilter.value.endDate,
    status: searchFilter.value.status
  };

  try {
   const res = await axios.get('/api/prd-planList', { params });
   console.log(res.data)

   planList.value = res.data.map(item => ({
      plId: item.plId,
      planType: item.planType,
      planNo: item.planNo,
      empName: item.empName,
      planMonth: item.startDate,
      planWeek: item.endDate,
      mrpStatus: item.mrpStatus,
      productList: [],  //Axios 응답 → planList → 아코디언 + DataTable 렌더링 => 반응형으로 미리 빈배열 선언해둬야함.
      mrpList: []     //Vue의 반응형 시스템은 객체 내부 속성이 나중에 추가 [마스터에 제품이랑 mrp 속성이 추가되어야 해서 미리선언 후 사용]
    }))

  } catch (error) {
    toast('error', '리스트 로드 실패', '주문 리스트 불러오기 실패:', '3000');
  }
};

//상세
const openAcco = async (plan) =>{
    try {
        const res = await axios.get('/api/prd-planDetail', {params: { plId: plan.plId }} )

        const { prodList, mrpList } = res.data

        plan.productList = prodList || []
        plan.mrpList = mrpList || []

    } catch (err) {
       toast('error', '상세 데이터 로드 실패', '데이터를 불러올 수 없습니다.')
    }

}

const loadStatusCodes = async () => {
  try {

    //mrp계산상태
    const mrpStatus = await axios.get('/api/mstatus/p01');
    mrpStatusMap.value = mrpStatus.data.reduce((acc, cur) => {
      acc[cur.codeId] = cur.codeName;
      return acc;
    }, {});

    statusOptions.value = mrpStatus.data.map((item) => ({
      name: item.codeName, // 화면 표시용
      value: item.codeId // 실제 값
    }));

    //월
    const month = await axios.get('/api/mstatus/month');
     monthMap.value = month.data.reduce((acc, cur) => {
      acc[cur.codeId] = cur.codeName;
      return acc;
    }, {});

    monthOptions.value = month.data.map((item) => ({
      name: item.codeName, // 화면 표시용
      value: item.codeId // 실제 값
    }));

    //주
    const week = await axios.get('/api/mstatus/week');
      weekMap.value = week.data.reduce((acc, cur) => {
      acc[cur.codeId] = cur.codeName;
      return acc;
    }, {});

    weekOptions.value = week.data.map((item) => ({
      name: item.codeName, // 화면 표시용
      value: item.codeId // 실제 값
    }));

    //생산유형
    const type = await axios.get('/api/mstatus/p03');
    planTypeMap.value = type.data.reduce((acc, cur) => {
      acc[cur.codeId] = cur.codeName;
      return acc;
    }, {});

    //발주유무
    const purStatus = await axios.get('/api/mstatus/p04');
    purStatusMap.value = purStatus.data.reduce((acc, cur) => {
      acc[cur.codeId] = cur.codeName;
      return acc;
    }, {});
  } catch (err) {
    toast('error', '공통코드 로드 실패', '상태명 불러오기 실패', '3000');
  }
};

const resetSearch = () => {
  searchFilter.value = {
    stardDate: '',
    endDate: '',
    status: ''
  };
  fetchList();
};

onMounted(() => {
  fetchList();
  loadStatusCodes();
});

</script>

<template>
  <div class="container">
    <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    <!--검색영역-->
    <div class="card flex flex-col gap-4 mt-4">
      <SearchCard title="계획 검색" @search="fetchList" @reset="resetSearch">
        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">

          <div class="flex flex-col w-full">
            <InputGroup>
              <InputGroupAddon><i :class="useIcon('calendar')" /></InputGroupAddon>
              <Select v-model="searchFilter.startDate" :options="monthOptions" optionLabel="name" optionValue="value" placeholder="월(Month)" class="w-full h-[48px] text-base" />
            </InputGroup>
          </div>

          <div class="flex flex-col w-full">
            <InputGroup>
              <InputGroupAddon><i :class="useIcon('calendar')" /></InputGroupAddon>
              <Select v-model="searchFilter.endDate" :options="weekOptions" optionLabel="name" optionValue="value" placeholder="주(Week)" class="w-full h-[48px] text-base" />
            </InputGroup>
          </div>

          <div class="flex flex-col w-full">
            <InputGroup>
              <InputGroupAddon><i :class="useIcon('tags')" /></InputGroupAddon>
              <Select v-model="searchFilter.status" :options="statusOptions" optionLabel="name" optionValue="value" placeholder="상태" class="w-full h-[48px] text-base" />
            </InputGroup>
          </div>
        </div>
      </SearchCard>
    </div>
    <!--검색박스 end-->
    <!--아코디언-->
    <div class="card p-0">
        <div class="font-semibold text-xl flex items-center justify-between gap-4 h-10 mb-6">
                <div class="flex items-center gap-4 "><span :class="useIcon('list')"></span> 생산계획 목록</div>
            </div>
        <Accordion value="0">
            <AccordionPanel v-for="plan in planList" :key="plan.plId" :value="plan.plId"  >
                <AccordionHeader @click="openAcco(plan)" >
                  <div class="flex flex-wrap text-xl gap-x-3 text-gray-600">
                    <span><span># </span>{{ monthMap?.[plan.planMonth] || plan.planMonth }}</span>
                    <span>{{ weekMap?.[plan.planWeek] || plan.planWeek }}</span>
                </div>
                </AccordionHeader>
                  <AccordionContent class="p-3 bg-white rounded-b-lg  max-h-[500px] overflow-auto">
                    <div class="flex flex-wrap text-lg gap-x-3 mb-3 justify-end">
                        <span>생산유형: {{ planTypeMap?.[plan.planType] || plan.planType }}</span>
                        <span>계획번호: {{ plan.planNo }}</span>
                        <span>담당자: {{ plan.empName }}</span>
                        <span>[ {{ mrpStatusMap?.[plan.mrpStatus] || plan.mrpStatus }} ]</span>
                    </div>
                    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
                        <!-- 좌측: 제품 -->
                         <div class="border rounded-lg shadow-sm p-3 bg-gray-50">
                            <h4 class="mb-3 font-semibold text-m text-gray-700 border-b pb-1"> 제품 계획</h4>
                            <DataTable :value="plan.productList || []" tableStyle="min-width: 40rem" showGridlines>
                                <Column field="proDate" header="생산일" >
                                    <template #body="{ data }">
                                        {{ useDateFormat(data.proDate) }}
                                    </template>
                                </Column>
                                <Column field="productVO.prodName" header="제품명" />
                                <Column field="proQty" header="계획수량" />
                                <Column field="productVO.unit" header="단위" />
                            </DataTable>
                        </div>

                        <!-- 우측: 자재 소요 -->
                        <div class="border rounded-lg shadow-sm p-3 bg-gray-50">
                            <h4 class="mb-3 font-semibold text-m text-gray-700 border-b pb-1">자재 소요 및 발주 현황</h4>
                            <DataTable :value="plan.mrpList || []" tableStyle="min-width: 40rem" showGridlines>
                                <Column field="materialVO.matName" header="자재명" />
                                <Column field="shortageQty" header="부족수량" />
                                <Column field="materialVO.stockUnit" header="단위" />
                                <Column field="needsWeight" header="환산중량" />
                                <Column field="unit" header="단위" />
                                <Column field="status" header="발주상태">
                                    <template #body="{ data }">
                                        {{ purStatusMap[data.status] || data.status }}
                                    </template>
                                </Column>
                            </DataTable>
                        </div>
                    </div>
                </AccordionContent>
            </AccordionPanel>
        </Accordion>
    </div>
    </div>
</template>
