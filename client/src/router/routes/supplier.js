import AppLayout from "@/layout/AppLayout.vue";

const supplier = {
     component: AppLayout,
     children: [
        {
            path: '/requstList',
            name: '자재요청 조회',
            component: () => import('@/views/pages/supplier/RequestList.vue'),
            meta: {
                requiresAuth: true,
                roles: ['admin', 'employee']
            }
        }
     ]
};

export default supplier;
