<!-- ======================================================
📄 AccountLedger.vue (UI = HTML 완전동일)
====================================================== -->
<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import { useAppToast } from '@/composables/useAppToast';
import { useIcon } from '@/composables/useIcon';
import InputText from 'primevue/inputtext';
import Calendar from 'primevue/calendar';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Paginator from 'primevue/paginator';
import Button from 'primevue/button';
import { useRoute } from 'vue-router';

const route = useRoute();
const { toast } = useAppToast();

const icons = { home: useIcon('home'), vendor: useIcon('vendor'), list: useIcon('list') };
const breadcrumbHome = { icon: icons.home, to: '/' };
const breadcrumbItems = computed(() => [{ label: '영업관리' }, { label: '거래처원장', to: route.fullPath }]);

const search = ref({
  keyword: '',
  startDate: null,
  endDate: null
});
const list = ref([]);
const page = ref(1);
const rows = ref(10);

function fmt(v) {
  const n = Number(v);
  return isNaN(n) ? '₩0' : '₩' + n.toLocaleString('ko-KR');
}

async function loadData() {
  try {
    const params = {
      keyword: search.value.keyword,
      startDate: search.value.startDate,
      endDate: search.value.endDate,
      page: page.value,
      size: rows.value
    };
    const { data } = await axios.get('/api/account-ledger', { params });
    list.value = data.items || [];
  } catch (err) {
    toast('error', '거래처 조회 실패', err.message);
  }
}

function resetForm() {
  search.value = { keyword: '', startDate: null, endDate: null };
  loadData();
}

function exportPdf() {
  window.open('/api/account-ledger/report', '_blank');
}

onMounted(() => loadData());
</script>

<template>
  <div class="p-4 bg-gray-50 min-h-screen font-pretendard">
    <Breadcrumb class="rounded-lg mb-3" :home="breadcrumbHome" :model="breadcrumbItems" />

    <!-- 검색 카드 -->
    <div class="bg-white rounded-xl border border-gray-200 shadow-sm p-5 mb-5">
      <h2 class="flex items-center gap-2 text-lg font-semibold text-gray-700 mb-4">
        <i class="pi pi-search"></i> 거래처 검색
      </h2>

      <!-- 한 줄 정렬 -->
      <div class="flex items-end flex-wrap gap-4">
        <!-- 거래처명 -->
        <div class="flex flex-col gap-1" style="width:220px;">
          <label class="text-sm text-gray-500">거래처명</label>
          <InputText v-model="search.keyword" placeholder="거래처 검색" class="w-full h-9 text-sm" />
        </div>

        <!-- 시작일 -->
        <div class="flex flex-col gap-1">
          <label class="text-sm text-gray-500">시작일</label>
          <Calendar v-model="search.startDate" dateFormat="yy-mm-dd" showIcon inputClass="h-9 text-sm w-40" />
        </div>

        <span class="text-gray-500 mb-2">~</span>

        <!-- 종료일 -->
        <div class="flex flex-col gap-1">
          <label class="text-sm text-gray-500">종료일</label>
          <Calendar v-model="search.endDate" dateFormat="yy-mm-dd" showIcon inputClass="h-9 text-sm w-40" />
        </div>

        <!-- 검색/초기화 버튼 -->
        <div class="ml-auto flex gap-2">
          <Button label="검색" icon="pi pi-search" outlined size="small" class="h-9" @click="loadData" />
          <Button label="초기화" icon="pi pi-refresh" outlined size="small" class="h-9" @click="resetForm" />
        </div>
      </div>
    </div>

    <!-- 목록 카드 -->
    <div class="bg-white rounded-xl border border-gray-200 shadow-sm p-5">
      <div class="flex items-center justify-between mb-3">
        <h2 class="flex items-center gap-2 text-lg font-semibold text-gray-700">
          <i class="pi pi-list"></i> 거래처 원장
        </h2>
        <Button label="PDF 출력" icon="pi pi-file-pdf" class="bg-red-500 border-none text-white text-sm px-3 py-2 rounded-md" severity="danger"  @click="exportPdf" />
      </div>

      <!-- 테이블 -->
      <DataTable
        :value="list"
        dataKey="vendorId"
        size="small"
        rowHover
        responsiveLayout="scroll"
          class="text-base"
        
      >
        <Column field="companyName" header="거래처명" />
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
        <Column field="orderCount" header="주문건수">
          <template #body="{ data }">{{ data.orderCount || 0 }}건</template>
        </Column>
        <Column field="unpaidCount" header="미수건수">
          <template #body="{ data }">{{ data.unpaidCount || 0 }}건</template>
        </Column>
        <Column field="lastOrderDate" header="최근거래일자" />
      </DataTable>

      <!-- 페이지네이션 -->
      <div class="pt-2 border-t border-gray-100 mt-3 flex justify-end">
        <Paginator
          :rows="rows"
          :totalRecords="list.length"
          template="FirstPageLink PrevPageLink CurrentPageReport NextPageLink LastPageLink"
          currentPageReportTemplate="({currentPage} / {totalPages})"
          @page="(e) => { page.value = e.page + 1; loadData(); }"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.font-pretendard {
  font-family: "Pretendard", sans-serif;
}
</style>
