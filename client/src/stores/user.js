import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null);

  // Getters
  const name = computed(() => userInfo.value?.name || '');
  const code = computed(() => userInfo.value?.code || '');
  const accountId = computed(() => userInfo.value?.accountId || '');
  const role = computed(() => userInfo.value?.role || '');
  const isLoggedIn = computed(() => !!userInfo.value);

  // Actions
  const setUserInfo = (data) => {
    userInfo.value = data;
  };

  const clearUserInfo = () => {
    userInfo.value = null;
  };

  return {
    userInfo,
    name,
    code,
    accountId,
    role,
    isLoggedIn,
    setUserInfo,
    clearUserInfo,
  };
});
