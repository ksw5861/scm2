<script setup>
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import InputText from 'primevue/inputtext';
import { defineProps, reactive, watch } from 'vue';

const props = defineProps({
  columns: {
    type: Array,
    required: true
  },
  data: {
    type: Array,
    required: true
  },
  paginator: {
    type: Boolean,
    default: true
  },
  rows: {
    type: Number,
    default: 10
  }
});

const localData = reactive(props.data.map((item) => ({ ...item })));
</script>

<template>
  <DataTable :value="localData" :paginator="paginator" :rows="rows" :stripedRows="true" :rowHover="true" showGridlines>
    <template v-for="col in columns" :key="col.field">
      <Column v-if="!col.input" :field="col.field" :header="col.label" :sortable="col.sortable ?? false" />
      <Column v-else :field="col.field" :header="col.label" :sortable="col.sortable ?? false">
        <template #body="slotProps">
          <InputText v-model="slotProps.data[col.field]" class="w-full" :placeholder="col.placeholder || ''" />
        </template>
      </Column>
    </template>
  </DataTable>
</template>
