import AppLayout from '@/layout/AppLayout.vue';

const sw = {
  component: AppLayout,
  children: [
    {
      path: '/material',
      name: '자재 관리',
      component: () => import('@/views/pages/sw/Material.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin']
      }
    },
    {
      path: '/product',
      name: '제품 관리',
      component: () => import('@/views/pages/sw/Product.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin']
      }
    },
    {
      path: '/warehouse',
      name: '창고 관리',
      component: () => import('@/views/pages/sw/WareHouse.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin']
      }
    },
    {
      path: '/bom',
      name: 'BOM 관리',
      component: () => import('@/views/pages/sw/Bom.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin']
      }
    }
  ]
};

export default sw;
