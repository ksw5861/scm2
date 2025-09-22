<script setup>
import { useRoute } from 'vue-router';
import { computed, ref, watch, reactive, onMounted } from 'vue';
import { useIcon } from '@/composables/useIcon';
import axios from 'axios';

const route = useRoute();

const iconInfo = useIcon('info');
const iconEmployee = useIcon('employee');
const iconEmail = useIcon('email');
const iconPhone = useIcon('phone');
const iconId = useIcon('id');

const breadcrumbHome = { icon: useIcon('home'), to: '/' };

const breadcrumbItems = computed(() => {
  const matched = route.matched.filter(r => r.meta);
  if (!matched.length) return [];
  const current = matched[matched.length - 1];
  const parentLabel = current.meta.breadcrumb?.parent || '';
  const currentLabel = current.name || '';
  return [
    { label: parentLabel },
    { label: currentLabel, to: route.fullPath }
  ];
});

const employeeColumns = [
  { label: '사원 번호', field: 'employeeId' },
  { label: '사원명', field: 'name' }
];

const searchParams = reactive({
  name: '',
  email: '',
  phone: '',
  employeeId: ''
});

const employees = ref([]);
const totalRecords = ref(0);
const loading = ref(false);
const currentPage = ref(1);
const rowsPerPage = ref(10);

const fetchEmployees = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: rowsPerPage.value,
      ...searchParams
    };
    const { data } = await axios.get('/api/employees', { params });
    employees.value = data.items;
    totalRecords.value = data.total;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  currentPage.value = 1;
  fetchEmployees();
};

onMounted(() => {
  fetchEmployees();
});

const handlePageChange = ({ page }) => {
  currentPage.value = page;
  fetchEmployees();
};

</script>

<template>
  <Fluid>
    <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />

    <SearchCard
      title="사원 검색"
      @search="handleSearch"
      @reset="
        () => {
          // 검색조건 초기화
          searchParams.name = '';
          searchParams.email = '';
          searchParams.phone = '';
          searchParams.employeeId = '';
          currentPage.value = 1;
          fetchEmployees();
        }
      "
    >
      <div class="flex flex-wrap w-full">
        <div class="flex flex-col gap-2 p-2 w-full lg:w-1/4 md:w-1/2">
          <InputGroup>
            <InputGroupAddon>
              <i :class="iconEmployee"></i>
            </InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchParams.name" inputId="name" />
              <label for="name">사원명</label>
            </IftaLabel>
          </InputGroup>
        </div>

        <div class="flex flex-col gap-2 p-2 w-full lg:w-1/4 md:w-1/2">
          <InputGroup>
            <InputGroupAddon>
              <i :class="iconEmail"></i>
            </InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchParams.email" inputId="email" />
              <label for="email">이메일</label>
            </IftaLabel>
          </InputGroup>
        </div>

        <div class="flex flex-col gap-2 p-2 w-full lg:w-1/4 md:w-1/2">
          <InputGroup>
            <InputGroupAddon>
              <i :class="iconPhone"></i>
            </InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchParams.phone" inputId="phone" />
              <label for="phone">휴대전화 번호</label>
            </IftaLabel>
          </InputGroup>
        </div>

        <div class="flex flex-col gap-2 p-2 w-full lg:w-1/4 md:w-1/2">
          <InputGroup>
            <InputGroupAddon>
              <i :class="iconId"></i>
            </InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchParams.employeeId" inputId="employeeId" />
              <label for="employeeId">사원 번호</label>
            </IftaLabel>
          </InputGroup>
        </div>
      </div>
    </SearchCard>

    <div class="flex flex-col md:flex-row w-full gap-4 mt-4">
      <ListCard
        :columns="employeeColumns"
        :data="employees"
        :totalRecords="totalRecords"
        :loading="loading"
        :paginator="true"
        :rows="rowsPerPage"
        :currentPage="currentPage"
        @page-change="handlePageChange"
      />
      <div class="w-full lg:w-7/12 md:1/2">
        <div class="card flex flex-col">
          <div class="font-semibold text-xl flex items-center gap-4">
            <span :class="[iconInfo, 'text-xl']"></span>
            사원 상세 정보
          </div>
          <Divider />
        </div>
      </div>
    </div>
  </Fluid>
</template>
