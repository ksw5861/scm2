import { useIcon } from '@/composables/useIcon';

const wareHouse =

      {
        label: '창고 관리',
        icon: useIcon('home'),
        to: '/warehouse',
        roles: ['admin', 'employee']
      }

export default wareHouse;
