import { useIcon } from "@/composables/useIcon";

const branchPos =
    {
        label: 'POS',
        icon: useIcon('pos'),
        to: '/branchpos',
        roles: ['customer']
    };

export default branchPos;
