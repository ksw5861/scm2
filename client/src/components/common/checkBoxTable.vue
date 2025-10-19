<script setup>
import { defineProps, defineEmits, ref, watch } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import Select from 'primevue/select';
import DatePicker from 'primevue/datepicker'

// Props 정의
const props = defineProps({
  columns: { type: Array, required: true }, // 동적 컬럼 정의
  data: { type: Array, required: true }, // 데이터 행
  page: { type: Object, default: () => ({ page: 1, size: 10, totalElements: 0 }) }, // 페이지네이션 여부
  rows: { type: Number, default: 10 }, // 페이지 당 행 수
  paginator: { type: Boolean, default: true },
  totalRecords: { type: Number, default: 0 }, // 전체 데이터 수
  loading: { type: Boolean, default: false }, // 로딩 상태
  currentPage: { type: Number, default: 1 }, // 현재 페이지
  sortField: { type: String, default: null }, // 정렬 필드
  sortOrder: { type: Number, default: null }, // 1=asc, -1=desc
  showCheckbox: { type: Boolean, default: true }, // 체크박스 열 표시 여부
  selectionMode: { type: String, default: 'multiple' } // 행 선택 모드 'multiple' or 'single' 두가지 유형 선택가능
});

// 이벤트 정의
const emit = defineEmits(['page-change', 'sort-change', 'row-select', 'selection-change']);

const localData = ref(props.data); // 감시 대상: 부모에서 내려온 props.data
watch(
  // 데이터 변경 감지
  () => props.data,
  (newData) => {
    localData.value = newData; // props.data가 바뀌면 localData도 새 값으로 교체
  }
);

// 선택된 행
const selectedRows = ref();

// 이벤트 핸들러
const onRowSelect = (event) => {
  emit('row-select', event.data);
};

const onSelectionChange = (event) => {
  selectedRows.value = event.value;
  emit('selection-change', event.value);
};

const onPage = (event) => {
  emit('page-change', { page: event.page + 1, size: event.rows });
};

const onSort = (event) => {
  emit('sort-change', event);
};
</script>

<template>
  <DataTable v-model:selection="selectedRows" dataKey="id" :scrollable="true" scrollDirection="both" scroll-height="400px" scrollWidth="100%" :selectionMode="selectionMode" :value="localData" :paginator="props.paginator" :rows="props.page.size" :totalRecords="props.page.totalElements" :lazy="false" :loading="loading" :first="(props.page.page - 1) * props.page.size" :sortField="sortField" :sortOrder="sortOrder" :stripedRows="true" :rowHover="true" showGridlines @row-select="onRowSelect" @selection-change="onSelectionChange" @page-change="onPage" @sort="onSort" @page="onPage">
    <!-- 행 선택 체크박스 -->
    <Column v-if="showCheckbox" :selectionMode="selectionMode" headerStyle="width: 3rem" />

    <!-- 동적 컬럼들 -->
    <template v-for="col in columns" :key="col.field">
      <!-- 단순 출력 열 -->
      <Column v-if="!col.inputText && !col.inputNumber && !col.select && !col.datePicker" :field="col.field" :header="col.label" :sortable="col.sortable ?? false" :style="col.style" />

      <!-- InputText 열 -->
      <Column v-else-if="col.inputText && !col.onClick" :field="col.field" :header="col.label" :style="col.style" :sortable="col.sortable ?? false">
        <template #body="slotProps">
          <InputText v-model="slotProps.data[col.field]" class="w-full" :placeholder="col.placeholder || ''" />
        </template>
      </Column>

      <!-- 클릭시 모달 -->
      <Column v-else-if="col.inputText && col.onClick" :field="col.field" :header="col.label" :style="col.style" :sortable="col.sortable ?? false">
        <template #body="slotProps">
          <InputText v-model="slotProps.data[col.field]" class="w-full" :placeholder="col.placeholder || ''" @click="col.onClick(slotProps.index)" />
        </template>
      </Column>

      <!--드롭다운-->
      <!-- <Column v-else-if="col.select" :field="col.field" :header="col.label" :style="col.style" :sortable="col.sortable ?? false">
        <template #body="slotProps">
          <Select v-model="slotProps.data[col.field]" :options="col.option" optionLabel="label" optionValue="value" :placeholder="col.placeholder" checkmark :highlightOnSelect="false" class="w-full" />
        </template>
      </Column> -->

      <!--드롭다운-->
      <Column v-else-if="col.select" :field="col.field" :header="col.label" :style="col.style" :sortable="col.sortable ?? false">
        <template #body="slotProps">
          <Select
            v-model="slotProps.data[col.field]"
            :options="
              typeof col.option === 'function'
                ? col.option(slotProps.data) // 행별 옵션
                : col.option?.value || col.option || []
            "
            optionLabel="label"
            optionValue="value"
            :placeholder="col.placeholder"
            checkmark
            class="w-full"
            @change="(e) => col.change?.(slotProps.data, e.value)"
          />
        </template>
      </Column>

      <!-- InputNumber 열 -->
      <Column v-else-if="col.inputNumber" :field="col.field" :header="col.label" :style="col.style" :sortable="col.sortable ?? false">
        <template #body="slotProps">
            <div class="inline-block" :style="col.inputStyle || { width: '8rem' }">
              <InputNumber v-model="slotProps.data[col.field]" :min="0" :showButtons="false"  inputClass="w-full text-right" />
            </div>
        </template>
      </Column>

      <!-- DatePicker 열 -->
      <Column v-else-if="col.datePicker" :field="col.field" :header="col.label" :style="col.style">
        <template #body="slotProps">
          <DatePicker v-model="slotProps.data[col.field]" showIcon fluid :showOnFocus="false" dateFormat="yy/mm/dd" />
        </template>
      </Column>
    </template>
  </DataTable>
</template>
