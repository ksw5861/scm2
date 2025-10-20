import { useIcon } from '@/composables/useIcon';

const notice = {
  label: '공지사항',
  icon: useIcon('bell'),
  to: '/notice',
  roles: ['admin', 'employee', 'customer', 'supplier']
};

export default notice;
