<script setup>
import { ref, onMounted, computed, getCurrentWatcher } from 'vue';
import axios from 'axios';
import selectTable from '@/components/common/checkBoxTable.vue';
import { useAppToast } from '@/composables/useAppToast';
import { useRoute } from 'vue-router';
import { useIcon } from '@/composables/useIcon';
import { useDateFormat, useNumberFormat } from '@/composables/useFormat';
import { useUserStore } from '@/stores/user';
import SearchCard from '@/components/card/SearchCard.vue';


// Pinia Store
const userStore = useUserStore();
const empName = userStore.name;
const empId = userStore.code;
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
const productList = ref();
const selectedRows = ref();
const prodLotList = ref();
const selectedDeRow = ref();

//검색조건
const searchFilter = ref({
  prodName: '',
  inboundId: '',
});
// pagination
const page = ref({ page: 1, size: 10, totalElements: 0 });

const fetchMatList = async () => {
  const params = {
    page: page.value.page,
    size: page.value.size,
    ...searchFilter.value
  };
  console.log(params);
  try {
    const res = await axios.get('/api/stockByProd', { params });
    const { list, page: pageInfo } = res.data;

    productList.value = list.map((item) => ({
      id: item.prodId,
      prodId: item.prodId,
      prodName: item.prodName,
      currQty: item.totalRemainQty,
      unit: item.unit
    }));

    page.value.totalElements = pageInfo.totalElements;
  } catch (error) {
    toast('error', '자재 재고 현황', '재고 리스트 불러오기 실패:', '3000');
  }
};

const detailInfo = async () => {

  try {
    const list = await axios.get('/api/stockByProdLotList', { params: { prodId: selectedRows.value.id } });
    prodLotList.value = list.data.map((item) => ({
        lotNo: item.inboundId,
        prodName: item.prodName,
        currQty: item.remainQty,
        unit: item.unit,
        regDate: useDateFormat(item.inDate).value,
        expDate: useDateFormat(item.expDate).value,
        warehouse: item.whName
    }));
  } catch (error) {
    toast('error', '리스트 로드 실패', 'LOT 리스트 불러오기 실패', '3000');
    return { items: [] };
  }
};

const resetSearch = () => {
  searchFilter.value = {
    prodName: '',
    inboundId: '',
  };
  fetchMatList();
  selectedRows.value = null;
};

const onPage = (event) => {
  const startRow = event.page * event.rows + 1;
  const endRow = (event.page + 1) * event.rows;
  fetchMatList({ startRow, endRow });
};

onMounted(() => {
  fetchMatList();
});

const productStock = [
  { label: '제품코드', field: 'prodId' },
  { label: '제품명', field: 'prodName', sortable: true },
  { label: '현재재고', field: 'currQty', sortable: true },
  { label: '단위', field: 'unit' }
];

const prodLotColumns = [
  { field: 'lotNo', label: 'LOT번호', style: 'width: 15rem' },
  { field: 'prodName', label: '제품명', style: 'width: 15rem' },
  { field: 'currQty', label: '현재고', style: 'width: 10rem' },
  { field: 'unit', label: '단위', style: 'width: 10rem' },
  //{ field: 'regDate', label: '등록일', style: 'width: 12rem' },
  { field: 'expDate', label: '유통기한', style: 'width: 12rem' },
  { field: 'warehouse', label: '보관창고', style: 'width: 12rem' }
];
</script>

<template>
  <div class="container">
    <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    <!--검색영역-->
    <div class="card flex flex-col gap-4 mt-4">
      <SearchCard title="재고 조회" @search="fetchMatList" @reset="resetSearch">
        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
          <InputGroup>
            <InputGroupAddon><i :class="useIcon('box')" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchFilter.prodName" inputId="searchName" />
              <label for="searchMatName">제품명</label>
            </IftaLabel>
          </InputGroup>

          <InputGroup>
            <InputGroupAddon><i :class="useIcon('box')" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchFilter.inboundId" inputId="searchLot" />
              <label for="searchLotNo">LOT번호</label>
            </IftaLabel>
          </InputGroup>

        </div>
      </SearchCard>
    </div>
    <!--검색박스 end-->
    <!--테이블영역-->
    <div class="flex flex-col md:flex-row gap-8">
      <div class="md:w-1/2">
        <div class="card flex flex-col gap-4 h-full" style="height: 850px">
          <!-- h-full 고정 -->
          <div class="card flex flex-col gap-4">
            <div class="font-semibold text-xl flex items-center justify-between gap-4 h-10">
              <div class="flex items-center gap-4"><span :class="useIcon('list')"></span>제품 재고 목록</div>
            </div>
            <Divider />
            <selectTable v-model:selection="selectedRows" selectionMode="single" :columns="productStock" :data="productList" :paginator="true" :page="page" :showCheckbox="false" @page-change="onPage" @row-select="detailInfo" />
          </div>
        </div>
      </div>
      <!--하단우측-->
      <div class="md:w-1/2">
        <div class="card flex flex-col gap-4" style="height: 850px">
          <div class="flex items-center justify-between my-3">
            <div class="font-semibold text-xl flex items-center justify-between gap-4 h-10">
              <div class="flex items-center gap-4"><span :class="useIcon('openfolder')"></span>LOT 상세정보</div>
            </div>
          </div>
          <Divider />
          <selectTable v-model:selection="selectedDeRow" :selectionMode="'single'" :columns="prodLotColumns" :data="prodLotList" :paginator="false" :showCheckbox="false" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
