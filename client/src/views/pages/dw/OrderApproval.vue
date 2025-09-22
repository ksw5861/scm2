<script setup>
import { ref, computed, onMounted } from 'vue';
import Modal from '@/components/common/Modal.vue';
import InputText from 'primevue/inputtext';
import InputGroup from 'primevue/inputgroup';
import InputGroupAddon from 'primevue/inputgroupaddon';
// Button/Calendar/DataTable/Column 은 전역 등록 가정

/* ===================== 검색 폼 ===================== */
const search = ref({
  fromDate: null,
  toDate: null,
  vendorName: '',
  orderNo: ''
});

/* ===================== 더미 데이터 (내일 API 교체) ===================== */
const allOrders = ref([
  { orderDate: '2025-09-16', vendor: '스타벅스 송도달점', orderNo: 'SO2025-09-17-002' },
  { orderDate: '2025-09-16', vendor: '스타벅스 종로점',   orderNo: 'SO2025-09-17-001' }
]);
const orderDetailsMap = {
  'SO2025-09-17-002': [
    { code:'P001', name:'콜롬비아 원두', spec:'20kg', unit:'BOX(4)', price:360000, qty:10 },
    { code:'P002', name:'에티오피아 원두', spec:'20kg', unit:'BOX(4)', price:420000, qty:25 },
    { code:'P003', name:'케냐 원두',     spec:'20kg', unit:'BOX(4)', price:360000, qty:15 },
    { code:'P004', name:'과테말라 원두', spec:'20kg', unit:'BOX(4)', price:450000, qty:30 }
  ],
  'SO2025-09-17-001': [
    { code:'P001', name:'콜롬비아 원두', spec:'20kg', unit:'BOX(4)', price:360000, qty: 8 },
    { code:'P003', name:'케냐 원두',     spec:'20kg', unit:'BOX(4)', price:360000, qty:12 }
  ]
};

/* ===================== 목록/상세 상태 ===================== */
const orderList = ref([]);
const selectedOrder = ref(null);
const detailRows = ref([]);
const selectedDetailRows = ref([]);

/* ===================== 조회/리셋 ===================== */
function applySearch() {
  const from = search.value.fromDate ? new Date(search.value.fromDate) : null;
  const to   = search.value.toDate   ? new Date(search.value.toDate)   : null;
  const v    = (search.value.vendorName || '').trim().toLowerCase();
  const no   = (search.value.orderNo || '').trim().toLowerCase();

  orderList.value = allOrders.value.filter(o => {
    const d = new Date(o.orderDate);
    const dateOk = (!from || d >= from) && (!to || d <= to);
    const venOk  = v ? o.vendor.toLowerCase().includes(v) : true;
    const noOk   = no ? o.orderNo.toLowerCase().includes(no) : true;
    return dateOk && venOk && noOk;
  });

  if (orderList.value.length) selectOrder(orderList.value[0]);
  else {
    selectedOrder.value = null;
    detailRows.value = [];
    selectedDetailRows.value = [];
  }
}

function resetSearch() {
  search.value = { fromDate:null, toDate:null, vendorName:'', orderNo:'' };
  orderList.value = [...allOrders.value];
  if (orderList.value.length) selectOrder(orderList.value[0]);
}

/* ===================== 주문 선택/합계/승인 ===================== */
function selectOrder(row) {
  selectedOrder.value = row;
  detailRows.value = (orderDetailsMap[row.orderNo] || []).map(x => ({
    ...x, amount: x.price * x.qty
  }));
  selectedDetailRows.value = [];
}

const totalAmount = computed(() =>
  detailRows.value.reduce((s, r) => s + r.amount, 0)
);
const selectedAmount = computed(() =>
  selectedDetailRows.value.reduce((s, r) => s + r.amount, 0)
);

function approveSelected() {
  if (!selectedDetailRows.value.length) return;
  alert('선택된 주문 품목 승인 (내일 API 연결)');
}

/* ===================== 모달: 판매처/주문번호 ===================== */
const showVendorModal = ref(false);
const showOrderNoModal = ref(false);

const fetchVendors = async () => ([
  { vendorId: 'V001', vendorName: '스타벅스 송도달점' },
  { vendorId: 'V002', vendorName: '스타벅스 종로점' },
  { vendorId: 'V003', vendorName: '이디야 신촌점' }
]);

const fetchOrderNos = async () => ([
  { orderNo: 'SO2025-09-17-002', vendorName:'스타벅스 송도달점', orderDate:'2025-09-16' },
  { orderNo: 'SO2025-09-17-001', vendorName:'스타벅스 종로점',   orderDate:'2025-09-16' }
]);

function handlePickVendor(row){
  search.value.vendorName = row.vendorName;
  showVendorModal.value = false;
  // ❌ 즉시 검색 제거 (조회 버튼 눌러야 적용)
}
function handlePickOrderNo(row){
  search.value.orderNo = row.orderNo;
  showOrderNoModal.value = false;
  // ❌ 즉시 검색 제거 (조회 버튼 눌러야 적용)
}

/* ===================== 초기 로딩 ===================== */
onMounted(() => {
  orderList.value = [...allOrders.value];
  if (orderList.value.length) selectOrder(orderList.value[0]);
});

/* ===================== 유틸 ===================== */
function fmtCurrency(n){ 
  try{ return new Intl.NumberFormat('ko-KR').format(n);} 
  catch{ return String(n);} 
}
</script>

<template>
  <div class="page-wrap">
    <div class="page-title">판매처에서 접수된 주문건을 승인</div>

    <!-- (1) 주문 승인 검색 -->
    <div class="box">
      <div class="box-title">주문 승인 검색</div>

      <div class="form-grid-4">
        <div class="field">
          <label>주문 일자</label>
          <div class="flex gap-2">
            <Calendar v-model="search.fromDate" dateFormat="yy-mm-dd" showIcon class="w-full" />
            <Calendar v-model="search.toDate"   dateFormat="yy-mm-dd" showIcon class="w-full" />
          </div>
        </div>

        <!-- 판매처명 -->
        <div class="field">
          <label>판매처명</label>
          <InputGroup>
            <InputText v-model="search.vendorName" placeholder="판매처명" />
            <InputGroupAddon>
              <Button icon="pi pi-search" severity="secondary" text @click="showVendorModal = true"/>
            </InputGroupAddon>
          </InputGroup>
        </div>

        <!-- 주문번호 -->
        <div class="field">
          <label>주문번호</label>
          <InputGroup>
            <InputText v-model="search.orderNo" placeholder="주문번호" />
            <InputGroupAddon>
              <Button icon="pi pi-search" severity="secondary" text @click="showOrderNoModal = true"/>
            </InputGroupAddon>
          </InputGroup>
        </div>
      </div>

      <div class="actions">
        <Button label="초기화" icon="pi pi-refresh" severity="secondary" outlined @click="resetSearch" />
        <Button label="조회"   icon="pi pi-search"  severity="success"  @click="applySearch" />
      </div>
    </div>

    <!-- 본문 2단 -->
    <div class="split">
      <!-- (2) 승인 대기 주문 목록 -->
      <div class="list-box">
        <div class="sub-title">승인 대기 주문 목록</div>
        <DataTable
          :value="orderList"
          dataKey="orderNo"
          selectionMode="single"
          :selection="selectedOrder"
          @rowSelect="e => selectOrder(e.data)"
          @update:selection="val => selectedOrder = val"
          paginator :rows="7"
        >
          <Column field="orderDate" header="주문 일자" />
          <Column field="vendor"    header="판매처명" />
          <Column field="orderNo"   header="주문번호" />
        </DataTable>
      </div>

      <!-- (3) 주문 상세 -->
      <div class="detail-box">
        <div class="detail-head">
          <div class="detail-title">
            주문 상세
            <span class="badge">주문 총액 {{ fmtCurrency(totalAmount) }}원</span>
            <span class="badge badge--accent">선택된 주문건 금액 {{ fmtCurrency(selectedAmount) }}원</span>
          </div>
          <div class="head-actions">
            <Button
              label="선택된 주문건 승인"
              icon="pi pi-check"
              :disabled="!selectedDetailRows.length"
              @click="approveSelected"
            />
          </div>
        </div>

        <DataTable
          :value="detailRows"
          dataKey="code"
          :selection="selectedDetailRows"
          @update:selection="val => selectedDetailRows = val"
          selectionMode="multiple"
          paginator :rows="8"
        >
          <Column selectionMode="multiple" headerStyle="width:3rem" />
          <Column field="code"  header="제품 번호" />
          <Column field="name"  header="제품명" />
          <Column field="spec"  header="규격" />
          <Column field="unit"  header="단위" />
          <Column field="price" header="단가" :body="d => fmtCurrency(d.price)" />
          <Column field="qty"   header="수량" />
          <Column field="amount" header="금액" :body="d => fmtCurrency(d.amount)" />
        </DataTable>
      </div>
    </div>

    <!-- 판매처 모달 -->
    <Modal
      :visible="showVendorModal"
      title="판매처 검색"
      idField="vendorId"
      :columns="[
        { key: 'vendorId',   label: '판매처ID' },
        { key: 'vendorName', label: '판매처명' }
      ]"
      :fetchData="fetchVendors"
      :page-size="5"
      @select="handlePickVendor"
      @close="showVendorModal = false"
    />

    <!-- 주문번호 모달 -->
    <Modal
      :visible="showOrderNoModal"
      title="주문번호 검색"
      idField="orderNo"
      :columns="[
        { key: 'orderNo',   label: '주문번호' },
        { key: 'vendorName',label: '판매처명' },
        { key: 'orderDate', label: '주문일자' }
      ]"
      :fetchData="fetchOrderNos"
      :page-size="5"
      @select="handlePickOrderNo"
      @close="showOrderNoModal = false"
    />
  </div>
</template>

<style scoped>
.page-wrap{padding:16px;background:#f5f7fb;}
.page-title{font-weight:700;font-size:18px;margin-bottom:12px;}
.box{background:#fff;border:1px solid #e5e7eb;border-radius:10px;padding:16px;margin-bottom:12px;}
.box-title{font-weight:700;margin-bottom:12px;}
.form-grid-4{display:grid;grid-template-columns:repeat(3,1fr);gap:12px;}
.field{display:flex;flex-direction:column;gap:6px;}
.actions{display:flex;gap:8px;justify-content:flex-end;margin-top:8px;}
.split{display:grid;grid-template-columns:1.1fr 1.4fr;gap:14px;}
.list-box,.detail-box{background:#fff;border:1px solid #e5e7eb;border-radius:10px;padding:12px;}
.sub-title{font-weight:700;margin-bottom:8px;}
.detail-head{display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;}
.detail-title{font-weight:700;display:flex;align-items:center;gap:10px;flex-wrap:wrap;}
.badge{background:#eef2ff;color:#1d4ed8;border-radius:9999px;padding:4px 10px;font-weight:600;}
.badge--accent{background:#fff7ed;color:#c2410c}
.head-actions{display:flex;gap:8px;}
</style>
