import { useIcon } from '@/composables/useIcon';

const dashboard = {
  label: '대시보드',
  icon: useIcon('chartline'),
  to: '/',
  roles: ['admin', 'employee', 'customer', 'supplier']
};

export default dashboard;
