import { useIcon } from '@/composables/useIcon';

const account = {
  label: '계정 관리',
  icon: useIcon('account'),
  to: '/account',
  roles: ['admin']
};

export default account;
