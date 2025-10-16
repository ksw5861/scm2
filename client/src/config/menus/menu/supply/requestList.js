import { useIcon } from "@/composables/useIcon";

const requestList = {
  label: '주문 조회',
  icon: useIcon('list'),
  to: '/requstList',
  roles: ['supplier']
};

export default requestList;
