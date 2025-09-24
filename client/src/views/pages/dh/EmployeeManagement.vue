<script setup>
import { useRoute } from 'vue-router';
import { computed, ref, reactive, onMounted, watch } from 'vue';
import axios from 'axios';
import { useIcon } from '@/composables/useIcon';
import { useAppToast } from '@/composables/useAppToast';
import { useDateFormat } from '@/composables/useFormat';

const route = useRoute();
const { toast } = useAppToast();

const defaultPhoto = 'https://i.namu.wiki/i/NB_qC6YRjH7hv6elNznBIBOBZ5AwE-PKYEWKcU03aFzGsc60bOt9KLxocyvB01OxAbOG8joW9mgkShFmTaTKsQ.webp';

// ===== 아이콘 =====
const icons = {
  info: useIcon('info'),
  add: useIcon('add'),
  edit: useIcon('edit'),
  list: useIcon('list'),
  employee: useIcon('employee'),
  phone: useIcon('phone'),
  email: useIcon('email'),
  calendar: useIcon('calendar'),
  id: useIcon('id'),
  home: useIcon('home'),
  cancel: useIcon('cancel'),
  delete: useIcon('delete'),
  refresh: useIcon('refresh'),
  save: useIcon('save')
};

// ===== BreadCrumb =====
const breadcrumbHome = { icon: icons.home, to: '/' };

const breadcrumbItems = computed(() => {
  const matched = route.matched.filter(r => r.meta);
  if (!matched.length) return [];
  const current = matched[matched.length - 1];
  return [
    { label: current.meta.breadcrumb?.parent || '' },
    { label: current.name || '', to: route.fullPath }
  ];
});

// ===== Options =====
const STATUS_OPTIONS = [
  { label: '재직', value: 0 },
  { label: '휴직', value: 1 },
  { label: '퇴사', value: 2 }
];

const ACTIVE_OPTIONS = [
  { label: '사용', value: 'Y' },
  { label: '미사용', value: 'N' }
];

// ===== Table Columns =====
const employeeColumns = [
  { label: '사원명', field: 'name', sortable: true },
  { label: '연락처', field: 'phone' },
  { label: '사원 번호', field: 'employeeId', sortable: true }
];

// ===== State =====
const employees = ref([]);
const selectedEmployee = ref(null);
const prevSelectedEmployee = ref(null);
const employeeDetail = ref(null);

const loading = ref(false);
const detailLoading = ref(false);
const cardMode = ref('create'); // create, view, edit

const page = ref({
  page: 1,
  size: 8,
  totalElements: 0
});

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

const editForm = reactive({
  name: '',
  phone: '',
  email: '',
  status: 0,
  isActive: 'N',
  hireDate: null,
  resignDate: null
});

// ===== 이미지 업로드 관련 =====
const fileInput = ref(null);
const previewImage = ref('');
const selectedFile = ref(null);

const triggerFileInput = () => {
  fileInput.value.click();
};

const handleImageUpload = (event) => {
  const file = event.target.files[0];
  if (file) {
    selectedFile.value = file;
    const reader = new FileReader();
    reader.onload = (e) => {
      previewImage.value = e.target.result;
    };
    reader.readAsDataURL(file);
  } else {
    selectedFile.value = null;
    previewImage.value = cardMode.value === 'edit' && employeeDetail.value ? getImageUrl(employeeDetail.value.employeeId, true) : defaultPhoto;
  }
};

const getImageUrl = (employeeId, addCacheBuster = false) => {
    if (!employeeId) return defaultPhoto;
    const url = `/api/img/employee/${employeeId}`;
    return addCacheBuster ? `${url}?t=${new Date().getTime()}` : url;
};

// ===== Helper Functions =====
const resetSearchParams = () => {
  Object.assign(searchParams, {
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
};

const resetEditForm = () => {
  Object.assign(editForm, {
    name: '',
    phone: '',
    email: '',
    status: 0,
    isActive: 'N',
    hireDate: null,
    resignDate: null
  });
  previewImage.value = '';
  selectedFile.value = null;
};

const confirmEditLoss = (message) => {
  if (cardMode.value !== 'edit') return true;
  return confirm(message || '현재 수정 중인 내용이 저장되지 않습니다. 계속 진행하시겠습니까?');
};

const validateEmployeeForm = () => {
  if (!editForm.name) return '사원의 이름을 입력해주세요.';
  if (!editForm.phone) return '사원의 연락처를 입력해주세요.';
  if (!editForm.email) return '사원의 이메일을 입력해주세요.';
  if (!editForm.hireDate) return '사원의 입사일을 선택해주세요.';
  if (editForm.status === 2 && !editForm.resignDate) return '퇴사일을 선택해주세요.';
  if (emailError.value) return emailError.value;
  return null;
};

// ===== Computed =====
const formattedHireDate = computed(() => {
  if (!employeeDetail.value?.hireDate) return '';
  return useDateFormat(employeeDetail.value.hireDate).value;
});

const formattedResignDate = computed(() => {
  if (!employeeDetail.value?.resignDate) return '';
  return useDateFormat(employeeDetail.value.resignDate).value;
});

// ===== API Calls =====
const fetchEmployeeList = async () => {
  loading.value = true;
  try {
    const { data } = await axios.get('/api/employee', {
      params: { page: page.value.page, size: page.value.size, ...searchParams }
    });
    employees.value = data.data ?? data.items;
    page.value = data.page ?? { page: page.value.page, size: page.value.size, totalElements: 0 };
  } catch (e) {
    console.error(e);
    toast('error', '조회 실패', '사원 목록 조회 중 오류가 발생했습니다.');
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
    // 이제 photoUrl 대신 employeeId를 사용해 이미지를 불러옵니다.
    previewImage.value = getImageUrl(employeeDetail.value.employeeId);
  } catch (e) {
    console.error(e);
    toast('error', '조회 실패', '사원 상세 정보 조회 중 오류가 발생했습니다.');
  } finally {
    detailLoading.value = false;
  }
};

const addEmployee = async () => {
  const error = validateEmployeeForm();
  if (error) {
    toast('warn', '등록 실패', error);
    return;
  }
  if (!confirm("등록하시겠습니까?")) return;

  const formData = new FormData();
  for (const key in editForm) {
    if (editForm[key] !== null && editForm[key] !== undefined) {
      if (editForm[key] instanceof Date) {
        formData.append(key, editForm[key].toISOString().slice(0, 10));
      } else {
        formData.append(key, editForm[key]);
      }
    }
  }
  if (selectedFile.value) {
    formData.append('photo', selectedFile.value);
  }

  try {
    const { data } = await axios.post('/api/employee', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    toast('success', '등록 성공', '새로운 사원이 성공적으로 등록되었습니다.');
    await fetchEmployeeList();
    
    selectedEmployee.value = employees.value.find(emp => emp.employeeId === data.employeeId);
    cardMode.value = 'view';
    await fetchEmployeeDetail(data.employeeId);
    
    resetEditForm();
  } catch (e) {
    console.error(e);
    toast('error', '등록 실패', '사원 등록 중 오류가 발생했습니다.');
  }
};

const modifyEmployee = async () => {
  const error = validateEmployeeForm();
  if (error) {
    toast('warn', '저장 실패', error);
    return;
  }
  if (!confirm("수정 내용을 저장하시겠습니까?")) return;

  const formData = new FormData();
  for (const key in editForm) {
    if (editForm[key] !== null && editForm[key] !== undefined) {
      if (editForm[key] instanceof Date) {
        formData.append(key, editForm[key].toISOString().slice(0, 10));
      } else {
        formData.append(key, editForm[key]);
      }
    }
  }
  if (selectedFile.value) {
    formData.append('photo', selectedFile.value);
  }

  try {
    await axios.put(`/api/employee/${employeeDetail.value.employeeId}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    toast('success', '저장 성공', '사원 정보가 성공적으로 수정되었습니다.');
    await fetchEmployeeList();
    cardMode.value = 'view';
    // 캐시 문제 해결을 위해 이미지 URL에 타임스탬프를 추가합니다.
    previewImage.value = getImageUrl(employeeDetail.value.employeeId, true);
    await fetchEmployeeDetail(employeeDetail.value.employeeId);
    selectedFile.value = null;
  } catch (e) {
    console.error(e);
    toast('error', '저장 실패', '사원 정보 수정 중 오류가 발생했습니다.');
  }
};

const removeEmployee = async () => {
  if (!employeeDetail.value || !confirm("정말로 이 사원 정보를 삭제하시겠습니까?")) return;
  
  try {
    await axios.delete(`/api/employee/${employeeDetail.value.employeeId}`);
    toast('success', '삭제 성공', '사원 정보가 성공적으로 삭제되었습니다.');
    await fetchEmployeeList();
    cardMode.value = 'create';
    selectedEmployee.value = null;
    employeeDetail.value = null;
  } catch (e) {
    console.error(e);
    toast('error', '삭제 실패', '사원 정보 삭제 중 오류가 발생했습니다.');
  }
};

// ===== Event Handlers =====
const handleSearch = () => {
  if (!confirmEditLoss('조회 시 수정 중인 내용이 사라집니다. 계속하시겠습니까?')) return;
  page.value.page = 1;
  selectedEmployee.value = null;
  employeeDetail.value = null;
  cardMode.value = 'create';
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
  page.value.page = 1;
  fetchEmployeeList();
};

const handleRowSelect = (employee) => {
  if (!confirmEditLoss('선택 시 수정 중인 내용이 사라집니다. 계속하시겠습니까?')) return;
  prevSelectedEmployee.value = employee;
  selectedEmployee.value = employee;
  cardMode.value = 'view';
  fetchEmployeeDetail(employee.employeeId);
};

const handleRowUnSelect = () => {
  if (!confirmEditLoss('선택 해제 시 수정 중인 내용이 사라집니다. 계속하시겠습니까?')) return;
  prevSelectedEmployee.value = null;
  selectedEmployee.value = null;
  cardMode.value = 'create';
  employeeDetail.value = null;
  resetEditForm();
};

const handleReset = () => {
  resetSearchParams();
  page.value.page = 1;
  selectedEmployee.value = null;
  employeeDetail.value = null;
  cardMode.value = 'create';
  fetchEmployeeList();
};

const handleEdit = () => {
  if (!employeeDetail.value) return;
  Object.assign(editForm, {
    ...employeeDetail.value,
    status: Number(employeeDetail.value.status),
    hireDate: employeeDetail.value.hireDate ? new Date(employeeDetail.value.hireDate) : null,
    resignDate: employeeDetail.value.resignDate ? new Date(employeeDetail.value.resignDate) : null
  });
  selectedFile.value = null;
  previewImage.value = getImageUrl(employeeDetail.value.employeeId);
  cardMode.value = 'edit';
};

const getStatusLabel = (status) =>
  STATUS_OPTIONS.find(opt => opt.value === Number(status))?.label || '';

const getIsActiveLabel = (value) =>
  ACTIVE_OPTIONS.find(opt => opt.value === value)?.label || '';

const formatPhone = (value) => {
  if (!value) return '';
  let numbers = value.replace(/\D/g, '');
  if (numbers.length > 11) {
    numbers = numbers.slice(0, 11);
  }
  if (numbers.length < 4) return numbers;
  if (numbers.length < 7) {
    return numbers.replace(/(\d{3})(\d+)/, '$1-$2');
  }
  return numbers.replace(/(\d{3})(\d{3,4})(\d{4})/, '$1-$2-$3');
};

const emailError = ref('');
const validateEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  emailError.value = email && !emailRegex.test(email) ? '유효한 이메일 주소를 입력하세요.' : '';
};

// ===== Watchers =====
watch(
  () => editForm.status,
  (newVal) => {
    if (newVal === 2) {
      editForm.isActive = 'N';
    }
  }
);

watch(
  () => searchParams.status,
  (newVal, oldVal) => {
    const isRetireChecked = newVal.includes(2);
    const wasRetireChecked = oldVal.includes(2);
    if (isRetireChecked && !wasRetireChecked) {
      if (!searchParams.isActive.includes('N')) {
        searchParams.isActive.push('N');
      }
    }
  }
);

watch(cardMode, (newMode) => {
  if (newMode === 'create') {
    previewImage.value = '';
    selectedFile.value = null;
    resetEditForm();
  }
});

// ===== 이미지 모달 =====
const isImageModalVisible = ref(false);
const modalImage = ref('');

const openImageModal = (imageUrl) => {
  if (!imageUrl) return;
  modalImage.value = imageUrl;
  isImageModalVisible.value = true;
};

const handleImageError = (event) => {
  event.target.src = defaultPhoto;
};

// ===== Mounted =====
onMounted(fetchEmployeeList);
</script>

<template>
  <Fluid>
    <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />

    <SearchCard
      title="사원 검색"
      @search="handleSearch"
      @reset="handleReset"
    >
      <div class="flex flex-wrap w-full">
        <div class="flex flex-col gap-2 p-2 w-full xl:w-1/3 lg:w-1/2">
          <InputGroup>
            <InputGroupAddon><i :class="icons.employee" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchParams.empName" inputId="name" />
              <label for="name">사원명</label>
            </IftaLabel>
          </InputGroup>
        </div>

        <div class="flex flex-col gap-2 p-2 w-full xl:w-1/3 lg:w-1/2">
          <InputGroup>
            <InputGroupAddon><i :class="icons.phone" /></InputGroupAddon>
            <IftaLabel>
              <InputText
                v-model="searchParams.phone"
                inputId="phone"
                @input="searchParams.phone = formatPhone(searchParams.phone)"
              />
              <label for="phone">휴대전화 번호</label>
            </IftaLabel>
          </InputGroup>
        </div>

        <div class="flex flex-col gap-2 p-2 w-full xl:w-1/3 lg:w-1/2">
          <InputGroup>
            <InputGroupAddon><i :class="icons.id" /></InputGroupAddon>
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
              v-for="opt in STATUS_OPTIONS"
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
              v-for="opt in ACTIVE_OPTIONS"
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
              <span :class="[icons.list, 'text-xl']"></span>
              사원 목록
            </div>
            <div class="text-sm text-gray-400">
              총 <span class="font-semibold text-sm text-gray-700">
                <span v-if="page.totalElements > 0">
                  <CountUp :end-val="page.totalElements" />
                </span>
                <span v-else>0</span>
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
            v-model:selected="selectedEmployee"
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
                cardMode === 'create' ? icons.add :
                cardMode === 'edit' ? icons.edit : icons.info,
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
              <Btn v-if="cardMode === 'view'" icon="cancel" color="secondary" class="whitespace-nowrap" @click="handleRowUnSelect" outlined>취소</Btn>
              <Btn v-if="cardMode === 'view'" icon="delete" color="danger" @click="removeEmployee" outlined>삭제</Btn>
              <Btn v-if="cardMode === 'view'" icon="edit" color="warn" @click="handleEdit" outlined>수정</Btn>
              <Btn v-if="cardMode === 'create'" icon="refresh" color="secondary" class="whitespace-nowrap" @click="resetEditForm" outlined>초기화</Btn>
              <Btn v-if="cardMode === 'create'" icon="add" @click="addEmployee" outlined>등록</Btn>
              <Btn v-if="cardMode === 'edit'" icon="save" @click="modifyEmployee" outlined>저장</Btn>
            </div>
          </div>

          <Divider />

          <div v-if="detailLoading" class="animate-pulse">
            <div class="flex items-center mb-4">
              <div class="skeleton w-[96px] h-[96px] rounded-lg"></div>
              <div class="ml-6 flex-1">
                <div class="skeleton w-32 h-4 mb-2"></div>
                <div class="skeleton w-20 h-4"></div>
              </div>
            </div>
            <div class="mb-4">
              <div class="skeleton w-full h-10 mb-2"></div>
              <div class="skeleton w-full h-10"></div>
            </div>
            <div class="flex gap-4">
              <div class="skeleton w-24 h-8"></div>
              <div class="skeleton w-24 h-8"></div>
            </div>
          </div>

          <div v-else-if="cardMode === 'view' && employeeDetail">
            <div class="w-full flex flex-row mb-2 gap-2">
              <div class="relative w-[96px] group">
                <img
                  :src="getImageUrl(employeeDetail.employeeId, true)"
                  @error="handleImageError"
                  class="w-[96px] aspect-square rounded-lg object-cover border border-gray-300"
                />

                <div
                  class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100
                         flex items-center justify-center rounded-lg cursor-pointer
                         transition-opacity duration-200"
                  @click="openImageModal(getImageUrl(employeeDetail.employeeId))"
                >
                  <i class="pi pi-search text-white text-2xl"></i>
                </div>
              </div>

              <div class="flex-1 ml-6 flex flex-col justify-center gap-0">
                <div class="font-light text-xs text-gray-500">
                  {{ employeeDetail.employeeId }}
                </div>
                <div class="font-semibold text-lg">
                  {{ employeeDetail.name }}
                </div>
              </div>
            </div>

            <div class="font-semibold text-base mb-2">연락처</div>
            <div class="w-full flex flex-col xl:flex-row gap-2 mb-4">
              <div class="w-full xl:w-1/2">
                <InputGroup>
                  <InputGroupAddon><i :class="icons.phone" /></InputGroupAddon>
                  <IftaLabel>
                    <InputText v-model="employeeDetail.phone" disabled />
                    <label>휴대전화 번호</label>
                  </IftaLabel>
                </InputGroup>
              </div>
              <div class="w-full xl:w-1/2">
                <InputGroup>
                  <InputGroupAddon><i :class="icons.email" /></InputGroupAddon>
                  <IftaLabel>
                    <InputText v-model="employeeDetail.email" disabled />
                    <label>이메일</label>
                  </IftaLabel>
                </InputGroup>
              </div>
            </div>

            <div class="w-full flex flex-col xl:flex-row gap-6 mb-4">
              <div class="w-full xl:w-1/2">
                <div class="font-semibold mb-2">재직 상태</div>
                <span
                  class="inline-block px-3 py-1 rounded-full text-sm font-medium text-white"
                  :class="{
                    'bg-green-500': employeeDetail.status == 0,
                    'bg-yellow-400': employeeDetail.status == 1,
                    'bg-red-400': employeeDetail.status == 2
                  }"
                >
                  {{ getStatusLabel(employeeDetail.status) }}
                </span>
              </div>

              <div class="w-full xl:w-1/2">
                <div class="font-semibold mb-2">사용 여부</div>
                <span
                  class="inline-block px-3 py-1 rounded-full text-sm font-medium text-white"
                  :class="{
                    'bg-green-500': employeeDetail.isActive === 'Y',
                    'bg-gray-400': employeeDetail.isActive === 'N'
                  }"
                >
                  {{ getIsActiveLabel(employeeDetail.isActive) }}
                </span>
              </div>
            </div>

            <div class="font-semibold text-base mb-2">입사일 및 퇴사일 정보</div>
            <div class="w-full flex flex-col xl:flex-row gap-2 mb-4">
              <div class="w-full xl:w-1/2">
                <InputGroup>
                  <InputGroupAddon><i :class="icons.calendar" /></InputGroupAddon>
                  <IftaLabel>
                    <InputText v-model="formattedHireDate" disabled />
                    <label>입사일</label>
                  </IftaLabel>
                </InputGroup>
              </div>
              <div v-if="employeeDetail.resignDate" class="w-full xl:w-1/2">
                <InputGroup>
                  <InputGroupAddon><i :class="icons.calendar" /></InputGroupAddon>
                  <IftaLabel>
                    <InputText v-model="formattedResignDate" disabled />
                    <label>퇴사일</label>
                  </IftaLabel>
                </InputGroup>
              </div>
            </div>
          </div>

          <div v-else-if="cardMode === 'edit' || cardMode === 'create'">
            <div class="w-full flex flex-row mb-2 gap-2">
              <div class="relative w-[96px] group">
                <img
                  :src="previewImage || defaultPhoto"
                  @error="handleImageError"
                  alt="사원 사진"
                  class="w-[96px] aspect-square rounded-lg object-cover border border-gray-300"
                />

                <div
                  class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100
                         flex items-center justify-center rounded-lg cursor-pointer
                         transition-opacity duration-200"
                  @click="triggerFileInput"
                >
                  <i v-if="cardMode === 'create'" class="pi pi-plus text-white text-2xl"></i>
                  <i v-if="cardMode === 'edit'" class="pi pi-pencil text-white text-2xl"></i>
                </div>

                <input
                  type="file"
                  ref="fileInput"
                  class="hidden"
                  accept="image/*"
                  @change="handleImageUpload"
                />
              </div>

              <div class="flex-1 ml-6 flex flex-col justify-center">
                <div v-if="cardMode === 'edit'" class="font-light text-xs text-gray-500">
                  {{ employeeDetail.employeeId }}
                </div>
                <div class="font-semibold text-lg">
                  <InputText
                    v-model="editForm.name"
                    placeholder="이름 입력"
                    class="w-full"
                  />
                </div>
              </div>
            </div>

            <div class="font-semibold text-base mb-2">연락처</div>
            <div class="w-full flex flex-col xl:flex-row gap-2 mb-4">
              <div class="w-full xl:w-1/2">
                <InputGroup>
                  <InputGroupAddon><i :class="icons.phone" /></InputGroupAddon>
                  <IftaLabel>
                    <InputText
                      v-model="editForm.phone"
                      placeholder="휴대전화 번호"
                      maxlength="13"
                      @input="editForm.phone = formatPhone(editForm.phone)"
                    />
                    <label>휴대전화 번호</label>
                  </IftaLabel>
                </InputGroup>
              </div>

              <div class="w-full xl:w-1/2">
                <InputGroup>
                  <InputGroupAddon><i :class="icons.email" /></InputGroupAddon>
                  <IftaLabel>
                    <InputText
                      v-model="editForm.email"
                      placeholder="이메일 주소"
                      @blur="validateEmail(editForm.email)"
                    />
                    <label>이메일</label>
                  </IftaLabel>
                </InputGroup>
                <span v-if="emailError" class="text-red-500 text-xs mt-1">{{ emailError }}</span>
              </div>
            </div>

            <div class="w-full flex flex-col xl:flex-row gap-6 mb-4">
              <div class="w-full xl:w-1/2">
                <div class="font-semibold mb-2">재직 상태</div>
                <Dropdown
                  v-model="editForm.status"
                  :options="STATUS_OPTIONS"
                  option-label="label"
                  option-value="value"
                  placeholder="재직 상태 선택"
                  class="w-full"
                />
              </div>

              <div class="w-full xl:w-1/2">
                <div class="font-semibold mb-2">사용 여부</div>
                <Checkbox
                  :binary="true"
                  v-model="editForm.isActive"
                  :true-value="'Y'"
                  :false-value="'N'"
                  inputId="isActiveCheckbox"
                  :disabled="editForm.status === 2"
                />
                <label for="isActiveCheckbox" class="ml-2 select-none cursor-pointer">
                  {{ getIsActiveLabel(editForm.isActive) }}
                </label>
              </div>
            </div>

            <div class="font-semibold text-base mb-2">입사일 및 퇴사일 정보</div>
            <div class="w-full flex flex-col xl:flex-row gap-2 mb-4">
              <div class="w-full xl:w-1/2">
                <Calendar
                  v-model="editForm.hireDate"
                  showIcon
                  dateFormat="yy-mm-dd"
                  placeholder="입사일"
                  class="w-full"
                />
              </div>
              <div class="w-full xl:w-1/2">
                <Calendar
                  v-if="editForm.status === 2"
                  v-model="editForm.resignDate"
                  showIcon
                  dateFormat="yy-mm-dd"
                  placeholder="퇴사일"
                  class="w-full"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <Dialog v-model:visible="isImageModalVisible" modal header="사진 보기" :style="{ width: '400px' }">
      <img :src="modalImage" class="w-full object-contain rounded-lg" alt="확대 이미지" />
    </Dialog>
  </Fluid>
</template>

<style scoped>
/* skeleton.css */
.skeleton {
  @apply bg-gray-200 rounded-md relative overflow-hidden;
}

.skeleton::after {
  content: "";
  @apply absolute top-0 left-[-150%] w-[150%] h-full;
  background: linear-gradient(to right, transparent 0%, rgba(255,255,255,0.5) 50%, transparent 100%);
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  100% {
    left: 100%;
  }
}
</style>