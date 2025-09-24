<script setup>
import { ref, onMounted } from 'vue';
import Toolbar from 'primevue/toolbar';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
import TabView from 'primevue/tabview';
import TabPanel from 'primevue/tabpanel';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Card from 'primevue/card';
import Fieldset from 'primevue/fieldset';
import Dialog from 'primevue/dialog';
import InputNumber from 'primevue/inputnumber';
import Calendar from 'primevue/calendar';
import { useAppToast } from '@/composables/useAppToast';
import axios from 'axios';

const { toast } = useAppToast();

// Tab state
const activeTabIndex = ref(0);

// Loading
const loading = ref(false);

// 검색 및 선택
const searchFilters = ref({ prodName: '', prodCode: '' });
const prodList = ref([]);
const selectedProd = ref(null);

// Form
const form = ref({ prodId: '', prodName: '', spec: '', unit: '', bomVer: '' });

// BOM
const bomList = ref([]);

// BOM Dialog
const bomDialogVisible = ref(false);
const isEditing = ref(false);
const currentBom = ref({
  bomId: '',
  bomVer: '',
  effectiveDate: new Date(),
  expireDate: new Date('9999-12-31'),
  prodId: '',
  matId: '',
  qty: 0,
  material: { unit: '' }
});

// 자재 선택 모달
const materialList = ref([]);
const materialDialogVisible = ref(false);

// 단위 선택 모달
const unitList = ref([]);
const unitDialogVisible = ref(false);

// 페이징
const prodRows = 5;
const bomRows = 5;

// --- 날짜 포맷 함수 ---
const formatDate = (date) => {
  if (!date) return '';
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

// --- 제품 목록 fetch ---
const fetchProdList = async () => {
  loading.value = true;
  try {
    const res = await axios.get('/api/product', { params: searchFilters.value });
    prodList.value = Array.isArray(res.data) ? res.data : [];
  } catch (e) {
    toast('error', '조회 실패', '제품 정보를 가져오지 못했습니다.');
    prodList.value = [];
  } finally {
    loading.value = false;
  }
};

// --- 제품 선택 ---
const selectProduct = async (prod) => {
  selectedProd.value = prod || null;
  form.value = selectedProd.value ? { prodId: prod.prodId, prodName: prod.prodName, spec: prod.spec, unit: prod.unit, bomVer: prod.bomVer } : { prodId: '', prodName: '', spec: '', unit: '', bomVer: '' };

  if (selectedProd.value) await fetchBomList(selectedProd.value.prodId);
  else bomList.value = [];
};

// --- BOM 목록 fetch ---
const fetchBomList = async (prodId) => {
  loading.value = true;
  try {
    const res = await axios.get(`/api/bom/${prodId}`);
    bomList.value = Array.isArray(res.data) ? res.data : [];
    bomList.value.forEach((b) => {
      b.effectiveDate = b.effectiveDate ? new Date(b.effectiveDate) : null;
      b.expireDate = b.expireDate ? new Date(b.expireDate) : null;
    });
  } catch (e) {
    toast('error', '조회 실패', 'BOM 정보를 가져오지 못했습니다.');
    bomList.value = [];
  } finally {
    loading.value = false;
  }
};

// --- 검색 초기화 ---
const resetSearch = () => {
  searchFilters.value = { prodName: '', prodCode: '' };
  fetchProdList();
};

// --- BOM Dialog 열기 ---
const openBomDialog = async (bom = null) => {
  if (bom) {
    isEditing.value = true;
    currentBom.value = { ...bom };
  } else {
    isEditing.value = false;
    const nextBomId = 'BOM' + String(Date.now());
    const nextBomVer = bomList.value.length ? Math.max(...bomList.value.map((b) => parseInt(b.bomVer.replace('V', '')))) + 1 : 1;
    currentBom.value = {
      bomId: nextBomId,
      bomVer: 'V' + nextBomVer,
      effectiveDate: new Date(),
      expireDate: new Date('9999-12-31'),
      prodId: selectedProd.value.prodId,
      matId: '',
      qty: 0,
      material: { unit: '' }
    };
  }
  bomDialogVisible.value = true;
};

// --- BOM 저장/수정 ---
const saveBom = async () => {
  try {
    const payload = {
      ...currentBom.value,
      effectiveDate: formatDate(currentBom.value.effectiveDate),
      expireDate: formatDate(currentBom.value.expireDate),
      createdAt: formatDate(new Date())
    };
    const url = isEditing.value ? `/api/bom/${payload.bomId}` : '/api/bom';
    const method = isEditing.value ? 'put' : 'post';

    await axios({ url, method, data: payload });
    toast('success', '저장 완료', isEditing.value ? 'BOM이 수정되었습니다.' : 'BOM이 등록되었습니다.');
    bomDialogVisible.value = false;
    await fetchBomList(selectedProd.value.prodId);
  } catch (e) {
    toast('error', '저장 실패', 'BOM 저장 중 오류가 발생했습니다.');
  }
};

// --- BOM 삭제 ---
const deleteBom = async (bomId) => {
  if (!confirm('정말 삭제하시겠습니까?')) return;
  try {
    await axios.delete(`/api/bom/${bomId}`);
    toast('success', '삭제 완료', 'BOM이 삭제되었습니다.');
    await fetchBomList(selectedProd.value.prodId);
  } catch (e) {
    toast('error', '삭제 실패', 'BOM 삭제 중 오류가 발생했습니다.');
  }
};

// --- 자재 목록 fetch ---
const fetchMaterialList = async () => {
  try {
    const res = await axios.get('/api/material');
    materialList.value = Array.isArray(res.data) ? res.data : [];
  } catch (e) {
    toast('error', '조회 실패', '자재 정보를 가져오지 못했습니다.');
    materialList.value = [];
  }
};

// --- 자재 선택 모달 열기 ---
const openMaterialDialog = async () => {
  await fetchMaterialList();
  materialDialogVisible.value = true;
};

// --- 자재 선택 ---
const selectMaterial = (material) => {
  currentBom.value.matId = material.matId;
  currentBom.value.material = { ...material, unit: currentBom.value.material.unit || '' };
  materialDialogVisible.value = false;
};

// --- 단위 목록 fetch ---
const fetchUnitList = async () => {
  try {
    const res = await axios.get('/api/unit');
    unitList.value = Array.isArray(res.data) ? res.data : [];
  } catch (e) {
    toast('error', '조회 실패', '단위 정보를 가져오지 못했습니다.');
    unitList.value = [];
  }
};

// --- 단위 선택 모달 열기 ---
const openUnitDialog = async () => {
  await fetchUnitList();
  unitDialogVisible.value = true;
};

// --- 단위 선택 ---
const selectUnit = (unit) => {
  currentBom.value.material.unit = unit.unitName;
  unitDialogVisible.value = false;
};

onMounted(() => fetchProdList());
</script>

<template>
  <div class="main-wrapper">
    <div class="card p-4">
      <div class="flex align-items-center gap-2 mb-4">
        <i class="pi pi-sitemap" style="font-size: 1.5rem"></i>
        <h2 class="m-0">BOM 관리</h2>
      </div>

      <div class="main-container">
        <!-- 좌측 제품 목록 -->
        <div class="panel left-panel">
          <Card class="h-full flex flex-column">
            <template #title><div class="text-xl">제품 목록</div></template>
            <template #content>
              <Toolbar class="mb-2">
                <template #start>
                  <div class="flex gap-2 flex-wrap">
                    <InputText v-model="searchFilters.prodName" placeholder="제품명" class="w-24" />
                    <InputText v-model="searchFilters.prodCode" placeholder="제품코드" class="w-24" />
                  </div>
                </template>
                <template #end>
                  <div class="flex gap-2">
                    <Button label="조회" icon="pi pi-search" @click="fetchProdList" />
                    <Button label="초기화" icon="pi pi-refresh" class="p-button-outlined" @click="resetSearch" />
                  </div>
                </template>
              </Toolbar>

              <DataTable :value="prodList" selectionMode="single" dataKey="prodId" :loading="loading" @row-click="selectProduct($event.data)" stripedRows paginator :rows="prodRows">
                <Column field="prodId" header="제품코드" />
                <Column field="prodName" header="제품명" />
              </DataTable>
            </template>
          </Card>
        </div>

        <!-- 우측 상세정보 -->
        <div class="panel right-panel">
          <Card class="h-full flex flex-column">
            <template #content>
              <TabView v-model:activeIndex="activeTabIndex" class="h-full flex flex-column" :key="selectedProd ? selectedProd.prodId : 'empty'">
                <!-- 제품 상세정보 -->
                <TabPanel header="제품 상세정보">
                  <div v-if="selectedProd" class="flex flex-column h-full">
                    <Fieldset legend="제품 상세정보">
                      <div class="p-fluid grid gap-2">
                        <div class="flex flex-column"><label>제품코드</label><InputText v-model="form.prodId" class="w-full h-10" disabled /></div>
                        <div class="flex flex-column"><label>제품명</label><InputText v-model="form.prodName" class="w-full h-10" disabled /></div>
                        <div class="flex flex-column"><label>규격</label><InputText v-model="form.spec" class="w-full h-10" disabled /></div>
                        <div class="flex flex-column"><label>단위</label><InputText v-model="form.unit" class="w-full h-10" disabled /></div>
                        <div class="flex flex-column"><label>BOM 버전</label><InputText v-model="form.bomVer" class="w-full h-10" disabled /></div>
                      </div>
                    </Fieldset>
                  </div>
                  <div v-else>제품 선택 필요</div>
                </TabPanel>

                <!-- BOM 관리 -->
                <TabPanel header="BOM 관리" class="flex flex-column h-full">
                  <div v-if="selectedProd" class="flex flex-column h-full">
                    <Button label="신규 BOM 등록" icon="pi pi-plus" class="mb-2" @click="openBomDialog()" />
                    <Fieldset legend="BOM 상세정보" class="flex flex-column h-full">
                      <div class="flex-grow-1 overflow-auto">
                        <DataTable :value="bomList" paginator :rows="bomRows" dataKey="bomId" :loading="loading">
                          <Column field="material.matId" header="자재코드" />
                          <Column field="material.matName" header="자재명" />
                          <Column field="material.spec" header="규격" />
                          <Column field="qty" header="수량" />
                          <Column field="material.unit" header="단위" />
                          <Column header="시작일" :body="(row) => formatDate(row.effectiveDate)" />
                          <Column header="종료일" :body="(row) => formatDate(row.expireDate)" />
                          <Column header="액션">
                            <template #body="slotProps">
                              <div class="flex gap-1">
                                <Button icon="pi pi-pencil" class="p-button-text p-button-sm" @click="openBomDialog(slotProps.data)" />
                                <Button icon="pi pi-trash" class="p-button-text p-button-sm p-button-danger" @click="deleteBom(slotProps.data.bomId)" />
                              </div>
                            </template>
                          </Column>
                        </DataTable>
                      </div>
                    </Fieldset>
                  </div>
                  <div v-else>제품 선택 필요</div>
                </TabPanel>
              </TabView>
            </template>
          </Card>
        </div>
      </div>
    </div>

    <!-- BOM 등록/수정 Dialog -->
    <Dialog v-model:visible="bomDialogVisible" :header="isEditing ? 'BOM 수정' : 'BOM 등록'" modal>
      <div class="p-fluid grid gap-2">
        <div class="flex flex-column">
          <label>자재코드</label>
          <div class="flex gap-2">
            <InputText v-model="currentBom.matId" disabled />
            <Button label="자재 선택" icon="pi pi-search" @click="openMaterialDialog()" />
          </div>
        </div>
        <div class="flex flex-column">
          <label>자재명</label>
          <InputText v-model="currentBom.material.matName" disabled />
        </div>
        <div class="flex flex-column">
          <label>규격</label>
          <InputText v-model="currentBom.material.spec" disabled />
        </div>
        <div class="flex flex-column">
          <label>단위</label>
          <div class="flex gap-2">
            <InputText v-model="currentBom.material.unit" disabled />
            <Button label="단위 선택" icon="pi pi-search" @click="openUnitDialog()" />
          </div>
        </div>
        <div class="flex flex-column">
          <label>수량</label>
          <InputNumber v-model="currentBom.qty" mode="decimal" />
        </div>
        <div class="flex flex-column">
          <label>시작일</label>
          <Calendar v-model="currentBom.effectiveDate" dateFormat="yy-mm-dd" />
        </div>
        <div class="flex flex-column">
          <label>종료일</label>
          <Calendar v-model="currentBom.expireDate" dateFormat="yy-mm-dd" />
        </div>
        <div class="flex justify-content-end mt-2">
          <Button label="저장" icon="pi pi-check" @click="saveBom" />
        </div>
      </div>
    </Dialog>

    <!-- 자재 선택 Dialog -->
    <Dialog v-model:visible="materialDialogVisible" header="자재 선택" modal>
      <DataTable :value="materialList" selectionMode="single" dataKey="matId" @row-click="selectMaterial($event.data)">
        <Column field="matId" header="자재코드" />
        <Column field="matName" header="자재명" />
      </DataTable>
    </Dialog>

    <!-- 단위 선택 Dialog -->
    <Dialog v-model:visible="unitDialogVisible" header="단위 선택" modal>
      <DataTable :value="unitList" selectionMode="single" dataKey="unitId" @row-click="selectUnit($event.data)">
        <Column field="unitId" header="단위코드" />
        <Column field="unitName" header="단위명" />
      </DataTable>
    </Dialog>
  </div>
</template>

<style scoped>
.main-wrapper {
  height: 100vh;
  padding: 1rem;
  box-sizing: border-box;
}
.main-container {
  display: flex;
  gap: 1rem;
  height: calc(100vh - 120px);
}
.panel {
  flex: 1;
}
.h-full {
  height: 100%;
}
.flex-column {
  display: flex;
  flex-direction: column;
}
.flex-grow-1 {
  flex-grow: 1;
}
.overflow-auto {
  overflow: auto;
}
.w-24 {
  width: 6rem;
}
</style>
