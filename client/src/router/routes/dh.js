import AppLayout from "@/layout/AppLayout.vue";

const Test = {
    path: '/',
    component: AppLayout,
    children: [
        {
            path: '/warehouse',
            name: '창고',
            component: () => import('@/views/pages/Warehouse.vue'),
            meta: {
                requiresAuth: true,
                roles: ['admin', 'employee']
            }
        }
    ]
};

export default Test;
