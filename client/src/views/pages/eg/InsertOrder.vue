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
      
      <!-- 제품단가 -->
      <Column field="prodUnitPrice" header="제품단가">
        <template #body="{ data }">
          <div class="text-right">{{ formatCurrency(data.prodUnitPrice) }}</div>
        </template>
      </Column>

      <!-- 주문수량 -->
      <template>
  <Column header="주문수량" style="text-align: center;">
    <template #body="{ data }">
      <InputNumber
        v-model="data.orderQty"
        :min="0"
        @input="data.orderQty = $event.value" 
        showButtons
        buttonLayout="horizontal"
        decrementButtonClass="p-button-outlined p-button-sm"
        incrementButtonClass="p-button-outlined p-button-sm"
        :inputStyle="{ width: '20px', textAlign: 'center', padding: '4px' }"
      />
    </template>
  </Column>

</template>

      <!-- 합계 -->
      <Column header="합계">
        <template #body="{ data }">
          <div class="text-right">{{ formatCurrency(data.total) }}</div>
        </template>
      </Column>
    </DataTable>

    <!-- 제품 검색 모달 -->
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
        @rowClick="handleSelect" 
      >
        <Column field="prodId" header="제품번호" />
        <Column field="prodName" header="제품명" />
        <Column field="spec" header="규격" />
        <Column field="unit" header="단위" />
        <Column field="prodUnitPrice" header="제품가격" />
      </DataTable>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import InputNumber from 'primevue/inputnumber'
import Dialog from 'primevue/dialog'
import { useAppToast } from '@/composables/useAppToast'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const { toast } = useAppToast()

// -----------------------------
// 상태 관리
// -----------------------------
const isShowModal = ref(false)          // 모달 표시 여부
const productList = ref([])             // 제품 목록
const selectedProduct = ref(null)       // 선택된 제품
const orderDetailList = ref([])         // 주문 상세 목록
const deliveryDate = ref('2025-10-01')  // 납기일자
const returnPrice = ref(1)              // 반품 관련 가격
const returnStatus = ref('대기')        // 반품 상태

// -----------------------------
// 계산 & 유틸
// -----------------------------
const totalAmount = computed(() =>
  orderDetailList.value.reduce((sum, item) => sum + (item.total || 0), 0)
)

const formatCurrency = (value) =>
  (value || 0).toLocaleString('ko-KR') + ' 원'

const calculateRowTotal = (row) => {
  row.total =
    (Number(row.orderQty) || 0) * (Number(row.prodUnitPrice) || 0)
  console.log('합계 계산:', row.total)
}

// -----------------------------
// API & 데이터 핸들링
// -----------------------------
// 제품 목록 조회
const fetchProducts = async () => {
  try {
    const { data } = await axios.get('/api/products', {
      params: { page: 1, pageSize: 50 }
    })
    console.log('제품 목록 API 응답:', data)
    productList.value = data.items || []
  } catch (err) {
    console.error('제품 목록 조회 오류:', err)
    productList.value = []
  }
}

// 제품 선택 시 주문 상세 목록에 추가
const handleSelect = () => {
  if (!selectedProduct.value) return

  const product = selectedProduct.value

  // 중복 체크
  const isDuplicate = orderDetailList.value.some(
    (item) => item.prodId === product.prodId
  )
  if (isDuplicate) {
    toast('warn', '중복 제품', '이미 추가된 제품입니다.')
    return
  }

  // 초기 행 데이터 추가
  orderDetailList.value.push({
    odetailId: null,
    prodId: product.prodId,
    prodName: product.prodName,
    spec: product.spec || '-',
    unit: product.unit || '-',
    prodUnitPrice: product.prodUnitPrice || 0,
    orderQty: 1,
    prodStatus: '대기',
    total: product.prodUnitPrice || 0
  })

  selectedProduct.value = null
  isShowModal.value = false
}

// 주문 저장
const saveOrder = async () => {
  console.log('✅ saveOrder 실행됨')

  const payload = {
    orderDate: new Date().toISOString().slice(0, 10),
    deliveryDate: deliveryDate.value,
    totalPrice: totalAmount.value,
    status: '대기',      // 서버에서 기본값 처리 가능
    payStatus: '대기',   // 서버에서 기본값 처리 가능
    vendorId: userStore.code,
    returnPrice: returnPrice.value || 1,
    returnStatus: returnStatus.value || '대기',
    details: JSON.parse(JSON.stringify(orderDetailList.value))
  }

  console.log('📤 전송되는 데이터:', payload)

  try {
    const { data } = await axios.post('/api/insertorder', payload)
    console.log('📥 응답 데이터:', data)

    if (data.status === 'success') {
      toast(
        'success',
        '주문 등록',
        data.message || '주문이 성공적으로 등록되었습니다.'
      )
      orderDetailList.value = []
    } else {
      toast(
        'warn',
        '등록 실패',
        data.message || '주문 등록에 실패했습니다.'
      )
    }
  } catch (err) {
    console.error('❌ API 오류:', err)
    toast('error', '서버 오류', '주문 등록 중 서버 오류가 발생했습니다.')
  }
}

// -----------------------------
// 라이프사이클 & watch
// -----------------------------
onMounted(fetchProducts)

// 주문수량 변경 → 자동 합계 계산
watch(
  () => orderDetailList.value,
  (newVal) => {
    newVal.forEach((row) => calculateRowTotal(row))
  },
  { deep: true }
)
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
