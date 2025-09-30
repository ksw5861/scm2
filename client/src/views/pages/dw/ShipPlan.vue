<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import InputText from 'primevue/inputtext';
import InputGroup from 'primevue/inputgroup';
import InputGroupAddon from 'primevue/inputgroupaddon';
import Modal from '@/components/common/Modal.vue';
// Button / Calendar / DataTable / Column 은 전역 등록 가정

const router = useRouter();

/* ===== 검색 폼 ===== */
const search = ref({
  scheduledDate: null, // 출하예정일
  vendorName: '', // 판매처명
  orderNo: '' // 주문번호
});

/* ===== 더미 데이터 (승인완료 → 지시대상) ===== */
const allApprovedOrders = ref([
  { id: 1, scheduledDate: '2025-09-01', vendor: '스타벅스 송도달점', orderNo: 'SO2025-09-17-002', status: '지시대상' },
  { id: 2, scheduledDate: '2025-09-01', vendor: '스타벅스 종로점', orderNo: 'SO2025-09-17-001', status: '지시대상' },
  { id: 3, scheduledDate: '2025-09-02', vendor: '이디야 신촌점', orderNo: 'SO2025-09-18-003', status: '지시대상' },
  { id: 4, scheduledDate: '2025-09-03', vendor: '메가커피 잠실점', orderNo: 'SO2025-09-19-004', status: '지시대상' }
]);
const leftList = ref([...allApprovedOrders.value]); // 좌측 목록
const leftSelected = ref([]); // 좌측 선택

/* ===== 우측: 출하지시 생성 목록 ===== */
const rightList = ref([]);
const rightSelected = ref([]);

/* ===== 모달(팀 패턴) ===== */
const showVendorModal = ref(false);
const showOrderModal = ref(false);

const pickVendor = (r) => {
  search.value.vendorName = r.vendorName;
  showVendorModal.value = false;
};

function pickOrder(r) {
  search.value.orderNo = r.orderNo;
  showOrderModal.value = false;
}

/* ===== 조회/초기화 ===== */
function dstr(d) {
  const t = new Date(d);
  const m = String(t.getMonth() + 1).padStart(2, '0');
  const day = String(t.getDate()).padStart(2, '0');
  return `${t.getFullYear()}-${m}-${day}`;
}

function applySearch() {
  const date = search.value.scheduledDate ? dstr(search.value.scheduledDate) : null;
  const v = (search.value.vendorName || '').trim().toLowerCase();
  const o = (search.value.orderNo || '').trim().toLowerCase();

  leftList.value = allApprovedOrders.value.filter((r) => {
    const dOk = date ? r.scheduledDate === date : true;
    const vOk = v ? r.vendor.toLowerCase().includes(v) : true;
    const oOk = o ? r.orderNo.toLowerCase().includes(o) : true;
    return dOk && vOk && oOk;
  });
  leftSelected.value = [];
}

function resetSearch() {
  search.value = { scheduledDate: null, vendorName: '', orderNo: '' };
  leftList.value = [...allApprovedOrders.value];
  leftSelected.value = [];
}

/* ===== 좌↔우 이동 ===== */
function addSelected() {
  if (!leftSelected.value.length) return;
  const exists = new Set(rightList.value.map((x) => x.id));
  leftSelected.value.forEach((r) => {
    if (!exists.has(r.id)) {
      rightList.value.push({
        id: r.id,
        orderNo: r.orderNo,
        warehouse: '동서울창고', // 더미
        vendor: r.vendor,
        scheduledDate: r.scheduledDate,
        manager: '-'
      });
    }
  });
  leftSelected.value = [];
}
function removeSelected() {
  if (!rightSelected.value.length) return;
  const del = new Set(rightSelected.value.map((x) => x.id));
  rightList.value = rightList.value.filter((r) => !del.has(r.id));
  rightSelected.value = [];
}

/* ===== 출하지시 생성 → 출하등록 이동 ===== */
function createInstruction() {
  if (!rightList.value.length) return alert('출하지시 대상이 없습니다.');
  // TODO: 실제 API 예) await axios.post('/api/ship-plan', rightList.value)
  sessionStorage.setItem('shippingInstructionItems', JSON.stringify(rightList.value));
  router.push('/shipping-register');
}

/* ===== API 호출 ====== */

const fetchLeftList = async () => {

  try {
    const { data } = await axios.get('/api/sadsadasdasdasds');
    if (data.retCode === "success") {
      // 요청 성공시
    } else {
      // 요청 실패시
    }
    
  } catch (e) {
    console.err(e);
    // return Toast();
  } 
  // finally {}
  
};

const fetchVendors = async () => [
  { vendorId: 'V001', vendorName: '스타벅스 송도달점' },
  { vendorId: 'V002', vendorName: '스타벅스 종로점' },
  { vendorId: 'V003', vendorName: '이디야 신촌점' }
];
const fetchOrders = async () => [
  { orderNo: 'SO2025-09-17-002', vendorName: '스타벅스 송도달점', orderDate: '2025-09-16' },
  { orderNo: 'SO2025-09-17-001', vendorName: '스타벅스 종로점', orderDate: '2025-09-16' }
];



onMounted(fetchLeftList);
</script>

<template>
  <div class="si-wrap">
    <div class="section-title">출하지시</div>

    <!-- ① 검색 -->
    <div class="card">
      <div class="box-title">출하지시 검색</div>
      <div class="grid grid--search">
        <div class="field">
          <label>출하예정일</label>
          <Calendar v-model="search.scheduledDate" dateFormat="yy-mm-dd" showIcon class="w-full" />
        </div>

        <div class="field">
          <label>판매처명</label>
          <InputGroup>
            <InputText v-model="search.vendorName" placeholder="판매처명" />
            <InputGroupAddon>
              <Button icon="pi pi-search" class="btn-icon" @click="showVendorModal = true" />
            </InputGroupAddon>
          </InputGroup>
        </div>

        <div class="field">
          <label>주문번호</label>
          <InputGroup>
            <InputText v-model="search.orderNo" placeholder="주문번호" />
            <InputGroupAddon>
              <Button icon="pi pi-search" class="btn-icon" @click="showOrderModal = true" />
            </InputGroupAddon>
          </InputGroup>
        </div>

        <div class="search-actions">
          <Button class="box-btn box-btn--ghost" icon="pi pi-refresh" label="초기화" @click="resetSearch" />
          <Button class="box-btn box-btn--green" icon="pi pi-search" label="조회" @click="applySearch" />
        </div>
      </div>
    </div>

    <!-- ② 좌: 지시대상 / ③ 우: 지시 생성 -->
    <div class="split">
      <div class="pane">
        <div class="pane-head">
          <div class="pane-title">지시대상 주문 목록</div>
          <div class="pane-actions">
            <Button label="선택" class="pill primary" :disabled="!leftSelected.length" @click="addSelected" />
          </div>
        </div>
        <DataTable :value="leftList" dataKey="id" selectionMode="multiple" v-model:selection="leftSelected" size="small" paginator :rows="7">
          <Column selectionMode="multiple" headerStyle="width:3rem" />
          <Column field="scheduledDate" header="출하예정일" />
          <Column field="vendor" header="판매처명" />
          <Column field="orderNo" header="주문번호" />
          <Column field="status" header="상태" :headerClass="'th-center'" />
        </DataTable>
      </div>

      <div class="pane">
        <div class="pane-head">
          <div class="pane-title">출하지시 생성</div>
          <div class="pane-actions">
            <Button label="삭제" class="pill danger" :disabled="!rightSelected.length" @click="removeSelected" />
            <Button label="출하지시 생성" class="pill primary" :disabled="!rightList.length" @click="createInstruction" />
          </div>
        </div>
        <DataTable :value="rightList" dataKey="id" selectionMode="multiple" v-model:selection="rightSelected" size="small" paginator :rows="7">
          <Column selectionMode="multiple" headerStyle="width:3rem" />
          <Column field="orderNo" header="주문번호" />
          <Column field="warehouse" header="출고창고" />
          <Column field="vendor" header="판매처명" />
          <Column field="testtest" header="부분출고"/>
          <Column field="scheduledDate" header="출하예정일" />
          <Column field="manager" header="담당자" />
        </DataTable>
      </div>
    </div>

    <!-- 판매처 모달 -->
    <Modal
      :visible="showVendorModal"
      title="판매처 검색"
      idField="vendorId"
      :columns="[
        { key: 'vendorId', label: '판매처ID' },
        { key: 'vendorName', label: '판매처명' }
      ]"
      :fetchData="fetchVendors"
      :page-size="7"
      @select="pickVendor"
      @close="showVendorModal = false"
    />
    <!-- 주문번호 모달 -->
    <Modal
      :visible="showOrderModal"
      title="주문번호 검색"
      idField="orderNo"
      :columns="[
        { key: 'orderNo', label: '주문번호' },
        { key: 'vendorName', label: '판매처명' },
        { key: 'orderDate', label: '주문일자' }
      ]"
      :fetchData="fetchOrders"
      :page-size="7"
      @select="pickOrder"
      @close="showOrderModal = false"
    />
  </div>
</template>

<style scoped>
.si-wrap {
  padding: 12px 14px;
  background: #f5f7fb;
}
.section-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 8px;
}
.card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 14px;
  margin-bottom: 12px;
}
.box-title {
  font-weight: 700;
  margin-bottom: 8px;
}

/* 검색 레이아웃 */
.grid--search {
  display: grid;
  grid-template-columns: repeat(2, minmax(280px, 1fr));
  gap: 12px 20px;
  align-items: end;
}
.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.btn-icon {
  padding: 0 10px !important;
  height: 36px !important;
}
.search-actions {
  grid-column: 1 / -1;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

/* 버튼 스타일 */
.box-btn {
  height: 38px !important;
  border-radius: 8px !important;
  padding: 0 14px !important;
  font-weight: 700;
}
.box-btn--ghost {
  background: #fff !important;
  border: 1px solid #22c55e !important;
  color: #22c55e !important;
}
.box-btn--green {
  background: #22c55e !important;
  border: 1px solid #22c55e !important;
  color: #fff !important;
}

.pill {
  border-radius: 999px !important;
  padding: 6px 14px !important;
}
.pill.primary {
  background: #22c55e !important;
  color: #fff !important;
  border: none !important;
}
.pill.danger {
  background: #ef4444 !important;
  color: #fff !important;
  border: none !important;
}

/* 2단 split */
.split {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}
.pane {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 12px;
}
.pane-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.pane-title {
  font-weight: 700;
}
.pane-actions {
  display: flex;
  gap: 8px;
}

/* 테이블 */
:deep(.p-datatable-thead > tr > th) {
  background: #f8fafc;
  padding: 10px;
}
:deep(.p-datatable-tbody > tr > td) {
  padding: 10px;
}
.th-center {
  text-align: center !important;
}
</style>
