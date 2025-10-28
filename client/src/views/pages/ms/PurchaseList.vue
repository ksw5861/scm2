<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import selectTable from '@/components/common/checkBoxTable.vue';
import { useAppToast } from '@/composables/useAppToast';
import { useRoute } from 'vue-router';
import { useIcon } from '@/composables/useIcon';
import { useDateFormat, useNumberFormat } from '@/composables/useFormat';
import Timeline from 'primevue/timeline';
import { useUserStore } from '@/stores/user';
import SearchCard from '@/components/card/SearchCard.vue';
import DatePicker from 'primevue/datepicker';

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
  const parentLabel = current.meta?.breadcrumb?.parent || '주문 목록';
  const currentLabel = current.name || '';
  return [{ label: parentLabel }, { label: currentLabel, to: route.fullPath }];
});

const events = ref(); //타임라인용!
const statusList = ref();
const purchaseList = ref();
const selectedRows = ref();
const codeMap = ref({}); // codeId → codeName 매핑용
// pagination
const page = ref({ page: 1, size: 10, totalElements: 0 });

//검색필드
const searchFilter = ref({
  startDate: null,
  endDate: null,
  vendorName: ''
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

const pageLoad = async () => {
  //검색필터 기간 유효성검사
  if (searchFilter.value.startDate && searchFilter.value.endDate) {
    if (new Date(searchFilter.value.startDate) > new Date(searchFilter.value.endDate)) {
      toast('warn', '기간 오류', '시작일은 종료일보다 이전이어야 합니다.', '3000');
      return;
    }
  }

  const params = {
    page: page.value.page,
    size: page.value.size,
    startDate: formatDate(searchFilter.value.startDate),
    endDate: formatDate(searchFilter.value.endDate),
    vendorName: searchFilter.value.vendorName
  };

  try {
    const res = await axios.get('/api/mpurchaseList', { params });
    const { list, page: pageInfo } = res.data;
    purchaseList.value = list.map((row) => ({
      id: row.purId,
      regDate: useDateFormat(row.regDate).value,
      purNo: row.purNo,
      matName: row.materialVO.matName,
      reqQty: row.reqQty,
      unit: row.materialVO.stockUnit,
      companyName: row.vendorVO.companyName,
      empName: row.empName
    }));
    page.value.totalElements = pageInfo.totalElements;
  } catch (error) {
    toast('error', '리스트 로드 실패', '주문 리스트 불러오기 실패:', '3000');
  }
};

const detailInfo = async () => {
  const purId = selectedRows.value.id; //주문테이블 id get!
  console.log(purId);
  try {
    const list = await axios.get('/api/mpurchaseListStatus', { params: { purId } });
    console.log(list);
    //타임라인
    events.value = list.data.map((item) => ({
      date: useDateFormat(item.reDate).value,
      status: codeMap.value[item.logPurMatStatus] || item.logPurMatStatus,
      rejMemo: item.logResonComm
    }));
    //상세테이블
    statusList.value = list.data.map((item) => ({
      id: item.purStatusId,
      updateDate: useDateFormat(item.reDate).value,
      chargeName: item.logName,
      status: codeMap.value[item.logPurMatStatus] || item.logPurMatStatus,
      supOutQty: item.logSupOutQty,
      expectDate: useDateFormat(item.logExpectDate).value
    }));
  } catch (error) {
    toast('error', '상세정보 실패', '상세정보 불러오기 실패:', '3000');
  }
};

const onPage = (event) => {
  page.value.page = event.page + 1;
  page.value.size = event.rows;
  pageLoad();
};

const loadStatusCodes = async () => {
  try {
    const res = await axios.get('/api/mstatus/m01');
    // {"ms1":"요청등록","ms2":"요청승인",...} 형태로 변환
    codeMap.value = res.data.reduce((acc, cur) => {
      acc[cur.codeId] = cur.codeName;
      return acc;
    }, {});
  } catch (err) {
    toast('error', '공통코드 로드 실패', '상태명 불러오기 실패', '3000');
  }
};

const resetSearch = async () => {
  searchFilter.value = {
    startDate: '',
    endDate: '',
    vendorName: ''
  };
  await pageLoad();
};

onMounted(() => {
  pageLoad();
  loadStatusCodes();
});

const purchaseListColumn = [
  { label: '주문등록일', field: 'regDate' },
  { label: '주문번호', field: 'purNo' },
  { label: '자재명', field: 'matName' },
  { label: '주문수량', field: 'reqQty', style: 'text-align: right' },
  { label: '단위', field: 'unit' },
  { label: '공급처', field: 'companyName' }
  // { label: '발주 담당자', field: 'empName' }
];

const statusColumn = [
  { label: '변경일', field: 'updateDate' },
  { label: '상태', field: 'status' },
  { label: '공급처 출고수량', field: 'supOutQty', style: 'text-align: right' },
  { label: '출고예정일', field: 'expectDate' },
  { label: '공급처 담당자', field: 'chargeName' }
];
</script>

<template>
  <div class="container">
    <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    <!--검색영역-->
    <div class="card flex flex-col gap-4 mt-4">
      <SearchCard title="현황 검색" @search="pageLoad" @reset="resetSearch">
        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
          <InputGroup>
            <InputGroupAddon><i :class="useIcon('calendar')" /></InputGroupAddon>
            <IftaLabel>
              <DatePicker v-model="searchFilter.startDate" inputId="searchStartDate" />
              <label for="searchStart">기간(시작일)</label>
            </IftaLabel>
          </InputGroup>

          <InputGroup>
            <InputGroupAddon><i :class="useIcon('calendar')" /></InputGroupAddon>
            <IftaLabel>
              <DatePicker v-model="searchFilter.endDate" inputId="searchEndDate" />
              <label for="searchEnd">기간(종료일)</label>
            </IftaLabel>
          </InputGroup>

          <InputGroup>
            <InputGroupAddon><i :class="useIcon('vendor')" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchFilter.vendorName" inputId="searchVendor" />
              <label for="searchVendor">공급처</label>
            </IftaLabel>
          </InputGroup>
        </div>
      </SearchCard>
    </div>
    <!--테이블영역--><!--테이블영역-->
    <div class="flex flex-col md:flex-row gap-8">
      <div class="md:w-1/2 planList">
        <div class="card flex flex-col gap-4 h-full">
          <!-- h-full 고정 -->
          <div class="card flex flex-col gap-4">
            <div class="font-semibold text-xl flex items-center justify-between gap-4 h-10">
              <div class="flex items-center gap-4"><span :class="useIcon('list')"></span> 자재주문 목록</div>
            </div>
            <Divider />
            <selectTable v-model:selection="selectedRows" :selectionMode="'single'" :columns="purchaseListColumn" :data="purchaseList" :lazy="true"  :paginator="true" :showCheckbox="false" @row-select="detailInfo" @page="onPage" :page="page" />
          </div>
        </div>
      </div>
      <!--하단우측-->
      <div class="md:w-1/2">
        <div class="card flex flex-col gap-4 h-full">
          <!-- 타이틀 -->
          <div class="card flex flex-col gap-4">
            <div class="font-semibold text-xl flex items-center justify-between gap-4 h-10">
              <div class="flex items-center gap-4"><span :class="useIcon('forward')"></span>진행현황</div>
            </div>
            <Divider />
            <!--타임라인-->
            <div class="card">
              <Timeline :value="events">
                <template #opposite="slotProps">
                  <small class="text-surface-500 dark:text-surface-400">{{ slotProps.item.date }}</small>
                </template>
                <template #content="slotProps"> [{{ slotProps.item.status }}] {{ slotProps.item.rejMemo }} </template>
              </Timeline>
            </div>
            <div>
              <selectTable v-model:selection="selectedStstus" :columns="statusColumn" :data="statusList" :paginator="false" :showCheckbox="false" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
