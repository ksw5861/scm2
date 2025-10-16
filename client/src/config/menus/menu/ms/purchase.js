import { useIcon } from "@/composables/useIcon";

const purchase = {
    label: '자재 발주 등록',
    icon: useIcon('cart'),
    to: '/purchase',
    roles: ['admin', 'employee']
};

export default purchase;
