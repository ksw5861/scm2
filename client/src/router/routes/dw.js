import AppLayout from '@/layout/AppLayout.vue';

const dw = {
  component: AppLayout,
  children: [
    {
      path: '/product-inbound',
      name: '본사 공급처 제품 입고',
      component: () => import('@/views/pages/dw/ProductInbound.vue'),
      meta: {
        breadcrumb: {
          parent: '입고 관리'
        },
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    {
      path: '/order-approval',
      name: '판매처 주문 승인',
      component: () => import('@/views/pages/dw/OrderApproval.vue'),
      meta: {
        breadcrumb: {
          parent: '판매처 주문 관리'
        },
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    {
      path: '/order-inquiry',
      name: '본사 판매처 주문 내역 조회',
      component: () => import('@/views/pages/dw/OrderInquiry.vue'),
      meta: {
        breadcrumb: {
          parent: '판매처 주문 관리'
        },
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    {
      path: '/return-process',
      name: '판매처 반품 승인',
      component: () => import('@/views/pages/dw/ReturnProcess.vue'),
      meta: {
        breadcrumb: {
          parent: '판매처 반품 관리'
        },
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    {
      path: '/return-History',
      name: '본사 판매처 반품 내역 조회',
      component: () => import('@/views/pages/dw/ReturnHistory.vue'),
      meta: {
        breadcrumb: {
          parent: '판매처 반품 관리'
        },
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    {
      path: '/ship-plan',
      name: '본사 출하 지시',
      component: () => import('@/views/pages/eg/GoDel.vue'),
      meta: {
        breadcrumb: {
          parent: '출하 관리'
        },
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    { path: '/ship-register',
      name: '본사 출하 등록',
      component: () => import('@/views/pages/eg/GoneDel.vue'),
      meta: {
        breadcrumb: {
          parent: '출하 관리'
        },
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    { path: '/account-ledger',
      name: '본사 거래처 원장',
      component: () => import('@/views/pages/dw/AccountLedger.vue'),
      meta: {
        breadcrumb: {
          parent: '거래처 관리'
        },
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    }
  ]
};

export default dw;
