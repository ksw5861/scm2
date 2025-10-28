<template>
    <Fluid>

        <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />

        <div class="grid grid-cols-2 2xl:grid-cols-4 gap-4 mt-4">
            <div class="card h-30" style="margin-bottom: 0;">
                <div class="flex justify-between mb-4">
                    <div>
                        <span class="block text-muted-color font-medium mb-4">총 미결제 금액</span>
                        <div class="dark:text-surface-0 font-bold text-xl text-red-500">{{ formatCurrency(summaryComputed.totalUnpaid) }}<span class="font-medium text-gray-700">원</span></div>
                    </div>
                    <div
                        class="flex items-center justify-center bg-red-100 dark:bg-red-100/10 rounded-border"
                        style="width:2.5rem;height:2.5rem;"
                    >
                        <i class="text-red-500 ,!text-xl" :class="icons.bill "></i>
                    </div>
                </div>
            </div>
            <div class="card h-30" style="margin-bottom: 0;">
                <div class="flex justify-between mb-4">
                    <div>
                        <span class="block text-muted-color font-medium mb-4">승인된 반품 총액</span>
                        <div class="dark:text-surface-0 font-bold text-xl text-green-500">{{ formatCurrency(summaryComputed.thisMonthReturnAmount) }}<span class="font-medium text-gray-700">원</span></div>
                    </div>
                    <div
                        class="flex items-center justify-center bg-green-100 dark:bg-green-100/10 rounded-border"
                        style="width:2.5rem;height:2.5rem;"
                    >
                        <i class="text-green-500 ,!text-xl" :class="icons.undo "></i>
                    </div>
                </div>
            </div>
            <div class="card h-30" style="margin-bottom: 0;">
                <div class="flex justify-between mb-4">
                    <div>
                        <span class="block text-muted-color font-medium mb-4">이번 납부 예정 금액</span>
                        <div class="dark:text-surface-0 font-bold text-xl text-blue-500">{{ formatCurrency(summaryComputed.currentPayable) }}<span class="font-medium text-gray-700">원</span></div>
                    </div>
                    <div
                        class="flex items-center justify-center bg-blue-100 dark:bg-blue-100/10 rounded-border"
                        style="width:2.5rem;height:2.5rem;"
                    >
                        <i class="text-blue-500 ,!text-xl" :class="icons.pay "></i>
                    </div>
                </div>
            </div>
            <div class="card h-30" style="margin-bottom: 0;">
              <div class="flex justify-between mb-4">
                  <div>
                      <span class="block text-muted-color font-medium mb-4">연체 주문건</span>
                      <div class="dark:text-surface-0 font-bold text-xl text-red-500">
                          {{ overdue.count }}건 | {{ formatCurrency(overdue.amount) }}<span class="font-medium text-gray-700">원</span>
                      </div>
                  </div>
                  <div
                      class="flex items-center justify-center bg-red-100 dark:bg-red-100/10 rounded-border"
                      style="width:2.5rem;height:2.5rem;"
                  >
                      <i class="text-red-500 ,!text-xl pi pi-exclamation-triangle"></i>
                  </div>
              </div>
          </div>

        </div>

        <div class="w-full xl:w-3/4">

        </div>

        <Tabs class="card mt-4" value="0">
            <TabList>
                <Tab value="0" @click="activeTab = 'pending'">미결제 주문 내역</Tab>
                <Tab value="1" @click="activeTab = 'pay'">납부 처리</Tab>
            </TabList>
            <TabPanels>
                <TabPanel value="0">

                    <div class="table-toolbar">
                        <div class="search-container">
                            <i class="pi pi-search search-icon"></i>
                            <InputText
                                v-model="searchQuery"
                                placeholder="주문번호 또는 제품명 검색..."
                                class="search-input"
                            />
                        </div>
                    </div>

                    <DataTable
                        :value="filteredOrders"
                        selectionMode="checkbox"
                        dataKey="orderId"
                        v-model:selection="selectedOrders"
                        paginator
                        :rows="20"
                        class="custom-table"
                    >
                        <template #header>
                        <div v-if="selectedOrders.length > 0" class="selected-summary">
                            <span>{{ selectedOrders.length }}개 항목 선택됨</span>
                            <span>
                            총 선택 금액:
                            <strong>₩{{ formatCurrency(selectedTotal) }}</strong>
                            </span>
                        </div>
                        </template>

                        <Column selectionMode="multiple" headerStyle="width:50px" />
                        <Column field="orderId" header="주문번호" style="width:140px;" />
                        <Column field="prodName" header="제품명" style="width:240px;" />

                        <Column field="totalPrice" header="총 주문금액" style="width:150px; text-align:right;">
                        <template #body="{ data }">
                            ₩{{ formatCurrency(data.totalPrice) }}
                        </template>
                        </Column>

                        <Column field="returnPrice" header="반품금액" style="width:150px; text-align:right;">
                        <template #body="{ data }">
                            ₩{{ formatCurrency(data.returnPrice) }}
                        </template>
                        </Column>

                        <Column field="finalAmount" header="실결제금액" style="width:150px; text-align:right;">
                        <template #body="{ data }">
                            ₩{{ formatCurrency(data.finalAmount) }}
                        </template>
                        </Column>

                        <Column field="sendDate" header="출고일자" style="width:140px;">
                        <template #body="{ data }">
                            {{ formatDate(data.sendDate) }}
                        </template>
                        </Column>

                        <Column field="status" header="주문상태" style="width:140px;" />
                        <Column field="paydueDate" header="결제기한" style="width:140px;">
                        <template #body="{ data }">
                            {{ formatDate(data.paydueDate) }}
                        </template>
                        </Column>
                    </DataTable>
                </TabPanel>
                <TabPanel value="1">
                    <div class="pay-summary">
                        <h3 class="section-title"><i class="pi pi-check-circle"></i> 납부 요약</h3>
                        <div class="summary-list">
                            <div v-for="order in selectedOrders" :key="order.orderId" class="summary-item">
                                <span>{{ order.prodName }}</span>
                                <span>₩{{ formatCurrency(order.finalAmount) }}</span>
                            </div>
                        </div>
                        <div class="summary-total">
                            <div class="row">
                                <span>총 납부금액</span>
                                <span>₩{{ formatCurrency(selectedTotal) }}</span>
                            </div>
                        </div>
                    </div>

                    <div class="pay-form">
                        <h3 class="section-title"><i class="pi pi-wallet"></i> 카카오페이 결제</h3>
                        <Button
                            :label="'₩' + formatCurrency(selectedTotal) + ' 결제하기'"
                            class="w-full pay-btn"
                            @click="requestPay"
                        />
                    </div>
                </TabPanel>
            </TabPanels>
        </Tabs>

    </Fluid>

    <Toast />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import Badge from 'primevue/badge'
import Toast from 'primevue/toast'
import InputText from 'primevue/inputtext'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import { useToast } from 'primevue/usetoast'
import { useUserStore } from '@/stores/user'
import { useIcon } from '@/composables/useIcon'
import { useRoute } from 'vue-router'

const route = useRoute();
const userStore = useUserStore()
const toast = useToast()

// -----------------------------
// 상태 관리
// -----------------------------
const vendorId = ref(userStore.code)
const today = new Date().toLocaleDateString('ko-KR')
const activeTab = ref('pending')
const orders = ref([])
const selectedOrders = ref([])
const searchQuery = ref('')


/* ───────────────────────────────
 *  아이콘 세트
 * ─────────────────────────────── */
const icons = {
  home: useIcon('home'),
  bill: useIcon('bill'),
  undo: useIcon('undo'),
  pay: useIcon('pay'),
  calendar: useIcon('calendar')

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
    { label: '대금 납부' }
  ];
});

// -----------------------------
// 상태 관리 (전역 reactive)
// -----------------------------
const overdue = ref({
  count: 0,
  amount: 0
})

// ✅ 독립 fetchOverdue 함수 (computed 안 X 절대 금지!)
const fetchOverdue = async () => {
  try {
    const { data } = await axios.get('/api/orders/overdue-summary', {
      params: { vendorId: vendorId.value }
    })
    const obj = Array.isArray(data) ? data[0] : data

    overdue.value = {
      count: obj.COUNT || 0,
      amount: obj.AMOUNT || 0
    }
  } catch (err) {
    console.error('연체 요약 조회 오류:', err)
  }
}

// -----------------------------
// 계산 속성
// -----------------------------
const filteredOrders = computed(() => {
  if (!searchQuery.value) return orders.value
  return orders.value.filter(
    (order) =>
      order.orderId.includes(searchQuery.value) ||
      order.prodName.includes(searchQuery.value)
  )
})

const selectedTotal = computed(() =>
  selectedOrders.value.reduce(
    (sum, order) => sum + (order.finalAmount || 0),
    0
  )
)

const summaryComputed = computed(() => {
  const totalUnpaid = orders.value.reduce(
    (sum, order) => sum + (order.totalPrice || 0),
    0
  )
  const thisMonthReturnAmount = orders.value.reduce(
    (sum, order) => sum + (order.returnPrice || 0),
    0
  )
  const currentPayable = totalUnpaid - thisMonthReturnAmount

  return {
    totalUnpaid,
    thisMonthReturnAmount,
    currentPayable
  }
})

// -----------------------------
// API 핸들링
// -----------------------------
const fetchOrders = async () => {
  try {
    const res = await axios.get('/api/pendingorders', {
      params: { vendorId: vendorId.value }
    })
    orders.value = res.data || []
  } catch (error) {
    console.error('미결제 목록 불러오기 실패:', error)
  }
}

// -----------------------------
// 결제 요청
// -----------------------------
const requestPay = () => {
  if (selectedOrders.value.length === 0) {
    toast.add({
      severity: 'warn',
      summary: '알림',
      detail: '납부할 주문을 선택하세요.',
      life: 3000
    })
    return
  }

  const IMP = window.IMP
  IMP.request_pay(
    {
      pg: 'kakaopay.TC0ONETIME',
      pay_method: 'card',
      merchant_uid: `ORDER_${new Date().getTime()}`,
      name: `주문 ${selectedOrders.value.length}건`,
      amount: selectedTotal.value,
      buyer_email: userStore.email || 'test@example.com',
      buyer_name: userStore.name || '홍길동',
      buyer_tel: '010-1234-5678',
      buyer_addr: '서울특별시 강남구 역삼동',
      buyer_postcode: '123-456'
    },
    async (rsp) => {
      if (rsp.success) {
        try {
          const payload = {
            impUid: rsp.imp_uid,
            merchantUid: rsp.merchant_uid,
            payAmount: selectedTotal.value,
            vendorId: vendorId.value,
            payType: '카카오페이',
            paymentDetails: selectedOrders.value.map((order) => ({
              orderId: order.orderId,
              totalPrice: order.totalPrice,
              returnPrice: order.returnPrice,
              finalAmount: order.finalAmount
            }))
          }

          await axios.post('/api/verify-payment', payload)

          toast.add({
            severity: 'success',
            summary: '성공',
            detail: '결제가 완료되었습니다.',
            life: 3000
          })

          fetchOrders()
          selectedOrders.value = []
        } catch (err) {
          toast.add({
            severity: 'error',
            summary: '서버 오류',
            detail: '백엔드 저장 중 오류가 발생했습니다.',
            life: 3000
          })
        }
      } else {
        toast.add({
          severity: 'error',
          summary: '결제 실패',
          detail: rsp.error_msg,
          life: 3000
        })
      }
    }
  )
}

// -----------------------------
// 유틸 함수
// -----------------------------
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(
    d.getDate()
  ).padStart(2, '0')}`
}

const formatCurrency = (value) =>
  value ? value.toLocaleString('ko-KR') : '0'

// -----------------------------
// 라이프사이클
// -----------------------------
onMounted(() => {
  const IMP = window.IMP
  IMP.init('imp62556076')
  fetchOrders()
  vendorId.value = userStore.code
  fetchOverdue()
})
</script>


<style scoped>
/* ===== 공통 스타일 ===== */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.title {
  font-size: 1.6rem;
  font-weight: bold;
}
.subtitle {
  font-size: 0.9rem;
  color: #777;
}
.date-badge {
  background: #eee;
  color: #333;
}

/* ===== 상단 카드 ===== */
.summary-cards-formula {
  display: grid;
  grid-template-columns: 2fr 0.3fr 2fr 0.3fr 2fr 2fr;
  align-items: stretch;
  gap: 12px;
  margin: 20px 0;
  width: 100%;
}
.summary-cards-formula .card {
  min-width: 160px;
  min-height: 140px;
  padding: 20px;
  border-radius: 12px;
  background: #fff;
  text-align: center;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  transition: transform 0.2s ease, background 0.2s ease;
  height: 100%;
}
.summary-cards-formula .card:hover {
  transform: translateY(-4px);
}
.card .label {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 10px;
}
.card .amount {
  font-size: 1.5rem;
  font-weight: bold;
}

/* 색상 */
.red .amount { color: #e74c3c; }
.green .amount { color: #27ae60; }
.blue .amount { color: #2980b9; }
.orange .amount { color: #f39c12; }

.formula-icon {
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 1.4rem;
  font-weight: bold;
  color: #888;
  height: 100%;
  min-height: 140px;
  background: transparent;
  box-shadow: none;
}

/* 반응형 */
@media (max-width: 1024px) {
  .summary-cards-formula {
    grid-template-columns: repeat(2, 1fr);
  }
  .formula-icon {
    display: none;
  }
}
@media (max-width: 480px) {
  .summary-cards-formula {
    grid-template-columns: 1fr;
  }
}

/* ===== 탭 ===== */
.tabs {
  display: flex;
  border-bottom: 1px solid #ddd;
  margin-bottom: 15px;
}
.tab-btn {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  font-size: 1rem;
  background: none;
  border: none;
  cursor: pointer;
}
.tab-btn.active {
  border-bottom: 3px solid #2980b9;
  font-weight: bold;
  color: #2980b9;
}

/* ===== 검색창 ===== */
.table-toolbar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 10px;
}
.search-container {
  position: relative;
  width: 280px;
}
.search-icon {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #aaa;
}
.search-input {
  padding-left: 30px;
  border-radius: 20px;
  height: 34px;
  width: 100%;
  border: 1px solid #ccc;
}

/* ===== 납부 처리 ===== */
.pay-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}
.pay-form,
.pay-summary {
  background: #fff;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}
.section-title {
  font-weight: bold;
  margin-bottom: 12px;
}
.pay-btn {
  background-color: #000;
  color: #fff;
  font-weight: bold;
  border: none;
  height: 40px;
  border-radius: 6px;
  cursor: pointer;
}
.pay-btn:hover {
  background-color: #333;
}
.summary-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.summary-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
  font-size: 0.9rem;
}
.summary-total {
  margin-top: 12px;
  padding-top: 8px;
  border-top: 1px solid #ccc;
}
.summary-total .row {
  display: flex;
  justify-content: space-between;
  font-weight: bold;
  font-size: 1.1rem;
}
.selected-summary {
  display: flex;
  justify-content: flex-end;
  gap: 20px;
  font-size: 1rem;
  padding: 4px 10px;
}
</style>
