<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import DTable from '@/components/common/DTable.vue';
import selectTable from '@/components/common/checkBoxTable.vue';
import btn from '@/components/common/Btn.vue';

const openWarehouseeModal = () => {
  console.log('창고 모달 오픈');
};

const productionPlans = ref([
  { id: 1, product: '제품 A', quantity: 100, startDate: '2024-07-01', endDate: '2024-07-10', status: '진행중' },
  { id: 2, product: '제품 B', quantity: 200, startDate: '2024-07-05', endDate: '2024-07-15', status: '대기중' },
  { id: 3, product: '제품 C', quantity: 150, startDate: '2024-07-10', endDate: '2024-07-20', status: '완료' }
]);

const detailColumns = [
  { field: 'prdCode', header: '제품코드', headerStyle: 'width: 20rem', inputTextWM: true, onClick: openWarehouseeModal },
  { field: 'comm', header: '비고', inputText: true },
  { field: 'purch_qty', header: '수량', inputNumber: true },
  { field: 'purch_qty', header: '수량', inputNumber: true }
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
