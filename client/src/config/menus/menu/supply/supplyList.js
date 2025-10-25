import { useIcon } from "@/composables/useIcon";

const supplyList = {
  label: '공급 현황 조회',
  icon: useIcon('gauge'),
  to: '/supplyList',
  roles: ['supplier']
};

export default supplyList;
