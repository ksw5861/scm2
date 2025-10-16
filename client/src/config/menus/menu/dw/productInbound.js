import { useIcon } from "@/composables/useIcon";

const productInbound = {
  label: '제품 입고 등록',
  icon: useIcon('box'),
  to: '/product-inbound',
  roles: ['admin', 'employee']
};

export default productInbound;
