import { useIcon } from "@/composables/useIcon";

const matShipment = {
  label: '출고 등록',
  icon: useIcon('truck'),
  to: '/matShipment',
  roles: ['supplier']
};

export default matShipment;
