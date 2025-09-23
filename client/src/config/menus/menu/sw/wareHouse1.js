import { useIcon } from '@/composables/useIcon';

const wareHouse1 = {
  label: '창고 관리',
  icon: useIcon('home'),
  to: '/warehouse1',
  roles: ['admin']
};

export default wareHouse1;
