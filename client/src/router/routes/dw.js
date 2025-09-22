import AppLayout from '@/layout/AppLayout.vue';

const dw = {
  component: AppLayout,
  children: [
    {
      path: '/testdw', //localhost:5173/testdw
      name: '창고',
      component: () => import('@/views/pages/dw/test.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
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
    }
  ]
};

export default dw;
