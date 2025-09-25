<script setup>
import { onMounted, ref, computed } from 'vue';
import axios from 'axios';
import inputUtil from '@/components/common/SearchBox.vue';
import { useDateFormat, useNumberFormat } from '@/composables/useFormat';
import selectTable from '@/components/common/checkBoxTable.vue';
import { useIcon } from '@/composables/useIcon';

const breadcrumbHome = { icon: useIcon('home'), to: '/' };

const planList = ref([]);

const planColumns = [
  { field: 'planId', label: '계획번호', style: 'width: 15rem' },
  { field: 'planDueDate', label: '생산예정일', style: 'width: 15rem' },
  { field: 'prodId', label: '제품코드', style: 'width: 15rem' },
  { field: 'prodName', label: '제품명', style: 'width: 15rem' },
  { field: 'planQty', label: '생산수량', style: 'width: 10rem' },
  { field: 'unit', label: '단위', style: 'width: 8rem' }
];

const mrpColumns = [
  { field: 'matId', label: '자재코드', style: 'width: 15rem' },
  { field: 'matName', label: '자재명', style: 'width: 15rem' },
  { field: 'mrpQty', label: '소요수량', style: 'width: 15rem' },
  { field: 'unit', label: '단위', style: 'width: 15rem' },
  { field: 'leadTime', label: '리드타임', style: 'width: 15rem' }
];

const mrpList = [
  { matId: 'MAT001', matName: 'PET병 500ml', mrpQty: 2000, unit: 'EA', leadTime: 3 },
  { matId: 'MAT002', matName: '라벨 스티커', mrpQty: 2000, unit: 'EA', leadTime: 2 },
  { matId: 'MAT003', matName: '뚜껑 (블루)', mrpQty: 2000, unit: 'EA', leadTime: 4 }
];

onMounted(async () =>{
//     select *
// from product_plan_detail ppd
// join product po
// on ppd.prod_id = po.prod_id
// where ppd.mpr_status = 'pp0';
});

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
            <span>생산계획 선택</span>
            <btn color="secondary" icon="pi pi-file-excel" @click="addRow">mrp산출</btn>
          </div>
          <Divider />
          <selectTable :columns="planColumns" :data="planList" :paginator="false" :showCheckbox="true" />
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
