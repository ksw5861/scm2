<template>
  <div class="return-register">
    <div class="header">
      <h2>반품 등록 (판매처)</h2>
      <Button 
        label="등록" 
        icon="pi pi-plus" 
        class="p-button-success" 
        @click="saveReturn" 
      />
    </div>

    <div class="return-container">
      <!-- 좌측 주문 목록 -->
      <div class="order-list">
        <h3>반품 가능 주문 목록</h3>
        <DataTable 
          :value="orderList"
          :rows="10"
          paginator
          selectionMode="single"
          v-model:selection="selectedOrder"
          @rowSelect="loadOrderDetails"
          :emptyMessage="'반품 가능한 주문이 없습니다.'"
        >
          <Column field="orderId" header="주문번호" style="width:120px"/>
          <Column field="orderDate" header="주문일자" style="width:140px"/>
          <Column field="totalPrice" header="총금액" style="width:120px">
            <template #body="{ data }">{{ formatCurrency(data.totalPrice) }}</template>
          </Column>
        </DataTable>
      </div>

      <!-- 우측 상세 목록 -->
      <div class="order-detail">
        <h3>주문 상세</h3>
        <DataTable 
          :value="orderDetailList"
          :rows="10"
          paginator
          :emptyMessage="'좌측에서 주문을 선택하세요.'"
        >
          <Column field="prodId" header="제품코드" style="width:120px"/>
          <Column field="prodName" header="제품명" style="width:160px"/>
          <Column field="spec" header="규격" style="width:100px"/>
          <Column field="unit" header="단위" style="width:80px"/>
          <Column field="prodUnitPrice" header="단가" style="width:100px">
            <template #body="{ data }">{{ formatCurrency(data.prodUnitPrice) }}</template>
          </Column>
          <Column field="orderQty" header="주문수량" style="width:80px; text-align:center;" />

          <!-- 반품 수량 -->
          <Column header="반품수량" style="width:120px">
            <template #body="{ data }">
              <InputNumber 
                v-model="data.returnQty"
                :min="0"
                :max="data.orderQty"
                showButtons
              />
            </template>
          </Column>

          <!-- 반품 사유 -->
          <Column header="반품 사유" style="width:180px">
            <template #body="{ data }">
              <InputText v-model="data.returnWhy" placeholder="사유 입력"/>
            </template>
          </Column>

          <!-- 합계 -->
          <Column header="합계" style="width:130px">
            <template #body="{ data }">{{ formatCurrency(data.returnTotal) }}</template>
          </Column>
        </DataTable>
      </div>
    </div>

    <!-- 하단 합계 -->
    <div class="summary">
      총 반품합계: <strong>{{ formatCurrency(totalReturnAmount) }}</strong>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import axios from 'axios'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import InputNumber from 'primevue/inputnumber'
import InputText from 'primevue/inputtext'
import { useAppToast } from '@/composables/useAppToast'
import { useUserStore } from '@/stores/user'

const { toast } = useAppToast()
const userStore = useUserStore()

// 주문 목록
const orderList = ref([])
const selectedOrder = ref(null)

// 주문 상세
const orderDetailList = ref([])

// 통화 포맷
const formatCurrency = (v) => (v ? v.toLocaleString('ko-KR') + ' 원' : '0 원')

// 총 반품 합계
const totalReturnAmount = computed(() =>
  orderDetailList.value.reduce((sum, it) => sum + (it.returnTotal || 0), 0)
)

// 1. 반품 가능 주문 목록 조회
const fetchReturnableOrders = async () => {
  try {
    const { data } = await axios.get('/api/returnableproducts', {
      params: { vendorId: userStore.code }
    })
    if (data.status === 'success') {
      orderList.value = data.items || []
    } else {
      toast('error', '조회 실패', data.message || '목록 조회 실패')
    }
  } catch (err) {
    console.error(err)
    toast('error', '조회 실패', '서버 오류 발생')
  }
}

// 2. 주문 상세 조회
const loadOrderDetails = async () => {
  if (!selectedOrder.value) return
  try {
    const { data } = await axios.get(`/api/orders/${selectedOrder.value.orderId}/details`)
    if (data.status === 'success') {
      orderDetailList.value = data.details.map(it => ({
        ...it,
        returnQty: 0,
        returnTotal: 0,
        returnWhy: ''
      }))
    }
  } catch (err) {
    console.error(err)
    toast('error', '상세 조회 실패', '서버 오류 발생')
  }
}

// 3. 반품 수량 실시간 검증 (watch)

watch(
  orderDetailList,
  (newList) => {
    newList.forEach(item => {
      // 주문 수량보다 많이 입력했을 경우
      if (item.returnQty > item.orderQty) {
        toast('warn', '반품 수량 자동 조정', 
          `반품 수량은 주문 수량(${item.orderQty})을 초과할 수 없습니다. 자동으로 ${item.orderQty}로 조정되었습니다.`)
        
        // 초과 입력값을 주문 수량으로 자동 되돌림
        item.returnQty = item.orderQty
      }

      // 합계 자동 계산
      item.returnTotal = (item.returnQty || 0) * item.prodUnitPrice
    })
  },
  { deep: true }  // 🔹 배열 내부의 모든 속성 변화 감시
)


// 4. 반품 등록
const saveReturn = async () => {
  // 초과 값이 있는지 최종 확인
  const invalidItems = orderDetailList.value.filter(it => it.returnQty > it.orderQty)
  if (invalidItems.length > 0) {
    toast('error', '반품 수량 오류', '반품 수량은 주문 수량을 초과할 수 없습니다.')
    return
  }

  const validItems = orderDetailList.value.filter(it => it.returnQty > 0 && it.returnWhy.trim() !== '')
  if (validItems.length === 0) {
    toast('warn', '반품 등록', '반품할 항목을 선택하세요.')
    return
  }

  const payload = {
    returnId: 'RT' + Date.now(),
    vendorId: userStore.code,
    status: '대기',
    details: validItems.map(it => ({
      rdetailId: 'RD' + Date.now() + '_' + it.prodId,
      prodId: it.prodId,
      returnQty: it.returnQty,
      prodUnitPrice: it.prodUnitPrice,
      returnWhy: it.returnWhy
    }))
  }

  try {
    const { data } = await axios.post('/api/insertreturn', payload)
    if (data.status === 'success') {
      toast('success', '반품 등록 완료', '반품이 성공적으로 등록되었습니다.')
      orderDetailList.value = []
      selectedOrder.value = null
      fetchReturnableOrders()
    } else {
      toast('error', '등록 실패', data.message || '등록 중 오류 발생')
    }
  } catch (err) {
    console.error(err)
    toast('error', '등록 실패', '서버 오류 발생')
  }
}


onMounted(fetchReturnableOrders)
</script>

<style scoped>
.return-register {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.return-container {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 20px;
  margin-top: 20px;
}
.summary {
  text-align: right;
  margin-top: 15px;
  font-size: 1.1rem;
  font-weight: bold;
  color: #007ad9;
}
</style>
