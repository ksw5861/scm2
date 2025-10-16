import { useIcon } from "@/composables/useIcon";

const purchaseList = {
  label: '자재 발주 내역 조회',
  icon: useIcon('list'),
  to: '/purchaseList',
  roles: ['admin', 'employee']
};

export default purchaseList;
