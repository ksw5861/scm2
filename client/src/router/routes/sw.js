import AppLayout from '@/layout/AppLayout.vue';

const sw = {
  component: AppLayout,
  children: [
    {
      path: '/material',
      name: '자재관리',
      component: () => import('@/views/pages/sw/Material.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin']
      }
    }
  ]
};

export default sw;
