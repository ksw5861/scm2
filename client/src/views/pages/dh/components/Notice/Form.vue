<script setup>
import { defineProps, defineEmits, ref, watch } from 'vue';
import axios from 'axios';

const emit = defineEmits(['done', 'cancel']);
const props = defineProps({
  item: [String, Number]
});

const isNew = ref(!props.item);
const formData = ref({});

watch(() => props.item, (noticeNo) => {
    if (noticeNo) {
        isNew.value = false;
        formData.value = { title: '로딩 중...', content: '로딩 중...' };
    } else {
        isNew.value = true;
        formData.value = { title: '', content: '' };
    }
}, { immediate: true });

const save = async () => {
    emit('done');
};
</script>

<template>
  <div class="p-6 space-y-6">
    <h2 class="text-2xl font-bold">{{ isNew ? '공지사항 등록' : '공지사항 수정' }}</h2>

    <div class="border p-4 rounded-lg space-y-4">
      <div>제목: <input v-model="formData.title" class="border" /></div>
      <div>내용: <textarea v-model="formData.content" class="border w-full"></textarea></div>
      </div>

    <div class="flex justify-end mt-6 gap-2">
      <Btn label="취소" outlined @click="emit('cancel')" />
      <Btn :label="isNew ? '등록' : '수정'" @click="save" />
    </div>
  </div>
</template>
