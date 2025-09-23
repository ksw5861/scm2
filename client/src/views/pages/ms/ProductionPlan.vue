<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

import selectTable from '@/components/common/checkBoxTable.vue';
import btn from '@/components/common/Btn.vue';
import SearchField from '@/components/common/SearchBox.vue';

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

//detail 컬럼
const openModal = () => {
  console.log('모달 열기');
};

const detailColumns = [
  { field: 'prdCod', label: '제품코드', style: 'width: 15rem' },
  { field: 'prdName', label: '제품명', inputText: true, style: 'width: 30rem', onClick: openModal },
  { field: 'qty', label: '생산수량', inputNumber: true, style: 'width: 10rem' },
  { field: 'unit', label: '단위', style: 'width: 9rem' },
  { field: 'planDate', label: '생산예정일', datePicker: true, style: 'width: 20rem' }
];

const columnId = ref(1);
const getNextId = () => {
  return columnId.value++;
};

// 디테일 1행출력 [페이지로드시 1행 자동셋팅]
const productionPlans = ref([{ id: columnId.value, prdCod: '', prdName: '', qty: null, unit: '', planDate: '' }]);

//상단박스버튼
const resetForm = () => {
  dateRange.value = { start: null, end: null };
  productionPlans.value = [{ id: columnId.value, prdCod: '', prdName: '', qty: null, unit: '', planDate: '' }];
  columnId.value = 1;
};

//행추가 && 삭제
const addRow = () => {
  productionPlans.value.push({ id: getNextId(), prdCod: '', prdName: '', qty: null, unit: '', planDate: '' });
};
const deleteRow = () => {
  if (productionPlans.value.length > 1) {
    productionPlans.value.pop();
  }
};

// 등록 함수
const submitForm = async () => {
  // 마스터 + 디테일 묶기
  const payload = {
    empName: empName.value,
    startDate: dateRange.value.start,
    endDate: dateRange.value.end,
    regDate: resDate.value,
    details: productionPlans.value
  };

  try {
    const response = await axios.post('/api/plan', payload);
    console.log('등록 성공:', response.data);
    alert('등록 성공!');
  } catch (error) {
    console.error('등록 실패:', error);
    alert('등록 실패!');
  }
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
        <btn color="contrast" icon="pi pi-search"> 등록 </btn>
      </div>
      <!--인풋박스s-->
      <div class="flex flex-col gap-4 md:flex-row md:justify-center gap-10 mt-5 mb-10">
        <SearchField type="readOnly" label="등록일" v-model="resDate" />
        <SearchField type="dateRange" label="생산계획기간" v-model="dateRange" />
        <SearchField type="readOnly" label="담당자" v-model="empName" />
      </div>
    </div>
    <!--(master)상단박스 end-->

    <!--(detail)하단박스 start-->
    <div class="card flex flex-col gap-4">
      <div class="flex justify-between items-center">
        <div class="font-semibold text-xl">제품 계획</div>
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
