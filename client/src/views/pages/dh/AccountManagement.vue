<script setup>
import { useRoute } from 'vue-router';
import { computed, ref, reactive, onMounted } from 'vue';
import axios from 'axios';
import { useIcon } from '@/composables/useIcon';
import { useAppToast } from '@/composables/useAppToast';

const route = useRoute();
const { toast } = useAppToast();

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
    save: useIcon('save'),
    register: useIcon('register'),
    address: useIcon('address'),
    vendor: useIcon('vendor')
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
const TYPE_OPTIONS = [
    { label: '관리자', value: 0 },
    { label: '사원', value: 1 },
    { label: '판매처', value: 2 },
    { label: '공급처', value: 3 },
    { label: '공급 및 판매처', value: 4 }
];

const ACTIVE_OPTIONS = [
    { label: '사용', value: 'Y' },
    { label: '미사용', value: 'N' }
];

// ===== Reactive Data =====
const searchParams = reactive({
  isActive: null,
  companyName: '',
  type: []
});

const page = reactive({
  pageNumber: 1,
  pageSize: 10,
  totalElements: 0,
  sortField: null,
  sortOrder: null
});

const loading = ref(false);
const vendors = ref([]);
const selectedVendor = ref(null);

// ===== Methods =====
const handleSearch = async () => {
  loading.value = true;
  try {
    // 실제 API 요청 예시
    const params = {
      isActive: searchParams.isActive,
      companyName: searchParams.companyName,
      type: searchParams.type.length ? searchParams.type : undefined,
      page: page.pageNumber,
      size: page.pageSize,
      sortField: page.sortField,
      sortOrder: page.sortOrder
    };

    const response = await axios.get('/api/vendors', { params });
    vendors.value = response.data.items || response.data.data || [];
    page.totalElements = response.data.totalElements || response.data.page?.totalElements || 0;
  } catch (error) {
    toast.error('계정 목록 조회에 실패했습니다.');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const handleReset = () => {
  searchParams.isActive = null;
  searchParams.companyName = '';
  searchParams.type = [];
  page.pageNumber = 1;
  page.sortField = null;
  page.sortOrder = null;
  vendors.value = [];
};

// 페이징, 정렬 등 이벤트 핸들러는 필요하면 추가하세요
const handlePageChange = (newPage) => {
  page.pageNumber = newPage;
  handleSearch();
};

const handleSortChange = ({ sortField, sortOrder }) => {
  page.sortField = sortField;
  page.sortOrder = sortOrder;
  handleSearch();
};

const handleRowSelect = (row) => {
  selectedVendor.value = row;
};

const handleRowUnSelect = () => {
  selectedVendor.value = null;
};

onMounted(() => {
  handleSearch();
});
</script>


<template>
    <Fluid>
        <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />

        <SearchCard title="계정 검색" @search="handleSearch" @reset="handleReset">
            <div class="flex flex-wrap w-full">
                <div class="flex flex-wrap p-2 gap-4 xl:gap-0 w-full xl:w-full">

                    <div class="flex flex-col gap-2 w-full xl:w-1/4 lg:w-1/2">
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

                    <div class="flex flex-col gap-2 p-2 w-full xl:w-1/4 lg:w-1/2">
                        <InputGroup>
                            <InputGroupAddon><i :class="icons.vendor" /></InputGroupAddon>
                            <IftaLabel>
                                <InputText v-model="companyName" inputId="searchCompanyName" />
                                <label for="searchCompanyName">계정명</label>
                            </IftaLabel>
                        </InputGroup>
                    </div>

                    <div class="hidden xl:block w-full xl:w-1/2"></div>
                </div>

            </div>
        </SearchCard>

        <div class="flex flex-col md:flex-row w-full gap-4 mt-4">
            <div class="w-full">
                <div class="card flex flex-col">
                    <div class="font-semibold text-lg sm:text-xl flex items-center justify-between gap-4 h-10">
                        <div class="flex items-center gap-4">
                            <span :class="icons.list"></span>
                            계정 목록
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
                    <!-- <DTable
                        :columns="vendorColumns"
                        :data="vendors"
                        :page="page"
                        :loading="loading"
                        dataKey="vendorId"
                        v-model:selected="selectedVendor"
                        @page-change="handlePageChange"
                        @sort-change="handleSortChange"
                        @row-select="handleRowSelect"
                        @row-unselect="handleRowUnSelect"
                    /> -->
                </div>
            </div>
        </div>
    </Fluid>
</template>

<style scoped>
/* skeleton.css (기존 유지) */
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
