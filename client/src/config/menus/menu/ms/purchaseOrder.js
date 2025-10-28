import { useIcon } from "@/composables/useIcon";

const purchaseOrder = {
  label: '자재 발주 조회',
  icon: useIcon('list'),
  to: '/purchaseOrder',
  roles: ['admin', 'employee']
};

export default purchaseOrder ;
