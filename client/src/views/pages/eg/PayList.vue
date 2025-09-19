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
        <!-- 결제번호 -->
        <div class="form-group">
          <label class="form-label">결제번호</label>
          <InputText
            v-model="filters.payId"
            placeholder="결제번호 입력"
            class="input-text"
          />
        </div>

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
        <Column field="pay_id" header="결제번호" style="width:120px;" />
        <Column field="pay_date" header="결제일자" style="width:120px;" />
        <Column field="previous_balance" header="이월잔액" style="width:120px; text-align:right;">
          <template #body="slotProps">
            {{ formatCurrency(slotProps.data.previous_balance) }}
          </template>
        </Column>
        <Column field="sales_amount" header="매출" style="width:120px; text-align:right;">
          <template #body="slotProps">
            {{ formatCurrency(slotProps.data.sales_amount) }}
          </template>
        </Column>
        <Column field="payment_amount" header="수금" style="width:120px; text-align:right;">
          <template #body="slotProps">
            {{ formatCurrency(slotProps.data.payment_amount) }}
          </template>
        </Column>
        <Column field="note" header="적요" style="width:200px;" />
        <Column field="final_balance" header="최종잔액" style="width:120px; text-align:right;">
          <template #body="slotProps">
            {{ formatCurrency(slotProps.data.final_balance) }}
          </template>
        </Column>
      </DataTable>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Calendar from 'primevue/calendar'

// 검색 조건 데이터
const filters = ref({
  payId: '',
  startDate: null,
  endDate: null
})

// 샘플 납부내역 데이터
const payList = ref([
  {
    pay_id: 'AB-002',
    pay_date: '2025-09-15',
    previous_balance: 1000000,
    sales_amount: 3500000,
    payment_amount: 2000000,
    note: '보라원두 반품',
    final_balance: 2500000
  }
])

// 금액 포맷팅
const formatCurrency = (value) => {
  return value.toLocaleString('ko-KR') + ' 원'
}

// 조회 버튼 클릭
const searchPayments = () => {
  console.log('검색 조건:', filters.value)
  alert('조회 기능은 추후 API 연동 예정')
}

// 초기화 버튼
const resetFilters = () => {
  filters.value.payId = ''
  filters.value.startDate = null
  filters.value.endDate = null
}

// PDF 출력
const exportPDF = () => {
  alert('PDF 출력 기능은 추후 구현 예정')
}

// 엑셀 다운로드
const exportExcel = () => {
  alert('엑셀 다운로드 기능은 추후 구현 예정')
}
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
