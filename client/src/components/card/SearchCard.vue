<script setup>
import { computed, defineEmits, ref } from 'vue';
import { useIcon } from '@/composables/useIcon';

const props = defineProps({
  title: { type: String, default: '검색' },
  icon: { type: String, default: 'search' },
  showButtons: { type: Boolean, default: true }
});

const emits = defineEmits(['search', 'reset']);
const collapsed = ref(false);

const iconClass = computed(() => useIcon(props.icon));

function onSearch() {
  emits('search');
}

function onReset() {
  emits('reset');
}

function toggleCollapse() {
  collapsed.value = !collapsed.value;
}
</script>

<template>
  <div class="card flex flex-col w-full mt-4" id="card">
    <div 
      class="font-semibold text-xl flex items-center gap-4 cursor-pointer select-none h-10"
      @click="toggleCollapse"
      aria-expanded="!collapsed"
      role="button"
      tabindex="0"
      @keydown.enter.prevent="toggleCollapse"
      @keydown.space.prevent="toggleCollapse"
    >
      <span :class="[iconClass, 'text-xl']"></span>
      {{ title }}
      <svg
        :class="{'rotate-180': !collapsed, 'rotate-0': collapsed}"
        class="ml-auto w-5 h-5 transition-transform duration-300"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M19 9l-7 7-7-7" />
      </svg>
    </div>

    <transition name="fade">
      <div v-show="!collapsed">
        <Divider />
        <slot />
        <div v-if="showButtons" class="flex sm:justify-end justify-start gap-2 mt-4">
          <div class="w-1/2 sm:w-32">
            <Btn color="secondary" class="w-full" icon="refresh" @click="onReset" outlined>초기화</Btn>
          </div>
          <div class="w-1/2 sm:w-32">
            <Btn class="w-full" icon="search" @click="onSearch">검색</Btn>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
#card {
  margin-bottom: 0;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
