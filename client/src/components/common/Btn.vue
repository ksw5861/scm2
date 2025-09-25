<script setup>
import { computed } from 'vue';
import { useIcon } from '@/composables/useIcon';

const props = defineProps({
  color: { type: String, default: 'primary' },
  icon: { type: String, default: null },
  label: { type: String, default: null }
});

const icon = computed(() => (props.icon ? useIcon(props.icon) : null));
</script>

<template>
  <Button
    :label="label"
    :severity="color"
    :icon="icon"
    class="mobile-responsive-btn"
  />
</template>

<style scoped>
/* 기본 스타일 (1200px 미만 화면에 적용) */
.mobile-responsive-btn {
  @apply !p-0 h-[35px] w-[35px];
}
.mobile-responsive-btn :deep(.p-button-label) {
  @apply hidden;
}

/* 1200px 이상 화면에 적용 */
@media (min-width: 1200px) {
  .mobile-responsive-btn {
    @apply !py-2 !px-4 !h-auto !w-auto;
  }
  .mobile-responsive-btn :deep(.p-button-label) {
    @apply inline-block whitespace-nowrap;
  }
}
</style>