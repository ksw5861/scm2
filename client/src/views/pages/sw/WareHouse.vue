<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import InputText from 'primevue/inputtext';
import AutoComplete from 'primevue/autocomplete';
import { useIcon } from '@/composables/useIcon';
import { useAppToast } from '@/composables/useAppToast';

const route = useRoute();
const { toast } = useAppToast();

const handlePageChange = ({ page: newPage, size: newSize }) => {
  page.value.page = newPage;
  page.value.size = newSize;
};

// 아이콘
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

/* 검색 파라미터 */
const searchParams = reactive({
  whId: '',
  whName: '',
  status: ''
});

/* 자동완성 */
const suggestions = ref([]);
const searchWhSuggestions = async (event) => {
  const keyword = (typeof event === 'string' ? event : event.query)?.trim();
  if (!keyword) {
    suggestions.value = [];
    return;
  }

  try {
    const { data } = await axios.get(`http://13.124.12.49/api/warehouse1/autocomplete?keyword=${keyword}`);
    suggestions.value = data;
  } catch {
    suggestions.value = [];
  }
};

/* 목록 / 선택 / 상세 / 폼 */
const warehouses = ref([]);
const selected = ref(null);

const detail = reactive({
  whId: '',
  whName: '',
  whAddress: '',
  whOwner: '',
  ownerTel: '',
  status: ''
});
const form = reactive({ ...detail });

const mode = ref('create'); // create | view | edit
const loading = ref(false);

/* 프론트 페이징 */
const page = ref({ page: 1, size: 6, totalElements: 0 });
const pagedWarehouses = computed(() => {
  const start = (page.value.page - 1) * page.value.size;
  const end = start + page.value.size;
  return warehouses.value.slice(start, end);
});
const maxPages = computed(() => Math.max(1, Math.ceil((page.value.totalElements || 0) / page.value.size)));
watch([() => page.value.page, () => page.value.size, () => page.value.totalElements], () => {
  if (page.value.page > maxPages.value) page.value.page = maxPages.value;
});

/* 테이블 컬럼 */
const columns = [
  { label: '창고코드', field: 'whId', sortable: true },
  { label: '창고명', field: 'whName', sortable: true },
  { label: '상태', field: 'status' }
];

/* API: 목록 */
const fetchList = async () => {
  loading.value = true;
  try {
    const params = {
      whId: searchParams.whId || undefined,
      whName: searchParams.whName || undefined,
      status: searchParams.status || undefined
    };
    const res = await axios.get('/api/warehouse1', { params });
    const data = Array.isArray(res.data) ? res.data : (res.data?.data ?? res.data?.items ?? []);
    warehouses.value = data;
    page.value.totalElements = warehouses.value.length;

    if (selected.value) {
      const found = warehouses.value.find((w) => (w.whId ?? w.WH_ID) === (selected.value.whId ?? selected.value.WH_ID));
      if (!found) handleUnselect();
    }

    if (warehouses.value.length > 0 && pagedWarehouses.value.length === 0) {
      page.value.page = maxPages.value;
    }
  } catch (e) {
    console.error('fetchList error', e);
    toast('error', '조회 실패', '창고 목록 조회 중 오류가 발생했습니다.');
  } finally {
    loading.value = false;
  }
};

/* API: 상세 */
const fetchDetail = async (id) => {
  if (!id) return;
  try {
    const res = await axios.get(`/api/warehouse1/${id}`);
    const data = Array.isArray(res.data) ? res.data[0] : res.data;
    if (data) {
      Object.assign(detail, {
        whId: data.whId ?? data.WH_ID ?? '',
        whName: data.whName ?? data.WH_NAME ?? '',
        whAddress: data.whAddress ?? data.WH_ADDRESS ?? '',
        whOwner: data.whOwner ?? data.WH_OWNER ?? '',
        ownerTel: data.ownerTel ?? data.OWNER_TEL ?? '',
        status: data.status ?? data.STATUS ?? ''
      });
      Object.assign(form, detail);
      selected.value = data;
      mode.value = 'view';
    } else {
      handleUnselect();
    }
  } catch (e) {
    console.error('fetchDetail error', e);
    toast('error', '조회 실패', '창고 상세 조회 중 오류가 발생했습니다.');
  }
};

/* CRUD */
const addWarehouse = async () => {
  if (!form.whName) return toast('warn', '등록 실패', '창고명을 입력하세요.');
  try {
    const payload = { ...form };
    delete payload.whId;
    const res = await axios.post('/api/warehouse1', payload);
    toast('success', '등록 성공', '창고가 등록되었습니다.');
    await fetchList();
    const newId = res.data?.whId ?? res.data?.WH_ID ?? null;
    if (newId) await fetchDetail(newId);
  } catch (e) {
    console.error('addWarehouse error', e);
    toast('error', '등록 실패', '창고 등록 중 오류가 발생했습니다.');
  }
};

const modifyWarehouse = async () => {
  if (!detail.whId) return toast('warn', '수정 실패', '수정할 창고를 선택하세요.');
  try {
    const payload = { ...form };
    await axios.put(`/api/warehouse1/${detail.whId}`, payload);
    toast('success', '수정 성공', '창고 정보가 수정되었습니다.');
    await fetchList();
    await fetchDetail(detail.whId);
    mode.value = 'view';
  } catch (e) {
    console.error('modifyWarehouse error', e);
    toast('error', '수정 실패', '창고 수정 중 오류가 발생했습니다.');
  }
};

const deleteWarehouse = async () => {
  if (!detail.whId) return toast('warn', '삭제 실패', '삭제할 창고를 선택하세요.');
  if (!confirm(`창고 [${detail.whId}]를 삭제하시겠습니까?`)) return;
  try {
    await axios.delete(`/api/warehouse1/${detail.whId}`);
    toast('success', '삭제 성공', '창고가 삭제되었습니다.');
    handleUnselect();
    await fetchList();
  } catch (e) {
    console.error('deleteWarehouse error', e);
    toast('error', '삭제 실패', '창고 삭제 중 오류가 발생했습니다.');
  }
};

/* 이벤트 */
const handleRowSelect = async (row) => {
  selected.value = row;
  await fetchDetail(row.whId ?? row.WH_ID);
};
const handleUnselect = () => {
  selected.value = null;
  Object.keys(detail).forEach((k) => (detail[k] = ''));
  Object.keys(form).forEach((k) => (form[k] = ''));
  mode.value = 'create';
};
const handleEdit = () => (mode.value = 'edit');
const handleResetForm = () => {
  if (mode.value === 'view' && detail.whId) Object.assign(form, detail);
  else Object.keys(form).forEach((k) => (form[k] = ''));
};

/* 검색/초기화 */
const handleSearch = () => {
  page.value.page = 1;
  handleUnselect();
  fetchList();
};
const handleReset = () => {
  Object.assign(searchParams, { whId: '', whName: '', status: '' });
  handleSearch();
};

onMounted(() => fetchList());
</script>

<template>
  <Fluid>
    <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />

    <SearchCard title="창고 검색" @search="handleSearch" @reset="handleReset">
      <div class="flex flex-wrap w-full">
        <div class="p-2 w-full md:w-1/4">
          <InputGroup>
            <InputGroupAddon><i :class="iconId" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchParams.whId" inputId="whId" />
              <label for="whId">창고코드</label>
            </IftaLabel>
          </InputGroup>
        </div>

        <div class="p-2 w-full md:w-1/4">
          <InputGroup>
            <InputGroupAddon><i :class="iconBox" /></InputGroupAddon>
            <IftaLabel>
              <AutoComplete v-model="searchParams.whName" inputId="whName" :suggestions="suggestions" @complete="searchWhSuggestions" placeholder="창고명 입력" />
              <label for="whName">창고명</label>
            </IftaLabel>
          </InputGroup>
        </div>

        <div class="p-2 w-full md:w-1/4">
          <label class="block text-sm mb-1">상태</label>
          <div class="flex gap-3 items-center">
            <label><input type="radio" v-model="searchParams.status" value="" /> 전체</label>
            <label><input type="radio" v-model="searchParams.status" value="정상" /> 정상</label>
            <label><input type="radio" v-model="searchParams.status" value="점검중" /> 점검중</label>
            <label><input type="radio" v-model="searchParams.status" value="폐쇄" /> 폐쇄</label>
          </div>
        </div>
      </div>
    </SearchCard>

    <div class="flex flex-col md:flex-row w-full gap-4 mt-4">
      <!-- 목록 -->
      <div class="w-full xl:w-5/12">
        <div class="card flex flex-col">
          <div class="font-semibold text-xl flex items-center justify-between gap-4 h-10">
            <div class="flex items-center gap-4"><span :class="[iconList, 'text-xl']"></span> 창고 목록</div>
            <div class="text-sm text-gray-400">
              총 <span class="font-semibold text-sm text-gray-700">{{ page.totalElements }}</span> 건
            </div>
          </div>
          <Divider />
          <DTable :columns="columns" :data="pagedWarehouses" :page="page" :loading="loading" dataKey="whId" v-model:selected="selected" @row-select="handleRowSelect" @row-unselect="handleUnselect" @page-change="handlePageChange" />
        </div>
      </div>

      <!-- 상세 / 폼 -->
      <div class="w-full xl:w-7/12">
        <div class="card flex flex-col">
          <div class="flex items-center justify-between h-10">
            <div class="font-semibold text-xl flex items-center gap-4">
              <span :class="[mode === 'create' ? iconAdd : mode === 'edit' ? iconEdit : iconInfo, 'text-xl']"></span>
              {{ mode === 'create' ? '신규 창고 등록' : mode === 'edit' ? '창고 정보 수정' : '창고 상세 정보' }}
            </div>

            <div class="flex gap-2">
              <Btn v-if="mode === 'create'" icon="add" @click="addWarehouse" outlined>등록</Btn>
              <Btn v-if="mode === 'edit'" icon="save" @click="modifyWarehouse" outlined>저장</Btn>
              <Btn v-if="mode === 'view' && detail.whId" icon="edit" color="warn" @click="handleEdit" outlined>수정</Btn>
              <Btn v-if="mode !== 'create' && detail.whId" icon="delete" color="danger" @click="deleteWarehouse" outlined>삭제</Btn>
              <Btn icon="refresh" color="secondary" @click="handleResetForm" outlined>초기화</Btn>
            </div>
          </div>

          <Divider />

          <div class="grid grid-cols-2 gap-4 mb-4">
            <div>
              <label class="text-sm block mb-1">창고명</label>
              <InputText v-model="form.whName" class="w-full h-10" placeholder="창고명 입력" />
            </div>

            <div>
              <label class="text-sm block mb-1">창고주소</label>
              <InputText v-model="form.whAddress" class="w-full h-10" placeholder="주소 입력" />
            </div>

            <div>
              <label class="text-sm block mb-1">담당자</label>
              <InputText v-model="form.whOwner" class="w-full h-10" placeholder="담당자 이름" />
            </div>

            <div>
              <label class="text-sm block mb-1">전화번호</label>
              <InputText v-model="form.ownerTel" class="w-full h-10" placeholder="ex) 010-0000-0000" />
            </div>

            <div>
              <label class="text-sm block mb-1">상태</label>
              <div class="flex gap-4 items-center">
                <label><input type="radio" value="정상" v-model="form.status" /> 정상</label>
                <label><input type="radio" value="점검중" v-model="form.status" /> 점검중</label>
                <label><input type="radio" value="폐쇄" v-model="form.status" /> 폐쇄</label>
              </div>
            </div>

            <div>
              <label class="text-sm block mb-1">창고코드</label>
              <InputText v-model="form.whId" class="w-full h-10" disabled placeholder="자동 생성" />
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
input[type='radio'] {
  transform: translateY(1px);
}
</style>
