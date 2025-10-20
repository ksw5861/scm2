<script setup>
import { ref, computed } from 'vue';
import { useRoute } from 'vue-router';
import { useIcon } from '@/composables/useIcon';

import List from './components/Notice/List.vue';
import Detail from './components/Notice/Detail.vue';
import Form from './components/Notice/Form.vue';

const route = useRoute();
const icons = { home: useIcon('home') };

const breadcrumbHome = { icon: icons.home, to: '/' };
const breadcrumbItems = computed(() => {
  const matched = route.matched.filter(r => r.meta);
  if (!matched.length) return [];
  const current = matched[matched.length - 1];
  return [{ label: String(current.name || ''), to: route.fullPath }];
});

const mode = ref('list');
const selected = ref(null);

const page = ref({
  page: 1,
  size: 10,
  totalElements: 0,
});

const searchParams = ref({
  title: ''
});

const updatePage = (newPage) => {
  page.value = { ...page.value, ...newPage };
};

const updateSearch = (newSearch) => {
  searchParams.value = { ...searchParams.value, ...newSearch };
};

const goDetail = (noticeNo) => {
  selected.value = noticeNo;
  mode.value = 'detail';
};

const goCreate = () => {
  selected.value = null;
  mode.value = 'form';
};

const goList = () => {
  selected.value = null;
  mode.value = 'list';
};

const goEdit = () => {
  mode.value = 'form';
};

const cancelForm = () => {
  mode.value = selected.value ? 'detail' : 'list';
};
</script>

<template>
  <Fluid>
    <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />

    <List
      v-if="mode === 'list'"
      :page="page"
      :searchParams="searchParams"
      @update-page="updatePage"
      @update-search="updateSearch"
      @select="goDetail"
      @create="goCreate"
    />

    <Detail
      v-else-if="mode === 'detail'"
      :item="selected"
      @back="goList"
      @edit="goEdit"
    />

    <Form
      v-else-if="mode === 'form'"
      :item="selected"
      @done="goList"
      @cancel="cancelForm"
    />

  </Fluid>
</template>
