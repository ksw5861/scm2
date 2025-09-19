<script setup>
import { ref, computed, watch } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
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
const fullData = ref([]);

const filteredData = computed(() =>
  !search.value
    ? fullData.value
    : fullData.value.filter(item =>
        Object.values(item).some(val =>
          String(val).toLowerCase().includes(search.value.toLowerCase())
        )
      )
);

const loadData = async () => {
  const result = await props.fetchData?.();
  fullData.value = result || [];
};

const selectItem = (item) => {
  emit('select', item);
  close();
};

const close = () => {
  dialog.value = false;
  search.value = '';
  emit('close');
};

watch(() => props.visible, (val) => {
  dialog.value = val;
  if (val) loadData();
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

    <DataTable
      :value="filteredData"
      :paginator="true"
      :rows="pageSize"
      dataKey="id"
      selectionMode="single"
      rowHover
      :stripedRows="true"
      :scrollable="true"
      scrollHeight="400px"
      @row-click="selectItem($event.data)"
    >
      <Column
        v-for="col in columns"
        :key="col.key"
        :field="col.key"
        :header="col.label"
        sortable
      />
    </DataTable>
  </Dialog>
</template>
