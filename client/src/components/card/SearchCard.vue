<script setup>
import { computed, defineEmits } from 'vue';
import { useIcon } from '@/composables/useIcon';

const props = defineProps({
  title: { type: String, default: '검색' },
  icon: { type: String, default: 'search' },
  showButtons: { type: Boolean, default: true }
});

const emits = defineEmits(['search', 'reset']);

const iconClass = computed(() => useIcon(props.icon));

function onSearch() {
  emits('search');
}

function onReset() {
  emits('reset');
}
</script>

<template>
  <div class="card flex flex-col w-full mt-4" id="card">
    <div class="font-semibold text-xl flex items-center gap-4">
      <span :class="[iconClass, 'text-xl']"></span>
      {{ title }}
    </div>
    <Divider />
    <div>
      <slot />
    </div>

    <div v-if="showButtons" class="flex flex-wrap justify-end gap-2 mt-4">
      <div class="w-full sm:w-32">
        <Btn color="secondary" class="w-full" icon="refresh" @click="onReset">초기화</Btn>
      </div>
      <div class="w-full sm:w-32">
        <Btn class="w-full" icon="search" @click="onSearch">검색</Btn>
      </div>
    </div>
  </div>
</template>


<style scoped>
#card {
    margin-bottom: 0;
}
</style>
