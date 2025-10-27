import { useIcon } from "@/composables/useIcon";

const purchaseList = {
  label: '자재 발주 현황 조회',
  icon: useIcon('gauge'),
  to: '/purchaseList',
  roles: ['admin', 'employee']
};

export default purchaseList;
