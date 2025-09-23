<script setup>
import { useRoute } from 'vue-router';
import { computed, ref, reactive, onMounted } from 'vue';
import { useIcon } from '@/composables/useIcon';
import axios from 'axios';

const route = useRoute();

const iconInfo = useIcon('info');
const iconList = useIcon('list');
const iconEmployee = useIcon('employee');
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
  { label: '사원명', field: 'name' },
  { label: '연락처', field: 'phone' },
  { label: '사원 번호', field: 'employeeId' }
];

const searchParams = reactive({
  empName: '',
  phone: '',
  empId: '',
  status: [],      // 재직 상태 다중 선택 (숫자 배열)
  isActive: []     // 사용 여부 다중 선택 (문자 배열)
});

// 재직 상태 체크박스 목록 (DB 값과 라벨)
const statusOptions = [
  { label: '재직', value: 1 },
  { label: '퇴사', value: 2 },
  { label: '휴직', value: 3 }
];

// 사용 여부 체크박스 목록
const isActiveOptions = [
  { label: '사용', value: 'Y' },
  { label: '미사용', value: 'N' }
];

const employees = ref([]);
const loading = ref(false);
const page = ref({
  page: 1,
  size: 10,
  totalElements: 0
});

const fetchEmployeeList = async () => {
  loading.value = true;
  try {
    const params = {
      page: page.value.page,
      size: page.value.size,
      ...searchParams
    };
    const { data } = await axios.get('/api/employeelist', { params });

    employees.value = data.data ?? data.items;
    page.value = data.page ?? {
      page: params.page,
      size: params.size,
      totalElements: 0
    };
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  page.value.page = 1;  // 검색 시 페이지 1로 초기화
  fetchEmployeeList();
};

const handlePageChange = ({ page: newPage, size }) => {
  page.value.page = newPage;
  page.value.size = size;
  fetchEmployeeList();
};

// 초기 로딩
onMounted(() => {
  fetchEmployeeList();
});
</script>

<template>
  <Fluid>
    <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />

    <SearchCard
      title="사원 검색"
      @search="handleSearch"
      @reset="() => {
        searchParams.empName = '';
        searchParams.phone = '';
        searchParams.empId = '';
        searchParams.status = [];
        searchParams.isActive = [];
        page.page = 1;
        fetchEmployeeList();
      }"
    >
      <div class="flex flex-wrap w-full">
        <div class="flex flex-col gap-2 p-2 w-full xl:w-1/6 lg:w-1/2">
          <InputGroup>
            <InputGroupAddon><i :class="iconEmployee" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchParams.empName" inputId="name" />
              <label for="name">사원명</label>
            </IftaLabel>
          </InputGroup>
        </div>

        <div class="flex flex-col gap-2 p-2 w-full xl:w-1/6 lg:w-1/2">
          <InputGroup>
            <InputGroupAddon><i :class="iconPhone" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchParams.phone" inputId="phone" />
              <label for="phone">휴대전화 번호</label>
            </IftaLabel>
          </InputGroup>
        </div>

        <div class="flex flex-col gap-2 p-2 w-full xl:w-1/6 lg:w-1/2">
          <InputGroup>
            <InputGroupAddon><i :class="iconId" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchParams.empId" inputId="employeeId" />
              <label for="employeeId">사원 번호</label>
            </IftaLabel>
          </InputGroup>
        </div>

        <!-- 재직 상태 체크박스 그룹 -->
        <div class="flex flex-col gap-2 p-2 w-full xl:w-1/6 lg:w-1/2">
          <label class="font-semibold mb-1">재직 상태</label>
          <div v-for="opt in statusOptions" :key="opt.value" class="flex items-center gap-2">
            <input
              type="checkbox"
              :id="'status-' + opt.value"
              :value="opt.value"
              v-model="searchParams.status"
            />
            <label :for="'status-' + opt.value">{{ opt.label }}</label>
          </div>
        </div>

        <!-- 사용 여부 체크박스 그룹 -->
        <div class="flex flex-col gap-2 p-2 w-full xl:w-1/6 lg:w-1/2">
          <label class="font-semibold mb-1">사용 여부</label>
          <div v-for="opt in isActiveOptions" :key="opt.value" class="flex items-center gap-2">
            <input
              type="checkbox"
              :id="'isActive-' + opt.value"
              :value="opt.value"
              v-model="searchParams.isActive"
            />
            <label :for="'isActive-' + opt.value">{{ opt.label }}</label>
          </div>
        </div>
      </div>
    </SearchCard>

    <div class="flex flex-col md:flex-row w-full gap-4 mt-4">
      <div class="w-full xl:w-5/12">
        <div class="card flex flex-col">
          <div class="font-semibold text-xl flex items-center gap-4">
            <span :class="[iconList, 'text-xl']"></span>
            사원 목록
          </div>
          <Divider />
          <DTable
            :columns="employeeColumns"
            :data="employees"
            :page="page"
            :loading="loading"
            dataKey="employeeId"
            @page-change="handlePageChange"
          />
        </div>
      </div>

      <div class="w-full xl:w-7/12">
        <div class="card flex flex-col">
          <div class="font-semibold text-xl flex items-center gap-4">
            <span :class="[iconInfo, 'text-xl']"></span>
            사원 상세 정보
          </div>
          <Divider />
          <!-- 상세 정보 내용 -->
        </div>
      </div>
    </div>
  </Fluid>
</template>
