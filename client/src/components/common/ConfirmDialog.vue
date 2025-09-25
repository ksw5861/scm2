<script setup>
import { defineProps, defineEmits, computed } from 'vue'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'
import { useIcon } from '@/composables/useIcon'

const props = defineProps({
  modelValue: Boolean,
  header: { type: String, default: '확인' },
  message: { type: String, default: '진행하시겠습니까?' },
  cancelLabel: { type: String, default: '취소' },
  confirmLabel: { type: String, default: '확인' },
  cancelIcon: { type: String, default: 'cancel' },
  confirmIcon: { type: String, default: 'check' },
  confirmSeverity: { type: String, default: 'primary' }
})

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

// computed로 visible 바인딩 처리
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const onCancel = () => {
  emit('cancel')
  visible.value = false
}

const onConfirm = () => {
  emit('confirm')
  visible.value = false
}
</script>

<template>
  <Dialog
    v-model:visible="visible"
    :header="header"
    :modal="true"
    :style="{ width: '350px' }"
  >
    <div class="flex items-center justify-center">
      <i :class="useIcon(confirmIcon)" style="font-size: 2rem; margin-right: 1rem;" />
      <span>{{ message }}</span>
    </div>

    <template #footer>
      <Button
        :label="cancelLabel"
        :icon="useIcon(cancelIcon)"
        @click="onCancel"
        text
        severity="secondary"
      />
      <Button
        :label="confirmLabel"
        :icon="useIcon(confirmIcon)"
        @click="onConfirm"
        :severity="confirmSeverity"
        outlined
        autofocus
      />
    </template>
  </Dialog>
</template>
