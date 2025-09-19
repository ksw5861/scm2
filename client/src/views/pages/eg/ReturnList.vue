<template>
  <div class="return-search">
    <!-- 헤더 -->
    <div class="header">
      <h2>반품 조회 (판매처)</h2>
      <div class="header-actions">
        <Button label="초기화" icon="pi pi-refresh" class="p-button-outlined" @click="resetFilters" />
        <Button label="조회" icon="pi pi-search" class="p-button-success" @click="searchReturns" />
      </div>
    </div>

    <!-- 검색 필터 -->
    <div class="filter-section">
      <div class="filter-item">
        <label>반품일자</label>
        <Calendar v-model="filters.startDate" placeholder="시작일" dateFormat="yy-mm-dd" showIcon />
        <span>~</span>
        <Calendar v-model="filters.endDate" placeholder="종료일" dateFormat="yy-mm-dd" showIcon />
      </div>

      <div class="filter-item">
        <label>제품명</label>
        <InputText v-model="filters.productName" placeholder="제품명 입력" />
      </div>

      <div class="filter-item">
        <label>반품상태</label>
        <Dropdown
          v-model="filters.returnStatus"
          :options="statusOptions"
          optionLabel="label"
          optionValue="value"
          placeholder="선택"
        />
      </div>

      <div class="filter-item">
        <label>반품번호</label>
        <InputText v-model="filters.returnNo" placeholder="반품번호 입력" />
      </div>
    </div>

    <!-- 다운로드 버튼 -->
    <div class="export-buttons">
      <Button label="PDF 출력" icon="pi pi-file-pdf" class="p-button-danger p-button-sm" @click="exportPDF" />
      <Button label="엑셀 다운로드" icon="pi pi-file-excel" class="p-button-success p-button-sm" @click="exportExcel" />
    </div>

    <!-- 반품 목록 테이블 -->
    <DataTable
      :value="returnList"
      paginator
      :rows="10"
      responsiveLayout="scroll"
      resizableColumns
      columnResizeMode="fit"
      class="custom-table"
    >
      <Column field="return_date" header="반품일자" style="width:120px; text-align:center;" />
      <Column field="prod_id" header="제품코드" style="width:120px; text-align:center;" />
      <Column field="prod_name" header="제품명" style="width:180px;" />
      <Column field="prod_spec" header="규격" style="width:100px; text-align:center;" />
      <Column field="prod_unit" header="단위" style="width:100px; text-align:center;" />

      <Column field="prod_price" header="제품가격" style="width:120px; text-align:right;">
        <template #body="slotProps">
          {{ formatCurrency(slotProps.data.prod_price) }}
        </template>
      </Column>

      <Column field="return_qty" header="반품수량" style="width:90px; text-align:center;" />

      <Column field="return_total" header="제품가격" style="width:120px; text-align:right;">
        <template #body="slotProps">
          {{ formatCurrency(slotProps.data.return_total) }}
        </template>
      </Column>

      <Column field="reason" header="사유" style="width:150px;" />
      <Column field="status" header="상태" style="width:120px; text-align:center;" />

      <Column header="" style="width:130px; text-align:center;">
        <template #body="slotProps">
          <Button label="제품 상태 확인" class="p-button-outlined p-button-sm" @click="checkProductStatus(slotProps.data)" />
        </template>
      </Column>
    </DataTable>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Calendar from 'primevue/calendar'
import Dropdown from 'primevue/dropdown'
import InputText from 'primevue/inputtext'

// 검색 필터
const filters = ref({
  startDate: null,
  endDate: null,
  productName: '',
  returnStatus: null,
  returnNo: ''
})

const statusOptions = [
  { label: '대기', value: 'WAIT' },
  { label: '승인', value: 'APPROVED' },
  { label: '승인완료', value: 'COMPLETED' }
]

// 샘플 데이터
const returnList = ref([
  {
    return_date: '2025-09-15',
    prod_id: 'AB-012',
    prod_name: '아라비카원두',
    prod_spec: '1 kg 팩',
    prod_unit: 'Box(24팩)',
    prod_price: 480000,
    return_qty: 2,
    return_total: 960000,
    reason: '브라질원두타입',
    status: '승인대기'
  },
  {
    return_date: '2025-09-15',
    prod_id: 'AB-013',
    prod_name: '온컵300미리',
    prod_spec: '100ea 줄',
    prod_unit: 'Box(30줄)',
    prod_price: 50000,
    return_qty: 4,
    return_total: 200000,
    reason: '로그불량',
    status: '승인완료'
  }
])

const formatCurrency = (value) => {
  return value ? value.toLocaleString('ko-KR') + ' 원' : '0 원'
}

const searchReturns = () => {
  console.log('반품 조회 API 호출', filters.value)
}

const resetFilters = () => {
  filters.value = {
    startDate: null,
    endDate: null,
    productName: '',
    returnStatus: null,
    returnNo: ''
  }
}

const exportPDF = () => console.log('PDF 출력 기능 호출')
const exportExcel = () => console.log('엑셀 다운로드 기능 호출')
const checkProductStatus = (data) => console.log('제품 상태 확인 모달', data)
</script>

<style scoped>
.return-search {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.filter-section {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 15px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.export-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-bottom: 10px;
}

/* 테이블 스타일 */
.custom-table {
  width: 100%;
  font-size: 0.95rem;
}

::v-deep(.p-datatable-thead > tr > th) {
  border-right: 1px solid #e5e7eb;
  background: #f9fafb;
  font-weight: 600;
  text-align: center !important;
  padding: 10px 0;
}

::v-deep(.p-datatable-tbody > tr > td) {
  border-right: 1px solid #f0f0f0;
  text-align: center;
  padding: 8px 10px;
}

/* 반응형 */
@media (max-width: 768px) {
  .filter-section {
    flex-direction: column;
  }

  .custom-table {
    font-size: 0.9rem;
  }

  .p-datatable-wrapper {
    overflow-x: auto;
  }
}
</style>
