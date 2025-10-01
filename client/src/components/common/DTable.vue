<script setup>
import { defineProps, defineEmits, computed } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import InputText from 'primevue/inputtext';

const props = defineProps({
    columns: { type: Array, required: true },
    data: { type: Array, required: true }, // 상위 컴포넌트의 데이터를 직접 사용
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
    dataKey: { type: String, default: 'id' },
    selected: { type: Object, default: null }
});

const emit = defineEmits([
    'page-change',
    'sort-change',
    'row-select',
    'row-unselect',
    'update:selected',
    'cell-edit'
]);

const selectedRow = computed({
    get: () => props.selected,
    set: (val) => emit('update:selected', val)
});

const rows = computed(() => props.page.size);
const totalRecords = computed(() => props.page.totalElements);
const currentPage = computed(() => props.page.page);


// ===== Event Handlers (상위 컴포넌트로 이벤트 전달) =====

const onRowSelect = (event) => {
    emit('row-select', event.data);
};

const onRowUnselect = (event) => {
    emit('row-unselect', event.data);
};

const onPage = (event) => {
    const newPage = Math.floor(event.first / event.rows) + 1;
    emit('page-change', { page: newPage, size: event.rows });
};

const onSort = (event) => {
    emit('sort-change', {
        sortField: event.sortField,
        sortOrder: event.sortOrder === 1 ? 'asc' : event.sortOrder === -1 ? 'desc' : null
    });
};

const handleCellEdit = (data, field, newValue) => {
    emit('cell-edit', { data, field, newValue });
};

</script>

<template>
    <DataTable
        v-model:selection="selectedRow"
        selectionMode="single"
        :dataKey="dataKey"
        :value="props.data" paginator
        :rows="rows"
        :totalRecords="totalRecords"
        :lazy="true"
        :loading="loading"
        :first="(currentPage - 1) * rows"
        :stripedRows="true"
        :rowHover="true"
        showGridlines
        @row-select="onRowSelect"
        @row-unselect="onRowUnselect"
        @page="onPage"
        @sort="onSort"
    >
        <template v-for="col in columns" :key="col.field">
            <Column
                v-if="!col.input"
                headerClass="whitespace-nowrap xl:text-sm text-xs"
                bodyClass="whitespace-nowrap text-xs xl:text-sm"
                :field="col.field"
                :header="col.label"
                :sortable="col.sortable ?? false"
            >
                <template v-if="col.format" #body="{ data }">
                    {{ col.format(data) }}
                </template>
            </Column>

            <Column
                v-else
                headerClass="whitespace-nowrap xl:text-sm text-xs"
                bodyClass="whitespace-nowrap text-xs xl:text-sm p-0" :field="col.field"
                :header="col.label"
                :sortable="col.sortable ?? false"
            >
                <template #body="slotProps">
                    <InputText
                        :model-value="slotProps.data[col.field]"
                        class="w-full"
                        :placeholder="col.placeholder || ''"
                        @update:model-value="handleCellEdit(slotProps.data, col.field, $event)" />
                </template>
            </Column>
        </template>
    </DataTable>
</template>

<style scoped>

@media (max-width: 1280px) {
    :deep(.p-datatable .p-datatable-tbody > tr > td) {
        font-size: 10px !important;
    }
}
</style>
