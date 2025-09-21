<script setup>
import { onMounted, ref } from 'vue';
import axios from 'axios';

</script>

<template>
    <div class = "container">
        <div class = "flex flex-col md:flex-row gap-8">
            <div class = "md:w-1/2 planList">
                <div class="card flex flex-col gap-4">
                    <div class="font-semibold text-xl">생산계획 선택</div>
                    <div class="flex flex-col gap-2">생산계획리스트 출력</div>
                </div>

                <div class="card flex flex-col gap-4">
                   <div class="font-semibold text-xl">MRP 합산자재 소요량</div>
                   <div class="flex flex-col gap-2">MRP산출결과 출력</div>
                </div>
            </div>


            <div class = "md:w-1/2 header">
                <div class="card flex flex-col gap-4">
                    <div class="font-semibold text-xl">구매등록 </div>
                    <div class="flex flex-col gap-2">구매등록 데이터 입력</div>
                </div>
            </div>
        </div>
    </div>



</template>

<style scoped>
</style>
