import { useIcon } from '@/composables/useIcon';

const vendor = {
  label: '거래처 관리',
  icon: useIcon('vendor'),
  to: '/vendor',
  roles: ['admin']
};

export default vendor;
