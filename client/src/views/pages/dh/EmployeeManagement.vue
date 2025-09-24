<script setup>
import { useRoute } from 'vue-router';
import { computed, ref, reactive, onMounted, watch } from 'vue';
import { useIcon } from '@/composables/useIcon';
import axios from 'axios';
import { useAppToast } from '@/composables/useAppToast';
import { useDateFormat } from '@/composables/useFormat'

const route = useRoute();
const { toast } = useAppToast();

const iconInfo = useIcon('info');
const iconAdd = useIcon('add');
const iconEdit = useIcon('edit');
const iconList = useIcon('list');
const iconEmployee = useIcon('employee');
const iconPhone = useIcon('phone');
const iconEmail = useIcon('email');
const iconCalendar = useIcon('calendar');
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

const editForm = reactive({
  name: '',
  phone: '',
  email: '',
  status: 0,
  isActive: 'N',
  hireDate: null,
  resignDate: null
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
const prevSelectedEmployee = ref(null);
const selectedEmployee = ref(null);
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

const formattedHireDate = computed(() => useDateFormat(employeeDetail.value?.hireDate).value);
const formattedResignDate = computed(() => useDateFormat(employeeDetail.value?.resignDate).value);

const handleSearch = () => {
  if (cardMode.value === 'edit') {
    if (!confirm("현재 수정 중인 내용이 있을 경우 저장되지 않습니다. 조회하시겠습니까?")) {
      selectedEmployee.value = prevSelectedEmployee.value;
      return;
    }
  }
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
  if (cardMode.value === 'edit') {
    if (!confirm("현재 수정 중인 내용이 있을 경우 저장되지 않습니다. 선택하시겠습니까?")) {
      selectedEmployee.value = prevSelectedEmployee.value;
      return;
    }
  }
  prevSelectedEmployee.value = employee;
  selectedEmployee.value = employee;
  cardMode.value = 'view';
  fetchEmployeeDetail(employee.employeeId);
};

const handleRowUnSelect = () => {
  if (cardMode.value === 'edit') {
    if (!confirm("현재 수정 중인 내용이 있을 경우 저장되지 않습니다. 선택 해제하시겠습니까?")) {
      selectedEmployee.value = prevSelectedEmployee.value;
      return;
    }
  }
  prevSelectedEmployee.value = null;
  selectedEmployee.value = null;
  cardMode.value = 'create';
  employeeDetail.value = null;
};

const handleReset = () => {
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
  page.value.page = 1;
  selectedEmployee.value = null;
  employeeDetail.value = null;
  cardMode.value = 'create';
  fetchEmployeeList();
};

const handleEdit = () => {
  if (!employeeDetail.value) return;

  Object.assign(editForm, {
    name: employeeDetail.value.name,
    phone: employeeDetail.value.phone,
    email: employeeDetail.value.email,
    status: Number(employeeDetail.value.status),
    isActive: employeeDetail.value.isActive,
    hireDate: employeeDetail.value.hireDate ? new Date(employeeDetail.value.hireDate) : null,
    resignDate: employeeDetail.value.resignDate ? new Date(employeeDetail.value.resignDate) : null
  });

  cardMode.value = 'edit';
};

const handleResetForm = () => {
    Object.assign(editForm, {
        name: "",
        phone: "",
        email: "",
        status: 0,
        isActive: "N",
        hireDate: null,
        resignDate: null
    });
}

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

const modifyEmployee = async () => {
    if (!editForm.name) return toast("warn", "저장 실패", "사원의 이름을 입력하여주세요.");
    if (!editForm.phone) return toast("warn", "저장 실패", "사원의 연락처를 입력하여주세요.");
    if (!editForm.email) return toast("warn", "저장 실패", "사원의 이메일을 입력하여주세요.");
    if (!editForm.hireDate) return toast("warn", "저장 실패", "사원의 입사일을 선택하여주세요.");
    if (editForm.status === 2 && !editForm.resignDate) return toast("warn", "저장 실패", "사원의 퇴사일을 선택하여주세요.");

    const payload = {
        name: editForm.name,
        phone: editForm.phone,
        email: editForm.email,
        status: editForm.status,
        isActive: editForm.isActive,
        hireDate: editForm.hireDate ? editForm.hireDate.toISOString().slice(0, 10) : null,
        resignDate: editForm.resignDate ? editForm.resignDate.toISOString().slice(0, 10) : null
    };

    try {
        const result = await axios.put(`/api/employee/${employeeDetail.value.employeeId}`, payload);
        if (result.status === 200) {
            fetchEmployeeList();
            fetchEmployeeDetail(employeeDetail.value.employeeId);
            cardMode.value = 'view';
            return toast('success','수정 성공', '성공적으로 수정되었습니다.');
        }
    } catch(e) {
        console.error(e);
        return toast('error','수정 실패', '오류가 발생하여 수정에 실패하였습니다.');
    }
};

const addEmployee = async () => {
    if (!editForm.name) return toast("warn", "등록 실패", "사원의 이름을 입력하여주세요.");
    if (!editForm.phone) return toast("warn", "등록 실패", "사원의 연락처를 입력하여주세요.");
    if (!editForm.email) return toast("warn", "등록 실패", "사원의 이메일을 입력하여주세요.");
    if (!editForm.hireDate) return toast("warn", "등록 실패", "사원의 입사일을 선택하여주세요.");
    if (editForm.status === 2 && !editForm.resignDate) return toast("warn", "저장 실패", "사원의 퇴사일을 선택하여주세요.");

    const payload = {
        name: editForm.name,
        phone: editForm.phone,
        email: editForm.email,
        status: editForm.status,
        isActive: editForm.isActive,
        hireDate: editForm.hireDate ? editForm.hireDate.toISOString().slice(0, 10) : null,
        resignDate: editForm.resignDate ? editForm.resignDate.toISOString().slice(0, 10) : null
    };

    try {
        const result = await axios.post(`/api/employee`, payload);
        if (result.status === 200) {
            fetchEmployeeList();
            fetchEmployeeDetail(result.data.employeeId);
            cardMode.value = 'view';
            handleResetForm();
            return toast('success','등록 성공', '성공적으로 등록되었습니다.');
        }
    } catch(e) {
        console.error(e);
        return toast('error','등록 실패', '오류가 발생하여 등록에 실패하였습니다.');
    }

}

const getStatusLabel = (status) => {
  const numStatus = Number(status);
  const found = statusOptions.find(opt => opt.value === numStatus);
  return found ? found.label : '';
};

const getIsActiveLabel = (value) => {
  const found = isActiveOptions.find(opt => opt.value === value);
  return found ? found.label : '';
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
      @reset="handleReset"
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
              <Btn v-if="cardMode === 'view'" icon="edit" color="warn" @click="handleEdit" outlined>수정</Btn>
              <Btn v-if="cardMode === 'create'" icon="refresh" color="secondary" class="whitespace-nowrap" @click="handleResetForm" outlined>초기화</Btn>
              <Btn v-if="cardMode === 'create'" icon="add" @click="addEmployee" outlined>등록</Btn>
              <Btn v-if="cardMode === 'edit'" icon="save" @click="modifyEmployee" outlined>저장</Btn>
            </div>
          </div>

          <Divider />

          <!-- 조회 모드 -->
          <div v-if="cardMode === 'view' && employeeDetail" >

            <div class="w-full flex flex-row mb-2 gap-2">
                <div class="w-[96px]">
                    <img
                        src="https://i.namu.wiki/i/NB_qC6YRjH7hv6elNznBIBOBZ5AwE-PKYEWKcU03aFzGsc60bOt9KLxocyvB01OxAbOG8joW9mgkShFmTaTKsQ.webp"
                        alt="사원 증명사진"
                        class="w-[96px] aspect-square rounded-lg object-cover border border-gray-300"
                    />
                </div>
                <div class="flex-1 ml-6 flex flex-col justify-center gap-0 xl:gap-0">
                    <div class="font-light text-xs flex items-center gap-4 text-gray-500">
                        {{ employeeDetail.employeeId }}
                    </div>
                    <div class="font-semibold text-lg flex items-center gap-4">
                        {{ employeeDetail.name }}
                    </div>
                </div>
            </div>
            <div class="font-semibold text-base flex items-center gap-4 mb-2">
                연락처
            </div>
            <div class="w-full flex flex-col xl:flex-row gap-2 mb-4">
                <div class="w-full xl:w-1/2">
                    <InputGroup>
                        <InputGroupAddon><i :class="iconPhone" /></InputGroupAddon>
                        <IftaLabel>
                        <InputText v-model="employeeDetail.phone" inputId="detailPhone" disabled/>
                        <label for="detailPhone">휴대전화 번호</label>
                        </IftaLabel>
                    </InputGroup>
                </div>
                <div class="w-full xl:w-1/2">
                    <InputGroup>
                        <InputGroupAddon><i :class="iconEmail" /></InputGroupAddon>
                        <IftaLabel>
                        <InputText v-model="employeeDetail.email" inputId="detailEmail" disabled/>
                        <label for="detailEmail">이메일</label>
                        </IftaLabel>
                    </InputGroup>
                </div>
            </div>

            <div class="w-full flex flex-col xl:flex-row gap-6 mb-4">

                <div class="w-full xl:w-1/2">
                    <div class="font-semibold text-base flex items-center gap-4 mb-2">
                        재직 상태
                    </div>
                    <div>
                        <span
                        class="inline-block px-3 py-1 rounded-full text-sm font-medium text-white"
                        :class="{
                            'bg-green-500': employeeDetail.status == 0 || employeeDetail.status === '0',
                            'bg-yellow-400': employeeDetail.status == 1 || employeeDetail.status === '1',
                            'bg-red-400': employeeDetail.status == 2 || employeeDetail.status === '2'
                        }"
                        >
                        {{ getStatusLabel(employeeDetail.status) }}
                        </span>
                    </div>
                    </div>

                    <div class="w-full xl:w-1/2">
                        <div class="font-semibold text-base flex items-center gap-2 mb-2">
                            사용 여부
                        </div>
                        <div>
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

            </div>

            <div class="font-semibold text-base flex items-center gap-4 mb-2">
                입사일 및 퇴사일 정보
            </div>
            <div class="w-full flex flex-col xl:flex-row gap-2 mb-4">
                <div class="w-full xl:w-1/2">
                    <InputGroup>
                        <InputGroupAddon><i :class="iconCalendar" /></InputGroupAddon>
                        <IftaLabel>
                        <InputText v-model="formattedHireDate" inputId="detailHireDate" disabled/>
                        <label for="detailHireDate">입사일</label>
                        </IftaLabel>
                    </InputGroup>
                </div>
                <div v-if="employeeDetail.resignDate !== null" class="w-full xl:w-1/2">
                    <InputGroup>
                        <InputGroupAddon><i :class="iconCalendar" /></InputGroupAddon>
                        <IftaLabel>
                        <InputText v-model="formattedResignDate" inputId="detailResignDate" disabled/>
                        <label for="detailResignDate">퇴사일</label>
                        </IftaLabel>
                    </InputGroup>
                </div>
            </div>
          </div>

            <!-- 등록 / 수정 모드 -->
            <div v-if="cardMode === 'edit' || cardMode === 'create'">
            <div class="w-full flex flex-row mb-2 gap-2">
                <div class="w-[96px]">
                <img
                    src="https://i.namu.wiki/i/NB_qC6YRjH7hv6elNznBIBOBZ5AwE-PKYEWKcU03aFzGsc60bOt9KLxocyvB01OxAbOG8joW9mgkShFmTaTKsQ.webp"
                    alt="사원 사진"
                    class="w-[96px] aspect-square rounded-lg object-cover border border-gray-300"
                />
                </div>
                <div class="flex-1 ml-6 flex flex-col justify-center gap-0 xl:gap-0">
                <div
                    v-if="cardMode === 'edit'"
                    class="font-light text-xs flex items-center gap-4 text-gray-500"
                >
                    {{ employeeDetail.employeeId }}
                </div>
                <div class="font-semibold text-lg flex items-center gap-4">
                    <InputText
                    v-model="editForm.name"
                    inputId="editName"
                    class="w-full"
                    placeholder="이름 입력"
                    />
                </div>
                </div>
            </div>

            <div class="font-semibold text-base flex items-center gap-4 mb-2">연락처</div>
            <div class="w-full flex flex-col xl:flex-row gap-2 mb-4">
                <div class="w-full xl:w-1/2">
                <InputGroup>
                    <InputGroupAddon><i :class="iconPhone" /></InputGroupAddon>
                    <IftaLabel>
                    <InputText
                        v-model="editForm.phone"
                        inputId="editPhone"
                        placeholder="휴대전화 번호"
                    />
                    <label for="editPhone">휴대전화 번호</label>
                    </IftaLabel>
                </InputGroup>
                </div>
                <div class="w-full xl:w-1/2">
                <InputGroup>
                    <InputGroupAddon><i :class="iconEmail" /></InputGroupAddon>
                    <IftaLabel>
                    <InputText
                        v-model="editForm.email"
                        inputId="editEmail"
                        placeholder="이메일 주소"
                    />
                    <label for="editEmail">이메일</label>
                    </IftaLabel>
                </InputGroup>
                </div>
            </div>

            <div class="w-full flex flex-col xl:flex-row gap-6 mb-4">
                <div class="w-full xl:w-1/2">
                <div class="font-semibold text-base flex items-center gap-4 mb-2">재직 상태</div>
                <Dropdown
                    v-model="editForm.status"
                    :options="statusOptions"
                    option-label="label"
                    option-value="value"
                    placeholder="재직 상태 선택"
                    class="w-full"
                />
                </div>

                <div class="w-full xl:w-1/2">
                <div class="font-semibold text-base flex items-center gap-2 mb-2">사용 여부</div>
                <div class="flex gap-4">
                    <div class="flex items-center gap-2">
                    <Checkbox
                        :binary="true"
                        v-model="editForm.isActive"
                        :true-value="'Y'"
                        :false-value="'N'"
                        inputId="isActiveCheckbox"
                    />
                    <label for="isActiveCheckbox" class="select-none cursor-pointer">
                        {{ getIsActiveLabel(editForm.isActive) }}
                    </label>
                    </div>
                </div>
                </div>
            </div>

            <div class="font-semibold text-base flex items-center gap-4 mb-2">
                입사일 및 퇴사일 정보
            </div>
            <div class="w-full flex flex-col xl:flex-row gap-2 mb-4">
                <div class="w-full xl:w-1/2">
                <Calendar
                    v-model="editForm.hireDate"
                    inputId="editHireDate"
                    showIcon
                    placeholder="입사일"
                    class="w-full"
                />
                </div>
                <div v-if="editForm.status === 2" class="w-full xl:w-1/2">
                <Calendar
                    v-model="editForm.resignDate"
                    inputId="editResignDate"
                    showIcon
                    placeholder="퇴사일"
                    class="w-full"
                />
                </div>
            </div>
            </div>


        </div>
      </div>

    </div>
  </Fluid>
</template>
