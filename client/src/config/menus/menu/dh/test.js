import { useIcon } from '@/composables/useIcon';

const test =

      {
        label: '테스트',
        icon: useIcon('info'),
        to: '/test',
        roles: ['admin']
      }

export default test;
