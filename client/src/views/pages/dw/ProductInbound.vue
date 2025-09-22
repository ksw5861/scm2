<script setup>
import { ref, onMounted } from 'vue';
import { useAppToast } from '@/composables/useAppToast';
import Modal from '@/components/common/Modal.vue';

import InputText from 'primevue/inputtext';
import InputGroup from 'primevue/inputgroup';
import InputGroupAddon from 'primevue/inputgroupaddon';
// (Button/Checkbox/Calendar/DataTable/Column은 전역 등록 가정)

const { toast } = useAppToast();

/* ------------------ 검색 폼 ------------------ */
const searchForm = ref({
  prodCode: '',
  prodName: '',
  makerDate: null,
  inStatuses: [] // ['입고완료','생산완료']
});

/* ------------------ 목록(원본/결과) ------------------ */
const allRows = ref([
  { lotNo: 'LOT-111', prodCode: 'PRD001', prodName: '리베리카 원두', qty: 100, status: '생산완료' },
  { lotNo: 'LOT-112', prodCode: 'PRD002', prodName: '로부스타 원두',  qty: 50,  status: '생산완료' },
  { lotNo: 'LOT-113', prodCode: 'PRD003', prodName: '아라비카 원두',  qty: 200, status: '입고완료' }
]);
const inboundList = ref([]);

/* ------------------ 상세(항상 표시) ------------------ */
const selectedRow = ref(null);
const detail = ref({
  prodCode: '', prodName: '', lotNo: '',
  qty: null, stockNow: 300, stockAfter: null,
  prodDate: null, expireDate: null, unitSpec: '1kg/1box',
  inStatus: '', whCode: '',
  manager: '김민서', inDate: '2025-09-15', user: '로그인 사용자 자동기록'
});

onMounted(() => {
  inboundList.value = [...allRows.value];
  if (inboundList.value.length) bindDetail(inboundList.value[0]);
});

function bindDetail(row) {
  selectedRow.value = row;
  detail.value.prodCode = row?.prodCode ?? '';
  detail.value.prodName = row?.prodName ?? '';
  detail.value.lotNo    = row?.lotNo ?? '';
  detail.value.qty      = row?.qty ?? null;
  detail.value.inStatus = row?.status ?? '';
  const base = Number(detail.value.stockNow) || 0;
  const add  = Number(detail.value.qty) || 0;
  detail.value.stockAfter = base + add;
}
function onSelectRow(e){ bindDetail(e.data); }

/* ------------------ 조회(로컬 필터) ------------------ */
function doSearch() {
  const code = (searchForm.value.prodCode || '').trim().toLowerCase();
  const name = (searchForm.value.prodName || '').trim().toLowerCase();
  const statuses = searchForm.value.inStatuses;

  inboundList.value = allRows.value.filter(r => {
    const codeOk = code ? r.prodCode.toLowerCase().includes(code) : true;
    const nameOk = name ? r.prodName.toLowerCase().includes(name) : true;
    const statusOk = statuses.length ? statuses.includes(r.status) : true;
    return codeOk && nameOk && statusOk;
  });

  if (inboundList.value.length) bindDetail(inboundList.value[0]);
  else {
    selectedRow.value = null;
    detail.value = { ...detail.value, prodCode:'', prodName:'', lotNo:'', qty:null, inStatus:'', stockAfter:null };
  }
}

function resetSearch() {
  searchForm.value = { prodCode:'', prodName:'', makerDate:null, inStatuses:[] };
  inboundList.value = [...allRows.value];
  if (inboundList.value.length) bindDetail(inboundList.value[0]);
}

/* ------------------ 제품 검색 모달 ------------------ */
const isShowProdModal = ref(false);
function openProdModal(){ isShowProdModal.value = true; }

// 더미 데이터(PRD / 원두) 출력
const fetchProductData = async () => {
  return [
    { prodId:'PRD001', prodName:'리베리카 원두' },
    { prodId:'PRD002', prodName:'로부스타 원두' },
    { prodId:'PRD003', prodName:'아라비카 원두' }
  ];
};

function handleSelectProduct(item){
  const code = item?.prodId || '';
  const name = item?.prodName || '';
  searchForm.value.prodCode = code;
  searchForm.value.prodName = name;
  detail.value.prodCode = code;
  detail.value.prodName = name;

  isShowProdModal.value = false;
  toast.info(`제품 선택: ${code} / ${name}`);
  doSearch(); // 선택 즉시 조건검색 실행
}

/* ------------------ 저장/삭제 자리 ------------------ */
function save(){ toast.success('저장 준비 완료 (내일 서버 연결)'); }
function remove(){ toast.warn('삭제 준비 완료 (내일 서버 연결)'); }
</script>

<template>
  <div class="page-wrap">
    <div class="page-title">생산 완료된 제품을 등록</div>

    <!-- 상단 박스: 제품입고 + 검색 -->
    <div class="box">
      <div class="box-title">제품입고</div>

      <div class="form-grid">
        <!-- 제품코드: InputGroup(우측 아이콘 붙임) -->
        <div class="field">
          <label>제품코드</label>
          <InputGroup>
            <InputText v-model="searchForm.prodCode" placeholder="PRD001" />
            <InputGroupAddon>
              <Button icon="pi pi-search" severity="secondary" text @click="openProdModal"/>
            </InputGroupAddon>
          </InputGroup>
        </div>

        <!-- 제품명: InputGroup(우측 아이콘 붙임) -->
        <div class="field">
          <label>제품명</label>
          <InputGroup>
            <InputText v-model="searchForm.prodName" placeholder="원두" />
            <InputGroupAddon>
              <Button icon="pi pi-search" severity="secondary" text @click="openProdModal"/>
            </InputGroupAddon>
          </InputGroup>
        </div>

        <!-- 생산일자: 캘린더(기존처럼) -->
        <div class="field">
          <label>생산일자</label>
          <Calendar v-model="searchForm.makerDate" dateFormat="yy-mm-dd" showIcon class="w-full" />
        </div>

        <!-- 입고 상태 -->
        <div class="field field--statuses">
          <label>입고 상태</label>
          <div class="checks">
            <Checkbox v-model="searchForm.inStatuses" inputId="statusDone" value="입고완료" />
            <label for="statusDone">입고 완료</label>
            <Checkbox v-model="searchForm.inStatuses" inputId="statusProd" value="생산완료" />
            <label for="statusProd">생산 완료</label>
          </div>
        </div>
      </div>

      <div class="actions">
        <Button label="초기화" icon="pi pi-refresh" severity="secondary" outlined @click="resetSearch" />
        <Button label="조회"   icon="pi pi-search"  severity="success"  @click="doSearch" />
      </div>
    </div>

    <!-- 본문: 목록 + 상세(항상 표시) -->
    <div class="split">
      <!-- 목록 -->
      <div class="list-box">
        <DataTable
          :value="inboundList"
          dataKey="lotNo"
          selectionMode="single"
          :selection="selectedRow"
          @rowSelect="onSelectRow"
          @update:selection="val => selectedRow = val"
          class="datatable"
        >
        <Column selectionMode="single" headerStyle="width:3rem" />
        <Column field="lotNo"    header="LOT번호" />
        <Column field="prodCode" header="제품코드" />
        <Column field="prodName" header="제품명" />
        <Column field="qty"      header="수량" />
        <Column field="status"   header="상태" />
        </DataTable>
      </div>

      <!-- 상세 -->
      <div class="detail-box">
        <div class="detail-head">
          <div class="detail-title">상세</div>
          <div class="head-actions">
            <Button label="삭제" icon="pi pi-trash" severity="danger" outlined @click="remove" />
            <Button label="저장" icon="pi pi-save"  severity="primary" @click="save" />
          </div>
        </div>

        <div class="detail-grid">
          <div class="field"><label>제품코드</label><InputText v-model="detail.prodCode" /></div>
          <div class="field"><label>제품명</label><InputText v-model="detail.prodName" /></div>
          <div class="field"><label>LOT번호</label><InputText v-model="detail.lotNo" /></div>

          <div class="field"><label>수량</label><InputText v-model="detail.qty" /></div>
          <div class="field"><label>현재 잔여 재고</label><InputText v-model="detail.stockNow" /></div>
          <div class="field"><label>입고 후 재고</label><InputText v-model="detail.stockAfter" /></div>

          <div class="field"><label>생산일자</label><Calendar v-model="detail.prodDate" dateFormat="yy-mm-dd" showIcon /></div>
          <div class="field"><label>유통기한</label><Calendar v-model="detail.expireDate" dateFormat="yy-mm-dd" showIcon /></div>

          <div class="field"><label>규격/단위</label><InputText v-model="detail.unitSpec" /></div>
          <div class="field"><label>입고상태</label><InputText v-model="detail.inStatus" /></div>

          <div class="field"><label>담당자</label><InputText v-model="detail.manager" /></div>
          <div class="field"><label>입고일자</label><InputText v-model="detail.inDate" /></div>

          <div class="field field--full"><label>사용자</label><InputText v-model="detail.user" /></div>
        </div>
      </div>
    </div>

    <!-- 제품 검색 모달 -->
    <Modal
      :visible="isShowProdModal"
      title="제품 검색"
      idField="prodId"
      :columns="[
        { key: 'prodId',   label: '제품코드' },
        { key: 'prodName', label: '제품명' }
      ]"
      :fetchData="fetchProductData"
      :page-size="5"
      @select="handleSelectProduct"
      @close="isShowProdModal = false"
    />
  </div>
</template>

<style scoped>
.page-wrap{padding:16px;background:#f5f7fb;}
.page-title{font-weight:700;font-size:18px;margin-bottom:12px;}
.box{background:#fff;border:1px solid #e5e7eb;border-radius:10px;padding:16px;margin-bottom:12px;}
.box-title{font-weight:700;margin-bottom:12px;}

.form-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:12px;}
.field{display:flex;flex-direction:column;gap:6px;}
.field--statuses .checks{display:flex;gap:12px;align-items:center;}

.actions{display:flex;gap:8px;justify-content:flex-end;margin-top:8px;}
.split{display:grid;grid-template-columns:1.2fr 1fr;gap:14px;}
.list-box{background:#fff;border:1px solid #e5e7eb;border-radius:10px;padding:12px;}
.datatable :deep(.p-datatable-thead > tr > th){background:#fafafa;}
.detail-box{background:#fff;border:1px solid #e5e7eb;border-radius:10px;padding:12px;}
.detail-head{display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;}
.detail-title{font-weight:700;}
.head-actions{display:flex;gap:8px;}
.detail-grid{display:grid;grid-template-columns:repeat(2,1fr);gap:12px;}
.field--full{grid-column:1 / -1;}
</style>
