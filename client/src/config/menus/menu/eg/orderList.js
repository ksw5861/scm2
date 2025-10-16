import { useIcon } from "@/composables/useIcon";

const orderList =
    {
        label: '주문 내역 조회',
        icon: useIcon('list'),
        to: '/orderlist',
        roles: ['customer']
    };

export default orderList;
