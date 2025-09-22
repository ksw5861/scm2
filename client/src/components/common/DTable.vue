<script setup>
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InputText from 'primevue/inputtext'
import { defineProps, ref, watch, defineEmits } from 'vue'

const props = defineProps({
  columns: { type: Array, required: true },
  data: { type: Array, required: true },
  paginator: { type: Boolean, default: true },
  rows: { type: Number, default: 10 },
  totalRecords: { type: Number, default: 0 },
  loading: { type: Boolean, default: false },
  currentPage: { type: Number, default: 1 },
  sortField: { type: String, default: null },
  sortOrder: { type: Number, default: null } // 1=asc, -1=desc
})

const emit = defineEmits(['page-change', 'sort-change', 'row-select'])

const localData = ref(props.data.map(item => ({ ...item })))

watch(
  () => props.data,
  (newData) => {
    localData.value = newData.map(item => ({ ...item }))
  },
  { deep: true }
)

const selectedRow = ref(null)

const onRowSelect = (event) => {
  selectedRow.value = event.data;
  emit('row-select', event.data);
}

const onPage = (event) => {
  emit('page-change', { first: event.first, rows: event.rows });
}

const onSort = (event) => {
  emit('sort-change', event)
}

</script>

<template>
  <DataTable
    v-model:selection="selectedRow"
    selectionMode="single"
    dataKey="employeeId"
    :value="localData"
    :paginator="paginator"
    :rows="rows"
    :totalRecords="totalRecords"
    :lazy="true"
    :loading="loading"
    :first="(currentPage - 1) * rows"
    :sortField="sortField"
    :sortOrder="sortOrder"
    :stripedRows="true"
    :rowHover="true"
    showGridlines
    @row-select="onRowSelect"
    @page="onPage"
    @sort="onSort"
  >
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
