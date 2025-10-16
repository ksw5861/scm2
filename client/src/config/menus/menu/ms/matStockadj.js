import { useIcon } from "@/composables/useIcon";

const matStockadj = {
  label: '재고 조정',
  icon: useIcon('wrench'),
  to: '/matStockadj',
  roles: ['admin', 'employee']
};

export default matStockadj;
