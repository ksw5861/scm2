<template>
  <div class="order-register">
    <!-- 헤더 -->
    <div class="header">
      <h2>주문 등록 (판매처)</h2>
      <div class="header-actions">
        <Button 
          label="제품 조회" 
          icon="pi pi-search" 
          class="p-button-outlined" 
          @click="isShowModal = true" 
        />
        <Button 
          label="등록" 
          icon="pi pi-plus" 
          class="p-button-success" 
          @click="saveOrder" 
        />
      </div>
    </div>

    <!-- 요약 -->
    <div class="summary">
      <span class="total-amount">
        총 주문합계: <strong>{{ formatCurrency(totalAmount) }}</strong>
      </span>
      <span class="due-date">
        납기일자: <strong>{{ deliveryDate }}</strong>
      </span>
    </div>

    <!-- 주문 상세 테이블 -->
    <DataTable
      :value="orderDetailList"
      paginator
      :rows="10"
      responsiveLayout="scroll"
      resizableColumns
      columnResizeMode="fit"
      class="order-table"
    >
      <Column field="prodId" header="제품코드" />
      <Column field="prodName" header="제품명" />
      <Column field="spec" header="규격" />
      <Column field="unit" header="단위" />

      <Column field="prodPrice" header="제품가격">
        <template #body="{ data }">
          <div class="text-right">{{ formatCurrency(data.prodPrice) }}</div>
        </template>
      </Column>

      <!-- 주문수량 -->
      <Column header="주문수량" style="text-align: center;">
  <template #body="{ data }">
    <div>
      <InputNumber
        v-model="data.orderQty"
        :min="0"
        @input="calculateRowTotal(data)"
        showButtons
        buttonLayout="horizontal"
        decrementButtonClass="p-button-outlined p-button-sm"
        incrementButtonClass="p-button-outlined p-button-sm"
        :inputStyle="{ width: '20px', textAlign: 'center', padding: '4px' }"
      />
    </div>
  </template>
</Column>


      <Column header="합계">
        <template #body="{ data }">
          <div class="text-right">{{ formatCurrency(data.total) }}</div>
        </template>
      </Column>
    </DataTable>

    <!-- 🔹 PrimeVue Dialog 기반 모달 -->
    <Dialog
      v-model:visible="isShowModal"
      header="제품 검색"
      :style="{ width: '600px' }"
      modal
    >
      <DataTable
        :value="productList"
        paginator
        :rows="5"
        responsiveLayout="scroll"
        selectionMode="single"
        v-model:selection="selectedProduct"
        @rowDblclick="handleSelect"
      >
        <Column field="prodId" header="제품번호" />
        <Column field="prodName" header="제품명" />
        <Column field="spec" header="규격" />
        <Column field="unit" header="단위" />
      </DataTable>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import InputNumber from 'primevue/inputnumber'
import Dialog from 'primevue/dialog'
import { useAppToast } from '@/composables/useAppToast'

const { toast } = useAppToast()

const isShowModal = ref(false)
const productList = ref([])
const selectedProduct = ref(null)

const orderDetailList = ref([
  {
    prodId: 'AB-012',
    prodName: '아라비카원두',
    prodSpec: '1 kg 팩',
    prodUnit: 'Box(24팩)',
    prodPrice: 480000,
    orderQty: 2,
    total: 960000
  }
])

const deliveryDate = ref('2025-11-15')

const totalAmount = computed(() =>
  orderDetailList.value.reduce((sum, item) => sum + (item.total || 0), 0)
)

const formatCurrency = (value) =>
  (value || 0).toLocaleString('ko-KR') + ' 원'

const calculateRowTotal = (row) => {
  row.total = row.orderQty * row.prodPrice
}

const fetchProducts = async () => {
  try {
    const { data } = await axios.get('/api/products', {
      params: { page: 1, pageSize: 50 }
    })
    console.log(data)
    productList.value = Array.isArray(data) ? data : data.items || []
  } catch (err) {
    console.error('제품 목록 조회 오류:', err)
    productList.value = []
  }
}

const handleSelect = () => {
  if (!selectedProduct.value) return
  const product = selectedProduct.value
  orderDetailList.value.push({
    prodId: product.prodId,
    prodName: product.prodName,
    spec: product.spec || '-',
    unit: product.unit || '-',
    prodPrice: product.prodPrice || 0,
    orderQty: 1,
    total: product.prodPrice || 0
  })
  isShowModal.value = false
}

const saveOrder = async () => {
  const payload = {
    orderDate: new Date().toISOString().slice(0, 10),
    deliveryDate: deliveryDate.value,
    totalPrice: totalAmount.value,
    details: orderDetailList.value
  }
  try {
    const { data } = await axios.post('/api/insertorder', payload)
    if (data.result === 'success') {
      toast('success', '주문 등록 성공', '주문 등록 성공하였습니다.')
    } else {
      toast('error', '주문 등록 실패', '주문 등록 실패하였습니다.')
    }
  } catch (err) {
    console.error('API 오류:', err)
    toast('error', '주문 등록 오류', '오류 발생하였습니다.')
  }
}

onMounted(fetchProducts)
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
.order-table {
  width: 100%;
  font-size: 0.95rem;
}
.text-right {
  text-align: right;
  padding-right: 6px;
}

/* InputNumber 박스 크기 */
.p-inputnumber {
  display: flex;
  align-items: center;
  max-width: 100%;
}

/* 버튼 크기 */
.p-inputnumber-button {
  width: 28px !important;
  height: 28px !important;
  padding: 0 !important;
}


</style>
