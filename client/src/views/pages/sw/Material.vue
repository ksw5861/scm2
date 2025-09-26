<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
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
const form = reactive({
  matId: '',
  matName: '',
  matType: '',
  spec: '',
  unit: '',
  matStoreCond: '',
  matUnitConv: '',
  leadTime: null,
  matUnitPrice: '',
  safeStock: '',
  status: ''
});

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

// 중복 체크 헬퍼
const isDuplicateName = (name) => {
  if (!name) return false;
  const normalize = (str) => str.replace(/\s+/g, '').toLowerCase();
  const inputName = normalize(name);
  return materials.value.some((m) => normalize(m.matName) === inputName && (m.matId ?? m.MAT_ID) !== (detail.matId ?? detail.MAT_ID));
};

// 숫자만 남기는 헬퍼
const sanitizeNumber = (val) => {
  if (val === null || val === undefined) return '';
  return String(val).replace(/[^0-9.]/g, '');
};

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
    const payload = {
      ...form,
      matUnitConv: sanitizeNumber(form.matUnitConv),
      leadTime: sanitizeNumber(form.leadTime),
      safeStock: sanitizeNumber(form.safeStock),
      matUnitPrice: sanitizeNumber(form.matUnitPrice)
    };
    delete payload.matId;

    const res = await axios.post('/api/material', payload);
    toast('success', '등록 성공', '자재가 등록되었습니다.');
    await fetchList();

    const newId = res.data?.matId ?? res.data?.MAT_ID ?? null;
    if (newId) {
      await fetchDetail(newId);
      mode.value = 'view';
      selectedMaterial.value = materials.value.find((m) => (m.matId ?? m.MAT_ID) === newId) ?? null;
    } else {
      Object.keys(form).forEach((k) => (form[k] = ''));
      mode.value = 'create';
    }
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
    const payload = {
      ...form,
      matUnitConv: sanitizeNumber(form.matUnitConv),
      leadTime: sanitizeNumber(form.leadTime),
      safeStock: sanitizeNumber(form.safeStock),
      matUnitPrice: sanitizeNumber(form.matUnitPrice)
    };

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
    Object.keys(detail).forEach((k) => (detail[k] = ''));
    Object.keys(form).forEach((k) => (form[k] = ''));
    selectedMaterial.value = null;
    mode.value = 'create';
    await fetchList();
  } catch (e) {
    console.error('deleteMaterial error', e);
    toast('error', '삭제 실패', '자재 삭제 중 오류가 발생했습니다.');
  }
};

// 이벤트 핸들러
const handleRowSelect = async (row) => {
  selectedMaterial.value = row;
  const id = row.matId ?? row.MAT_ID;
  await fetchDetail(id);
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
  selectedMaterial.value = null;
  Object.keys(detail).forEach((k) => (detail[k] = ''));
  Object.keys(form).forEach((k) => (form[k] = ''));
  mode.value = 'create';
  fetchList();
};
const handleReset = () => {
  Object.assign(searchParams, { matId: '', matName: '', matType: '', matStoreCond: '', status: '' });
  handleSearch();
};

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
            <label><input type="radio" value="사용" v-model="searchParams.status" /> 사용</label>
            <label><input type="radio" value="미사용" v-model="searchParams.status" /> 미사용</label>
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

      <!-- 상세 / 폼 -->
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

          <div class="grid grid-cols-2 gap-4 mb-4">
            <div>
              <label class="text-sm block mb-1">자재명</label>
              <InputText v-model="form.matName" class="w-full h-10" placeholder="자재명" />
            </div>
            <div>
              <label class="text-sm block mb-1">자재유형</label>
              <InputText v-model="form.matType" class="w-full h-10" />
            </div>
            <div>
              <label class="text-sm block mb-1">보관조건</label>
              <InputText v-model="form.matStoreCond" class="w-full h-10" />
            </div>
            <div>
              <label class="text-sm block mb-1">단위</label>
              <InputText v-model="form.unit" class="w-full h-10" />
            </div>
            <div>
              <label class="text-sm block mb-1">규격</label>
              <InputText v-model="form.spec" class="w-full h-10" />
            </div>
            <div>
              <label class="text-sm block mb-1">상태</label>
              <div class="flex gap-2 items-center">
                <label><input type="radio" value="사용" v-model="form.status" /> 사용</label>
                <label><input type="radio" value="미사용" v-model="form.status" /> 미사용</label>
              </div>
            </div>
            <div>
              <label class="text-sm block mb-1">단위환산</label>
              <InputText
                v-model="form.matUnitConv"
                class="w-full h-10"
                @keypress="
                  (e) => {
                    if (!/[0-9.]/.test(e.key)) e.preventDefault();
                  }
                "
              />
            </div>

            <div>
              <label class="text-sm block mb-1">리드타임</label>
              <InputText
                v-model="form.leadTime"
                class="w-full h-10"
                @keypress="
                  (e) => {
                    if (!/[0-9.]/.test(e.key)) e.preventDefault();
                  }
                "
              />
            </div>

            <div>
              <label class="text-sm block mb-1">안전재고</label>
              <InputText
                v-model="form.safeStock"
                class="w-full h-10"
                @keypress="
                  (e) => {
                    if (!/[0-9.]/.test(e.key)) e.preventDefault();
                  }
                "
              />
            </div>

            <div>
              <label class="text-sm block mb-1">단가</label>
              <InputText
                v-model="form.matUnitPrice"
                class="w-full h-10"
                @keypress="
                  (e) => {
                    if (!/[0-9.]/.test(e.key)) e.preventDefault();
                  }
                "
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </Fluid>
</template>

<style scoped>
.SearchCard .flex > .p-2 {
  padding: 0.5rem;
}
</style>
