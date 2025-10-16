import AppLayout from '@/layout/AppLayout.vue';

const ms = {
  component: AppLayout,
  children: [
    {
      path: '/productionplan',
      name: '본사 생산 계획 등록',
      component: () => import('@/views/pages/ms/ProductionPlan.vue'),
      meta: {
        breadcrumb: {
          parent: '생산 관리'
        },
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    {
      path: '/purchase',
      name: '본사 발주 등록',
      component: () => import('@/views/pages/ms/Purchase.vue'),
      meta: {
        breadcrumb: {
          parent: '발주 관리'
        },
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    {
      path: '/purchaseList',
      name: '본사 발주 내역 조회',
      component: () => import('@/views/pages/ms/PurchaseList.vue'),
      meta: {
        breadcrumb: {
          parent: '발주 관리'
        },
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    {
      path: '/MaterialReceip',
      name: '본사 입고 등록',
      component: () => import('@/views/pages/ms/MaterialReceip.vue'),
      meta: {
        breadcrumb: {
          parent: '물류 관리'
        },
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    {
      path: '/MatUnload',
      name: '본사 하차 등록',
      component: () => import('@/views/pages/ms/MatUnload.vue'),
      meta: {
        breadcrumb: {
          parent: '물류 관리'
        },
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    {
      path: '/matOutboundRegistration',
      name: '본사 자재 출고 등록',
      component: () => import('@/views/pages/ms/matOutboundRegistration.vue'),
      meta: {
        breadcrumb: {
          parent: '물류 관리'
        },
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    {
      path: '/matLotStock',
      name: '본사 재고 현황',
      component: () => import('@/views/pages/ms/MatLotStock.vue'),
      meta: {
        breadcrumb: {
          parent: '재고 관리'
        },
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    {
      path: '/matStockadj',
      name: '본사 재고 조정',
      component: () => import('@/views/pages/ms/MatStockAdj.vue'),
      meta: {
        breadcrumb: {
          parent: '재고 관리'
        },
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    }
  ]
};

export default ms;
