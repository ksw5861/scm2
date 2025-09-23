<script setup>
import { useRoute } from 'vue-router';
import { computed, ref, reactive, onMounted } from 'vue';
import { useIcon } from '@/composables/useIcon';
import axios from 'axios';
import { useAppToast } from '@/composables/useAppToast';

const route = useRoute();
const { toast } = useAppToast();

const iconInfo = useIcon('info');
const iconAdd = useIcon('add');
const iconEdit = useIcon('edit');
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
  { label: '사원명', field: 'name', sortable: true },
  { label: '연락처', field: 'phone' },
  { label: '사원 번호', field: 'employeeId', sortable: true }
];

const searchParams = reactive({
  empName: '',
  phone: '',
  empId: '',
  status: [0],
  isActive: ['Y'],
  startHireDate: null,
  endHireDate: null,
  sortField: null,
  sortOrder: null
});

const cardMode = ref('create');

const statusOptions = [
  { label: '재직', value: 0 },
  { label: '휴직', value: 1 },
  { label: '퇴사', value: 2 }
];

const isActiveOptions = [
  { label: '사용', value: 'Y' },
  { label: '미사용', value: 'N' }
];

const employees = ref([]);
const employeeDetail = ref(null);
const loading = ref(false);
const detailLoading = ref(false);
const page = ref({
  page: 1,
  size: 8,
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
    const { data } = await axios.get('/api/employee', { params });

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

const fetchEmployeeDetail = async (employeeId) => {
  if (!employeeId) return;
  detailLoading.value = true;
  try {
    const { data } = await axios.get(`/api/employee/${employeeId}`);
    employeeDetail.value = data;
  } catch (e) {
    console.error(e);
  } finally {
    detailLoading.value = false;
  }
};

const handleSearch = () => {
  page.value.page = 1;
  employeeDetail.value = null;
  fetchEmployeeList();
};

const handlePageChange = ({ page: newPage, size }) => {
  page.value.page = newPage;
  page.value.size = size;
  fetchEmployeeList();
};

const handleSortChange = ({ sortField, sortOrder }) => {
  searchParams.sortField = sortField;
  searchParams.sortOrder = sortOrder;
  fetchEmployeeList();
};

const handleRowSelect = (employee) => {
  cardMode.value = 'view';
  fetchEmployeeDetail(employee.employeeId);
};

const handleRowUnSelect = () => {
  cardMode.value = 'create';
  employeeDetail.value = null;
};

const removeEmployee = async () => {
  if (!employeeDetail.value || !employeeDetail.value.employeeId) return;
  if (!confirm('정말로 삭제하시겠습니까?')) return;
  try {
    const result = await axios.delete(`/api/employee/${employeeDetail.value.employeeId}`);
    if (result.status === 200) {
      employeeDetail.value = null;
      cardMode.value = 'create';
      fetchEmployeeList();
      return toast('success','삭제 성공', '성공적으로 삭제되었습니다.');
    } else {
      return toast('error','삭제 실패', '삭제에 실패하였습니다.');
    }
  } catch (e) {
    console.error(e);
    return toast('error','삭제 실패', '오류가 발생하여 삭제에 실패하였습니다.');
  }
};

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
        searchParams.startHireDate = null;
        searchParams.endHireDate = null;
        page.page = 1;
        fetchEmployeeList();
      }"
    >
      <div class="flex flex-wrap w-full">
        <div class="flex flex-col gap-2 p-2 w-full xl:w-1/3 lg:w-1/2">
          <InputGroup>
            <InputGroupAddon><i :class="iconEmployee" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchParams.empName" inputId="name" />
              <label for="name">사원명</label>
            </IftaLabel>
          </InputGroup>
        </div>

        <div class="flex flex-col gap-2 p-2 w-full xl:w-1/3 lg:w-1/2">
          <InputGroup>
            <InputGroupAddon><i :class="iconPhone" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchParams.phone" inputId="phone" />
              <label for="phone">휴대전화 번호</label>
            </IftaLabel>
          </InputGroup>
        </div>

        <div class="flex flex-col gap-2 p-2 w-full xl:w-1/3 lg:w-1/2">
          <InputGroup>
            <InputGroupAddon><i :class="iconId" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchParams.empId" inputId="employeeId" />
              <label for="employeeId">사원 번호</label>
            </IftaLabel>
          </InputGroup>
        </div>

        <div class="flex flex-col gap-2 p-2 w-full xl:w-1/3 lg:w-1/2">
          <label class="font-semibold mb-1">재직 상태</label>
          <div class="flex flex-wrap gap-4">
            <div
              v-for="opt in statusOptions"
              :key="opt.value"
              class="flex items-center gap-2"
            >
              <Checkbox
                v-model="searchParams.status"
                :inputId="'status-' + opt.value"
                :value="opt.value"
              />
              <label
                :for="'status-' + opt.value"
                class="select-none cursor-pointer"
              >
                {{ opt.label }}
              </label>
            </div>
          </div>
        </div>

        <div class="flex flex-col gap-2 p-2 w-full xl:w-1/3 lg:w-1/2">
          <label class="font-semibold mb-1">사용 여부</label>
          <div class="flex flex-wrap gap-4">
            <div
              v-for="opt in isActiveOptions"
              :key="opt.value"
              class="flex items-center gap-2"
            >
              <Checkbox
                v-model="searchParams.isActive"
                :inputId="'isActive-' + opt.value"
                :value="opt.value"
              />
              <label
                :for="'isActive-' + opt.value"
                class="select-none cursor-pointer"
              >
                {{ opt.label }}
              </label>
            </div>
          </div>
        </div>
      </div>
    </SearchCard>

    <div class="flex flex-col md:flex-row w-full gap-4 mt-4">
      <div class="w-full xl:w-5/12">
        <div class="card flex flex-col">
          <div class="font-semibold text-xl flex items-center justify-between gap-4 h-10">
            <div class="flex items-center gap-4">
              <span :class="[iconList, 'text-xl']"></span>
              사원 목록
            </div>
            <div class="text-sm text-gray-400">
              총 <span class="font-semibold text-sm text-gray-700">
                
                <span v-if="page.totalElements > 0" class="font-semibold text-sm text-gray-700">
                  <CountUp :end-val="page.totalElements" />
                </span>
                <span v-else class="font-semibold text-sm text-gray-700">0</span>
                
              </span>건
            </div>
          </div>
          <Divider />
          <DTable
            :columns="employeeColumns"
            :data="employees"
            :page="page"
            :loading="loading"
            dataKey="employeeId"
            @page-change="handlePageChange"
            @sort-change="handleSortChange"
            @row-select="handleRowSelect"
            @row-unselect="handleRowUnSelect"
          />
        </div>
      </div>

      <div class="w-full xl:w-7/12">
        <div class="card flex flex-col">
          
          <div class="flex items-center justify-between h-10">
            <div class="font-semibold text-xl flex items-center gap-4">
              <span :class="[
                cardMode === 'create'
                  ? iconAdd
                  : cardMode === 'edit'
                  ? iconEdit
                  : iconInfo, 
                'text-xl'
              ]"></span>
              {{
                cardMode === 'create'
                  ? '신규 사원 등록'
                  : cardMode === 'edit'
                  ? '사원 정보 수정'
                  : '사원 상세 정보'
              }}
            </div>

            <div class="flex gap-2">
              <Btn v-if="cardMode === 'view'" icon="delete" color="danger" @click="removeEmployee" outlined>삭제</Btn>
              <Btn v-if="cardMode === 'view'" icon="edit" color="warn" @click="cardMode = 'edit'" outlined>수정</Btn>
              <Btn v-if="cardMode === 'create'" icon="add" outlined>등록</Btn>
              <Btn v-if="cardMode === 'edit'" icon="save" @click="cardMode = 'view'" outlined>저장</Btn>
            </div>
          </div>

          <Divider />

          <div v-if="cardMode === 'view'"
              class="text-center text-gray-400 py-20">
            {{ employeeDetail }}
          </div>
        </div>
      </div>

    </div>
  </Fluid>
</template>
