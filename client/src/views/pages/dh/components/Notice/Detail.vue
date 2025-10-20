<script setup>
import { ref, watch, defineProps, defineEmits } from 'vue';
import axios from 'axios';
import { useUserStore } from '@/stores/user';

const emit = defineEmits(['back', 'edit']);

const props = defineProps({
  item: [String, Number]
});

const userStore = useUserStore();
const detail = ref(null);

watch(
  () => props.item,
  async (noticeNo) => {
    if (!noticeNo) {
      detail.value = null;
      return;
    }

    try {
      const { data } = await axios.get(`/api/notice/${noticeNo}`);
      detail.value = data.data;
      console.log("조회됨:", detail.value);
    } catch (err) {
      console.error('상세 정보 조회 중 오류:', err);
      emit('back');
    }
  },
  { immediate: true }
);
</script>

<template>

    <div v-if="detail" class="w-full mt-4">
        <div class="card">
            <div class="flex">
                <Btn
                    icon="left"
                    color="secondary"
                    label="뒤로 가기"
                    outlined
                    @click="emit('back')"
                />
            </div>

            <div class="flex justify-between mt-4">
                <div class="text-2xl font-bold">{{ detail.title }}</div>
                <Btn
                    v-if="['admin', 'employee'].some(role => userStore.role?.includes(role))"
                    label="수정하기"
                    icon="edit"
                    color="warn"
                    @click="emit('edit')"
                    outlined
                />
            </div>

            <div class="flex justify-between text-gray-500 text-sm mt-4">
                <div>작성자 : {{ detail.author }}</div>
                <div>{{ detail.createdAt }}</div>
            </div>

            <Divider />

            <div v-if="detail.noticeNo" class="mt-2">
                <img
                    :src="`/api/notice-banner-img/${detail.noticeNo}`"
                    alt="공지 이미지"
                    class="w-full rounded-lg"
                />
                <div class="py-4 text-lg whitespace-pre-line">
                    {{ detail.content }}
                </div>
            </div>
        </div>
    </div>
    <div v-else class="text-center p-10 text-gray-500">
        상세 정보를 불러오는 중이거나, 항목이 존재하지 않습니다.
    </div>

</template>
