import { useIcon } from "@/composables/useIcon";

const orderInquiry = {
  label: '제품 주문 내역 조회',
  icon: useIcon('list'),
  to: '/order-inquiry',
  roles: ['admin', 'employee']
};
export default orderInquiry;
