<script setup>
import { toRef } from 'vue';
import { useCountUp } from '@/composables/useCountUp';

const props = defineProps({
  endVal: {
    type: Number,
    required: true
  },
  duration: {
    type: Number,
    default: 0.15
  }
});

const endValRef = toRef(props, 'endVal');
const { displayVal } = useCountUp(endValRef, props.duration);
</script>

<template>
  <span>{{ displayVal }}</span>
</template>
