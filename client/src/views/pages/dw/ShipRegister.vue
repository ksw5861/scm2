<script setup>
import { ref, onMounted } from 'vue';
import InputText from 'primevue/inputtext';
import InputGroup from 'primevue/inputgroup';
import InputGroupAddon from 'primevue/inputgroupaddon';
import Modal from '@/components/common/Modal.vue';
// (Button / DataTable / Column 전역 등록 가정)

/* ========= 검색 상태 ========= */
const search = ref({
  instructionNo: '',
  vendorName: '',
  status: ''
});

/* ========= 모달 ========= */
const modalTarget = ref('');
const isShowModal = ref(false);
const modalColumns = [
  { key: 'code', label: '코드' },
  { key: 'name', label: '이름' }
];

/* fetchData 함수 (팀원 스타일) */
const fetchModalData = async () => {
  if (modalTarget.value === 'instructionNo') {
    return [
      { code: 'SH2025-0001', name: '스타벅스 송도달점' },
      { code: 'SH2025-0002', name: '스타벅스 종로점' }
    ];
  }
  if (modalTarget.value === 'vendorName') {
    return [
      { code: '스타벅스', name: '스타벅스' },
      { code: '이디야', name: '이디야' }
    ];
  }
  if (modalTarget.value === 'status') {
    return [
      { code: '대기', name: '대기' },
      { code: '완료', name: '완료' }
    ];
  }
  return [];
};

function openModal(target) {
  modalTarget.value = target;
  isShowModal.value = true;
}

function handleSelect(item) {
  if (modalTarget.value === 'instructionNo') search.value.instructionNo = item.code;
  if (modalTarget.value === 'vendorName') search.value.vendorName = item.code;
  if (modalTarget.value === 'status') search.value.status = item.code;
  isShowModal.value = false;
}

/* ========= 더미 데이터 ========= */
const allInstructions = ref([
  { id: 1, instructionNo: 'SH2025-0001', vendor: '스타벅스 송도달점', scheduledDate: '2025-09-20', status: '대기' },
  { id: 2, instructionNo: 'SH2025-0002', vendor: '스타벅스 종로점', scheduledDate: '2025-09-20', status: '대기' },
  { id: 3, instructionNo: 'SH2025-0003', vendor: '스타벅스 강남점', scheduledDate: '2025-09-21', status: '완료' }
]);
const leftList = ref([]);
const leftSelected = ref([]);
const rightList = ref([]);
const rightSelected = ref([]);

onMounted(() => {
  leftList.value = [...allInstructions.value];
});

/* ========= 검색 / 초기화 ========= */
function applySearch() {
  const no = (search.value.instructionNo || '').trim().toLowerCase();
  const v = (search.value.vendorName || '').trim().toLowerCase();
  const s = search.value.status;

  leftList.value = allInstructions.value.filter((r) => {
    const noOk = no ? r.instructionNo.toLowerCase().includes(no) : true;
    const vOk = v ? r.vendor.toLowerCase().includes(v) : true;
    const sOk = s ? r.status === s : true;
    return noOk && vOk && sOk;
  });
  leftSelected.value = [];
}
function resetSearch() {
  search.value = { instructionNo: '', vendorName: '', status: '' };
  leftList.value = [...allInstructions.value];
  leftSelected.value = [];
}

/* ========= 좌→우 이동 ========= */
function addSelected() {
  if (!leftSelected.value.length) return;
  const exists = new Set(rightList.value.map((x) => x.id));
  leftSelected.value.forEach((r) => {
    if (!exists.has(r.id)) {
      rightList.value.push({
        id: r.id,
        instructionNo: r.instructionNo,
        prodCode: 'P001',
        prodName: '아라비카 원두',
        qty: 100,
        unit: 'BOX(20)',
        amount: 100000,
        scheduledDate: r.scheduledDate,
        manager: '김관리',
        status: r.status
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

/* ========= 출하등록 처리 ========= */
function registerShipment() {
  if (!rightList.value.length) return alert('출하등록 대상이 없습니다.');
  alert('출하등록 완료 (내일 API 연동)');
}
</script>

<template>
  <div class="sr-wrap">
    <div class="section-title">출하등록</div>

    <!-- 검색폼 -->
    <div class="card">
      <div class="box-title">출하등록 검색</div>
      <div class="grid grid--search">
        <div class="field">
          <label>출하지시번호</label>
          <InputGroup>
            <InputText v-model="search.instructionNo" placeholder="출하지시번호" />
            <InputGroupAddon><Button icon="pi pi-search" @click="openModal('instructionNo')" /></InputGroupAddon>
          </InputGroup>
        </div>
        <div class="field">
          <label>판매처명</label>
          <InputGroup>
            <InputText v-model="search.vendorName" placeholder="판매처명" />
            <InputGroupAddon><Button icon="pi pi-search" @click="openModal('vendorName')" /></InputGroupAddon>
          </InputGroup>
        </div>
        <div class="field">
          <label>출하상태</label>
          <InputGroup>
            <InputText v-model="search.status" placeholder="대기/완료" />
            <InputGroupAddon><Button icon="pi pi-search" @click="openModal('status')" /></InputGroupAddon>
          </InputGroup>
        </div>
        <div class="search-actions">
          <Button class="box-btn box-btn--ghost" icon="pi pi-refresh" label="초기화" @click="resetSearch" />
          <Button class="box-btn box-btn--green" icon="pi pi-search" label="조회" @click="applySearch" />
        </div>
      </div>
    </div>

    <!-- 좌측 테이블 -->
    <div class="split">
      <div class="pane">
        <div class="pane-head">
          <div class="pane-title">출하지시 목록</div>
          <Button label="선택" class="pill primary" :disabled="!leftSelected.length" @click="addSelected" />
        </div>
        <DataTable :value="leftList" dataKey="id" selectionMode="multiple" v-model:selection="leftSelected" paginator :rows="7">
          <Column selectionMode="multiple" headerStyle="width:3rem" />
          <Column field="instructionNo" header="출하지시번호" />
          <Column field="vendor" header="판매처명" />
          <Column field="scheduledDate" header="출하예정일" />
          <Column field="status" header="상태" />
        </DataTable>
      </div>

      <!-- 우측 테이블 -->
      <div class="pane">
        <div class="pane-head">
          <div class="pane-title">출하등록</div>
          <div class="pane-actions">
            <Button label="삭제" class="pill danger" :disabled="!rightSelected.length" @click="removeSelected" />
            <Button label="출하등록" class="pill primary" :disabled="!rightList.length" @click="registerShipment" />
          </div>
        </div>
        <DataTable :value="rightList" dataKey="id" selectionMode="multiple" v-model:selection="rightSelected" paginator :rows="7">
          <Column selectionMode="multiple" headerStyle="width:3rem" />
          <Column field="instructionNo" header="출하지시번호" />
          <Column field="prodCode" header="제품코드" />
          <Column field="prodName" header="제품명" />
          <Column field="qty" header="주문수량" />
          <Column field="unit" header="단위" />
          <Column field="amount" header="금액" />
          <Column field="scheduledDate" header="출하예정일" />
          <Column field="manager" header="담당자" />
          <Column field="status" header="상태" />
        </DataTable>
      </div>
    </div>

    <!-- 모달 -->
    <Modal :visible="isShowModal" title="검색" idField="code" :columns="modalColumns" :fetchData="fetchModalData" :page-size="5" @select="handleSelect" @close="isShowModal = false" />
  </div>
</template>

<style scoped>
.sr-wrap {
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
</style>
