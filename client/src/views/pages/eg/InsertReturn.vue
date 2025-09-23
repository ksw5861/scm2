<template>
  <div class="return-register">
    <!-- 헤더 -->
    <div class="header">
      <h2>반품 등록 (판매처)</h2>
      <div class="header-actions">
        <Button label="조회" icon="pi pi-search" class="p-button-outlined" @click="openOrderModal" />
        <Button label="등록" icon="pi pi-plus" class="p-button-success" @click="saveReturn" />
      </div>
    </div>

    <div class="content-wrapper">
      <!-- 좌측: 선택된 주문의 상세 품목 -->
      <div class="left-panel">
        <DataTable
          :value="orderDetailList"
          paginator
          :rows="7"
          resizableColumns
          columnResizeMode="fit"
          class="custom-table"
          selectionMode="single"
          v-model:selection="selectedDetail"
          @rowSelect="addToReturnList"
          :emptyMessage="selectedOrderId ? '주문 상세를 클릭하세요.' : '상단 조회로 주문을 선택하세요.'"
        >
          <Column field="prodId" header="제품코드" style="width:120px;" />
          <Column field="prodName" header="제품명" style="width:180px;" />
          <Column field="spec" header="규격" style="width:120px; text-align:center;" />
          <Column field="unit" header="단위" style="width:100px; text-align:center;" />
          <Column field="prodPrice" header="제품가격" style="width:120px; text-align:right;">
            <template #body="{ data }">{{ formatCurrency(data.prodPrice) }}</template>
          </Column>
          <Column field="orderQty" header="주문수량" style="width:100px; text-align:center;" />
          <Column header="합계" style="width:130px; text-align:right;">
            <template #body="{ data }">{{ formatCurrency(data.orderQty * data.prodPrice) }}</template>
          </Column>
        </DataTable>
      </div>

      <!-- 우측: 반품 입력 -->
      <div class="right-panel">
        <DataTable
          :value="returnDetailList"
          paginator
          :rows="7"
          resizableColumns
          columnResizeMode="fit"
          class="custom-table"
          :emptyMessage="selectedOrderId ? '좌측에서 품목을 선택하세요.' : '상단 조회로 주문을 선택하세요.'"
        >
          <Column field="prodId" header="제품코드" style="width:120px;" />
          <Column field="prodName" header="제품명" style="width:180px;" />
          <Column field="spec" header="규격" style="width:120px; text-align:center;" />
          <Column field="unit" header="단위" style="width:100px; text-align:center;" />
          <Column field="prodPrice" header="제품가격" style="width:120px; text-align:right;">
            <template #body="{ data }">{{ formatCurrency(data.prodPrice) }}</template>
          </Column>
          <Column field="orderQty" header="주문수량" style="width:100px; text-align:center;" />
          <Column header="반품수량" style="width:110px; text-align:center;">
            <template #body="{ data }">
              <InputNumber
                v-model="data.returnQty"
                :min="0"
                :max="data.orderQty"
                @input="data.returnTotal = (data.returnQty || 0) * data.prodPrice"
                showButtons
                buttonLayout="horizontal"
                decrementButtonClass="p-button-outlined p-button-sm"
                incrementButtonClass="p-button-outlined p-button-sm"
                :inputStyle="{ width: '70px', textAlign: 'center' }"
              />
            </template>
          </Column>
          <Column header="합계" style="width:130px; text-align:right;">
            <template #body="{ data }">{{ formatCurrency(data.returnTotal) }}</template>
          </Column>
          <Column header="사유" style="width:180px;">
            <template #body="{ data }">
              <InputText v-model="data.returnWhy" placeholder="반품 사유" />
            </template>
          </Column>
        </DataTable>

        <div class="summary">
          총 반품합계 : <strong>{{ formatCurrency(totalReturnAmount) }}</strong>
        </div>
      </div>
    </div>

    <!-- 주문 선택 모달 -->
    <Dialog v-model:visible="isShowOrderModal" header="배송완료 주문 선택" :style="{ width: '800px' }" modal>
      <div class="mb-2" style="text-align:right">
        <Button label="배송완료 재조회" icon="pi pi-refresh" class="p-button-text" @click="fetchShippedOrders" />
      </div>
      <DataTable
        :value="orderList"
        paginator
        :rows="8"
        selectionMode="single"
        v-model:selection="selectedOrderRow"
        @rowDblclick="handlePickOrder"
      >
        <!-- getOrderListForView 결과 컬럼에 맞춤 -->
        <Column field="orderId" header="주문번호" style="width:160px;" />
        <Column field="orderDate" header="주문일자" style="width:140px;" />
        <Column field="prodName" header="대표 제품명" />
        <Column field="totalPrice" header="총액" style="width:140px; text-align:right;">
          <template #body="{ data }">{{ formatCurrency(data.totalPrice) }}</template>
        </Column>
        <Column field="deliveryDate" header="납기" style="width:140px;" />
      </DataTable>
      <template #footer>
        <Button label="선택" icon="pi pi-check" @click="handlePickOrder" :disabled="!selectedOrderRow" />
        <Button label="닫기" icon="pi pi-times" class="p-button-text" @click="isShowOrderModal=false" />
      </template>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import axios from 'axios'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import InputNumber from 'primevue/inputnumber'
import InputText from 'primevue/inputtext'
import Dialog from 'primevue/dialog'

const API_BASE = '/api'
const SHIPPED_STATUS = 'SHIPPED' // ⚠️ DB에서 쓰는 실제 출고완료 상태값으로 필요시 바꿔줘

// 모달 & 목록 상태
const isShowOrderModal = ref(false)
const orderList = ref([])            // 모달에 띄울 출고완료 주문 목록(마스터)
const selectedOrderRow = ref(null)   // 모달에서 선택된 주문행

// 좌측(상세) & 우측(반품)
const selectedOrderId = ref('')      // 현재 선택한 주문번호
const orderDetailList = ref([])      // 좌측: 선택된 주문의 상세 품목 리스트
const selectedDetail = ref(null)     // 좌측에서 방금 선택한 상세행
const returnDetailList = ref([])     // 우측: 반품 입력 목록

// 통화 포맷
const formatCurrency = (v) => (v ? v.toLocaleString('ko-KR') + ' 원' : '0 원')

// 총 합계
const totalReturnAmount = computed(() =>
  returnDetailList.value.reduce((sum, it) => sum + (it.returnTotal || 0), 0)
)

// 1) 조회 버튼 → 모달 오픈 + 출고완료 주문 조회
const openOrderModal = async () => {
  isShowOrderModal.value = true
  await fetchShippedOrders()
}

const fetchShippedOrders = async () => {
  try {
    const { data } = await axios.get(`${API_BASE}/orderlist`, { params: { status: '배송완료' } })
    // getOrderListForView가 반환하는 필드명 그대로 사용 (orderId, orderDate, prodName, totalPrice, deliveryDate, status)
    orderList.value = Array.isArray(data) ? data : []
  } catch (e) {
    console.error('배송완료 주문 조회 실패:', e)
    orderList.value = []
  }
}

// 2) 모달에서 주문 선택 → 모달 닫고 상세조회하여 좌측에 표시
const handlePickOrder = async () => {
  if (!selectedOrderRow.value) return
  selectedOrderId.value = selectedOrderRow.value.orderId
  isShowOrderModal.value = false
  await fetchOrderDetails(selectedOrderId.value)
  // 새 주문 선택 시 기존 반품 목록 초기화
  returnDetailList.value = []
}

const fetchOrderDetails = async (orderId) => {
  try {
    const { data } = await axios.get(`${API_BASE}/orderdetail`, { params: { orderId } })
    // Mapper(getOrderDetailList)가 반환하는 컬럼: odetail_id, order_id, prod_id, prod_name, spec, unit, order_qty, prod_price, prod_status
    orderDetailList.value = (data || []).map(d => ({
      odetailId: d.odetailId || d.odetail_id,
      orderId: d.orderId || d.order_id,
      prodId: d.prodId || d.prod_id,
      prodName: d.prodName || d.prod_name,
      spec: d.spec,
      unit: d.unit,
      orderQty: d.orderQty || d.order_qty,
      prodPrice: d.prodPrice || d.prod_price,
      prodStatus: d.prodStatus || d.prod_status
    }))
  } catch (e) {
    console.error('주문 상세 조회 실패:', e)
    orderDetailList.value = []
  }
}

// 3) 좌측 상세 클릭 → 우측 반품 목록에 1건 추가(중복 방지)
const addToReturnList = () => {
  const row = selectedDetail.value
  if (!row) return
  const exists = returnDetailList.value.some(it => it.odetailId === row.odetailId)
  if (exists) return
  returnDetailList.value.push({
    ...row,
    returnQty: 0,
    returnTotal: 0,
    returnWhy: ''
  })
}
  
// 4) 반품 등록
const saveReturn = async () => {
  if (!selectedOrderId.value) {
    alert('주문을 먼저 선택하세요.')
    return
  }
  if (returnDetailList.value.length === 0) {
    alert('반품할 품목을 선택하세요.')
    return
  }
  // 유효성
  for (const it of returnDetailList.value) {
    if (!it.returnQty || it.returnQty <= 0) {
      alert(`반품수량을 확인하세요. (${it.prodName})`)
      return
    }
    if (it.returnQty > it.orderQty) {
      alert(`반품수량이 주문수량을 초과했습니다. (${it.prodName})`)
      return
    }
    if (!it.returnWhy || it.returnWhy.trim() === '') {
      alert(`반품 사유를 입력하세요. (${it.prodName})`)
      return
    }
    it.returnTotal = (it.returnQty || 0) * it.prodPrice
  }

  const payload = returnDetailList.value.map(it => ({
    odetailId: it.odetailId,
    returnQty: it.returnQty,
    returnWhy: it.returnWhy,
    returnPrice: it.returnTotal,
    returnStatus: 'REQUESTED'
  }))

  try {
    const { data } = await axios.post(`${API_BASE}/insertreturn`, payload)
    console.log('반품 등록 성공:', data)
    alert('반품 등록이 완료되었습니다.')
    // 초기화
    returnDetailList.value = []
    selectedDetail.value = null
    // 현재 주문 상세 재조회(남은 품목 반품을 이어서 할 수 있게)
    await fetchOrderDetails(selectedOrderId.value)
  } catch (e) {
    console.error('반품 등록 실패:', e)
    alert('반품 등록 중 오류가 발생했습니다.')
  }
}
</script>

<style scoped>
.return-register { padding: 20px; }
.header { display:flex; justify-content:space-between; align-items:center; margin-bottom:15px; }
.content-wrapper { display:flex; gap:15px; }
.left-panel, .right-panel { flex:1; }
.summary { text-align:right; margin-top:10px; font-size:1.1rem; font-weight:bold; }
.custom-table { width:100%; font-size:0.95rem; }
::v-deep(.p-datatable-thead > tr > th){ border-right:1px solid #e5e7eb; background:#f9fafb; font-weight:600; text-align:center!important; }
::v-deep(.p-datatable-tbody > tr > td){ border-right:1px solid #f0f0f0; text-align:center; padding:8px 10px; }
@media (max-width:768px){ .content-wrapper{ flex-direction:column; } }
</style>
