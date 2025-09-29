import { useIcon } from '@/composables/useIcon';

const vendor = {
  label: '거래처 관리',
  icon: useIcon('info'),
  to: '/vendor',
  roles: ['admin', 'employee']
};

export default vendor;
