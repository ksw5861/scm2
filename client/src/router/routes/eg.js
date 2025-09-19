import AppLayout from '@/layout/AppLayout.vue';

const eg = {
  component: AppLayout,
  children: [
    {
      path: '/insertorder',
      name: '주문등록(판매처)',
      component: () => import('@/views/pages/eg/InsertOrder.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    {
      path: '/orderlist',
      name: '주문목록(판매처)',
      component: () => import('@/views/pages/eg/OrderList.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    {
      path: '/insertreturn',
      name: '반품등록(판매처)',
      component: () => import('@/views/pages/eg/InsertReturn.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    {
      path: '/returnlist',
      name: '반품목록(판매처)',
      component: () => import('@/views/pages/eg/ReturnList.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    {
      path: '/insertpay',
      name: '납부등록(판매처)',
      component: () => import('@/views/pages/eg/InsertPay.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    {
      path: '/paylist',
      name: '납부목록(판매처)',
      component: () => import('@/views/pages/eg/PayList.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    },
    {
      path: '/branchdash',
      name: '대시보드',
      component: () => import('@/views/pages/eg/BranchDash.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin', 'employee']
      }
    }
  ]
};

export default eg;
