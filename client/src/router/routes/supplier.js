import AppLayout from '@/layout/AppLayout.vue';

const supplier = {
  component: AppLayout,
  children: [
    {
      path: '/requstList',
      name: '자재요청 조회',
      component: () => import('@/views/pages/supplier/RequestList.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    {
      path: '/matSupply',
      name: '출고 승인',
      component: () => import('@/views/pages/supplier/matSupply.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    {
      path: '/matShipment',
      name: '출고 등록',
      component: () => import('@/views/pages/supplier/MatShipment.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    }
  ]
};

export default supplier;
