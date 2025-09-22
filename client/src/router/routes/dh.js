import AppLayout from '@/layout/AppLayout.vue';

const dh = {
  component: AppLayout,
  children: [
    {
      path: '/warehouse',
      name: '창고 관리',
      component: () => import('@/views/pages/dh/WareHouse.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin', 'employee']
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
        roles: ['admin', 'employee']
      }
    }
  ]
};

export default dh;
