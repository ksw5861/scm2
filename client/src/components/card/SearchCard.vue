<script setup>
import { computed, defineEmits, ref } from 'vue';
import { useIcon } from '@/composables/useIcon';
import Button from 'primevue/button'; // PrimeVue Button 컴포넌트를 직접 import 합니다.

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
      class="font-semibold text-lg sm:text-xl flex items-center gap-4 cursor-pointer select-none h-10"
      @click="toggleCollapse"
      aria-expanded="!collapsed"
      role="button"
      tabindex="0"
      @keydown.enter.prevent="toggleCollapse"
      @keydown.space.prevent="toggleCollapse"
    >
      <span :class="iconClass"></span>
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
        <div v-if="showButtons" class="flex justify-between sm:justify-end gap-2 mt-4">
          <div class="w-1/2 xl:w-32">
            <Button
              label="초기화"
              severity="secondary"
              icon="pi pi-refresh"
              @click="onReset"
              outlined
              class="w-full"
            />
          </div>
          <div class="w-1/2 xl:w-32">
            <Button
              label="조회"
              icon="pi pi-search"
              @click="onSearch"
              class="w-full"
            />
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

/* 1200px 미만 화면에서 버튼 스타일 조정 */
.responsive-btn :deep(.p-button-label) {
  @apply hidden;
}

.responsive-btn {
  @apply !p-0 h-[35px] w-[35px];
}

/* 1200px 이상 화면에서 버튼 스타일 조정 */
@media (min-width: 1200px) {
  .responsive-btn :deep(.p-button-label) {
    @apply inline-block;
  }
  .responsive-btn {
    @apply !py-2 !px-4 !h-auto !w-auto;
  }
}
</style>