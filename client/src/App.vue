<script setup>
import Toast from 'primevue/toast';
import axios from 'axios';
import { useUserStore } from './stores/user';
import { useRouter } from 'vue-router';
import { onMounted } from 'vue';

const userStore = useUserStore();
const router = useRouter();

onMounted(async () => {
  try {
    const { data } = await axios.get('/api/auth/me', { withCredentials: true });
    if (data && data.accountId) {
      userStore.setUserInfo(data);
    } else {
      router.push('/login');
    }
  } catch (e) {
    router.push('/login');
  }
});
</script>

<template>
  <Toast />
  <router-view />
</template>
