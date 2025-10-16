<!-- ======================================================
📄 AccountLedger.vue
- 거래처 목록 및 상세 페이지
- 요약/그래프는 Dashboard.vue로 이동함
====================================================== -->
<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import { useAppToast } from '@/composables/useAppToast';
import { useIcon } from '@/composables/useIcon';
import InputText from 'primevue/inputtext';
import InputGroup from 'primevue/inputgroup';
import InputGroupAddon from 'primevue/inputgroupaddon';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Paginator from 'primevue/paginator';
import Divider from 'primevue/divider';
import { useRoute } from 'vue-router';

const route = useRoute();
const { toast } = useAppToast();

/* ───────────── 아이콘/Breadcrumb ───────────── */
const icons = { home: useIcon('home'), vendor: useIcon('vendor'), list: useIcon('list') };
const breadcrumbHome = { icon: icons.home, to: '/' };
const breadcrumbItems = computed(() => [
  { label: '영업관리' },
  { label: '거래처원장', to: route.fullPath }
]);

/* ───────────── 상태 ───────────── */
const search = ref({ keyword: '' });
const list = ref([]);
const totalRecords = ref(0);
const selectedVendor = ref({});
const page = ref(1);
const rows = ref(10);

/* ───────────── 금액 포맷 ───────────── */
function fmt(v) {
  const n = Number(v);
  return isNaN(n) ? '₩0' : '₩' + n.toLocaleString('ko-KR');
}

/* ───────────── 거래처 목록 조회 ───────────── */
async function loadData() {
  try {
    const params = { keyword: search.value.keyword, page: page.value, size: rows.value };
    const { data } = await axios.get('/api/account-ledger', { params });
    list.value = data.items || [];
    totalRecords.value = data.total || 0;
  } catch (err) {
    toast('error', '거래처 조회 실패', err.message);
  }
}

/* ───────────── 상세 선택 이벤트 ───────────── */
function handleRowSelect(e) {
  selectedVendor.value = e.data;
}

onMounted(() => loadData());
</script>

<template>
  <Fluid>
    <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />

    <!-- 🔍 검색 -->
    <SearchCard title="거래처 검색" @search="loadData" @reset="() => (search.value.keyword = '')">
      <div class="p-2 w-full">
        <InputGroup>
          <InputGroupAddon><i :class="icons.vendor" /></InputGroupAddon>
          <IftaLabel>
            <InputText v-model="search.keyword" inputId="searchName" />
            <label for="searchName">거래처명</label>
          </IftaLabel>
        </InputGroup>
      </div>
    </SearchCard>

    <!-- 📋 목록 -->
    <div class="flex flex-col md:flex-row w-full gap-4 mt-4">
      <div class="w-full xl:w-5/12 lg:w-1/2">
        <div class="card flex flex-col">
          <div class="font-semibold text-lg flex items-center justify-between gap-4 h-10">
            <div class="flex items-center gap-4">
              <span :class="icons.list"></span> 거래처 목록
            </div>
            <div class="text-sm text-gray-400">
              총 <span class="font-semibold text-sm text-gray-700">{{ totalRecords }}</span>건
            </div>
          </div>
          <Divider />
          <DataTable
            :value="list"
            dataKey="vendorId"
            size="small"
            rowHover
            selectionMode="single"
            v-model:selection="selectedVendor"
            @rowSelect="handleRowSelect"
          >
            <Column field="companyName" header="거래처명" />
            <Column field="totalPrice" header="총금액">
              <template #body="{ data }">{{ fmt(data.totalPrice) }}</template>
            </Column>
            <Column field="totalAr" header="미수금">
              <template #body="{ data }">{{ fmt(data.totalAr) }}</template>
            </Column>
          </DataTable>
          <Paginator :rows="rows" :totalRecords="totalRecords"
                     @page="(e) => { page.value = e.page + 1; loadData(); }"/>
        </div>
      </div>

      <!-- 📊 상세 -->
      <div class="w-3/5">
        <div class="detail-card">
          <h3 class="text-lg font-bold mb-3">{{ selectedVendor.companyName }}</h3>
          <div class="grid grid-cols-3 gap-4">
            <div class="p-3 bg-gray-50 rounded-md">
              <p class="text-gray-500 text-sm">총매출</p>
              <p class="font-semibold text-blue-600">{{ fmt(selectedVendor.totalPrice) }}</p>
            </div>
            <div class="p-3 bg-gray-50 rounded-md">
              <p class="text-gray-500 text-sm">총반품</p>
              <p class="font-semibold text-red-500">{{ fmt(selectedVendor.returnPrice) }}</p>
            </div>
            <div class="p-3 bg-gray-50 rounded-md">
              <p class="text-gray-500 text-sm">총입금</p>
              <p class="font-semibold text-green-600">{{ fmt(selectedVendor.totalPayment) }}</p>
            </div>
            <div class="p-3 bg-gray-50 rounded-md">
              <p class="text-gray-500 text-sm">미수금</p>
              <p class="font-semibold text-orange-500">{{ fmt(selectedVendor.totalAr) }}</p>
            </div>
            <div class="p-3 bg-gray-50 rounded-md">
              <p class="text-gray-500 text-sm">최근거래일자</p>
              <p class="font-semibold text-indigo-500">
                {{ selectedVendor.lastOrderDate || '-' }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Fluid>
</template>
