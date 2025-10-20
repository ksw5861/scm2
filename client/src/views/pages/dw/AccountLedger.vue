<!-- ======================================================
📄 AccountLedger.vue (페이지네이션 완성본)
====================================================== -->
<script setup>
import Modal from '@/components/common/Modal.vue'
import { useAppToast } from '@/composables/useAppToast'
import { useIcon } from '@/composables/useIcon'
import axios from 'axios'
import Button from 'primevue/button'
import Calendar from 'primevue/calendar'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import InputGroup from 'primevue/inputgroup'
import InputGroupAddon from 'primevue/inputgroupaddon'
import InputText from 'primevue/inputtext'
import Paginator from 'primevue/paginator'
import { computed, onMounted, ref } from 'vue'

const { toast } = useAppToast()
const icons = { home: useIcon('home'), vendor: useIcon('vendor'), list: useIcon('list') }

/* ====================== 데이터 ====================== */
const list = ref([])
const page = ref(1)
const rows = ref(10)
const totalRecords = ref(0)

const search = ref({
  vendorId: '',
  companyName: '',
  startDate: null,
  endDate: null
})

/* ====================== 날짜 변환 ====================== */
function useDateFormat(inputDate) {
  const valueRef = computed(() =>
    inputDate && typeof inputDate === 'object' && 'value' in inputDate
      ? inputDate.value
      : inputDate
  )

  return computed(() => {
    const input = valueRef.value
    if (!input) return ''
    const date = typeof input === 'string' ? new Date(input) : input
    if (isNaN(date.getTime())) return ''
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  })
}

const startDateFormatted = useDateFormat(computed(() => search.value.startDate))
const endDateFormatted = useDateFormat(computed(() => search.value.endDate))

/* ====================== 목록조회 ====================== */
async function loadData() {
  try {
    const params = {
      vendorId: search.value.vendorId,
      startDate: startDateFormatted.value,
      endDate: endDateFormatted.value,
      page: page.value,
      size: rows.value
    }
    console.log('[송신 파라미터]', params)
    const { data } = await axios.get('/api/account-ledger', { params })
    list.value = data.items || []
    totalRecords.value = data.page?.totalElements || list.value.length || 0
  } catch (err) {
    toast('error', '판매처 조회 실패', err.message)
  }
}

/* ====================== 페이지 초기화 / 변경 ====================== */
function resetForm() {
  search.value = { vendorId: '', companyName: '', startDate: null, endDate: null }
  page.value = 1
  loadData()
}

function exportPdf() {
  window.open('/api/account-ledger/report', '_blank')
}

function handlePageChange(e) {
  page.value = e.page + 1
  rows.value = e.rows
  loadData()
}

onMounted(() => loadData())

/* ====================== 판매처 모달 ====================== */
const isShowVendorModal = ref(false)
function openVendorModal() { isShowVendorModal.value = true }
function closeVendorModal() { isShowVendorModal.value = false }

const fetchVendorData = async ({ page = 1, size = 10, search = '' } = {}) => {
  const params = { page, size, keyword: search }
  const { data } = await axios.get('/api/wonjang/modal-list', { params })
  return { items: data.data || [], total: data.page?.totalElements || 0 }
}

function handleSelectVendor(item) {
  search.value.vendorId = item?.vendorId || ''
  search.value.companyName = item?.companyName || ''
  closeVendorModal()
  toast('info', '판매처 선택', `${item?.companyName}`)
}
</script>

<template>
  <div class="page-wrap font-pretendard">
    <!-- ====================== 검색 ====================== -->
    <div class="box">
      <div class="flex items-end flex-wrap gap-4 mb-3">
        <!-- 판매처명 -->
        <div class="flex flex-col gap-1" style="width: 240px">
          <label class="text-sm text-gray-500">판매처명</label>
          <InputGroup>
            <InputText
              v-model="search.companyName"
              placeholder="판매처 선택"
              @click="openVendorModal"
              readonly
              class="h-9 text-sm"
            />
            <InputGroupAddon>
              <Button icon="pi pi-search" text @click="openVendorModal" />
            </InputGroupAddon>
          </InputGroup>
        </div>

        <!-- 시작일 -->
        <div class="flex flex-col gap-1" style="width: 150px">
          <label class="text-sm text-gray-500">시작일</label>
          <Calendar v-model="search.startDate" dateFormat="yy-mm-dd" showIcon inputClass="h-9 text-sm w-full" />
        </div>

        <span class="text-gray-500 mb-2">~</span>

        <!-- 종료일 -->
        <div class="flex flex-col gap-1" style="width: 150px">
          <label class="text-sm text-gray-500">종료일</label>
          <Calendar v-model="search.endDate" dateFormat="yy-mm-dd" showIcon inputClass="h-9 text-sm w-full" />
        </div>
      </div>

      <!-- 버튼 -->
      <div class="flex justify-end gap-2 mt-3">
        <Button label="초기화" icon="pi pi-refresh" outlined class="h-10 px-4 text-sm" @click="resetForm" />
        <Button label="조회" icon="pi pi-search" class="h-10 px-4 text-sm" @click="loadData" />
      </div>
    </div>

    <!-- ====================== 목록 ====================== -->
    <div class="list-box">
      <div class="flex items-center justify-between mb-3">
        <h2 class="flex items-center gap-2 text-lg font-semibold text-gray-700">
          <i class="pi pi-list"></i> 판매처 원장
        </h2>
        <Button
          label="PDF 출력"
          icon="pi pi-file-pdf"
          class="bg-red-500 border-none text-white text-sm px-3 py-2 rounded-md"
          @click="exportPdf"
        />
      </div>

      <DataTable
        :value="list"
        dataKey="vendorId"
        size="small"
        rowHover
        responsiveLayout="scroll"
        class="text-base"
      >
        <Column field="companyName" header="판매처명" />
        <Column field="totalPrice" header="총매출">
          <template #body="{ data }">₩{{ Number(data.totalPrice || 0).toLocaleString() }}</template>
        </Column>
        <Column field="returnPrice" header="총반품">
          <template #body="{ data }">₩{{ Number(data.returnPrice || 0).toLocaleString() }}</template>
        </Column>
        <Column field="totalPayment" header="총입금">
          <template #body="{ data }">₩{{ Number(data.totalPayment || 0).toLocaleString() }}</template>
        </Column>
        <Column field="totalAr" header="미수금">
          <template #body="{ data }">₩{{ Number(data.totalAr || 0).toLocaleString() }}</template>
        </Column>
        <Column field="orderCount" header="주문건수">
          <template #body="{ data }">{{ data.orderCount || 0 }}건</template>
        </Column>
        <Column field="unpaidCount" header="미수건수">
          <template #body="{ data }">{{ data.unpaidCount || 0 }}건</template>
        </Column>
        <Column field="lastOrderDate" header="최근거래일자" />
      </DataTable>

      <!-- ✅ 페이지네이션 -->
     <div class="pt-2 border-t border-gray-100 mt-3 flex justify-center">
  <Paginator
    :first="(page - 1) * rows"
    :rows="rows"
    :totalRecords="totalRecords"
    :rowsPerPageOptions="[10, 20, 50]"
    template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink"
    @page="handlePageChange"
  />
</div>
    </div>

    <!-- 판매처 모달 -->
    <Modal
      :visible="isShowVendorModal"
      title="판매처 검색"
      dataKey="vendorId"
      :columns="[{ label: '판매처명', field: 'companyName' }]"
      :fetchData="fetchVendorData"
      :pageSize="5"
      :frontPagination="false"
      @select="handleSelectVendor"
      @close="closeVendorModal"
      :style="{ width: '500px' }"
    />
  </div>
</template>

<style scoped>
.page-wrap { padding: 16px; background: #f5f7fb; }
.box { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; padding: 20px 18px; margin-bottom: 14px; }
.list-box { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; padding: 16px; }
</style>
