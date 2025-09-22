<script setup>
import { ref } from 'vue';
import InputText from 'primevue/inputtext';
import InputGroup from 'primevue/inputgroup';
import InputGroupAddon from 'primevue/inputgroupaddon';
import Modal from '@/components/common/Modal.vue';
// Button, Calendar, DataTable, Column, Dialog 은 전역 등록 가정

/* 검색폼 */
const search = ref({
  productName: '',
  productCode: '',
  processedDate: null,
  method: 'all'
});

/* 더미 목록 */
const rows = ref([
  { id: 1, shipNo: 'AB-001', vendor: '스타벅스', product: '아라비카 원두', qty: 100, method: 'return', processedAt: '2025-09-15', handler: '김현수', amount: 500000, imageUrl: '' },
  { id: 2, shipNo: 'AB-002', vendor: '이디야', product: '리베리카 원두', qty: 50, method: 'discard', processedAt: '2025-09-15', handler: '이영희', amount: 200000, imageUrl: '' },
  { id: 3, shipNo: 'AB-003', vendor: '메가커피', product: '콜롬비아 원두', qty: 30, method: 'reject', processedAt: '2025-09-14', handler: '관리자', amount: 0, imageUrl: '' }
]);
const selected = ref([]);

/* 모달 */
const modal = ref({ visible: false });
function openLookup() {
  modal.value.visible = true;
}
function handlePick(row) {
  search.value.productCode = row.prodCode;
  search.value.productName = row.prodName;
  modal.value.visible = false;
}

/* 조회/초기화 */
function applySearch() {
  console.log('조회 실행', search.value);
}
function resetSearch() {
  search.value = { productName: '', productCode: '', processedDate: null, method: 'all' };
}

/* 이미지 확인 */
const showImage = ref(false);
const imageSrc = ref('');
function openImage(row) {
  imageSrc.value = row.imageUrl || '';
  showImage.value = true;
}

/* 유틸 */
const nf = (n) => new Intl.NumberFormat('ko-KR').format(n ?? 0);
function methodLabel(v) {
  return v === 'reject' ? '반려' : v === 'return' ? '재입고' : v === 'discard' ? '폐기' : '전체';
}
</script>

<template>
  <div class="rh-wrap">
    <div class="section-title">반품이력관리</div>

    <!-- 검색 -->
    <div class="card">
      <div class="grid grid--search">
        <div class="field">
          <label>제품명</label>
          <InputGroup>
            <InputText v-model="search.productName" placeholder="A0001" />
            <InputGroupAddon><Button icon="pi pi-search" class="btn-icon" @click="openLookup" /></InputGroupAddon>
          </InputGroup>
        </div>
        <div class="field">
          <label>제품코드</label>
          <InputGroup>
            <InputText v-model="search.productCode" placeholder="A0001" />
            <InputGroupAddon><Button icon="pi pi-search" class="btn-icon" @click="openLookup" /></InputGroupAddon>
          </InputGroup>
        </div>
        <div class="field">
          <label>처리일자</label>
          <Calendar v-model="search.processedDate" dateFormat="yy-mm-dd" showIcon class="w-full" />
        </div>
        <div class="field">
          <label>처리방식</label>
          <div class="radios">
            <div class="radio"><RadioButton inputId="mAll" v-model="search.method" value="all" /><label for="mAll">전체</label></div>
            <div class="radio"><RadioButton inputId="mReject" v-model="search.method" value="reject" /><label for="mReject">반려</label></div>
            <div class="radio"><RadioButton inputId="mReturn" v-model="search.method" value="return" /><label for="mReturn">재입고</label></div>
            <div class="radio"><RadioButton inputId="mDiscard" v-model="search.method" value="discard" /><label for="mDiscard">폐기</label></div>
          </div>
        </div>

        <div class="search-actions">
          <Button class="box-btn box-btn--ghost" icon="pi pi-refresh" label="초기화" @click="resetSearch" />
          <Button class="box-btn box-btn--green" icon="pi pi-search" label="조회" @click="applySearch" />
        </div>
      </div>
    </div>

    <!-- 목록 -->
    <div class="card">
      <DataTable :value="rows" v-model:selection="selected" selectionMode="multiple" dataKey="id" size="small" class="rh-table">
        <Column selectionMode="multiple" headerStyle="width:40px" />
        <Column field="shipNo" header="출고번호" />
        <Column field="vendor" header="거래처명" />
        <Column field="product" header="제품명" />
        <Column field="qty" header="수량" />
        <Column field="method" header="처리방식" :body="(d) => methodLabel(d.method)" />
        <Column field="processedAt" header="처리일자" />
        <Column field="handler" header="처리자" />

        <Column header="금액">
          <template #body="{ data }">
            <div class="cell-amount">
              <span class="num">{{ nf(data.amount) }}</span>
              <Button class="btn-mini gray" label="제품상태확인" @click="openImage(data)" />
            </div>
          </template>
        </Column>
      </DataTable>
    </div>

    <!-- 모달 -->
    <Modal
      :visible="modal.visible"
      title="제품 검색"
      idField="prodCode"
      :columns="[
        { key: 'prodCode', label: '제품코드' },
        { key: 'prodName', label: '제품명' }
      ]"
      :fetchData="async () => [{ prodCode: 'P001', prodName: '아라비카 원두' }]"
      :page-size="5"
      @select="handlePick"
      @close="modal.visible = false"
    />

    <!-- 이미지 확인 -->
    <Dialog v-model:visible="showImage" header="제품상태확인" modal style="width: 48rem">
      <img v-if="imageSrc" :src="imageSrc" class="w-full" alt="제품상태" />
      <div v-else>이미지가 없습니다.</div>
    </Dialog>
  </div>
</template>

<style scoped>
.rh-wrap {
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

.grid--search {
  display: grid;
  grid-template-columns: repeat(2, minmax(280px, 1fr));
  gap: 12px 20px;
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

.rh-table :deep(.p-datatable-thead > tr > th) {
  background: #f8fafc;
  padding: 10px;
}
.rh-table :deep(.p-datatable-tbody > tr > td) {
  padding: 10px;
}
.cell-amount {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
}
.btn-mini {
  height: 28px !important;
  padding: 0 10px !important;
  border-radius: 12px !important;
  font-size: 12px;
}
.btn-mini.gray {
  background: #e5e7eb !important;
  color: #111827 !important;
  border: none !important;
}
.num {
  min-width: 80px;
  text-align: right;
}
</style>
