import { useIcon } from "@/composables/useIcon";

const posSetting =

    {
        label: 'POS 설정',
        icon: useIcon('setting'),
        to: '/possetting',
        roles: ['customer']
    };

export default posSetting;
