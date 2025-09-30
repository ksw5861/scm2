import AppLayout from "@/layout/AppLayout.vue";

const ms = {
     component: AppLayout,
     children: [
        {
            path: '/productionplan',
            name: '생산계획 등록',
            component: () => import('@/views/pages/ms/ProductionPlan.vue'),
            meta: {
                requiresAuth: true,
                roles: ['admin', 'employee']
            }
        },
        {
            path: '/purchase',
            name: '주문등록',
            component: () => import('@/views/pages/ms/Purchase.vue'),
            meta: {
                requiresAuth: true,
                roles: ['admin', 'employee']
            }
        },
        {
            path: '/purchaseList',
            name: '주문 목록',
            component: () => import('@/views/pages/ms/PurchaseList.vue'),
            meta: {
                requiresAuth: true,
                roles: ['admin', 'employee']
            }
        },

     ]
};

export default ms;
