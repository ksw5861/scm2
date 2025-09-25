<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import selectTable from '@/components/common/checkBoxTable.vue';
import btn from '@/components/common/Btn.vue';
import searchField from '@/components/common/SearchBox.vue';

//등록일 날짜출력[페이지로드시 오늘날짜 자동셋팅]
const getNowDate = () => {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, '0');
  const day = String(today.getDate()).padStart(2, '0');
  return `${year}/${month}/${day}`;
};

//master 인풋박스
const empName = ref('로그인구현전');
const dateRange = ref({ start: null, end: null });
const resDate = ref(getNowDate());

//테이블 행 key값
const columnId = ref(1);
const getNextId = () => {
  return ++columnId.value;
};
// 디테일 1행출력 [페이지로드시 1행 자동셋팅]
const productionPlans = ref([{ id: columnId.value, prodId: '', prdName: '', proQty: null, unit: '', proDate: '' }]);
//드롭다운옵션용
const productOptions = ref([]);

onMounted(async () => {
  const res = await axios.get('/api/mat/productsList');
  productOptions.value = res.data.map((item) => ({
    label: item.prodName,
    value: item.prodId,
    unit: item.unit
  }));
});

console.log(productOptions);
console.log(productOptions.value);

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
  { field: 'prdName', label: '제품명', select: true, style: 'width: 20rem', option: productOptions.value, selectOpt },
  { field: 'proQty', label: '생산수량', inputNumber: true, style: 'width: 10rem' },
  { field: 'unit', label: '단위', style: 'width: 9rem' },
  { field: 'proDate', label: '생산예정일', datePicker: true, style: 'width: 20rem' }
]);

//상단박스버튼
const resetForm = () => {
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
    startDate: dateRange.value.start,
    endDate: dateRange.value.end,
    empName: empName.value,
    prdPlanDetailList: productionPlans.value
  };

  // 유효성 검사: 제품 계획이 비어있는지 확인
  if (!plan.prdPlanDetailList || plan.prdPlanDetailList.length === 0 || !plan.prdPlanDetailList[0].prdName || !plan.prdPlanDetailList[0].proQty) {
    alert('제품 계획을 하나 이상 입력해주세요.');
    return;
  }

  try {
    const response = await axios.post('/api/mat/productionPlan', plan);
    alert('등록 성공!');
  } catch (error) {
    alert('등록 실패!');
  }

  //폼 초기화
  resetForm();
};
</script>

<template>
  <div class="container">
    <!--(master)상단박스 start-->
    <div class="card flex flex-col gap-4">
      <div class="font-semibold text-xl">생산계획 등록</div>
      <Divider />
      <div class="flex flex-wrap md:justify-end items-center gap-2">
        <btn color="secondary" icon="pi pi-undo" @click="resetForm"> 초기화 </btn>
        <btn color="contrast" icon="pi pi-search" @click="submit"> 등록 </btn>
      </div>
      <!--인풋박스s-->
      <div class="flex flex-col gap-4 md:flex-row md:justify-center gap-10 mt-5 mb-10">
        <searchField type="readOnly" label="등록일" v-model="resDate" />
        <searchField type="dateRange" label="생산계획기간" v-model="dateRange" />
        <searchField type="readOnly" label="담당자" v-model="empName" />
      </div>
    </div>
    <!--(master)상단박스 end-->

    <!--(detail)하단박스 start-->
    <div class="card flex flex-col gap-4">
      <div class="flex justify-between items-center">
        <div class="font-semibold text-l">제품 계획</div>
        <div class="flex whitespace-nowrap items-center justify-end gap-2">
          <btn color="secondary" icon="pi pi-file-excel" @click="addRow"> 행추가 </btn>
          <btn color="secondary" icon="pi pi-file-pdf" @click="deleteRow"> 행삭제 </btn>
        </div>
      </div>
      <selectTable :columns="detailColumns" :data="productionPlans" :paginator="false" :showCheckbox="false" />
    </div>
    <!--(detail)하단박스 end-->
  </div>
</template>
