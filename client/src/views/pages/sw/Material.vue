<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import { useIcon } from '@/composables/useIcon';
import { useAppToast } from '@/composables/useAppToast';
import { useDateFormat } from '@/composables/useFormat'; // 프로젝트에 있는 경우 자동 사용

const route = useRoute();
const { toast } = useAppToast();

// icons
const iconList = useIcon('list');
const iconAdd = useIcon('add');
const iconEdit = useIcon('edit');
const iconInfo = useIcon('info');
const iconBox = useIcon('box');
const iconId = useIcon('id');

// breadcrumb (same pattern as 사원 페이지)
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
  matStoreCond: ''
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

// pagination (kept simple to match original)
const page = ref({ page: 1, size: 10, totalElements: 0 });

// DTable columns
const columns = [
  { label: '자재ID', field: 'matId', sortable: true },
  { label: '자재명', field: 'matName', sortable: true }
];

// 날짜 포맷 (useDateFormat 있으면 사용, 없으면 fallback)
const tryUseDateFormat = typeof useDateFormat === 'function';

// 유틸: 서버에서 오는 ISO 날짜를 YYYY-MM-DD 로 바꿔주는 함수
const toYYYYMMDD = (d) => {
  if (!d) return '';
  const dt = new Date(d);
  if (isNaN(dt)) return String(d).slice(0, 10);
  const yyyy = dt.getFullYear();
  const mm = String(dt.getMonth() + 1).padStart(2, '0');
  const dd = String(dt.getDate()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
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
      page: page.value.page,
      size: page.value.size
    };
    const res = await axios.get('/api/material', { params });
    // 서버 응답 구조에 따라 안전하게 처리
    const data = Array.isArray(res.data) ? res.data : (res.data?.data ?? res.data?.items ?? []);
    materials.value = data;
    page.value.totalElements = res.data?.page?.totalElements ?? materials.value.length;
    // 선택된 항목이 있으면 목록 갱신 후 다시 선택 유지
    if (selectedMaterial.value) {
      const found = materials.value.find((m) => (m.matId ?? m.MAT_ID) === (selectedMaterial.value.matId ?? selectedMaterial.value.MAT_ID));
      if (found) selectedMaterial.value = found;
      else handleUnselect();
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
      // detail에 안전하게 채워넣어 Vue 반응성 유지
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
      // 폼도 동기화 (선택시 바로 폼에 채워짐)
      Object.assign(form, detail);
    } else {
      // 비어있으면 초기화
      Object.keys(detail).forEach((k) => (detail[k] = ''));
      Object.keys(form).forEach((k) => (form[k] = ''));
    }
  } catch (e) {
    console.error('fetchDetail error', e);
    toast('error', '조회 실패', '자재 상세 조회 중 오류가 발생했습니다.');
  }
};

// CRUD: 등록
const addMaterial = async () => {
  // 간단 유효성
  if (!form.matName) return toast('warn', '등록 실패', '자재명을 입력하세요.');
  if (!form.matUnitPrice) {
    /* 단가 필수가 아니면 주석 처리 */
  }
  try {
    // matId는 서버에서 생성 (Mapper XML: next_code('MAT'))
    const payload = { ...form };
    delete payload.matId;
    const res = await axios.post('/api/material', payload);
    if (res.status === 200 || res.status === 201) {
      toast('success', '등록 성공', '자재가 등록되었습니다.');
      await fetchList();
      // 서버가 생성한 ID가 응답에 있으면 상세로 이동
      const newId = res.data?.matId ?? res.data?.MAT_ID ?? res.data?.employeeId ?? null;
      if (newId) {
        await fetchDetail(newId);
        mode.value = 'view';
        selectedMaterial.value = materials.value.find((m) => (m.matId ?? m.MAT_ID) === newId) ?? null;
      } else {
        // 그냥 초기화 폼
        Object.keys(form).forEach((k) => (form[k] = ''));
        mode.value = 'create';
      }
    }
  } catch (e) {
    console.error('addMaterial error', e);
    toast('error', '등록 실패', '자재 등록 중 오류가 발생했습니다.');
  }
};

// CRUD: 수정
const modifyMaterial = async () => {
  if (!detail.matId) return toast('warn', '수정 실패', '수정할 자재를 선택하세요.');
  try {
    const payload = { ...form };
    const res = await axios.put(`/api/material/${detail.matId}`, payload);
    if (res.status === 200) {
      toast('success', '수정 성공', '자재가 수정되었습니다.');
      await fetchList();
      await fetchDetail(detail.matId);
      mode.value = 'view';
    }
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
    // 초기화 / 목록 갱신
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
const handleEdit = () => {
  // 폼은 이미 detail에서 동기화되므로 모드 변경만
  mode.value = 'edit';
};
const handleResetForm = () => {
  Object.keys(form).forEach((k) => (form[k] = ''));
  // if viewing an item, keep details but clear form? We'll refill on fetchDetail
  if (mode.value === 'view' && detail.matId) Object.assign(form, detail);
};

// 검색 관련 (same look & behaviour as employee page)
const handleSearch = () => {
  // if editing, warn user
  if (mode.value === 'edit') {
    if (!confirm('현재 수정 중입니다. 조회하면 수정 중인 내용은 저장되지 않습니다. 계속하시겠습니까?')) return;
  }
  page.value.page = 1;
  selectedMaterial.value = null;
  Object.keys(detail).forEach((k) => (detail[k] = ''));
  Object.keys(form).forEach((k) => (form[k] = ''));
  mode.value = 'create';
  fetchList();
};
const handleReset = () => {
  Object.assign(searchParams, { matId: '', matName: '', matType: '', matStoreCond: '' });
  handleSearch();
};

onMounted(() => fetchList());
</script>

<template>
  <Fluid>
    <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />

    <SearchCard title="자재 검색" @search="handleSearch" @reset="handleReset">
      <div class="flex flex-wrap w-full">
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
          <InputGroup>
            <InputGroupAddon><i :class="iconBox" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchParams.matType" inputId="matType" />
              <label for="matType">자재유형</label>
            </IftaLabel>
          </InputGroup>
        </div>

        <div class="p-2 w-full md:w-1/4">
          <InputGroup>
            <InputGroupAddon><i :class="iconBox" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchParams.matStoreCond" inputId="matStoreCond" />
              <label for="matStoreCond">보관조건</label>
            </IftaLabel>
          </InputGroup>
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
              <!-- 버튼 집합: 상황에 따라 동작 -->
              <Btn v-if="mode === 'create'" icon="add" @click="addMaterial" outlined>등록</Btn>
              <Btn v-if="mode === 'edit'" icon="save" @click="modifyMaterial" outlined>저장</Btn>
              <Btn v-if="mode === 'view' && detail.matId" icon="edit" color="warn" @click="handleEdit" outlined>수정</Btn>
              <Btn v-if="mode !== 'create' && detail.matId" icon="delete" color="danger" @click="deleteMaterial" outlined>삭제</Btn>
              <Btn icon="refresh" color="secondary" @click="handleResetForm" outlined>초기화</Btn>
            </div>
          </div>

          <Divider />

          <div class="w-full flex flex-row mb-2 gap-2">
            <div class="flex-1 ml-6 flex flex-col justify-center gap-0">
              <div class="font-light text-xs flex items-center gap-4 text-gray-500">
                {{ detail.matId || (mode === 'create' ? '(신규)' : '') }}
              </div>
              <div class="font-semibold text-lg flex items-center gap-4">
                <InputText v-model="form.matName" placeholder="자재명" class="w-full" />
              </div>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4 mb-4">
            <div>
              <label class="text-sm block mb-1">자재유형</label>
              <InputText v-model="form.matType" class="w-full h-10" placeholder="자재유형" />
            </div>

            <div>
              <label class="text-sm block mb-1">보관조건</label>
              <InputText v-model="form.matStoreCond" class="w-full h-10" placeholder="보관조건" />
            </div>

            <div>
              <label class="text-sm block mb-1">단위</label>
              <InputText v-model="form.unit" class="w-full h-10" placeholder="단위" />
            </div>

            <div>
              <label class="text-sm block mb-1">규격</label>
              <InputText v-model="form.spec" class="w-full h-10" placeholder="규격" />
            </div>

            <div>
              <label class="text-sm block mb-1">단위환산</label>
              <InputText v-model="form.matUnitConv" class="w-full h-10" placeholder="단위환산" />
            </div>

            <div>
              <label class="text-sm block mb-1">리드타임</label>
              <InputText v-model="form.leadTime" class="w-full h-10" placeholder="리드타임" />
            </div>

            <div>
              <label class="text-sm block mb-1">안전재고</label>
              <InputText v-model="form.safeStock" class="w-full h-10" placeholder="안전재고" />
            </div>

            <div>
              <label class="text-sm block mb-1">상태</label>
              <InputText v-model="form.status" class="w-full h-10" placeholder="상태" />
            </div>

            <div>
              <label class="text-sm block mb-1">단가</label>
              <InputText v-model="form.matUnitPrice" class="w-full h-10" placeholder="단가" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </Fluid>
</template>

<style scoped>
/* 작은 커스터마이즈: 검색 input 기본 너비 제한 (사원 페이지 스타일과 유사) */
.SearchCard .flex > .p-2 {
  padding: 0.5rem;
}
</style>
