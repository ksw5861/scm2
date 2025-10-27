<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import btn from '@/components/common/Btn.vue';
import selectTable from '@/components/common/checkBoxTable.vue';
import { useAppToast } from '@/composables/useAppToast';
import { useRoute } from 'vue-router';
import { useIcon } from '@/composables/useIcon';
import { useDateFormat, useNumberFormat } from '@/composables/useFormat';
import Timeline from 'primevue/timeline';
import Dialog from 'primevue/dialog';
import { useUserStore } from '@/stores/user';
import SearchCard from '@/components/card/SearchCard.vue';
import DatePicker from 'primevue/datepicker';

// Pinia Store
// (userStore.name)이름
// (userStore.code)코드 - 계정기준으로
const userStore = useUserStore();
const vendorId = userStore.code;

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
const supplyList = ref();
const selectedRows = ref();
const codeMap = ref({}); // codeId → codeName 매핑용
//이미지모달
const imgModal = ref(false);
const defectImg = ref();
//pagenation
const page = ref({ page: 1, size: 10, totalElements: 0 });

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

const fetchSuppliyList = async () => {
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
    const list = await axios.get(`/api/ssupplyList/${vendorId}`, { params });
    console.log(list);
    // flatMap으로 평탄화
    const flattened = list.data.flatMap((row) =>
      row.details.map((detail) => ({
        id: detail.inboundDetId,
        outDate: useDateFormat(row.vendorOutDate).value,
        outNo: row.venOutNo,
        matName: detail.materialVO.matName,
        outQty: detail.outQty,
        unit: detail.materialVO.stockUnit,
        empName: row.venName
      }))
    );
    //총 데이터 수 계산해서 paginator에 반영
    supplyList.value = flattened;
    page.value.totalElements = flattened.length;
  } catch (error) {
    toast('error', '리스트 로드 실패', '주문 리스트 불러오기 실패:', '3000');
  }
};

const detailInfo = async () => {
  const inboundDetId = selectedRows.value.id;
  console.log(inboundDetId);
  try {
    const list = await axios.get('/api/ssupplyDetailList', { params: { inboundDetId } });
    console.log(list);
    //타임라인
    events.value = list.data.map((item) => ({
      date: useDateFormat(item.reDate).value,
      status: codeMap.value[item.logInboundStatus] || item.logInboundStatus,
      rejMemo: item.logMemo,
      logId: item.inLogId
    }));
    //상세테이블
    statusList.value = list.data.map((item) => ({
      id: item.inLogId,
      updateDate: useDateFormat(item.reDate).value,
      chargeName: item.logName,
      status: codeMap.value[item.logInboundStatus] || item.logInboundStatus,
      rejQty: item.logRejQty
    }));
  } catch (error) {
    toast('error', '상세정보 실패', '상세정보 불러오기 실패:', '3000');
  }
};

const loadStatusCodes = async () => {
  try {
    const res = await axios.get('/api/mstatus/ind');
    // {"ms1":"요청등록","ms2":"요청승인",...} 형태로 변환
    codeMap.value = res.data.reduce((acc, cur) => {
      acc[cur.codeId] = cur.codeName;
      return acc;
    }, {});
  } catch (err) {
    toast('error', '공통코드 로드 실패', '상태명 불러오기 실패', '3000');
  }
};

const openImgModal = async (inLogId) => {
  try {
    const res = await axios.get(`/api/img/defect/${inLogId}`, {
      //이미지 데이터는 바이너리로 responseType: 'blob' 옵션을 줘야함!
      responseType: 'blob'
    });
    // blob → 브라우저 URL로 변환
    defectImg.value = URL.createObjectURL(res.data);
  } catch (error) {
    toast('error', '이미지 로드 실패', '이미지불러오기 실패', '3000');
  }

  imgModal.value = true;
};

const closeRejModal = () => {
  imgModal.value = false;
};

const onPage = (event) => {
  const startRow = event.page * event.rows + 1;
  const endRow = (event.page + 1) * event.rows;
  fetchSuppliyList({
    startRow,
    endRow,
    startDate: formatDate(searchFilter.value.startDate),
    endDate: formatDate(searchFilter.value.endDate),
    matName: searchFilter.value.matName,
    status: searchFilter.value.status
  });
};

const resetSearch = () => {
  searchFilter.value = {
    startDate: '',
    endDate: '',
    matName: '',
    status: ''
  };
  fetchSuppliyList();
};

onMounted(() => {
  fetchSuppliyList();
  loadStatusCodes();
});

const supplyListColumn = [
  { label: '출고일', field: 'outDate' },
  { label: '출고번호', field: 'outNo' },
  { label: '자재명', field: 'matName' },
  { label: '출고수량', field: 'outQty', style: 'text-align: right' },
  { label: '단위', field: 'unit' },
  { label: '담당자', field: 'empName' }
];

const statusColumn = [
  { label: '변경일', field: 'updateDate' },
  { label: '상태', field: 'status' },
  { label: '반송 및 불량수량', field: 'rejQty', style: 'width: 10rem; text-align: right' },
  { label: '발주처 담당자', field: 'chargeName', style: 'width: 10rem' }
];
</script>

<template>
  <div class="container">
    <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    <!--검색영역-->
    <div class="card flex flex-col gap-4 mt-4">
      <SearchCard title="현황 검색" @search="fetchSuppliyList" @reset="resetSearch">
        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
          <InputGroup>
            <InputGroupAddon><i :class="useIcon('calendar')" /></InputGroupAddon>
            <IftaLabel>
              <DatePicker v-model="searchFilter.startDate" inputId="searchStart" />
              <label for="searchStart">시작일</label>
            </IftaLabel>
          </InputGroup>

          <InputGroup>
            <InputGroupAddon><i :class="useIcon('calendar')" /></InputGroupAddon>
            <IftaLabel>
              <DatePicker v-model="searchFilter.endDate" inputId="searchEnd" />
              <label for="searchEnd">종료일</label>
            </IftaLabel>
          </InputGroup>

          <InputGroup>
            <InputGroupAddon><i :class="useIcon('box')" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchFilter.matName" inputId="searchMatName" />
              <label for="searchMatName">자재명</label>
            </IftaLabel>
          </InputGroup>

          <!-- <div class="flex flex-col w-full">
            <InputGroup>
              <InputGroupAddon><i :class="useIcon('box')" /></InputGroupAddon>
              <Select v-model="searchFilter.status" :options="statusOptions" optionLabel="name" optionValue="value" placeholder="출고 상태" class="w-full h-[48px] text-base" />
            </InputGroup>
          </div> -->
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
              <div class="flex items-center gap-4"><span :class="useIcon('list')"></span> 자재공급 목록</div>
            </div>
            <Divider />
            <selectTable v-model:selection="selectedRows" :selectionMode="'single'" :columns="supplyListColumn" :data="supplyList" :paginator="true" :showCheckbox="false" @row-select="detailInfo" @page-change="onPage" :page="page" />
          </div>
        </div>
      </div>
      <!--하단우측-->
      <div class="md:w-1/2">
        <div class="card flex flex-col gap-4 h-full">
          <!-- 타이틀 -->
          <div class="card flex flex-col gap-4">
            <div class="font-semibold text-xl flex items-center justify-between gap-4 h-10">
              <div class="flex items-center gap-4"><span :class="useIcon('forward')"></span> 진행현황</div>
            </div>
            <Divider />
            <!--타임라인-->
            <div class="card">
              <Timeline :value="events">
                <template #opposite="slotProps">
                  <small class="text-surface-500 dark:text-surface-400">{{ slotProps.item.date }}</small>
                </template>
                <template #content="slotProps"> [{{ slotProps.item.status }}] {{ slotProps.item.rejMemo }} <btn v-if="slotProps.item.status === '불량'" icon="clip" color="secondary" variant="text" @click="openImgModal(slotProps.item.logId)" outlined :size="size" /> </template>
                <!--d4[불량일경우] 돋보기 아이콘???-->
              </Timeline>
            </div>
            <div>
              <selectTable v-model:selection="selectedStatus" :columns="statusColumn" :data="statusList" :paginator="false" :showCheckbox="false" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <Dialog v-model:visible="imgModal" modal header="불량 이미지(발주처)" :style="{ width: '500px' }">
    <div class="card flex justify-center">
      <img :src="defectImg" alt="불량 이미지" class="w-full rounded-lg" />
    </div>
    <div class="flex justify-center">
      <btn color="contrast" icon="check" label="닫기" @click="closeRejModal" class="whitespace-nowrap" outlined />
    </div>
  </Dialog>
</template>

<style scoped></style>
