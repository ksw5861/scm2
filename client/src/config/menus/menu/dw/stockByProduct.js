import { useIcon } from "@/composables/useIcon";

const stockProduct = {
  label: '제품 재고 현황',
  icon: useIcon('chart'),
  to: '/stock-product',
  roles: ['admin', 'employee']
};

export default stockProduct;
