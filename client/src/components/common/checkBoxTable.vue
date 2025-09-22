<script setup>
import { defineProps, defineEmits, ref, watch } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'

// Props 정의
const props = defineProps({
  columns: { type: Array, required: true },     // 동적 컬럼 정의
  data: { type: Array, required: true },        // 데이터 행
  paginator: { type: Boolean, default: true },  // 페이지네이션 여부
  rows: { type: Number, default: 10 },          // 페이지 당 행 수
  totalRecords: { type: Number, default: 0 },   // 전체 데이터 수
  loading: { type: Boolean, default: false },
  currentPage: { type: Number, default: 1 },
  sortField: { type: String, default: null },
  sortOrder: { type: Number, default: null },   // 1=asc, -1=desc
  selectionMode: { type: String, default: 'multiple' } // 행 선택 모드
})

// 이벤트 정의
const emit = defineEmits(['page-change', 'sort-change', 'row-select', 'selection-change'])

// 로컬 데이터 (props.data를 감싸서 반응형 유지)
const localData = ref(props.data.map(item => ({ ...item })))
watch(
  () => props.data,
  (newData) => {
    localData.value = newData.map(item => ({ ...item }))
  },
  { deep: true }
)

// 선택된 행
const selectedRows = ref([])

// 이벤트 핸들러
const onRowSelect = (event) => {
  emit('row-select', event.data)
}

const onSelectionChange = (event) => {
  selectedRows.value = event.value
  emit('selection-change', event.value)
}

const onPage = (event) => {
  emit('page-change', { first: event.first, rows: event.rows })
}

const onSort = (event) => {
  emit('sort-change', event)
}
</script>

<template>
  <DataTable
    v-model:selection="selectedRows"
    :selectionMode="selectionMode"
    dataKey="id"
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
    @selection-change="onSelectionChange"
    @page="onPage"
    @sort="onSort"
  >
    <!-- 행 선택 체크박스 -->
    <Column v-if="selectionMode === 'multiple'" selectionMode="multiple" headerStyle="width: 3rem" />

    <!-- 동적 컬럼 -->
    <template v-for="col in columns" :key="col.field">
      <!-- 단순 출력 열 -->
      <Column
        v-if="!col.inputText && !col.inputNumber && !col.datePicker"
        :field="col.field"
        :header="col.label"
        :sortable="col.sortable ?? false"
        :style="col.style"
      />

      <!-- InputText 열 -->
      <Column v-else-if="col.inputText" :field="col.field" :header="col.label" :style="col.style" :sortable="col.sortable ?? false">
        <template #body="slotProps">
          <InputText
            v-model="slotProps.data[col.field]"
            class="w-full"
            :placeholder="col.placeholder || ''"
          />
        </template>
      </Column>

      <!-- 클릭시 모달 -->
      <Column v-else-if="col.inputText" :field="col.field" :header="col.label" :style="col.style" :sortable="col.sortable ?? false">
        <template #body="slotProps">
          <InputText
            v-model="slotProps.data[col.field]"
            class="w-full"
            :placeholder="col.placeholder || ''" @click="col.onClick(slotProps.index)"
          />
        </template>
      </Column>


      <!-- InputNumber 열 -->
      <Column v-else-if="col.inputNumber" :field="col.field" :header="col.label" :style="col.style" :sortable="col.sortable ?? false">
        <template #body="slotProps">
          <InputNumber
            v-model="slotProps.data[col.field]"
            :min="0"
            showButtons
            style="width: 100%"
          />
        </template>
      </Column>

      <!-- DatePicker 열 -->
  <Column v-else-if="col.datePicker" :field="col.field" :header="col.label" :style="col.style">
    <template #body="slotProps">
      <DatePicker
        v-model="slotProps.data[col.field]"
        showIcon
        fluid
        :showOnFocus="false"  dateFormat="yy-mm-dd"
      />
    </template>
  </Column>

    </template>
  </DataTable>
</template>
