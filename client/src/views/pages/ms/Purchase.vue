<script setup>
import { onMounted, ref, computed } from 'vue';
import axios from 'axios';
import inputUtil from '@/components/common/SearchBox.vue';
import { useDateFormat, useNumberFormat } from '@/composables/useFormat';
import selectTable from '@/components/common/checkBoxTable.vue';
import { useIcon } from '@/composables/useIcon';
import { useAppToast } from '@/composables/useAppToast';

const { toast } = useAppToast();

const breadcrumbHome = { icon: useIcon('home'), to: '/' };
const mrpList = ref([]);
const planMasterList = ref([]);

const planMasterColumns = [
  { field: 'planType', label: '계획유형', style: 'width: 8rem' },
  { field: 'startDate', label: '생산시작일', style: 'width: 15rem' },
  { field: 'endDate', label: '생산종료일', style: 'width: 15rem' },
  { field: 'memo', label: '비고', style: 'width: 20rem' },
  { field: 'planNo', label: '계획번호', style: 'width: 15rem' },
  { field: 'empName', label: '담당자', style: 'width: 10rem' }
];

onMounted(async () => {
  try {
    const list = await axios.get('/api/mat/planMasterList');
    planMasterList.value = list.data.map((item) => ({
      id: item.plId,
      planType: item.planType,
      planNo: item.planNo,
      startDate: useDateFormat(item.startDate).value,
      endDate: useDateFormat(item.endDate).value,
      memo: item.memo,
      empName: item.empName
    }));
  } catch (error) {
    toast('생산계획 리스트 불러오기 실패:', error);
  }
});

const mrpColumns = [
  { field: 'matId', label: '자재코드', style: 'width: 15rem' },
  { field: 'matName', label: '자재명', style: 'width: 15rem' },
  { field: 'mrpQty', label: '소요수량', style: 'width: 15rem' },
  { field: 'unit', label: '단위', style: 'width: 15rem' },
  { field: 'leadTime', label: '리드타임', style: 'width: 15rem' }
];

// const planList = ref([]);
// const planColumns = [
//   { field: 'planDueDate', label: '생산예정일', style: 'width: 15rem' },
//   { field: 'prodId', label: '제품코드', style: 'width: 8rem' },
//   { field: 'prodName', label: '제품명', style: 'width: 20rem' },
//   { field: 'planQty', label: '생산수량', style: 'width: 10rem' },
//   { field: 'unit', label: '단위', style: 'width: 5rem' }
// ];
// onMounted(async () => {
//   try {
//     const list = await axios.get('/api/mat/planList');
//     planList.value = list.data.map((item) => ({
//       id: item.prodNo,
//       planId: item.prodNo,
//       planDueDate: useDateFormat(item.proDate).value,
//       prodId: item.prodId,
//       prodName: item.productVO.prodName,
//       planQty: item.proQty,
//       unit: item.productVO.unit
//     }));
//   } catch (error) {
//     toast('제품별 생산계획 리스트 불러오기 실패:', error);
//   }
// });
</script>

<template>
  <div class="container">
    <div class="p-4">
      <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    </div>
    <!--2분할-->
    <div class="flex flex-col md:flex-row gap-8">
      <div class="md:w-1/2 planList">
        <div class="card flex flex-col gap-4">
          <div class="flex items-center justify-between font-semibold text-m">
            <span>생산계획 목록</span>
            <btn color="secondary" icon="pi pi-file-excel" @click="calculatMrp">mrp산출</btn>
          </div>
          <Divider />
          <selectTable :columns="planMasterColumns" :data="planMasterList" :paginator="false" :showCheckbox="false" />
        </div>

        <div class="card flex flex-col gap-4">
          <div class="font-semibold text-m">MRP 합산자재 소요량</div>
          <Divider />
          <selectTable :columns="mrpColumns" :data="mrpList" :paginator="false" :showCheckbox="false" />
        </div>
      </div>

      <div class="md:w-1/2 header">
        <div class="card flex flex-col gap-4">
          <div class="font-semibold text-m mb-5">구매 등록</div>

          <Divider />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
