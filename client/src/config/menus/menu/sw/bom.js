import { useIcon } from '@/composables/useIcon';

const Bom = {
  label: 'BOM 관리',
  icon: useIcon('bom'),
  to: '/bom',
  roles: ['admin']
};

export default Bom;
