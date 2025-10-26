<script setup>
import { ref, watch, defineProps, defineEmits } from 'vue';
import axios from 'axios';
import { useUserStore } from '@/stores/user';
import Skeleton from 'primevue/skeleton';
import { useAppToast } from '@/composables/useAppToast';
import { useIcon } from '@/composables/useIcon';

const icons = {
  list: useIcon('list'),
};

const emit = defineEmits(['back', 'edit']);
const { toast } = useAppToast();

const props = defineProps({
  item: Object
});

const userStore = useUserStore();
const detail = ref(null);
const neighbors = ref([]);
const loading = ref(false);

const handleDelete = async () => {
  if (!confirm('정말로 이 공지사항을 삭제하시겠습니까?')) return;

  try {
    const { status } = await axios.delete(`/api/notice/${props.item.noticeNo}`); 
    if (status === 200) {
      toast('success', '삭제 성공', '공지사항이 성공적으로 삭제되었습니다.');
      emit('back');
    }
  } catch (err) {
    console.error('삭제 중 오류:', err);
    alert('공지사항 삭제에 실패했습니다. 다시 시도해주세요.');
  }
};

const fetchDetailAndNeighbors = async (notice) => {
    if (!notice || !notice.noticeNo) {
        detail.value = null;
        neighbors.value = [];
        return;
    }
    loading.value = true;
    try {
        const detailResponse = await axios.get(`/api/notice/${notice.noticeNo}`);
        detail.value = detailResponse.data.data;
        
        const neighborsResponse = await axios.get(`/api/notice/${notice.noticeNo}/neighbors`);
        neighbors.value = neighborsResponse.data.data || []; 
        
    } catch (err) {
        if (err.response && err.response.status === 204) {
             neighbors.value = [];
        } else {
             console.error('상세/인접 정보 조회 중 오류:', err);
             if (!detail.value) {
                 emit('back'); 
             }
        }
    } finally {
        loading.value = false;
    }
}

watch(
  () => props.item,
  (notice) => {
    fetchDetailAndNeighbors(notice);
  },
  { immediate: true, deep: true }
);
</script>

<template>
  <div class="w-full mt-4">
    <div class="card" style="margin-bottom: 0;">
      <div class="flex">
        <Btn icon="left" color="secondary" label="뒤로 가기" outlined @click="emit('back')" />
      </div>

      <div class="flex justify-between mt-4">
        <div class="text-2xl font-bold">{{ props.item.title }}</div>

        <div class="flex gap-2">
          <Btn
            v-if="['admin', 'employee'].some(role => userStore.role?.includes(role))"
            label="삭제"
            icon="delete"
            color="danger"
            outlined
            @click="handleDelete"
          />
          <Btn
            v-if="['admin', 'employee'].some(role => userStore.role?.includes(role))"
            label="수정"
            icon="edit"
            color="warn"
            outlined
            @click="emit('edit')"
          />
        </div>

      </div>

      <div class="flex justify-between text-gray-500 text-sm mt-4">
        <div>작성자 : {{ props.item.author }}</div>
        <div>{{ props.item.createdAt }}</div>
      </div>

      <Divider />

      <div v-if="loading" class="mt-2">
        <Skeleton width="60%" height="4rem" class="mb-4" />
        <Skeleton width="60%" height="1rem" class="mb-2" />
        <Skeleton width="60%" height="1rem" class="mb-2" />
        <Skeleton width="40%" height="1rem" class="mb-2" />
        <Skeleton width="20%" height="1rem" />
      </div>

      <div v-else-if="detail" class="mt-2">
        <img
          :src="`/api/notice-banner-img/${detail.noticeNo}`"
          alt="공지 이미지"
          class="w-full rounded-lg"
        />
        <div class="py-4 text-lg whitespace-pre-line">
          {{ detail.content }}
        </div>
      </div>

      <div v-else class="text-center p-10 text-gray-500">
        항목이 존재하지 않습니다.
      </div>
      
    </div>

    <div class="card mt-4">

      <div class="font-semibold text-lg sm:text-xl flex items-center justify-between gap-4 h-10">
        <div class="flex items-center gap-4">
          <span :class="icons.list"></span> 공지사항 목록
        </div>
      </div>

      <Divider/>

      <div v-if="loading">
          <Skeleton height="1.5rem" class="mb-2" />
          <Skeleton height="1.5rem" class="mb-2" />
          <Skeleton height="1.5rem" class="mb-2" />
          <Skeleton height="1.5rem" class="mb-2" />
          <Skeleton height="1.5rem" class="mb-2" />
      </div>
      <div v-else-if="neighbors.length > 0">
        <template v-for="neighbor in neighbors" :key="neighbor.noticeNo">
            <div v-if="neighbor.noticeNo !== detail.noticeNo" 
                  class="border-b py-2 hover:bg-gray-50 cursor-pointer rounded"
                  @click="emit('back'); emit('edit', neighbor);">
                <div class="font-medium text-gray-700">{{ neighbor.title }}</div>
                <div class="text-sm text-gray-500">{{ neighbor.createdAt }} | {{ neighbor.author }}</div>
            </div>
        </template>
      </div>
    </div>
  </div>
</template>