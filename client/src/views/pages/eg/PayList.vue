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
        <!-- DB 컬럼 기준 -->
        <Column field="PAY_ID" header="결제번호" style="width:120px;" />
        <Column field="PAY_DATE" header="결제일자" style="width:120px;" />

        <!-- 미수금 -->
        <Column field="OUTSTANDING_AMOUNT" header="미수금" style="width:120px; text-align:right;">
          <template #body="slotProps">
            {{ formatCurrency(slotProps.data.OUTSTANDING_AMOUNT) }}
          </template>
        </Column>

        <!-- 납부금 -->
        <Column field="PAY_AMOUNT" header="납부금액" style="width:120px; text-align:right;">
          <template #body="slotProps">
            {{ formatCurrency(slotProps.data.PAY_AMOUNT) }}
          </template>
        </Column>

        <!-- 최종잔액 -->
        <Column field="FINAL_BALANCE" header="최종잔액" style="width:120px; text-align:right;">
          <template #body="slotProps">
            {{ formatCurrency(slotProps.data.FINAL_BALANCE) }}
          </template>
        </Column>

        <!-- 여신잔액 -->
        <Column field="CREDIT_BALANCE" header="여신잔액" style="width:120px; text-align:right;">
          <template #body="slotProps">
            {{ formatCurrency(slotProps.data.CREDIT_BALANCE) }}
          </template>
        </Column>

        <!-- 적요 -->
        <Column field="PAY_RMK" header="적요" style="width:180px;" />
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

// ===== 검색 조건 =====
const filters = ref({
  payId: '',
  startDate: null,
  endDate: null
})

// ===== 납부내역 데이터 =====
const payList = ref([])

// ===== 날짜 포맷 =====
const formatDate = (date) => {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = ('0' + (d.getMonth() + 1)).slice(-2)
  const day = ('0' + d.getDate()).slice(-2)
  return `${year}-${month}-${day}`
}

// ===== 금액 포맷 =====
const formatCurrency = (value) => {
  if (!value && value !== 0) return '0 원'
  return value.toLocaleString('ko-KR') + ' 원'
}

// ===== 납부내역 조회 API =====
const searchPayments = async () => {
  try {
    const params = {
      payId: filters.value.payId || '',
      startDate: filters.value.startDate ? formatDate(filters.value.startDate) : '',
      endDate: filters.value.endDate ? formatDate(filters.value.endDate) : ''
    }

    const res = await axios.get('/api/paymentlist', { params })

    console.log(res)

    payList.value = Array.isArray(res.data) ? res.data : []
  } catch (error) {
    console.error('납부내역 조회 실패:', error)
    payList.value = []
    alert('납부내역 조회 중 오류가 발생했습니다.')
  }
}

// ===== 초기화 버튼 =====
const resetFilters = () => {
  filters.value.payId = ''
  filters.value.startDate = null
  filters.value.endDate = null
}

// ===== PDF 출력 =====
const exportPDF = () => {
  alert('PDF 출력 기능은 추후 구현 예정')
}

// ===== 엑셀 다운로드 =====
const exportExcel = () => {
  alert('엑셀 다운로드 기능은 추후 구현 예정')
}

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

.date-group {
  display: flex;
  flex-direction: column;
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

/* 테이블 헤더 */
::v-deep(.p-datatable-thead > tr > th) {
  background: #f9fafb;
  border-right: 1px solid #e5e7eb;
  text-align: center;
  font-weight: 600;
  padding: 10px;
}

/* 본문 */
::v-deep(.p-datatable-tbody > tr > td) {
  border-right: 1px solid #f0f0f0;
  text-align: center;
  padding: 8px 10px;
}

/* 반응형 */
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
</style>
