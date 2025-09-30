import { useIcon } from '@/composables/useIcon';

const employees =

      {
        label: '사원 관리',
        icon: useIcon('employees'),
        to: '/employees',
        roles: ['admin']
      }

export default employees;
