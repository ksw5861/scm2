import AppLayout from '@/layout/AppLayout.vue';
import { createRouter, createWebHistory } from 'vue-router';
import axios from 'axios';
import { useUserStore } from '@/stores/user';

import login from './routes/login';
import dh from '@/router/routes/dh';
import ms from '@/router/routes/ms';
import sw from '@/router/routes/sw';
import eg from '@/router/routes/eg';
import dw from '@/router/routes/dw';
import supplier from '@/router/routes/supplier';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '',
      redirect: { name: 'dashboard' }
    },
    {
      path: '/',
      component: AppLayout,
      children: [
        {
          path: '/',
          name: 'dashboard',
          component: () => import('@/views/Dashboard.vue')
        }
      ]
    },
    login,
    dh,
    ms,
    sw,
    eg,
    dw,
    supplier
  ]
});

router.beforeEach(async (to, from, next) => {
  const publicPages = ['/login', '/devlogin'];
  const changePasswordPath = '/change-password';
  const userStore = useUserStore();

  if (publicPages.includes(to.path)) {
    try {
      const { data } = await axios.get('/api/auth/me', { withCredentials: true });

      if (data && data.accountId) {
        userStore.setUserInfo(data);

        if (to.path === '/login') {
          if (data.tempPassword === 'Y') {
            return next(changePasswordPath);
          } else {
            return next('/');
          }
        }
      }
    } catch (e) {}
    return next();
  }

  try {
    const { data } = await axios.get('/api/auth/me', { withCredentials: true });

    if (!data || !data.accountId) {
      return next('/login');
    }

    userStore.setUserInfo(data);

    if (data.tempPassword === 'Y' && to.path !== changePasswordPath) {
      return next(changePasswordPath);
    }

    const showAllMenus = import.meta.env.VITE_SHOW_ALL_MENUS === 'true';

    // 권한 체크
    if (!showAllMenus) {
      const routeRoles = to.meta?.roles;
      if (routeRoles && routeRoles.length > 0) {
        const userRoles = userStore.role; // ['admin'], ['employee'], ...

        const hasAccess = routeRoles.some((requiredRole) =>
          userRoles.includes(requiredRole)
        );

        if (!hasAccess) {
          alert('권한이 없습니다.');
          window.history.back();
          return;
        }
      }
    }

    return next();
  } catch (error) {
    return next('/login');
  }
});

export default router;
