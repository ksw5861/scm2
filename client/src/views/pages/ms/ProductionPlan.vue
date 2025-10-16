<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import selectTable from '@/components/common/checkBoxTable.vue';
import btn from '@/components/common/Btn.vue';
import searchField from '@/components/common/SearchBox.vue';
import { useAppToast } from '@/composables/useAppToast';
import { useRoute } from 'vue-router';
import { useIcon } from '@/composables/useIcon';
import { useUserStore } from '@/stores/user';

// Pinia Store
const userStore = useUserStore();
const empName = ref(userStore.name);
const empId = ref(userStore.code);


const route = useRoute();
const { toast } = useAppToast();

// breadcrumb
const breadcrumbHome = { icon: useIcon('home'), to: '/' };
const breadcrumbItems = computed(() => {
  const matched = route.matched.filter((r) => r.meta);
  if (!matched.length) return [];
  const current = matched[matched.length - 1];
  const parentLabel = current.meta?.breadcrumb?.parent || '생산 계획';
  const currentLabel = current.name || '';
  return [{ label: parentLabel }, { label: currentLabel, to: route.fullPath }];
});

//등록일 날짜출력[페이지로드시 오늘날짜 자동셋팅]
const getNowDate = () => {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, '0');
  const day = String(today.getDate()).padStart(2, '0');
  return `${year}/${month}/${day}`;
};

//master 인풋박스
const dateRange = ref({ start: null, end: null });
const resDate = ref(getNowDate());
const memo = ref('');
const statusOptions = ref([]);
const planType = ref('');

//테이블 행 key값
const columnId = ref(1);
const getNextId = () => {
  return ++columnId.value;
};
// 디테일 1행출력 [페이지로드시 1행 자동셋팅]
const productionPlans = ref([{ id: columnId.value, prodId: '', prdName: '', proQty: null, unit: '', proDate: '' }]);
//드롭다운용 (제품선택용/제품리스트)
const productOptions = ref([]);

const pageLoad = async () => {
  try {
    const res = await axios.get('/api/mat/productsList');
    productOptions.value = res.data.map((item) => ({
      label: item.prodName,
      value: item.prodId,
      unit: item.unit
    }));
  } catch (error) {
    toast('error', '리스트 로드 실패', '제품 리스트 불러오기 실패:', '3000');
  }
};

onMounted(() => {
  pageLoad();
  planTypeList();
});

const planTypeList = async () => {
  try {
    const res = await axios.get('/api/mat/status/p03');
    statusOptions.value = res.data.map((item) => ({
      name: item.codeName, // 화면 표시용
      value: item.codeId // 실제 값
    }));
  } catch (error) {
    toast('error', '상태값 로드 실패', '코드 불러오기 실패', '3000');
  }
};

//옵션선택시 컬럼반영
const selectOpt = (row, value) => {
  const selected = productOptions.value.find((opt) => opt.value === value);
  if (selected) {
    row.prodId = selected.value;
    row.unit = selected.unit;
  }
};

const detailColumns = computed(() => [
  { field: 'prodId', label: '제품코드', style: 'width: 15rem' },
  { field: 'prdName', label: '제품명', select: true, style: 'width: 20rem', option: productOptions.value, change: selectOpt },
  { field: 'proQty', label: '생산수량', inputNumber: true, style: 'width: 10rem' },
  { field: 'unit', label: '단위', style: 'width: 9rem' },
  { field: 'proDate', label: '생산예정일', datePicker: true, style: 'width: 20rem' }
]);

//상단박스버튼
const resetForm = () => {
  planType.value = '';
  memo.value = '';
  dateRange.value = { start: null, end: null };
  productionPlans.value = [{ id: columnId.value, prodId: '', prdName: '', proQty: null, unit: '', proDate: '' }];
  columnId.value = 1;
};

//행추가 && 삭제
const addRow = () => {
  productionPlans.value.push({ id: getNextId(), prodId: '', prdName: '', proQty: null, unit: '', proDate: '' });
};
const deleteRow = () => {
  if (productionPlans.value.length > 1) {
    productionPlans.value.pop();
  }
};

// 계획등록 함수
const submit = async () => {
  // 마스터 + 디테일 묶기
  const plan = {
    //vo필드명 : vue 바인드
    planType: planType.value,
    memo: memo.value,
    startDate: dateRange.value.start,
    endDate: dateRange.value.end,
    empName: empName.value,
    prdPlanDetailList: productionPlans.value
  };

  // 유효성 검사: 제품 계획이 비어있는지 확인
  if (!plan.prdPlanDetailList || plan.prdPlanDetailList.length === 0 || !plan.prdPlanDetailList[0].prdName || !plan.prdPlanDetailList[0].proQty) {
    toast('info', '유효성 검사', '제품 계획을 하나 이상 입력해주세요.', '3000');
    return;
  }

  console.log('plan', JSON.stringify(plan, null, 2));

  try {
    const response = await axios.post('/api/mat/productionPlan', plan);
    toast('info', '등록 성공', '생산 계획등록 성공:', '3000');
    resetForm();
  } catch (error) {
    toast('error', '등록 실패', '생산 계획등록 실패:', '3000');
  }

  //폼 초기화
  resetForm();
};
</script>

<template>
  <div class="container">
    <div class="p-4">
      <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    </div>
    <!--(master)상단박스 start-->
    <div class="card flex flex-col gap-4">
      <div class="font-semibold text-xl">생산계획 등록</div>
      <Divider />
      <!--인풋박스s-->
      <div class="flex flex-col gap-6 mt-5 mb-10">
        <!-- 1행 -->
        <div class="flex flex-col gap-4 md:flex-row md:gap-10">
          <searchField type="dropDown" label="계획유형" v-model="planType" class="w-64" :options="statusOptions" />
          <searchField type="dateRange" label="생산계획기간" v-model="dateRange" class="w-100" />
          <searchField type="text" label="비고" v-model="memo" class="w-96" />
          <searchField type="readOnly" label="등록일" v-model="resDate" class="w-45" />
          <searchField type="readOnly" label="담당자" v-model="empName" class="w-45" />
        </div>
      </div>
      <div class="flex sm:justify-end justify-start gap-2 mt-4">
        <div class="w-1/2 sm:w-32">
          <Btn color="secondary" class="w-full" icon="refresh" label="초기화" @click="resetForm" outlined />
        </div>
        <div class="w-1/2 sm:w-32">
          <Btn class="w-full" icon="add" severity="success" label="등록" variant="outlined" @click="submit" />
        </div>
      </div>
    </div>
    <!--(master)상단박스 end-->

    <!--(detail)하단박스 start-->
    <div class="card flex flex-col gap-4">
      <div class="flex justify-between items-center">
        <div class="font-semibold text-l">제품 계획</div>
        <div class="flex whitespace-nowrap items-center justify-end gap-2">
          <btn color="secondary" icon="add" @click="addRow" severity="secondary" variant="outlined"></btn>
          <btn color="secondary" icon="minus" @click="deleteRow" severity="secondary" variant="outlined"></btn>
        </div>
      </div>
      <selectTable :columns="detailColumns" :data="productionPlans" :paginator="false" :showCheckbox="false" />
    </div>
    <!--(detail)하단박스 end-->
  </div>
</template>
