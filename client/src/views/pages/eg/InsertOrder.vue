<template>
    <Fluid>

        <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />

        <div class="w-full mt-4">
            <div class="card flex flex-col">
                <div class="flex items-center justify-between h-10">
                    <div class="font-semibold text-lg sm:text-xl flex items-center gap-4 whitespace-nowrap">
                        <span :class="icons.list"></span>
                        주문 등록 예정 목록
                        <div class="text-sm text-gray-400" style="align-self: flex-end;">
                            총 주문 예정 금엑
                            <span class="font-semibold text-sm text-gray-700 text-red-400">
                                {{ formatCurrency(totalAmount) }}
                            </span>
                            원
                        </div>
                    </div>

                    <div class="flex gap-2 ">
                        <Btn
                            icon="search"
                            color="primary"
                            label="제품 조회"
                            class="whitespace-nowrap"
                            @click="isShowModal = true"
                        />
                        <Btn
                            icon="add"
                            color="primary"
                            label="주문 등록"
                            class="whitespace-nowrap"
                            @click="saveOrder"
                            outlined
                        />
                    </div>
                </div>

                <Divider />

                <div class="w-full flex flex-row mb-2 gap-2">
                    <DataTable
                        :value="orderDetailList"
                        paginator
                        :rows="10"
                        responsiveLayout="scroll"
                        resizableColumns
                        columnResizeMode="fit"
                        class="w-full"
                    >
                        <Column field="prodId" header="제품코드" style="width: 72px;"/>
                        <Column field="prodName" header="제품명" style="width: 280px;"/>
                        <Column field="spec" header="규격" style="width: 124px;"/>
                        <Column field="unit" header="단위" style="width: 60px;"/>

                        <Column field="prodUnitPrice" header="제품단가" style="width: 360px;">
                            <template #body="{ data }">
                            <div class="text-right">{{ formatCurrency(data.prodUnitPrice) }} 원</div>
                            </template>
                        </Column>

                        <Column header="주문수량" style="width: 240px;">
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

                        <Column header="합계">
                            <template #body="{ data }">
                                <div class="text-right font-semibold"><span class="text-red-400">{{ formatCurrency(data.total) }}</span> 원</div>
                            </template>
                        </Column>
                    </DataTable>
                </div>
            </div>
        </div>
    </Fluid>

    <Dialog
      v-model:visible="isShowModal"
      header="제품 검색"
      :style="{ width: '600px' }"
      modal
    >
      <div class="mb-3">
        <InputText
          v-model="search"
          placeholder="제품명 검색"
          class="w-full"
        />
      </div>

      <DataTable
        :value="filteredProducts"
        paginator
        :rows="15"
        responsiveLayout="scroll"
        selectionMode="single"
        v-model:selection="selectedProduct"
        @rowClick="handleSelect"
      >
        <Column field="prodId" header="제품번호" />
        <Column field="prodName" header="제품명" />
        <Column field="spec" header="규격" />
        <Column field="unit" header="단위" />
        <Column field="prodUnitPrice" header="제품가격">
          <template #body="{ data }">
            <div class="text-right">{{ formatCurrency(data.prodUnitPrice) }}</div>
          </template>
        </Column>
      </DataTable>
    </Dialog>

</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import InputNumber from 'primevue/inputnumber'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import { useAppToast } from '@/composables/useAppToast'
import { useUserStore } from '@/stores/user'
import { useIcon } from '@/composables/useIcon'
import { useRoute } from 'vue-router'

const route = useRoute();
const userStore = useUserStore()
const { toast } = useAppToast()

// -----------------------------
// 상태 관리
// -----------------------------
const isShowModal = ref(false)          // 모달 표시 여부
const productList = ref([])             // 제품 목록
const selectedProduct = ref(null)       // 선택된 제품
const orderDetailList = ref([])         // 주문 상세 목록
const deliveryDate = ref(null)          // 납기일자
const returnPrice = ref(1)              // 반품 관련 가격
const returnStatus = ref('대기')        // 반품 상태
const search = ref('')                  // 검색어

/* ───────────────────────────────
 *  아이콘 세트
 * ─────────────────────────────── */
const icons = {
  home: useIcon('home'),
  list: useIcon('list')
};

/* ───────────────────────────────
 *  Breadcrumb 구성
 * ─────────────────────────────── */
const breadcrumbHome = { icon: icons.home, to: '/' };
const breadcrumbItems = computed(() => {
  const matched = route.matched.filter(r => r.meta);
  if (!matched.length) return [];
  const current = matched[matched.length - 1];
  return [
    { label: current.meta.breadcrumb?.parent || '' },
    { label: '주문 등록' }
  ];
});

// -----------------------------
// 계산 & 유틸
// -----------------------------
const totalAmount = computed(() =>
  orderDetailList.value.reduce((sum, item) => sum + (item.total || 0), 0)
)

const formatCurrency = (value) =>
  (value || 0).toLocaleString('ko-KR')

const calculateRowTotal = (row) => {
  row.total =
    (Number(row.orderQty) || 0) * (Number(row.prodUnitPrice) || 0)
}

// -----------------------------
// 필터링된 제품 목록
// -----------------------------
const filteredProducts = computed(() => {
  if (!search.value) return productList.value
  return productList.value.filter(item =>
    item.prodName?.toLowerCase().includes(search.value.toLowerCase())
  )
})

// -----------------------------
// API & 데이터 핸들링
// -----------------------------
const fetchProducts = async () => {
  try {
    const { data } = await axios.get('/api/products', {
      params: { page: 1, pageSize: 50 }
    })
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
  const payload = {
    orderDate: new Date().toISOString().slice(0, 10),
    deliveryDate: deliveryDate.value,
    totalPrice: totalAmount.value,
    status: '대기',
    payStatus: '대기',
    vendorId: userStore.code,
    returnPrice: returnPrice.value || 1,
    returnStatus: returnStatus.value || '대기',
    details: JSON.parse(JSON.stringify(orderDetailList.value))
  }

  try {
    const res = await axios.post('/api/insertorder', payload)
    console.log(res)
    if (res.data.status === 'success') {
      toast('success', '주문 등록', res.data.message || '주문이 성공적으로 등록되었습니다.')
      orderDetailList.value = []
    } else {
      toast('error', '등록 실패', res.data.message)
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
