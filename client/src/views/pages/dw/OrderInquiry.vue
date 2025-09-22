<script setup>
import { ref, computed, onMounted } from 'vue';
import Modal from '@/components/common/Modal.vue';
import InputText from 'primevue/inputtext';
import InputGroup from 'primevue/inputgroup';
import InputGroupAddon from 'primevue/inputgroupaddon';
// Button / Calendar / DataTable / Column 은 전역 등록 가정

/* ============ 검색 폼(모달/달력, 조회 버튼으로만 검색) ============ */
const search = ref({
  fromDate: null,
  toDate: null,
  vendorName: '',
  statuses: [] // ['대기','완료']
});

/* ============ 더미데이터(내일 API로 교체) ============ */
// 좌측 “주문 헤더” 리스트(요약)
const allOrders = ref([
  { orderDate: '2025-09-11', vendor: '종로점',     summary: '아라비카 원두 외 6건',  total: 2160000, status: '완료', orderNo: 'SO-001' },
  { orderDate: '2025-09-10', vendor: '대림점',     summary: '리베리카 원두',        total:  500000, status: '대기', orderNo: 'SO-002' },
  { orderDate: '2025-09-10', vendor: '강남점',     summary: '로부스타 원두',        total: 1000000, status: '완료', orderNo: 'SO-003' },
  { orderDate: '2025-09-09', vendor: '성북점',     summary: '로부스타 원두',        total: 1000000, status: '대기', orderNo: 'SO-004' },
  { orderDate: '2025-09-09', vendor: '반월점',     summary: '로부스타 원두',        total: 1000000, status: '완료', orderNo: 'SO-005' },
]);

// 우측 “주문 상세” (orderNo → rows)
const orderDetailsMap = {
  'SO-001': [
    { code:'AB-012', name:'아라비카 원두', spec:'1 kg 팩',    unit:'Box(24팩)', price:480000, qty: 2, amount: 960000,  approve:'승인' },
    { code:'AB-012', name:'온고잉모리',   spec:'100ea 줄',    unit:'Box(30줄)',  price: 50000, qty: 4, amount: 200000,  approve:'승인' },
    { code:'AB-012', name:'냉장500미리',  spec:'100ea 줄',    unit:'Box(30줄)',  price: 50000, qty: 4, amount: 200000,  approve:'승인' },
    { code:'AB-012', name:'녹차 파우더',   spec:'1 kg 팩',     unit:'팩(1kg)',    price: 30000, qty: 1, amount:  30000,  approve:'승인' },
  ],
  'SO-002': [
    { code:'RB-020', name:'리베리카 원두', spec:'20 kg',      unit:'Box(1)',     price:500000, qty: 1, amount: 500000,  approve:'대기' },
  ],
  'SO-003': [
    { code:'RS-010', name:'로부스타 원두', spec:'20 kg',      unit:'Box(1)',     price:500000, qty: 2, amount:1000000,  approve:'반려' },
  ],
  'SO-004': [
    { code:'RS-010', name:'로부스타 원두', spec:'20 kg',      unit:'Box(1)',     price:500000, qty: 2, amount:1000000,  approve:'대기' },
  ],
  'SO-005': [
    { code:'RS-010', name:'로부스타 원두', spec:'20 kg',      unit:'Box(1)',     price:500000, qty: 2, amount:1000000,  approve:'승인' },
  ],
};

/* ============ 상태 ============ */
const list = ref([]);
const selectedHeader = ref(null);
const detailRows = ref([]);

onMounted(() => {
  resetSearch(); // 초기 로딩
});

/* ============ 조회/리셋 (조회 버튼으로만 실행) ============ */
function applySearch() {
  const from = search.value.fromDate ? new Date(search.value.fromDate) : null;
  const to   = search.value.toDate   ? new Date(search.value.toDate)   : null;
  const v    = (search.value.vendorName || '').trim().toLowerCase();
  const sts  = search.value.statuses;

  list.value = allOrders.value.filter(o => {
    const d = new Date(o.orderDate);
    const dateOk = (!from || d >= from) && (!to || d <= to);
    const venOk  = v ? o.vendor.toLowerCase().includes(v) : true;
    const stOk   = sts.length ? sts.includes(o.status) : true;
    return dateOk && venOk && stOk;
  });

  if (list.value.length) selectHeader(list.value[0]);
  else {
    selectedHeader.value = null;
    detailRows.value = [];
  }
}

function resetSearch() {
  search.value = { fromDate:null, toDate:null, vendorName:'', statuses:[] };
  list.value = [...allOrders.value];
  if (list.value.length) selectHeader(list.value[0]);
}

/* ============ 좌측 선택 → 우측 상세 바인딩 ============ */
function selectHeader(row) {
  selectedHeader.value = row;
  detailRows.value = (orderDetailsMap[row.orderNo] || []).map(r => ({
    ...r,
    amount: r.price * r.qty
  }));
}

/* ============ 모달(판매처) — 선택 시 바인딩만, 검색 X ============ */
const showVendorModal = ref(false);
const fetchVendors = async () => ([
  { vendorId:'V001', vendorName:'종로점' },
  { vendorId:'V002', vendorName:'대림점' },
  { vendorId:'V003', vendorName:'강남점' },
  { vendorId:'V004', vendorName:'성북점' },
  { vendorId:'V005', vendorName:'반월점' },
]);
function pickVendor(row){
  search.value.vendorName = row.vendorName;
  showVendorModal.value = false; // ❌ 즉시 검색 안 함 (조회 버튼으로만)
}

/* ============ 유틸/액션 ============ */
function fmt(n){ try{ return new Intl.NumberFormat('ko-KR').format(n);}catch{ return String(n);} }
function exportExcel(){ /* TODO: 내일 실제 엑셀 유틸 연결 */ alert('엑셀 다운로드 (자리)'); }
function printPDF(){ /* TODO: 내일 PDF 유틸 연결 */ alert('PDF 출력 (자리)'); }
</script>

<template>
  <div class="page-wrap">
    <div class="page-title">주문조회(본사)</div>

    <!-- 검색 박스 -->
    <div class="box">
      <div class="box-title">주문조회 (본사)</div>
      <div class="desc">주문조회 페이지 입니다.</div>

      <div class="form-grid-4">
        <div class="field">
          <label>주문일자</label>
          <div class="flex gap-2">
            <Calendar v-model="search.fromDate" dateFormat="yy-mm-dd" showIcon class="w-full" />
            <Calendar v-model="search.toDate"   dateFormat="yy-mm-dd" showIcon class="w-full" />
          </div>
        </div>

        <div class="field">
          <label>판매처명</label>
          <InputGroup>
            <InputText v-model="search.vendorName" placeholder="선택" />
            <InputGroupAddon>
              <Button icon="pi pi-search" severity="secondary" text @click="showVendorModal = true"/>
            </InputGroupAddon>
          </InputGroup>
        </div>

        <div class="field">
          <label>주문상태</label>
          <div class="flex items-center gap-3">
            <div class="flex items-center gap-2">
              <Checkbox v-model="search.statuses" inputId="stWait" value="대기" />
              <label for="stWait">대기</label>
            </div>
            <div class="flex items-center gap-2">
              <Checkbox v-model="search.statuses" inputId="stDone" value="완료" />
              <label for="stDone">완료</label>
            </div>
          </div>
        </div>

        <div class="field actions-top">
          <div class="flex gap-2 justify-end">
            <Button label="초기화" icon="pi pi-refresh" severity="secondary" outlined @click="resetSearch" />
            <Button label="조회"   icon="pi pi-search"  severity="success"  @click="applySearch" />
          </div>
        </div>
      </div>

      <div class="flex gap-2 justify-end mt-2">
        <Button label="PDF 출력" icon="pi pi-file-pdf" severity="help" outlined @click="printPDF"/>
        <Button label="엑셀 다운로드" icon="pi pi-file-excel" severity="success" outlined @click="exportExcel"/>
      </div>
    </div>

    <!-- 본문 2단: 좌측 목록 / 우측 상세 -->
    <div class="split">
      <!-- 좌측 목록 -->
      <div class="list-box">
        <DataTable
          :value="list"
          dataKey="orderNo"
          selectionMode="single"
          :selection="selectedHeader"
          @rowSelect="e => selectHeader(e.data)"
          @update:selection="val => selectedHeader = val"
          paginator :rows="9"
        >
          <Column field="vendor"    header="판매처명" />
          <Column field="orderDate" header="주문일자" />
          <Column field="summary"   header="제품명" />
          <Column field="total"     header="주문총액(원)" :body="d => fmt(d.total)" />
          <Column field="status"    header="상태" />
        </DataTable>
      </div>

      <!-- 우측 상세 -->
      <div class="detail-box">
        <DataTable :value="detailRows" dataKey="code" paginator :rows="9">
          <Column field="code"   header="제품코드" />
          <Column field="name"   header="제품명" />
          <Column field="spec"   header="규격" />
          <Column field="unit"   header="단위" />
          <Column field="price"  header="제품가격" :body="d => fmt(d.price)" />
          <Column field="qty"    header="주문수량" />
          <Column field="amount" header="합계"      :body="d => fmt(d.amount)" />
          <Column field="approve" header="승인여부" />
        </DataTable>
      </div>
    </div>

    <!-- 판매처 모달 -->
    <Modal
      :visible="showVendorModal"
      title="판매처 검색"
      idField="vendorId"
      :columns="[
        { key:'vendorId',   label:'판매처ID' },
        { key:'vendorName', label:'판매처명' }
      ]"
      :fetchData="fetchVendors"
      :page-size="7"
      @select="pickVendor"
      @close="showVendorModal = false"
    />
  </div>
</template>

<style scoped>
.page-wrap{padding:16px;background:#f5f7fb;}
.page-title{font-weight:700;font-size:18px;margin-bottom:12px;}
.box{background:#fff;border:1px solid #e5e7eb;border-radius:10px;padding:16px;margin-bottom:12px;}
.box-title{font-weight:700;margin-bottom:6px;}
.desc{color:#6b7280;font-size:12px;margin-bottom:10px;}
.form-grid-4{display:grid;grid-template-columns:repeat(3,1fr);gap:12px;align-items:end;}
.field{display:flex;flex-direction:column;gap:6px;}
.actions-top{grid-column:3/4;}
.split{display:grid;grid-template-columns:1fr 1fr;gap:14px;}
.list-box,.detail-box{background:#fff;border:1px solid #e5e7eb;border-radius:10px;padding:12px;}
</style>
