import { useIcon } from "@/composables/useIcon";

const supplyList = {
  label: '공급 내역 조회',
  icon: useIcon('history'),
  to: '/supplyList',
  roles: ['supplier']
};

export default supplyList;
