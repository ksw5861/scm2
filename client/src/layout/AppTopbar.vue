<script setup>
import { ref } from 'vue';
import { useLayout } from '@/layout/composables/layout';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { useAppToast } from '@/composables/useAppToast';
import logo from '@/assets/logo.webp'
import axios from 'axios';
import AppConfigurator from './AppConfigurator.vue';

const { toggleMenu, toggleDarkMode, isDarkTheme } = useLayout();
const userStore = useUserStore();
const { toast } = useAppToast();
const router = useRouter();

const profilePopover = ref(null);

const toggleProfileMenu = (event) => {
  profilePopover.value.toggle(event);
};

const logout = async () => {
  try {
    const result = await axios.post('/api/auth/logout', {}, { withCredentials: true });

    if (result.status === 200) {
        userStore.clearUserInfo?.();
        router.push('/login');
        toast('info', '로그아웃 완료', '안전하게 로그아웃 되었습니다.');
    }
  } catch (error) {
    console.error('로그아웃 실패:', error);
    toast('error', '오류', '로그아웃 중 문제가 발생했습니다.');
  }
};

</script>

<template>
    <div class="layout-topbar">
        <div class="layout-topbar-logo-container">
            <button class="layout-menu-button layout-topbar-action" @click="toggleMenu">
                <i class="pi pi-bars"></i>
            </button>
            <router-link to="/" class="layout-topbar-logo flex items-center gap-0">
                <img :src="logo" width="36" height="24" />
                <h5 class="text-xl font-semibold m-0">Coffee Beans</h5>
            </router-link>
        </div>

        <div class="layout-topbar-actions">

            <div class="flex align-items-center" style="display: flex; align-items: center; margin-top: 2px;">
                <span class="text-base">{{ userStore.name }}님 환영합니다! :)</span>
            </div>

            <div class="layout-config-menu">
                <!-- <button type="button" class="layout-topbar-action" @click="toggleDarkMode">
                    <i :class="['pi', { 'pi-moon': isDarkTheme, 'pi-sun': !isDarkTheme }]"></i>
                </button> -->
                <div class="relative">
                    <button
                        v-styleclass="{ selector: '@next', enterFromClass: 'hidden', enterActiveClass: 'animate-scalein', leaveToClass: 'hidden', leaveActiveClass: 'animate-fadeout', hideOnOutsideClick: true }"
                        type="button"
                        class="layout-topbar-action layout-topbar-action-highlight"
                    >
                        <i class="pi pi-palette"></i>
                    </button>
                    <AppConfigurator />
                </div>
            </div>

            <button
                class="layout-topbar-menu-button layout-topbar-action"
                v-styleclass="{ selector: '@next', enterFromClass: 'hidden', enterActiveClass: 'animate-scalein', leaveToClass: 'hidden', leaveActiveClass: 'animate-fadeout', hideOnOutsideClick: true }"
            >
                <i class="pi pi-ellipsis-v"></i>
            </button>

            <div class="layout-topbar-menu hidden lg:block">
                <div class="layout-topbar-menu-content">
                    
                    <!-- <button type="button" class="layout-topbar-action">
                        <i class="pi pi-inbox"></i>
                        <span>공지사항</span>
                    </button> -->

                    <button type="button" class="layout-topbar-action" @click="logout">
                        <i class="pi pi-sign-out"></i>
                        <span>로그아웃</span>
                    </button>

                </div>
            </div>
        </div>
    </div>
</template>
