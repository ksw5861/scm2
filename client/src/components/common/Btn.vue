<script setup>
import { useSlots, computed } from 'vue';
import { useIcon } from '@/composables/useIcon';

const props = defineProps({
    color: { type: String, default: 'primary' },
    icon: { type: String, default: null }
});

const slots = useSlots();
const label = computed(() => slots.default?.()[0]?.children ?? '');

const icon = computed(() => (props.icon ? useIcon(props.icon) : null));
</script>

<template>
    <Button :label="label" :severity="color" :icon="icon" class="btn-bold" />
</template>

<style scoped>
.btn-bold .p-button-label {
    font-weight: bold;
}
</style>
