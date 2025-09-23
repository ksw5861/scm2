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
    },
    {
      path: '/product',
      name: '제품관리',
      component: () => import('@/views/pages/sw/Product.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin']
      }
    },
    {
      path: '/warehouse1',
      name: '창고관리',
      component: () => import('@/views/pages/sw/WareHouse1.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin']
      }
    },
    {
      path: '/bom',
      name: 'bom 관리',
      component: () => import('@/views/pages/sw/Bom.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin']
      }
    }
  ]
};

export default sw;
