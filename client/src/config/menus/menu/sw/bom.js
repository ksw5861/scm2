import { useIcon } from '@/composables/useIcon';

const Bom = {
  label: 'bom 관리',
  icon: useIcon('info'),
  to: '/bom',
  roles: ['admin']
};

export default Bom;
