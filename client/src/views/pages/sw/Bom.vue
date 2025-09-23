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
import Divider from 'primevue/divider';
import Message from 'primevue/message';
import Skeleton from 'primevue/skeleton';
import Calendar from 'primevue/calendar';
import RadioButton from 'primevue/radiobutton';

// Tab state
const activeTabIndex = ref(0);

// Loading & editing
const loading = ref(false);
const isEditing = ref(false);

// Filters & selected
const searchFilters = ref({
  prodName: '',
  expiryDate: null,
  prodCode: '',
  spec: '',
  uom: '',
  bomVer: ''
});
const prodList = ref([]);
const selectedProd = ref(null);
const originalForm = ref(null);

// Form & BOM
const form = ref({ prodCode: '', prodName: '', expiryDate: null, spec: '', uom: '', bomCode: '', bomVer: 'v1' });
const bomList = ref([]);
const bomHistory = ref([]);
const selectedVersion = ref('v1');

// Skeleton placeholders
const skeletonProducts = new Array(3).fill({});
const skeletonBomItems = new Array(3).fill({});

// Fetch product list (mock)
const fetchProdList = async () => {
  loading.value = true;
  await new Promise((r) => setTimeout(r, 300));
  prodList.value = [
    { prodCode: '100-00-1', prodName: '제품1', uom: 'EA', bomVer: 'v1', expiryDate: '2026-05-15', spec: '규격A' },
    { prodCode: '100-00-2', prodName: '제품2', uom: 'EA', bomVer: 'v2', expiryDate: '2026-06-20', spec: '규격B' }
  ];
  loading.value = false;
};

// Fetch detail (mock)
const fetchDetails = async (prodCode) => {
  loading.value = true;
  await new Promise((r) => setTimeout(r, 300));
  const p = prodList.value.find((p) => p.prodCode === prodCode);
  form.value = { ...p, bomCode: `BOM-${prodCode.slice(-3)}`, bomVer: 'v1' };
  originalForm.value = { ...form.value };
  bomList.value = [
    { materialCode: 'M001', materialName: '자재1', spec: '10mm', qty: 10, uom: 'EA', expiryDate: '2026-01-01', remainingDays: 92 },
    { materialCode: 'M002', materialName: '자재2', spec: '20mm', qty: 5, uom: 'EA', expiryDate: '2026-01-02', remainingDays: 93 }
  ];
  bomHistory.value = [
    { ver: 'v1', changeDate: '2025-09-14', changer: '관리자', changeDesc: '수량 변경' },
    { ver: 'v2', changeDate: '2025-09-15', changer: '관리자', changeDesc: '신규 추가' }
  ];
  loading.value = false;
};

const onRowSelect = (event) => {
  selectedProd.value = event.data;
  fetchDetails(event.data.prodCode);
  isEditing.value = false;
};
const resetSearch = () => {
  searchFilters.value = { prodName: '', expiryDate: null, prodCode: '', spec: '', uom: '', bomVer: '' };
  fetchProdList();
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
        <!-- Left Panel -->
        <div class="panel left-panel">
          <Card class="h-full flex flex-column">
            <template #title><div class="text-xl">제품 목록</div></template>
            <template #content>
              <Toolbar class="mb-2">
                <template #start>
                  <div class="flex gap-2 flex-wrap">
                    <InputText v-model="searchFilters.prodName" placeholder="제품명" class="flex-1" />
                    <Calendar v-model="searchFilters.expiryDate" selectionMode="range" placeholder="유통기한" class="flex-1" />
                    <InputText v-model="searchFilters.prodCode" placeholder="제품코드" class="flex-1" />
                    <InputText v-model="searchFilters.spec" placeholder="규격" class="flex-1" />
                    <InputText v-model="searchFilters.uom" placeholder="단위" class="flex-1" />
                    <InputText v-model="searchFilters.bomVer" placeholder="BOM 버전" class="flex-1" />
                  </div>
                </template>
                <template #end>
                  <div class="flex gap-2">
                    <Button label="조회" icon="pi pi-search" @click="fetchProdList" />
                    <Button label="초기화" icon="pi pi-refresh" class="p-button-outlined" @click="resetSearch" />
                  </div>
                </template>
              </Toolbar>

              <div class="data-table-wrapper">
                <DataTable :value="loading ? skeletonProducts : prodList" v-model:selection="selectedProd" selectionMode="single" dataKey="prodCode" @row-select="onRowSelect" responsiveLayout="scroll" paginator :rows="6" stripedRows>
                  <Column selectionMode="single" headerStyle="width:3rem" />
                  <Column field="prodCode" header="제품코드" />
                  <Column field="prodName" header="제품명" />
                </DataTable>
              </div>
            </template>
          </Card>
        </div>

        <!-- Right Panel -->
        <div class="panel right-panel">
          <Card class="h-full flex flex-column">
            <template #content>
              <TabView v-model:activeIndex="activeTabIndex" class="h-full flex flex-column">
                <!-- 제품 상세정보 -->
                <TabPanel header="제품 상세정보">
                  <div v-if="selectedProd">
                    <Fieldset legend="제품 상세정보">
                      <div class="p-fluid grid">
                        <div class="field col-12 md:col-6"><label>제품코드</label><InputText v-model="form.prodCode" disabled /></div>
                        <div class="field col-12 md:col-6"><label>BOM 코드</label><InputText v-model="form.bomCode" /></div>
                        <div class="field col-12 md:col-6"><label>제품명</label><InputText v-model="form.prodName" /></div>
                        <div class="field col-12 md:col-6"><label>규격</label><InputText v-model="form.spec" /></div>
                        <div class="field col-12 md:col-6"><label>유통기한</label><Calendar v-model="form.expiryDate" :manualInput="false" dateFormat="yy-mm-dd" /></div>
                        <div class="field col-12 md:col-6"><label>단위</label><InputText v-model="form.uom" /></div>
                      </div>
                    </Fieldset>
                    <div class="flex justify-content-end gap-2 mt-2">
                      <Button label="삭제" icon="pi pi-trash" class="p-button-danger" />
                      <Button label="수정" icon="pi pi-pencil" class="p-button-primary" />
                      <Button label="등록" icon="pi pi-plus" />
                    </div>
                  </div>
                  <div v-else><Message severity="info" :closable="false">제품 목록에서 항목 선택</Message></div>
                </TabPanel>

                <!-- BOM 관리 -->
                <TabPanel header="BOM 관리">
                  <div v-if="selectedProd">
                    <div class="grid p-fluid">
                      <div class="col-12 md:col-4">
                        <Fieldset legend="버전 관리">
                          <div class="flex flex-column gap-2 mb-4">
                            <div class="flex align-items-center"><RadioButton v-model="selectedVersion" value="v1" /><label class="ml-2">v1</label></div>
                            <div class="flex align-items-center"><RadioButton v-model="selectedVersion" value="v2" /><label class="ml-2">v2</label></div>
                          </div>
                        </Fieldset>
                      </div>
                      <div class="col-12 md:col-8">
                        <Fieldset legend="BOM 상세정보">
                          <DataTable :value="bomList" responsiveLayout="scroll">
                            <Column selectionMode="multiple" headerStyle="width:3rem" />
                            <Column field="materialCode" header="자재코드" />
                            <Column field="materialName" header="자재명" />
                            <Column field="spec" header="규격" />
                            <Column field="qty" header="수량" />
                            <Column field="uom" header="단위" />
                          </DataTable>
                        </Fieldset>
                      </div>
                    </div>

                    <Divider class="my-4" />
                    <Fieldset legend="BOM 이력조회">
                      <DataTable :value="bomHistory" responsiveLayout="scroll">
                        <Column field="ver" header="버전" />
                        <Column field="changeDate" header="변경일자" />
                        <Column field="changer" header="변경자" />
                        <Column field="changeDesc" header="변경내용" />
                      </DataTable>
                    </Fieldset>
                  </div>
                  <div v-else><Message severity="info" :closable="false">제품 선택 필요</Message></div>
                </TabPanel>
              </TabView>
            </template>
          </Card>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.main-wrapper {
  height: 100vh;
  overflow: hidden;
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
.data-table-wrapper {
  flex-grow: 1;
  overflow-y: auto;
}
</style>
