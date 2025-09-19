import AppLayout from "@/layout/AppLayout.vue";

const Test = {
    path: '/',
    component: AppLayout,
    children: [
        {
            path: '/insertorder',
            name: '주문등록(판매처)',
            component: () => import('@/views/pages/eg/InsertOrder.vue'),
            meta: {
                requiresAuth: true,
                roles: ['admin', 'employee']
            }
        }
    ]
};

export default Test;
