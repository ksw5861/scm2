import { useIcon } from "@/composables/useIcon";

const returnHistory = {
  label: '제품 반품 내역 조회',
  icon: useIcon('history'),
  to: '/return-history',
  roles: ['admin', 'employee']
};

export default returnHistory;
