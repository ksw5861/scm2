import { useIcon } from "@/composables/useIcon";

//ms/material.js -> master.js -> sidebar.vue 순서로 구조화.
const planList = {
  label: '생산 계획 조회',
  icon: useIcon('list'),
  to: '/planList',
  roles: ['admin', 'employee']
};

export default planList;
