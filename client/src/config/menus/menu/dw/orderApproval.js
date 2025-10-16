import { useIcon } from "@/composables/useIcon";

const orderApproval = {
  label: '제품 주문 승인',
  icon: useIcon('checksquere'),
  to: '/order-approval',
  roles: ['admin', 'employee']
};
export default orderApproval;
