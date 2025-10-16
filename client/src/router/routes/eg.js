import AppLayout from '@/layout/AppLayout.vue';

const eg = {
  component: AppLayout,
  children: [
    {
      path: '/insertorder',
      name: '판매처 주문 등록',
      component: () => import('@/views/pages/eg/InsertOrder.vue'),
      meta: {
        breadcrumb: {
          parent: '판매처'
        },
        requiresAuth: true,
        roles: ['customer']
      }
    },
    {
      path: '/orderlist',
      name: '판매처 주문 내역 조회',
      component: () => import('@/views/pages/eg/OrderList.vue'),
      meta: {
        breadcrumb: {
          parent: '판매처'
        },
        requiresAuth: true,
        roles: ['customer']
      }
    },
    {
      path: '/insertreturn',
      name: '판매처 반품 등록',
      component: () => import('@/views/pages/eg/InsertReturn.vue'),
      meta: {
        breadcrumb: {
          parent: '판매처'
        },
        requiresAuth: true,
        roles: ['customer']
      }
    },
    {
      path: '/returnlist',
      name: '판매처 반품 내역 조회',
      component: () => import('@/views/pages/eg/ReturnList.vue'),
      meta: {
        breadcrumb: {
          parent: '판매처'
        },
        requiresAuth: true,
        roles: ['customer']
      }
    },
    {
      path: '/insertpay',
      name: '판매처 대금 납부',
      component: () => import('@/views/pages/eg/InsertPay.vue'),
      meta: {
        breadcrumb: {
          parent: '판매처'
        },
        requiresAuth: true,
        roles: ['customer']
      }
    },
    {
      path: '/paylist',
      name: '판매처 납부 내역 조회',
      component: () => import('@/views/pages/eg/PayList.vue'),
      meta: {
        breadcrumb: {
          parent: '판매처'
        },
        requiresAuth: true,
        roles: ['customer']
      }
    },
    {
      path: '/branchdash',
      name: '판매처 대시보드',
      component: () => import('@/views/pages/eg/BranchDash.vue'),
      meta: {
        breadcrumb: {
          parent: '판매처'
        },
        requiresAuth: true,
        roles: ['customer']
      }
    },
    {
      path: '/branchpos',
      name: '판매처 POS',
      component: () => import('@/views/pages/eg/BranchPos.vue'),
      meta: {
        breadcrumb: {
          parent: '판매처'
        },
        requiresAuth: true,
        roles: ['customer']
      }
    },
    {
      path: '/possetting',
      name: '판매처 POS 설정',
      component: () => import('@/views/pages/eg/PosSetting.vue'),
      meta: {
        breadcrumb: {
          parent: '판매처'
        },
        requiresAuth: true,
        roles: ['customer']
      }
    }

  ]
};

export default eg;
