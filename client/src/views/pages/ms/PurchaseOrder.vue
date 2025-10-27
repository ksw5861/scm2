<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import btn from '@/components/common/Btn.vue';
import selectTable from '@/components/common/checkBoxTable.vue';
import { useAppToast } from '@/composables/useAppToast';
import { useRoute } from 'vue-router';
import { useIcon } from '@/composables/useIcon';
import { useDateFormat, useNumberFormat } from '@/composables/useFormat';
import { useUserStore } from '@/stores/user';
import SearchCard from '@/components/card/SearchCard.vue';
import DatePicker from 'primevue/datepicker';
//출고승인화면

// Pinia Store
// (userStore.name)이름
// (userStore.code)코드 - 계정기준으로
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

const purchaseOrderData = ref([]);
const selectedRows = ref([]);
const page = ref({ page: 1, size: 10, totalElements: 0 });
//공통코드
const codeMap = ref();
const statusOptions = ref([]);

//검색필드
const searchFilter = ref({
  startDate: '',
  endDate: '',
  matName: '',
  status: ''
});

//datePicker날짜변환
const formatDate = (date) => {
  if (!date) return '';
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

//주문목록(페이지로드시)
const fetchList = async () => {
  //검색필터 기간 유효성검사
  if (searchFilter.value.startDate && searchFilter.value.endDate) {
    if (new Date(searchFilter.value.startDate) > new Date(searchFilter.value.endDate)) {
      toast('warn', '기간 오류', '시작일은 종료일보다 이전이어야 합니다.', '3000');
      return;
    }
  }

  const params = {
    startDate: formatDate(searchFilter.value.startDate),
    endDate: formatDate(searchFilter.value.endDate),
    matName: searchFilter.value.matName,
    status: searchFilter.value.status
  };

  try {
    const list = await axios.get('/api/mgetPurchaseOrderList', { params });
    console.log(list);
    purchaseOrderData.value = list.data.map((item) => ({
      id: item.purId,
      orderDate: useDateFormat(item.regDate).value,
      orderNo: item.purNo,
      matId: item.matId,
      matName: item.materialVO.matName,
      reqQty: item.reqQty,
      unit: item.materialVO.stockUnit,
      dueDate: useDateFormat(item.dueDate).value,
      companyName: item.vendorVO.companyName,
      status: codeMap.value[item.purStatusLogVO.logPurMatStatus],
      epmName: item.empName,
      venName: item.purStatusLogVO.logName,
      reDate: useDateFormat(item.purStatusLogVO.reDate).value,
      resonComm: item.purStatusLogVO.logResonComm
    }));

    //page.value.totalElements = matOrderData.value.length;
  } catch (error) {
    toast('error', '리스트 로드 실패', '주문 리스트 불러오기 실패:', '3000');
  }
};

//발주취소
const cancel = async () => {
  const list = JSON.parse(JSON.stringify(selectedRows.value));
  const idList = list.map((row) => row.id);
  const checkStatus = list.map((row) => row.status);

  //유효성 검사
  for (const status of checkStatus) {
    if (status !== '발주등록') {
      toast('warn', '취소 불가', '발주등록상태 외 주문건은 취소가 불가능합니다.', '3000');
      return;
    }
  }

  if (!confirm('등록된 발주를 취소하시겠습니까?')) {
    return;
  }

  try {
    //취소담당자, purId,
    await axios.post('/api/mcancel', { purId: idList, empName: empName });
    toast('success', '취소 성공', '발주 취소 성공:', '3000');
    fetchList();
  } catch (err) {
    toast('error', '취소 실패', '발주 취소 실패:', '3000');
  }
};

const onPage = (event) => {
  const startRow = event.page * event.rows + 1;
  const endRow = (event.page + 1) * event.rows;

  fetchList({
    startDate: formatDate(searchFilter.value.startDate),
    endDate: formatDate(searchFilter.value.endDate),
    matName: searchFilter.value.matName,
    status: searchFilter.value.status,
    startRow,
    endRow
  });
};

const loadStatusCodes = async () => {
  try {
    const res = await axios.get('/api/mstatus/purResult');
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
    stardDate: '',
    endDate: '',
    matName: '',
    status: ''
  };
  fetchList();
};

onMounted(() => {
  fetchList();
  loadStatusCodes();
});

const matOrderColumns = [
  { label: '주문일자', field: 'orderDate', sortable: true },
  { label: '주문번호', field: 'orderNo' },
  { label: '자재코드', field: 'matId' },
  { label: '자재명', field: 'matName', sortable: true },
  { label: '주문수량', field: 'reqQty', style: 'text-align: right', sortable: true },
  { label: '단위', field: 'unit' },
  { label: '납기요청일', field: 'dueDate', sortable: true },
  { label: '공급처', field: 'companyName', sortable: true },
  { label: '상태', field: 'status' },
  { label: '담당자', field: 'epmName', style: 'width: 5rem' },
  { label: '공급처 담당자', field: 'venName', style: 'width: 8rem' },
  { label: '처리일자', field: 'reDate', sortable: true },
  { label: '비고', field: 'resonComm', style: 'width: 12rem' }
];
</script>

<template>
  <div class="container">
    <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    <!--검색영역-->
    <div class="card flex flex-col gap-4 mt-4">
      <SearchCard title="발주 검색" @search="fetchList" @reset="resetSearch">
        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
          <InputGroup>
            <InputGroupAddon><i :class="useIcon('calendar')" /></InputGroupAddon>
            <IftaLabel>
              <DatePicker v-model="searchFilter.startDate" inputId="searcStart" />
              <label for="searchStart">시작일</label>
            </IftaLabel>
          </InputGroup>

          <InputGroup>
            <InputGroupAddon><i :class="useIcon('calendar')" /></InputGroupAddon>
            <IftaLabel>
              <DatePicker v-model="searchFilter.endDate" inputId="searcEnd" />
              <label for="searchEnd">종료일</label>
            </IftaLabel>
          </InputGroup>

          <InputGroup>
            <InputGroupAddon><i :class="useIcon('vendor')" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchFilter.matName" inputId="searchMatName" />
              <label for="searchMatName">공급처</label>
            </IftaLabel>
          </InputGroup>

          <div class="flex flex-col w-full">
            <InputGroup>
              <InputGroupAddon><i :class="useIcon('tags')" /></InputGroupAddon>
              <Select v-model="searchFilter.status" :options="statusOptions" optionLabel="name" optionValue="value" placeholder="상태" class="w-full h-[48px] text-base" />
            </InputGroup>
          </div>
        </div>
      </SearchCard>
    </div>
    <!--검색박스 end-->

    <!--중간버튼영역-->
    <div class="card flex flex-col gap-4">
      <div class="flex items-center justify-between my-3">
        <div class="font-semibold text-xl flex items-center justify-between gap-4 h-10">
          <div class="flex items-center gap-4"><span :class="useIcon('list')"></span> 발주 목록</div>
        </div>
        <div>
          <btn color="warn" icon="cancel" label="발주취소" @click="cancel" class="whitespace-nowrap" outlined />
        </div>
      </div>
      <selectTable v-model:selection="selectedRows" :columns="matOrderColumns" :data="purchaseOrderData" :showCheckbox="true" :paginator="true" :rows="15" @page-change="onPage" :page="page" />
    </div>
  </div>
</template>
<style scoped></style>
