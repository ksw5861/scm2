<script setup>
import BestSellingWidget from '@/components/dashboard/BestSellingWidget.vue';
import NotificationsWidget from '@/components/dashboard/NotificationsWidget.vue';
import RecentSalesWidget from '@/components/dashboard/RecentSalesWidget.vue';
import RevenueStreamWidget from '@/components/dashboard/RevenueStreamWidget.vue';
import StatsWidget from '@/components/dashboard/StatsWidget.vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { useAppToast } from '@/composables/useAppToast';
import axios from 'axios';

const userStore = useUserStore();
const { toast } = useAppToast();
const router = useRouter();

const logout = async () => {
  try {
    const result = await axios.post('/api/auth/logout', {}, { withCredentials: true });

    if (result.status === 200) {
        userStore.clearUserInfo?.();
        router.push('/login');
        toast('info', '로그아웃 완료', '안전하게 로그아웃 되었습니다.');
    }
  } catch (error) {
    console.error('로그아웃 실패:', error);
    toast('error', '오류', '로그아웃 중 문제가 발생했습니다.');
  }
};

</script>

<template>
  <div class="grid grid-cols-12 gap-8">
    <Btn color="danger" label="로그아웃" @click="logout"></Btn>

    <StatsWidget />

    <div class="col-span-12 xl:col-span-6">
      <RecentSalesWidget />
      <BestSellingWidget />
    </div>
    <div class="col-span-12 xl:col-span-6">
      <RevenueStreamWidget />
      <NotificationsWidget />
    </div>
  </div>
</template>
