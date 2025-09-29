<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import InputText from 'primevue/inputtext';
import { useIcon } from '@/composables/useIcon';
import { useAppToast } from '@/composables/useAppToast';

const route = useRoute();
const { toast } = useAppToast();

// icons
const iconList = useIcon('list');
const iconAdd = useIcon('add');
const iconEdit = useIcon('edit');
const iconInfo = useIcon('info');
const iconSearch = useIcon('search');

// breadcrumb (same pattern as other pages)
const breadcrumbHome = { icon: useIcon('home'), to: '/' };
const breadcrumbItems = computed(() => {
  const matched = route.matched.filter((r) => r.meta);
  if (!matched.length) return [];
  const current = matched[matched.length - 1];
  const parentLabel = current.meta?.breadcrumb?.parent || '기준 정보';
  const currentLabel = current.name || '';
  return [{ label: parentLabel }, { label: currentLabel, to: route.fullPath }];
});

/*
  Vendor.vue
  - 제품관리 코드 스타일로 구성된 거래처 관리 컴포넌트
  - 왼쪽: 검색 + 목록
  - 오른쪽: 상세 / 등록 폼
*/

// 검색 파라미터 (제품관리와 동일한 스타일)
const searchParams = reactive({
  type: [], // 거래처 유형 (예: 공급처/판매처)
  isActive: '', // 사용여부: '' / 'Y' / 'N'
  companyName: '', // 업체명
  ceoName: '', // 대표자 / 담당자명
  phoneNumber: '', // 전화번호
  businessRegistration: '' // 사업자등록번호
});

// 목록 / 선택 / 상세 / 폼 상태 (products 스타일에 맞춤)
const partners = ref([]);
const selectedPartner = ref(null);

const partnerDetail = reactive({
  vendorId: '',
  businessRegistration: '',
  type: '',
  companyName: '',
  ceoName: '',
  phoneNumber: '',
  address: '',
  isActive: '', // 'Y' or 'N'
  ownerName: '',
  ownerEmail: '',
  ownerPhone: ''
});

const partnerForm = reactive({ ...partnerDetail });

const mode = ref('create'); // create | view | edit
const loading = ref(false);

// pagination / columns (제품관리 코드 스타일)
const page = ref({ page: 1, size: 10, totalElements: 0 });
const columns = [
  { label: '사업자 등록 번호', field: 'businessRegistration' },
  { label: '거래처 유형', field: 'type' },
  { label: '업체명', field: 'companyName' }
];

// normalize response -> helper (some APIs return items / data / array)
const normalizeListResponse = (res) => {
  if (!res) return [];
  if (Array.isArray(res)) return res;
  if (Array.isArray(res.data)) return res.data;
  return res.data?.items ?? res.data?.data ?? [];
};

// API: 목록
const fetchPartnerList = async () => {
  loading.value = true;
  try {
    const params = {
      vendorId: searchParams.businessRegistration || undefined,
      companyName: searchParams.companyName || undefined,
      isActive: searchParams.isActive || undefined,
      type: searchParams.type && searchParams.type.length ? searchParams.type.join(',') : undefined,
      ceoName: searchParams.ceoName || undefined,
      phoneNumber: searchParams.phoneNumber || undefined,
      page: page.value.page,
      size: page.value.size
    };
    const res = await axios.get('/api/vendor', { params });
    const payload = normalizeListResponse(res);
    partners.value = payload;
    page.value.totalElements = res.data?.page?.totalElements ?? payload.length ?? 0;

    // if a partner was selected but is no longer in list, unselect
    if (selectedPartner.value) {
      const key = selectedPartner.value.vendorId ?? selectedPartner.value.businessRegistration;
      const found = partners.value.find((p) => (p.vendorId ?? p.businessRegistration) === key);
      if (!found) handleUnselect();
    }
  } catch (e) {
    console.error('fetchPartnerList error', e);
    toast('error', '조회 실패', '거래처 목록 조회 중 오류가 발생했습니다.');
  } finally {
    loading.value = false;
  }
};

// API: 상세
const fetchPartnerDetail = async (id) => {
  if (!id) return;
  try {
    const res = await axios.get(`/api/vendor/${encodeURIComponent(id)}`);
    const data = Array.isArray(res.data) ? res.data[0] : res.data;
    if (data) {
      Object.assign(partnerDetail, {
        vendorId: data.vendorId ?? data.VENDOR_ID ?? data.businessRegistration ?? '',
        businessRegistration: data.businessRegistration ?? data.BUSINESS_REGISTRATION ?? data.vendorId ?? '',
        type: data.type ?? data.TYPE ?? '',
        companyName: data.companyName ?? data.COMPANY_NAME ?? '',
        ceoName: data.ceoName ?? data.CEO_NAME ?? '',
        phoneNumber: data.phoneNumber ?? data.PHONE_NUMBER ?? '',
        address: data.address ?? data.ADDRESS ?? '',
        isActive: (data.isActive ?? data.IS_ACTIVE ?? '') === true ? 'Y' : (data.isActive ?? data.IS_ACTIVE ?? '') || '',
        ownerName: data.ownerName ?? data.OWNER_NAME ?? '',
        ownerEmail: data.ownerEmail ?? data.OWNER_EMAIL ?? '',
        ownerPhone: data.ownerPhone ?? data.OWNER_PHONE ?? data.ownerTel ?? ''
      });
      Object.assign(partnerForm, partnerDetail);
      selectedPartner.value = data;
      mode.value = 'view';
    } else {
      // clear
      Object.keys(partnerDetail).forEach((k) => (partnerDetail[k] = ''));
      Object.keys(partnerForm).forEach((k) => (partnerForm[k] = ''));
      selectedPartner.value = null;
      mode.value = 'create';
    }
  } catch (e) {
    console.error('fetchPartnerDetail error', e);
    toast('error', '조회 실패', '거래처 상세 조회 중 오류가 발생했습니다.');
  }
};

// CRUD: 등록
const addPartner = async () => {
  if (!partnerForm.companyName) return toast('warn', '등록 실패', '업체명을 입력하세요.');
  try {
    const payload = {
      vendorId: partnerForm.vendorId || partnerForm.businessRegistration || undefined,
      businessRegistration: partnerForm.businessRegistration,
      companyName: partnerForm.companyName,
      ceoName: partnerForm.ceoName,
      phoneNumber: partnerForm.phoneNumber,
      address: partnerForm.address,
      isActive: partnerForm.isActive,
      type: partnerForm.type,
      ownerName: partnerForm.ownerName,
      ownerEmail: partnerForm.ownerEmail,
      ownerPhone: partnerForm.ownerPhone
    };
    const res = await axios.post('/api/vendor', payload);
    toast('success', '등록 성공', '거래처가 등록되었습니다.');
    await fetchPartnerList();
    // try to open newly added detail (API may return id)
    const newId = res.data?.vendorId ?? res.data?.businessRegistration ?? payload.vendorId;
    if (newId) await fetchPartnerDetail(newId);
    else {
      Object.keys(partnerForm).forEach((k) => (partnerForm[k] = ''));
      mode.value = 'create';
    }
  } catch (e) {
    console.error('addPartner error', e);
    toast('error', '등록 실패', '거래처 등록 중 오류가 발생했습니다.');
  }
};

// CRUD: 수정
const modifyPartner = async () => {
  if (!partnerDetail.vendorId && !partnerDetail.businessRegistration) return toast('warn', '수정 실패', '수정할 거래처를 선택하세요.');
  try {
    const id = partnerDetail.vendorId || partnerDetail.businessRegistration;
    const payload = {
      vendorId: partnerForm.vendorId || id,
      businessRegistration: partnerForm.businessRegistration,
      companyName: partnerForm.companyName,
      ceoName: partnerForm.ceoName,
      phoneNumber: partnerForm.phoneNumber,
      address: partnerForm.address,
      isActive: partnerForm.isActive,
      type: partnerForm.type,
      ownerName: partnerForm.ownerName,
      ownerEmail: partnerForm.ownerEmail,
      ownerPhone: partnerForm.ownerPhone
    };
    await axios.put(`/api/vendor/${encodeURIComponent(id)}`, payload);
    toast('success', '수정 성공', '거래처 정보가 수정되었습니다.');
    await fetchPartnerList();
    await fetchPartnerDetail(id);
  } catch (e) {
    console.error('modifyPartner error', e);
    toast('error', '수정 실패', '거래처 수정 중 오류가 발생했습니다.');
  }
};

// CRUD: 삭제
const deletePartner = async () => {
  const id = partnerDetail.vendorId || partnerForm.businessRegistration || partnerForm.vendorId;
  if (!id) return toast('warn', '삭제 실패', '삭제할 거래처를 선택하세요.');
  if (!confirm(`거래처 [${partnerDetail.companyName || partnerForm.companyName}] 삭제하시겠습니까?`)) return;
  try {
    await axios.delete(`/api/vendor/${encodeURIComponent(id)}`);
    toast('success', '삭제 성공', '거래처가 삭제되었습니다.');
    handleUnselect();
    await fetchPartnerList();
  } catch (e) {
    console.error('deletePartner error', e);
    toast('error', '삭제 실패', '거래처 삭제 중 오류가 발생했습니다.');
  }
};

// 이벤트 handlers (제품관리 방식과 동일)
const handleRowSelect = async (row) => {
  const id = row.vendorId ?? row.businessRegistration;
  await fetchPartnerDetail(id);
};

const handleUnselect = () => {
  Object.keys(partnerDetail).forEach((k) => (partnerDetail[k] = ''));
  Object.keys(partnerForm).forEach((k) => (partnerForm[k] = ''));
  selectedPartner.value = null;
  mode.value = 'create';
};

const handleEdit = () => {
  mode.value = 'edit';
};

const handleResetForm = () => {
  if (mode.value === 'view' && (partnerDetail.vendorId || partnerDetail.businessRegistration)) {
    Object.assign(partnerForm, partnerDetail);
  } else {
    Object.keys(partnerForm).forEach((k) => (partnerForm[k] = ''));
  }
};

// 검색 / 초기화 (제품관리 스타일)
const handleSearch = () => {
  page.value.page = 1;
  handleUnselect();
  fetchPartnerList();
};

const handleReset = () => {
  Object.assign(searchParams, {
    type: [],
    isActive: '',
    companyName: '',
    ceoName: '',
    phoneNumber: '',
    businessRegistration: ''
  });
  handleSearch();
};

// init
onMounted(() => fetchPartnerList());
</script>

<template>
  <Fluid>
    <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />

    <!-- 검색 -->
    <SearchCard title="거래처 검색" @search="handleSearch" @reset="handleReset">
      <div class="flex flex-wrap w-full">
        <div class="p-2 w-full md:w-1/4">
          <label class="block text-sm mb-1">거래처 유형</label>
          <div class="flex gap-2">
            <label><input type="checkbox" value="3" v-model="searchParams.type" /> 공급처(3)</label>
            <label><input type="checkbox" value="2" v-model="searchParams.type" /> 판매처(2)</label>
          </div>
        </div>

        <div class="p-2 w-full md:w-1/4">
          <label class="block text-sm mb-1">사용여부</label>
          <div class="flex gap-2">
            <label><input type="radio" value="" v-model="searchParams.isActive" /> 전체</label>
            <label><input type="radio" value="Y" v-model="searchParams.isActive" /> 사용</label>
            <label><input type="radio" value="N" v-model="searchParams.isActive" /> 미사용</label>
          </div>
        </div>

        <div class="p-2 w-full md:w-1/4">
          <IftaLabel>
            <InputText v-model="searchParams.companyName" />
            <label>거래처명</label>
          </IftaLabel>
        </div>

        <div class="p-2 w-full md:w-1/4">
          <IftaLabel>
            <InputText v-model="searchParams.ceoName" />
            <label>대표자/담당자</label>
          </IftaLabel>
        </div>

        <div class="p-2 w-full md:w-1/4">
          <IftaLabel>
            <InputText v-model="searchParams.phoneNumber" />
            <label>전화번호</label>
          </IftaLabel>
        </div>

        <div class="p-2 w-full md:w-1/4">
          <IftaLabel>
            <InputText v-model="searchParams.businessRegistration" />
            <label>사업자 등록번호</label>
          </IftaLabel>
        </div>
      </div>
    </SearchCard>

    <!-- 목록 + 상세 (제품관리와 동일한 레이아웃) -->
    <div class="flex flex-col md:flex-row w-full gap-4 mt-4">
      <!-- 목록 -->
      <div class="w-full xl:w-5/12">
        <div class="card">
          <div class="flex justify-between items-center">
            <div class="font-semibold text-xl flex items-center gap-2">
              <span :class="[iconList, 'text-xl']"></span>
              거래처 목록
            </div>
            <div class="text-sm text-gray-400">총 {{ page.totalElements || partners.length }} 건</div>
          </div>

          <Divider />

          <DTable :columns="columns" :data="partners" :page="page" :loading="loading" dataKey="businessRegistration" v-model:selected="selectedPartner" @row-select="handleRowSelect" @row-unselect="handleUnselect" />
        </div>
      </div>

      <!-- 상세 / 폼 -->
      <div class="w-full xl:w-7/12">
        <div class="card">
          <div class="flex justify-between items-center">
            <div class="font-semibold text-xl flex items-center gap-2">
              <span :class="[mode === 'create' ? iconAdd : mode === 'edit' ? iconEdit : iconInfo, 'text-xl']"></span>
              {{ mode === 'create' ? '신규 거래처 등록' : mode === 'edit' ? '거래처 정보 수정' : '거래처 상세 정보' }}
            </div>

            <div class="flex gap-2">
              <Btn v-if="mode === 'create'" icon="add" @click="addPartner" outlined>등록</Btn>
              <Btn v-if="mode === 'edit'" icon="save" @click="modifyPartner" outlined>저장</Btn>
              <Btn v-if="mode === 'view'" icon="edit" color="warn" @click="handleEdit" outlined>수정</Btn>
              <Btn v-if="mode !== 'create'" icon="delete" color="danger" @click="deletePartner" outlined>삭제</Btn>
              <Btn icon="refresh" color="secondary" @click="handleResetForm" outlined>초기화</Btn>
            </div>
          </div>

          <Divider />

          <div class="grid grid-cols-2 gap-4 mb-4">
            <div>
              <label class="block text-sm">사업자 등록번호</label>
              <InputText v-model="partnerForm.businessRegistration" class="w-full h-10" />
            </div>

            <div>
              <label class="block text-sm">업체명</label>
              <InputText v-model="partnerForm.companyName" class="w-full h-10" />
            </div>

            <div>
              <label class="block text-sm">대표자명</label>
              <InputText v-model="partnerForm.ceoName" class="w-full h-10" />
            </div>

            <div>
              <label class="block text-sm">전화번호</label>
              <InputText v-model="partnerForm.phoneNumber" class="w-full h-10" />
            </div>

            <div class="col-span-2">
              <label class="block text-sm">소재지</label>
              <InputText v-model="partnerForm.address" class="w-full h-10" />
            </div>

            <div>
              <label class="block text-sm">거래처 유형</label>
              <InputText v-model="partnerForm.type" class="w-full h-10" placeholder="공급처/판매처" />
            </div>

            <div>
              <label class="block text-sm">사용여부</label>
              <div class="flex gap-2 items-center">
                <label><input type="radio" value="Y" v-model="partnerForm.isActive" /> 사용</label>
                <label><input type="radio" value="N" v-model="partnerForm.isActive" /> 미사용</label>
              </div>
            </div>
          </div>

          <Divider />

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm">담당자 이름</label>
              <InputText v-model="partnerForm.ownerName" class="w-full h-10" />
            </div>

            <div>
              <label class="block text-sm">담당자 이메일</label>
              <InputText v-model="partnerForm.ownerEmail" class="w-full h-10" />
            </div>

            <div class="col-span-2">
              <label class="block text-sm">담당자 연락처</label>
              <InputText v-model="partnerForm.ownerPhone" class="w-full h-10" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </Fluid>
</template>
