import { useIcon } from "@/composables/useIcon";

const insertReturn =
    {
        label: '반품 등록',
        icon: useIcon('undo'),
        to: '/insertreturn',
        roles: ['customer']
    };

export default insertReturn;
