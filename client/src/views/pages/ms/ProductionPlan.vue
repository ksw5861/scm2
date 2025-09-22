<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import DTable from '@/components/common/DTable.vue';
import selectTable from '@/components/common/checkBoxTable.vue';
import btn from '@/components/common/Btn.vue';


const openModal = () => {
  console.log('모달 열기');
};

const productionPlans = ref([
    { id: '', empName: '', age: null, dept: '', unit: '', planDate: '' }
])

const detailColumns = [
  { field: 'prdCod', label: '제품코드', style: 'width: 15rem'},
  { field: 'prdName', label: '제품명', inputText: true, style: 'width: 30rem', onClick: openModal },
  { field: 'qty', label: '생산수량', inputNumber: true, style: 'width: 10rem' },
  { field: 'unit', label: '단위', style: 'width: 9rem' },
  { field: 'planDate', label: '생산예정일', datePicker: true, style: 'width: 20rem' }
];


</script>

<template>
  <div class="card flex flex-col gap-4">
    <div class="font-semibold text-xl">기간 단위 계획</div>
    <div class="flex flex-col gap-2">마스터정보</div>
  </div>

  <div class="card flex flex-col gap-4">
    <div class="flex justify-between items-center">
      <div class="font-semibold text-xl">제품 단위 계획</div>
      <div class="flex whitespace-nowrap items-center justify-end gap-2">
        <btn color="secondary" icon="pi pi-file-excel"> 행추가 </btn>
        <btn color="secondary" icon="pi pi-file-pdf"> 행삭제 </btn>
      </div>
    </div>
    <selectTable :columns="detailColumns" :data="productionPlans" :paginator="true" :rows="10" />
  </div>
</template>

<style scoped></style>
