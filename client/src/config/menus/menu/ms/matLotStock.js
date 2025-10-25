import { useIcon } from "@/composables/useIcon";

const matLotStock = {
  label: '자재 재고 현황',
  icon: useIcon('pie'),
  to: '/matLotStock',
  roles: ['admin', 'employee']
};

export default matLotStock;
