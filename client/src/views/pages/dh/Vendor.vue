<script setup>
import { useRoute } from 'vue-router';
import { computed, ref, reactive, onMounted, watch } from 'vue';
import axios from 'axios';
import { useIcon } from '@/composables/useIcon';
import { useAppToast } from '@/composables/useAppToast';
import { usePhoneFormat, useBusinessRegistrationFormat } from '@/composables/useFormat';

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
const STATUS_OPTIONS = [
    { label: '판매처', value: 0 },
    { label: '공급처', value: 1 },
    { label: '공급 및 판매처', value: 2 }
];

const ACTIVE_OPTIONS = [
    { label: '사용', value: 'Y' },
    { label: '미사용', value: 'N' }
];

const getDisplayTypes = (typeValue) => {
    let types = [];
    if (Array.isArray(typeValue)) {
        types = typeValue.map(Number);
    } else if (typeof typeValue === 'number' || typeof typeValue === 'string') {
        types = String(typeValue).split(',').map(v => parseInt(v.trim())).filter(v => !isNaN(v));
    }

    const finalDisplayTypes = new Set();

    types.forEach(type => {
        if (type === 2) {
            finalDisplayTypes.add(0);
            finalDisplayTypes.add(1);
        } else {
            finalDisplayTypes.add(type);
        }
    });

    return Array.from(finalDisplayTypes);
};

const getStatusLabel = (value) =>
    STATUS_OPTIONS.find(opt => opt.value === Number(value))?.label || '';

const getStatusLabels = (typeArray) => {
    if (!typeArray || (Array.isArray(typeArray) && typeArray.length === 0)) return '없음';

    const types = Array.isArray(typeArray)
        ? typeArray
        : String(typeArray).split(',').map(v => v.trim()).filter(v => v !== '').map(Number);

    return types.map(type => getStatusLabel(type)).filter(label => label).join(', ');
};

const getIsActiveLabel = (value) =>
    ACTIVE_OPTIONS.find(opt => opt.value === value)?.label || '';

const formatBusinessRegistration = (value) => {
    const formattedComputed = useBusinessRegistrationFormat(value);

    return formattedComputed.value;
};

const formatPhone = (value) => {
    const formattedComputed = usePhoneFormat(value);

    return formattedComputed.value;
};

const emailError = ref('');
const validateEmail = (email) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    emailError.value = email && !emailRegex.test(email) ? '유효한 이메일 주소를 입력하세요.' : '';
};

const getVendorTypePayload = (typeArray) => {
    if (!Array.isArray(typeArray) || typeArray.length === 0) {
        return '';
    }

    const types = new Set(typeArray.map(Number));

    if (types.has(0) && types.has(1)) {
        return '2';
    }

    return Array.from(types).join(',');
};


const validateVendorForm = () => {
    if (!editForm.businessRegistration) return '사업자 등록 번호를 입력해주세요.';
    if (!editForm.companyName) return '거래처명을 입력해주세요.';
    if (!editForm.ceoName) return '대표자명을 입력해주세요.';
    if (editForm.type.length === 0) return '거래처 유형을 하나 이상 선택해주세요.';
    if (editForm.ownerEmail) validateEmail(editForm.ownerEmail);
    if (emailError.value) return emailError.value;
    return null;
};

// ===== Table Columns =====
const vendorColumns = [
    { label: '거래처 유형', field: 'type', format: (data) => getStatusLabels(data.type) },
    { label: '거래처명', field: 'companyName'},
    { label: '사업자 등록 번호', field: 'businessRegistration'}
];

// ===== State =====
const vendors = ref([]);
const selectedVendor = ref(null);
const prevSelectedVendor = ref(null);
const vendorDetail = ref(null);

const loading = ref(false);
const detailLoading = ref(false);
const cardMode = ref('create'); // create, view, edit

const page = ref({
    page: 1,
    size: 8,
    totalElements: 0
});

const searchParams = reactive({
    type: [], // 체크박스 바인딩을 위해 배열
    isActive: ['Y'],
    companyName: '',
    ceoName: '',
    phoneNumber: '',
    businessRegistration: '',
    sortField: null,
    sortOrder: null
});

const editForm = reactive({
    businessRegistration: '',
    companyName: '',
    ceoName: '',
    phoneNumber: '',
    address: '',
    type: [],
    isActive: 'N',
    ownerName: '',
    ownerEmail: '',
    ownerPhone: ''
});

// ===== State Reset Functions =====
const resetSearchParams = () => {
    Object.assign(searchParams, {
        type: [],
        isActive: ['Y'],
        companyName: '',
        ceoName: '',
        phoneNumber: '',
        businessRegistration: '',
        sortField: null,
        sortOrder: null
    });
};

const resetEditForm = () => {
    Object.assign(editForm, {
        businessRegistration: '',
        companyName: '',
        ceoName: '',
        phoneNumber: '',
        address: '',
        type: [],
        isActive: 'N',
        ownerName: '',
        ownerEmail: '',
        ownerPhone: ''
    });
    emailError.value = '';
};

const confirmEditLoss = (message) => {
    if (cardMode.value !== 'edit') return true;
    return confirm(message || '현재 수정 중인 내용이 저장되지 않습니다. 계속 진행하시겠습니까?');
};


// ===== API Calls =====
const fetchVendorList = async () => {
    loading.value = true;
    try {
        let finalType = [...searchParams.type].map(Number);

        const hasVendor = finalType.includes(0);
        const hasSupplier = finalType.includes(1);

        if (hasVendor && hasSupplier) {
            if (!finalType.includes(2)) {
                finalType.push(2);
            }
        }

        finalType = [...new Set(finalType)];

        const cleanSearchParams = Object.fromEntries(
            Object.entries(searchParams).filter(([key, value]) => {
                if (key === 'type') return false; // type은 수동으로 처리
                if (Array.isArray(value) && value.length === 0) return false;
                return true;
            })
        );

        const { data } = await axios.get('/api/vendor', {
            params: {
                page: page.value.page,
                size: page.value.size,
                ...cleanSearchParams,
                ...(finalType.length > 0 && { type: finalType.join(',') })
            }
        });

        vendors.value = data.data ?? data.items;

        Object.assign(page.value, data.page || {
             page: page.value.page,
             size: page.value.size,
             totalElements: 0
        });

        console.log(data)

    } catch (e) {
        console.error(e);
        toast('error', '조회 실패', '거래처 목록 조회 중 오류가 발생했습니다.');
    } finally {
        loading.value = false;
    }
};

const fetchVendorDetail = async (vendorId) => {
    if (!vendorId) return;
    detailLoading.value = true;
    try {
        const { data } = await axios.get(`/api/vendor/${vendorId}`);
        let vendorData = data.data ?? data;

        if (typeof vendorData.type === 'string') {
            vendorData.type = vendorData.type.split(',').map(v => parseInt(v.trim())).filter(v => !isNaN(v));
        } else if (typeof vendorData.type === 'number') {
            vendorData.type = [vendorData.type];
        } else if (!Array.isArray(vendorData.type)) {
            vendorData.type = [];
        } else {
             vendorData.type = vendorData.type.map(v => parseInt(v));
        }

        vendorDetail.value = vendorData;
    } catch (e) {
        console.error(e);
        toast('error', '조회 실패', '거래처 상세 정보 조회 중 오류가 발생했습니다.');
    } finally {
        detailLoading.value = false;
    }
};

const addVendor = async () => {
    const error = validateVendorForm();
    if (error) {
        toast('warn', '등록 실패', error);
        return;
    }
    if (!confirm("등록하시겠습니까?")) return;

    try {
        const payload = {
             ...editForm,
             type: getVendorTypePayload(editForm.type)
        };

        const response = await axios.post('/api/vendor', payload);
        const responseData = response.data.data ?? response.data;

        if (response.status === 200 || response.status === 201) {
            toast('success', '등록 성공', '새로운 거래처가 성공적으로 등록되었습니다.');
            await fetchVendorList();

            const newVendorId = responseData.vendorId;

            selectedVendor.value = vendors.value.find(v => v.vendorId === newVendorId);
            cardMode.value = 'view';
            await fetchVendorDetail(newVendorId);

            resetEditForm();
        } else {
            toast('error', '등록 실패', `거래처 등록 중 오류가 발생했습니다. (상태 코드: ${response.status})`);
            console.error("API call failed with status code:", response.status, response.data);
        }
    } catch (e) {
        console.error(e);
        toast('error', '등록 실패', '거래처 등록 중 오류가 발생했습니다.');
    }
};

const modifyVendor = async () => {
    const error = validateVendorForm();
    if (error) {
        toast('warn', '저장 실패', error);
        return;
    }
    if (!confirm("수정 내용을 저장하시겠습니까?")) return;
    if (!vendorDetail.value?.vendorId) {
        toast('error', '저장 실패', '수정할 거래처 ID를 찾을 수 없습니다.');
        return;
    }

    try {
        const payload = {
             ...editForm,
             type: getVendorTypePayload(editForm.type)
        };

        const response = await axios.put(`/api/vendor/${vendorDetail.value.vendorId}`, payload);
        if (response.status === 200) {
            toast('success', '저장 성공', '거래처 정보가 성공적으로 수정되었습니다.');
            await fetchVendorList();
            cardMode.value = 'view';
            await fetchVendorDetail(vendorDetail.value.vendorId);
        } else {
            toast('error', '저장 실패', `거래처 정보 수정 중 오류가 발생했습니다. (상태 코드: ${response.status})`);
            console.error("API call failed with status code:", response.status, response.data);
        }
    } catch (e) {
        console.error(e);
        toast('error', '저장 실패', '거래처 정보 수정 중 오류가 발생했습니다.');
    }
};

const removeVendor = async () => {
    if (!vendorDetail.value || !confirm("정말로 이 거래처 정보를 삭제하시겠습니까?")) return;

    try {
        const response = await axios.delete(`/api/vendor/${vendorDetail.value.vendorId}`);
        if (response.status === 200) {
            toast('success', '삭제 성공', '거래처 정보가 성공적으로 삭제되었습니다.');
            await fetchVendorList();
            cardMode.value = 'create';
            selectedVendor.value = null;
            vendorDetail.value = null;
            resetEditForm();
        } else {
            toast('error', '삭제 실패', `거래처 정보 삭제 중 오류가 발생했습니다. (상태 코드: ${response.status})`);
            console.error("API call failed with status code:", response.status, response.data);
        }
    } catch (e) {
        console.error(e);
        toast('error', '삭제 실패', '거래처 정보 삭제 중 오류가 발생했습니다.');
    }
};

// ===== Event Handlers =====
const handleSearch = () => {
    if (!confirmEditLoss('조회 시 수정 중인 내용이 사라집니다. 계속하시겠습니까?')) return;
    page.value.page = 1;
    selectedVendor.value = null;
    vendorDetail.value = null;
    cardMode.value = 'create';
    fetchVendorList();
};

const handlePageChange = ({ page: newPage, size }) => {
    page.value.page = newPage;
    page.value.size = size;
    fetchVendorList();
};

const handleSortChange = ({ sortField, sortOrder }) => {
    searchParams.sortField = sortField;
    searchParams.sortOrder = sortOrder;
    page.value.page = 1;
    fetchVendorList();
};

const handleRowSelect = (vendor) => {
    if (!confirmEditLoss('선택 시 수정 중인 내용이 사라집니다. 계속하시겠습니까?')) return;
    prevSelectedVendor.value = vendor;
    selectedVendor.value = vendor;
    cardMode.value = 'view';
    fetchVendorDetail(vendor.vendorId);
};

const handleRowUnSelect = () => {
    if (!confirmEditLoss('선택 해제 시 수정 중인 내용이 사라집니다. 계속하시겠습니까?')) return;
    prevSelectedVendor.value = null;
    selectedVendor.value = null;
    cardMode.value = 'create';
    vendorDetail.value = null;
    resetEditForm();
};

const handleReset = () => {
    if (!confirmEditLoss('검색 조건 초기화 시 수정 중인 내용이 사라집니다. 계속하시겠습니까?')) return;
    resetSearchParams();
    page.value.page = 1;
    selectedVendor.value = null;
    vendorDetail.value = null;
    cardMode.value = 'create';
    fetchVendorList();
};

const handleResetForm = () => {
    const isFormDirty =
        editForm.businessRegistration ||
        editForm.companyName ||
        editForm.ceoName ||
        editForm.phoneNumber ||
        editForm.address ||
        editForm.ownerName ||
        editForm.ownerEmail ||
        editForm.ownerPhone ||
        editForm.type.length > 0 ||
        editForm.isActive !== 'N';

    if (isFormDirty) {
        if (confirm('현재 입력 중인 내용이 저장되지 않습니다. 계속 진행하시겠습니까?')) {
            resetEditForm();
        }
    } else {
        resetEditForm();
    }
};

const handleEdit = () => {
    if (!vendorDetail.value) return;

    Object.assign(editForm, {
         ...vendorDetail.value,
         type: Array.isArray(vendorDetail.value.type) ? vendorDetail.value.type : (vendorDetail.value.type ? [vendorDetail.value.type] : []),
         isActive: vendorDetail.value.isActive || 'N'
    });

    cardMode.value = 'edit';
};

const handleCancelEdit = () => {
    if (!vendorDetail.value) return;
    if (!confirm('수정한 내용이 저장되지 않습니다. 취소하시겠습니까?')) return;

    resetEditForm();
    cardMode.value = 'view';
};

// ===== Watchers & Mounted =====
watch(cardMode, (newMode) => {
    if (newMode === 'create') {
        resetEditForm();
    }
});

watch(() => editForm.ownerEmail, (newVal) => {
      validateEmail(newVal);
});

// Mounted
onMounted(fetchVendorList);
</script>

<template>
    <Fluid>
        <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />

        <SearchCard title="거래처 검색" @search="handleSearch" @reset="handleReset">
            <div class="flex flex-wrap w-full">
                <div class="flex flex-wrap p-2 gap-4 xl:gap-0 w-full xl:w-full">
                    <div class="flex flex-col gap-2 w-full xl:w-1/4 lg:w-1/2">
                        <label class="font-semibold mb-1">거래처 유형</label>
                        <div class="flex flex-wrap gap-4">
                            <div
                                v-for="opt in STATUS_OPTIONS.filter(o => o.value !== 2)"
                                :key="opt.value"
                                class="flex items-center gap-2"
                            >
                                <Checkbox
                                    v-model="searchParams.type"
                                    :inputId="'searchType-' + opt.value"
                                    :value="opt.value"
                                />
                                <label
                                    :for="'searchType-' + opt.value"
                                    class="select-none cursor-pointer"
                                >
                                    {{ opt.label }}
                                </label>
                            </div>
                        </div>
                    </div>

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
                    <div class="hidden xl:block w-full xl:w-1/2"></div>
                </div>

                <div class="flex flex-col gap-2 p-2 w-full xl:w-1/4 lg:w-1/2">
                    <InputGroup>
                        <InputGroupAddon><i :class="icons.vendor" /></InputGroupAddon>
                        <IftaLabel>
                            <InputText v-model="searchParams.companyName" inputId="searchCompanyName" />
                            <label for="searchCompanyName">거래처명</label>
                        </IftaLabel>
                    </InputGroup>
                </div>

                <div class="flex flex-col gap-2 p-2 w-full xl:w-1/4 lg:w-1/2">
                    <InputGroup>
                        <InputGroupAddon><i :class="icons.id" /></InputGroupAddon>
                        <IftaLabel>
                            <InputText
                                v-model="searchParams.businessRegistration"
                                inputId="searchBusinessRegistration"
                                @input="searchParams.businessRegistration = formatBusinessRegistration(searchParams.businessRegistration)"
                                maxlength="12" />
                            <label for="searchBusinessRegistration">사업자 등록 번호</label>
                        </IftaLabel>
                    </InputGroup>
                </div>

                <div class="flex flex-col gap-2 p-2 w-full xl:w-1/4 lg:w-1/2">
                    <InputGroup>
                        <InputGroupAddon><i :class="icons.employee" /></InputGroupAddon>
                        <IftaLabel>
                            <InputText v-model="searchParams.ceoName" inputId="searchCeoName" />
                            <label for="searchCeoName">대표자/담당자</label>
                        </IftaLabel>
                    </InputGroup>
                </div>

                <div class="flex flex-col gap-2 p-2 w-full xl:w-1/4 lg:w-1/2">
                    <InputGroup>
                        <InputGroupAddon><i :class="icons.phone" /></InputGroupAddon>
                        <IftaLabel>
                            <InputText
                                v-model="searchParams.phoneNumber"
                                inputId="searchPhoneNumber"
                                @input="searchParams.phoneNumber = formatPhone(searchParams.phoneNumber)"
                                maxlength="13" />
                            <label for="searchPhoneNumber">전화번호</label>
                        </IftaLabel>
                    </InputGroup>
                </div>
            </div>
        </SearchCard>

        <div class="flex flex-col md:flex-row w-full gap-4 mt-4">
            <div class="w-full xl:w-5/12 lg:w-1/2">
                <div class="card flex flex-col">
                    <div class="font-semibold text-lg sm:text-xl flex items-center justify-between gap-4 h-10">
                        <div class="flex items-center gap-4">
                            <span :class="icons.list"></span>
                            거래처 목록
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
                    />
                </div>
            </div>

            <div class="w-full xl:w-7/12 lg:w-1/2">
                <div class="card flex flex-col">
                    <div class="flex items-center justify-between h-10">
                        <div class="font-semibold text-lg sm:text-xl flex items-center gap-4 whitespace-nowrap">
                            <span :class="[
                                cardMode === 'create' ? icons.add :
                                cardMode === 'edit' ? icons.edit : icons.info,
                            ]"></span>
                            {{
                                cardMode === 'create'
                                    ? '신규 거래처 등록'
                                    : cardMode === 'edit'
                                    ? '거래처 정보 수정'
                                    : '거래처 상세 정보'
                            }}
                        </div>

                        <div class="flex gap-2">
                            <Btn
                                v-if="cardMode === 'view'"
                                icon="cancel"
                                color="secondary"
                                label="선택 해제"
                                class="whitespace-nowrap"
                                @click="handleRowUnSelect"
                                outlined
                            />
                            <Btn
                                v-if="cardMode === 'view'"
                                icon="delete"
                                color="danger"
                                label="삭제"
                                @click="removeVendor"
                                outlined
                            />
                            <Btn
                                v-if="cardMode === 'view'"
                                icon="edit"
                                color="warn"
                                label="수정"
                                class="whitespace-nowrap"
                                @click="handleEdit"
                                outlined
                            />
                            <Btn
                                v-if="cardMode === 'create'"
                                icon="refresh"
                                color="secondary"
                                label="초기화"
                                class="whitespace-nowrap"
                                @click="handleResetForm"
                                outlined
                            />
                            <Btn
                                v-if="cardMode === 'create'"
                                icon="add"
                                label="등록"
                                @click="addVendor"
                                class="whitespace-nowrap"
                                outlined
                            />
                            <Btn
                                v-if="cardMode === 'edit'"
                                icon="cancel"
                                color="secondary"
                                label="취소"
                                @click="handleCancelEdit"
                                class="whitespace-nowrap"
                                outlined
                            />
                            <Btn
                                v-if="cardMode === 'edit'"
                                icon="save"
                                label="저장"
                                @click="modifyVendor"
                                class="whitespace-nowrap"
                                outlined
                            />
                        </div>
                    </div>

                    <Divider />

                    <div v-if="detailLoading" class="animate-pulse">
                        <div class="skeleton w-full h-8 mb-4"></div>
                        <div class="grid grid-cols-2 gap-4 mb-4">
                            <div class="skeleton w-full h-10"></div>
                            <div class="skeleton w-full h-10"></div>
                            <div class="skeleton w-full h-10"></div>
                            <div class="skeleton w-full h-10"></div>
                            <div class="col-span-2 skeleton w-full h-10"></div>
                            <div class="skeleton w-full h-10"></div>
                            <div class="skeleton w-full h-10"></div>
                        </div>
                        <Divider />
                        <div class="skeleton w-1/3 h-6 mb-2"></div>
                        <div class="grid grid-cols-2 gap-4">
                            <div class="skeleton w-full h-10"></div>
                            <div class="skeleton w-full h-10"></div>
                            <div class="col-span-2 skeleton w-full h-10"></div>
                        </div>
                    </div>

                    <div v-else-if="cardMode === 'view' && vendorDetail">
                        <div class="font-semibold text-lg mb-4">{{ vendorDetail.companyName }} ({{ vendorDetail.businessRegistration }})</div>

                        <div class="grid grid-cols-2 gap-4 mb-4">
                            <InputGroup>
                                <InputGroupAddon><i :class="icons.employee" /></InputGroupAddon>
                                <IftaLabel>
                                    <InputText :model-value="vendorDetail.ceoName" disabled />
                                    <label>대표자명</label>
                                </IftaLabel>
                            </InputGroup>

                            <InputGroup>
                                <InputGroupAddon><i :class="icons.phone" /></InputGroupAddon>
                                <IftaLabel>
                                    <InputText :model-value="formatPhone(vendorDetail.phoneNumber)" disabled />
                                    <label>전화번호</label>
                                </IftaLabel>
                            </InputGroup>

                            <div class="col-span-2">
                                <InputGroup>
                                    <InputGroupAddon><i :class="icons.address" /></InputGroupAddon>
                                    <IftaLabel>
                                        <InputText :model-value="vendorDetail.address" disabled />
                                        <label>소재지</label>
                                    </IftaLabel>
                                </InputGroup>
                            </div>

                            <div class="flex flex-col gap-2">
                                <div class="font-semibold text-sm">거래처 유형</div>
                                <div class="flex gap-2">
                                    <span v-for="type in getDisplayTypes(vendorDetail.type)"
                                        :key="type"
                                        class="inline-block px-3 py-1 rounded-full text-xs font-medium text-white"
                                        :class="{
                                            'bg-blue-500': type == 0, // 판매처 (원래 0)
                                            'bg-pink-500': type == 1, // 공급처 (원래 1)
                                        }"
                                    >
                                        {{ getStatusLabel(type) }}
                                    </span>
                                </div>
                            </div>

                            <div class="flex flex-col gap-2">
                                <div class="font-semibold text-sm">사용 여부</div>
                                <span
                                    class="inline-block px-3 py-1 rounded-full text-xs font-medium text-white self-start"
                                    :class="{
                                        'bg-green-500': vendorDetail.isActive === 'Y',
                                        'bg-gray-400': vendorDetail.isActive === 'N'
                                    }"
                                >
                                    {{ getIsActiveLabel(vendorDetail.isActive) }}
                                </span>
                            </div>
                        </div>

                        <Divider />

                        <div class="font-semibold text-base mb-2">담당자 정보</div>
                        <div class="grid grid-cols-2 gap-4">

                            <InputGroup>
                                <InputGroupAddon><i :class="icons.employee" /></InputGroupAddon>
                                <IftaLabel>
                                    <InputText :model-value="vendorDetail.ownerName" disabled />
                                    <label>담당자 이름</label>
                                </IftaLabel>
                            </InputGroup>

                            <InputGroup>
                                <InputGroupAddon><i :class="icons.email" /></InputGroupAddon>
                                <IftaLabel>
                                    <InputText :model-value="vendorDetail.ownerEmail" disabled />
                                    <label>담당자 이메일</label>
                                </IftaLabel>
                            </InputGroup>

                            <div class="col-span-2">
                                <InputGroup>
                                    <InputGroupAddon><i :class="icons.phone" /></InputGroupAddon>
                                    <IftaLabel>
                                        <InputText :model-value="formatPhone(vendorDetail.ownerPhone)" disabled />
                                        <label>담당자 연락처</label>
                                    </IftaLabel>
                                </InputGroup>
                            </div>
                        </div>
                    </div>

                    <div v-else-if="cardMode === 'edit' || cardMode === 'create'">
                        <div class="font-semibold text-base mb-2">기본 정보</div>
                        <div class="grid grid-cols-2 gap-4 mb-4">
                            <InputGroup>
                                <InputGroupAddon><i :class="icons.id" /></InputGroupAddon>
                                <IftaLabel>
                                    <InputText
                                        v-model="editForm.businessRegistration"
                                        :disabled="cardMode === 'edit'"
                                        inputId="editBusinessRegistration"
                                        @input="editForm.businessRegistration = formatBusinessRegistration(editForm.businessRegistration)"
                                        maxlength="12" />
                                    <label for="editBusinessRegistration">사업자 등록번호</label>
                                </IftaLabel>
                            </InputGroup>

                            <InputGroup>
                                <InputGroupAddon><i :class="icons.list" /></InputGroupAddon>
                                <IftaLabel>
                                    <InputText v-model="editForm.companyName" inputId="editCompanyName" />
                                    <label for="editCompanyName">거래처명</label>
                                </IftaLabel>
                            </InputGroup>

                            <InputGroup>
                                <InputGroupAddon><i :class="icons.employee" /></InputGroupAddon>
                                <IftaLabel>
                                    <InputText v-model="editForm.ceoName" inputId="editCeoName" />
                                    <label for="editCeoName">대표자명</label>
                                </IftaLabel>
                            </InputGroup>

                            <InputGroup>
                                <InputGroupAddon><i :class="icons.phone" /></InputGroupAddon>
                                <IftaLabel>
                                    <InputText
                                        v-model="editForm.phoneNumber"
                                        @input="editForm.phoneNumber = formatPhone(editForm.phoneNumber)"
                                        inputId="editPhoneNumber"
                                        maxlength="13" />
                                    <label for="editPhoneNumber">전화번호</label>
                                </IftaLabel>
                            </InputGroup>

                            <div class="col-span-2">
                                <InputGroup>
                                    <InputGroupAddon><i :class="icons.address" /></InputGroupAddon>
                                    <IftaLabel>
                                        <InputText v-model="editForm.address" inputId="editAddress" />
                                        <label for="editAddress">소재지</label>
                                    </IftaLabel>
                                </InputGroup>
                            </div>

                            <div class="flex flex-col gap-2">
                                <div class="font-semibold text-sm mb-1">거래처 유형</div>
                                <div class="flex gap-4 items-center">
                                    <div v-for="opt in STATUS_OPTIONS.filter(o => o.value !== 2)" :key="opt.value" class="flex items-center gap-2">
                                        <Checkbox
                                            v-model="editForm.type"
                                            :inputId="'editType-' + opt.value"
                                            :value="opt.value"
                                        />
                                        <label :for="'editType-' + opt.value">{{ opt.label }}</label>
                                    </div>
                                </div>
                            </div>

                            <div class="flex flex-col gap-2">
                                <div class="font-semibold text-sm mb-1">사용 여부</div>
                                <div class="flex items-center gap-2">
                                    <Checkbox
                                        :binary="true"
                                        v-model="editForm.isActive"
                                        :true-value="'Y'"
                                        :false-value="'N'"
                                        inputId="editIsActiveCheckbox"
                                    />
                                    <label for="editIsActiveCheckbox" class="select-none cursor-pointer">
                                        {{ getIsActiveLabel(editForm.isActive) }}
                                    </label>
                                </div>
                            </div>
                        </div>

                        <Divider />

                        <div class="font-semibold text-base mb-2">담당자 정보</div>
                        <div class="grid grid-cols-2 gap-4">

                            <InputGroup>
                                <InputGroupAddon><i :class="icons.employee" /></InputGroupAddon>
                                <IftaLabel>
                                    <InputText v-model="editForm.ownerName" inputId="editOwnerName" />
                                    <label for="editOwnerName">담당자 이름</label>
                                </IftaLabel>
                            </InputGroup>

                            <InputGroup>
                                <InputGroupAddon><i :class="icons.email" /></InputGroupAddon>
                                <IftaLabel>
                                    <InputText
                                        v-model="editForm.ownerEmail"
                                        @blur="validateEmail(editForm.ownerEmail)"
                                        inputId="editOwnerEmail"
                                        :class="{ 'p-invalid': emailError }"
                                    />
                                    <label for="editOwnerEmail">담당자 이메일</label>
                                </IftaLabel>
                            </InputGroup>
                            <span v-if="emailError" class="col-span-2 text-red-500 text-xs mt-[-10px]">{{ emailError }}</span>

                            <div class="col-span-2">
                                <InputGroup>
                                    <InputGroupAddon><i :class="icons.phone" /></InputGroupAddon>
                                    <IftaLabel>
                                        <InputText
                                            v-model="editForm.ownerPhone"
                                            @input="editForm.ownerPhone = formatPhone(editForm.ownerPhone)"
                                            inputId="editOwnerPhone"
                                            maxlength="13" />
                                        <label for="editOwnerPhone">담당자 연락처</label>
                                    </IftaLabel>
                                </InputGroup>
                            </div>
                        </div>
                    </div>

                    <div v-else class="text-center text-gray-500 p-8">
                        왼쪽 목록에서 거래처를 선택하거나, 새로운 거래처를 등록하세요.
                    </div>
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
