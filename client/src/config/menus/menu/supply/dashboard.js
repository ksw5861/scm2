import { useIcon } from "@/composables/useIcon";

const spDashboard = {
  label: '대시 보드',
  icon: useIcon('chartline'),
  to: '/supplyDash',
  roles: ['supplier']
};

export default spDashboard;
