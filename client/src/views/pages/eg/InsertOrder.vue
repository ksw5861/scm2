<template>
  <div class="order-register">
    <!-- 헤더 -->
    <div class="header">
      <h2>주문 등록 (판매처)</h2>
      <div class="header-actions">
        <!-- 🔹 제품 조회 버튼 -->
        <Button 
          label="제품 조회" 
          icon="pi pi-search" 
          class="p-button-outlined" 
          @click="openProductModal" 
        />
        <Button
          label="등록"
          icon="pi pi-plus"
          class="p-button-success"
          @click="saveOrder"
        />
      </div>
    </div>

    <!-- 총 주문합계 + 납기일자 -->
    <div class="summary">
      <span class="total-amount">총 주문합계: <strong>{{ formatCurrency(totalAmount) }}</strong></span>
      <span class="due-date">납기일자: <strong>{{ deliveryDate }}</strong></span>
    </div>

    <!-- 제품 테이블 -->
    <DataTable
      :value="orderDetailList"
      paginator
      :rows="10"
      responsiveLayout="scroll"
      resizableColumns
      columnResizeMode="fit"
      class="order-table"
    >
      <Column field="prod_id" header="제품코드" style="width:120px;" />
      <Column field="prod_name" header="제품명" style="width:200px;" />
      <Column field="prod_spec" header="규격" style="width:120px;" />
      <Column field="prod_unit" header="단위" style="width:100px;" />

      <!-- 제품가격 -->
      <Column field="prod_price" header="제품가격" style="width:120px;">
        <template #body="slotProps">
          <div class="text-right">{{ formatCurrency(slotProps.data.prod_price) }}</div>
        </template>
      </Column>

      <!-- 주문수량 -->
      <Column header="주문수량" style="width:100px;">
        <template #body="slotProps">
          <div class="qty-input-wrapper">
            <InputNumber
              v-model="slotProps.data.order_qty"
              :min="0"
              @input="calculateRowTotal(slotProps.data)"
              showButtons
              buttonLayout="horizontal"
              decrementButtonClass="p-button-outlined"
              incrementButtonClass="p-button-outlined"
            />
          </div>
        </template>
      </Column>

      <!-- 합계 -->
      <Column header="합계" style="width:130px;">
        <template #body="slotProps">
          <div class="text-right">{{ formatCurrency(slotProps.data.total) }}</div>
        </template>
      </Column>
    </DataTable>

    <!-- 🔹 제품 검색 모달 추가 -->
    <Modal
      :visible="isShowModal"
      title="제품 검색"
      idField="prodId"
      :columns="[
        { key: 'prodId', label: '제품번호' },
        { key: 'prodName', label: '제품명' }
      ]"
      :fetchData="fetchWarehouseData"
      :page-size="5"
      @select="handleSelect"
      @close="isShowModal = false"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import axios from 'axios'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import InputNumber from 'primevue/inputnumber'
import { useAppToast } from '@/composables/useAppToast';

const { toast } = useAppToast();

// ===== 모달 상태 관리 =====
const isShowModal = ref(false)
const openProductModal = () => {
  isShowModal.value = true
}

// ===== 주문 상세 데이터 (예시 데이터) =====
const orderDetailList = ref([
  { prod_id: 'AB-012', prod_name: '아라비카원두', prod_spec: '1 kg 팩', prod_unit: 'Box(24팩)', prod_price: 480000, order_qty: 2, total: 960000 }
])

// ===== 납기일자 =====
const deliveryDate = ref('2025-11-15')

// ===== 총 주문 금액 =====
const totalAmount = computed(() => {
  return orderDetailList.value.reduce((acc, item) => acc + (item.total || 0), 0)
})

// ===== 금액 포맷팅 =====
const formatCurrency = (value) => {
  return value.toLocaleString('ko-KR') + ' 원'
}

// ===== 수량 변경 시 자동 합계 계산 =====
const calculateRowTotal = (row) => {
  row.total = row.order_qty * row.prod_price
}

// ===== 제품 목록 조회 (모달에서 사용) =====
const fetchWarehouseData = async (page, pageSize) => {
  try {
    const { data } = await axios.get('/api/products', {
      params: {
        page,
        pageSize
      }
    })
    return {
      total: data.totalCount,
      data: data.items
    }
  } catch (error) {
    console.error('제품 목록 조회 오류:', error)
    return { total: 0, data: [] }
  }
}

// ===== 모달에서 제품 선택 시 실행 =====
const handleSelect = (selectedProduct) => {
  // 주문 테이블에 추가
  orderDetailList.value.push({
    prod_id: selectedProduct.prodId,
    prod_name: selectedProduct.prodName,
    prod_spec: selectedProduct.prodSpec || '-',
    prod_unit: selectedProduct.prodUnit || '-',
    prod_price: selectedProduct.prodPrice || 0,
    order_qty: 1,
    total: selectedProduct.prodPrice || 0
  })
  isShowModal.value = false
}

// ===== 주문 등록 (API 호출) =====
const saveOrder = async () => {
  const orderPayload = {
    order_date: new Date().toISOString().slice(0, 10),
    delivery_date: deliveryDate.value,
    total_price: totalAmount.value,
    details: orderDetailList.value
  }

  try {
    const response = await axios.post('/api/insertorder', orderPayload)
    console.log('주문 저장 API 응답:', response.data)

    if (response.data.result === 'success') {
      toast("success", "주문 등록 성공", "주문 등록 성공하였습니다.")
    } else {
      toast("error", "주문 등록 실패", "주문 등록 실패하였습니다.")
    }
  } catch (error) {
    console.error('API 호출 오류:', error)
    toast("error", "주문 등록 오류", "오류 발생하였습니다.")
  }
}
</script>

<style scoped>
.order-register {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.summary {
  display: flex;
  justify-content: flex-end;
  gap: 30px;
  align-items: center;
  margin-bottom: 15px;
  padding: 10px;
  border: 1px solid #dcdcdc;
  border-radius: 8px;
  background-color: #f9f9f9;
  font-size: 1.1rem;
}

.total-amount {
  font-weight: bold;
  color: #007ad9;
}

.total-amount strong {
  font-size: 1.2rem;
}

.due-date {
  font-weight: bold;
  color: #444;
}

.p-button {
  margin-left: 10px;
}

/* === 테이블 전체 스타일 === */
.order-table {
  width: 100%;
  font-size: 0.95rem;
}

/* 헤더 중앙정렬 */
::v-deep(.p-datatable-column-header-content) {
  display: flex;
  justify-content: center;
  align-items: center;
  text-align: center !important;
}

/* 헤더 컬럼 구분선 */
::v-deep(.p-datatable-thead > tr > th) {
  border-right: 1px solid #e5e7eb;
  background: #f9fafb;
  font-weight: 600;
  padding: 10px 0;
  vertical-align: middle;
}

/* 본문 컬럼 구분선 */
::v-deep(.p-datatable-tbody > tr > td) {
  border-right: 1px solid #f0f0f0;
  text-align: center;
  vertical-align: middle;
  padding: 8px 10px;
}

/* 금액 컬럼만 오른쪽 정렬 */
.text-right {
  text-align: right;
  padding-right: 6px;
}

/* === 수량 입력란 최적화 === */
.qty-input-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

::v-deep(.p-inputnumber) {
  width: 70px !important;
  height: 36px !important;
  display: flex;
  align-items: center;
  border: 1px solid #dcdcdc;
  border-radius: 6px;
  background: none;
}

::v-deep(.p-inputnumber input) {
  width: 34px !important;
  height: 34px !important;
  text-align: center;
  font-size: 0.9rem;
  padding: 0 !important;
  border: none;
  outline: none;
}

/* 증감 버튼 */
::v-deep(.p-inputnumber-button) {
  width: 18px !important;
  height: 36px !important;
  padding: 0 !important;
  border: none;
  background: transparent;
}

::v-deep(.p-inputnumber-button .pi) {
  font-size: 0.8rem !important;
  color: #555;
}

/* 반응형 */
@media (max-width: 768px) {
  .order-table {
    font-size: 0.9rem;
  }

  .p-datatable-wrapper {
    overflow-x: auto;
  }
}
</style>
