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

/* ---------- 검색 파라미터 ---------- */
const searchParams = reactive({
  prodId: '',
  prodName: '',
  status: '',
  unit: ''
});

/* ---------- 목록 / 선택 /상세 /폼 상태 ---------- */
const products = ref([]);
const selectedProduct = ref(null);

const productDetail = reactive({
  prodId: '',
  prodName: '',
  prodStoreCond: '',
  spec: '',
  unit: '',
  safeStock: '',
  status: '',
  exp: '',
  prodUnitPrice: ''
});

const productForm = reactive({
  prodId: '',
  prodName: '',
  prodStoreCond: '',
  spec: '',
  unit: '',
  safeStock: '',
  status: '',
  exp: '',
  prodUnitPrice: ''
});

const mode = ref('create'); // create | view | edit
const loading = ref(false);

/* ---------- 페이징 / 컬럼 ---------- */
const page = ref({ page: 1, size: 10, totalElements: 0 });
const columns = [
  { label: '제품코드', field: 'prodId', sortable: true },
  { label: '제품명', field: 'prodName', sortable: true }
];

/* ---------- API: 목록 ---------- */
const fetchProductList = async () => {
  loading.value = true;
  try {
    const params = {
      prodId: searchParams.prodId || undefined,
      prodName: searchParams.prodName || undefined,
      status: searchParams.status || undefined,
      unit: searchParams.unit || undefined,
      page: page.value.page,
      size: page.value.size
    };
    const res = await axios.get('/api/product', { params });
    // 안전하게 파싱: 서버가 배열을 바로 반환하거나 {data:[], page:...} 형태 모두 처리
    const payload = Array.isArray(res.data) ? res.data : (res.data?.data ?? res.data?.items ?? []);
    products.value = payload;
    page.value.totalElements = res.data?.page?.totalElements ?? products.value.length;

    // 선택 유지 (목록 갱신 시)
    if (selectedProduct.value) {
      const found = products.value.find((p) => (p.prodId ?? p.PROD_ID) === (selectedProduct.value.prodId ?? selectedProduct.value.PROD_ID));
      if (!found) handleUnselect();
    }
  } catch (e) {
    console.error('fetchProductList error', e);
    toast('error', '조회 실패', '제품 목록 조회 중 오류가 발생했습니다.');
  } finally {
    loading.value = false;
  }
};

/* ---------- API: 상세 ---------- */
const fetchProductDetail = async (id) => {
  if (!id) return;
  try {
    const res = await axios.get(`/api/product/${id}`);
    const data = Array.isArray(res.data) ? res.data[0] : res.data;
    if (data) {
      Object.assign(productDetail, {
        prodId: data.prodId ?? data.PROD_ID ?? '',
        prodName: data.prodName ?? data.PROD_NAME ?? '',
        prodStoreCond: data.prodStoreCond ?? data.PROD_STORE_COND ?? '',
        spec: data.spec ?? data.SPEC ?? '',
        unit: data.unit ?? data.UNIT ?? '',
        safeStock: data.safeStock ?? data.SAFE_STOCK ?? '',
        status: data.status ?? data.STATUS ?? '',
        exp: data.exp ?? data.EXP ?? '',
        prodUnitPrice: data.prodUnitPrice ?? data.PROD_UNIT_PRICE ?? ''
      });
      // 폼 동기화
      Object.assign(productForm, productDetail);
      selectedProduct.value = data;
      mode.value = 'view';
    } else {
      // 초기화
      Object.keys(productDetail).forEach((k) => (productDetail[k] = ''));
      Object.keys(productForm).forEach((k) => (productForm[k] = ''));
      selectedProduct.value = null;
      mode.value = 'view';
    }
  } catch (e) {
    console.error('fetchProductDetail error', e);
    toast('error', '조회 실패', '제품 상세 조회 중 오류가 발생했습니다.');
  }
};

/* ---------- CRUD ---------- */
const addProduct = async () => {
  if (!productForm.prodName) return toast('warn', '등록 실패', '제품명을 입력하세요.');
  try {
    const payload = { ...productForm };
    delete payload.prodId; // 서버에서 생성한다고 가정
    const res = await axios.post('/api/product', payload);
    toast('success', '등록 성공', '제품이 등록되었습니다.');
    await fetchProductList();
    // 서버가 새 id를 반환하면 상세로 이동
    const newId = res.data?.prodId ?? res.data?.PROD_ID ?? null;
    if (newId) {
      await fetchProductDetail(newId);
      mode.value = 'view';
      // 선택 갱신
      selectedProduct.value = products.value.find((p) => (p.prodId ?? p.PROD_ID) === newId) ?? null;
    } else {
      // 폼 리셋
      Object.keys(productForm).forEach((k) => (productForm[k] = ''));
      mode.value = 'create';
    }
  } catch (e) {
    console.error('addProduct error', e);
    toast('error', '등록 실패', '제품 등록 중 오류가 발생했습니다.');
  }
};

const modifyProduct = async () => {
  if (!productDetail.prodId) return toast('warn', '수정 실패', '수정할 제품을 선택하세요.');
  try {
    const payload = { ...productForm };
    const res = await axios.put(`/api/product/${productDetail.prodId}`, payload);
    toast('success', '수정 성공', '제품 정보가 수정되었습니다.');
    await fetchProductList();
    await fetchProductDetail(productDetail.prodId);
    mode.value = 'view';
  } catch (e) {
    console.error('modifyProduct error', e);
    toast('error', '수정 실패', '제품 수정 중 오류가 발생했습니다.');
  }
};

const deleteProduct = async () => {
  if (!productDetail.prodId) return toast('warn', '삭제 실패', '삭제할 제품을 선택하세요.');
  if (!confirm(`제품 [${productDetail.prodId}]를 삭제하시겠습니까?`)) return;
  try {
    await axios.delete(`/api/product/${productDetail.prodId}`);
    toast('success', '삭제 성공', '제품이 삭제되었습니다.');
    // 초기화 및 목록 갱신
    Object.keys(productDetail).forEach((k) => (productDetail[k] = ''));
    Object.keys(productForm).forEach((k) => (productForm[k] = ''));
    selectedProduct.value = null;
    mode.value = 'create';
    await fetchProductList();
  } catch (e) {
    console.error('deleteProduct error', e);
    toast('error', '삭제 실패', '제품 삭제 중 오류가 발생했습니다.');
  }
};

/* ---------- 이벤트 핸들러 / 유틸 ---------- */
const handleRowSelect = async (row) => {
  selectedProduct.value = row;
  const id = row.prodId ?? row.PROD_ID;
  await fetchProductDetail(id);
  mode.value = 'view';
};

const handleUnselect = () => {
  selectedProduct.value = null;
  Object.keys(productDetail).forEach((k) => (productDetail[k] = ''));
  Object.keys(productForm).forEach((k) => (productForm[k] = ''));
  mode.value = 'create';
};

const handleEdit = () => {
  // form은 이미 detail에서 동기화됨
  mode.value = 'edit';
};

const handleResetForm = () => {
  // view 상황이면 detail로, 아니면 빈 폼
  if (mode.value === 'view' && productDetail.prodId) Object.assign(productForm, productDetail);
  else Object.keys(productForm).forEach((k) => (productForm[k] = ''));
};

/* 검색/초기화 (employee page와 동일 동작) */
const handleSearch = () => {
  if (mode.value === 'edit') {
    if (!confirm('현재 수정 중입니다. 조회하면 수정중인 내용이 사라집니다. 계속하시겠습니까?')) return;
  }
  page.value.page = 1;
  selectedProduct.value = null;
  Object.keys(productDetail).forEach((k) => (productDetail[k] = ''));
  Object.keys(productForm).forEach((k) => (productForm[k] = ''));
  mode.value = 'create';
  fetchProductList();
};

const handleReset = () => {
  Object.assign(searchParams, { prodId: '', prodName: '', prodStoreCond: '', status: '', unit: '', exp: '', prodUnitPrice: '' });
  handleSearch();
};

onMounted(() => fetchProductList());
</script>

<template>
  <Fluid>
    <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />

    <SearchCard title="제품 검색" @search="handleSearch" @reset="handleReset">
      <div class="flex flex-wrap w-full">
        <div class="p-2 w-full md:w-1/4">
          <InputGroup>
            <InputGroupAddon><i :class="iconBox" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchParams.prodName" inputId="prodName" />
              <label for="prodName">제품명</label>
            </IftaLabel>
          </InputGroup>
        </div>

        <div class="p-2 w-full md:w-1/4">
          <InputGroup>
            <InputGroupAddon><i :class="iconId" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchParams.prodId" inputId="prodId" />
              <label for="prodId">제품코드</label>
            </IftaLabel>
          </InputGroup>
        </div>

        <div class="p-2 w-full md:w-1/4">
          <InputGroup>
            <InputGroupAddon><i :class="iconBox" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchParams.status" inputId="status" />
              <label for="status">상태</label>
            </IftaLabel>
          </InputGroup>
        </div>

        <div class="p-2 w-full md:w-1/4">
          <InputGroup>
            <InputGroupAddon><i :class="iconBox" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchParams.unit" inputId="unit" />
              <label for="unit">단위</label>
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
              제품 목록
            </div>
            <div class="text-sm text-gray-400">
              총 <span class="font-semibold text-sm text-gray-700">{{ page.totalElements || products.length }}</span> 건
            </div>
          </div>

          <Divider />

          <DTable :columns="columns" :data="products" :page="page" :loading="loading" dataKey="prodId" v-model:selected="selectedProduct" @row-select="handleRowSelect" @row-unselect="handleUnselect" />
        </div>
      </div>

      <!-- 상세 / 폼 -->
      <div class="w-full xl:w-7/12">
        <div class="card flex flex-col">
          <div class="flex items-center justify-between h-10">
            <div class="font-semibold text-xl flex items-center gap-4">
              <span :class="[mode === 'create' ? iconAdd : mode === 'edit' ? iconEdit : iconInfo, 'text-xl']"></span>
              {{ mode === 'create' ? '신규 제품 등록' : mode === 'edit' ? '제품 정보 수정' : '제품 상세 정보' }}
            </div>

            <div class="flex gap-2">
              <Btn v-if="mode === 'create'" icon="add" @click="addProduct" outlined>등록</Btn>
              <Btn v-if="mode === 'edit'" icon="save" @click="modifyProduct" outlined>저장</Btn>
              <Btn v-if="mode === 'view' && productDetail.prodId" icon="edit" color="warn" @click="handleEdit" outlined>수정</Btn>
              <Btn v-if="mode !== 'create' && productDetail.prodId" icon="delete" color="danger" @click="deleteProduct" outlined>삭제</Btn>
              <Btn icon="refresh" color="secondary" @click="handleResetForm" outlined>초기화</Btn>
            </div>
          </div>

          <Divider />

          <div class="w-full flex flex-row mb-2 gap-2">
            <div class="flex-1 ml-6 flex flex-col justify-center gap-0">
              <div class="font-light text-xs flex items-center gap-4 text-gray-500">
                {{ productDetail.prodId || (mode === 'create' ? '(신규)' : '') }}
              </div>
              <div class="font-semibold text-lg flex items-center gap-4">
                <InputText v-model="productForm.prodName" inputId="formName" class="w-full" placeholder="제품명 입력" />
              </div>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4 mb-4">
            <div>
              <label class="text-sm block mb-1">규격</label>
              <InputText v-model="productForm.spec" class="w-full h-10" placeholder="규격" />
            </div>

            <div>
              <label class="text-sm block mb-1">단위</label>
              <InputText v-model="productForm.unit" class="w-full h-10" placeholder="단위" />
            </div>

            <div>
              <label class="text-sm block mb-1">보관조건</label>
              <InputText v-model="productForm.prodStoreCond" class="w-full h-10" placeholder="보관조건" />
            </div>

            <div>
              <label class="text-sm block mb-1">안전재고</label>
              <InputText v-model="productForm.safeStock" class="w-full h-10" placeholder="안전재고" />
            </div>

            <div>
              <label class="text-sm block mb-1">상태</label>
              <InputText v-model="productForm.status" class="w-full h-10" placeholder="상태" />
            </div>

            <div>
              <label class="text-sm block mb-1">만료일</label>
              <InputText v-model="productForm.exp" class="w-full h-10" placeholder="만료일" />
            </div>

            <div>
              <label class="text-sm block mb-1">단가</label>
              <InputText v-model="productForm.prodUnitPrice" class="w-full h-10" placeholder="단가" />
            </div>

            <div>
              <label class="text-sm block mb-1">제품코드</label>
              <InputText v-model="productForm.prodId" class="w-full h-10" :disabled="true" placeholder="자동 생성" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </Fluid>
</template>

<style scoped>
/* 검색 박스 폭을 줄여서 한 줄에 들어가도록 조정 */
.SearchCard .flex > .p-2 {
  padding: 0.5rem;
}
/* 기존 스타일 재사용 */
.container {
  max-width: 1100px;
  margin: 0 auto;
}
.btn {
  padding: 6px 12px;
  background: #2563eb;
  color: white;
  border-radius: 4px;
}
.btn-secondary {
  padding: 6px 12px;
  background: #e5e7eb;
  color: #111827;
  border-radius: 4px;
}
.btn-danger {
  padding: 6px 12px;
  background: #ff0000;
  color: #ffffff;
  border-radius: 4px;
}
table td,
table th {
  border-bottom: 1px solid #e5e7eb;
  padding: 6px 4px;
}
</style>
