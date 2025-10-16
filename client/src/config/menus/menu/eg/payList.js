import { useIcon } from "@/composables/useIcon";

const payList =

    {
        label: '납부 내역 조회',
        icon: useIcon('dollar'),
        to: '/paylist',
        roles: ['customer']
    };

export default payList;
