// composables/useCountUp.js
import { ref, watch, onMounted } from 'vue';

export function useCountUp(endVal, duration = 0.15) {
  const displayVal = ref(0);
  let startTime = null;
  let frameId = null;
  let startVal = 0;

  const animate = (timestamp) => {
    if (!startTime) startTime = timestamp;
    const elapsed = (timestamp - startTime) / 1000;
    if (elapsed < duration) {
      const progress = elapsed / duration;
      displayVal.value = Math.floor(startVal + (endVal.value - startVal) * progress);
      frameId = requestAnimationFrame(animate);
    } else {
      displayVal.value = endVal.value;
      cancelAnimationFrame(frameId);
    }
  };

  watch(endVal, (newVal, oldVal) => {
    startVal = displayVal.value;
    if (frameId) cancelAnimationFrame(frameId);
    startTime = null;
    frameId = requestAnimationFrame(animate);
  }, { immediate: true });

  onMounted(() => {
    startVal = 0;
    frameId = requestAnimationFrame(animate);
  });

  return {
    displayVal
  };
}
