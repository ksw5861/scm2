import { useIcon } from "@/composables/useIcon";

const shipRegister = {
  label: '출하 등록',
  icon: useIcon('send'),
  to: '/ship-register',
  roles: ['admin', 'employee']
};

export default shipRegister;
