<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import selectTable from '@/components/common/checkBoxTable.vue';
import btn from '@/components/common/Btn.vue';
import { useAppToast } from '@/composables/useAppToast';
import { useRoute } from 'vue-router';
import { useIcon } from '@/composables/useIcon';
import { useUserStore } from '@/stores/user';

// Pinia Store
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
const statusOptions = ref([]); //생산옵션
const monthOptions = ref([]); //월옵션
const weekOptions = ref([]); //주차옵션
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
    const res = await axios.get('/api/mproductsList');
    productOptions.value = res.data.map((item) => ({
      label: item.prodName,
      value: item.prodId,
      unit: item.unit
    }));
  } catch (error) {
    toast('error', '리스트 로드 실패', '제품 리스트 불러오기 실패:', '3000');
  }
};

const planTypeList = async () => {
  try {
    const res = await axios.get('/api/mstatus/p03');
    statusOptions.value = res.data.map((item) => ({
      name: item.codeName, // 화면 표시용
      value: item.codeId // 실제 값
    }));
  } catch (error) {
    toast('error', '상태값 로드 실패', '코드 불러오기 실패', '3000');
  }
};

//월코드
const monthList = async () => {
  try {
    const res = await axios.get('/api/mstatus/month');
    monthOptions.value = res.data.map((item) => ({
      name: item.codeName, // 화면 표시용
      value: item.codeId // 실제 값
    }));
  } catch (error) {
    toast('error', '상태값 로드 실패', '코드 불러오기 실패', '3000');
  }
};

//주차코드
const weekList = async () => {
  try {
    const res = await axios.get('/api/mstatus/week');
    weekOptions.value = res.data.map((item) => ({
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

  console.log(dateRange.value.start);
  // 마스터 + 디테일 묶기
  const plan = {
    //vo필드명 : vue 바인드
    planType: planType.value,
    memo: memo.value,
    startDate: dateRange.value.start,
    endDate: dateRange.value.end,
    empName: empName,
    prdPlanDetailList: productionPlans.value
  };

    //유효성 검사: 필수 입력값 확인
  if (!plan.planType || !plan.startDate || !plan.endDate) {
    toast('warn', '유효성 검사', '필수 입력값을 모두 입력해주세요.', '3000');
    return;
  }

  // 유효성 검사: 제품 계획이 비어있는지 확인
  if (!plan.prdPlanDetailList || plan.prdPlanDetailList.length === 0 || !plan.prdPlanDetailList[0].prdName || !plan.prdPlanDetailList[0].proQty) {
    toast('warn', '유효성 검사', '제품 계획을 하나 이상 입력해주세요.', '3000');
    return;
  }

  if (!confirm('생산계획을 등록하시겠습니까?')) {
    return;
  }


  try {
    const response = await axios.post('/api/mproductionPlan', plan);
    toast('success', '등록 성공', '제품생산이 계획등록되었습니다.', '3000');
    resetForm();
  } catch (error) {
    toast('error', '등록 실패', '생산 계획등록 실패', '3000');
  }

  //폼 초기화
  resetForm();
};

onMounted(() => {
  pageLoad();
  planTypeList();
  monthList();
  weekList();
});
</script>

<template>
  <div class="container">
    <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    <!--(master)상단박스 start-->
    <div class="card flex flex-col gap-4 mt-4 pb-6">
      <div class="font-semibold text-xl flex items-center justify-between gap-4 h-10">
        <div class="flex items-center gap-4"><span :class="useIcon('edit')"></span>생산계획 등록</div>
      </div>
      <Divider />
      <!--인풋박스s-->
      <div class="flex flex-col gap-6 mt-5 mb-10">
        <!-- 1행 -->
        <div class="flex flex-col gap-4 md:flex-row md:gap-10">
          <!-- <searchField type="dropDown" label="계획유형" v-model="planType" class="w-64" :options="statusOptions" />
          <searchField type="dateRange" label="생산계획기간" v-model="dateRange" class="w-100" />
          <searchField type="text" label="비고" v-model="memo" class="w-96" />
          <searchField type="readOnly" label="등록일" v-model="resDate" class="w-45" />
          <searchField type="readOnly" label="담당자" v-model="empName" class="w-45" /> -->

          <!--생산계획-->
          <div class="flex flex-col w-full">
            <InputGroup>
              <InputGroupAddon><i :class="useIcon('key')" /></InputGroupAddon>
              <Select v-model="planType" :options="statusOptions" optionLabel="name" optionValue="value" placeholder="계획유형" class="w-full h-[48px] text-base" />
            </InputGroup>
          </div>

          <!--생산계획기간/월-->
          <div class="flex flex-col w-full">
            <InputGroup>
              <InputGroupAddon><i :class="useIcon('calendar')" /></InputGroupAddon>
              <Select v-model="dateRange.start" :options="monthOptions" optionLabel="name" optionValue="value" placeholder="월" class="w-full h-[48px] text-base" />
            </InputGroup>
          </div>
          <!--주-->
          <div class="flex flex-col w-full">
            <InputGroup>
              <InputGroupAddon><i :class="useIcon('calendar')" /></InputGroupAddon>
              <Select v-model="dateRange.end" :options="weekOptions" optionLabel="name" optionValue="value" placeholder="주차" class="w-full h-[48px] text-base" />
            </InputGroup>
          </div>
          <!--비고-->
          <InputGroup>
            <InputGroupAddon><i :class="useIcon('add')" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="memo" inputId="memo" />
              <label for="searchMatName">비고</label>
            </IftaLabel>
          </InputGroup>
          <!--등록일-->
          <InputGroup>
            <InputGroupAddon><i :class="useIcon('calendar')" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="resDate" inputId="resDate" readonly />
              <label for="searchMatName">등록일</label>
            </IftaLabel>
          </InputGroup>
          <!--담당자-->
          <InputGroup>
            <InputGroupAddon><i :class="useIcon('employee')" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="empName" inputId="empName" readonly />
              <label for="searchMatName">담당자</label>
            </IftaLabel>
          </InputGroup>
        </div>
      </div>
      <div class="flex sm:justify-end justify-start gap-2 mt-4">
        <div class="flex gap-2">
          <btn color="secondary" icon="check" label="초기화" class="whitespace-nowrap" outlined @click="resetForm" />
          <btn color="info" icon="check" label="생산계획등록" class="whitespace-nowrap" outlined @click="submit" />
        </div>
      </div>
    </div>
    <!--(master)상단박스 end-->

    <!--(detail)하단박스 start-->
    <div class="card flex flex-col gap-4">
      <div class="flex justify-between items-center">
        <div class="font-semibold text-xl flex items-center justify-between gap-4 h-10">
          <div class="flex items-center gap-4"><span :class="useIcon('box')"></span> 제품 계획</div>
        </div>
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
