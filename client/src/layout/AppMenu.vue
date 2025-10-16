<script setup>
import { computed } from 'vue';
import { useUserStore } from '@/stores/user';
import AppMenuItem from './AppMenuItem.vue';
import SideBarMenu from '@/config/menus/sideBar';
import { filterMenuByRole } from '@/utils/menuFilter';

const userStore = useUserStore();
const userRoles = computed(() => userStore.role);

const filteredMenu = computed(() =>
  filterMenuByRole(SideBarMenu, userRoles.value)
);

const model = computed(() => [...filteredMenu.value]);
</script>

<template>
    <ul class="layout-menu">
        <template v-for="(item, i) in model" :key="item">
            <app-menu-item v-if="!item.separator" :item="item" :index="i"></app-menu-item>
            <li v-if="item.separator" class="menu-separator"></li>
        </template>
    </ul>
</template>

<style lang="scss" scoped></style>
