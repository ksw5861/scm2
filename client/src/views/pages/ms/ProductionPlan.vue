<script setup>
import { ref, onMounted, } from 'vue';
import axios from 'axios';

const productionPlans = ref([
  { id: 1, product: '제품 A', quantity: 100, startDate: '2024-07-01', endDate: '2024-07-10', status: '진행중' },
  { id: 2, product: '제품 B', quantity: 200, startDate: '2024-07-05', endDate: '2024-07-15', status: '대기중' },
  { id: 3, product: '제품 C', quantity: 150, startDate: '2024-07-10', endDate: '2024-07-20', status: '완료' },
]);

</script>

<template>
    <fluid>
        <div class="card flex flex-col gap-4">
            <div class="font-semibold text-xl">기간 단위 계획 </div>
            <div class="flex flex-col gap-2">마스터정보</div>
        </div>

        <div class="card flex flex-col gap-4">
            <div class="font-semibold text-xl">제품 단위 계획</div>
             <div class="flex flex-col gap-2">세부정보테이블</div>
        </div>
    </fluid>
</template>

<style scoped>
</style>
