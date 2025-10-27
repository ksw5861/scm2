<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import btn from '@/components/common/Btn.vue';
import selectTable from '@/components/common/checkBoxTable.vue';
import { useAppToast } from '@/composables/useAppToast';
import { useRoute } from 'vue-router';
import { useIcon } from '@/composables/useIcon';
import { useDateFormat, useNumberFormat } from '@/composables/useFormat';
import Dialog from 'primevue/dialog';
import Textarea from 'primevue/textarea';
import { useUserStore } from '@/stores/user';
import SearchCard from '@/components/card/SearchCard.vue';
import DatePicker from 'primevue/datepicker';
//출고승인화면

// Pinia Store
// (userStore.name)이름
// (userStore.code)코드 - 계정기준으로
const userStore = useUserStore();
const vendorId = userStore.code;
const vendorName = '승인담당';

console.log(vendorId);

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

const matOrderData = ref([]);
const selectedRows = ref([]);
const rejModal = ref(false);
const rejMemo = ref('');

const page = ref({ page: 1, size: 10, totalElements: 0 });

//검색필드
const searchFilter = ref({
  startDate: '',
  endDate: '',
  matName: '',
  toWarehouse: ''
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
    toWarehouse: searchFilter.value.toWarehouse
  };

  try {
    const list = await axios.get(`/api/sOrderList/${vendorId}`, { params });
    console.log(list);
    matOrderData.value = list.data.map((item) => ({
      id: item.purId,
      orderDate: useDateFormat(item.regDate).value,
      orderNo: item.purNo,
      dueDate: useDateFormat(item.dueDate).value,
      matId: item.matId,
      matName: item.materialVO.matName,
      orderQty: item.reqQty,
      unit: item.materialVO.stockUnit,
      toWarehouse: item.wareHouseVO.whName,
      total: item.total,
      buyerName: item.empName
    }));

    page.value.totalElements = matOrderData.value.length;
  } catch (error) {
    toast('error', '리스트 로드 실패', '주문 리스트 불러오기 실패:', '3000');
  }
};

const onPage = (event) => {
  const startRow = event.page * event.rows + 1;
  const endRow = (event.page + 1) * event.rows;

  fetchList({
    startDate: formatDate(searchFilter.value.startDate),
    endDate: formatDate(searchFilter.value.endDate),
    matName: searchFilter.value.matName,
    toWarehouse: searchFilter.value.toWarehouse,
    startRow,
    endRow
  });
};

//주문승인
const approve = async () => {
  const list = JSON.parse(JSON.stringify(selectedRows.value));
  const idList = list.map((row) => row.id);

  //유효성검사
  if (idList.length === 0) {
    toast('warn', '선택 필요', '승인할 항목을 선택해주세요:', '3000');
    return;
  }
  if (!confirm('선택한 주문을 승인하시겠습니까?')) {
    return;
  }

  try {
    await axios.post('/api/sapprove', { purId: idList, vId: vendorId, name: vendorName });
    toast('success', '승인 성공', '주문 승인 성공:', '3000');

    fetchList();
  } catch (error) {
    toast('error', '승인 실패', '주문 승인 실패:', '3000');
  }
};
//반려
const rejectPurchase = async () => {
  //유효성검사
  if (rejMemo.value.trim() === '') {
    toast('warn', '사유 필요', '반려 사유를 입력해주세요:', '3000');
    return;
  }
  if (!confirm('선택한 주문을 반려하시겠습니까?')) {
    return;
  }

  try {
    await axios.post('/api/sreject', null, { params: { purId: selectedRows.value[0].id, rejMemo: rejMemo.value, staff: vendorName } });
    rejModal.value = false;
    rejMemo.value = '';
    toast('success', '등록 성공', '주문 거부 성공:', '3000');
    fetchList();
    selectedRows.value = [];
  } catch (error) {
    toast('error', '등록 실패', '주문 승인 실패:', '3000');
  }
};

const resetSearch = () => {
  searchFilter.value = {
    stardDate: '',
    endDate: '',
    matName: '',
    location: ''
  };
  fetchList();
};

//모달(열기)
const oepnRejModal = () => {
  const selectedCount = selectedRows.value.length;

  if (selectedCount === 0) {
    toast('warn', '선택 필요', '반려할 항목을 선택해주세요:', '3000');
    return;
  }

  if (selectedCount > 1) {
    toast('error', '반려 불가', '1건씩만 처리 가능합니다:', '3000');
    return;
  }

  rejModal.value = true;
};
//모달(닫기)
const closeRejModal = () => {
  rejModal.value = false;
};

onMounted(() => {
  fetchList();
});

const matOrderColumns = [
  { label: '주문일자', field: 'orderDate', sortable: true },
  { label: '주문번호', field: 'orderNo' },
  { label: '납기요청일', field: 'dueDate', sortable: true },
  { label: '자재코드', field: 'matId' },
  { label: '자재명', field: 'matName', sortable: true },
  { label: '주문수량', field: 'orderQty',style: 'text-align: right', sortable: true },
  { label: '단위', field: 'unit' },
  { label: '도착지', field: 'toWarehouse' },
  { label: '총 금액', field: 'total', style: 'text-align: right', sortable: true },
  { label: '구매처 담당자', field: 'buyerName' }
];
</script>

<template>
  <div class="container">
    <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    <!--검색영역-->
    <div class="card flex flex-col gap-4 mt-4">
      <SearchCard title="주문 검색" @search="fetchList" @reset="resetSearch">
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
            <InputGroupAddon><i :class="useIcon('box')" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchFilter.matName" inputId="searchMatName" />
              <label for="searchMatName">자재명</label>
            </IftaLabel>
          </InputGroup>

          <InputGroup>
            <InputGroupAddon><i :class="useIcon('truck')" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchFilter.toWarehouse" inputId="searchtoWarehouse" />
              <label for="searchtoWarehouse">도착지</label>
            </IftaLabel>
          </InputGroup>
        </div>
      </SearchCard>
    </div>
    <!--검색박스 end-->

    <!--중간버튼영역-->
    <div class="card flex flex-col gap-4">
      <div class="my-3 flex flex-wrap items-center justify-end gap-2">
        <btn color="warn" icon="cancel" label="주문 취소" @click="oepnRejModal" outlined class="whitespace-nowrap" />
        <btn color="info" icon="check" label="주문 승인" @click="approve" class="whitespace-nowrap" outlined />
      </div>
      <div class="font-semibold text-xl flex items-center justify-between gap-4 h-10">
        <div class="flex items-center gap-4"><span :class="useIcon('list')"></span> 주문 목록</div>
      </div>
      <selectTable v-model:selection="selectedRows" :columns="matOrderColumns" :data="matOrderData" :paginator="true" :rows="15" @page-change="onPage" :page="page" />
    </div>
  </div>

  <!--반려모달-->
  <Dialog v-model:visible="rejModal" modal header="반려 사유(공급처)" :style="{ width: '500px' }">
    <div class="card flex justify-center">
      <Textarea v-model="rejMemo" rows="5" cols="100" />
    </div>
    <div class="flex justify-center gap-2">
      <btn color="secondary" icon="check" label="닫기" @click="closeRejModal" class="whitespace-nowrap" outlined />
      <btn color="info" icon="cancel" label="등록" @click="rejectPurchase" class="whitespace-nowrap" outlined />
    </div>
  </Dialog>
</template>

<style scoped></style>
