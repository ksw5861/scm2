import { useIcon } from "@/composables/useIcon";

const returnList =

      {
        label: '반품 내역 조회',
        icon: useIcon('history'),
        to: '/returnlist',
        roles: ['customer']
      };

export default returnList;
