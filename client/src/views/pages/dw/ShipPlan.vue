<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import InputText from 'primevue/inputtext';
import InputGroup from 'primevue/inputgroup';
import InputGroupAddon from 'primevue/inputgroupaddon';
import Modal from '@/components/common/Modal.vue';
// (Button / DataTable / Column 은 전역 등록 가정)

const router = useRouter();

/* 검색 폼 */
const search = ref({
  vendorId: '',
  orderId: ''
});

/* 좌측 주문 목록 */
const leftList = ref([]);
const leftSelected = ref([]);  // 여러 주문 선택

/* 우측 상세 */
const detailRows = ref([]);
const selectedDetailRows = ref([]);

/* 모달 상태 */
const showVendorModal = ref(false);
const showOrderModal = ref(false);

/* 모달 이벤트 */
const pickVendor = (r) => {
  search.value.vendorId = r.vendorId;
  showVendorModal.value = false;
};
const pickOrder = (r) => {
  search.value.orderId = r.orderId;
  showOrderModal.value = false;
};

/* 주문 목록 조회 */
const fetchLeftList = async () => {
  try {
    const params = { vendorId: search.value.vendorId, orderId: search.value.orderId };
    const { data } = await axios.get('/api/ship/orders', { params });
    leftList.value = Array.isArray(data) ? data : [];
  } catch (e) {
    console.error('출하지시 목록 조회 실패:', e);
  }
};

/* 선택된 주문 상세 조회 */
const fetchOrderDetails = async () => {
  if (!leftSelected.value.length) {
    detailRows.value = [];
    return;
  }
  try {
    // 여러 주문 중 첫 번째 것만 상세조회
    const orderId = leftSelected.value[0].orderId;
    const { data } = await axios.get('api/ship/order-details', { params: { orderId } });
    detailRows.value = data.data || [];
  } catch (e) {
    console.error('상세조회 실패:', e);
    detailRows.value = [];
  }
};


/* 여러 주문 등록 (헤더 단위) */
const createShipOrders = async () => {
  if (!leftSelected.value.length) {
    alert('주문을 선택하세요.');
    return;
  }
  try {
    const { data } = await axios.post('/api/ship/orders', leftSelected.value);
    if (data.retCode === 'success') {
      alert('출하지시가 등록되었습니다.');
      router.push('/shipping-register');
    } else {
      alert('등록 실패');
    }
  } catch (e) {
    console.error('출하지시 등록 실패:', e);
  }
};

/* 단건 부분출고 등록 (상세 단위) */
const createShipOrderDetails = async () => {
  if (!selectedDetailRows.value.length) {
    alert('부분출고할 품목을 선택하세요.');
    return;
  }
  try {
    const payload = selectedDetailRows.value.map(r => ({
      orderId: r.orderId,
      odetailId: r.odetailId,
      shipQty: r.shipQty || 0
    }));
    const { data } = await axios.post('/api/ship/order-details', payload);
    if (data.retCode === 'success') {
      alert('부분출고가 등록되었습니다.');
      router.push('/shipping-register');
    } else {
      alert('등록 실패');
    }
  } catch (e) {
    console.error('부분출고 등록 실패:', e);
  }
};

/* 선택 변경 시 상세조회 */
watch(leftSelected, fetchOrderDetails);
onMounted(fetchLeftList);
</script>

<template>
  <div class="si-wrap">
    <div class="section-title">출하지시</div>

    <!-- 검색 -->
    <div class="card">
      <div class="box-title">출하지시 검색</div>
      <div class="grid grid--search">
        <div class="field">
          <label>판매처ID</label>
          <InputGroup>
            <InputText v-model="search.vendorId" placeholder="판매처ID" />
            <InputGroupAddon>
              <Button icon="pi pi-search" class="btn-icon" @click="showVendorModal = true" />
            </InputGroupAddon>
          </InputGroup>
        </div>
        <div class="field">
          <label>주문ID</label>
          <InputGroup>
            <InputText v-model="search.orderId" placeholder="주문ID" />
            <InputGroupAddon>
              <Button icon="pi pi-search" class="btn-icon" @click="showOrderModal = true" />
            </InputGroupAddon>
          </InputGroup>
        </div>
        <div class="search-actions">
          <Button class="box-btn box-btn--ghost" icon="pi pi-refresh" label="초기화"
                  @click="() => { search.vendorId=''; search.orderId=''; fetchLeftList(); }" />
          <Button class="box-btn box-btn--green" icon="pi pi-search" label="조회"
                  @click="fetchLeftList" />
        </div>
      </div>
    </div>

    <!-- 좌/우 패널 -->
    <div class="split">
      <!-- 좌측 주문 -->
      <div class="pane">
        <div class="pane-head">
          <div class="pane-title">지시대상 주문</div>
          <div class="pane-actions">
            <Button label="출하지시 생성" class="pill primary"
                    :disabled="!leftSelected.length" @click="createShipOrders" />
          </div>
        </div>
        <DataTable
          :value="leftList"
          dataKey="orderId"
          selectionMode="multiple"
          v-model:selection="leftSelected"
          size="small"
          paginator :rows="7"
        >
          <Column selectionMode="multiple" headerStyle="width:3rem" />
          <Column field="orderId" header="주문ID" />
          <Column field="vendorId" header="판매처ID" />
          <Column field="status" header="상태" />
        </DataTable>
      </div>

      <!-- 우측 상세 -->
      <div class="pane">
        <div class="pane-head">
          <div class="pane-title">부분출고 상세</div>
          <div class="pane-actions">
            <Button label="부분출고 등록" class="pill primary"
                    :disabled="!selectedDetailRows.length" @click="createShipOrderDetails" />
          </div>
        </div>
        <DataTable
          :value="detailRows"
          dataKey="odetailId"
          v-model:selection="selectedDetailRows"
          selectionMode="multiple"
          :metaKeySelection="false"
          paginator :rows="10"
        >
          <Column selectionMode="multiple" />
          <Column field="orderId" header="주문ID" />
          <Column field="prodId" header="제품코드" />
          <Column field="orderQty" header="주문수량" />
          <Column field="shipQty" header="출하수량">
            <template #body="{ data }">
              <InputText v-model="data.shipQty" style="width:70px" placeholder="0" />
            </template>
          </Column>
          <Column field="spec" header="규격" />
          <Column field="unit" header="단위" />
          <Column field="status" header="상태" />
        </DataTable>
      </div>
    </div>
  </div>
</template>



<style scoped>
/* ===================== 레이아웃 기본 ===================== */
.si-wrap { padding: 12px 14px; background: #f5f7fb; }
.section-title { font-size: 18px; font-weight: 700; margin-bottom: 8px; }
.card { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; padding: 14px; margin-bottom: 12px; }
.box-title { font-weight: 700; margin-bottom: 8px; }

/* 검색 영역 */
.grid--search { display: grid; grid-template-columns: repeat(2, minmax(280px, 1fr)); gap: 12px 20px; align-items: end; }
.field { display: flex; flex-direction: column; gap: 6px; }
.btn-icon { padding: 0 10px !important; height: 36px !important; }
.search-actions { grid-column: 1 / -1; display: flex; justify-content: flex-end; gap: 8px; }

/* 버튼 스타일 */
.box-btn { height: 38px !important; border-radius: 8px !important; padding: 0 14px !important; font-weight: 700; }
.box-btn--ghost { background: #fff !important; border: 1px solid #22c55e !important; color: #22c55e !important; }
.box-btn--green { background: #22c55e !important; border: 1px solid #22c55e !important; color: #fff !important; }
.pill { border-radius: 999px !important; padding: 6px 14px !important; }
.pill.primary { background: #22c55e !important; color: #fff !important; border: none !important; }
.pill.danger { background: #ef4444 !important; color: #fff !important; border: none !important; }

/* 2단 split */
.split { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.pane { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; padding: 12px; }
.pane-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.pane-title { font-weight: 700; }
.pane-actions { display: flex; gap: 8px; }

/* 테이블 스타일 */
:deep(.p-datatable-thead > tr > th) { background: #f8fafc; padding: 10px; }
:deep(.p-datatable-tbody > tr > td) { padding: 10px; }
.th-center { text-align: center !important; }
</style>
