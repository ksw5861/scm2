import AppLayout from '@/layout/AppLayout.vue';

const supplier = {
  component: AppLayout,
  children: [
    {
      path: '/requstList',
      name: '공급처 주문 조회',
      component: () => import('@/views/pages/supplier/RequestList.vue'),
      meta: {
        breadcrumb: {
          parent: '공급처'
        },
        requiresAuth: true,
        roles: ['supplier']
      }
    },
    {
      path: '/matSupply',
      name: '공급처 출고 승인',
      component: () => import('@/views/pages/supplier/matSupply.vue'),
      meta: {
        breadcrumb: {
          parent: '공급처'
        },
        requiresAuth: true,
        roles: ['supplier']
      }
    },
    {
      path: '/matShipment',
      name: '공급처 출고 등록',
      component: () => import('@/views/pages/supplier/MatShipment.vue'),
      meta: {
        breadcrumb: {
          parent: '공급처'
        },
        requiresAuth: true,
        roles: ['supplier']
      }
    },
    {
      path: '/supplyList',
      name: '공급처 공급 내역 조회',
      component: () => import('@/views/pages/supplier/SupplyList.vue'),
      meta: {
        breadcrumb: {
          parent: '공급처'
        },
        requiresAuth: true,
        roles: ['supplier']
      }
    },
    {
      path: '/supplyDash',
      name: '공급처 대시보드',
      component: () => import('@/views/pages/supplier/SupplierDashboard.vue'),
      meta: {
        breadcrumb: {
          parent: '공급처'
        },
        requiresAuth: true,
        roles: ['supplier']
      }
    }
  ]
};

export default supplier;
