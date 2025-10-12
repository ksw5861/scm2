<script setup>
import { ref, onMounted, computed, getCurrentWatcher } from 'vue';
import axios from 'axios';
import btn from '@/components/common/Btn.vue';
import selectTable from '@/components/common/checkBoxTable.vue';
import SearchField from '@/components/common/SearchBox.vue';
import CommonModal from '@/components/common/Modal.vue'
import { useAppToast } from '@/composables/useAppToast';
import { useRoute } from 'vue-router';
import { useIcon } from '@/composables/useIcon';
import { useDateFormat, useNumberFormat } from '@/composables/useFormat';1

const route = useRoute();
const { toast } = useAppToast();

// breadcrumb
const breadcrumbHome = { icon: useIcon('home'), to: '/' };
const breadcrumbItems = computed(() => {
  const matched = route.matched.filter((r) => r.meta);
  if (!matched.length) return [];
  const current = matched[matched.length - 1];
  const parentLabel = current.meta?.breadcrumb?.parent || '주문 조회';
  const currentLabel = current.name || '';
  return [{ label: parentLabel }, { label: currentLabel, to: route.fullPath }];
});

const matStockList = ref();
const selectedRows = ref();
const showMatLot = ref(false);
// 모달 열기
const openPlanModal = (row) => {
  selectedRows.value = row;
  showMatLot.value = true;
};

// 모달 닫기
const closePlanModal = () => {
  showMatLot.value = false;
};

const matLotList = async () => {
  try {
    const res = await axios.get('/api/mat/matLotList', { params: {matId : selectedRows.value.id} })
    console.log(selectedRows.value.id)
    console.log(res)
    return {
      items: res.data.map((item) => ({
        ...item,
        lotNo: item.lotNo,
        matName: item.materialVO.matName,
        currWeight: item.currWeight,
        unit: item.materialVO.unit,
        currQty: item.currQty,
        stockUnit: item.materialVO.stockUnit
      }))
    }
  } catch (error) {
    toast('error', '리스트 로드 실패', 'LOT 리스트 불러오기 실패', '3000')
    return { items: [] }
  }
}

const pageLoad = async () => {
  try {
    const list = await axios.get('/api/mat/matStockList');
    console.log(list)
    matStockList.value = list.data.map((item) => ({
      id: item.matId,
      matId: item.matId,
      matName: item.materialVO.matName,
      currWeight: item.currWeight,
      unit: item.materialVO.unit,
      currQty: item.currQty,
      qtyUnit: item.materialVO.stockUnit
    }));
  } catch (error) {
    toast('error', '자재 재고 현황', '재고 리스트 불러오기 실패:', '3000');
  }
};

onMounted(() => {
  pageLoad();
});

const matStock = [
  { label: '자재코드', field: 'matId' },
  { label: '자재명', field: 'matName', sortable: true },
  { label: '현재재고', field: 'currWeight', sortable: true },
  { label: '단위', field: 'unit' },
  { label: '환산재고', field: 'currQty', sortable: true },
  { label: '환산단위', field: 'qtyUnit' }
];

//모달컬럼
const matLotColumns = [
  { field: 'lotNo', label: 'LOT번호', style: 'width: 10rem' },
  { field: 'matName', label: '자재명', style: 'width: 8rem' },
  { field: 'currWeight', label: '현재재고', style: 'width: 10rem' },
  { field: 'unit', label: '단위', style: 'width: 10rem' },
  { field: 'currQty', label: '환산재고', style: 'width: 10rem' },
  { field: 'stockUnit', label: '환산단위', style: 'width: 20rem' }
];
</script>

<template>
  <div class="container">
    <div class="p-4">
      <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    </div>
    <div class="card flex flex-col gap-4">
      <div class="font-semibold text-xl">재고 조회</div>
      <Divider />
      <!--search BOX 영역-->
      <div class="flex flex-col gap-4 md:flex-row md:items-end md:gap-6 mt-5 mb-10">
        <SearchField type="dateRange" label="요청일자" v-model="dateRange" />
        <SearchField type="textIcon" label="자재명" v-model="materialName" />
        <SearchField type="date" label="등록일" v-model="registerDate" />
        <SearchField
          type="checkbox"
          label="상태"
          v-model="statusList"
          :options="[
            { label: '대기', value: 'WAIT' },
            { label: '진행중', value: 'PROGRESS' },
            { label: '완료', value: 'DONE' },
            { label: '취소', value: 'CANCEL' }
          ]"
        />

        <!-- 버튼 영역 -->
        <div class="flex flex-wrap items-center gap-2">
          <btn color="secondary" icon="pi pi-undo" label="초기화" />
          <btn color="contrast" icon="pi pi-search" label="조회" />
        </div>
      </div>
    </div>
    <!--검색박스 end-->

    <div class="card flex flex-col gap-4">
      <div class="font-semibold text-xl mb-5">재고 현황</div>
      <selectTable v-model:selection="selectedRows"  selectionMode="single" :columns="matStock" :data="matStockList" :paginator="true" :rows="15" :showCheckbox="false"  @row-select="openPlanModal"/>
    </div>
  </div>
  <!-- 자재별 LOT현황 모달 -->
    <CommonModal
      :visible="showMatLot"
      title="자재별 LOT 현황"
      :columns="matLotColumns"
      :fetchData="matLotList"
      @close="closePlanModal"
    />
</template>

<style scoped></style>
