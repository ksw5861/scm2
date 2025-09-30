import { useIcon } from '@/composables/useIcon';

const wareHouse = {
  label: '창고 관리',
  icon: useIcon('inventory'),
  to: '/warehouse',
  roles: ['admin']
};

export default wareHouse;
