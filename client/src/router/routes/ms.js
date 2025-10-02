import AppLayout from '@/layout/AppLayout.vue';

const ms = {
  component: AppLayout,
  children: [
    {
      path: '/productionplan',
      name: '생산계획 등록',
      component: () => import('@/views/pages/ms/ProductionPlan.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    {
      path: '/purchase',

      name: '주문 등록',

      component: () => import('@/views/pages/ms/Purchase.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    {
      path: '/purchaseList',
      name: '주문 목록',
      component: () => import('@/views/pages/ms/PurchaseList.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    {
      path: '/MaterialReceip',
      name: '입고 등록',
      component: () => import('@/views/pages/ms/MaterialReceip.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    {
      path: '/MatUnload',
      name: '하차 등록',
      component: () => import('@/views/pages/ms/MatUnload.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    }
  ]
};

export default ms;
