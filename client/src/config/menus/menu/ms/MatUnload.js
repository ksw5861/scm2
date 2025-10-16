import { useIcon } from "@/composables/useIcon";

const MatUnload = {
  label: '자재 하차 등록',
  icon: useIcon('send'),
  to: '/MatUnload',
  roles: ['admin', 'employee']
};

export default MatUnload;
