import { useIcon } from "@/composables/useIcon";

const returnProcess = {
  label: '제품 반품 승인',
  icon: useIcon('checksquere'),
  to: '/return-process',
  roles: ['admin', 'employee']
};

export default returnProcess;
