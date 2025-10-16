import { useIcon } from "@/composables/useIcon";

//ms/material.js -> master.js -> sidebar.vue 순서로 구조화.
const productionPlan = {
  label: '생산 계획 등록',
  icon: useIcon('calendar'),
  to: '/productionplan',
  roles: ['admin', 'employee']
};

export default productionPlan;
