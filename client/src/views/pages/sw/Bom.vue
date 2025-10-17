<script setup>
import { ref, onMounted, computed, watch } from 'vue';
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
import { useRoute } from 'vue-router';
import { useIcon } from '@/composables/useIcon';
import axios from 'axios';

const route = useRoute();
const { toast } = useAppToast();

// breadcrumb (사원 페이지 스타일과 동일)
const breadcrumbHome = { icon: useIcon('home'), to: '/' };
const breadcrumbItems = computed(() => {
  const matched = route.matched.filter((r) => r.meta);
  if (!matched.length) return [];
  const current = matched[matched.length - 1];
  const parentLabel = current.meta?.breadcrumb?.parent || '기준 정보';
  const currentLabel = current.name || '';
  return [{ label: parentLabel }, { label: currentLabel, to: route.fullPath }];
});
// Tab state
const activeTabIndex = ref(0);

// Loading
const loading = ref(false);

// 검색 및 선택
const searchFilters = ref({ prodName: '', prodId: '' });
const prodList = ref([]);
const selectedProd = ref(null);

// Form
const form = ref({ prodId: '', prodName: '', spec: '', unit: '', createdAt: '' });

// BOM
const bomList = ref([]);

// BOM Dialog
const bomDialogVisible = ref(false);
const isEditing = ref(false);
const currentBom = ref({
  bomId: '',
  bomVersion: '',
  effectiveDate: new Date(),
  expireDate: new Date('9999-12-31'),
  prodId: '',
  matId: '',
  mixingRate: 0,
  material: { unit: '' },
  createdAt: new Date()
});

// 자재 선택 모달
const materialList = ref([]);
const materialDialogVisible = ref(false);

// // 단위 선택 모달
// const unitList = ref([]);
// const unitDialogVisible = ref(false);

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
  form.value = selectedProd.value ? { prodId: prod.prodId, prodName: prod.prodName, spec: prod.spec, unit: prod.unit, createdAt: prod.createdAt } : { prodId: '', prodName: '', spec: '', unit: '', createdAt: '' };

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
  // 검색 조건 리셋
  searchFilters.value = { prodName: '', prodId: '' };
  fetchProdList();

  // 오른쪽 영역 처음 상태로 리셋
  selectedProd.value = null;
  form.value = { prodId: '', prodName: '', spec: '', unit: '', bomVersion: '', createdAt: '' };
  bomList.value = [];
};

// --- 신규 BOM 초기화 (신규탭 진입 시) ---
const initNewBom = () => {
  isEditing.value = false;
  const nextBomId = 'BOM' + String(Date.now());
  const nextBomVer = bomList.value.length ? Math.max(...bomList.value.map((b) => parseInt((b.bomVersion || 'V0').replace('V', '')))) + 1 : 1;
  currentBom.value = {
    bomId: nextBomId,
    bomVersion: 'V' + nextBomVer,
    effectiveDate: new Date(),
    expireDate: new Date('9999-12-31'),
    prodId: selectedProd.value ? selectedProd.value.prodId : '',
    matId: '',
    mixingRate: 0,
    material: { unit: '' },
    createdAt: ''
  };
};

// --- BOM Dialog 열기 ---
const openBomDialog = async (bom = null) => {
  if (!selectedProd.value) {
    toast('warn', '제품 미선택', '먼저 제품을 선택하세요.');
    return;
  }
  if (bom) {
    isEditing.value = true;
    currentBom.value = { ...bom };
  } else {
    initNewBom();
  }
  bomDialogVisible.value = true;
};

// --- BOM 저장/수정 ---
const saveBom = async () => {
  try {
    if (!selectedProd.value) {
      toast('warn', '저장 실패', '제품이 선택되어야 합니다.');
      return;
    }
    const payload = {
      ...currentBom.value,
      effectiveDate: formatDate(currentBom.value.effectiveDate),
      expireDate: formatDate(currentBom.value.expireDate),
      createdAt: formatDate(new Date())
    };
    // ensure prodId present for creation
    payload.prodId = payload.prodId || selectedProd.value.prodId;

    const url = isEditing.value ? `/api/bom/${payload.bomId}` : '/api/bom';
    const method = isEditing.value ? 'put' : 'post';

    await axios({ url, method, data: payload });
    toast('success', '저장 완료', isEditing.value ? 'BOM이 수정되었습니다.' : 'BOM이 등록되었습니다.');
    bomDialogVisible.value = false;
    // refresh list after save
    await fetchBomList(selectedProd.value.prodId);
    // if saved from 신규탭, switch to BOM 관리 tab (index 1)
    activeTabIndex.value = 1;
  } catch (e) {
    toast('error', '저장 실패', 'BOM 저장 중 오류가 발생했습니다.');
  }
};

// --- BOM 삭제 ---
const deleteBom = async (bomId) => {
  if (!confirm('정말 삭제하시겠습니까?')) return;
  try {
    const res = await axios.delete(`/api/bom/${bomId}`);
    console.log(res);
    console.log(res.data);
    if (res.data > 0) {
      toast('success', '삭제 완료', 'BOM이 삭제되었습니다.');
      await fetchBomList(selectedProd.value.prodId);
    } else {
      toast('error', '삭제 실패', 'BOM 삭제에 실패했습니다.');
    }
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
  currentBom.value.material = { ...material };
  materialDialogVisible.value = false;
};

// 자동으로 신규탭(인덱스 2)에 진입하면 신규 BOM 초기화
watch(activeTabIndex, (idx) => {
  if (idx === 2) {
    initNewBom();
  }
});

onMounted(() => fetchProdList());
</script>

<template>
  <Fluid>
    <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    <div class="main-wrapper">
      <div class="main-container">
        <!-- 좌측 제품 목록 -->
        <div class="panel left-panel">
          <Card class="flex-column">
            <template #title><div class="text-xl">제품 목록</div></template>
            <template #content>
              <Toolbar class="mb-2">
                <template #start>
                  <div class="flex gap-2 flex-wrap">
                    <InputText v-model="searchFilters.prodName" placeholder="제품명" />
                  </div>
                </template>
                <template #end>
                  <div class="flex gap-2">
                    <Button label="" icon="pi pi-search" @click="fetchProdList" />
                    <Button label="" icon="pi pi-refresh" class="p-button-outlined" @click="resetSearch" />
                  </div>
                </template>
              </Toolbar>

              <DataTable :value="prodList" selectionMode="single" dataKey="prodId" v-model:selection="selectedProd" :loading="loading" @row-select="selectProduct($event.data)" stripedRows paginator :rows="prodRows" class="flex-grow-1 overflow-auto">
                <Column field="prodId" header="제품코드" />
                <Column field="prodName" header="제품명" />
              </DataTable>
            </template>
          </Card>
        </div>

        <!-- 우측 상세정보 (비율 2) -->
        <div class="panel right-panel">
          <Card class="flex-column">
            <template #content>
              <TabView v-model:activeIndex="activeTabIndex" class="h-full flex flex-column" :key="selectedProd ? selectedProd.prodId : 'empty'">
                <!-- 제품 상세정보 -->
                <TabPanel header="제품 상세정보">
                  <div class="flex flex-column h-full">
                    <Fieldset legend="제품 상세정보">
                      <div class="p-fluid grid gap-2">
                        <div class="flex flex-column">
                          <label>제품코드</label>
                          <InputText v-model="form.prodId" class="w-full h-10" :disabled="!selectedProd" readonly />
                        </div>
                        <div class="flex flex-column">
                          <label>제품명</label>
                          <InputText v-model="form.prodName" class="w-full h-10" :disabled="!selectedProd" readonly />
                        </div>
                        <div class="flex flex-column">
                          <label>단위</label>
                          <InputText v-model="form.unit" class="w-full h-10" :disabled="!selectedProd" readonly />
                        </div>
                        <div class="flex flex-column">
                          <label>등록날짜</label>
                          <InputText v-model="form.createdAt" class="w-full h-10" :disabled="!selectedProd" readonly />
                        </div>
                      </div>
                    </Fieldset>
                  </div>
                </TabPanel>

                <!-- BOM 관리: 수정/삭제만 (신규 버튼 제거) -->
                <TabPanel header="BOM 관리" class="flex flex-column h-full">
                  <div v-if="selectedProd" class="flex flex-column h-full">
                    <Fieldset legend="BOM 상세정보" class="flex flex-column h-full">
                      <div class="flex-grow-1 overflow-auto">
                        <DataTable :value="bomList" paginator :rows="bomRows" dataKey="bomId" :loading="loading" class="h-full">
                          <!-- bom코드 -->
                          <Column header="BOM코드">
                            <template #body="slotProps">
                              {{ slotProps.data.bomId || '' }}
                            </template>
                          </Column>

                          <!-- 자재코드 -->
                          <Column header="자재코드">
                            <template #body="slotProps">
                              {{ slotProps.data.material?.matId || '' }}
                            </template>
                          </Column>

                          <!-- 자재명 -->
                          <Column header="자재명">
                            <template #body="slotProps">
                              {{ slotProps.data.material?.matName || '' }}
                            </template>
                          </Column>

                          <!-- 배합비율 -->
                          <Column header="비율(%)">
                            <template #body="slotProps">
                              {{ slotProps.data.bomDetail?.mixingRate || '' }}
                            </template>
                          </Column>

                          <!-- 수량 -->
                          <Column header="수량">
                            <template #body="slotProps">
                              {{ slotProps.data.qty || 0 }}
                            </template>
                          </Column>

                          <!-- 단위 -->
                          <Column header="단위">
                            <template #body="slotProps">
                              {{ slotProps.data.material?.unit || '' }}
                            </template>
                          </Column>

                          <!-- 시작일 -->
                          <Column header="시작일">
                            <template #body="slotProps">
                              {{ formatDate(slotProps.data.effectiveDate) }}
                            </template>
                          </Column>

                          <!-- 종료일 -->
                          <Column header="종료일">
                            <template #body="slotProps">
                              {{ formatDate(slotProps.data.expireDate) }}
                            </template>
                          </Column>

                          <!-- 액션 -->
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

                <!-- 신규 BOM 등록용 탭 -->
                <TabPanel header="신규 BOM">
                  <div v-if="selectedProd" class="flex flex-column h-full">
                    <div class="mb-2">
                      <div class="font-semibold mb-2">신규 BOM 정보 (제품: {{ selectedProd.prodName }})</div>

                      <div class="grid grid-cols-2 gap-4">
                        <div>
                          <label class="block text-sm mb-1">자재코드</label>
                          <div class="flex gap-2">
                            <InputText v-model="currentBom.matId" class="w-full h-10" placeholder="자재 선택" readonly />
                            <Button label="자재 선택" icon="pi pi-search" @click="openMaterialDialog()" />
                          </div>
                        </div>

                        <div>
                          <label class="block text-sm mb-1">자재명</label>
                          <InputText v-model="currentBom.material.matName" class="w-full h-10" readonly />
                        </div>

                        <div>
                          <label class="block text-sm mb-1">단위</label>
                          <div class="flex gap-2">
                            <InputText v-model="currentBom.material.unit" class="w-full h-10" readonly />
                          </div>
                        </div>

                        <div>
                          <label class="block text-sm mb-1">비율(%)</label>
                          <InputNumber v-model="currentBom.mixingRate" class="w-full" mode="decimal" />
                        </div>

                        <div>
                          <label class="block text-sm mb-1">BOM 버전</label>
                          <InputText v-model="currentBom.bomVersion" class="w-full h-10" />
                        </div>

                        <div>
                          <label class="block text-sm mb-1">시작일</label>
                          <Calendar v-model="currentBom.effectiveDate" class="w-full" dateFormat="yy-mm-dd" />
                        </div>

                        <div>
                          <label class="block text-sm mb-1">종료일</label>
                          <Calendar v-model="currentBom.expireDate" class="w-full" dateFormat="yy-mm-dd" />
                        </div>
                      </div>

                      <div class="flex justify-end gap-2 mt-4">
                        <Button label="초기화" icon="pi pi-refresh" class="p-button-outlined" @click="initNewBom" />
                        <Button label="저장" icon="pi pi-check" @click="saveBom" />
                      </div>
                    </div>
                  </div>
                  <div v-else>제품 선택 필요</div>
                </TabPanel>
              </TabView>
            </template>
          </Card>
        </div>
      </div>

      <!-- BOM 등록/수정 Dialog (기존대로 유지, 수정/등록 공용) -->
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
            <label>단위</label>
            <div class="flex gap-2">
              <InputText v-model="currentBom.material.unit" disabled />
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
        </div>

        <template #footer>
          <div class="flex justify-end gap-2">
            <Button label="취소" icon="pi pi-times" class="p-button-text" @click="bomDialogVisible = false" />
            <Button label="저장" icon="pi pi-check" @click="saveBom" />
          </div>
        </template>
      </Dialog>

      <!-- 자재 선택 Dialog -->
      <Dialog v-model:visible="materialDialogVisible" header="자재 선택" modal>
        <DataTable :value="materialList" selectionMode="single" dataKey="matId" @row-click="selectMaterial($event.data)" paginator :rows="8">
          <Column field="matId" header="자재코드" />
          <Column field="matName" header="자재명" />
          <Column field="unit" header="단위" />
        </DataTable>
      </Dialog>
    </div>
  </Fluid>
</template>

<style scoped>
.main-wrapper {
  height: 100vh;
  padding-top: 1rem;
  box-sizing: border-box;
}
.main-container {
  display: flex;
  gap: 1rem;
  height: calc(100vh - 120px);
}
/* 좌:우 = 1:2 */
.left-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.right-panel {
  flex: 2;
  display: flex;
  flex-direction: column;
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
