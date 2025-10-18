<template>

    <Fluid>

        <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />

        <SearchCard title="주문 내역 검색" @search="fetchOrders" @reset="resetFilters">
            <div class="flex flex-wrap w-full">

                <!-- 주문일자 -->
                <div class="flex flex-col gap-2 p-2 w-full xl:w-1/2">
                    <label class="font-semibold mb-1">주문 일자</label>
                    <div class="flex items-center gap-2">
                        <DatePicker v-model="filters.startDate" placeholder="시작일" dateFormat="yy-mm-dd" showIcon />
                        <span>~</span>
                        <DatePicker v-model="filters.endDate" placeholder="종료일" dateFormat="yy-mm-dd" showIcon />
                    </div>
                </div>

                <!-- 주문 상태 -->
                <div class="flex flex-col gap-2 p-2 w-full xl:w-1/2">
                    <label class="font-semibold mb-1">주문 상태</label>
                    <div class="flex gap-2 flex-nowrap overflow-x-auto">
                        <Button
                        v-for="status in ORDER_STATUS_OPTIONS"
                        :key="status"
                        :label="status"
                        :class="[
                            'whitespace-nowrap',
                            filters.status === status ? '' : 'p-button-outlined'
                        ]"
                        @click="selectStatus(status)"
                        />
                    </div>
                </div>

                <!-- 제품명 -->
                <div class="flex flex-col gap-2 p-2 w-full xl:w-1/2">
                    <InputGroup>
                        <InputGroupAddon><i class="pi pi-box" /></InputGroupAddon>
                        <IftaLabel>
                            <InputText v-model="filters.prodName" inputId="filterProdName" />
                            <label for="filterProdName">제품명</label>
                        </IftaLabel>
                    </InputGroup>
                </div>

                <!-- 주문번호 -->
                <div class="flex flex-col gap-2 p-2 w-full xl:w-1/2">
                    <InputGroup>
                        <InputGroupAddon><i class="pi pi-tag" /></InputGroupAddon>
                        <IftaLabel>
                            <InputText v-model="filters.orderId" inputId="filterOrderId" />
                            <label for="filterOrderId">주문 번호</label>
                        </IftaLabel>
                    </InputGroup>
                </div>

            </div>
        </SearchCard>

        <div class="flex flex-col 2xl:flex-row w-full gap-4 mt-4">

            <div class="w-full xl:w-13/24">
                <div class="card flex flex-col">
                    <div class="font-semibold text-lg sm:text-xl flex items-center justify-between gap-4 h-10">
                        <div class="flex items-center gap-4">
                            <span :class="icons.list"></span>
                            주문 내역 목록
                        </div>
                    </div>

                    <Divider />

                    <DataTable
                        :value="orders"
                        selectionMode="single"
                        v-model:selection="selectedOrder"
                        dataKey="orderId"
                        paginator
                        :rows="10"
                        responsiveLayout="scroll"
                        resizableColumns
                        columnResizeMode="fit"
                        class="custom-table"
                    >
                        <Column field="orderId" header="주문 번호" style="font-size: 12px; width:140px; text-align:center;" />
                        <Column field="orderDate" header="주문 일자" style="font-size: 12px; width:140px; text-align:center;" />
                        <Column field="prodName" header="주문명" style="font-size: 12px; width:200px;" />

                        <Column field="totalPrice" header="주문 총액" style="font-size: 12px; width:150px; text-align:right;">
                            <template #body="slotProps">
                            {{ formatCurrency(slotProps.data.totalPrice) }} 원
                            </template>
                        </Column>

                        <Column field="status" header="주문 상태" style="font-size: 12px; width:120px; text-align:center;" />
                        <Column field="payStatus" header="결제 상태" style="font-size: 12px; width:120px; text-align:center;" />

                        <Column field="deliveryDate" header="배송예정일" style="font-size: 12px; width:140px; text-align:center;" />
                    </DataTable>

                </div>
            </div>

            <div class="w-full xl:w-11/24">
                <div class="card flex flex-col">
                    <div class="flex items-center justify-between h-10">
                        <div class="font-semibold text-lg sm:text-xl flex items-center justify-between gap-4 h-10">
                            <div class="flex items-center gap-4">
                                <span :class="icons.info"></span>
                                주문 상세 내역
                            </div>
                        </div>
                        <div class="flex gap-2 ">
                            <Btn
                                icon="excel"
                                label="Excel 다운로드"
                                class="whitespace-nowrap"
                                style="background-color: #16a34a; border: none;"
                                @click="exportExcel"
                            />
                            <Btn
                                icon="pdf"
                                label="PDF 다운로드"
                                class="whitespace-nowrap"
                                style="background-color: #dc2626; border: none;"
                                @click="exportPDF"
                            />
                            <Btn
                                icon="cancel"
                                label="주문 취소"
                                color="danger"
                                class="whitespace-nowrap"
                                @click="cancelOrder"
                                outlined
                            />
                        </div>
                    </div>

                    <Divider />

                    <DataTable
                        :value="orderDetails"
                        responsiveLayout="scroll"
                        resizableColumns
                        columnResizeMode="fit"
                        class="custom-table"
                    >
                        <Column field="prodId" header="제품코드" style="font-size: 12px; width:84px; text-align:center;" />
                        <Column field="prodName" header="제품명" style="font-size: 12px; width:180px;" />
                        <Column field="spec" header="규격" style="font-size: 12px; width:120px; text-align:center;" />
                        <Column field="unit" header="단위" style="font-size: 12px; width:48px; text-align:center;" />

                        <Column field="prodUnitPrice" header="제품단가" style="font-size: 12px; width:120px; text-align:right;">
                            <template #body="slotProps">
                            {{ formatCurrency(slotProps.data.prodUnitPrice) }} 원
                            </template>
                        </Column>

                        <Column field="orderQty" header="주문 수량" style="font-size: 12px; width:56px; text-align:center;" />
                        <Column field="totalUnitPrice" header="합계" style="font-size: 12px; width:120px; text-align:right;">
                            <template #body="slotProps">
                            {{ formatCurrency(slotProps.data.totalUnitPrice) }} 원
                            </template>
                        </Column>
                        <Column field="prodStatus" header="상태" style="font-size: 12px;"/>

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
import InputText from 'primevue/inputtext'
import DatePicker from 'primevue/datepicker'
import { useUserStore } from '@/stores/user'
import { useRoute } from 'vue-router'
import { useIcon } from '@/composables/useIcon'

const route = useRoute();
const userStore = useUserStore()

// -----------------------------
// 상태 관리
// -----------------------------
const ORDER_STATUS_OPTIONS = {
  WAITING: '대기',
  PROCESSING: '처리중',
  COMPLETED: '처리완료',
  READY_TO_SHIP: '배송준비중',
  SHIPPED: '출고완료',
  DELIVERED: '배송완료',
  CANCELED: '주문취소',
}

const filters = ref({
  startDate: null,
  endDate: null,
  prodName: '',
  status: '',
  orderId: ''
})
const orders = ref([])
const selectedOrder = ref(null)
const orderDetails = ref([])


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
    { label: '주문 내역 조회' }
  ];
});

// -----------------------------
// 계산 속성 & 유틸
// -----------------------------
const detailTotal = computed(() =>
  orderDetails.value.reduce((acc, item) => acc + (item.totalUnitPrice || 0), 0)
)

const formatCurrency = (value) => {
  if (value === null || value === undefined) return '0'
  return value.toLocaleString('ko-KR')
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const addDays = (dateString, days) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  date.setDate(date.getDate() + days)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const formatDateForAPI = (date) => {
  if (!date) return null
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// -----------------------------
// API 핸들링
// -----------------------------
const selectStatus = (status) => {
  filters.value.status = status
  fetchOrders()
}

const fetchOrders = async () => {
  try {
    const params = {
      ...filters.value,
      vendorId: userStore.code,
      startDate: formatDateForAPI(filters.value.startDate),
      endDate: formatDateForAPI(filters.value.endDate)
    }

    const { data } = await axios.get('/api/orderlist', { params })

    if (data.status === 'success') {
      orders.value = data.orders.map((item) => ({
        orderId: item.orderId,
        orderDate: formatDate(item.orderDate),
        prodName: item.prodName || '-',
        totalPrice: item.totalPrice || 0,
        deliveryDate: addDays(item.orderDate, 2),
        status: item.status || '-',
        payStatus: item.payStatus || '-'
      }))
    } else {
      alert(data.message || '주문 목록을 불러오지 못했습니다.')
    }
  } catch (error) {
    console.error('❌ 주문 목록 조회 오류:', error)
    alert('서버 오류로 주문 목록을 불러올 수 없습니다.')
  }
}

const fetchOrderDetail = async (orderId) => {
  try {
    const { data } = await axios.get(`/api/orders/${orderId}/details`)
    if (data.status === 'success') {
      orderDetails.value = data.details.map((item) => ({
        ...item,
        totalUnitPrice: item.prodUnitPrice * item.orderQty
      }))
    } else {
      alert(data.message || '주문 상세 데이터를 불러오지 못했습니다.')
    }
  } catch (error) {
    console.error('❌ 주문 상세 조회 오류:', error)
    alert('서버 오류로 상세 데이터를 불러올 수 없습니다.')
  }
}

// -----------------------------
// 배송완료 처리
// -----------------------------
const markAsDelivered = async () => {
  if (!selectedOrder.value) {
    alert('먼저 주문을 선택하세요.')
    return
  }
  if (selectedOrder.value.status !== '출고완료') {
    alert('출고완료 상태만 완료 처리할 수 있습니다.')
    return
  }
  try {
    const { data } = await axios.put(`/api/orders/${selectedOrder.value.orderId}/status`, {
      status: '배송완료'
    })
    if (data.status === 'success') {
      alert('배송완료로 변경되었습니다.')
      fetchOrders()
      fetchOrderDetail(selectedOrder.value.orderId)
    } else {
      alert(data.message || '상태 변경 실패')
    }
  } catch (error) {
    console.error('상태 변경 오류:', error)
    alert('서버 오류로 상태를 변경하지 못했습니다.')
  }
}

// -----------------------------
//  출고완료로 번경하는 버튼
// -----------------------------

const markAsShipping = async () => {
  if (!selectedOrder.value) {
    alert('먼저 주문을 선택하세요.')
    return
  }
  if (selectedOrder.value.status !== '처리완료') {
    alert('처리완료 상태만 출고완료으로 변경할 수 있습니다.')
    return
  }
  try {
    const { data } = await axios.put(`/api/orders/${selectedOrder.value.orderId}/status`, {
      status: '출고완료'
    })
    if (data.status === 'success') {
      alert('출고완료로 변경되었습니다.')
      fetchOrders()
      fetchOrderDetail(selectedOrder.value.orderId)
    } else {
      alert(data.message || '상태 변경 실패')
    }
  } catch (error) {
    console.error('상태 변경 오류:', error)
    alert('서버 오류로 상태를 변경하지 못했습니다.')
  }
}

// -----------------------------
// 주문취소 처리
// -----------------------------
const cancelOrder = async () => {
  if (!selectedOrder.value) {
    alert('먼저 주문을 선택하세요.')
    return
  }
  if (selectedOrder.value.status !== '대기') {
    alert('대기 상태인 주문만 취소(삭제)할 수 있습니다.')
    return
  }

  if (!confirm('해당 주문을 정말 삭제하시겠습니까?')) {
    return
  }

  try {
    const { data } = await axios.delete(`/api/orders/${selectedOrder.value.orderId}`)
    if (data.status === 'success') {
      alert('주문이 삭제되었습니다.')
      fetchOrders()
      orderDetails.value = []
    } else {
      alert(data.message || '주문 삭제 실패')
    }
  } catch (error) {
    console.error('주문 삭제 오류:', error)
    alert('서버 오류로 주문을 삭제하지 못했습니다.')
  }
}


// -----------------------------
// PDF 출력
// -----------------------------
const exportPDF = async () => {
  try {
    const response = await axios.get(`/api/orders/${selectedOrder.value.orderId}/pdf`, {
      responseType: 'blob' // ✅ PDF binary 데이터 받기
    });

    const blob = new Blob([response.data], { type: 'application/pdf' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `order_${selectedOrder.value.orderId}.pdf`;
    link.click();
    window.URL.revokeObjectURL(url);
  } catch (err) {
    alert('PDF 생성 중 오류가 발생했습니다.');
    console.error(err);
  }
};


// -----------------------------
// 엑셀 / 초기화
// -----------------------------


const exportExcel = () => {
  alert('엑셀 다운로드 기능은 아직 구현되지 않았습니다.')
}

const resetFilters = () => {
  filters.value = {
    startDate: null,
    endDate: null,
    prodName: '',
    status: '',
    orderId: ''
  }
  fetchOrders()
}

// -----------------------------
// watch & lifecycle
// -----------------------------
watch(
  () => selectedOrder.value,
  (newOrder) => {
    if (newOrder && newOrder.orderId) {
      fetchOrderDetail(newOrder.orderId)
    } else {
      orderDetails.value = []
    }
  }
)

onMounted(fetchOrders)
</script>
