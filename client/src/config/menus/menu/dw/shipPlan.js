import { useIcon } from "@/composables/useIcon";

const shipPlan = {
  label: '출하 지시',
  icon: useIcon('truck'),
  to: '/ship-plan',
  roles: ['admin', 'employee']
};

export default shipPlan;
