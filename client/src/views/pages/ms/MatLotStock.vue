<script setup>
import { ref, onMounted, computed, getCurrentWatcher } from 'vue';
import axios from 'axios';
import btn from '@/components/common/Btn.vue';
import selectTable from '@/components/common/checkBoxTable.vue';
import SearchField from '@/components/common/SearchBox.vue';
import CommonModal from '@/components/common/Modal.vue';
import { useAppToast } from '@/composables/useAppToast';
import { useRoute } from 'vue-router';
import { useIcon } from '@/composables/useIcon';
import { useDateFormat, useNumberFormat } from '@/composables/useFormat';
1;

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
//테이블
const matStockList = ref();
const selectedRows = ref();
const matLotList = ref();
//공통코드
const codeMap = ref();
const statusOptions = ref([]);
//검색조건
const searchFilter = ref({
  materialId: '',
  materialName: '',
  lotNo: '',
  lotStatus: ''
});
// pagination
const page = ref({ page: 1, size: 10, totalElements: 0 });
// 모달 열기
const openPlanModal = () => {
  showMat.value = true;
};

// 모달 닫기
const closePlanModal = () => {
  showMat.value = false;
};

const fetchMatList = async () => {
  const pageParam = { page: page.value.page, size: page.value.size };

  console.log(pageParam);
  try {
    const res = await axios.get('/api/mat/matStockList', { params: pageParam });
    const { list, page: pageInfo } = res.data;

    matStockList.value = list.map((item) => ({
      id: item.matId,
      matId: item.matId,
      matName: item.materialVO.matName,
      currWeight: item.currWeight,
      unit: item.materialVO.unit,
      currQty: item.currQty,
      qtyUnit: item.materialVO.stockUnit
    }));

    page.value.totalElements = pageInfo.totalElements;
  } catch (error) {
    toast('error', '자재 재고 현황', '재고 리스트 불러오기 실패:', '3000');
  }
};

const detailInfo = async () => {
  try {
    const list = await axios.get('/api/mat/matLotList', { params: { matId: selectedRows.value.id } });
    matLotList.value = list.data.map((item) => ({
      regDate: useDateFormat(item.regDate).value,
      lotNo: item.lotNo,
      matName: item.materialVO.matName,
      currWeight: item.currWeight,
      unit: item.materialVO.unit,
      currQty: item.currQty,
      stockUnit: item.materialVO.stockUnit,
      expDate: useDateFormat(item.expDate).value,
      status: codeMap.value[item.lotStatus]
    }));
  } catch (error) {
    toast('error', '리스트 로드 실패', 'LOT 리스트 불러오기 실패', '3000');
    return { items: [] };
  }
};

const loadStatusCodes = async () => {
  try {
    const res = await axios.get('/api/mat/status/lcnd');
    codeMap.value = res.data.reduce((acc, cur) => {
      acc[cur.codeId] = cur.codeName;
      return acc;
    }, {});

    statusOptions.value = res.data.map((item) => ({
      name: item.codeName, // 화면 표시용
      value: item.codeId // 실제 값
    }));
  } catch (err) {
    toast('error', '공통코드 로드 실패', '상태명 불러오기 실패', '3000');
  }
};

const onPage = (event) => {
  const startRow = event.page * event.rows + 1;
  const endRow = (event.page + 1) * event.rows;
  fetchMatList({ startRow, endRow });
};

onMounted(() => {
  fetchMatList();
  loadStatusCodes();
});

const matStock = [
  { label: '자재코드', field: 'matId' },
  { label: '자재명', field: 'matName', sortable: true },
  { label: '현재재고', field: 'currWeight', sortable: true },
  { label: '단위', field: 'unit' },
  { label: '환산재고', field: 'currQty', sortable: true },
  { label: '환산단위', field: 'qtyUnit' }
];

const matLotColumns = [
  //{ field: 'regDate', label: '등록일', style: 'width: 12rem' },
  { field: 'lotNo', label: 'LOT번호', style: 'width: 15rem' },
  { field: 'currWeight', label: '현재재고', style: 'width: 10rem' },
  { field: 'unit', label: '단위', style: 'width: 8rem' },
  { field: 'currQty', label: '환산재고', style: 'width: 10rem' },
  { field: 'stockUnit', label: '환산단위', style: 'width: 10rem' },
  { field: 'expDate', label: '유통기한', style: 'width: 12rem' },
  { field: 'status', label: '상태', style: 'width: 8rem' }
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
        <SearchField type="textIcon" label="자재코드" v-model="materialId" />
        <SearchField type="text" label="자재명" v-model="materialName" />
        <SearchField type="text" label="LOT번호" v-model="lotNo" />
        <SearchField type="dropDown" label="LOT상태" v-model="lotStatus" :options="statusOptions" />
        <!-- <SearchField type="date" label="등록일" v-model="registerDate" /> -->

        <!-- 버튼 영역 -->
        <div class="flex flex-wrap items-center gap-2">
          <btn color="secondary" icon="pi pi-undo" label="초기화" />
          <btn color="contrast" icon="pi pi-search" label="조회" />
        </div>
      </div>
    </div>
    <!--검색박스 end-->
    <!--테이블영역-->
    <div class="flex flex-col md:flex-row gap-8">
      <div class="md:w-1/2">
        <div class="card flex flex-col gap-4 h-full">
          <!-- h-full 고정 -->
          <div class="card flex flex-col gap-4">
            <div class="font-semibold text-m">자재별 재고 목록</div>
            <Divider />
            <selectTable v-model:selection="selectedRows" selectionMode="single" :columns="matStock" :data="matStockList" :paginator="true" :page="page" :showCheckbox="false" @page-change="onPage" @row-select="detailInfo" />
          </div>
        </div>
      </div>
      <!--하단우측-->
      <div class="md:w-1/2">
        <div class="card flex flex-col gap-4">
          <!-- 버튼 + 제목을 같은 행에 배치 -->
          <div class="flex items-center justify-between my-3">
            <!-- 왼쪽: 제목 -->
            <div class="font-semibold text-m">LOT 상세정보</div>
          </div>
          <Divider />
          <selectTable v-model:selection="selectedDeRow" :selectionMode="'single'" :columns="matLotColumns" :data="matLotList" :paginator="false" :showCheckbox="false" />
        </div>
      </div>
    </div>
  </div>
  <!-- 자재별 LOT현황 모달 -->
  <!-- <CommonModal :visible="showMatLot" title="자재별 LOT 현황" :columns="matLotColumns" :fetchData="matLotList" @close="closePlanModal" /> -->
</template>

<style scoped></style>
