<template>

    <Fluid>

        <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />

        <!-- ===== 헤더 ===== -->
        <SearchCard title="반품 내역 검색" @search="fetchReturns" @reset="resetFilters">
            <div class="flex flex-wrap w-full">

                <!-- 반품일자 -->
                <div class="flex flex-col gap-2 p-2 w-full xl:w-1/2">
                    <label class="font-semibold mb-1">반품 일자</label>
                    <div class="flex items-center gap-2">
                        <DatePicker v-model="filters.startDate" placeholder="시작일" dateFormat="yy-mm-dd" showIcon />
                        <span>~</span>
                        <DatePicker v-model="filters.endDate" placeholder="종료일" dateFormat="yy-mm-dd" showIcon />
                    </div>
                </div>

                <!-- 반품 상태 -->
                <div class="flex flex-col gap-2 p-2 w-full xl:w-1/2">
                    <label class="font-semibold mb-1">반품 상태</label>
                    <div class="flex gap-2 flex-nowrap overflow-x-auto">
                        <Button
                            v-for="status in RETURN_STATUS_OPTIONS"
                            :key="status"
                            :label="status"
                            :class="[
                                'whitespace-nowrap',
                                filters.status === status ? '' : 'p-button-outlined'
                            ]"
                            @click="() => { filters.status = status; fetchReturns(); }"
                        />
                    </div>
                </div>

                <!-- 반품번호 -->
                <div class="flex flex-col gap-2 p-2 w-full">
                    <InputGroup>
                        <InputGroupAddon><i class="pi pi-tag" /></InputGroupAddon>
                        <IftaLabel>
                        <InputText v-model="filters.returnId" inputId="filterReturnId" />
                        <label for="filterReturnId">반품 번호</label>
                        </IftaLabel>
                    </InputGroup>
                </div>

            </div>
        </SearchCard>

        <div class="flex flex-col 2xl:flex-row w-full gap-4 mt-4">

            <div class="w-full xl:w-1/2">
                <div class="card flex flex-col">
                    <div class="font-semibold text-lg sm:text-xl flex items-center justify-between gap-4 h-10">
                        <div class="flex items-center gap-4">
                            <span :class="icons.list"></span>
                            반품 내역
                        </div>
                    </div>

                    <Divider />

                    <DataTable
                        :value="returns"
                        selectionMode="single"
                        v-model:selection="selectedReturn"
                        dataKey="returnId"
                        :key="returns.length"
                        paginator
                        :rows="10"
                        responsiveLayout="scroll"
                        resizableColumns
                        columnResizeMode="fit"
                        class="custom-table"
                        :emptyMessage="'조회된 반품 내역이 없습니다.'"
                    >
                        <Column field="returnId" header="반품번호" style="width:140px; text-align:center;" />
                        <Column header="대표제품명" style="width:200px;">
                            <template #body="{ data }">
                            {{ data.prodName }}
                            <span v-if="data.prodCount > 1"> 외 {{ data.prodCount - 1 }}건</span>
                            </template>
                        </Column>
                        <Column field="returnDate" header="반품일자" style="width:130px; text-align:center;" />
                        <Column field="returnPrice" header="반품총액" style="width:130px; text-align:right;">
                            <template #body="slotProps">
                            {{ formatCurrency(slotProps.data.returnPrice) }} 원
                            </template>
                        </Column>
                        <Column field="returnStatus" header="상태" style="width:100px; text-align:center;">
                            <template #body="slotProps">
                            <span :class="statusClass(slotProps.data.returnStatus)">
                                {{ statusLabel(slotProps.data.returnStatus) }}
                            </span>
                            </template>
                        </Column>
                    </DataTable>

                </div>
            </div>

            <div class="w-full xl:w-1/2">
                <div class="card flex flex-col">
                    <div class="font-semibold text-lg sm:text-xl flex items-center justify-between gap-4 h-10">
                        <div class="flex items-center gap-4">
                            <span :class="icons.info"></span>
                            반품 상세 내역
                            <div class="text-sm text-gray-400" style="align-self: flex-end;">
                                총 반품 금엑
                                <span class="font-semibold text-sm text-gray-700 text-red-400">
                                    {{ formatCurrency(detailTotal) }}
                                </span>
                                원
                            </div>
                        </div>

                        <div class="flex gap-2 ">
                            <Btn
                                icon="excel"
                                label="Excel 다운로드"
                                class="whitespace-nowrap"
                                style="background-color: #16a34a; border: none;"
                                @click="exportExcel"
                            />
                            <Btn
                                icon="pdf"
                                label="PDF 다운로드"
                                class="whitespace-nowrap"
                                style="background-color: #dc2626; border: none;"
                                @click="exportPDF"
                            />
                        </div>
                    </div>

                    <Divider />

                    <DataTable
                        v-if="returnDetails.length"
                        :value="returnDetails"
                        :key="selectedReturn?.returnId"
                        responsiveLayout="scroll"
                        resizableColumns
                        columnResizeMode="fit"
                        class="custom-table"
                    >
                        <Column field="prodId" header="제품코드" style="width:120px; text-align:center;" />
                        <Column field="prodName" header="제품명" style="width:150px;" />
                        <Column field="spec" header="규격" style="width:100px; text-align:center;" />
                        <Column field="unit" header="단위" style="width:80px; text-align:center;" />
                        <Column field="prodUnitPrice" header="제품단가" style="width:120px; text-align:right;">
                            <template #body="slotProps">
                            {{ formatCurrency(slotProps.data.prodUnitPrice) }} 원
                            </template>
                        </Column>
                        <Column field="returnQty" header="반품수량" style="width:80px; text-align:center;" />
                        <Column field="returnTotal" header="합계" style="width:120px; text-align:right;">
                            <template #body="slotProps">
                            {{ formatCurrency(slotProps.data.returnTotal) }} 원
                            </template>
                        </Column>
                        <Column field="returnWhy" header="반품사유" style="width:150px;" />
                        <Column field="rdetailStatus" header="개별상태" style="width:100px; text-align:center;">
                            <template #body="slotProps">
                            <span :class="statusClass(slotProps.data.rdetailStatus)">
                                {{ statusLabel(slotProps.data.rdetailStatus) }}
                            </span>
                            </template>
                        </Column>
                    </DataTable>

                </div>
            </div>


        </div>
    </Fluid>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import axios from 'axios'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import DatePicker from 'primevue/datepicker'
import { useAppToast } from '@/composables/useAppToast'
import { useUserStore } from '@/stores/user'
import { useRoute } from 'vue-router'
import { useIcon } from '@/composables/useIcon'

const route = useRoute();
const userStore = useUserStore()
const { toast } = useAppToast()

// -----------------------------
// 상태 관리
// -----------------------------
const filters = ref({
  startDate: null,
  endDate: null,
  status: '',
  returnId: ''
})
const returns = ref([])
const selectedReturn = ref(null)
const returnDetails = ref([])
const RETURN_STATUS_OPTIONS = ['대기', '처리중', '처리완료'];

/* ───────────────────────────────
 *  아이콘 세트
 * ─────────────────────────────── */
const icons = {
  home: useIcon('home'),
  list: useIcon('list'),
  info: useIcon('info')
};

/* ───────────────────────────────
 *  Breadcrumb 구성
 * ─────────────────────────────── */
const breadcrumbHome = { icon: icons.home, to: '/' };
const breadcrumbItems = computed(() => {
  const matched = route.matched.filter(r => r.meta);
  if (!matched.length) return [];
  const current = matched[matched.length - 1];
  return [
    { label: current.meta.breadcrumb?.parent || '' },
    { label: '반품 내역 조회' }
  ];
});

// -----------------------------
// 계산 속성
// -----------------------------
const detailTotal = computed(() =>
  returnDetails.value.reduce((acc, item) => acc + (item.returnTotal || 0), 0)
)

// -----------------------------
// 유틸 함수
// -----------------------------
const formatCurrency = (value) => {
  if (value === null || value === undefined) return '0'
  return value.toLocaleString('ko-KR')
}
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
const formatDateForAPI = (date) => {
  if (!date) return null
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const statusLabel = (status) => {
  switch (status) {
    case '대기': return '대기'
    case '승인': return '승인'
    case '반려': return '반려'
    default: return status || ''
  }
}
const statusClass = (status) => ({
  'status-wait': status === '대기',
  'status-approve': status === '승인',
  'status-reject': status === '반려'
})

// -----------------------------
// API 핸들링
// -----------------------------
const fetchReturns = async () => {
  try {
    const { data } = await axios.get('/api/returnlist', {
      params: {
        vendorId: userStore.code,
        startDate: formatDateForAPI(filters.value.startDate),
        endDate: formatDateForAPI(filters.value.endDate),
        returnStatus: filters.value.status,
        returnId: filters.value.returnId
      }
    })

    if (data.status !== 'success' || !Array.isArray(data.list)) {
      toast('error', '반품 목록 조회 실패', '서버에서 올바른 데이터를 반환하지 않았습니다.')
      return
    }

    returns.value = data.list.map((item) => ({
      returnId: item.returnId,
      prodName: item.prodName,
      prodCount: item.prodCount,
      returnDate: formatDate(item.returnDate),
      returnPrice: item.returnPrice || 0,
      returnStatus: item.returnStatus
    }))

    selectedReturn.value = null
    returnDetails.value = []
  } catch (error) {
    console.error('반품 목록 조회 오류:', error)
    toast('error', '반품 목록 조회 실패', '서버 오류가 발생했습니다.')
  }
}

const fetchReturnDetail = async (returnId) => {
  try {
    const { data } = await axios.get(`/api/returns/${returnId}/details`)

    if (data.status !== 'success' || !Array.isArray(data.details)) {
      toast('error', '반품 상세 조회 실패', '서버에서 올바른 데이터를 반환하지 않았습니다.')
      return
    }

    returnDetails.value = data.details.map((item) => ({
      ...item,
      returnTotal: item.prodUnitPrice * item.returnQty
    }))
  } catch (error) {
    console.error('반품 상세 조회 오류:', error)
    toast('error', '반품 상세 조회 실패', '서버 오류가 발생했습니다.')
  }
}

// -----------------------------
// watch
// -----------------------------
watch(
  () => selectedReturn.value,
  (newReturn) => {
    if (newReturn?.returnId) {
      fetchReturnDetail(newReturn.returnId)
    } else {
      returnDetails.value = []
    }
  }
)

// -----------------------------
// 상태 업데이트
// -----------------------------
const updateReturnStatus = async (newStatus) => {
  if (!selectedReturn.value) {
    alert('반품 건을 먼저 선택하세요.')
    return
  }

  try {
    const { data } = await axios.put(`/api/returns/${selectedReturn.value.returnId}/status`, {
      status: newStatus
    })

    if (data.status === 'success') {
      alert(`반품 상태가 '${newStatus}'(으)로 변경되었습니다.`)
      fetchReturns()
    } else {
      alert(data.message || '상태 변경 실패')
    }
  } catch (error) {
    console.error('반품 상태 변경 오류:', error)
    alert('서버 오류로 상태를 변경하지 못했습니다.')
  }
}

// -----------------------------
// 기타 기능
// -----------------------------
const resetFilters = () => {
  filters.value = {
    startDate: null,
    endDate: null,
    status: '',
    returnId: ''
  }
  fetchReturns()
}
const exportExcel = () => console.log('엑셀 다운로드 실행 (추후 구현)')
const exportPDF = () => console.log('PDF 출력 실행 (추후 구현)')

// -----------------------------
// lifecycle
// -----------------------------
onMounted(fetchReturns)
</script>
