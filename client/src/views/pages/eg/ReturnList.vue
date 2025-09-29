<template>
  <div class="return-list">
    <!-- 헤더 -->
    <div class="header">
      <h2>반품 조회 (판매처)</h2>
      <div class="header-actions">
        <Button label="초기화" icon="pi pi-refresh" class="p-button-outlined" @click="resetFilters" />
        <Button label="조회" icon="pi pi-search" class="p-button-primary" @click="fetchReturns" />
        <Button label="엑셀 다운로드" icon="pi pi-file-excel" class="p-button-success" @click="exportExcel" />
        <Button label="PDF 출력" icon="pi pi-file-pdf" class="p-button-danger" @click="exportPDF" />
      </div>
    </div>

    <!-- 검색 영역 -->
    <div class="filter-section">
      <!-- 반품일자 -->
      <div class="filter-group">
        <span class="filter-label">반품일자</span>
        <DatePicker v-model="filters.startDate" placeholder="시작일" dateFormat="yy-mm-dd" showIcon />
        <span>~</span>
        <DatePicker v-model="filters.endDate" placeholder="종료일" dateFormat="yy-mm-dd" showIcon />
      </div>

      <!-- 반품 상태 -->
      <div class="filter-group">
        <span class="filter-label">반품상태</span>
        <div class="status-buttons">
          <Button label="대기" :class="{selected: filters.status === 'REQ'}" @click="() => { filters.status = 'REQ'; fetchReturns(); }" />
          <Button label="승인" :class="{selected: filters.status === 'APPROVE'}" @click="() => { filters.status = 'APPROVE'; fetchReturns(); }" />
          <Button label="반려" :class="{selected: filters.status === 'REJECT'}" @click="() => { filters.status = 'REJECT'; fetchReturns(); }" />
        </div>
      </div>

      <!-- 반품번호 -->
      <div class="filter-group">
        <span class="filter-label">반품번호</span>
        <InputText v-model="filters.returnId" placeholder="반품번호 검색" />
      </div>
    </div>

    <!-- 목록 & 상세 -->
    <div class="content-section">
      <!-- 좌측 반품 목록 -->
      <div class="return-list-table">
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
              {{ formatCurrency(slotProps.data.returnPrice) }}
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

      <!-- 우측 반품 상세 -->
      <div class="return-detail-table">
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
              {{ formatCurrency(slotProps.data.prodUnitPrice) }}
            </template>
          </Column>
          
          <Column field="returnQty" header="반품수량" style="width:80px; text-align:center;" />

          <Column field="returnTotal" header="합계" style="width:120px; text-align:right;">
            <template #body="slotProps">
              {{ formatCurrency(slotProps.data.returnTotal) }}
            </template>
          </Column>

          <Column field="returnWhy" header="반품사유" style="width:150px;" />

          <!-- 상태 컬럼 -->
          <Column field="rdetailStatus" header="상태" style="width:100px; text-align:center;">
            <template #body="slotProps">
              <span :class="statusClass(slotProps.data.rdetailStatus)">
                {{ statusLabel(slotProps.data.rdetailStatus) }}
              </span>
            </template>
          </Column>
        </DataTable>

        <!-- 합계 금액 -->
        <div v-if="returnDetails.length" class="total-footer">
          총 반품 금액 : <strong>{{ formatCurrency(detailTotal) }}</strong>
        </div>
      </div>
    </div>
  </div>
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

const { toast } = useAppToast()

/* ===== 검색 조건 ===== */
const filters = ref({
  startDate: null,
  endDate: null,
  status: '',
  returnId: ''
})

/* ===== 반품 목록 ===== */
const returns = ref([])

/* ===== 선택된 반품 ===== */
const selectedReturn = ref(null)

/* ===== 반품 상세 ===== */
const returnDetails = ref([])

/* ===== 합계 계산 ===== */
const detailTotal = computed(() => {
  return returnDetails.value.reduce((acc, item) => acc + (item.returnTotal || 0), 0)
})

/* ===== 통화 포맷 ===== */
const formatCurrency = (value) => {
  if (value === null || value === undefined) return '0 원'
  return value.toLocaleString('ko-KR') + ' 원'
}

/* ===== 날짜 포맷 ===== */
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

/* ===== API 날짜 포맷 ===== */
const formatDateForAPI = (date) => {
  if (!date) return null
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

/* ===== 상태 라벨 ===== */
const statusLabel = (status) => {
  switch (status) {
    case 'REQ': return '대기'
    case 'APPROVE': return '승인'
    case 'REJECT': return '반려'
    default: return status || '-'
  }
}

/* ===== 상태별 CSS 클래스 ===== */
const statusClass = (status) => {
  return {
    'status-wait': status === 'REQ',
    'status-approve': status === 'APPROVE',
    'status-reject': status === 'REJECT'
  }
}

/* ===== 반품 목록 조회 ===== */
const fetchReturns = async () => {
  try {
    const { data } = await axios.get('/api/returnlist', {
      params: {
        startDate: formatDateForAPI(filters.value.startDate),
        endDate: formatDateForAPI(filters.value.endDate),
        returnStatus: filters.value.status,
        returnId: filters.value.returnId
      }
    })

    // 서버 응답 검증
    if (data.status !== 'success' || !Array.isArray(data.list)) {
      toast('error', '반품 목록 조회 실패', '서버에서 올바른 데이터를 반환하지 않았습니다.')
      return
    }

    // 백엔드 데이터 그대로 저장 (프론트에서 화면만 가공)
    returns.value = data.list.map(item => ({
      returnId: item.returnId,
      prodName: item.prodName,           // 대표제품명
      prodCount: item.prodCount,         // 총 제품 개수
      returnDate: formatDate(item.returnDate),
      returnPrice: item.returnPrice || 0,
      returnStatus: item.returnStatus
    }))

    // 초기화
    selectedReturn.value = null
    returnDetails.value = []
  } catch (error) {
    console.error('반품 목록 조회 오류:', error)
    toast('error', '반품 목록 조회 실패', '서버 오류가 발생했습니다.')
  }
}


/* ===== 반품 상세 조회 ===== */
const fetchReturnDetail = async (returnId) => {
  try {
    const { data } = await axios.get(`/api/returns/${returnId}/details`)

    if (data.status !== 'success' || !Array.isArray(data.details)) {
      toast('error', '반품 상세 조회 실패', '서버에서 올바른 데이터를 반환하지 않았습니다.')
      return
    }

    returnDetails.value = data.details.map(item => ({
      ...item,
      returnTotal: item.prodUnitPrice * item.returnQty
    }))
  } catch (error) {
    console.error('반품 상세 조회 오류:', error)
    toast('error', '반품 상세 조회 실패', '서버 오류가 발생했습니다.')
  }
}

/* ===== 선택된 반품 변경 감시 ===== */
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

/* ===== 초기화 ===== */
const resetFilters = () => {
  filters.value = {
    startDate: null,
    endDate: null,
    status: '',
    returnId: ''
  }
  fetchReturns()
}

onMounted(fetchReturns)

/* ===== PDF & 엑셀 ===== */
const exportExcel = () => console.log('엑셀 다운로드 실행')
const exportPDF = () => console.log('PDF 출력 실행')
</script>

<style scoped>
.return-list { padding: 20px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }

.filter-section {
  display: flex; flex-wrap: wrap; align-items: center; gap: 15px;
  padding: 10px; border: 1px solid #dcdcdc; border-radius: 8px;
  background: #f9f9f9; margin-bottom: 20px;
}

.filter-group { display: flex; align-items: center; gap: 10px; }
.filter-label { font-weight: 600; min-width: 70px; text-align: right; }
.status-buttons button { margin-right: 5px; }
.status-buttons .selected { background: #007ad9 !important; color: white !important; }

.content-section { display: flex; flex-wrap: wrap; gap: 15px; }
.return-list-table, .return-detail-table { flex: 1; min-width: 400px; }

.total-footer {
  margin-top: 10px; text-align: right;
  font-size: 1.1rem; font-weight: bold; color: #333;
}

.custom-table { width: 100%; font-size: 0.95rem; }
::v-deep(.p-datatable-thead > tr > th) {
  border-right: 1px solid #e5e7eb; background: #f9fafb;
  font-weight: 600; text-align: center !important; padding: 10px 0;
}
::v-deep(.p-datatable-tbody > tr > td) {
  border-right: 1px solid #f0f0f0; text-align: center; padding: 8px 10px;
}

.status-wait { color: #ff9800; font-weight: bold; }
.status-approve { color: #4caf50; font-weight: bold; }
.status-reject { color: #f44336; font-weight: bold; }
</style>
