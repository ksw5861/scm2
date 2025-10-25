import { useIcon } from "@/composables/useIcon";

const requestResultList = {
  label: '주문 내역 조회',
  icon: useIcon('list'),
  to: '/requstResultList',
  roles: ['supplier']
};

export default requestResultList;
