<template>
  <div class="return-register">
    <!-- 헤더 -->
    <div class="header">
      <h2>반품 등록 (판매처)</h2>
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
          @click="saveReturn" 
        />
      </div>
    </div>

    <!-- 반품 요약 -->
    <div class="summary">
      총 반품합계: <strong>{{ formatCurrency(totalReturnAmount) }}</strong>
    </div>

    <!-- 반품 상세 테이블 -->
    <DataTable
      :value="returnDetailList"
      paginator
      :rows="10"
      responsiveLayout="scroll"
      resizableColumns
      columnResizeMode="fit"
      class="return-table"
      :emptyMessage="'제품을 검색 후 선택하세요.'"
    >
      <Column field="prodId" header="제품코드" style="width:120px;" />
      <Column field="prodName" header="제품명" style="width:180px;" />
      <Column field="spec" header="규격" style="width:120px;" />
      <Column field="unit" header="단위" style="width:80px;" />

      <!-- 단가 -->
      <Column field="prodUnitPrice" header="제품단가" style="width:120px;">
        <template #body="{ data }">
          <div class="text-right">{{ formatCurrency(data.prodUnitPrice) }}</div>
        </template>
      </Column>

      <!-- 반품수량 -->
      <Column header="반품수량" style="width:110px;">
        <template #body="{ data }">
          <InputNumber
            v-model="data.returnQty"
            :min="0"
            @input="data.returnTotal = (data.returnQty || 0) * data.prodUnitPrice"
            showButtons
            buttonLayout="horizontal"
            decrementButtonClass="p-button-outlined p-button-sm"
            incrementButtonClass="p-button-outlined p-button-sm"
            :inputStyle="{ width: '70px', textAlign: 'center' }"
          />
        </template>
      </Column>

      <!-- 합계 -->
      <Column header="합계" style="width:130px;">
        <template #body="{ data }">
          <div class="text-right">{{ formatCurrency(data.returnTotal) }}</div>
        </template>
      </Column>

      <!-- 사유 -->
      <Column header="반품 사유" style="width:180px;">
        <template #body="{ data }">
          <InputText v-model="data.returnWhy" placeholder="사유 입력" />
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
        @rowClick="handleSelectProduct" 
        :emptyMessage="'제품 목록이 없습니다.'"
      >
        <Column field="prodId" header="제품코드" />
        <Column field="prodName" header="제품명" />
        <Column field="spec" header="규격" />
        <Column field="unit" header="단위" />
        <Column field="prodUnitPrice" header="제품가격">
          <template #body="{ data }">
            {{ formatCurrency(data.prodUnitPrice) }}
          </template>
        </Column>
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
import InputText from 'primevue/inputtext'
import Dialog from 'primevue/dialog'
import { useAppToast } from '@/composables/useAppToast'

const { toast } = useAppToast()
const API_BASE = '/api'

// 모달 상태
const isShowModal = ref(false)

// 제품 목록
const productList = ref([])
const selectedProduct = ref(null)

// 반품 상세 목록
const returnDetailList = ref([])

// 통화 포맷
const formatCurrency = (v) => (v ? v.toLocaleString('ko-KR') + ' 원' : '0 원')

// 총 반품 합계 계산
const totalReturnAmount = computed(() =>
  returnDetailList.value.reduce((sum, it) => sum + (it.returnTotal || 0), 0)
)

// 제품 목록 조회
const fetchProducts = async () => {
  try {
    const { data } = await axios.get(`${API_BASE}/products`, {
      params: { page: 1, pageSize: 50 }
    })
    productList.value = data.items || []
  } catch (err) {
    console.error('제품 목록 조회 오류:', err)
    productList.value = []
  }
}

// 제품 선택 시 반품 상세 목록에 추가
const handleSelectProduct = () => {
  if (!selectedProduct.value) return

  const product = selectedProduct.value

  // 중복 체크
  const exists = returnDetailList.value.some(it => it.prodId === product.prodId)
  if (exists) {
    toast('warn', '중복 제품', '이미 선택된 제품입니다.')
    return
  }

  returnDetailList.value.push({
    prodId: product.prodId,
    prodName: product.prodName,
    spec: product.spec || '-',
    unit: product.unit || '-',
    prodUnitPrice: product.prodUnitPrice || 0,
    returnQty: 0,
    returnTotal: 0,
    returnWhy: ''
  })

  selectedProduct.value = null
  isShowModal.value = false
}

// 반품 등록
const saveReturn = async () => {
  if (returnDetailList.value.length === 0) {
    alert('반품할 제품을 선택하세요.')
    return
  }

  // 유효성 검사
  for (const it of returnDetailList.value) {
    if (!it.returnQty || it.returnQty <= 0) {
      alert(`반품 수량을 확인하세요. (${it.prodName})`)
      return
    }
    if (!it.returnWhy || it.returnWhy.trim() === '') {
      alert(`반품 사유를 입력하세요. (${it.prodName})`)
      return
    }
    it.returnTotal = it.returnQty * it.prodUnitPrice
  }

  // 마스터 + 상세 구조
  const payload = {
    returnId: 'RT' + Date.now(),           // 프론트에서 임시 PK 생성
    totalAmt: totalReturnAmount.value,     // 반품 총액
    status: 'REQ',
    details: returnDetailList.value.map(it => ({
      rdetailId: 'RD' + Date.now() + '_' + it.prodId,
      prodId: it.prodId,
      returnQty: it.returnQty,
      prodUnitPrice: it.prodUnitPrice,
      returnWhy: it.returnWhy
    }))
  }

  try {
    const { data } = await axios.post(`${API_BASE}/insertreturn`, payload)
    console.log('반품 등록 성공:', data)
    toast('success', '반품 등록', '반품이 성공적으로 등록되었습니다.')

    // 초기화
    returnDetailList.value = []
    selectedProduct.value = null
  } catch (e) {
    console.error('반품 등록 실패:', e)
    toast('error', '반품 실패', '반품 등록 중 오류가 발생했습니다.')
  }
}

onMounted(fetchProducts)
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
.summary {
  text-align: right;
  margin-bottom: 15px;
  font-size: 1.1rem;
  font-weight: bold;
  color: #007ad9;
}
.return-table {
  width: 100%;
  font-size: 0.95rem;
}
.text-right {
  text-align: right;
  padding-right: 6px;
}
</style>
