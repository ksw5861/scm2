<script setup>
import { defineProps, defineEmits, ref, watch } from 'vue';
import axios from 'axios';

const emit = defineEmits(['done', 'cancel']);
const props = defineProps({
  item: [String, Number]
});

const isNew = ref(!props.item);
const formData = ref({
  title: '',
  content: '',
  selectedBannerImg: null
});

watch(
  () => props.item,
  async (noticeNo) => {
    if (!noticeNo) {
      isNew.value = true;
      formData.value = { title: '', content: '', selectedBannerImg: null };
    } else {
      isNew.value = false;
      try {
        const { data } = await axios.get(`/api/notice/${noticeNo}`);
        formData.value = {
          title: data.data.title,
          content: data.data.content,
          selectedBannerImg: null
        };
      } catch {
        emit('cancel');
      }
    }
  },
  { immediate: true }
);

const save = async () => {
  try {
    const fd = new FormData();
    fd.append('title', formData.value.title);
    fd.append('content', formData.value.content);
    if (formData.value.selectedBannerImg) {
      fd.append('selectedBannerImg', formData.value.selectedBannerImg);
    }
    if (!isNew.value) {
      fd.append('noticeNo', props.item);
    }

    await axios({
      method: isNew.value ? 'post' : 'put',
      url: '/api/notice',
      data: fd,
      headers: { 'Content-Type': 'multipart/form-data' }
    });

    emit('done', isNew.value ? null : props.item);

  } catch (e) {
    alert('저장 중 오류가 발생했습니다.');
  }
};
</script>

<template>
  <div class="p-6 space-y-6">
    <h2 class="text-2xl font-bold">{{ isNew ? '공지사항 등록' : '공지사항 수정' }}</h2>

    <div class="border p-4 rounded-lg space-y-4">
      <div>제목: <input v-model="formData.title" class="border" /></div>
      <div>내용: <textarea v-model="formData.content" class="border w-full"></textarea></div>
      <div>
        배너 이미지 업로드:
        <input type="file" @change="e => formData.selectedBannerImg = e.target.files[0]" />
      </div>
    </div>

    <div class="flex justify-end mt-6 gap-2">
      <Btn label="취소" icon="cancel" color="secondary" outlined @click="emit('cancel')" />
      <Btn :label="isNew ? '등록' : '저장'" :icon="isNew ? 'add' : 'save'" outlined @click="save" />
    </div>
  </div>
</template>
