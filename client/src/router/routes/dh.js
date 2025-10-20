import AppLayout from '@/layout/AppLayout.vue';

const dh = {
  component: AppLayout,
  children: [
    {
      path: '/test',
      name: '테스트',
      component: () => import('@/views/pages/dh/test.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin']
      }
    },
    {
      path: '/notice',
      name: '공지사항',
      component: () => import('@/views/pages/dh/Notice.vue'),
      meta: {
        breadcrumb: {
          parent: '홈'
        },
        requiresAuth: true,
        roles: ['admin', 'employee', 'customer', 'supplier']
      }
    },
    {
      path: '/employees',
      name: '사원 관리',
      component: () => import('@/views/pages/dh/EmployeeManagement.vue'),
      meta: {
        breadcrumb: {
          parent: '기준 정보'
        },
        requiresAuth: true,
        roles: ['admin']
      }
    },
    {
      path: '/vendor',
      name: '거래처 관리',
      component: () => import('@/views/pages/dh/Vendor.vue'),
      meta: {
        breadcrumb: {
          parent: '기준 정보'
        },
        requiresAuth: true,
        roles: ['admin']
      }
    },
    {
      path: '/account',
      name: '계정 관리',
      component: () => import('@/views/pages/dh/AccountManagement.vue'),
      meta: {
        breadcrumb: {
          parent: '기준 정보'
        },
        requiresAuth: true,
        roles: ['admin']
      }
    }
  ]
};

export default dh;
