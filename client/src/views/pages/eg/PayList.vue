<template>
  <div class="pay-list">
    <!-- ===== 헤더 ===== -->
    <div class="header">
      <h2>납부내역조회 (판매처)</h2>
      <p class="subtitle">납부내역 조회 페이지입니다.</p>
    </div>

    <!-- ===== 검색 영역 ===== -->
    <div class="search-section">
      <div class="search-form">
        <!-- 결제일자 -->
        <div class="form-group date-group">
          <label class="form-label">결제일자</label>
          <div class="date-range">
            <Calendar
              v-model="filters.startDate"
              placeholder="시작일"
              dateFormat="yy-mm-dd"
              class="calendar-input"
            />
            <span class="range-separator">~</span>
            <Calendar
              v-model="filters.endDate"
              placeholder="종료일"
              dateFormat="yy-mm-dd"
              class="calendar-input"
            />
          </div>
        </div>

        <!-- 결제번호 -->
        <div class="form-group code-group">
          <label class="form-label">결제번호</label>
          <InputText
            v-model="filters.payId"
            placeholder="결제번호 입력"
            class="input-text"
          />
        </div>

        <!-- 버튼 영역 -->
        <div class="form-actions">
          <Button
            label="초기화"
            icon="pi pi-refresh"
            class="p-button-outlined reset-btn"
            @click="resetFilters"
          />
          <Button
            label="조회"
            icon="pi pi-search"
            class="p-button-primary search-btn"
            @click="searchPayments"
          />
        </div>
      </div>
    </div>

    <!-- ===== 결과 테이블 ===== -->
    <div class="result-section">
      <div class="export-buttons">
        <Button
          label="PDF 출력"
          icon="pi pi-file-pdf"
          class="p-button-danger p-button-sm"
          @click="exportPDF"
        />
        <Button
          label="엑셀 다운로드"
          icon="pi pi-file-excel"
          class="p-button-success p-button-sm"
          @click="exportExcel"
        />
      </div>

      <DataTable
        :value="payList"
        paginator
        :rows="10"
        responsiveLayout="scroll"
        resizableColumns
        columnResizeMode="fit"
        class="payment-table"
      >
        <Column field="payId" header="결제번호" style="width:140px;" />
        <Column field="payDate" header="결제일자" style="width:140px;" />
        <Column field="creditLimit" header="당시 여신한도" style="width:140px; text-align:right;">
          <template #body="slotProps">
            {{ formatCurrency(slotProps.data.creditLimit) }}
          </template>
        </Column>
        <Column field="outstandingAmount" header="납부 전 미수금" style="width:140px; text-align:right;">
          <template #body="slotProps">
            {{ formatCurrency(slotProps.data.outstandingAmount) }}
          </template>
        </Column>

        <Column field="payAmount" header="납부금액" style="width:140px; text-align:right;">
          <template #body="slotProps">
            <span :class="{ negative: slotProps.data.payAmount < 0 }">
              {{ formatCurrency(slotProps.data.payAmount) }}
            </span>
          </template>
        </Column>

        <Column field="finalBalance" header="납부 후 미수금" style="width:140px; text-align:right;">
          <template #body="slotProps">
            {{ formatCurrency(slotProps.data.finalBalance) }}
          </template>
        </Column>

        <Column field="creditBalance" header="여신잔액" style="width:140px; text-align:right;">
          <template #body="slotProps">
            {{ formatCurrency(slotProps.data.creditBalance) }}
          </template>
        </Column>

        <Column field="payRmk" header="적요" style="width:200px;" />
      </DataTable>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Calendar from 'primevue/calendar'
import { useToast } from 'primevue/usetoast'
import { useUserStore } from '@/stores/user'
import vendor from '@/config/menus/menu/dh/vendor'

const userStore = useUserStore()
const toast = useToast()

// -----------------------------
// 상태 관리
// -----------------------------
const filters = ref({
  payId: '',
  startDate: null,
  endDate: null
})
const payList = ref([])

// -----------------------------
// 유틸 함수
// -----------------------------
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const formatCurrency = (value) => {
  if (value === null || value === undefined) return '0 원'
  return value.toLocaleString('ko-KR') + ' 원'
}

// -----------------------------
// API 핸들링
// -----------------------------
const searchPayments = async () => {
  try {
    const params = {
      payId: filters.value.payId || '',
      startDate: filters.value.startDate ? formatDate(filters.value.startDate) : '',
      endDate: filters.value.endDate ? formatDate(filters.value.endDate) : '',
      vendorId: userStore.code
    }

    const res = await axios.get('/api/payments', { params })
    console.log('납부내역 조회 결과:', res.data)
    console.log('납부내역 조회 결과2:', res.data.list[0])

    payList.value = (res.data.list || []).map(item => ({
          payId: item.payId,
          payDate: item.payDate,
          outstandingAmount: item.outstandingAmount,
          payAmount: item.payAmount,
          finalBalance: item.finalBalance,
          creditBalance: item.creditBalance,
          payRmk: item.payRmk || '',
          creditLimit: item.creditLimit

          }))

  } catch (error) {
    console.error('납부내역 조회 실패:', error)
    payList.value = []
    toast.add({
      severity: 'error',
      summary: '오류',
      detail: '납부내역 조회 중 오류가 발생했습니다.',
      life: 3000
    })
  }
}

// -----------------------------
// 기타 기능
// -----------------------------
const resetFilters = () => {
  filters.value.payId = ''
  filters.value.startDate = null
  filters.value.endDate = null
  payList.value = []
}

const exportPDF = () => {
  toast.add({
    severity: 'info',
    summary: 'PDF 출력',
    detail: 'PDF 출력 기능은 추후 구현 예정입니다.',
    life: 3000
  })
}

const exportExcel = () => {
  toast.add({
    severity: 'info',
    summary: '엑셀 다운로드',
    detail: '엑셀 다운로드 기능은 추후 구현 예정입니다.',
    life: 3000
  })
}

// -----------------------------
// lifecycle
// -----------------------------
onMounted(searchPayments)
</script>

<style scoped>
/* ===== 전체 레이아웃 ===== */
.pay-list {
  padding: 20px;
  background: #f8f9fa;
}

/* ===== 헤더 ===== */
.header {
  margin-bottom: 10px;
}
.header h2 {
  font-size: 1.4rem;
  margin-bottom: 5px;
}
.subtitle {
  font-size: 0.9rem;
  color: #888;
}

/* ===== 검색 영역 ===== */
.search-section {
  background: #fff;
  padding: 15px;
  border: 1px solid #dcdcdc;
  border-radius: 6px;
  margin-bottom: 20px;
}
.search-form {
  display: flex;
  align-items: flex-end;
  gap: 20px;
  flex-wrap: wrap;
}
.form-group {
  display: flex;
  flex-direction: column;
}
.form-label {
  font-size: 0.9rem;
  font-weight: 500;
  margin-bottom: 5px;
}
.input-text {
  width: 200px;
}
.date-range {
  display: flex;
  align-items: center;
}
.calendar-input {
  width: 140px;
}
.range-separator {
  margin: 0 8px;
  font-weight: bold;
  color: #555;
}

/* ===== 버튼 ===== */
.form-actions {
  display: flex;
  gap: 10px;
}
.reset-btn {
  background: white;
  color: #333;
}
.search-btn {
  background: #007ad9;
  border: none;
}

/* ===== 결과 테이블 ===== */
.result-section {
  background: #fff;
  padding: 15px;
  border: 1px solid #dcdcdc;
  border-radius: 6px;
}
.export-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-bottom: 10px;
}
.payment-table {
  width: 100%;
  font-size: 0.95rem;
}
::v-deep(.p-datatable-thead > tr > th) {
  background: #f9fafb;
  border-right: 1px solid #e5e7eb;
  text-align: center;
  font-weight: 600;
  padding: 10px;
}
::v-deep(.p-datatable-tbody > tr > td) {
  border-right: 1px solid #f0f0f0;
  text-align: center;
  padding: 8px 10px;
}

/* ===== 반응형 ===== */
@media (max-width: 768px) {
  .search-form {
    flex-direction: column;
    gap: 10px;
  }
  .input-text,
  .calendar-input {
    width: 100%;
  }
  .export-buttons {
    flex-direction: column;
    align-items: flex-end;
  }
}

/* ===== 금액 음수 색상 ===== */
.negative {
  color: #e74c3c;
  font-weight: bold;
}
</style>
