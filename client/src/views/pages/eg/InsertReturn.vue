<template>
    <Fluid>

        <!-- 공지사항 -->
        <div class="w-full mb-4">
            <div class="card flex flex-col" style="box-shadow: inset 4px 0 0 0 #10b981;">
                <h5 style="color: #10b981;">공지사항</h5>
                <p>
                    고객님의 주문건은 <span class="highlight">배송 완료일로부터 7일 이내</span>에만 반품이 가능합니다.<br />
                    또한, 각 주문건은 <span class="highlight">한 번만 반품 신청</span>이 가능하오니, 신중하게 신청해 주시기 바랍니다.
                </p>
            </div>
        </div>

        <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />

        <div class="flex flex-col 2xl:flex-row w-full gap-4 mt-4">

            <div class="w-full xl:w-1/3">
                <div class="card flex flex-col">
                    <div class="font-semibold text-lg sm:text-xl flex items-center justify-between gap-4 h-10">
                        <div class="flex items-center gap-4">
                            <span :class="icons.list"></span>
                            반품 가능한 주문 내역
                        </div>
                    </div>

                    <Divider />

                    <DataTable
                        :value="orderList"
                        :rows="10"
                        paginator
                        selectionMode="single"
                        v-model:selection="selectedOrder"
                        @rowSelect="loadOrderDetails"
                        :emptyMessage="'반품 가능한 주문이 없습니다.'"
                    >
                        <Column field="orderId" header="주문번호" style="width:180px" />
                        <Column field="orderDate" header="주문일자" style="width:160px" >
                            <template #body="{ data }">
                                {{ useDateFormat(data.orderDate) }}
                            </template>
                        </Column>
                        <Column field="totalPrice" header="총금액" style="width:120px">
                            <template #body="{ data }">
                            {{ formatCurrency(data.totalPrice) }} 원
                            </template>
                        </Column>
                    </DataTable>

                </div>
            </div>

            <div class="w-full xl:w-2/3">
                <div class="card flex flex-col">
                    <div class="font-semibold text-lg sm:text-xl flex items-center justify-between gap-4 h-10">
                        <div class="flex items-center gap-4">
                            <span :class="icons.info"></span>
                            주문 상세 내역
                            <div class="text-sm text-gray-400" style="align-self: flex-end;">
                                총 반품 예정 금엑
                                <span class="font-semibold text-sm text-gray-700 text-red-400">
                                    {{ formatCurrency(totalReturnAmount) }}
                                </span>
                                원
                            </div>
                        </div>
                        <div class="flex gap-2 ">
                            <Btn
                                icon="add"
                                color="primary"
                                label="반품 등록"
                                class="whitespace-nowrap"
                                @click="saveReturn"
                                outlined
                            />
                        </div>
                    </div>

                    <Divider />

                    <DataTable
                        :value="orderDetailList"
                        :rows="10"
                        paginator
                        :emptyMessage="'좌측에서 주문을 선택하세요.'"
                    >
                        <Column field="prodId" header="제품코드" style="width:120px" />
                        <Column field="prodName" header="제품명" style="width:160px" />
                        <Column field="spec" header="규격" style="width:100px" />
                        <Column field="unit" header="단위" style="width:80px" />
                        <Column field="prodUnitPrice" header="단가" style="width:100px">
                            <template #body="{ data }">
                            {{ formatCurrency(data.prodUnitPrice) }} 원
                            </template>
                        </Column>
                        <Column field="orderQty" header="주문수량" style="width:80px; text-align:center;" />

                        <!-- 반품 수량 -->
                        <Column header="반품수량" style="width:120px">
                            <template #body="{ data }">
                            <InputNumber
                                v-model="data.returnQty"
                                :min="0"
                                :max="data.remainQty"
                                :disabled="data.remainQty === 0"
                            />
                            </template>
                        </Column>


                        <!-- 반품 사유 -->
                        <Column header="반품 사유" style="width:180px">
                            <template #body="{ data }">
                            <InputText
                                v-model="data.returnWhy"
                                :disabled="data.remainQty === 0"
                                placeholder="사유 입력"
                            />
                            </template>
                        </Column>


                        <!-- 합계 -->
                        <Column header="합계" style="width:130px">
                            <template #body="{ data }">
                            {{ formatCurrency(data.returnTotal) }} 원
                            </template>
                        </Column>
                    </DataTable>

                </div>
            </div>

        </div>
    </Fluid>

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
import { useRoute } from 'vue-router'
import { useIcon } from '@/composables/useIcon'
import { useDateFormat } from '@/composables/useFormat'

const route = useRoute();
const { toast } = useAppToast()
const userStore = useUserStore()

// -----------------------------
// 상태 관리
// -----------------------------
const orderList = ref([])        // 반품 가능 주문 목록
const selectedOrder = ref(null)  // 선택된 주문
const orderDetailList = ref([])  // 선택된 주문 상세

/* ───────────────────────────────
 *  아이콘 세트
 * ─────────────────────────────── */
const icons = {
  home: useIcon('home'),
  list: useIcon('list'),
  info: useIcon('info')
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
    { label: '반품 등록' }
  ];
});

// -----------------------------
// 계산 속성 & 유틸
// -----------------------------
const formatCurrency = (v) =>
  v ? v.toLocaleString('ko-KR') : '0'

const totalReturnAmount = computed(() =>
  orderDetailList.value.reduce((sum, it) => sum + (it.returnTotal || 0), 0)
)

// -----------------------------
// API 핸들링
// -----------------------------
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

const loadOrderDetails = async () => {
  if (!selectedOrder.value) return
  try {
    const { data } = await axios.get(`/api/returnableorder/${selectedOrder.value.orderId}/details`)
    if (data.status === 'success') {
      orderDetailList.value = data.details.map((it) => {
        const returned = it.returnedQty || 0
        const remainQty = Math.max(0, (it.orderQty || 0) - returned)
        return {
          ...it,
          returnQty: 0,
          returnTotal: 0,
          returnWhy: '',
          remainQty
        }
      })
    }
  } catch (err) {
    console.error(err)
    toast('error', '상세 조회 실패', '서버 오류 발생')
  }
}



// -----------------------------
// watch: 실시간 검증 & 합계 계산
// -----------------------------
watch(
  orderDetailList,
  (list) => {
    list.forEach((item) => {
      const cap = item.remainQty ?? item.orderQty ?? 0
      if ((item.returnQty || 0) > cap) {
        toast('warn', '반품 수량 자동 조정', `반품 가능 수량은 ${cap}개 입니다.`)
        item.returnQty = cap
      }
      item.returnTotal = (item.returnQty || 0) * (item.prodUnitPrice || 0)
    })
  },
  { deep: true }
)


// -----------------------------
// 반품 등록
// -----------------------------
const saveReturn = async () => {
  // 최종 검증
  const invalidItems = orderDetailList.value.filter(
    (it) => it.returnQty > it.orderQty
  )
  if (invalidItems.length > 0) {
    toast('error', '반품 수량 오류', '반품 수량은 주문 수량을 초과할 수 없습니다.')
    return
  }

  const validItems = orderDetailList.value.filter(
    (it) => it.returnQty > 0 && it.returnWhy.trim() !== ''
  )
  if (validItems.length === 0) {
    toast('warn', '반품 등록', '반품 사유를 입력하세요.')
    return
  }

  const payload = {
    returnId: 'RT' + Date.now(),
    vendorId: userStore.code,
    status: '대기',
    details: validItems.map((it) => ({
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

// -----------------------------
// 초기 로드
// -----------------------------
onMounted(fetchReturnableOrders)
</script>
