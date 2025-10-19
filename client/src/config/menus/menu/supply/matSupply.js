import { useIcon } from "@/composables/useIcon";

const matSupply = {
  label: '출고 지시',
  icon: useIcon('checksquere'),
  to: '/matSupply',
  roles: ['supplier']
};

export default matSupply;
