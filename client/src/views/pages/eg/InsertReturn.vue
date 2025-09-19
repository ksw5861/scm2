<template>
  <div class="return-register">
    <!-- 헤더 -->
    <div class="header">
      <h2>반품 등록 (판매처)</h2>
      <div class="header-actions">
        <Button label="조회" icon="pi pi-search" class="p-button-outlined" @click="fetchShippedOrders" />
        <Button label="등록" icon="pi pi-plus" class="p-button-success" @click="saveReturn" />
      </div>
    </div>

    <div class="content-wrapper">
      <!-- 좌측 출고된 주문 목록 -->
      <div class="left-panel">
        <DataTable
          :value="shippedOrderList"
          paginator
          :rows="7"
          resizableColumns
          columnResizeMode="fit"
          class="custom-table"
          selectionMode="single"
          v-model:selection="selectedOrder"
          @rowSelect="loadOrderDetails"
        >
          <Column field="prod_id" header="제품코드" style="width:120px;" />
          <Column field="prod_name" header="제품명" style="width:180px;" />
          <Column field="prod_spec" header="규격" style="width:120px; text-align:center;" />
          <Column field="prod_unit" header="단위" style="width:100px; text-align:center;" />
          <Column field="prod_price" header="제품가격" style="width:120px; text-align:right;">
            <template #body="slotProps">
              {{ formatCurrency(slotProps.data.prod_price) }}
            </template>
          </Column>
          <Column field="order_qty" header="주문수량" style="width:100px; text-align:center;" />
          <Column field="total" header="합계" style="width:130px; text-align:right;">
            <template #body="slotProps">
              {{ formatCurrency(slotProps.data.total) }}
            </template>
          </Column>
        </DataTable>
      </div>

      <!-- 우측 반품 등록 영역 -->
      <div class="right-panel">
        <DataTable
          :value="returnDetailList"
          paginator
          :rows="7"
          resizableColumns
          columnResizeMode="fit"
          class="custom-table"
        >
          <Column field="prod_id" header="제품코드" style="width:120px;" />
          <Column field="prod_name" header="제품명" style="width:180px;" />
          <Column field="prod_spec" header="규격" style="width:120px; text-align:center;" />
          <Column field="prod_unit" header="단위" style="width:100px; text-align:center;" />

          <Column field="prod_price" header="제품가격" style="width:120px; text-align:right;">
            <template #body="slotProps">
              {{ formatCurrency(slotProps.data.prod_price) }}
            </template>
          </Column>

          <Column field="order_qty" header="주문수량" style="width:100px; text-align:center;" />

          <!-- 반품수량 입력 -->
          <Column header="반품수량" style="width:100px; text-align:center;">
            <template #body="slotProps">
              <InputNumber
                v-model="slotProps.data.return_qty"
                :min="0"
                @input="calculateRowTotal(slotProps.data)"
                showButtons
                buttonLayout="horizontal"
                decrementButtonClass="p-button-outlined"
                incrementButtonClass="p-button-outlined"
              />
            </template>
          </Column>

          <!-- 합계 -->
          <Column header="합계" style="width:130px; text-align:right;">
            <template #body="slotProps">
              {{ formatCurrency(slotProps.data.return_total) }}
            </template>
          </Column>

          <!-- 사유 입력 -->
          <Column header="사유" style="width:150px;">
            <template #body="slotProps">
              <InputText v-model="slotProps.data.reason" placeholder="반품 사유 입력" />
            </template>
          </Column>
        </DataTable>

        <!-- 총 반품합계 -->
        <div class="summary">
          총 반품합계 : <strong>{{ formatCurrency(totalReturnAmount) }}</strong>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import InputNumber from 'primevue/inputnumber'
import InputText from 'primevue/inputtext'

// 좌측 출고된 주문 목록 (샘플)
const shippedOrderList = ref([
  { prod_id: 'AB-012', prod_name: '다크블렌드', prod_spec: '1Box', prod_unit: 'Box(4개)', prod_price: 480000, order_qty: 2, total: 960000 },
  { prod_id: 'AB-013', prod_name: '온컵300미리', prod_spec: '100ea 줄', prod_unit: 'Box(30줄)', prod_price: 50000, order_qty: 4, total: 200000 },
  { prod_id: 'AB-014', prod_name: '냉컵300미리', prod_spec: '100ea 줄', prod_unit: 'Box(30줄)', prod_price: 50000, order_qty: 4, total: 200000 },
  { prod_id: 'AB-015', prod_name: '냉컵500미리', prod_spec: '100ea 줄', prod_unit: 'Box(30줄)', prod_price: 50000, order_qty: 4, total: 200000 }
])

// 선택된 주문
const selectedOrder = ref(null)

// 반품 상세 목록
const returnDetailList = ref([])

// 선택된 주문의 제품 불러오기
const loadOrderDetails = () => {
  if (!selectedOrder.value) return
  returnDetailList.value = [ { ...selectedOrder.value, return_qty: 0, return_total: 0, reason: '' } ]
}

// 금액 계산
const calculateRowTotal = (row) => {
  row.return_total = row.return_qty * row.prod_price
}

// 총 반품합계
const totalReturnAmount = computed(() => {
  return returnDetailList.value.reduce((acc, item) => acc + (item.return_total || 0), 0)
})

// 금액 포맷
const formatCurrency = (value) => {
  return value ? value.toLocaleString('ko-KR') + ' 원' : '0 원'
}

// 조회 버튼
const fetchShippedOrders = () => {
  console.log('출고된 주문 목록 조회 API 호출')
}

// 저장 버튼
const saveReturn = () => {
  console.log('반품 등록 API 호출', returnDetailList.value)
}
</script>

<style scoped>
.return-register {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.content-wrapper {
  display: flex;
  gap: 15px;
}

.left-panel,
.right-panel {
  flex: 1;
}

.summary {
  text-align: right;
  margin-top: 10px;
  font-size: 1.1rem;
  font-weight: bold;
}

.custom-table {
  width: 100%;
  font-size: 0.95rem;
}

::v-deep(.p-datatable-thead > tr > th) {
  border-right: 1px solid #e5e7eb;
  background: #f9fafb;
  font-weight: 600;
  text-align: center !important;
}

::v-deep(.p-datatable-tbody > tr > td) {
  border-right: 1px solid #f0f0f0;
  text-align: center;
  padding: 8px 10px;
}

/* 반응형 */
@media (max-width: 768px) {
  .content-wrapper {
    flex-direction: column;
  }
}
</style>
