<script setup>
import { ref, onMounted, computed, getCurrentWatcher } from 'vue';
import axios from 'axios';
import selectTable from '@/components/common/checkBoxTable.vue';
import SearchField from '@/components/common/SearchBox.vue';
import { useAppToast } from '@/composables/useAppToast';
import { useRoute } from 'vue-router';
import { useIcon } from '@/composables/useIcon';
import { useDateFormat, useNumberFormat } from '@/composables/useFormat';
import { useUserStore } from '@/stores/user';
import SearchCard from '@/components/card/SearchCard.vue';
import Select from 'primevue/select';
import Breadcrumb from 'primevue/breadcrumb';
import Divider from 'primevue/divider';
import Btn from '@/components/common/Btn.vue';

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
const matStockList = ref();
const selectedRows = ref();
const matLotList = ref();
const selectedLotRow = ref(null);
const adjustHistoryList = ref([]);
//공통코드
const codeMap = ref();
const statusOptions = ref([]);
//검색조건
const searchFilter = ref({
  materialId: '',
  materialName: '',
  lotNo: '',
  type: '',
  lotStatus: ''
});

//조정폼
const adjustForm = ref({
  lotNo: '',
  adjustWeight: 0,
  unit: '',
  adjustReason: '',
  lotId: ''
});

// pagination
const page = ref({ page: 1, size: 10, totalElements: 0 });

const fetchMatList = async () => {
  const params = {
    page: page.value.page,
    size: page.value.size,
    ...searchFilter.value
  };
  console.log('요청 params:', params);
  try {
    const res = await axios.get('/api/mat/matStockList', { params });
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
  selectedLotRow.value = null;

  adjustForm.value = {
    lotNo: '',
    adjustWeight: 0,
    unit: '',
    adjustReason: '',
    lotId: '',
    type: ''
  };

  adjustHistoryList.value = [];

  try {
    //LOT 상세
    const list = await axios.get('/api/mat/matLotList', { params: { matId: selectedRows.value.matId } });
    matLotList.value = list.data.map((item) => ({
      id: item.lotId,
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

//조정이력History
const onLotSelect = async (lotRow) => {
  if (!lotRow) return;

  adjustForm.value.lotNo = lotRow.lotNo;
  adjustForm.value.unit = lotRow.unit;
  adjustForm.value.lotId = lotRow.id || lotRow.id || '';

  try {
    const res = await axios.get('/api/mat/adjustHistory', { params: { lotId: lotRow.id } });
    adjustHistoryList.value = res.data.map((item) => ({
      adjDate: useDateFormat(item.reDate).value,
      adjWeight: item.weight,
      inOut: item.inOut,
      reason: item.reson,
      name: item.name,
      beforeWeight: item.beforeWeight,
      afterWeight: item.afterWeight,
      unit: lotRow.unit
    }));
  } catch (error) {
    toast('error', '조정이력 불러오기 실패', '이력조회 실패', '3000');
  }
};

// 조정등록 버튼 클릭
const submitAdjustStock = async () => {
  if (!adjustForm.value.lotId) {
    return toast('warn', '조정등록', 'LOT를 선택해 주세요', 2500);
  }
  if (!adjustForm.value.adjustWeight || adjustForm.value.adjustWeight <= 0) {
    return toast('warn', '조정등록', '조정중량(양수)을 입력해 주세요', 2500);
  }
  if (!adjustForm.value.type) {
    return toast('warn', '조정등록', '증감 유형(IN/OUT)을 선택해 주세요', 2500);
  }

  const payload = {
    lotId: adjustForm.value.lotId,
    weight: adjustForm.value.adjustWeight, // 양수
    inOut: adjustForm.value.type.toUpperCase(), // 'IN' | 'OUT'
    reason: adjustForm.value.adjustReason,
    name: empName
  };

  try {
    const { data } = await axios.post('/api/mat/adjustStock', payload);
    toast('success', '조정등록', `등록 성공 (ID: ${data.adjStockId})`, 2500);

    // 이력 새로고침 (현재 선택 LOT 기준)
    await onLotSelect(selectedLotRow.value);
    await fetchMatList();
  } catch (e) {
    toast('error', '조정등록 실패', e?.response?.data?.message || '서버 오류', 3000);
  }
};

const loadStatusCodes = async () => {
  try {
    const res = await axios.get('/api/mat/status/searchStock');
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

const resetSearch = () => {
  searchFilter.value = {
    materialId: '',
    materialName: '',
    lotNo: '',
    lotStatus: ''
  };
  fetchMatList();
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
  { field: 'lotNo', label: 'LOT번호', style: 'width: 15rem' },
  { field: 'currWeight', label: '현재재고', style: 'width: 10rem' },
  { field: 'unit', label: '단위', style: 'width: 8rem' },
  { field: 'currQty', label: '환산재고', style: 'width: 10rem' },
  { field: 'stockUnit', label: '환산단위', style: 'width: 10rem' },
  { field: 'expDate', label: '유통기한', style: 'width: 12rem' },
  { field: 'status', label: '상태', style: 'width: 8rem' }
];

const adjustHistoryColumns = [
  { field: 'adjDate', label: '조정일시', style: 'width:10rem' },
  { field: 'inOut', label: '유형', style: 'width: 8rem' },
  { field: 'adjWeight', label: '조정중량', style: 'width: 10rem' },
  { field: 'unit', label: '단위', style: 'width: 8rem' },
  { field: 'reason', label: '사유', style: 'width: 10rem' },
  { field: 'name', label: '담당자', style: 'width: 10rem' },
  { field: 'beforeWeight', label: '조정 전', style: 'width: 10rem' },
  { field: 'afterWeight', label: '조정 후', style: 'width: 10rem' }
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
              <InputText v-model="searchFilter.materialId" inputId="searchMa" />
              <label for="searchMatName">자재명</label>
            </IftaLabel>
          </InputGroup>

          <InputGroup>
            <InputGroupAddon><i :class="useIcon('box')" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchFilter.materialId" inputId="searchMa" />
              <label for="searchLotNo">LOT번호</label>
            </IftaLabel>
          </InputGroup>

          <div class="flex flex-col w-full">
            <InputGroup>
              <InputGroupAddon><i :class="useIcon('box')" /></InputGroupAddon>
              <Select v-model="searchFilter.lotStatus" :options="statusOptions" optionLabel="name" optionValue="value" placeholder="LOT 상태" class="w-full h-[48px] text-base" />
            </InputGroup>
          </div>
        </div>
      </SearchCard>
    </div>
    <!--검색박스 end-->
    <!--테이블영역-->
    <div class="flex flex-col md:flex-row gap-8">
      <div class="md:w-1/2">
        <div class="card flex flex-col gap-4 h-full">
          <!-- h-full 고정 -->
          <div class="card flex flex-col gap-4">
            <div class="font-semibold text-m">목록</div>
            <Divider />
            <selectTable v-model:selection="selectedRows" selectionMode="single" :columns="matStock" :data="matStockList" :paginator="true" :page="page" :showCheckbox="false" @page-change="onPage" @row-select="detailInfo" />
          </div>
        </div>
      </div>
      <!--오른쪽-->
      <div class="md:w-1/2 h-full">
        <div class="card flex flex-col gap-4 h-full">
          <!-- 상단 헤더 -->
          <div class="flex items-center justify-between my-3">
            <div class="font-semibold text-m">상세정보</div>
            <div class="flex gap-2">
              <btn color="info" icon="check" label="재고 조정" class="whitespace-nowrap" outlined @click="submitAdjustStock" />
            </div>
          </div>
          <Divider />
          <!-- 메인 콘텐츠 -->
          <div class="flex flex-col gap-4 h-[600px]">
            <!-- LOT 상세 -->
            <div class="flex-[4] overflow-auto">
              <selectTable v-model:selection="selectedLotRow" :selectionMode="'single'" :columns="matLotColumns" :data="matLotList" :paginator="false" :showCheckbox="false" @row-select="onLotSelect" />
            </div>
            <!-- 조정 입력 -->
            <div class="flex-[2] card !p-4">
              <div class="font-semibold text-m mb-3">조정입력</div>
              <Divider class="my-2" />
              <div class="flex items-end mb-3">
                <div class="flex flex-1 items-end gap-1">
                  <div class="w-[6rem]">
                    <SearchField
                      type="dropDown"
                      label="유형"
                      v-model="adjustForm.type"
                      :options="[
                        { name: '감소', value: 'out' },
                        { name: '증감', value: 'in' }
                      ]"
                    />
                  </div>
                  <div class="flex-1">
                    <SearchField type="number" label="조정중량" v-model="adjustForm.adjustWeight" />
                  </div>
                  <!-- 단위 -->
                  <div class="w-[5rem]">
                    <SearchField type="readOnly" label="단위" v-model="adjustForm.unit" />
                  </div>
                </div>
                <!-- 조정사유 -->
                <div class="flex-1 ml-3">
                  <SearchField
                    type="dropDown"
                    label="조정사유"
                    v-model="adjustForm.adjustReason"
                    :options="[
                      { name: '파손', value: 'adjrs1' },
                      { name: '습기', value: 'adjrs2' },
                      { name: '감모', value: 'adjrs3' },
                      { name: '기타', value: 'adjrs4' }
                    ]"
                  />
                </div>
              </div>
              <!-- 두 번째 줄: 담당자 / LOT번호 -->
              <div class="grid grid-cols-2 gap-4">
                <SearchField type="readOnly" label="담당자" v-model="empName" />
                <SearchField type="readOnly" label="선택 LOT번호" v-model="adjustForm.lotNo" />
              </div>
            </div>
            <!-- 조정이력 -->
            <div class="flex-[4] card !p-0 overflow-auto">
              <div class="font-semibold text-m p-4 pb-2">조정이력</div>
              <Divider class="m-0" />
              <selectTable :columns="adjustHistoryColumns" :data="adjustHistoryList" :paginator="false" :showCheckbox="false" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
