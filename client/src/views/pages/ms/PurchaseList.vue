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
  stardDate: '',
  endDate: '',
  metName: '',
  vendor: ''
});

const pageLoad = async () => {
  const pageParam = { page: page.value.page, size: page.value.size };

  try {
    const res = await axios.get('/api/mat/purchaseList', { params: pageParam });
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
    const list = await axios.get('/api/mat/purchaseListStatus', { params: { purId } });
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
  const startRow = event.page * event.rows + 1;
  const endRow = (event.page + 1) * event.rows;

  pageLoad({ startRow, endRow }); // 여기서 axios 호출
};

const loadStatusCodes = async () => {
  try {
    const res = await axios.get('/api/mat/status/m01');
    // {"ms1":"요청등록","ms2":"요청승인",...} 형태로 변환
    codeMap.value = res.data.reduce((acc, cur) => {
      acc[cur.codeId] = cur.codeName;
      return acc;
    }, {});
  } catch (err) {
    toast('error', '공통코드 로드 실패', '상태명 불러오기 실패', '3000');
  }
};

onMounted(() => {
  pageLoad();
  loadStatusCodes();
});

const purchaseListColumn = [
  { label: '주문등록일', field: 'regDate' },
  { label: '주문번호', field: 'purNo' },
  { label: '자재명', field: 'matName' },
  { label: '주문수량', field: 'reqQty' },
  { label: '단위', field: 'unit' },
  { label: '공급처', field: 'companyName' },
  { label: '발주 담당자', field: 'empName' }
];

const statusColumn = [
  { label: '변경일', field: 'updateDate' },
  { label: '상태', field: 'status' },
  { label: '공급처 출고수량', field: 'supOutQty' },
  { label: '출고예정일', field: 'expectDate' },
  { label: '공급처 담당자', field: 'chargeName' }
];
</script>

<template>
  <div class="container">
    <div class="p-4">
      <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    </div>
    <!--검색영역-->
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
            <div class="font-semibold text-m">자재주문 목록</div>
            <Divider />
            <selectTable v-model:selection="selectedRows" :selectionMode="'single'" :columns="purchaseListColumn" :data="purchaseList" :paginator="true" :showCheckbox="false" @row-select="detailInfo" @page-change="onPage" :page="page" />
          </div>
        </div>
      </div>
      <!--하단우측-->
      <div class="md:w-1/2">
        <div class="card flex flex-col gap-4 h-full">
          <!-- 타이틀 -->
          <div class="card flex flex-col gap-4">
            <div class="font-semibold text-m">상세정보</div>
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
              <selectTable :columns="statusColumn" :data="statusList" :paginator="false" :showCheckbox="false" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
