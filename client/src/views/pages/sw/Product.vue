<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import InputText from 'primevue/inputtext';
import { useIcon } from '@/composables/useIcon';
import { useAppToast } from '@/composables/useAppToast';
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
    const { data } = await axios.get(`http://localhost:8080/api/product/autocomplete?keyword=${keyword}`);
    suggestions.value = data;
  } catch (e) {
    suggestions.value = [];
  }
};

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
  prodId: '',
  prodName: '',
  status: '', // 전체/사용/미사용
  unit: ''
});

// 목록 / 선택 / 상세 / 폼 상태
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

const productForm = reactive({ ...productDetail });
const mode = ref('create');
const loading = ref(false);

// 페이지네이션 (프론트 전용)
const page = ref({ page: 1, size: 10, totalElements: 0 });
const columns = [
  { label: '제품코드', field: 'prodId', sortable: true },
  { label: '제품명', field: 'prodName', sortable: true },
  { label: '상태', field: 'status' }
];

// 프론트 페이징용 slice
const pagedProducts = computed(() => {
  const start = (page.value.page - 1) * page.value.size;
  const end = start + page.value.size;
  return products.value.slice(start, end);
});

const maxPages = computed(() => Math.max(1, Math.ceil((page.value.totalElements || 0) / page.value.size)));

// 페이지 변경
const handlePageChange = ({ page: newPage, size: newSize }) => {
  page.value.page = newPage;
  page.value.size = newSize;
  if (page.value.page > maxPages.value) page.value.page = maxPages.value;
};

// API: 목록 (서버 페이징 X)
const fetchProductList = async () => {
  loading.value = true;
  try {
    const params = {
      prodId: searchParams.prodId || undefined,
      prodName: searchParams.prodName || undefined,
      status: searchParams.status || undefined,
      unit: searchParams.unit || undefined
    };
    const res = await axios.get('/api/product', { params });
    const data = Array.isArray(res.data) ? res.data : (res.data?.data ?? res.data?.items ?? []);
    products.value = data;
    page.value.totalElements = products.value.length;

    // 현재 페이지가 비면 앞쪽 페이지로 보정
    if (products.value.length > 0 && pagedProducts.value.length === 0) {
      page.value.page = maxPages.value;
    }
  } catch (e) {
    console.error('fetchProductList error', e);
    toast('error', '조회 실패', '제품 목록 조회 중 오류가 발생했습니다.');
  } finally {
    loading.value = false;
  }
};

// API: 상세
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
      Object.assign(productForm, productDetail);
      selectedProduct.value = data;
      mode.value = 'view';
    } else {
      Object.keys(productDetail).forEach((k) => (productDetail[k] = ''));
      Object.keys(productForm).forEach((k) => (productForm[k] = ''));
      selectedProduct.value = null;
      mode.value = 'create';
    }
  } catch (e) {
    console.error('fetchProductDetail error', e);
    toast('error', '조회 실패', '제품 상세 조회 중 오류가 발생했습니다.');
  }
};

// CRUD
const addProduct = async () => {
  if (!productForm.prodName) return toast('warn', '등록 실패', '제품명을 입력하세요.');
  try {
    const payload = { ...productForm };
    delete payload.prodId;
    const res = await axios.post('/api/product', payload);
    toast('success', '등록 성공', '제품이 등록되었습니다.');
    await fetchProductList();
    const newId = res.data?.prodId ?? res.data?.PROD_ID ?? null;
    if (newId) {
      await fetchProductDetail(newId);
      mode.value = 'view';
      selectedProduct.value = products.value.find((p) => (p.prodId ?? p.PROD_ID) === newId) ?? null;
    } else {
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
    await axios.put(`/api/product/${productDetail.prodId}`, productForm);
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
    Object.keys(productDetail).forEach((k) => (productDetail[k] = ''));
    Object.keys(productForm).forEach((k) => (productForm[k] = ''));
    selectedProduct.value = null;
    mode.value = 'create';
    await fetchProductList();
  } catch (e) {
    console.error('deleteProduct error', e);
    if (e.response?.status === 409) {
      toast('warn', '삭제 불가', e.response.data?.message || '다른 테이블에서 사용 중인 제품입니다.');
    } else {
      toast('error', '삭제 실패', '제품 삭제 중 오류가 발생했습니다.');
    }
  }
};

// 이벤트 핸들러
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
const handleEdit = () => (mode.value = 'edit');
const handleResetForm = () => {
  if (mode.value === 'view' && productDetail.prodId) Object.assign(productForm, productDetail);
  else Object.keys(productForm).forEach((k) => (productForm[k] = ''));
};

// 검색 / 초기화
const handleSearch = () => {
  if (mode.value === 'edit') {
    if (!confirm('현재 수정 중입니다. 조회하면 수정 중인 내용이 사라집니다. 계속하시겠습니까?')) return;
  }
  page.value.page = 1;
  selectedProduct.value = null;
  Object.keys(productDetail).forEach((k) => (productDetail[k] = ''));
  Object.keys(productForm).forEach((k) => (productForm[k] = ''));
  mode.value = 'create';
  fetchProductList();
};
const handleReset = () => {
  Object.assign(searchParams, { prodId: '', prodName: '', status: '', unit: '', exp: '', prodUnitPrice: '' });
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
              <AutoComplete v-model="searchParams.prodName" inputId="prodName" :suggestions="suggestions" @complete="searchProdSuggestions" placeholder="제품명 입력" />
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
              제품 목록
            </div>
            <div class="text-sm text-gray-400">
              총 <span class="font-semibold text-sm text-gray-700">{{ page.totalElements }}</span> 건
            </div>
          </div>
          <Divider />
          <DTable :columns="columns" :data="pagedProducts" :page="page" :loading="loading" dataKey="prodId" v-model:selected="selectedProduct" @row-select="handleRowSelect" @row-unselect="handleUnselect" @page-change="handlePageChange" />
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
            <div><label class="text-sm block mb-1">규격</label><InputText v-model="productForm.spec" class="w-full h-10" /></div>
            <div><label class="text-sm block mb-1">단위</label><InputText v-model="productForm.unit" class="w-full h-10" /></div>
            <div><label class="text-sm block mb-1">보관조건</label><InputText v-model="productForm.prodStoreCond" class="w-full h-10" /></div>
            <div>
              <label class="text-sm block mb-1">상태</label>
              <div class="flex gap-2 items-center">
                <label><input type="radio" value="Y" v-model="productForm.status" /> 사용</label>
                <label><input type="radio" value="N" v-model="productForm.status" /> 미사용</label>
              </div>
            </div>
            <div><label class="text-sm block mb-1">만료일</label><InputText :value="productForm.exp" @input="(e) => (productForm.exp = e.target.value.replace(/[^0-9]/g, ''))" class="w-full h-10" placeholder="숫자만 입력" /></div>
            <div>
              <label class="text-sm block mb-1">단가</label>
              <InputText
                :value="productForm.prodUnitPrice"
                @input="
                  (e) => {
                    // 숫자만 남기기
                    let val = e.target.value.replace(/[^0-9]/g, '');
                    // 3자리마다 콤마
                    val = val.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
                    productForm.prodUnitPrice = val;
                  }
                "
                class="w-full h-10"
                placeholder="숫자만 입력"
              />
            </div>
            <div><label class="text-sm block mb-1">제품코드</label><InputText v-model="productForm.prodId" class="w-full h-10" disabled /></div>
          </div>
        </div>
      </div>
    </div>
  </Fluid>
</template>
