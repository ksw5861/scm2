<script setup>
import { defineProps, defineEmits } from 'vue';
import DTable from '@/components/common/DTable.vue';
import { useIcon } from '@/composables/useIcon';

const props = defineProps({
  title: { type: String, default: '목록' },
  iconList: { type: String, default: useIcon('list') },
  columns: { type: Array, required: true },
  data: { type: Array, required: true },
  paginator: { type: Boolean, default: true },
  rows: { type: Number, default: 10 },
  totalRecords: { type: Number, default: 0 },
  loading: { type: Boolean, default: false },
  currentPage: { type: Number, default: 1 },
  sortField: { type: String, default: null },
  sortOrder: { type: Number, default: null } // 1=asc, -1=desc
});

const emit = defineEmits(['page-change', 'sort-change', 'row-select']);

const onPageChange = (event) => {
  emit('page-change', event);
}

const onSortChange = (event) => {
  emit('sort-change', event);
}

const onRowSelect = (event) => {
  emit('row-select', event);
}
</script>

<template>
  <div class="w-full lg:w-5/12 md:w-1/2">
    <div class="card flex flex-col">
      <div class="font-semibold text-xl flex items-center gap-4">
        <span :class="[iconList, 'text-xl']" />
        {{ title }}
      </div>
      <Divider />
      <DTable
        :columns="columns"
        :data="data"
        :paginator="paginator"
        :rows="rows"
        :totalRecords="totalRecords"
        :loading="loading"
        :currentPage="currentPage"
        :sortField="sortField"
        :sortOrder="sortOrder"
        @page-change="onPageChange"
        @sort-change="onSortChange"
        @row-select="onRowSelect"
      />
    </div>
  </div>
</template>
