<script setup>
import { ref, defineProps, defineEmits, watch, onMounted } from 'vue';
import axios from 'axios';
import SearchCard from '@/components/card/SearchCard.vue';
import { useUserStore } from '@/stores/user';
import { useIcon } from '@/composables/useIcon';

const emit = defineEmits(['select', 'update-page', 'update-search', 'create']);
const props = defineProps({
  page: Object,
  searchParams: Object
});

const userStore = useUserStore();
const icons = {
  search: useIcon('search'),
  list: useIcon('list'),
};

const columns = [
  { label: '번호', field: 'noticeNo' },
  { label: '제목', field: 'title' },
  { label: '작성자', field: 'author' },
  { label: '작성일자', field: 'createdAt' },
];

const list = ref([]);
const loading = ref(false);

const fetchList = async () => {
  try {
    loading.value = true;
    const { data, status } = await axios.get('/api/notice', {
      params: {
        title: props.searchParams.title,
        page: props.page.page,
        size: props.page.size,
      }
    });

    if (status === 204) {
      list.value = [];
      emit('update-page', { ...props.page, totalElements: 0 });
      return;
    }

    list.value = data.data;
    emit('update-page', { ...props.page, totalElements: data.page.totalElements });
  } catch (err) {
    console.error('조회 중 오류:', err);
  } finally {
    loading.value = false;
  }
};

watch(
  () => props.page.page,
  () => {
    fetchList();
  }
);

const handleSearch = () => {
  emit('update-search', { title: props.searchParams.title });
  emit('update-page', { ...props.page, page: 1 });
  fetchList();
};

const handleReset = () => {
  emit('update-search', { title: '' });
  emit('update-page', { ...props.page, page: 1 });
  fetchList();
};

const handlePageChange = ({ page, size }) => {
  emit('update-page', { ...props.page, page, size });
};

const onRowSelect = (item) => {
  emit('select', item.noticeNo);
};

const handleCreate = () => {
  emit('create');
};

onMounted(() => {
  fetchList();
});
</script>

<template>
  <SearchCard title="공지사항 검색" @search="handleSearch" @reset="handleReset">
    <div class="flex flex-col gap-2 p-2 w-full">
      <InputGroup>
        <InputGroupAddon><i :class="icons.search" /></InputGroupAddon>
        <IftaLabel>
          <InputText v-model="props.searchParams.title" inputId="title" />
          <label for="title">제목</label>
        </IftaLabel>
      </InputGroup>
    </div>
  </SearchCard>

  <div class="w-full mt-4">
    <div class="card flex flex-col">
      <div class="font-semibold text-lg sm:text-xl flex items-center justify-between gap-4 h-10">
        <div class="flex items-center gap-4">
          <span :class="icons.list"></span> 공지사항 목록
          <div class="text-sm text-gray-400" style="align-self: flex-end;">
            총
            <span class="font-semibold text-sm text-gray-700">
              <CountUp :end-val="props.page.totalElements" v-if="props.page.totalElements > 0" />
              <span v-else>0</span>
            </span>건
          </div>
        </div>

        <div
          v-if="['admin', 'employee'].some(role => userStore.role?.includes(role))"
          class="flex gap-2"
        >
          <Btn
            icon="add"
            label="공지사항 등록"
            class="whitespace-nowrap"
            outlined
            @click="handleCreate"
          />
        </div>
      </div>

      <Divider />

      <DTable
        :columns="columns"
        :data="list"
        :page="props.page"
        :loading="loading"
        dataKey="noticeNo"
        @page-change="handlePageChange"
        @row-select="onRowSelect"
      />
    </div>
  </div>
</template>
