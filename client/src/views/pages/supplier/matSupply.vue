<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import btn from '@/components/common/Btn.vue';
import selectTable from '@/components/common/checkBoxTable.vue';
import SearchField from '@/components/common/SearchBox.vue';
import { useAppToast } from '@/composables/useAppToast';
import { useRoute } from 'vue-router';
import { useIcon } from '@/composables/useIcon';
import { useDateFormat, useNumberFormat } from '@/composables/useFormat';
import { useUserStore } from '@/stores/user';
import SearchCard from '@/components/card/SearchCard.vue';
import Select from 'primevue/select';
import DatePicker from 'primevue/datepicker';

// Pinia Store
const userStore = useUserStore();
// const vendorId = userStore.code;
const vendorId = ref('V800');
const vanEmpName = ref('홍길동');

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

const dateRange = ref({ start: null, end: null }); // 초기값을 객체로
const materialName = ref();
const statusList = ref();
const matOutData = ref();
const selectedRows = ref();
const page = ref({ page: 1, size: 10, totalElements: 0 });

//검색필드
const searchFilter = ref({
  stardDate: '',
  endDate: '',
  vendor: '',
//   lotStatus: ''
});

const pageLoad = async () => {
  try {
    const list = await axios.get(`/api/supplier/releaseList/${vendorId.value}`);
    console.log(list);

    matOutData.value = list.data.map((item) => ({
      id: item.purId,
      dueDate: useDateFormat(item.dueDate).value,
      purNo: item.purNo,
      matId: item.matId,
      matName: item.materialVO.matName,
      buyerName: item.empName,
      orderQty: item.reqQty,
      unit: item.materialVO.stockUnit,
      outQty: '',
      expectDate: '',
      outTotalQty: item.outTotalQty,
      restQty: item.reqQty - item.outTotalQty,
      releaseStatus: item.purMatStatus,
      approveDate: useDateFormat(item.purStatusLogVO.reDate).value
    }));
  } catch (error) {
    toast('error', '리스트 로드 실패', '리스트 불러오기 실패:', '3000');
  }
};

const onPage = (event) => {
  const startRow = event.page * event.rows + 1;
  const endRow = (event.page + 1) * event.rows;
  pageLoad({ startRow, endRow });
};

onMounted(() => {
  pageLoad();
});

const approvedShip = async () => {
  //선택행 없으면 출고처리 선택 안내
  if (!selectedRows.value) {
    toast('info', '유효성 검사', '출고 지시 제품을 선택해 주세요.', '3000');
    return;
  }
  // //출고수량 null
  // if (!matOutData.value.orderQty || matOutData.value.orderQty == 0) {
  // toast('info', '유효성 검사', '출고 수량을 입력해 주세요.', '3000');
  // return;
  // }

  // if (!matOutData.value.expectDate) {
  // toast('info', '유효성 검사', '출고 예정일을 입력해 주세요.', '3000');
  // return;
  // }

  // //잔여수량대비 유효성검사
  // if (matOutData.value.outQty > matOutData.value.orderQty) {
  // toast('info', '유효성 검사', '주문수량 대비 출고수량이 많습니다.', '3000');
  // return;
  // }

  const list = JSON.parse(JSON.stringify(selectedRows.value));

  const payload = list.map((row) => ({
    purId: row.id,
    matId: row.matId,
    purNo: row.purNo,
    purStatusLogVO: { logSupOutQty: row.outQty }, //이걸 백 로그VO에 넣기위해!
    expectDate: row.expectDate,
    vendorId: vendorId.value //이렇게 넣으면 행 마다 다 들어감.
  }));

  console.log(payload);

  try {
    const res = await axios.post('/api/supplier/shipMaterial', payload);
    toast('info', '등록 성공', '출고등록  성공:', '3000');
    selectedRows.value = [];
    await pageLoad();
  } catch (error) {
    toast('error', '등록 실패', '출고등록  실패:', '3000');
  }
};

const matOutColumns = [
  { label: '납기요청일', field: 'dueDate' },
  { label: '주문번호', field: 'purNo' },
  { label: '자재코드', field: 'matId' },
  { label: '자재명', field: 'matName' },
  { label: '구매처 담당자', field: 'buyerName' },
  { label: '수량', field: 'orderQty' },
  { label: '단위', field: 'unit' },
  { label: '잔여수량', field: 'restQty' },
  { label: '출고수량', field: 'outQty', inputText: true },
  { label: '출고예정일', field: 'expectDate', datePicker: true },
  { label: '누적출고수량', field: 'outTotalQty' }
  //{ label: '상태', field: 'releaseStatus' },
  //{ label: '출고승인일', field: 'approveDate' }
];
</script>

<template>
  <div class="container">
    <div class="p-4">
      <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    </div>
    <div class="card flex flex-col gap-4">
        <SearchCard title="입고 조회" @search="fetchMatList" @reset="resetSearch">
            <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">

                <InputGroup>
                    <InputGroupAddon><i :class="useIcon('box')" /></InputGroupAddon>
                    <IftaLabel>
                        <DatePicker v-model="searchFilter.sartDate" inputId="searchMatId" />
                        <label for="searchStart">시작일</label>
                    </IftaLabel>
                </InputGroup>

                <InputGroup>
                    <InputGroupAddon><i :class="useIcon('box')" /></InputGroupAddon>
                    <IftaLabel>
                        <DatePicker v-model="searchFilter.endDate" inputId="searchMa" />
                        <label for="searchEnd">종료일</label>
                    </IftaLabel>
                </InputGroup>

                <InputGroup>
                    <InputGroupAddon><i :class="useIcon('box')" /></InputGroupAddon>
                    <IftaLabel>
                        <InputText v-model="searchFilter.vendor" inputId="searchMa" />
                        <label for="searchVendor">자재명</label>
                    </IftaLabel>
                </InputGroup>

                <div class="flex flex-col w-full">
                    <InputGroup>
                        <InputGroupAddon><i :class="useIcon('box')" /></InputGroupAddon>
                        <Select
                        v-model="searchFilter.lotStatus"
                        :options="statusOptions"
                        optionLabel="name"
                        optionValue="value"
                        placeholder="출고 상태"
                        class="w-full h-[48px] text-base"/>
                    </InputGroup>
                </div>
            </div>
        </SearchCard>
    </div>
    <!--테이블영역--><!--테이블영역-->
    <div class="card flex flex-col gap-4">
      <div class="my-3 flex flex-wrap items-center justify-end gap-2">
        <btn color="info" icon="pi pi-file-pdf" @click="approvedShip" label="승인" />
      </div>
      <div class="font-semibold text-xl mb-5">출고대기 목록</div>
      <selectTable v-model:selection="selectedRows" :columns="matOutColumns" :data="matOutData" :paginator="true" :rows="15" :page="page" @page-change="onPage" />
    </div>
  </div>
</template>
