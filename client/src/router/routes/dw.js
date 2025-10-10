import AppLayout from '@/layout/AppLayout.vue';

const dw = {
  component: AppLayout,
  children: [
    {
      path: '/product-inbound',
      name: '제품입고',
      component: () => import('@/views/pages/dw/ProductInbound.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    {
      path: '/order-approval',
      name: '주문승인',
      component: () => import('@/views/pages/dw/OrderApproval.vue'),
      meta: { requiresAuth: true, roles: ['admin', 'employee'] }
    },
    {
      path: '/order-inquiry',
      name: '주문조회',
      component: () => import('@/views/pages/dw/OrderInquiry.vue'),
      meta: { requiresAuth: true, roles: ['admin', 'employee'] }
    },
    {
      path: '/order-shipment',
      name: '제품출고',
      component: () => import('@/views/pages/dw/OrderShipment.vue'),
      meta: { requiresAuth: true, roles: ['admin', 'employee'] }
    },
    {
      path: '/return-process',
      name: '반품승인처리',
      component: () => import('@/views/pages/dw/ReturnProcess.vue'),
      meta: { requiresAuth: true, roles: ['admin', 'employee'] }
    },
    {
      path: '/return-History',
      name: '반품이력',
      component: () => import('@/views/pages/dw/ReturnHistory.vue'),
      meta: { requiresAuth: true, roles: ['admin', 'employee'] }
    },
    {
      path: '/ship-plan',
      name: '출하지시',
      component: () => import('@/views/pages/dw/ShipPlan.vue'),
      meta: { requiresAuth: true, roles: ['admin', 'employee'] }
    },
    { path: '/ship-register',
      name: '출하등록', 
      component: () => import('@/views/pages/dw/ShipRegister.vue'),
      meta: { requiresAuth: true, roles: ['admin', 'employee'] }
    },
    { path: '/account-ledger',
      name: '거래처원장', 
      component: () => import('@/views/pages/dw/AccountLedger.vue'),
      meta: { requiresAuth: true, roles: ['admin', 'employee'] }
    }
  ]
};

export default dw;
