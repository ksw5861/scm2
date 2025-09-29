const login = {
  children: [
    {
      path: '/login',
      name: '로그인',
      component: () => import('@/views/pages/dh/Login.vue'),
      meta: {
        requiresAuth: false,
      }
    },
    {
      path: '/change-password',
      name: '비밀번호 변경',
      component: () => import('@/views/pages/dh/ChangePassword.vue'),
      meta: {
        requiresAuth: true
      }
    }
  ]
};

export default login;
