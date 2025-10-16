import { useIcon } from "@/composables/useIcon";

const insertOrder =
    {
        label: '주문 등록',
        icon: useIcon('cart'),
        to: '/insertorder',
        roles: ['customer']
    };

export default insertOrder;
