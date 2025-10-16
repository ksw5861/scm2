<!-- ======================================================
📄 AccountLedger.vue
- 거래처 목록 및 상세 페이지 (UI 리터칭 / 고정 높이 + 톤 다운)
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

const icons = { home: useIcon('home'), vendor: useIcon('vendor'), list: useIcon('list') };
const breadcrumbHome = { icon: icons.home, to: '/' };
const breadcrumbItems = computed(() => [{ label: '영업관리' }, { label: '거래처원장', to: route.fullPath }]);

const search = ref({ keyword: '' });
const list = ref([]);
const selectedVendor = ref({});
const page = ref(1);
const rows = ref(10);

function fmt(v) {
  const n = Number(v);
  return isNaN(n) ? '₩0' : '₩' + n.toLocaleString('ko-KR');
}

async function loadData() {
  try {
    const params = { keyword: search.value.keyword, page: page.value, size: rows.value };
    const { data } = await axios.get('/api/account-ledger', { params });
    list.value = data.items || [];
  } catch (err) {
    toast('error', '거래처 조회 실패', err.message);
  }
}

function handleRowSelect(e) {
  selectedVendor.value = e.data;
}

onMounted(() => loadData());
</script>

<template>
  <Fluid>
    <Breadcrumb class="rounded-lg mb-2" :home="breadcrumbHome" :model="breadcrumbItems" />

    <!-- 🔍 검색폼 -->
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

    <!-- 📋 목록 + 상세 -->
    <div class="flex flex-col md:flex-row w-full gap-4 mt-4">
      <!-- 🧾 거래처 목록 -->
      <div class="w-full xl:w-6/12 lg:w-1/2">
        <div class="card flex flex-col shadow-sm border border-gray-100 bg-white rounded-lg" style="min-height: 440px">
          <div class="flex items-center justify-between px-4 py-3 bg-gray-50 border-b border-gray-100 rounded-t-lg">
            <div class="flex items-center gap-2 font-semibold text-gray-700">
              <i :class="icons.list"></i>
              <span>거래처 목록</span>
            </div>
          </div>

          <DataTable :value="list" dataKey="vendorId" size="small" rowHover selectionMode="single" v-model:selection="selectedVendor" @rowSelect="handleRowSelect">
            <Column field="companyName" header="거래처명" />
            <Column field="prevTotalPrice" header="이월금액">
              <template #body="{ data }">{{ fmt(data.prevTotalPrice) }}</template>
            </Column>
            <Column field="totalPrice" header="총매출">
              <template #body="{ data }">{{ fmt(data.totalPrice) }}</template>
            </Column>
            <Column field="returnPrice" header="총반품">
              <template #body="{ data }">{{ fmt(data.returnPrice) }}</template>
            </Column>
            <Column field="totalPayment" header="총입금">
              <template #body="{ data }">{{ fmt(data.totalPayment) }}</template>
            </Column>
            <Column field="totalAr" header="미수금">
              <template #body="{ data }">{{ fmt(data.totalAr) }}</template>
            </Column>
            <Column field="lastOrderDate" header="최근거래일자" />
          </DataTable>

          <div class="px-3 py-2 border-t border-gray-100 bg-gray-50 mt-auto">
            <Paginator
              :rows="rows"
              :totalRecords="list.length"
              template="FirstPageLink PrevPageLink CurrentPageReport NextPageLink LastPageLink"
              currentPageReportTemplate="({currentPage} / {totalPages})"
              @page="
                (e) => {
                  page.value = e.page + 1;
                  loadData();
                }
              "
            />
          </div>
        </div>
      </div>

      <!-- 📊 상세 카드 -->
      <div class="w-3/5">
        <div class="card shadow-sm border border-gray-100 bg-white rounded-lg p-5 flex flex-col" style="min-height: 440px">
          <h3 class="text-lg font-bold mb-4 text-gray-700">
            {{ selectedVendor.companyName || '거래처 선택' }}
          </h3>

          <!-- 🔹 상세 데이터 박스 -->
          <div class="grid grid-cols-3 grid-rows-2 gap-4 flex-grow h-full">
            <div
              v-for="(v, i) in [
                { label: '이월금액', value: fmt(selectedVendor.prevTotalPrice), color: 'text-gray-700' },
                { label: '총매출', value: fmt(selectedVendor.totalPrice), color: 'text-blue-600' },
                { label: '총반품', value: fmt(selectedVendor.returnPrice), color: 'text-red-500' },
                { label: '총입금', value: fmt(selectedVendor.totalPayment), color: 'text-green-600' },
                { label: '미수금', value: fmt(selectedVendor.totalAr), color: 'text-orange-500' },
                { label: '최근거래일자', value: selectedVendor.lastOrderDate || '-', color: 'text-indigo-500' }
              ]"
              :key="i"
              class="flex flex-col justify-center items-center bg-gray-50 border border-gray-100 rounded-lg shadow-sm"
            >
              <p class="text-sm text-gray-500 mb-1">{{ v.label }}</p>
              <p class="font-bold text-lg" :class="v.color">{{ v.value }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Fluid>
</template>
