<script setup>
import { ref, computed } from 'vue';
import axios from 'axios';

import InputText from 'primevue/inputtext';
import InputGroup from 'primevue/inputgroup';
import InputGroupAddon from 'primevue/inputgroupaddon';
import Modal from '@/components/common/Modal.vue';
// (Button / Calendar / DataTable / Column / Dialog / Textarea 전역 등록 가정)

/* ── 검색 폼 ──────────────────────────────────────── */
const search = ref({
  productName: '',
  productCode: '',
  customerName: '',
  expDate: null
});

/* ── 목록/선택 ─────────────────────────────────────── */
const rows = ref([
  { rtnId: 1, shipNo: 'RTN001', productName: '아라비카 원두', productCode: 'P001', customerName: '스타벅스', reason: '파손', qty: 100, status: '대기', processedAt: '-', expDate: '-', manager: '-', returnAmount: 100000, salesDeduct: 100000 },
  { rtnId: 2, shipNo: 'RTN002', productName: '리베리카 원두', productCode: 'P002', customerName: '이디야', reason: '유통기한임박', qty: 80, status: '승인', processedAt: '2025-09-15', expDate: '2026-09-16', manager: '김관리', returnAmount: 120000, salesDeduct: 120000 },
  { rtnId: 3, shipNo: 'RTN003', productName: '리베리카 원두', productCode: 'P002', customerName: '이디야', reason: '잘못된 발주', qty: 50, status: '반려', processedAt: '2025-09-14', expDate: '2026-09-15', manager: '김사원', returnAmount: 100000, salesDeduct: 100000 }
]);
const selected = ref([]);
const hasSelection = computed(() => selected.value.length > 0);

/* ── 공용 검색 모달(팀 스타일) ───────────────────── */
const modal = ref({ visible: false, title: '', idField: '', columns: [], fetcher: null });

const fetchProducts = async () => [
  { prodCode: 'P001', prodName: '아라비카 원두' },
  { prodCode: 'P002', prodName: '리베리카 원두' },
  { prodCode: 'P003', prodName: '케냐 원두' }
];
const fetchCustomers = async () => [
  { custId: 'C001', custName: '스타벅스' },
  { custId: 'C002', custName: '이디야' }
];

function openLookup(type) {
  if (type === 'product' || type === 'productCode') {
    modal.value = {
      visible: true,
      title: '제품 검색',
      idField: 'prodCode',
      columns: [
        { key: 'prodCode', label: '제품코드' },
        { key: 'prodName', label: '제품명' }
      ],
      fetcher: fetchProducts
    };
  } else if (type === 'customer') {
    modal.value = {
      visible: true,
      title: '거래처 검색',
      idField: 'custId',
      columns: [
        { key: 'custId', label: '거래처ID' },
        { key: 'custName', label: '거래처명' }
      ],
      fetcher: fetchCustomers
    };
  }
}
function handlePick(row) {
  if (row.prodCode) {
    search.value.productCode = row.prodCode;
    search.value.productName = row.prodName;
  }
  if (row.custId) {
    search.value.customerName = row.custName;
  }
  modal.value.visible = false;
}

/* ── 조회/초기화 ───────────────────────────────────── */
async function applySearch() {
  const params = {
    productName: search.value.productName || undefined,
    productCode: search.value.productCode || undefined,
    customerName: search.value.customerName || undefined,
    expDate: search.value.expDate ? fmtDate(search.value.expDate) : undefined
  };
  try {
    // const { data } = await axios.get('/api/return-process', { params })
    // rows.value = data
    selected.value = [];
  } catch {
    alert('조회 실패');
  }
}
function resetSearch() {
  search.value = { productName: '', productCode: '', customerName: '', expDate: null };
  selected.value = [];
}

/* ── 반려/승인 ─────────────────────────────────────── */
const showReject = ref(false);
const rejectReason = ref('');
const showApprove = ref(false);

function openReject() {
  if (!hasSelection.value) return;
  rejectReason.value = '';
  showReject.value = true;
}
async function submitReject() {
  try {
    // await axios.post('/api/return-process/reject', { ids:selected.value.map(r=>r.rtnId), reason:rejectReason.value })
    showReject.value = false;
    await applySearch();
  } catch {
    alert('반려 실패');
  }
}
function openApprove() {
  if (!hasSelection.value) return;
  showApprove.value = true;
}
async function submitApprove() {
  try {
    // await axios.post('/api/return-process/approve', { ids:selected.value.map(r=>r.rtnId) })
    showApprove.value = false;
    await applySearch();
  } catch {
    alert('승인 실패');
  }
}

/* ── 유틸 ─────────────────────────────────────────── */
const nf = (n) => new Intl.NumberFormat('ko-KR').format(n ?? 0);
function fmtDate(d) {
  const t = new Date(d);
  const m = String(t.getMonth() + 1).padStart(2, '0');
  const day = String(t.getDate()).padStart(2, '0');
  return `${t.getFullYear()}-${m}-${day}`;
}
</script>

<template>
  <div class="rp-wrap">
    <div class="section-title">반품승인처리</div>

    <!-- 검색 영역 -->
    <div class="card">
      <div class="grid grid--search">
        <div class="field">
          <label>제품명</label>
          <InputGroup>
            <InputText v-model="search.productName" placeholder="ADD01" />
            <InputGroupAddon><Button icon="pi pi-search" class="btn-icon" @click="openLookup('product')" /></InputGroupAddon>
          </InputGroup>
        </div>

        <div class="field">
          <label>제품코드</label>
          <InputGroup>
            <InputText v-model="search.productCode" placeholder="ADD01" />
            <InputGroupAddon><Button icon="pi pi-search" class="btn-icon" @click="openLookup('productCode')" /></InputGroupAddon>
          </InputGroup>
        </div>

        <div class="field">
          <label>거래처명</label>
          <InputGroup>
            <InputText v-model="search.customerName" placeholder="ADD01" />
            <InputGroupAddon><Button icon="pi pi-search" class="btn-icon" @click="openLookup('customer')" /></InputGroupAddon>
          </InputGroup>
        </div>

        <div class="field">
          <label>유통기한</label>
          <Calendar v-model="search.expDate" placeholder="ADD01" showIcon dateFormat="yy-mm-dd" class="w-full" />
        </div>

        <!-- ▶ 버튼행 (조회/초기화 + 승인/반려 같이 오른쪽) -->
        <div class="search-actions">
          <Button class="box-btn box-btn--ghost" icon="pi pi-refresh" label="초기화" @click="resetSearch" />
          <Button class="box-btn box-btn--green" icon="pi pi-search" label="조회" @click="applySearch" />
          <Button label="반려" class="pill danger" :disabled="!hasSelection" @click="openReject" />
          <Button label="승인" class="pill primary" :disabled="!hasSelection" @click="openApprove" />
        </div>
      </div>
    </div>

    <!-- 목록 -->
    <div class="card">
      <DataTable
        :value="rows"
        v-model:selection="selected"
        selectionMode="multiple"
        dataKey="rtnId"
        size="small"
        responsiveLayout="scroll"
        class="rp-table"
      >
        <Column selectionMode="multiple" headerStyle="width:40px" />
        <Column field="shipNo" header="출고번호" />
        <Column field="productName" header="제품명" />
        <Column field="productCode" header="제품코드" />
        <Column field="customerName" header="거래처명" />
        <Column field="reason" header="반품사유" />
        <Column field="qty" header="수량" :headerClass="'th-center'" :bodyClass="'td-num'" />
        <Column field="status" header="상태" :headerClass="'th-center'" />
        <Column field="processedAt" header="처리일자" :headerClass="'th-center'" />
        <Column field="expDate" header="유통기한" :headerClass="'th-center'" />
        <Column field="manager" header="담당자" :headerClass="'th-center'" />
        <Column header="반품금액" :headerClass="'th-right'">
          <template #body="{ data }"><div class="td-num">{{ nf(data.returnAmount) }}</div></template>
        </Column>
        <Column header="매출차감" :headerClass="'th-right'">
          <template #body="{ data }"><div class="td-num">{{ nf(data.salesDeduct) }}</div></template>
        </Column>
      </DataTable>
    </div>

    <!-- 공용 검색 모달 -->
    <Modal
      :visible="modal.visible"
      :title="modal.title"
      :idField="modal.idField"
      :columns="modal.columns"
      :fetchData="modal.fetcher"
      :page-size="7"
      @select="handlePick"
      @close="modal.visible = false"
    />

    <!-- 반려 사유 -->
    <Dialog v-model:visible="showReject" header="반려 사유 입력" modal style="width: 32rem">
      <Textarea v-model="rejectReason" rows="4" autoResize placeholder="반려 사유는 필수입니다." />
      <div class="dlg-actions">
        <Button label="취소" outlined @click="showReject = false" />
        <Button label="반려" class="pill danger" :disabled="!rejectReason.trim()" @click="submitReject" />
      </div>
    </Dialog>

    <!-- 승인 안내 -->
    <Dialog v-model:visible="showApprove" header="승인 처리" modal style="width: 30rem">
      <p class="m-0">승인 시 <b>반품금액만큼 매출 차감</b>이 반영됩니다.</p>
      <div class="dlg-actions">
        <Button label="취소" outlined @click="showApprove = false" />
        <Button label="승인" class="pill primary" @click="submitApprove" />
      </div>
    </Dialog>
  </div>
</template>


<style scoped>
/* Layout */
.rp-wrap {
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

/* Search form */
.grid--search {
  display: grid;
  grid-template-columns: repeat(2, minmax(280px, 1fr));
  gap: 12px 20px;
}
.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-width: 520px;
}
.field label {
  font-size: 12px;
  font-weight: 700;
  color: #374151;
}
.btn-icon {
  padding: 0 10px !important;
  height: 36px !important;
}

/* ▶ 오른쪽 끝으로 보내는 버튼 전용 행 */
.search-actions {
  grid-column: 1 / -1;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 2px;
}

/* 네모 박스 버튼 */
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

/* Table */
.rp-table :deep(.p-datatable-thead > tr > th) {
  background: #f8fafc;
  padding: 10px;
}
.rp-table :deep(.p-datatable-tbody > tr > td) {
  padding: 10px;
  vertical-align: middle;
}
.th-center {
  text-align: center !important;
}
.th-right {
  text-align: right !important;
}
.td-num {
  text-align: right;
  min-width: 80px;
}

/* Bottom actions */
.table-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
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

/* Prime tweaks */
:deep(.p-inputtext) {
  height: 36px;
}
:deep(.p-button) {
  border-radius: 10px;
}
</style>
