<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import InputText from 'primevue/inputtext';

const searchName = ref('');
const searchMatType = ref('');
const searchMatStoreCond = ref('');
const searchMatId = ref('');
const matList = ref([]);
const selected = ref(null);

const form = ref({
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
const mode = ref('view'); // default

const fetchList = async () => {
  try {
    const res = await axios.get('/api/material', {
      params: {
        matName: searchName.value || undefined,
        matType: searchMatType.value || undefined,
        matStoreCond: searchMatStoreCond.value || undefined,
        matId: searchMatId.value || undefined
      }
    });
    matList.value = Array.isArray(res.data) ? res.data : (res.data?.items ?? []);
  } catch (e) {
    console.error('fetchList error', e);
    alert('자재 목록을 불러오는데 실패했습니다.');
  }
};

const fetchDetail = async (matId) => {
  if (!matId) return;
  try {
    const res = await axios.get(`/api/material/${matId}`);
    const data = Array.isArray(res.data) ? res.data[0] : res.data;
    if (data) {
      selected.value = data;
      form.value = {
        matId: data.matId ?? data.MAT_ID ?? '',
        matName: data.matName ?? data.MAT_NAME ?? '',
        matType: data.matType ?? data.MAT_TYPE ?? '',
        spec: data.spec ?? data.SPEC ?? '',
        unit: data.unit ?? data.UNIT ?? '',
        matStoreCond: data.matStoreCond ?? data.MAT_STORE_COND ?? '',
        matUnitConv: data.matUnitConv ?? data.MAT_UNIT_CONV ?? '',
        leadTime: data.leadTime ?? data.LEAD_TIME ?? null,
        matUnitPrice: data.matUnitPrice ?? data.MAT_UNIT_PRICE ?? '',
        safeStock: data.safeStock ?? data.SAFE_STOCK ?? '',
        status: data.status ?? data.STATUS ?? ''
      };
      mode.value = 'view';
    } else {
      selected.value = null;
      mode.value = 'view';
    }
  } catch (e) {
    console.error('fetchDetail error', e);
    alert('상세 정보를 불러오는데 실패했습니다.');
  }
};

const onRowClick = (row) => fetchDetail(row.matId ?? row.MAT_ID);
const onSearch = () => fetchList();
const onReset = () => {
  searchMatId.value = '';
  searchMatType.value = '';
  searchMatStoreCond.value = '';
  searchName.value = '';
  fetchList();
};

const newMaterial = () => {
  form.value = {
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
  };
  selected.value = null;
  mode.value = 'new';
};

const editMaterial = () => {
  if (!selected.value) return alert('수정할 항목을 선택하세요.');
  mode.value = 'edit';
};

const saveMaterial = async () => {
  if (!form.value.matName) return alert('자재명을 입력하세요.');
  const payload = { ...form.value };
  try {
    if (mode.value === 'new') {
      await axios.post('/api/material', payload);
      await fetchList();
      mode.value = 'view';
      alert('등록되었습니다.');
    } else if (mode.value === 'edit') {
      await axios.put(`/api/material/${form.value.matId}`, payload);
      await fetchList();
      await fetchDetail(form.value.matId);
      alert('수정되었습니다.');
    }
  } catch (e) {
    console.error('saveMaterial error', e);
    alert('저장에 실패했습니다.');
  }
};

const deleteMaterial = async () => {
  const matId = selected.value?.matId ?? selected.value?.MAT_ID ?? form.value.matId;
  if (!matId) return alert('삭제할 항목을 선택하세요.');
  if (!confirm(`자재 [${matId}]를 삭제하시겠습니까?`)) return;

  try {
    await axios.delete(`/api/material/${matId}`);
    alert('삭제되었습니다.');
    selected.value = null;
    form.value = {
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
    };
    mode.value = 'view';
    await fetchList();
  } catch (e) {
    console.error('deleteMaterial error', e);
    alert('삭제에 실패했습니다.');
  }
};

const cancelEdit = () => {
  if (mode.value === 'new') {
    form.value = {
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
    };
    selected.value = null;
    mode.value = 'view';
  } else if (mode.value === 'edit') {
    if (selected.value) fetchDetail(selected.value.matId ?? selected.value.MAT_ID);
    mode.value = 'view';
  }
};

onMounted(() => fetchList());
</script>

<template>
  <div class="container p-4">
    <h2 class="mb-3">자재 관리</h2>

    <div class="mb-4 flex gap-2 items-center">
      <InputText v-model="searchName" placeholder="자재명" class="h-10" />
      <InputText v-model="searchMatType" placeholder="자재유형" class="h-10" />
      <InputText v-model="searchMatStoreCond" placeholder="보관조건" class="h-10" />
      <InputText v-model="searchMatId" placeholder="자재코드" class="h-10" />
      <button class="btn" @click="onSearch">조회</button>
      <button class="btn-secondary" @click="onReset">초기화</button>
      <div class="ml-auto flex gap-2">
        <button class="btn" @click="newMaterial">신규</button>
      </div>
    </div>

    <div class="grid grid-cols-2 gap-6">
      <!-- 목록 -->
      <div>
        <h3>자재 목록</h3>
        <table class="w-full border-collapse">
          <thead>
            <tr class="text-left border-b">
              <th class="py-2">MAT_ID</th>
              <th class="py-2">자재명</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in matList" :key="row.matId ?? row.MAT_ID" @click="onRowClick(row)" class="cursor-pointer hover:bg-gray-100">
              <td class="py-2">{{ row.matId ?? row.MAT_ID }}</td>
              <td class="py-2">{{ row.matName ?? row.MAT_NAME }}</td>
            </tr>
          </tbody>
        </table>
        <div v-if="!matList.length" class="text-sm text-gray-500 mt-2">데이터가 없습니다.</div>
      </div>

      <!-- 상세 -->
      <div>
        <h3>상세 / 편집</h3>
        <div class="flex justify-end gap-2 mb-2">
          <button v-if="mode === 'view' && selected" class="btn" @click="editMaterial">수정</button>
          <button v-if="mode === 'view' && selected" class="btn-danger" @click="deleteMaterial">삭제</button>
          <button v-if="mode === 'edit' || mode === 'new'" class="btn" @click="saveMaterial">저장</button>
          <button v-if="mode === 'edit' || mode === 'new'" class="btn-secondary" @click="cancelEdit">취소</button>
        </div>

        <div class="grid gap-2">
          <div>
            <label class="text-sm block mb-1">자재코드</label>
            <InputText v-model="form.matId" class="w-full h-10" :disabled="true" placeholder="자재코드는 자동 생성됩니다" />
          </div>
          <div>
            <label class="text-sm block mb-1">자재명</label>
            <InputText v-model="form.matName" class="w-full h-10" placeholder="자재명 (필수)" />
          </div>
          <div>
            <label class="text-sm block mb-1">자재유형</label>
            <InputText v-model="form.matType" class="w-full h-10" placeholder="자재유형" />
          </div>
          <div>
            <label class="text-sm block mb-1">규격</label>
            <InputText v-model="form.spec" class="w-full h-10" placeholder="규격" />
          </div>
          <div>
            <label class="text-sm block mb-1">단위</label>
            <InputText v-model="form.unit" class="w-full h-10" placeholder="단위" />
          </div>
          <div>
            <label class="text-sm block mb-1">보관조건</label>
            <InputText v-model="form.matStoreCond" class="w-full h-10" placeholder="보관조건" />
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
.text-red-600 {
  color: #dc2626;
}
.text-blue-600 {
  color: #2563eb;
}
.text-black {
  color: #111827;
}
.text-gray-600 {
  color: #4b5563;
}
</style>
