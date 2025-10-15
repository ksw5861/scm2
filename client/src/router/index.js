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
        },
        {
          path: '/uikit/formlayout',
          name: 'formlayout',
          component: () => import('@/views/uikit/FormLayout.vue')
        },
        {
          path: '/uikit/input',
          name: 'input',
          component: () => import('@/views/uikit/InputDoc.vue')
        },
        {
          path: '/uikit/button',
          name: 'button',
          component: () => import('@/views/uikit/ButtonDoc.vue')
        },
        {
          path: '/uikit/table',
          name: 'table',
          component: () => import('@/views/uikit/TableDoc.vue')
        },
        {
          path: '/uikit/list',
          name: 'list',
          component: () => import('@/views/uikit/ListDoc.vue')
        },
        {
          path: '/uikit/tree',
          name: 'tree',
          component: () => import('@/views/uikit/TreeDoc.vue')
        },
        {
          path: '/uikit/panel',
          name: 'panel',
          component: () => import('@/views/uikit/PanelsDoc.vue')
        },
        {
          path: '/uikit/overlay',
          name: 'overlay',
          component: () => import('@/views/uikit/OverlayDoc.vue')
        },
        {
          path: '/uikit/media',
          name: 'media',
          component: () => import('@/views/uikit/MediaDoc.vue')
        },
        {
          path: '/uikit/message',
          name: 'message',
          component: () => import('@/views/uikit/MessagesDoc.vue')
        },
        {
          path: '/uikit/file',
          name: 'file',
          component: () => import('@/views/uikit/FileDoc.vue')
        },
        {
          path: '/uikit/menu',
          name: 'menu',
          component: () => import('@/views/uikit/MenuDoc.vue')
        },
        {
          path: '/uikit/charts',
          name: 'charts',
          component: () => import('@/views/uikit/ChartDoc.vue')
        },
        {
          path: '/uikit/misc',
          name: 'misc',
          component: () => import('@/views/uikit/MiscDoc.vue')
        },
        {
          path: '/uikit/timeline',
          name: 'timeline',
          component: () => import('@/views/uikit/TimelineDoc.vue')
        },
        {
          path: '/pages/empty',
          name: 'empty',
          component: () => import('@/views/pages/Empty.vue')
        },
        {
          path: '/pages/crud',
          name: 'crud',
          component: () => import('@/views/pages/Crud.vue')
        },
        {
          path: '/documentation',
          name: 'documentation',
          component: () => import('@/views/pages/Documentation.vue')
        }
      ]
    },
    {
      path: '/landing',
      name: 'landing',
      component: () => import('@/views/pages/Landing.vue')
    },
    {
      path: '/pages/notfound',
      name: 'notfound',
      component: () => import('@/views/pages/NotFound.vue')
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
          return next('/');
        }
      }
    }

    return next();
  } catch (error) {
    return next('/login');
  }
});

export default router;
