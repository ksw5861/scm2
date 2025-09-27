import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
  }),
  actions: {
    setUserInfo(data) {
      this.userInfo = data;
    },
    clearUserInfo() {
      this.userInfo = null;
    },
  },
});
