import { useIcon } from '@/composables/useIcon';

const Material = {
  label: '자재 관리',
  icon: useIcon('box'),
  to: '/material',
  roles: ['admin']
};

export default Material;
