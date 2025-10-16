import { useIcon } from "@/composables/useIcon";

const matSupply = {
  label: '출고 승인',
  icon: useIcon('checksquere'),
  to: '/matSupply',
  roles: ['supplier']
};

export default matSupply;
