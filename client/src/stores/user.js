import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
  }),

  getters: {
    name: (state) => state.userInfo?.name || '',
    code: (state) => state.userInfo?.code || '',
    accountId: (state) => state.userInfo?.accountId || '',
    role: (state) => state.userInfo?.role || '',
    isLoggedIn: (state) => !!state.userInfo,
  },

  actions: {
    setUserInfo(data) {
      this.userInfo = data;
    },
    clearUserInfo() {
      this.userInfo = null;
    },
  },
});
