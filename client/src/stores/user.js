import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null);

  // Role mapping
  const roleMap = {
    0: ['admin'],
    1: ['employee'],
    2: ['customer'],
    3: ['supplier'],
    4: ['customer', 'supplier'],
  };

  // Getters
  const name = computed(() => userInfo.value?.name || '');
  const code = computed(() => userInfo.value?.code || '');
  const accountId = computed(() => userInfo.value?.accountId || '');
  const role = computed(() => userInfo.value?.role || []);
  const tempPassword = computed(() => userInfo.value?.tempPassword || '');
  const isLoggedIn = computed(() => !!userInfo.value);

  // Actions
  const setUserInfo = (data) => {
    const mappedRole = roleMap[data.role] || [];
    userInfo.value = {
      ...data,
      role: mappedRole,
    };
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
    tempPassword,
    isLoggedIn,
    setUserInfo,
    clearUserInfo,
  };
}, {
  persist: true
});