<script setup>
/* ===========================
   Imports
=========================== */
import { ref, computed, onMounted, watch } from 'vue';
import axios from 'axios';
import { useRoute } from 'vue-router';
import { useAppToast } from '@/composables/useAppToast';
import { useIcon } from '@/composables/useIcon';

import AutoComplete from 'primevue/autocomplete';
import Breadcrumb from 'primevue/breadcrumb';
import Button from 'primevue/button';
import Calendar from 'primevue/calendar';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import Dialog from 'primevue/dialog';
import Divider from 'primevue/divider';
import InputGroup from 'primevue/inputgroup';
import InputGroupAddon from 'primevue/inputgroupaddon';
import InputNumber from 'primevue/inputnumber';
import InputText from 'primevue/inputtext';
import TabView from 'primevue/tabview';
import TabPanel from 'primevue/tabpanel';
import btn from '@/components/common/Btn.vue';

/* ===========================
   Setup & Common
=========================== */
const route = useRoute();
const { toast } = useAppToast();

/* Breadcrumb */
const breadcrumbHome = { icon: useIcon('home'), to: '/' };
const breadcrumbItems = computed(() => {
  const matched = route.matched.filter((r) => r.meta);
  if (!matched.length) return [];
  const current = matched[matched.length - 1];
  const parentLabel = current.meta?.breadcrumb?.parent || '기준 정보';
  const currentLabel = current.name || '';
  return [{ label: parentLabel }, { label: currentLabel, to: route.fullPath }];
});

const searchProdSuggestions = async (event) => {
  const keyword = (typeof event === 'string' ? event : event.query)?.trim();
  if (!keyword) {
    suggestions.value = [];
    return;
  }
  try {
    const { data } = await axios.get(`http://localhost:8080/api/product/autocomplete?keyword=${keyword}`);
    suggestions.value = Array.isArray(data) ? data : [];
  } catch {
    suggestions.value = [];
  }
};

/* 날짜 포맷 */
const fmt = (d) => (d ? new Date(d).toISOString().slice(0, 10) : '');

/* ===========================
   좌측: 제품 목록
=========================== */
const searchFilters = ref({ prodName: '' });
const prodList = ref([]);
const selectedProd = ref(null);
const loading = ref(false);
const suggestions = ref([]);

const fetchProdList = async () => {
  loading.value = true;
  try {
    const res = await axios.get('/api/product', { params: searchFilters.value });
    prodList.value = Array.isArray(res.data) ? res.data : [];
  } catch {
    toast('error', '조회 실패', '제품 조회 중 오류 발생');
  } finally {
    loading.value = false;
  }
};

const selectProduct = async (prod) => {
  selectedProd.value = prod;
  form.value = {
    prodId: prod.prodId,
    prodName: prod.prodName,
    unit: prod.unit,
    spec: prod.spec,
    createdAt: fmt(prod.createdAt),
    lastUpdateDate: ''
  };
  await fetchBomList(prod.prodId);
};

/* ===========================
   우측 탭
=========================== */
const activeTabIndex = ref(0);

/* ===========================
   BOM 목록
=========================== */
const bomList = ref([]);
const fetchBomList = async (prodId) => {
  if (!prodId) return;
  try {
    const { data } = await axios.get(`/api/bom/${prodId}`);
    bomList.value = Array.isArray(data) ? data : [];
  } catch {
    toast('error', 'BOM 조회 실패');
  }
};

/* 삭제 */
const deleteBom = async (bomId) => {
  if (!confirm('정말 삭제하시겠습니까?')) return;
  try {
    await axios.delete(`/api/bom/${bomId}`);
    toast('success', '삭제 완료');
    bomList.value = bomList.value.filter((b) => b.bomId !== bomId);
  } catch {
    toast('error', '삭제 실패');
  }
};

const resetSearch = async () => {
  // 검색 조건 초기화
  searchFilters.value.prodName = '';

  // 선택된 제품 및 목록 초기화
  selectedProd.value = null;
  prodList.value = [];

  // 제품 상세 폼 초기화
  Object.assign(form.value, {
    prodId: '',
    prodName: '',
    unit: '',
    spec: '',
    createdAt: '',
    lastUpdateDate: ''
  });

  // BOM 목록 초기화
  bomList.value = [];

  // 다시 전체 제품 목록 조회
  await fetchProdList();
};

/* ===========================
   BOM 수정 모달
=========================== */
const editDialogVisible = ref(false);
const editHeader = ref({ bomId: '', effectiveDate: new Date(), expireDate: new Date('9999-12-31') });
const editDetails = ref([]);

const openEditDialog = (bomId) => {
  const targetBom = bomList.value.find((b) => b.bomId === bomId);
  if (!targetBom) return toast('warn', '데이터 없음');

  editHeader.value = {
    bomId: targetBom.bomId,
    effectiveDate: new Date(targetBom.effectiveDate),
    expireDate: new Date(targetBom.expireDate)
  };

  editDetails.value = targetBom.details.map((d) => ({
    id: Date.now() + Math.random(),
    bomDeId: d.bomDeId,
    bomId: d.bomId,
    matId: d.material?.matId || d.matId || '',
    matName: d.material?.matName || d.matName || '',
    baseUnit: d.material?.unit || d.baseUnit || '',
    mixingRate: d.mixingRate ?? 0,
    qty: d.qty ?? 0
  }));

  editDialogVisible.value = true;
};

const saveEditBom = async () => {
  const req = {
    bom: {
      bomId: editHeader.value.bomId,
      prodId: selectedProd.value.prodId,
      effectiveDate: fmt(editHeader.value.effectiveDate),
      expireDate: fmt(editHeader.value.expireDate)
    },
    details: editDetails.value.map((d) => ({
      bomDeId: d.bomDeId,
      matId: d.matId,
      mixingRate: d.mixingRate,
      qty: d.qty,
      baseUnit: d.baseUnit
    }))
  };
  try {
    await axios.put(`/api/bom/with-detail/${req.bom.bomId}`, req);
    toast('success', '수정 완료');
    editDialogVisible.value = false;
    await fetchBomList(selectedProd.value?.prodId);
  } catch {
    toast('error', '수정 실패');
  }
};

/* ===========================
   신규 BOM 등록
=========================== */
const bomMaster = ref({ effectiveDate: new Date(), expireDate: new Date('9999-12-31') });
const bomDetails = ref([{ id: 1, matId: '', matName: '', baseUnit: '', mixingRate: 0, qty: 0 }]);
const addRow = () => bomDetails.value.push({ id: Date.now(), matId: '', matName: '', baseUnit: '', mixingRate: 0, qty: 0 });
const deleteRow = () => {
  if (bomDetails.value.length > 1) bomDetails.value.pop();
};

const saveNewBom = async () => {
  const prodId = selectedProd.value?.prodId;
  if (!prodId) return toast('warn', '제품을 먼저 선택하세요');
  const req = {
    bom: {
      prodId,
      effectiveDate: fmt(bomMaster.value.effectiveDate),
      expireDate: fmt(bomMaster.value.expireDate)
    },
    details: bomDetails.value.map((d) => ({
      matId: d.matId,
      mixingRate: d.mixingRate,
      qty: d.qty,
      baseUnit: d.baseUnit || 'EA'
    }))
  };
  try {
    await axios.post('/api/bom/with-detail', req);
    toast('success', '등록 완료');
    await fetchBomList(prodId);
    bomDetails.value = [{ id: 1, matId: '', matName: '', baseUnit: '', mixingRate: 0, qty: 0 }];
    activeTabIndex.value = 1;
  } catch {
    toast('error', '등록 실패');
  }
};

/* ===========================
   자재 모달
=========================== */
const materialList = ref([]);
const materialDialogVisible = ref(false);
const selectedRowDetail = ref(null);

const openMaterialDialog = async (row) => {
  selectedRowDetail.value = row;
  const res = await axios.get('/api/material');
  materialList.value = res.data;
  materialDialogVisible.value = true;
};

const selectMaterial = (mat) => {
  if (!selectedRowDetail.value) return;

  const row = selectedRowDetail.value;

  row.matId = mat.matId;
  row.matName = mat.matName;
  row.baseUnit = mat.unit;

  const idx = editDetails.value.findIndex((d) => d.id === row.id || d.bomDeId === row.bomDeId);
  if (idx !== -1) {
    editDetails.value[idx] = {
      ...editDetails.value[idx],
      matId: mat.matId,
      matName: mat.matName,
      baseUnit: mat.unit
    };
  }

  // 콘솔로 확인
  console.log('자재 선택 후 editDetails:', JSON.stringify(editDetails.value, null, 2));

  materialDialogVisible.value = false;
};

/* ===========================
   Mounted
=========================== */
onMounted(fetchProdList);

const form = ref({ prodId: '', prodName: '', unit: '', spec: '', createdAt: '', lastUpdateDate: '' });
</script>

<template>
  <div class="container">
    <Breadcrumb class="rounded-lg mb-4" :home="breadcrumbHome" :model="breadcrumbItems" />

    <div class="main-wrapper">
      <!-- 좌측 -->
      <div class="panel left-panel">
        <div class="card flex flex-col gap-3">
          <div class="font-semibold text-xl">제품 목록</div>
          <Divider class="mt-0" />
          <div class="flex gap-2">
            <AutoComplete v-model="searchFilters.prodName" :suggestions="suggestions" :loading="autoLoading" @complete="searchProdSuggestions" placeholder="제품명 입력" class="w-full" />
            <Button icon="pi pi-search" @click="fetchProdList" />
            <Button icon="pi pi-refresh" class="p-button-outlined" @click="resetSearch" />
          </div>
          <DataTable :value="prodList" selectionMode="single" dataKey="prodId" v-model:selection="selectedProd" @row-select="selectProduct($event.data)" paginator :rows="6" stripedRows>
            <Column field="prodId" header="제품코드" />
            <Column field="prodName" header="제품명" />
          </DataTable>
        </div>
      </div>

      <!-- 우측 -->
      <div class="panel right-panel">
        <div class="card flex flex-col gap-3 h-full">
          <TabView v-model:activeIndex="activeTabIndex">
            <!-- 탭1: 상세정보 -->
            <TabPanel header="제품 상세정보">
              <div class="grid grid-cols-2 gap-4">
                <div><label>제품코드</label><InputText v-model="form.prodId" readonly class="w-full" /></div>
                <div><label>제품명</label><InputText v-model="form.prodName" readonly class="w-full" /></div>
                <div><label>단위</label><InputText v-model="form.unit" readonly class="w-full" /></div>
                <div><label>등록일</label><InputText v-model="form.createdAt" readonly class="w-full" /></div>
                <div><label>마지막수정날짜</label><InputText v-model="form.lastUpdateDate" readonly class="w-full" /></div>
              </div>
            </TabPanel>

            <!-- 탭2: BOM 관리 -->
            <TabPanel header="BOM 관리">
              <div v-if="selectedProd">
                <DataTable :value="bomList.flatMap((b) => b.details.map((d) => ({ ...d, bomId: b.bomId })))" stripedRows paginator :rows="8">
                  <Column field="bomId" header="BOM코드" />
                  <Column field="matId" header="자재코드">
                    <template #body="{ data }">{{ data.material?.matId }}</template>
                  </Column>
                  <Column header="자재명">
                    <template #body="{ data }">{{ data.material?.matName }}</template>
                  </Column>
                  <Column field="mixingRate" header="비율(%)" />
                  <Column field="qty" header="수량" />
                  <Column field="baseUnit" header="단위" />
                  <Column header="작업">
                    <template #body="{ data, index }">
                      <div v-if="index === 0 || bomList.flatMap((b) => b.details).findIndex((d) => d.bomId === data.bomId) === index">
                        <Button icon="pi pi-pencil" class="p-button-text" @click="openEditDialog(data.bomId)" />
                        <Button icon="pi pi-trash" class="p-button-text p-button-danger" @click="deleteBom(data.bomId)" />
                      </div>
                    </template>
                  </Column>
                </DataTable>
              </div>
              <div v-else>제품을 먼저 선택하세요.</div>
            </TabPanel>

            <!-- 탭3: 신규 등록 -->
            <TabPanel header="신규 BOM 등록">
              <div v-if="selectedProd">
                <div class="grid gap-4 md:grid-cols-2">
                  <InputGroup>
                    <InputGroupAddon><i :class="useIcon('calendar')" /></InputGroupAddon>
                    <Calendar v-model="bomMaster.effectiveDate" dateFormat="yy-mm-dd" />
                  </InputGroup>
                  <InputGroup>
                    <InputGroupAddon><i :class="useIcon('calendar')" /></InputGroupAddon>
                    <Calendar v-model="bomMaster.expireDate" dateFormat="yy-mm-dd" />
                  </InputGroup>
                </div>
                <div class="flex justify-between items-center mt-4">
                  <div class="font-semibold text-l">자재 구성</div>
                  <div class="flex gap-2">
                    <btn color="secondary" icon="add" @click="addRow" />
                    <btn color="secondary" icon="minus" @click="deleteRow" />
                    <btn color="info" icon="check" label="등록" outlined @click="saveNewBom" />
                  </div>
                </div>

                <DataTable :value="bomDetails" stripedRows>
                  <Column header="자재선택">
                    <template #body="{ data }">
                      <Button icon="pi pi-search" class="p-button-text p-button-sm" @click="openMaterialDialog(data)" />
                    </template>
                  </Column>
                  <Column field="matId" header="자재코드">
                    <template #body="{ data }"><InputText v-model="data.matId" readonly class="w-full" /></template>
                  </Column>
                  <Column field="matName" header="자재명">
                    <template #body="{ data }"><InputText v-model="data.matName" readonly class="w-full" /></template>
                  </Column>
                  <Column field="baseUnit" header="단위">
                    <template #body="{ data }"><InputText v-model="data.baseUnit" readonly class="w-full" /></template>
                  </Column>
                  <Column field="mixingRate" header="비율(%)">
                    <template #body="{ data }"><InputNumber v-model="data.mixingRate" :useGrouping="false" class="w-full" /></template>
                  </Column>
                  <Column field="qty" header="수량">
                    <template #body="{ data }"><InputNumber v-model="data.qty" :useGrouping="false" class="w-full" /></template>
                  </Column>
                </DataTable>
              </div>
              <div v-else>제품을 먼저 선택하세요.</div>
            </TabPanel>
          </TabView>
        </div>
      </div>
    </div>

    <!-- 자재 선택 모달 -->
    <Dialog v-model:visible="materialDialogVisible" header="자재 선택" modal>
      <DataTable :value="materialList" selectionMode="single" dataKey="matId" @row-click="selectMaterial($event.data)" paginator :rows="8">
        <Column field="matId" header="자재코드" />
        <Column field="matName" header="자재명" />
        <Column field="unit" header="단위" />
      </DataTable>
    </Dialog>

    <!-- 수정 모달 -->
    <!-- 수정 모달 -->
    <Dialog v-model:visible="editDialogVisible" header="BOM 수정" modal style="width: 80vw">
      <div class="grid gap-4 md:grid-cols-2 mb-4">
        <div>
          <label>시작일</label>
          <Calendar v-model="editHeader.effectiveDate" dateFormat="yy-mm-dd" />
        </div>
        <div>
          <label>종료일</label>
          <Calendar v-model="editHeader.expireDate" dateFormat="yy-mm-dd" />
        </div>
      </div>

      <!-- BOM 자재 수정 테이블 -->
      <DataTable :value="editDetails" stripedRows>
        <!-- 자재 선택 -->
        <Column header="자재선택">
          <template #body="{ data }">
            <Button icon="pi pi-search" class="p-button-text p-button-sm" @click="openMaterialDialog(data)" />
          </template>
        </Column>

        <!-- 자재 코드 -->
        <Column field="matId" header="자재코드">
          <template #body="{ data }">
            <InputText v-model="data.matId" readonly class="w-full" />
          </template>
        </Column>

        <!-- 자재명 -->
        <Column field="matName" header="자재명">
          <template #body="{ data }">
            <InputText v-model="data.matName" readonly class="w-full" />
          </template>
        </Column>

        <!-- 단위 -->
        <Column field="baseUnit" header="단위">
          <template #body="{ data }">
            <InputText v-model="data.baseUnit" readonly class="w-full" />
          </template>
        </Column>

        <!-- 비율 -->
        <Column field="mixingRate" header="비율(%)">
          <template #body="{ data }">
            <InputNumber v-model="data.mixingRate" :useGrouping="false" class="w-full" />
          </template>
        </Column>

        <!-- 수량 -->
        <Column field="qty" header="수량">
          <template #body="{ data }">
            <InputNumber v-model="data.qty" :useGrouping="false" class="w-full" />
          </template>
        </Column>
      </DataTable>

      <div class="mt-3 text-right">
        <btn color="info" icon="check" label="저장" outlined @click="saveEditBom" />
      </div>
    </Dialog>
  </div>
</template>

<style scoped>
.container {
  padding: 1rem;
}
.main-wrapper {
  display: flex;
  gap: 1rem;
  height: calc(100vh - 120px);
}
.panel {
  display: flex;
  flex-direction: column;
}
.left-panel {
  flex: 1;
  min-width: 320px;
}
.right-panel {
  flex: 2;
}
.card {
  background: #fff;
  padding: 1.25rem;
  border-radius: 0.75rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  height: 100%;
}
</style>
