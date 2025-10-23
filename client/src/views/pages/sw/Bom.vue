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
import Breadcrumb from 'primevue/breadcrumb'; // 👈 PrimeVue Breadcrumb import 추가
import { useAppToast } from '@/composables/useAppToast';
import { useRoute } from 'vue-router';
import { useIcon } from '@/composables/useIcon';
import axios from 'axios';
import AutoComplete from 'primevue/autocomplete';

const route = useRoute();
const { toast } = useAppToast();
const suggestions = ref([]);

const searchProdSuggestions = async (event) => {
  const keyword = (typeof event === 'string' ? event : event.query)?.trim();
  if (!keyword) {
    suggestions.value = [];
    return;
  }

  try {
    const { data } = await axios.get(`http://13.124.12.49/api/product/autocomplete?keyword=${keyword}`);
    suggestions.value = data;
  } catch (e) {
    suggestions.value = [];
  }
};

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
const form = ref({ prodId: '', prodName: '', spec: '', unit: '', createdAt: '', lastUpdateDate: '' });

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
  qty: 0,
  mixingRate: 0,
  material: { unit: '' },
  createdAt: new Date(),
  lastUpdateDate: new Date()
});

// 자재 선택 모달
const materialList = ref([]);
const materialDialogVisible = ref(false);

// 페이징
const prodRows = 5;
const bomRows = 5;

// --- 날짜 포맷 함수 ---
const formatDateOnly = (date) => {
  if (!date) return '';
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};
const formatDate = formatDateOnly;

// --- 날짜 + 시간 포맷 함수 (lastUpdateDate 표시용) ---
const formatDateTime = (date) => {
  if (!date) return '';
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const hour = String(d.getHours()).padStart(2, '0');
  const minute = String(d.getMinutes()).padStart(2, '0');
  const second = String(d.getSeconds()).padStart(2, '0');

  return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
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
  form.value = selectedProd.value ? { prodId: prod.prodId, prodName: prod.prodName, spec: prod.spec, unit: prod.unit, createdAt: prod.createdAt, lastUpdateDate: '' } : { prodId: '', prodName: '', spec: '', unit: '', createdAt: '', lastUpdateDate: '' };

  if (selectedProd.value) await fetchBomList(selectedProd.value.prodId);
  else bomList.value = [];
};

// --- BOM 목록 fetch ---
const fetchBomList = async (prodId) => {
  loading.value = true;
  try {
    const res = await axios.get(`/api/bom/${prodId}`);
    bomList.value = Array.isArray(res.data) ? res.data : [];
    let latestDate = null;
    bomList.value.forEach((b) => {
      b.effectiveDate = b.effectiveDate ? new Date(b.effectiveDate) : null;
      b.expireDate = b.expireDate ? new Date(b.expireDate) : null;

      // 수정 부분 1: matId가 최상위 속성에 확실히 존재하도록 설정
      if (!b.matId && b.material?.matId) {
        b.matId = b.material.matId;
      }
      // material 객체가 없으면 초기화하여 material?.xxx 에러 방지
      if (!b.material) {
        b.material = { matName: '', unit: '', matId: b.matId || '' };
      }
      if (b.lastUpdateDate) {
        const currentUpdateDate = new Date(b.lastUpdateDate);
        if (!latestDate || currentUpdateDate > latestDate) {
          latestDate = currentUpdateDate;
        }
      }
    });
    form.value.lastUpdateDate = latestDate ? formatDateTime(latestDate) : '';
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
    qty: 0, // qty 초기화 추가
    material: { matName: '', unit: '', matId: '' }, // material 초기화
    createdAt: '',
    lastUpdateDate: ''
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
    currentBom.value = {
      ...bom,
      // Dialog 입력 필드에 바인딩되는 핵심 필드들을 명시적으로 설정
      matId: bom.matId,
      qty: bom.qty,

      // mixingRate는 bomDetail 객체 안에서 가져와 currentBom 최상위로 설정
      mixingRate: bom.bomDetail?.mixingRate ?? 0,

      // material 객체는 자재명/단위 표시를 위해 복사
      material: bom.material ? { ...bom.material } : { matName: '', unit: '' },

      // bomDetail 원본은 서버 전송을 위해 유지 (bomDeId 획득)
      bomDetail: bom.bomDetail
    };

    // 날짜 객체로 변환
    currentBom.value.effectiveDate = new Date(currentBom.value.effectiveDate);
    currentBom.value.expireDate = new Date(currentBom.value.expireDate);
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

    // 1) 요청 바디를 bom + details 로 구성
    const req = {
      bom: {
        bomId: currentBom.value.bomId || null, // 신규면 null/blank
        bomVersion: currentBom.value.bomVersion || 'V1',
        effectiveDate: formatDateOnly(currentBom.value.effectiveDate),
        expireDate: formatDateOnly(currentBom.value.expireDate),
        prodId: selectedProd.value.prodId,
        matId: currentBom.value.matId,
        qty: currentBom.value.qty ?? 0,
        lossRate: currentBom.value.lossRate ?? 0
      },
      details: [
        {
          bomDeId: currentBom.value.bomDetail?.bomDeId ?? null, // 수정시 채워짐, 신규면 null
          bomId: currentBom.value.bomId || null, // 서버에서 selectKey로 채움
          matId: currentBom.value.matId,
          mixingRate: currentBom.value.mixingRate ?? 0,
          baseUnit: currentBom.value.material?.unit || 'EA'
        }
      ]
    };

    // 2) 신규/수정 분기 — with-detail 엔드포인트로 호출
    const url = isEditing.value ? `/api/bom/with-detail/${req.bom.bomId}` : '/api/bom/with-detail';
    const method = isEditing.value ? 'put' : 'post';

    await axios({ url, method, data: req });

    toast('success', isEditing.value ? 'BOM 수정 완료' : 'BOM 등록 완료', '');
    bomDialogVisible.value = false;
    await fetchBomList(selectedProd.value.prodId);
    activeTabIndex.value = 1;
  } catch (e) {
    console.error(e);
    toast('error', '저장 실패', 'BOM 저장 중 오류가 발생했습니다.');
  }
};

// --- BOM 삭제 ---
const deleteBom = async (bomId) => {
  if (!confirm('정말 삭제하시겠습니까?')) return;
  try {
    const res = await axios.delete(`/api/bom/${bomId}`);
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
        <div class="panel left-panel">
          <Card class="flex-column">
            <template #title><div class="text-xl">제품 목록</div></template>
            <template #content>
              <Toolbar class="mb-2">
                <template #start>
                  <div class="flex gap-2 flex-wrap">
                    <AutoComplete v-model="searchFilters.prodName" inputId="prodName" :suggestions="suggestions" @complete="searchProdSuggestions" placeholder="제품명 입력" />
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

        <div class="panel right-panel">
          <Card class="flex-column">
            <template #content>
              <TabView v-model:activeIndex="activeTabIndex" class="h-full flex flex-column" :key="selectedProd ? selectedProd.prodId : 'empty'">
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
                        <div class="flex flex-column">
                          <label>BOM 마지막 수정날짜</label>
                          <InputText v-model="form.lastUpdateDate" class="w-full h-10" :disabled="!selectedProd" readonly />
                        </div>
                      </div>
                    </Fieldset>
                  </div>
                </TabPanel>

                <TabPanel header="BOM 관리" class="flex flex-column h-full">
                  <div v-if="selectedProd" class="flex flex-column h-full">
                    <Fieldset legend="BOM 상세정보" class="flex flex-column h-full">
                      <div class="flex-grow-1 overflow-auto">
                        <DataTable :value="bomList" paginator :rows="bomRows" dataKey="bomId" :loading="loading" class="h-full">
                          <Column header="BOM코드">
                            <template #body="slotProps">
                              {{ slotProps.data.bomId || '' }}
                            </template>
                          </Column>

                          <Column header="자재코드">
                            <template #body="slotProps">
                              {{ slotProps.data.matId || '' }}
                            </template>
                          </Column>

                          <Column header="자재명">
                            <template #body="slotProps">
                              {{ slotProps.data.material?.matName || '' }}
                            </template>
                          </Column>

                          <Column header="비율(%)">
                            <template #body="slotProps">
                              {{ slotProps.data.bomDetail?.mixingRate || '-' }}
                            </template>
                          </Column>

                          <Column header="수량">
                            <template #body="slotProps">
                              {{ slotProps.data.qty || 0 }}
                            </template>
                          </Column>

                          <Column header="단위">
                            <template #body="slotProps">
                              {{ slotProps.data.material?.unit || '' }}
                            </template>
                          </Column>

                          <Column header="시작일">
                            <template #body="slotProps">
                              {{ formatDate(slotProps.data.effectiveDate) }}
                            </template>
                          </Column>

                          <Column header="종료일">
                            <template #body="slotProps">
                              {{ formatDate(slotProps.data.expireDate) }}
                            </template>
                          </Column>

                          <Column header="수정 | 삭제">
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
                          <InputNumber
                            v-model="currentBom.mixingRate"
                            class="w-full"
                            mode="decimal"
                            :min="1"
                            :max="100"
                            :useGrouping="false"
                            @input="
                              (e) => {
                                if (currentBom.mixingRate < 1) currentBom.mixingRate = 1;
                                if (currentBom.mixingRate > 100) currentBom.mixingRate = 100;
                              }
                            "
                          />
                        </div>

                        <div>
                          <label class="block text-sm mb-1">수량</label>
                          <InputNumber
                            v-model="currentBom.qty"
                            class="w-full"
                            mode="decimal"
                            :min="0"
                            :useGrouping="false"
                            @input="
                              (e) => {
                                if (currentBom.qty < 0) currentBom.qty = 0;
                              }
                            "
                          />
                        </div>
                        <!--
                        <div>
                          <label class="block text-sm mb-1">BOM 버전</label>
                          <InputText v-model="currentBom.bomVersion" class="w-full h-10" />
                        </div>-->

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
            <InputNumber
              v-model="currentBom.qty"
              mode="decimal"
              :min="0"
              :useGrouping="false"
              @input="
                (e) => {
                  if (currentBom.qty < 0) currentBom.qty = 0;
                }
              "
            />
          </div>
          <div class="flex flex-column">
            <label>비율</label>
            <InputNumber
              v-model="currentBom.mixingRate"
              class="w-full"
              mode="decimal"
              :min="1"
              :max="100"
              :useGrouping="false"
              @input="
                (e) => {
                  if (currentBom.mixingRate < 1) currentBom.mixingRate = 1;
                  if (currentBom.mixingRate > 100) currentBom.mixingRate = 100;
                }
              "
            />
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
