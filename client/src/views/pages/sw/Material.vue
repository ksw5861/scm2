<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import InputText from 'primevue/inputtext';
import { useIcon } from '@/composables/useIcon';
import { useAppToast } from '@/composables/useAppToast';

const route = useRoute();
const { toast } = useAppToast();

// icons
const iconList = useIcon('list');
const iconAdd = useIcon('add');
const iconEdit = useIcon('edit');
const iconInfo = useIcon('info');
const iconBox = useIcon('box');
const iconId = useIcon('id');

// breadcrumb
const breadcrumbHome = { icon: useIcon('home'), to: '/' };
const breadcrumbItems = computed(() => {
  const matched = route.matched.filter((r) => r.meta);
  if (!matched.length) return [];
  const current = matched[matched.length - 1];
  const parentLabel = current.meta?.breadcrumb?.parent || '기준 정보';
  const currentLabel = current.name || '';
  return [{ label: parentLabel }, { label: currentLabel, to: route.fullPath }];
});

// 모달 (더이상 거래처 모달은 사용하지 않지만 기존 변수 유지)
const showVendorModal = ref(false);

// 자재-거래처 폼 (필드명 DB 기준으로 변경)
const vendorForm = reactive({
  matVendorId: '',
  contractPrice: '',
  companyName: '',
  matId: '',
  vendorId: '',
  createdBy: '',
  createdAt: '',
  status: 'Y'
});

// 거래처 폼 모드: 'create' | 'edit'
const vendorMode = ref('create');

// 검색 파라미터
const searchParams = reactive({
  matId: '',
  matName: '',
  matType: '',
  matStoreCond: '',
  status: '' // '' = 전체, '사용', '미사용'
});

// 목록 / 선택 / 상세 / 폼 상태
const materials = ref([]);
const selectedMaterial = ref(null);
const detail = reactive({
  matId: '',
  matName: '',
  matType: '',
  matStoreCond: '',
  matUnitConv: null,
  matUnitPrice: '',
  leadTime: null,
  safeStock: null,
  status: '',
  spec: '',
  unit: ''
});
const form = reactive({ ...detail });

const mode = ref('create'); // create | view | edit
const loading = ref(false);

// pagination
const page = ref({ page: 1, size: 10, totalElements: 0 });

// DTable columns
const columns = [
  { label: '자재ID', field: 'matId', sortable: true },
  { label: '자재명', field: 'matName', sortable: true },
  { label: '상태', field: 'status' }
];

// 중복 체크
const isDuplicateName = (name) => {
  if (!name) return false;
  const normalize = (str) => str.replace(/\s+/g, '').toLowerCase();
  const inputName = normalize(name);
  return materials.value.some((m) => normalize(m.matName) === inputName && (m.matId ?? m.MAT_ID) !== (detail.matId ?? detail.MAT_ID));
};

// 숫자만
const sanitizeNumber = (val) => (val == null ? '' : String(val).replace(/[^0-9.]/g, ''));

// API: 목록
const fetchList = async () => {
  loading.value = true;
  try {
    const params = {
      matId: searchParams.matId || undefined,
      matName: searchParams.matName || undefined,
      matType: searchParams.matType || undefined,
      matStoreCond: searchParams.matStoreCond || undefined,
      status: searchParams.status || undefined,
      page: page.value.page,
      size: page.value.size
    };
    const res = await axios.get('/api/material', { params });
    const data = Array.isArray(res.data) ? res.data : (res.data?.data ?? res.data?.items ?? []);
    materials.value = data;
    page.value.totalElements = res.data?.page?.totalElements ?? materials.value.length;

    if (selectedMaterial.value) {
      const found = materials.value.find((m) => (m.matId ?? m.MAT_ID) === (selectedMaterial.value.matId ?? selectedMaterial.value.MAT_ID));
      if (!found) handleUnselect();
    }
  } catch (e) {
    console.error('fetchList error', e);
    toast('error', '조회 실패', '자재 목록 조회 중 오류가 발생했습니다.');
  } finally {
    loading.value = false;
  }
};

// API: 상세
const fetchDetail = async (id) => {
  if (!id) return;
  try {
    const res = await axios.get(`/api/material/${id}`);
    const data = Array.isArray(res.data) ? res.data[0] : res.data;
    if (data) {
      Object.assign(detail, {
        matId: data.matId ?? data.MAT_ID ?? '',
        matName: data.matName ?? data.MAT_NAME ?? '',
        matType: data.matType ?? data.MAT_TYPE ?? '',
        matStoreCond: data.matStoreCond ?? data.MAT_STORE_COND ?? '',
        matUnitConv: data.matUnitConv ?? data.MAT_UNIT_CONV ?? null,
        matUnitPrice: data.matUnitPrice ?? data.MAT_UNIT_PRICE ?? '',
        leadTime: data.leadTime ?? data.LEAD_TIME ?? null,
        safeStock: data.safeStock ?? data.SAFE_STOCK ?? null,
        status: data.status ?? data.STATUS ?? '',
        spec: data.spec ?? data.SPEC ?? '',
        unit: data.unit ?? data.UNIT ?? ''
      });
      Object.assign(form, detail);
      selectedMaterial.value = data;
      mode.value = 'view';
    } else {
      Object.keys(detail).forEach((k) => (detail[k] = ''));
      Object.keys(form).forEach((k) => (form[k] = ''));
      selectedMaterial.value = null;
      mode.value = 'create';
    }
  } catch (e) {
    console.error('fetchDetail error', e);
    toast('error', '조회 실패', '자재 상세 조회 중 오류가 발생했습니다.');
  }
};

// CRUD: 등록
const addMaterial = async () => {
  if (!form.matName) return toast('warn', '등록 실패', '자재명을 입력하세요.');
  if (isDuplicateName(form.matName)) return toast('warn', '중복', '이미 존재하는 자재명입니다.');

  try {
    const payload = { ...form, matUnitConv: sanitizeNumber(form.matUnitConv), leadTime: sanitizeNumber(form.leadTime), safeStock: sanitizeNumber(form.safeStock), matUnitPrice: sanitizeNumber(form.matUnitPrice) };
    delete payload.matId;

    const { data } = await axios.post('/api/material', payload);
    if (data.status === 'success') {
      toast('success', '등록 성공', '자재가 등록되었습니다.');
      await fetchList();
      const newId = data?.matId ?? data?.MAT_ID ?? null;
      if (newId) await fetchDetail(newId);
      else Object.keys(form).forEach((k) => (form[k] = ''));
    } else toast('error', '등록 실패', '자재 등록에 실패했습니다.');
  } catch (e) {
    console.error('addMaterial error', e);
    toast('error', '등록 실패', '자재 등록 중 오류가 발생했습니다.');
  }
};

// CRUD: 수정
const modifyMaterial = async () => {
  if (!detail.matId) return toast('warn', '수정 실패', '수정할 자재를 선택하세요.');
  if (isDuplicateName(form.matName)) return toast('warn', '중복', '이미 존재하는 자재명입니다.');

  try {
    const payload = { ...form, matUnitConv: sanitizeNumber(form.matUnitConv), leadTime: sanitizeNumber(form.leadTime), safeStock: sanitizeNumber(form.safeStock), matUnitPrice: sanitizeNumber(form.matUnitPrice) };
    await axios.put(`/api/material/${detail.matId}`, payload);
    toast('success', '수정 성공', '자재가 수정되었습니다.');
    await fetchList();
    await fetchDetail(detail.matId);
    mode.value = 'view';
  } catch (e) {
    console.error('modifyMaterial error', e);
    toast('error', '수정 실패', '자재 수정 중 오류가 발생했습니다.');
  }
};

// CRUD: 삭제
const deleteMaterial = async () => {
  if (!detail.matId) return toast('warn', '삭제 실패', '삭제할 자재를 선택하세요.');
  if (!confirm(`자재 [${detail.matId}]를 삭제하시겠습니까?`)) return;

  try {
    await axios.delete(`/api/material/${detail.matId}`);
    toast('success', '삭제 성공', '자재가 삭제되었습니다.');

    // 폼 초기화
    Object.keys(detail).forEach((k) => (detail[k] = ''));
    Object.keys(form).forEach((k) => (form[k] = ''));
    selectedMaterial.value = null;
    mode.value = 'create';

    await fetchList();
  } catch (e) {
    console.error('deleteMaterial error', e);

    // 409 Conflict 응답 처리
    if (e.response?.status === 409) {
      toast('warn', '삭제 불가', e.response.data?.message || '다른 테이블에서 사용 중인 자재입니다.');
    } else {
      toast('error', '삭제 실패', '자재 삭제 중 오류가 발생했습니다.');
    }
  }
};

// 이벤트
const handleRowSelect = async (row) => {
  selectedMaterial.value = row;
  const id = row.matId ?? row.MAT_ID;
  await fetchDetail(id);
  await fetchVendorsByMaterial(id);
  openVendorFormForCreate(); // 거래처 폼 초기화
  mode.value = 'view';
};
const handleUnselect = () => {
  selectedMaterial.value = null;
  Object.keys(detail).forEach((k) => (detail[k] = ''));
  Object.keys(form).forEach((k) => (form[k] = ''));
  mode.value = 'create';
};
const handleEdit = () => (mode.value = 'edit');
const handleResetForm = () => {
  Object.keys(form).forEach((k) => (form[k] = ''));
  if (mode.value === 'view' && detail.matId) Object.assign(form, detail);
};

// 검색/초기화
const handleSearch = () => {
  if (mode.value === 'edit') {
    if (!confirm('현재 수정 중입니다. 조회하면 수정 중인 내용이 사라집니다. 계속하시겠습니까?')) return;
  }
  page.value.page = 1;
  handleUnselect();
  fetchList();
};
const handleReset = () => {
  Object.assign(searchParams, { matId: '', matName: '', matType: '', matStoreCond: '', status: '' });
  handleSearch();
};

// --- 거래처 탭 ---
const activeTab = ref(0); // 0: 자재상세, 1: 거래처
const vendors = ref([]);
const vendorLoading = ref(false);
const vendorPage = ref({ page: 1, size: 10, totalElements: 0 });
const vendorColumns = [
  { label: '거래처ID', field: 'vendorId' },
  { label: '거래처명', field: 'vendorName' },
  { label: '계약단가', field: 'contractPrice' },
  { label: '상태', field: 'status' }
];

// DTable selection for vendors (left list)
const vendorSelected = ref(null);

// 거래처 폼 초기화 (신규)
function openVendorFormForCreate() {
  vendorMode.value = 'create';
  vendorSelected.value = null;
  vendorForm.matVendorId = '';
  vendorForm.matId = selectedMaterial.value?.matId ?? '';
  vendorForm.vendorId = '';
  vendorForm.companyName = '';
  vendorForm.contractPrice = '';
  vendorForm.createdBy = '';
  vendorForm.createdAt = '';
  vendorForm.status = 'Y';
}

// 거래처 폼에 선택된 거래처 데이터 복사 (수정)
function openVendorFormForEdit(row) {
  vendorMode.value = 'edit';
  vendorSelected.value = row;
  vendorForm.matVendorId = row.matVendorId;
  vendorForm.matId = row.matId;
  vendorForm.vendorId = row.vendorId;
  vendorForm.companyName = row.vendorName ?? row.companyName ?? '';
  vendorForm.contractPrice = row.contractPrice;
  vendorForm.createdBy = row.createdBy;
  vendorForm.createdAt = row.createdAt;
  vendorForm.status = row.status;
}

// 거래처 저장 (등록/수정)
const saveVendor = async () => {
  if (!selectedMaterial.value) return toast('warn', '저장 실패', '자재를 선택하세요.');
  if (!vendorForm.vendorId) return toast('warn', '저장 실패', '거래처ID를 입력하세요.');

  try {
    const matId = selectedMaterial.value.matId ?? selectedMaterial.value.MAT_ID;
    if (vendorMode.value === 'create') {
      // 등록
      const payload = {
        ...vendorForm,
        matId
      };
      const { data } = await axios.post('/api/material/vendor', payload);
      if (data > 0 || data.status === 'success') {
        toast('success', '등록 성공', '거래처가 등록되었습니다.');
        await fetchVendorsByMaterial(matId);
        openVendorFormForCreate();
      } else {
        toast('error', '등록 실패', '거래처 등록에 실패했습니다.');
      }
    } else {
      // 수정
      if (!vendorForm.matVendorId) return toast('warn', '수정 실패', '수정할 거래처를 선택하세요.');
      await axios.put(`/api/material/vendor/${vendorForm.matVendorId}`, vendorForm);
      toast('success', '수정 성공', '거래처 정보가 수정되었습니다.');
      await fetchVendorsByMaterial(matId);
    }
  } catch (e) {
    toast('error', '저장 실패', vendorMode.value === 'create' ? '거래처 등록 중 오류가 발생했습니다.' : '거래처 수정 중 오류가 발생했습니다.');
  }
};

// 거래처 삭제
const deleteVendor = async () => {
  if (!vendorForm.matVendorId) return toast('warn', '삭제 실패', '삭제할 거래처를 선택하세요.');
  if (!confirm(`거래처 [${vendorForm.vendorId}]를 삭제하시겠습니까?`)) return;

  try {
    await axios.delete(`/api/material/vendor/${vendorForm.matVendorId}`);
    toast('success', '삭제 성공', '거래처가 삭제되었습니다.');
    await fetchVendorsByMaterial(selectedMaterial.value.matId ?? selectedMaterial.value.MAT_ID);
    openVendorFormForCreate();
  } catch (e) {
    toast('error', '삭제 실패', '거래처 삭제 중 오류가 발생했습니다.');
  }
};

// 거래처 목록 조회 (자재별, 거래처명 포함)
const fetchVendorsByMaterial = async (matId) => {
  vendors.value = [];
  if (!matId) return;
  vendorLoading.value = true;
  try {
    // MaterialVendorMapper.xml의 getMaterialVendorList와 연결됨
    const res = await axios.get(`/api/material/${matId}/vendor`);
    vendors.value = Array.isArray(res.data) ? res.data : [];
  } catch (e) {
    vendors.value = [];
  } finally {
    vendorLoading.value = false;
  }
};

// 거래처 테이블 행 선택
const handleVendorRowSelect = (row) => {
  vendorSelected.value = row;
  if (row) openVendorFormForEdit(row);
};

// 거래처 선택 모달
const showVendorSelectModal = ref(false);
const vendorList = ref([]);
const vendorSearch = ref('');
const vendorLoadingModal = ref(false);

// 거래처 목록 불러오기 (모달용)
const fetchVendorListForModal = async () => {
  vendorLoadingModal.value = true;
  try {
    const res = await axios.get('/api/vendor', { params: { companyName: vendorSearch.value } });
    vendorList.value = Array.isArray(res.data) ? res.data : (res.data?.items ?? []);
  } catch (e) {
    vendorList.value = [];
  } finally {
    vendorLoadingModal.value = false;
  }
};

// 거래처 모달 열기
const openVendorModal = () => {
  vendorSearch.value = '';
  fetchVendorListForModal();
  showVendorSelectModal.value = true;
};

// 거래처 모달에서 선택
const selectVendorFromModal = (vendor) => {
  vendorForm.vendorId = vendor.vendorId;
  vendorForm.companyName = vendor.companyName; // 거래처명 자동기입
  showVendorSelectModal.value = false;
};

// 숫자포맷팅
const displayPrice = computed({
  get: () => {
    if (!form.matUnitPrice) return '';
    return form.matUnitPrice.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
  },
  set: (val) => {
    // 콤마 제거 후 숫자만 form에 저장
    form.matUnitPrice = val.replace(/,/g, '');
  }
});

onMounted(() => fetchList());
</script>

<template>
  <Fluid>
    <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />

    <!-- 검색 -->
    <SearchCard title="자재 검색" @search="handleSearch" @reset="handleReset">
      <div class="flex flex-wrap w-full gap-2">
        <div class="p-2 w-full md:w-1/4">
          <InputGroup>
            <InputGroupAddon><i :class="iconBox" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchParams.matName" inputId="matName" />
              <label for="matName">자재명</label>
            </IftaLabel>
          </InputGroup>
        </div>
        <div class="p-2 w-full md:w-1/4">
          <InputGroup>
            <InputGroupAddon><i :class="iconId" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchParams.matId" inputId="matId" />
              <label for="matId">자재ID</label>
            </IftaLabel>
          </InputGroup>
        </div>
        <div class="p-2 w-full md:w-1/4">
          <label class="block mb-1 text-sm">상태</label>
          <div class="flex gap-2 items-center">
            <label><input type="radio" value="" v-model="searchParams.status" /> 전체</label>
            <label><input type="radio" value="Y" v-model="searchParams.status" /> 사용</label>
            <label><input type="radio" value="N" v-model="searchParams.status" /> 미사용</label>
          </div>
        </div>
      </div>
    </SearchCard>

    <div class="flex flex-col md:flex-row w-full gap-4 mt-4">
      <!-- 목록 -->
      <div class="w-full xl:w-5/12">
        <div class="card flex flex-col">
          <div class="font-semibold text-xl flex items-center justify-between gap-4 h-10">
            <div class="flex items-center gap-4">
              <span :class="[iconList, 'text-xl']"></span>
              자재 목록
            </div>
            <div class="text-sm text-gray-400">
              총 <span class="font-semibold text-sm text-gray-700">{{ page.totalElements || materials.length }}</span> 건
            </div>
          </div>
          <Divider />
          <DTable :columns="columns" :data="materials" :page="page" :loading="loading" dataKey="matId" v-model:selected="selectedMaterial" @row-select="handleRowSelect" @row-unselect="handleUnselect" />
        </div>
      </div>

      <!-- 상세 / 폼 + 탭 -->
      <div class="w-full xl:w-7/12">
        <div class="card flex flex-col">
          <div class="flex items-center justify-between h-10">
            <div class="font-semibold text-xl flex items-center gap-4">
              <span :class="[mode === 'create' ? iconAdd : mode === 'edit' ? iconEdit : iconInfo, 'text-xl']"></span>
              {{ mode === 'create' ? '신규 자재 등록' : mode === 'edit' ? '자재 정보 수정' : '자재 상세 정보' }}
            </div>
            <div class="flex gap-2">
              <Btn v-if="mode === 'create'" icon="add" @click="addMaterial" outlined>등록</Btn>
              <Btn v-if="mode === 'edit'" icon="save" @click="modifyMaterial" outlined>저장</Btn>
              <Btn v-if="mode === 'view' && detail.matId" icon="edit" color="warn" @click="handleEdit" outlined>수정</Btn>
              <Btn v-if="mode !== 'create' && detail.matId" icon="delete" color="danger" @click="deleteMaterial" outlined>삭제</Btn>
              <Btn icon="refresh" color="secondary" @click="handleResetForm" outlined>초기화</Btn>
            </div>
          </div>
          <Divider />

          <!-- 탭 -->
          <TabView v-model:activeIndex="activeTab">
            <!-- 자재 상세정보 -->
            <TabPanel header="자재 상세정보">
              <div class="w-full flex flex-row mb-2 gap-2">
                <div class="flex-1 ml-6 flex flex-col justify-center gap-0">
                  <div class="font-light text-xs flex items-center gap-4 text-gray-500">
                    {{ detail.matId || (mode === 'create' ? '(신규)' : '') }}
                  </div>
                  <div class="font-semibold text-lg flex items-center gap-4">
                    <InputText v-model="form.matName" inputId="formName" class="w-full" placeholder="자재명 입력" />
                  </div>
                </div>
              </div>
              <div class="grid grid-cols-2 gap-4 mb-4">
                <div>
                  <label class="text-sm block mb-1">자재유형</label>
                  <InputText v-model="form.matType" class="w-full h-10" />
                </div>
                <div>
                  <label class="text-sm block mb-1">보관조건</label>
                  <InputText v-model="form.matStoreCond" class="w-full h-10" />
                </div>
                <div>
                  <label class="text-sm block mb-1">단위환산</label>
                  <InputText v-model="form.matUnitConv" class="w-full h-10" @keypress="(e) => /[0-9.]/.test(e.key) || e.preventDefault()" />
                </div>
                <div>
                  <label class="text-sm block mb-1">단위</label>
                  <InputText v-model="form.unit" class="w-full h-10" />
                </div>
                <div>
                  <label class="text-sm block mb-1">안전재고(EA)</label>
                  <InputText v-model="form.safeStock" class="w-full h-10" @keypress="(e) => /[0-9.]/.test(e.key) || e.preventDefault()" />
                </div>
                <div>
                  <label class="text-sm block mb-1">리드타임(일)</label>
                  <InputText v-model="form.leadTime" class="w-full h-10" @keypress="(e) => /[0-9.]/.test(e.key) || e.preventDefault()" />
                </div>
                <div>
                  <label class="text-sm block mb-1">단가(원)</label>
                  <InputText v-model="displayPrice" class="w-full h-10" />
                </div>
                <div>
                  <label class="text-sm block mb-1">상태</label>
                  <div class="flex gap-2 items-center">
                    <label><input type="radio" value="Y" v-model="form.status" /> 사용</label>
                    <label><input type="radio" value="N" v-model="form.status" /> 미사용</label>
                  </div>
                </div>
              </div>
            </TabPanel>

            <!-- 거래처 정보: 왼쪽 목록 / 오른쪽 폼 -->
            <TabPanel header="거래처 정보">
              <div v-if="!selectedMaterial">자재를 선택해야 거래처 정보를 볼 수 있습니다.</div>

              <div v-else class="w-full">
                <div class="grid grid-cols-2 gap-4">
                  <!-- 왼쪽: 거래처 목록 -->
                  <div class="col-span-1">
                    <div class="flex items-center justify-between mb-2">
                      <div class="font-semibold">거래처 목록</div>
                    </div>

                    <DTable
                      :columns="vendorColumns"
                      :data="vendors || []"
                      :page="vendorPage"
                      :loading="vendorLoading"
                      dataKey="matVendorId"
                      v-model:selected="vendorSelected"
                      @row-select="handleVendorRowSelect"
                      @row-unselect="
                        () => {
                          vendorSelected = null;
                          openVendorFormForCreate();
                        }
                      "
                    />
                  </div>

                  <!-- 오른쪽: 거래처 등록/수정 폼 -->
                  <div class="col-span-1">
                    <div class="font-semibold mb-2">거래처 등록/수정 ({{ vendorMode === 'create' ? '신규' : '수정' }})</div>

                    <div class="grid grid-cols-1 gap-3">
                      <div>
                        <label class="text-sm block mb-1">거래처ID</label>
                        <div class="flex gap-2">
                          <InputText v-model="vendorForm.vendorId" class="w-full h-10" placeholder="거래처 ID" readonly />
                          <button class="btn-secondary" type="button" @click="openVendorModal">🔍</button>
                        </div>
                      </div>

                      <div>
                        <label class="text-sm block mb-1">거래처명</label>
                        <InputText v-model="vendorForm.companyName" class="w-full h-10" placeholder="거래처명" readonly />
                      </div>

                      <div>
                        <label class="text-sm block mb-1">계약단가</label>
                        <InputText v-model="vendorForm.contractPrice" class="w-full h-10" @keypress="(e) => /[0-9.]/.test(e.key) || e.preventDefault()" placeholder="숫자만 입력" />
                      </div>

                      <div>
                        <label class="text-sm block mb-1">상태</label>
                        <div class="flex gap-2 items-center">
                          <label><input type="radio" value="Y" v-model="vendorForm.status" /> 사용</label>
                          <label><input type="radio" value="N" v-model="vendorForm.status" /> 미사용</label>
                        </div>
                      </div>

                      <div class="flex gap-2 justify-end mt-3">
                        <btn class="btn" icon="save" @click="saveVendor" outlined>저장</btn>
                        <btn color="danger" icon="delete" @click="deleteVendor" outlined>삭제</btn>
                        <btn color="secondary" icon="refresh" @click="openVendorFormForCreate" outlined>초기화</btn>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </TabPanel>
          </TabView>
        </div>
      </div>
    </div>

    <!-- 거래처 선택 모달 -->
    <Dialog v-model:visible="showVendorSelectModal" modal header="거래처 선택" :style="{ width: '600px' }">
      <div class="mb-2 flex gap-2">
        <InputText v-model="vendorSearch" placeholder="거래처명 검색" @keyup.enter="fetchVendorListForModal" />
        <button class="btn" @click="fetchVendorListForModal">🔍</button>
      </div>
      <div style="max-height: 300px; overflow: auto">
        <table class="w-full border-collapse">
          <thead>
            <tr>
              <th class="py-2">거래처ID</th>
              <th class="py-2">거래처명</th>
              <th class="py-2">대표자명</th>
              <th class="py-2">선택</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="vendor in vendorList" :key="vendor.vendorId">
              <td class="py-2">{{ vendor.vendorId }}</td>
              <td class="py-2">{{ vendor.companyName }}</td>
              <td class="py-2">{{ vendor.ceoName }}</td>
              <td class="py-2">
                <button class="btn" @click="selectVendorFromModal(vendor)">✅</button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="vendorLoadingModal" class="text-center py-2">불러오는 중...</div>
        <div v-if="!vendorLoadingModal && !vendorList.length" class="text-sm text-gray-500 py-2">검색 결과 없음</div>
      </div>
      <template #footer>
        <button class="btn-secondary" @click="showVendorSelectModal = false">닫기</button>
      </template>
    </Dialog>
  </Fluid>
</template>
