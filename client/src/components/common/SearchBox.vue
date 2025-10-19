<script setup>
import { ref, watch } from 'vue';
import DatePicker from 'primevue/datepicker';
import InputText from 'primevue/inputtext';
import Checkbox from 'primevue/checkbox';
import Select from 'primevue/select';
import InputGroup from 'primevue/inputgroup';
import InputGroupAddon from 'primevue/inputgroupaddon';
import FloatLabel from 'primevue/floatlabel';
import InputNumber from 'primevue/inputnumber';


const props = defineProps({
  type: { type: String, required: true }, // "dateRange", "text", "checkbox"
  label: { type: String, required: true },
  modelValue: { type: [String, Array, Object], default: null },
  options: { type: Array, default: () => [] }, // checkbox 전용 [{label:'대기', value:'WAIT'}, ...]
  icon: { type: String, default: '' },
  width: { type: String, default: 'w-64' },
  sizs: { type: String, default: ''},
  style: { type: [String, Object], default: () => ({}) },
});

const emit = defineEmits(['update:modelValue']);
const internalValue = ref(props.modelValue ?? (props.type === 'dateRange' ? { start: null, end: null } : props.type === 'checkbox' ? [] : ''));

watch(internalValue, (val) => emit('update:modelValue', val));
watch(
  () => props.modelValue,
  (val) => (internalValue.value = val)
);
</script>

<template>
  <div class="flex flex-col">
    <label class="whitespace-nowrap mb-3">{{ label }}</label>

    <!-- 날짜 범위 -->
    <div v-if="type === 'dateRange'" :class="['w-full flex items-center gap-2 ', width]">
      <DatePicker v-model="internalValue.start" showIcon fluid :showOnFocus="false" dateFormat="yy/mm/dd" placeholder="시작일" class="w-full" />
      <span> ~ </span>
      <DatePicker v-model="internalValue.end" showIcon fluid :showOnFocus="false" dateFormat="yy/mm/dd" placeholder="종료일" class="w-full" />
    </div>

    <!--날짜만-->
    <div v-if="type === 'date'" class="flex items-center gap-2">
      <DatePicker v-model="internalValue" showIcon fluid :showOnFocus="false" dateFormat="yy/mm/dd" />
    </div>

    <!-- 텍스트 입력 -->
    <div v-else-if="type === 'text'">
      <InputText v-model="internalValue" :class="['w-full', width]" />
    </div>

    <!--숫자-->
     <div v-else-if="type === 'number'">
      <InputNumber v-model="internalValue" :class="['w-10rem', width]" :style="style"/>
    </div>

    <!-- 텍스트박스 읽기 전용 -->
    <div v-else-if="type === 'readOnly'">
      <InputText v-model="internalValue" class="w-full" readonly="true" :class="['w-full', width]" />
    </div>

    <!-- 텍스트 입력 + 돋보기 아이콘-->
    <div v-else-if="type === 'textIcon'">
      <IconField iconPosition="left">
        <InputText v-model="internalValue" class="w-full" />
        <InputIcon class="pi pi-search" />
      </IconField>
    </div>

    <!--드롭다운-->
    <div v-else-if="type === 'dropDown'">
      <Select v-model="internalValue" :options="options" optionValue="value" optionLabel="name" :class="['w-full', width]" />
    </div>

    <!-- 체크박스 -->
    <div v-else-if="type === 'checkbox'" class="flex flex-wrap items-center gap-4">
      <div v-for="opt in options" :key="opt.value" class="flex items-center gap-2">
        <Checkbox v-model="internalValue" :inputId="opt.value" :value="opt.value" />
        <label :for="opt.value">{{ opt.label }}</label>
      </div>
    </div>

    <!-- 아이콘 + 라벨 InputGroup -->
<div v-else-if="type === 'iconLabel'" :class="width">
  <InputGroup>
    <InputGroupAddon v-if="icon">
      <i :class="icon" />
    </InputGroupAddon>
    <FloatLabel>
      <InputText v-model="internalValue" :inputId="label" />
      <label :for="label">{{ label }}</label>
    </FloatLabel>
  </InputGroup>
</div>
  </div>
</template>

<!--
사용방법
1) div 구역지정 후 내부에 searchField 컴포넌트 삽입

  <div class="flex flex-col gap-4 md:flex-row md:items-end md:gap-6 mt-5 mb-10">

2) 컴포넌트 import후 사용

    label이랑 v-model은 필요한 값으로 변경하면 됨.

    type 종류별로 5가지 있음.

         1. 날짜범위
        <SearchField type="dateRange" label="구매요청일자" v-model="dateRange" />
         2. 날짜
        <SearchField type="textIcon" label="자재명" v-model="materialName" />
         3. 단순텍스트박스
        <SearchField type="date" label="등록일" v-model="registerDate" />
         4. 체크박스
        <SearchField
          type="checkbox"
          label="상태"
          v-model="statusList"
          :options="[
            { label: '대기', value: 'WAIT' },
            { label: '진행중', value: 'PROGRESS' },
            { label: '완료', value: 'DONE' },
            { label: '취소', value: 'CANCEL' }
          ]"
        />
        5. 읽기전용텍스트박스
        <SearchField type="readOnly" label="등록일" v-model="registerDate" />

 </div>



-->
