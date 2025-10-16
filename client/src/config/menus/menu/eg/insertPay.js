import { useIcon } from "@/composables/useIcon";

const insertPay =
    {
        label: '대금 납부',
        icon: useIcon('pay'),
        to: '/insertpay',
        roles: ['customer']
    };

export default insertPay;
