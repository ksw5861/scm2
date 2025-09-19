import AppLayout from "@/layout/AppLayout.vue";

const dh = {
    component: AppLayout,
    children: [
        {
            path: '/warehouse',
            name: '창고',
            component: () => import('@/views/pages/dh/Warehouse.vue'),
            meta: {
                requiresAuth: true,
                roles: ['admin', 'employee']
            }
        }
    ]
};

export default dh;
