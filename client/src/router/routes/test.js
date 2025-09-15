import AppLayout from "@/layout/AppLayout.vue";

const Test = {
    path: '/',
    component: AppLayout,
    children: [
        {
            path: '/test',
            name: 'Test',
            component: () => import('@/views/pages/Test.vue')
        }
    ]
};

export default Test;
