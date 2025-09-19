import AppLayout from '@/layout/AppLayout.vue';

const login = {
  children: [
    {
      path: '/login',
      name: '로그인',
      component: () => import('@/views/pages/dh/Login.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    }
  ]
};

export default login;
