<!-- ======================================================
📄 Inbound.vue (모달 컬럼 분리 + 체크박스 복구 + 생산일자 제거 완전체)
====================================================== -->
<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useAppToast } from '@/composables/useAppToast';
import Modal from '@/components/common/Modal.vue';

import InputText from 'primevue/inputtext';
import InputGroup from 'primevue/inputgroup';
import InputGroupAddon from 'primevue/inputgroupaddon';
import Button from 'primevue/button';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Breadcrumb from 'primevue/breadcrumb'; // ✅ 추가
import { useRoute } from 'vue-router'; // ✅ 추가
import { computed } from 'vue'; // ✅ 추가
import { useIcon } from '@/composables/useIcon'; // ✅ 추가

const { toast } = useAppToast();
/* ------------------ 브레드크럼 ------------------ */
const route = useRoute();
const breadcrumbHome = { icon: useIcon('home'), to: '/' };
const breadcrumbItems = computed(() => {
  const matched = route.matched.filter((r) => r.meta);
  if (!matched.length) return [];
  const current = matched[matched.length - 1];
  const parentLabel = current.meta?.breadcrumb?.parent || '';
  const currentLabel = current.name || '';
  return [{ label: parentLabel }, { label: currentLabel, to: route.fullPath }];
});

/* ------------------ 유틸 ------------------ */
function fmtDate(d) {
  if (!d) return '';
  try {
    const dt = typeof d === 'string' ? new Date(d) : d;
    return `${dt.getFullYear()}-${String(dt.getMonth() + 1).padStart(2, '0')}-${String(dt.getDate()).padStart(2, '0')}`;
  } catch {
    return '';
  }
}

/* ------------------ 로그인 사용자 ------------------ */
const currentUser = ref('');
async function loadCurrentUser() {
  try {
    const { data } = await axios.get('/api/user/me');
    currentUser.value = data?.userName ?? '';
  } catch {
    currentUser.value = '관리자';
  }
}

/* ------------------ 검색 폼 ------------------ */
const searchForm = ref({ prodId: '', prodName: '' });

/* ------------------ 목록/상세 ------------------ */
const inboundList = ref([]);
const selectedRow = ref(null);

/* ✅ 페이지네이션 상태 */
const page = ref(1);
const rows = ref(10);
const totalRecords = ref(0);

const detail = ref({
  prodId: '',
  prodName: '',
  prodNo: '',
  proQty: null,
  proDate: '',
  endDate: null,
  spec: '',
  unit: '',
  manager: '',
  whCode: ''
});

/* ------------------ 초기화 ------------------ */
function clearDetail() {
  detail.value = {
    prodId: '',
    prodName: '',
    prodNo: '',
    proQty: null,
    proDate: '',
    endDate: null,
    spec: '',
    unit: '',
    manager: currentUser.value,
    whCode: ''
  };
}

/* ------------------ 바인딩 ------------------ */
function bindDetail(row) {
  selectedRow.value = row;
  detail.value = {
    prodId: row?.prodId ?? '',
    prodName: row?.prodName ?? '',
    prodNo: row?.prodNo ?? '',
    proQty: row?.proQty ?? null,
    endDate: fmtDate(row?.endDate),
    spec: row?.spec ?? '',
    manager: currentUser.value,
    whCode: row?.whCode ?? '',
    proDate: fmtDate(row?.proDate)
  };
}

/* ✅ 체크박스 단일 선택 */
function toggleInboundRow(row) {
  if (selectedRow.value && selectedRow.value.prodNo === row.prodNo) {
    selectedRow.value = null;
    clearDetail();
    return;
  }
  selectedRow.value = row;
  bindDetail(row);
}

/* ------------------ 목록 조회 ------------------ */
async function doSearch() {
  try {
    const params = {
      prodCode: searchForm.value.prodId.trim(),
      prodName: searchForm.value.prodName.trim(),
      page: page.value,
      size: rows.value
    };
    const { data } = await axios.get('/api/lots', { params });
    inboundList.value = data.data ?? data ?? [];
    totalRecords.value = data.page?.totalElements ?? inboundList.value.length;
    clearDetail();
  } catch (err) {
    toast.error('목록 조회 오류');
    console.error(err);
  }
}

/* ✅ 페이지 변경 */
function onPageChange(e) {
  page.value = e.page + 1;
  rows.value = e.rows;
  doSearch();
}

/* ------------------ 초기화 ------------------ */
function resetSearch() {
  searchForm.value = { prodId: '', prodName: '' };
  page.value = 1;
  doSearch();
}

/* ------------------ 제품 모달 ------------------ */
const isShowProdModal = ref(false);
const prodModalColumns = ref([]); // ✅ 동적 컬럼
let lastClickedField = '';

function openProdModal(type) {
  lastClickedField = type;
  if (type === 'code') {
    prodModalColumns.value = [{ label: '제품코드', field: 'prodId' }];
  } else {
    prodModalColumns.value = [{ label: '제품명', field: 'prodName' }];
  }
  isShowProdModal.value = true;
}

function closeProdModal() {
  isShowProdModal.value = false;
}

const fetchProductData = async ({ page = 1, size = 10, search = '' } = {}) => {
  const params = { page, size, keyword: search };
  const { data } = await axios.get('/api/inbound/product', { params });
  return { items: data.data, total: data.page.totalElements };
};

function handleSelectProduct(item) {
  const code = item?.prodId || '';
  const name = item?.prodName || '';
  if (lastClickedField === 'code') {
    searchForm.value.prodId = code;
  } else if (lastClickedField === 'name') {
    searchForm.value.prodName = name;
  }
  closeProdModal();
  toast.info(`제품 선택: ${lastClickedField === 'code' ? code : name}`);
  doSearch();
}

/* ------------------ 창고 모달 ------------------ */
const isShowWhModal = ref(false);
function openWhModal() {
  isShowWhModal.value = true;
}
function closeWhModal() {
  isShowWhModal.value = false;
}

const fetchWarehouseData = async ({ page = 1, size = 10, search = '' } = {}) => {
  const params = { page, size, keyword: search };
  const { data } = await axios.get('/api/inbound/warehouse1', { params });
  return { items: data.data, total: data.page.totalElements };
};

function handleSelectWarehouse(item) {
  detail.value.whCode = item?.whId || '';
  closeWhModal();
  toast.info(`창고 선택: ${detail.value.whCode}`);
}

/* ------------------ 저장 ------------------ */
async function save() {
  try {
    const body = {
      prodNo: detail.value.prodNo,
      employeeName: currentUser.value,
      totalQty: Number(detail.value.proQty || 0),
      whId: detail.value.whCode
    };
    const { status } = await axios.post('/api/inbound', body);
    if (status === 200) {
      await doSearch();
      toast('success', '등록 성공', '성공적으로 입고처리되었습니다.');
    } else {
      toast('error', '실패', '실패 하였습니다');
    }
  } catch (err) {
    toast('error', '실패', '실패 하였습니다');
    console.error(err);
  }
}

onMounted(async () => {
  await loadCurrentUser();
  doSearch();
});
</script>

<template>
  <div class="page-wrap">
    <!-- 검색 -->
    <!-- ✅ 브레드크럼 추가 -->
    <Breadcrumb class="rounded-lg mb-3" :home="breadcrumbHome" :model="breadcrumbItems" />
    <div class="box">
      <div class="form-grid">
        <div class="field">
          <label>제품코드</label>
          <InputGroup>
            <InputText v-model="searchForm.prodId" placeholder="PRD001" readonly @click="() => openProdModal('code')" />
            <InputGroupAddon>
              <Button icon="pi pi-search" text @click="() => openProdModal('code')" />
            </InputGroupAddon>
          </InputGroup>
        </div>
        <div class="field">
          <label>제품명</label>
          <InputGroup>
            <InputText v-model="searchForm.prodName" placeholder="원두" readonly @click="() => openProdModal('name')" />
            <InputGroupAddon>
              <Button icon="pi pi-search" text @click="() => openProdModal('name')" />
            </InputGroupAddon>
          </InputGroup>
        </div>
      </div>
      <div class="actions">
        <Button label="초기화" icon="pi pi-refresh" outlined @click="resetSearch" />
        <Button label="조회" icon="pi pi-search" @click="doSearch" />
      </div>
    </div>

    <!-- 목록 + 상세 -->
    <div class="split">
      <!-- ✅ LOT 목록 -->
      <div class="list-box">
        <div class="sub-title">LOT 목록</div>
        <DataTable :value="inboundList" dataKey="prodNo" paginator :rows="rows" :totalRecords="totalRecords" :first="(page - 1) * rows" @page="onPageChange" @row-click="(e) => toggleInboundRow(e.data)">
          <Column headerStyle="width:3rem; text-align:center;">
            <template #body="{ data }">
              <div class="p-checkbox p-component custom-checkbox">
                <input type="checkbox" class="p-checkbox-box" :checked="selectedRow?.prodNo === data.prodNo" @change="() => toggleInboundRow(data)" />
              </div>
            </template>
          </Column>
          <Column field="prodNo" header="LOT번호" />
          <Column field="prodId" header="제품코드" />
          <Column field="prodName" header="제품명" />
          <Column field="proQty" header="수량" />
          <Column field="matStatus" header="상태" />
        </DataTable>
      </div>

      <!-- ✅ 상세 -->
      <div class="detail-box">
        <div class="detail-head">
          <div class="detail-title">입고 상세</div>
          <div class="head-actions">
            <Button label="등록" icon="pi pi-save" severity="primary" @click="save" />
          </div>
        </div>

        <div class="detail-grid">
          <div class="field"><label>제품코드</label><InputText v-model="detail.prodId" readonly /></div>
          <div class="field"><label>제품명</label><InputText v-model="detail.prodName" readonly /></div>
          <div class="field"><label>LOT번호</label><InputText v-model="detail.prodNo" readonly /></div>
          <div class="field"><label>입고일자</label><InputText :value="fmtDate(detail.proDate)" readonly /></div>
          <div class="field"><label>입고수량</label><InputText v-model="detail.proQty" readonly /></div>
          <div class="field"><label>유통기한</label><InputText :value="fmtDate(detail.endDate)" readonly /></div>
          <div class="field"><label>규격</label><InputText v-model="detail.spec" readonly /></div>
          <div class="field"><label>담당자</label><InputText v-model="detail.manager" readonly /></div>
          <div class="field">
            <label>창고코드</label>
            <InputGroup>
              <InputText v-model="detail.whCode" placeholder="WH001" readonly @click="openWhModal" />
              <InputGroupAddon>
                <Button icon="pi pi-search" text @click="openWhModal" />
              </InputGroupAddon>
            </InputGroup>
          </div>
        </div>
      </div>
    </div>

    <!-- ✅ 제품 모달 -->
    <Modal :visible="isShowProdModal" title="제품 검색" :columns="prodModalColumns" dataKey="prodId" :fetchData="fetchProductData" :pageSize="5" :frontPagination="false" @select="handleSelectProduct" @close="closeProdModal" />

    <!-- ✅ 창고 모달 -->
    <Modal
      :visible="isShowWhModal"
      title="창고 검색"
      dataKey="whId"
      :columns="[
        { label: '창고코드', field: 'whId' },
        { label: '창고명', field: 'whName' }
      ]"
      :fetchData="fetchWarehouseData"
      :pageSize="5"
      :frontPagination="false"
      @select="handleSelectWarehouse"
      @close="closeWhModal"
    />
  </div>
</template>

<style scoped>
.page-wrap {
  padding: 16px;
  background: #f5f7fb;
}
.box {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 12px;
}
.form-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 8px;
}
.split {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 14px;
}
.list-box,
.detail-box {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 12px;
}
.sub-title {
  font-weight: 700;
  margin-bottom: 8px;
}
.detail-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.detail-title {
  font-weight: 700;
}
.head-actions {
  display: flex;
  gap: 8px;
}
.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

/* ✅ 손가락 커서 표시 */
:deep(.p-datatable-tbody > tr:hover) {
  background: #f9fafb;
  cursor: pointer;
  transition: background 120ms ease-in-out;
}

/* ✅ 초록 체크박스 스타일 */
:deep(.custom-checkbox) {
  display: flex;
  justify-content: center;
  align-items: center;
}
:deep(.custom-checkbox .p-checkbox-box) {
  width: 18px;
  height: 18px;
  border: 1px solid #ced4da;
  border-radius: 3px;
  background: #fff;
  cursor: pointer;
  transition:
    background 0.15s,
    border-color 0.15s;
  appearance: none;
  outline: none;
  position: relative;
}
:deep(.custom-checkbox .p-checkbox-box:checked) {
  background: #16a34a;
  border-color: #16a34a;
}
:deep(.custom-checkbox .p-checkbox-box:checked::after) {
  content: '';
  position: absolute;
  width: 4px;
  height: 8px;
  border: solid #fff;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
  top: 2px;
  left: 6px;
}
</style>
