<script setup>
import { ref, watch } from 'vue';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import InputText from 'primevue/inputtext';
import axios from 'axios';
import { debounce } from 'lodash-es';

const props = defineProps({
  visible: Boolean,
  title: { type: String, default: '주소 검색' },
  columns: {
    type: Array,
    default: () => [{ label: '도로명 주소', field: 'address' }]
  },
  pageSize: { type: Number, default: 8 }
});

const emit = defineEmits(['close', 'select']);

const search = ref('');
const data = ref([]);
const allData = ref([]);
const loading = ref(false);
const page = ref({ page: 1, size: props.pageSize, totalElements: 0 });

const showDetailInput = ref(false);
const selectedAddressItem = ref(null);
const detailAddress = ref('');

const handleLocalPaging = () => {
  const sortedData = allData.value;
  page.value.totalElements = sortedData.length;

  const start = (page.value.page - 1) * page.value.size;
  const end = start + page.value.size;
  data.value = sortedData.slice(start, end);
};

const fetchAllAddressData = async (query) => {
  loading.value = true;
  allData.value = [];
  try {
    const response = await axios.get('/api/search/address', { params: { query } });
    const items = Array.isArray(response.data?.items)
      ? response.data.items
      : Array.isArray(response.data)
      ? response.data
      : [];
    allData.value = items;
    page.value.page = 1;
    handleLocalPaging();
  } catch (error) {
    console.error('주소 검색 API 오류:', error);
    allData.value = [];
    page.value.totalElements = 0;
  } finally {
    loading.value = false;
  }
};

const debouncedSearch = debounce(() => {
  if (search.value.trim().length >= 2) {
    fetchAllAddressData(search.value.trim());
  }
}, 500);

watch(search, (newSearch) => {
  const query = newSearch.trim();
  if (query.length >= 2) {
    debouncedSearch();
  } else {
    debouncedSearch.cancel();
    loading.value = false;
    data.value = [];
    allData.value = [];
    page.value.page = 1;
    page.value.totalElements = 0;
  }
});

const selectAddress = (item) => {
  selectedAddressItem.value = item;
  detailAddress.value = '';
  showDetailInput.value = true;
};

const confirmAddress = () => {
  if (!selectedAddressItem.value) return;
  const fullAddress = `${selectedAddressItem.value.address} ${detailAddress.value.trim()}`.trim();
  emit('select', fullAddress);
  close();
};

const close = () => {
  search.value = '';
  data.value = [];
  allData.value = [];
  page.value.page = 1;
  page.value.totalElements = 0;
  loading.value = false;
  selectedAddressItem.value = null;
  detailAddress.value = '';
  showDetailInput.value = false;
  debouncedSearch.cancel();
  emit('close');
};

const onPageChange = (event) => {
  page.value.page = event.page;
  page.value.size = event.size;
  handleLocalPaging();
};
</script>

<template>
  <Dialog :visible="props.visible" modal :style="{ width: '800px' }" :closable="false" @hide="close">
    <template #header>
      <div class="text-lg font-bold flex justify-between items-center w-full">
        {{ title }}
        <Button icon="pi pi-times" class="p-button-text" @click="close" />
      </div>
    </template>

    <div v-if="!showDetailInput">
      <div class="mb-3">
        <InputText
          v-model="search"
          class="w-full"
          placeholder="검색할 주소를 2글자 이상 입력하세요."
          @keydown.enter="() => { if (search.trim().length >= 2) fetchAllAddressData(search.trim()); }"
        />
      </div>

      <DTable
        :columns="columns"
        :data="data"
        :page="page"
        :loading="loading"
        dataKey="zipNo"
        @row-select="selectAddress"
        @page-change="onPageChange"
      />

      <div v-if="!loading && data.length === 0" class="p-4 text-center text-gray-500">
        검색 결과가 없습니다.
      </div>
    </div>

    <div v-else>
      <div class="mb-3">
        <div class="font-semibold mb-2">선택된 주소</div>
        <div class="p-2 border rounded mb-4">{{ selectedAddressItem.address }}</div>

        <label for="detailAddress" class="font-semibold block mb-1">상세 주소 입력</label>
        <InputText
          id="detailAddress"
          v-model="detailAddress"
          placeholder="상세 주소를 입력하세요 (선택사항)"
          class="w-full"
          @keydown.enter.prevent="confirmAddress"
        />
      </div>

      <div class="flex justify-end gap-2">
        <Button label="취소" class="p-button-text" @click="close" />
        <Button label="확인" @click="confirmAddress" />
      </div>
    </div>
  </Dialog>
</template>