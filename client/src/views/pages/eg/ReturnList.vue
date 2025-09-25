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
        <DatePicker
          v-model="filters.startDate"
          placeholder="시작일"
          dateFormat="yy-mm-dd"
          showIcon
        />
        <span>~</span>
        <DatePicker
          v-model="filters.endDate"
          placeholder="종료일"
          dateFormat="yy-mm-dd"
          showIcon
        />
      </div>

      <div class="filter-item">
        <label>제품명</label>
        <InputText v-model="filters.prodName" placeholder="제품명 입력" />
      </div>

      <div class="filter-item">
        <label>반품상태</label>
        <Select
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
      <Column field="returnDate" header="반품일자" />
      <Column field="prodId" header="제품코드" />
      <Column field="prodName" header="제품명" />
      <Column field="spec" header="규격" />
      <Column field="unit" header="단위" />

      <!-- ✅ 제품 단가 컬럼 -->
      <Column field="prodUnitPrice" header="제품단가">
        <template #body="slotProps">
          {{ formatCurrency(slotProps.data.prodUnitPrice) }}
        </template>
      </Column>

      <Column field="returnQty" header="반품수량" />

      <!-- ✅ 총액 계산 (단가 × 수량) -->
      <Column field="returnTotal" header="총액">
        <template #body="slotProps">
          {{ formatCurrency(slotProps.data.prodUnitPrice * slotProps.data.returnQty) }}
        </template>
      </Column>

      <Column field="returnWhy" header="사유" />
      <Column field="returnStatus" header="상태" />

      <!-- 제품 상태 확인 버튼 -->
      <Column header="" style="width:130px; text-align:center;">
        <template #body="slotProps">
          <Button
            label="제품 상태 확인"
            class="p-button-outlined p-button-sm"
            @click="checkProductStatus(slotProps.data)"
          />
        </template>
      </Column>
    </DataTable>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

// PrimeVue 컴포넌트
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import DatePicker from 'primevue/datepicker'
import Select from 'primevue/select'
import InputText from 'primevue/inputtext'

// 날짜 포맷 함수 (YYYY-MM-DD)
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 검색 필터
const filters = ref({
  startDate: null,
  endDate: null,
  prodName: '',
  returnStatus: '',
  returnNo: ''
})

const statusOptions = [
  { label: '대기', value: '대기' },
  { label: '반려', value: '반려' },
  { label: '승인완료', value: '승인완료' }
]

// 반품 목록 데이터
const returnList = ref([])

// 통화 포맷 함수
const formatCurrency = (value) => {
  return value ? value.toLocaleString('ko-KR') + ' 원' : '0 원'
}

// 반품 목록 조회
const searchReturns = async () => {
  try {
    const { data } = await axios.get('/api/returnlist', {
      params: {
        startDate: filters.value.startDate,
        endDate: filters.value.endDate,
        returnStatus: filters.value.returnStatus,
        prodName: filters.value.prodName,
        returnNo: filters.value.returnNo  
      }
    })

    console.log('반품 목록 응답:', data)

    // 서버 데이터 가공
    returnList.value = (Array.isArray(data) ? data : (data.items || [])).map(item => ({
      ...item,
      // ✅ 단가 컬럼 매핑
      prodUnitPrice: item.prodUnitPrice || 0,
      // ✅ 날짜를 YYYY-MM-DD 형식으로 변환
      returnDate: formatDate(item.returnDate),
      // ✅ 상태값 REQUEST → 대기 치환
      returnStatus: item.returnStatus === 'REQUEST' ? '대기' : item.returnStatus
    }))
  } catch (err) {
    console.error('반품 목록 조회 오류:', err)
  }
}

// 필터 초기화
const resetFilters = () => {
  filters.value = {
    startDate: null,
    endDate: null,
    prodName: '',
    returnStatus: '', 
    returnNo: ''
  }
}

// PDF 및 엑셀 다운로드
const exportPDF = () => console.log('PDF 출력 기능 호출')
const exportExcel = () => console.log('엑셀 다운로드 기능 호출')

// 제품 상태 확인 모달
const checkProductStatus = (data) => {
  console.log('제품 상태 확인 모달', data)
}

// 페이지 진입 시 자동으로 반품 목록 조회
onMounted(() => {
  searchReturns();
});
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
