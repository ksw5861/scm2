import { useIcon } from "@/composables/useIcon";

const materialReceip = {
  label: '자재 입고 등록',
  icon: useIcon('add'),
  to: '/MaterialReceip',
  roles: ['admin', 'employee']
};

export default materialReceip;
