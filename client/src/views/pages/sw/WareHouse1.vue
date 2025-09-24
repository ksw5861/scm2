<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import InputText from 'primevue/inputtext';

const searchWhId = ref('');
const searchWhName = ref('');
const searchStatus = ref('');
const whList = ref([]);
const selected = ref(null);

const form = ref({
  whId: '',
  whName: '',
  whAddress: '',
  whOwner: '',
  ownerTel: '',
  status: ''
});

const mode = ref('view'); // view | edit | new

const fetchList = async () => {
  try {
    const res = await axios.get('/api/warehouse1', {
      params: {
        whId: searchWhId.value || undefined,
        whName: searchWhName.value || undefined,
        status: searchStatus.value || undefined
      }
    });
    whList.value = res.data || [];
  } catch (e) {
    console.error(e);
    alert('창고 목록 조회 실패');
  }
};

const fetchDetail = async (whId) => {
  if (!whId) return;
  try {
    const res = await axios.get(`/api/warehouse1/${whId}`);
    selected.value = res.data;
    form.value = { ...res.data };
    mode.value = 'view';
  } catch (e) {
    console.error(e);
    alert('창고 상세 조회 실패');
  }
};

const onRowClick = (row) => {
  fetchDetail(row.whId ?? row.WH_ID);
};

const onSearch = () => fetchList();
const onReset = () => {
  searchWhId.value = '';
  searchWhName.value = '';
  searchStatus.value = '';
  fetchList();
};

const newWareHouse = () => {
  form.value = { whId: '', whName: '', whAddress: '', whOwner: '', ownerTel: '', status: '' };
  selected.value = null;
  mode.value = 'new';
};

const editWareHouse = () => {
  if (!selected.value) return alert('수정할 항목을 선택하세요.');
  mode.value = 'edit';
};

const saveWareHouse = async () => {
  if (mode.value === 'new') {
    if (!form.value.whName) return alert('창고명을 입력하세요.');
  } else {
    if (!form.value.whId || !form.value.whName) return alert('창고코드와 이름을 입력하세요.');
  }

  try {
    if (mode.value === 'new') {
      await axios.post('/api/warehouse1', form.value);
      await fetchList();
      alert('등록되었습니다.');
    } else if (mode.value === 'edit') {
      await axios.put(`/api/warehouse/${form.value.whId}`, form.value);
      await fetchList();
      await fetchDetail(form.value.whId);
      alert('수정되었습니다.');
    }
  } catch (e) {
    console.error(e);
    alert('저장 실패');
  } finally {
    mode.value = 'view';
  }
};

const deleteWareHouse = async () => {
  const whId = selected.value?.whId ?? form.value.whId;
  if (!whId) return alert('삭제할 항목을 선택하세요.');
  if (!confirm(`창고 [${whId}]를 삭제하시겠습니까?`)) return;

  try {
    await axios.delete(`/api/warehouse1/${whId}`);
    alert('삭제되었습니다.');
    selected.value = null;
    form.value = { whId: '', whName: '', whAddress: '', whOwner: '', ownerTel: '', status: '' };
    mode.value = 'view';
    await fetchList();
  } catch (e) {
    console.error(e);
    alert('삭제 실패');
  }
};

const cancelEdit = () => {
  if (mode.value === 'new') {
    form.value = { whId: '', whName: '', whAddress: '', whOwner: '', ownerTel: '', status: '' };
    mode.value = 'view';
    selected.value = null;
  } else if (mode.value === 'edit') {
    if (selected.value) fetchDetail(selected.value.whId);
    mode.value = 'view';
  }
};

onMounted(() => {
  fetchList();
});
</script>

<template>
  <div class="container p-4">
    <h2 class="mb-3">창고 관리</h2>

    <div class="mb-4 flex gap-2 items-center">
      <InputText v-model="searchWhId" placeholder="창고코드" class="h-10" />
      <InputText v-model="searchWhName" placeholder="창고명" class="h-10" />
      <InputText v-model="searchStatus" placeholder="상태" class="h-10" />
      <button class="btn" @click="onSearch">조회</button>
      <button class="btn-secondary" @click="onReset">초기화</button>
      <div class="ml-auto flex gap-2">
        <button class="btn" @click="newWareHouse">신규</button>
      </div>
    </div>

    <div class="grid grid-cols-2 gap-6">
      <div>
        <h3>창고 목록</h3>
        <table class="w-full border-collapse">
          <thead>
            <tr class="text-left border-b">
              <th class="py-2">WH_ID</th>
              <th class="py-2">창고명</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in whList" :key="row.whId ?? row.WH_ID" @click="onRowClick(row)" class="cursor-pointer hover:bg-gray-100">
              <td>{{ row.whId ?? row.WH_ID }}</td>
              <td>{{ row.whName ?? row.WH_NAME }}</td>
            </tr>
          </tbody>
        </table>
        <div v-if="!whList.length" class="text-sm text-gray-500 mt-2">데이터가 없습니다.</div>
      </div>

      <div>
        <h3>상세 / 편집</h3>

        <div class="flex justify-end gap-2 mb-2">
          <button v-if="mode === 'view' && selected" class="btn" @click="editWareHouse">수정</button>
          <button v-if="mode === 'view' && selected" class="btn-danger" @click="deleteWareHouse">삭제</button>

          <button v-if="mode === 'edit' || mode === 'new'" class="btn" @click="saveWareHouse">저장</button>
          <button v-if="mode === 'edit' || mode === 'new'" class="btn-secondary" @click="cancelEdit">취소</button>
        </div>

        <div class="grid gap-2">
          <div>
            <label class="text-sm block mb-1">창고코드</label>
            <InputText v-model="form.whId" class="w-full h-10" :disabled="true" placeholder="자동 생성" />
          </div>
          <div>
            <label class="text-sm block mb-1">창고명</label>
            <InputText v-model="form.whName" class="w-full h-10" placeholder="창고명" />
          </div>
          <div>
            <label class="text-sm block mb-1">주소</label>
            <InputText v-model="form.whAddress" class="w-full h-10" placeholder="주소" />
          </div>
          <div>
            <label class="text-sm block mb-1">창고 담당자</label>
            <InputText v-model="form.whOwner" class="w-full h-10" placeholder="담당자" />
          </div>
          <div>
            <label class="text-sm block mb-1">전화번호</label>
            <InputText v-model="form.ownerTel" class="w-full h-10" placeholder="전화번호" />
          </div>
          <div>
            <label class="text-sm block mb-1">상태</label>
            <InputText v-model="form.status" class="w-full h-10" placeholder="상태" />
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
  color: white;
  border-radius: 4px;
}
table td,
table th {
  border-bottom: 1px solid #e5e7eb;
  padding: 6px 4px;
}
</style>
