<script>
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';

export default {
  components: { DataTable, Column },
  props: {
    columns: {
      type: Array,
      required: true
    },
    dataRows: {
      type: Array,
      required: true
    },
    selection: {
      type: [Object, Array], // 단일 선택 or 다중 선택
      default: null
    }
  },
  emits: ['update:dataRows', 'update:selection']
};
</script>

<template>
  <div class="card w-full">
    <DataTable :value="dataRows" dataKey="id" tableStyle="min-width: 50rem" :selection="selection" @update:selection="$emit('update:selection', $event)" :paginator="paginator" :rows="rows" :stripedRows="true" :rowHover="true" showGridlines>
      <Column selectionMode="multiple" headerStyle="width: 3rem"></Column>
      <!--채크박스영역-->

      <!--컬럼 동적으로 사용. 호출하는 곳 data()영역에 columns 정의해줄것. 
                columns : [{field:'matName', header: '자재명' },{field:'matCode', header: '자재코드'}]-->
      <Column v-for="col in columns" :key="col.field" :field="col.field" :header="col.header" :headerStyle="col.headerStyle">
        <template #body="slotProps">
          <!--조건에 따라 input-->
          <!-- { field: 'purch_qty', header: '수량', inputNumber: true },  {field: 'comm', header: '비고', inputText: true } -->
          <!--{ field: 'warehouse', header: '보관창고', headerStyle: 'width: 20rem', inputTextWM: true, onClick: this.openWarehouseeModal },-->
          <InputText v-model="slotProps.data[col.field]" type="text" style="width: 100%" v-if="col.inputText" />
          <InputText v-model="slotProps.data[col.field]" type="text " style="width: 100%" @click="col.onClick(slotProps.index)" v-else-if="col.inputTextWM" />
          <InputNumber v-model="slotProps.data[col.field]" :min="0" type="number" style="width: 80%" v-else-if="col.inputNumber" />

          <!-- 기본 출력 -->
          <span v-else>{{ slotProps.data[col.field] }}</span>
        </template>
      </Column>
    </DataTable>
  </div>
</template>
