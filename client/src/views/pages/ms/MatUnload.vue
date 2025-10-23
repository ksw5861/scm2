<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import btn from '@/components/common/Btn.vue';
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
const empId = userStore.code; //사원코드

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

const shipedListData = ref();
const selectedRows = ref();
const selectedDeRow = ref();
const shipDetailListData = ref([{ matId: '', matName: '', ortQty: null, unit: '' }]);
//모달
const returnModal = ref(false);
const returnMemo = ref('');
// pagination
const page = ref({ page: 1, size: 10, totalElements: 0 });
//검색조건
const searchFilter = ref({
  startDate: '',
  endDate: '',
  vendorName: ''
});

//datePicker날짜변환
const formatDate = (date) => {
  if (!date) return '';
  const d = new Date(date);
  return d.toISOString().slice(0, 10);
};

const pageLoad = async () => {
  const params = {
    page: page.value.page,
    size: page.value.size,
    startDate: formatDate(searchFilter.value.startDate),
    endDate: formatDate(searchFilter.value.endDate),
    vendorName: searchFilter.value.vendorName
  };

  try {
    const res = await axios.get(`/api/mshipedList`, { params });
    const { list, page: pageInfo } = res.data;
    shipedListData.value = list.map((item) => ({
      id: item.inboundId,
      shipedDate: useDateFormat(item.vendorOutDate).value,
      vanOutNo: item.venOutNo,
      companyName: item.vendorVO.companyName,
      inStatus: item.inboundStatus,
      vanEmpName: item.venName
    }));
    page.value.totalElements = pageInfo.totalElements;
  } catch (err) {
    toast('error', '리스트 로드 실패', '주문 리스트 불러오기 실패:', '3000');
  }
};

const detailInfo = async () => {
  try {
    const list = await axios.get('/api/mshipedDetailList', { params: { inboundId: selectedRows.value.id } });
    console.log(list);
    //상세테이블
    shipDetailListData.value = list.data.map((item) => ({
      id: item.inboundDetId,
      matId: item.matId,
      matName: item.materialVO.matName,
      outQty: item.outQty,
      unit: item.materialVO.unit,
      purStatusId: item.purStatusId,
      purId: item.purId
    }));
  } catch (error) {
    toast('error', '상세정보 실패', '상세정보 불러오기 실패:', '3000');
  }
};

const approve = async () => {
  try {
    await axios.post('/api/mapproveUnload', null, { params: { inboundId: selectedRows.value.id, unloadEmp: empName } });
    toast('info', '하차승인 성공', '하차승인 성공:', '3000');
    await pageLoad();
    await detailInfo();
    shipDetailListData.value = [{ matId: '', matName: '', ortQty: null, unit: '' }];
  } catch (error) {
    toast('error', '하차승인 실패', '하차승인 실패:', '3000');
  }
};

const returnSubmit = async () => {
  //사유, 담당자, 하차등록시 해당 마스터와 디테일 모두 상태값 변경하고 기록해줘야함. [입고마스터 + 디테일 + 상태변경로그] + @ 발주상태값 반품으로도 가능??/??
  try {
    await axios.post('/api/munloadReturn', null, { params: { inboundId: selectedRows.value.id, unloadEmp: empName, rejMemo: returnMemo.value } });
    toast('info', '반품등록 성공', '반품등록 성공:', '3000');
    await pageLoad();
    closeReturnModal();
  } catch (error) {
    toast('error', '반품등록 실패', '반품등록 실패:', '3000');
  }
};

const openShipmentReport = () => {
  console.log(selectedRows.value.id);
  if (!selectedRows.value || !selectedRows.value.id) {
    toast('info', '선택 필요', '명세서를 출력할 출고건을 선택해주세요.', '3000');
    return;
  }

  const inboundId = selectedRows.value.id;
  window.open(`/api/mat/shipment/${inboundId}`, '_blank');
};

const onPage = (event) => {
  const startRow = event.page * event.rows + 1;
  const endRow = (event.page + 1) * event.rows;

  pageLoad({ startRow, endRow }); // 여기서 axios 호출
};

const resetSearch = () => {
  searchFilter.value = {
    startDate: '',
    endDate: '',
    vendorName: ''
  };
  pageLoad();
};

onMounted(() => {
  pageLoad();
});

const openRetrunModal = () => {
  if (selectedRows.value.length === 0) {
    toast('info', '선택 필요', '반품등록할 항목을 출고번호를 선택해주세요:', '3000');
    return;
  }

  toast('info', '반품등록', '반품등록시 전량 반품처리됩니다.', '5000');

  returnModal.value = true;
};

const closeReturnModal = () => {
  returnModal.value = false;
};
const shipedColumn = [
  { label: '출고일', field: 'shipedDate' },
  { label: '출고번호', field: 'vanOutNo' },
  { label: '공급처', field: 'companyName' },
  // { label: '상태', field: 'inStatus' }, //출고 ->  미도착 -> 1) 하차완료 2) 하차거부 -> 입고대기
  { label: '공급처 담당자', field: 'vanEmpName' }
  //운송정보 (운송사, 차량번호, 기사 연락처 등), 송장번호
];

const shipDetailColumn = [
  { label: '자재코드', field: 'matId' },
  { label: '자재명', field: 'matName' },
  { label: '수량', field: 'outQty' },
  { label: '단위', field: 'unit' }
];
</script>

<template>
  <div class="container">
    <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    <div class="card flex flex-col gap-4 mt-4">
      <SearchCard title="입고 조회" @search="pageLoad" @reset="resetSearch">
        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
          <InputGroup>
            <InputGroupAddon><i :class="useIcon('box')" /></InputGroupAddon>
            <IftaLabel>
              <DatePicker v-model="searchFilter.startDate" inputId="searchStart" />
              <label for="searchStart">시작일</label>
            </IftaLabel>
          </InputGroup>

          <InputGroup>
            <InputGroupAddon><i :class="useIcon('box')" /></InputGroupAddon>
            <IftaLabel>
              <DatePicker v-model="searchFilter.endDate" inputId="searchEnd" />
              <label for="searchEnd">종료일</label>
            </IftaLabel>
          </InputGroup>

          <InputGroup>
            <InputGroupAddon><i :class="useIcon('box')" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchFilter.vendorName" inputId="searchVendor" />
              <label for="searchVendor">공급처</label>
            </IftaLabel>
          </InputGroup>
        </div>
      </SearchCard>
    </div>

    <div class="flex flex-col md:flex-row gap-8">
      <!-- 왼쪽 카드 -->
      <div class="md:w-1/2">
        <div class="card flex flex-col gap-4" style="height: 800px">
          <div class="flex items-center justify-between my-3">
            <div class="font-semibold text-m">하차대기 목록</div>
            <div class="flex gap-2">
              <btn color="secondary" icon="file" label="출고명세서" class="whitespace-nowrap" outlined @click="openShipmentReport" />
            </div>
          </div>
          <Divider />
          <selectTable v-model:selection="selectedRows" :selectionMode="'single'" :columns="shipedColumn" :data="shipedListData" :paginator="true" :rows="15" @row-select="detailInfo" :page="page" @page-change="onPage" />
        </div>
      </div>

      <!-- 오른쪽 카드 -->
      <div class="md:w-1/2">
        <div class="card flex flex-col gap-4" style="height: 800px">
          <div class="flex items-center justify-between my-3">
            <div class="font-semibold text-m">상세정보</div>
            <div class="flex gap-2">
              <btn color="warn" icon="cancel" label="반송" class="whitespace-nowrap" outlined @click="openRetrunModal" />
              <btn color="info" icon="check" label="승인" class="whitespace-nowrap" outlined @click="approve" />
            </div>
          </div>
          <Divider />
          <selectTable v-model:selection="selectedDeRow" :selectionMode="'single'" :columns="shipDetailColumn" :data="shipDetailListData" :paginator="false" :showCheckbox="false" />
        </div>
      </div>
    </div>
  </div>
  <!--반품모달-->
  <Dialog v-model:visible="returnModal" modal header="반송 사유" :style="{ width: '500px' }">
    <div class="card flex justify-center">
      <Textarea v-model="returnMemo" rows="5" cols="100" />
    </div>
    <div class="flex justify-center gap-2">
      <btn color="warn" icon="pi pi-file-excel" label="취소" @click="closeReturnModal" />
      <btn color="warn" icon="pi pi-file-excel" label="등록" @click="returnSubmit" />
    </div>
  </Dialog>
</template>

<scoped>
</scoped>
