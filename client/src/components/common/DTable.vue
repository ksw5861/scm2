<!-- DTable.vue -->
<script setup>
import { ref, watch, defineProps, defineEmits, computed } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InputText from 'primevue/inputtext'

const props = defineProps({
  columns: { type: Array, required: true },
  data: { type: Array, required: true },
  page: {
    type: Object,
    required: true,
    default: () => ({
      page: 1,
      size: 10,
      totalElements: 0
    })
  },
  loading: { type: Boolean, default: false },
  dataKey: { type: String, default: 'id' }
})

const emit = defineEmits(['page-change', 'sort-change', 'row-select'])

const localData = ref([...props.data])

watch(() => props.data, (newData) => {
  localData.value = [...newData]
})

const selectedRow = ref(null)

const rows = computed(() => props.page.size)
const totalRecords = computed(() => props.page.totalElements)
const currentPage = computed(() => props.page.page)

const onRowSelect = (event) => {
  selectedRow.value = event.data
  emit('row-select', event.data)
}

const onPage = (event) => {
  const newPage = Math.floor(event.first / event.rows) + 1
  emit('page-change', { page: newPage, size: event.rows })
}

const onSort = (event) => {
  emit('sort-change', event)
}
</script>

<template>
  <DataTable
    v-model:selection="selectedRow"
    selectionMode="single"
    :dataKey="dataKey"
    :value="localData"
    paginator
    :rows="rows"
    :totalRecords="totalRecords"
    :lazy="true"
    :loading="loading"
    :first="(currentPage - 1) * rows"
    :stripedRows="true"
    :rowHover="true"
    showGridlines
    @row-select="onRowSelect"
    @page="onPage"
    @sort="onSort"
    >
    <template v-for="col in columns" :key="col.field">
        <Column
        v-if="!col.input"
        :field="col.field"
        :header="col.label"
        :sortable="col.sortable ?? false"
        />
        <Column
        v-else
        :field="col.field"
        :header="col.label"
        :sortable="col.sortable ?? false"
        >
        <template #body="slotProps">
            <InputText
            v-model="slotProps.data[col.field]"
            class="w-full"
            :placeholder="col.placeholder || ''"
            />
        </template>
        </Column>
    </template>
  </DataTable>
</template>
