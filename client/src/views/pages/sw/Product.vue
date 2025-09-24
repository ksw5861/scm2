<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import InputText from 'primevue/inputtext';

const searchProdName = ref('');
const searchStatus = ref('');
const searchUnit = ref('');
const searchProdId = ref('');
const prodList = ref([]);
const selectedProduct = ref(null);

/* form + mode: view | edit | new */
const productForm = ref({
  prodId: '',
  prodName: '',
  safeStock: '',
  status: '',
  spec: '',
  unit: ''
});
const mode = ref('view'); // default

/* 목록 호출 */
const fetchProductList = async () => {
  try {
    const res = await axios.get('/api/product', {
      params: {
        prodName: searchProdName.value || undefined,
        status: searchStatus.value || undefined,
        unit: searchUnit.value || undefined,
        prodId: searchProdId.value || undefined
      }
    });
    prodList.value = Array.isArray(res.data) ? res.data.map((i) => ({ ...i })) : [];
  } catch (e) {
    console.error('fetchProductList error', e);
    alert('제품 목록 불러오기 실패');
  }
};

/* 상세 호출 */
const fetchProductDetail = async (prodId) => {
  if (!prodId) return;
  try {
    const res = await axios.get(`/api/product/${prodId}`);
    const data = Array.isArray(res.data) ? res.data[0] : res.data;
    if (data) {
      selectedProduct.value = data;
      productForm.value = {
        prodId: data.prodId ?? data.PROD_ID ?? '',
        prodName: data.prodName ?? data.PROD_NAME ?? '',
        safeStock: data.safeStock ?? data.SAFE_STOCK ?? '',
        status: data.status ?? data.STATUS ?? '',
        spec: data.spec ?? data.SPEC ?? '',
        unit: data.unit ?? data.UNIT ?? ''
      };
      mode.value = 'view';
    } else {
      selectedProduct.value = null;
      mode.value = 'view';
    }
  } catch (e) {
    console.error('fetchProductDetail error', e);
    alert('제품 상세 불러오기 실패');
  }
};

const onProductRowClick = (row) => {
  fetchProductDetail(row.prodId ?? row.PROD_ID);
};

const onProductSearch = () => fetchProductList();
const onProductReset = () => {
  searchProdId.value = '';
  searchProdName.value = '';
  searchStatus.value = '';
  searchUnit.value = '';
  fetchProductList();
};

/* 신규 폼 초기화 */
const newProduct = () => {
  productForm.value = {
    prodId: '',
    prodName: '',
    safeStock: '',
    status: '',
    spec: '',
    unit: ''
  };
  selectedProduct.value = null;
  mode.value = 'new';
};

/* 편집 모드 진입 */
const editProduct = () => {
  if (!selectedProduct.value) return alert('수정할 항목을 선택하세요.');
  mode.value = 'edit';
};

/* 저장 (등록/수정) */
const saveProduct = async () => {
  if (mode.value === 'new') {
    if (!productForm.value.prodName) return alert('제품명을 입력하세요.');
  } else {
    if (!productForm.value.prodId || !productForm.value.prodName) return alert('제품코드와 제품명을 입력하세요.');
  }

  const payload = { ...productForm.value };
  if (mode.value === 'new') delete payload.prodId;

  try {
    if (mode.value === 'new') {
      const res = await axios.post('/api/product', payload);
      let newId = null;
      if (res && res.data) {
        if (typeof res.data === 'string') newId = res.data;
        else newId = res.data.prodId ?? res.data.PROD_ID ?? null;
      }
      await fetchProductList();
      if (newId) {
        await fetchProductDetail(newId);
        alert('등록되었습니다. (코드: ' + newId + ')');
      } else {
        mode.value = 'view';
        alert('등록되었습니다.');
      }
    } else if (mode.value === 'edit') {
      await axios.put(`/api/product/${productForm.value.prodId}`, payload);
      await fetchProductList();
      await fetchProductDetail(productForm.value.prodId);
      alert('수정되었습니다.');
    }
  } catch (e) {
    console.error('saveProduct error', e);
    alert('저장에 실패했습니다. 콘솔 확인');
  } finally {
    mode.value = 'view';
  }
};

/* 삭제 */
const deleteProduct = async () => {
  const prodId = selectedProduct.value?.prodId ?? selectedProduct.value?.PROD_ID ?? productForm.value.prodId;
  if (!prodId) return alert('삭제할 항목을 선택하세요.');
  if (!confirm(`제품 [${prodId}]를 삭제하시겠습니까?`)) return;

  try {
    await axios.delete(`/api/product/${prodId}`);
    alert('삭제되었습니다.');
    selectedProduct.value = null;
    productForm.value = {
      prodId: '',
      prodName: '',
      safeStock: '',
      status: '',
      spec: '',
      unit: ''
    };
    mode.value = 'view';
    await fetchProductList();
  } catch (e) {
    console.error('deleteProduct error', e);
    alert('삭제에 실패했습니다. 콘솔 확인');
  }
};

/* 취소 */
const cancelProductEdit = () => {
  if (mode.value === 'new') {
    productForm.value = {
      prodId: '',
      prodName: '',
      safeStock: '',
      status: '',
      spec: '',
      unit: ''
    };
    mode.value = 'view';
    selectedProduct.value = null;
  } else if (mode.value === 'edit') {
    if (selectedProduct.value) fetchProductDetail(selectedProduct.value.prodId ?? selectedProduct.value.PROD_ID);
    mode.value = 'view';
  }
};

onMounted(() => {
  fetchProductList();
});
</script>

<template>
  <div class="container p-4">
    <h2 class="mb-3">제품 관리</h2>

    <div class="mb-4 flex gap-2 items-center">
      <InputText v-model="searchProdName" placeholder="제품명" class="h-10" />
      <InputText v-model="searchStatus" placeholder="상태" class="h-10" />
      <InputText v-model="searchUnit" placeholder="단위" class="h-10" />
      <InputText v-model="searchProdId" placeholder="제품코드" class="h-10" />
      <button class="btn" @click="onProductSearch">조회</button>
      <button class="btn-secondary" @click="onProductReset">초기화</button>
      <div class="ml-auto flex gap-2">
        <button class="btn" @click="newProduct">신규</button>
      </div>
    </div>

    <div class="grid grid-cols-2 gap-6">
      <!-- 목록 -->
      <div>
        <h3>제품 목록</h3>
        <table class="w-full border-collapse">
          <thead>
            <tr class="text-left border-b">
              <th class="py-2">PROD_ID</th>
              <th class="py-2">제품명</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in prodList" :key="row.prodId ?? row.PROD_ID" @click="onProductRowClick(row)" class="cursor-pointer hover:bg-gray-100">
              <td class="py-2">{{ row.prodId ?? row.PROD_ID }}</td>
              <td class="py-2">{{ row.prodName ?? row.PROD_NAME }}</td>
            </tr>
          </tbody>
        </table>
        <div v-if="!prodList.length" class="text-sm text-gray-500 mt-2">데이터가 없습니다.</div>
      </div>

      <!-- 상세 -->
      <div>
        <h3>상세 / 편집</h3>

        <!-- 버튼 -->
        <div class="flex justify-end gap-2 mb-2">
          <button v-if="mode === 'view' && selectedProduct" class="btn" @click="editProduct">수정</button>
          <button v-if="mode === 'view' && selectedProduct" class="btn-danger" @click="deleteProduct">삭제</button>

          <button v-if="mode === 'edit' || mode === 'new'" class="btn" @click="saveProduct">저장</button>
          <button v-if="mode === 'edit' || mode === 'new'" class="btn-secondary" @click="cancelProductEdit">취소</button>
        </div>

        <!-- form -->
        <div class="grid gap-2">
          <div>
            <label class="text-sm block mb-1">제품코드</label>
            <InputText v-model="productForm.prodId" class="w-full h-10" :disabled="true" placeholder="제품코드는 자동 생성됩니다" />
          </div>

          <div>
            <label class="text-sm block mb-1">제품명</label>
            <InputText v-model="productForm.prodName" class="w-full h-10" placeholder="제품명 (필수)" />
          </div>

          <div>
            <label class="text-sm block mb-1">규격</label>
            <InputText v-model="productForm.spec" class="w-full h-10" placeholder="규격" />
          </div>

          <div>
            <label class="text-sm block mb-1">단위</label>
            <InputText v-model="productForm.unit" class="w-full h-10" placeholder="단위" />
          </div>

          <div>
            <label class="text-sm block mb-1">안전재고</label>
            <InputText v-model="productForm.safeStock" class="w-full h-10" placeholder="안전재고" />
          </div>

          <div>
            <label class="text-sm block mb-1">상태</label>
            <InputText v-model="productForm.status" class="w-full h-10" placeholder="상태" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
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
