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

/* 날짜 유틸 */
const formatDateOnly = (date) => {
  if (!date) return '';
  const d = new Date(date);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${dd}`;
};
const formatDateTime = (date) => {
  if (!date) return '';
  const d = new Date(date);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  const hh = String(d.getHours()).padStart(2, '0');
  const mm = String(d.getMinutes()).padStart(2, '0');
  const ss = String(d.getSeconds()).padStart(2, '0');
  return `${y}-${m}-${dd} ${hh}:${mm}:${ss}`;
};

/* ===========================
   좌측: 제품 목록
=========================== */
const suggestions = ref([]);
const searchFilters = ref({ prodName: '', prodId: '' });
const prodList = ref([]);
const selectedProd = ref(null);
const loading = ref(false);

const searchProdSuggestions = async (event) => {
  const keyword = (typeof event === 'string' ? event : event.query)?.trim();
  if (!keyword) {
    suggestions.value = [];
    return;
  }
  try {
    const { data } = await axios.get(`/api/product/autocomplete?keyword=${keyword}`);
    suggestions.value = Array.isArray(data) ? data : [];
  } catch {
    suggestions.value = [];
  }
};

const fetchProdList = async () => {
  loading.value = true;
  try {
    const res = await axios.get('/api/product', { params: searchFilters.value });
    prodList.value = Array.isArray(res.data) ? res.data : [];
  } catch {
    toast('error', '조회 실패', '제품 정보를 가져오지 못했습니다.');
    prodList.value = [];
  } finally {
    loading.value = false;
  }
};

const form = ref({ prodId: '', prodName: '', unit: '', spec: '', createdAt: '', lastUpdateDate: '' });

const selectProduct = async (prod) => {
  selectedProd.value = prod || null;
  form.value = selectedProd.value
    ? {
        prodId: prod.prodId,
        prodName: prod.prodName,
        unit: prod.unit,
        spec: prod.spec,
        createdAt: prod.createdAt,
        lastUpdateDate: ''
      }
    : { prodId: '', prodName: '', unit: '', spec: '', createdAt: '', lastUpdateDate: '' };

  if (selectedProd.value) await fetchBomList(selectedProd.value.prodId);
  else {
    bomList.value = [];
    form.value.lastUpdateDate = '';
  }
};

const resetSearch = () => {
  searchFilters.value = { prodName: '', prodId: '' };
  fetchProdList();
  selectedProd.value = null;
  form.value = { prodId: '', prodName: '', unit: '', spec: '', createdAt: '', lastUpdateDate: '' };
  bomList.value = [];
};

/* ===========================
   우측 탭 제어
=========================== */
const activeTabIndex = ref(0);

/* ===========================
   BOM 리스트
=========================== */
const bomList = ref([]);
const fetchBomList = async (prodId) => {
  if (!prodId) return;
  loading.value = true;
  try {
    const { data } = await axios.get(`/api/bom/${prodId}`);
    const list = Array.isArray(data) ? data : [];
    let latestDate = null;
    list.forEach((b) => {
      b.effectiveDate = b.effectiveDate ? new Date(b.effectiveDate) : null;
      b.expireDate = b.expireDate ? new Date(b.expireDate) : null;
      if (!b.material) b.material = { matName: '', unit: '', matId: b.matId || '' };
      if (!b.matId && b.material?.matId) b.matId = b.material.matId;
      if (b.lastUpdateDate) {
        const d = new Date(b.lastUpdateDate);
        if (!latestDate || d > latestDate) latestDate = d;
      }
    });
    form.value.lastUpdateDate = latestDate ? formatDateTime(latestDate) : '';
    bomList.value = list;
  } catch {
    toast('error', '조회 실패', 'BOM 목록을 불러올 수 없습니다.');
    bomList.value = [];
  } finally {
    loading.value = false;
  }
};

/* ===========================
   BOM 삭제
=========================== */
const deleteBom = async (bomId) => {
  if (!confirm('정말 삭제하시겠습니까?')) return;
  try {
    const res = await axios.delete(`/api/bom/${bomId}`);
    if (res.data > 0) {
      toast('success', '삭제 완료', 'BOM이 삭제되었습니다.');
      await fetchBomList(selectedProd.value?.prodId);
    } else {
      toast('warn', '삭제 실패', '삭제된 행이 없습니다.');
    }
  } catch {
    toast('error', '삭제 실패', 'BOM 삭제 중 오류 발생');
  }
};

/* ===========================
   자재선택 모달
=========================== */
const materialList = ref([]);
const materialDialogVisible = ref(false);
const selectedRowDetail = ref(null);

const fetchMaterialList = async () => {
  try {
    const res = await axios.get('/api/material');
    materialList.value = Array.isArray(res.data) ? res.data : [];
  } catch {
    toast('error', '조회 실패', '자재 정보를 가져오지 못했습니다.');
    materialList.value = [];
  }
};
const openMaterialDialog = async (row) => {
  selectedRowDetail.value = row;
  await fetchMaterialList();
  materialDialogVisible.value = true;
};
const selectMaterial = (material) => {
  if (selectedRowDetail.value) {
    selectedRowDetail.value.matId = material.matId;
    selectedRowDetail.value.matName = material.matName;
    selectedRowDetail.value.baseUnit = material.unit;
  }
  materialDialogVisible.value = false;
};

/* ===========================
   신규 BOM 등록
=========================== */
const bomMaster = ref({
  prodId: '',
  prodName: '',
  effectiveDate: new Date(),
  expireDate: new Date('9999-12-31')
});
const bomDetails = ref([{ id: 1, matId: '', matName: '', mixingRate: 0, qty: 0, baseUnit: '' }]);

const addRow = () => {
  bomDetails.value.push({ id: Date.now(), matId: '', matName: '', mixingRate: 0, qty: 0, baseUnit: '' });
};
const deleteRow = () => {
  if (bomDetails.value.length > 1) bomDetails.value.pop();
};

const saveNewBom = async () => {
  const prodId = selectedProd.value?.prodId || bomMaster.value.prodId;
  if (!prodId) {
    toast('warn', '제품을 선택해주세요.');
    return;
  }
  const req = {
    bom: {
      prodId,
      effectiveDate: formatDateOnly(bomMaster.value.effectiveDate),
      expireDate: formatDateOnly(bomMaster.value.expireDate)
    },
    details: bomDetails.value.map((d) => ({
      matId: d.matId,
      mixingRate: d.mixingRate ?? 0,
      qty: d.qty ?? 0,
      baseUnit: d.baseUnit || 'EA'
    }))
  };
  try {
    await axios.post('/api/bom/with-detail', req);
    toast('success', 'BOM 등록 완료', '');
    await fetchBomList(prodId);
    resetNewForm();
    activeTabIndex.value = 1;
  } catch {
    toast('error', 'BOM 등록 실패', '입력값을 확인하세요.');
  }
};
const resetNewForm = () => {
  bomMaster.value = { prodId: '', prodName: '', effectiveDate: new Date(), expireDate: new Date('9999-12-31') };
  bomDetails.value = [{ id: 1, matId: '', matName: '', mixingRate: 0, qty: 0, baseUnit: '' }];
};

/* ===========================
   Mounted
=========================== */
onMounted(fetchProdList);
</script>

<template>
  <div class="container">
    <Breadcrumb class="rounded-lg mb-4" :home="breadcrumbHome" :model="breadcrumbItems" />

    <div class="main-wrapper">
      <!-- 좌: 제품 목록 -->
      <div class="panel left-panel">
        <div class="card flex flex-col gap-3">
          <div class="font-semibold text-xl">제품 목록</div>
          <Divider class="mt-0" />
          <div class="flex gap-2">
            <AutoComplete v-model="searchFilters.prodName" :suggestions="suggestions" @complete="searchProdSuggestions" placeholder="제품명 입력" class="w-full" />
            <Button icon="pi pi-search" @click="fetchProdList" />
            <Button icon="pi pi-refresh" class="p-button-outlined" @click="resetSearch" />
          </div>
          <DataTable :value="prodList" selectionMode="single" dataKey="prodId" v-model:selection="selectedProd" :loading="loading" @row-select="selectProduct($event.data)" stripedRows paginator :rows="6" class="flex-grow overflow-auto">
            <Column field="prodId" header="제품코드" />
            <Column field="prodName" header="제품명" />
          </DataTable>
        </div>
      </div>

      <!-- 우: 신규 BOM 등록 -->
      <div class="panel right-panel">
        <div class="card flex flex-col gap-3 h-full">
          <div v-if="selectedProd" class="flex flex-col gap-4">
            <div class="grid gap-4 md:grid-cols-3">
              <InputGroup>
                <InputGroupAddon><i :class="useIcon('calendar')" /></InputGroupAddon>
                <Calendar v-model="bomMaster.effectiveDate" dateFormat="yy-mm-dd" />
              </InputGroup>
              <InputGroup>
                <InputGroupAddon><i :class="useIcon('calendar')" /></InputGroupAddon>
                <Calendar v-model="bomMaster.expireDate" dateFormat="yy-mm-dd" />
              </InputGroup>
            </div>

            <div class="flex justify-between items-center">
              <div class="font-semibold text-l">자재 구성</div>
              <div class="flex gap-2">
                <btn color="secondary" icon="add" @click="addRow" />
                <btn color="secondary" icon="minus" @click="deleteRow" />
                <btn color="info" icon="check" label="BOM 등록" outlined @click="saveNewBom" />
              </div>
            </div>

            <DataTable :value="bomDetails" stripedRows>
              <Column header="자재선택" style="width: 8rem">
                <template #body="{ data }">
                  <Button icon="pi pi-search" class="p-button-text p-button-sm" @click="openMaterialDialog(data)" />
                </template>
              </Column>
              <Column field="matId" header="자재코드">
                <template #body="{ data }">
                  <InputText v-model="data.matId" readonly class="w-full" />
                </template>
              </Column>
              <Column field="matName" header="자재명">
                <template #body="{ data }">
                  <InputText v-model="data.matName" readonly class="w-full" />
                </template>
              </Column>
              <Column field="baseUnit" header="단위">
                <template #body="{ data }">
                  <InputText v-model="data.baseUnit" readonly class="w-full" />
                </template>
              </Column>
              <Column field="mixingRate" header="비율(%)">
                <template #body="{ data }">
                  <InputNumber v-model="data.mixingRate" :useGrouping="false" class="w-full" />
                </template>
              </Column>
              <Column field="qty" header="수량">
                <template #body="{ data }">
                  <InputNumber v-model="data.qty" :useGrouping="false" class="w-full" />
                </template>
              </Column>
            </DataTable>
          </div>
          <div v-else>제품을 먼저 선택하세요.</div>
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
.flex-grow {
  flex: 1 1 auto;
}
.overflow-auto {
  overflow: auto;
}
</style>
