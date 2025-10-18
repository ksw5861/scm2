<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import btn from '@/components/common/Btn.vue';
import selectTable from '@/components/common/checkBoxTable.vue';
import SearchField from '@/components/common/SearchBox.vue';
import { useAppToast } from '@/composables/useAppToast';
import { useRoute } from 'vue-router';
import { useIcon } from '@/composables/useIcon';
import { useDateFormat, useNumberFormat } from '@/composables/useFormat';
import Dialog from 'primevue/dialog';
import Textarea from 'primevue/textarea';
import { useUserStore } from '@/stores/user';
import SearchCard from '@/components/card/SearchCard.vue';
import Select from 'primevue/select';
import DatePicker from 'primevue/datepicker';

// Pinia Store
const userStore = useUserStore();
// const vendorId = userStore.code;
const vendorId = ref('V800');
const vendorName = ref('공급처담당자이름'); //공급처담당자이름으로 로그찍히니 변경xxx

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

const dateRange = ref({ start: null, end: null });
const materialName = ref();
const statusList = ref([]);
const matOrderData = ref([]);
const selectedRows = ref([]);
const rejModal = ref(false);
const rejMemo = ref('');

const page = ref({ page: 1, size: 10, totalElements: 0 });

//검색필드
const searchFilter = ref({
  stardDate: '',
  endDate: '',
  vendor: '',
//   lotStatus: ''
});


//주문목록(페이지로드시)
const fetchList = async () => {
  try {
    const list = await axios.get(`/api/supplier/OrderList/${vendorId.value}`);
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
  fetchList({ startRow, endRow });
};

onMounted(() => {
  fetchList();
});

//주문승인
const approve = async () => {
  const list = JSON.parse(JSON.stringify(selectedRows.value));
  const idList = list.map((row) => row.id);

  try {
    await axios.post('/api/supplier/approve', { purId: idList, name: vendorId.value });
    toast('info', '승인 성공', '주문 승인 성공:', '3000');

    fetchList();
  } catch (error) {
    toast('error', '승인 실패', '주문 승인 실패:', '3000');
  }
};

//반려모달
const oepnRejModal = () => {
  const selectedCount = selectedRows.value.length;

  if (selectedCount === 0) {
    toast('warn', '선택 필요', '반려할 항목을 선택해주세요:', '3000');
    return;
  }

  if (selectedCount > 1) {
    toast('info', '반려 불가', '1건씩만 처리 가능합니다:', '3000');
    return;
  }

  rejModal.value = true;
};

const closeRejModal = () => {
  rejModal.value = false;
};

const rejectPurchase = async () => {
  try {
    await axios.post('/api/supplier/reject', null, { params: { purId: selectedRows.value[0].id, rejMemo: rejMemo.value, staff: vendorName.value } });
    rejModal.value = false;
  } catch (error) {
    toast('error', '등록 실패', '주문 승인 실패:', '3000');
  }
};

const matOrderColumns = [
  { label: '주문일자', field: 'orderDate', sortable: true },
  { label: '구매요청번호', field: 'orderNo' },
  { label: '납기요청일', field: 'dueDate', sortable: true },
  { label: '자재코드', field: 'matId' },
  { label: '자재명', field: 'matName', sortable: true },
  { label: '주문수량', field: 'orderQty', sortable: true },
  { label: '단위', field: 'unit' },
  { label: '도착지', field: 'toWarehouse' },
  { label: '총 금액', field: 'total', sortable: true },
  { label: '구매처 담당자', field: 'buyerName', sortable: true }
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
                        <label for="searchVendor">자재명</label>
                    </IftaLabel>
                </InputGroup>

                 <InputGroup>
                    <InputGroupAddon><i :class="useIcon('box')" /></InputGroupAddon>
                    <IftaLabel>
                        <InputText v-model="searchFilter.vendor" inputId="searchMa" />
                        <label for="searchVendor">도착지</label>
                    </IftaLabel>
                </InputGroup>

            </div>
        </SearchCard>
    </div>
    <!--검색박스 end-->

    <!--중간버튼영역-->
    <div class="card flex flex-col gap-4">
      <div class="my-3 flex flex-wrap items-center justify-end gap-2">
        <btn color="secondary" icon="cancel" label="취소" @click="oepnRejModal" outlined />
        <btn color="info" icon="add" label="등록" class="whitespace-nowrap" outlined @click="approve" />
      </div>
      <div class="font-semibold text-xl mb-5">조회 내역</div>
      <selectTable v-model:selection="selectedRows" :columns="matOrderColumns" :data="matOrderData" :paginator="true" :rows="15" @page-change="onPage" :page="page" />
    </div>
  </div>

  <!--반려모달-->
  <Dialog v-model:visible="rejModal" modal header="반려 사유(공급처)" :style="{ width: '500px' }">
    <div class="card flex justify-center">
      <Textarea v-model="rejMemo" rows="5" cols="100" />
    </div>
    <div class="flex justify-center gap-2">
      <btn color="warn" icon="pi pi-file-excel" label="취소" @click="closeRejModal" />
      <btn color="warn" icon="pi pi-file-excel" label="등록" @click="rejectPurchase" />
    </div>
  </Dialog>
</template>

<style scoped></style>
