import { useIcon } from "@/composables/useIcon";

const matLotStock = {
  label: '재고 현황',
  icon: useIcon('chart'),
  to: '/matLotStock',
  roles: ['admin', 'employee']
};

export default matLotStock;
