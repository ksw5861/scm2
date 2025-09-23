<script setup>
import { ref, watch } from 'vue';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import InputText from 'primevue/inputtext';

const props = defineProps({
  visible: Boolean,
  title: String,
  columns: Array,
  fetchData: Function,
  pageSize: {
    type: Number,
    default: 5
  }
});

const emit = defineEmits(['close', 'select']);

const dialog = ref(false);
const search = ref('');
const data = ref([]);
const totalRecords = ref(0);
const loading = ref(false);
const currentPage = ref(1);
const sortField = ref(null);
const sortOrder = ref(null);

const loadData = async () => {
  loading.value = true;
  const res = await props.fetchData({
    page: currentPage.value,
    size: props.pageSize,
    search: search.value,
    sortField: sortField.value,
    sortOrder: sortOrder.value,
  });
  data.value = res.items || [];
  totalRecords.value = res.total || 0;
  loading.value = false;
};

const close = () => {
  dialog.value = false;
  search.value = '';
  emit('close');
};

const selectItem = (item) => {
  emit('select', item);
  close();
};

const onPageChange = ({ first, rows }) => {
  currentPage.value = first / rows + 1;
  loadData();
};

const onSortChange = (sortEvent) => {
  sortField.value = sortEvent.sortField;
  sortOrder.value = sortEvent.sortOrder;
  loadData();
};

watch(() => props.visible, (val) => {
  dialog.value = val;
  if (val) loadData();
});

watch(search, () => {
  currentPage.value = 1;
  loadData();
});
</script>

<template>
  <Dialog
    :visible="dialog"
    modal
    :style="{ width: '800px' }"
    :closable="false"
    @hide="close"
  >
    <template #header>
      <div class="text-lg font-bold flex justify-between items-center w-full">
        {{ title }}
        <Button icon="pi pi-times" class="p-button-text" @click="close" />
      </div>
    </template>

    <div class="mb-3">
      <InputText v-model="search" placeholder="검색" class="w-full" />
    </div>

    <DTable
      :columns="columns"
      :data="data"
      :rows="pageSize"
      :totalRecords="totalRecords"
      :currentPage="currentPage"
      :loading="loading"
      :sortField="sortField"
      :sortOrder="sortOrder"
      dataKey="id"
      @row-select="selectItem"
      @page-change="onPageChange"
      @sort-change="onSortChange"
    />
  </Dialog>
</template>
