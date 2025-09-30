<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import InputText from 'primevue/inputtext';
import { useIcon } from '@/composables/useIcon';
import { useAppToast } from '@/composables/useAppToast';

/* Hook & Composable ==================================== */

const route = useRoute();
const { toast } = useAppToast();

/* ========================================== */
/* ========================================== */
/* ========================================== */





/* 아이콘 ==================================== */

const iconList = useIcon('list');
const iconAdd = useIcon('add');
const iconEdit = useIcon('edit');
const iconInfo = useIcon('info');
const iconSearch = useIcon('search');

/* ========================================== */
/* ========================================== */
/* ========================================== */





/* 탐색 경로 ==================================== */

const breadcrumbHome = { icon: useIcon('home'), to: '/' };
const breadcrumbItems = computed(() => {
  const matched = route.matched.filter((r) => r.meta);
  if (!matched.length) return [];
  const current = matched[matched.length - 1];
  const parentLabel = current.meta?.breadcrumb?.parent || '기준 정보';
  const currentLabel = current.name || '';
  return [{ label: parentLabel }, { label: currentLabel, to: route.fullPath }];
});

/* ========================================== */
/* ========================================== */
/* ========================================== */





/* 검색 조건 ==================================== */

const searchParams = reactive({

 /* 거래처 유형   */ type: [],        // 0: 공급처 / 1: 판매처
 /* 사용 여부     */ isActive: '',    // 사용여부: 'Y' / 'N'
 /* 업체명       */ companyName: '', // 업체명
 /* 이름         */ name: '',        // 대표자 / 담당자명
 /* 연락처       */ phoneNumber: '', // 업체 연락처 / 담당자 연락처
 /* 사업자등록번호 */ businessRegistration: '' // 사업자등록번호
});

/* ========================================== */
/* ========================================== */
/* ========================================== */





/* 초기화 ==================================== */

// 거래처 목록
const vendorList = ref([]);

// 선택된 거래처
const selectedVendor = ref(null);

// 거래처 상세
const vendorDetail = reactive({
  vendorId: '',
  businessRegistration: '',
  type: '',
  companyName: '',
  ceoName: '',
  phoneNumber: '',
  address: '',
  isActive: '',
  ownerName: '',
  ownerEmail: '',
  ownerPhone: ''
});

// 거래처 입력폼
const vendorForm = reactive({ ...vendorDetail });

// 모드 기본값 == 등록(create)
const mode = ref('create'); // create | view | edit

// 로딩중
const loading = ref(false);

// 페이지네이션
const page = ref({ page: 1, size: 10, totalElements: 0 });

// 거래처 목록 (표시할 컬럼 정보)
const columns = [
  { label: '거래처 유형', field: 'type' },
  { label: '업체명', field: 'companyName' },
  { label: '사업자 등록 번호', field: 'businessRegistration' },
  { label: '거래처 번호', field: 'vendorId' }
];

/* ========================================== */
/* ========================================== */
/* ========================================== */





/* API 호출 ==================================== */

// 목록 조회
const fetchVendorList = async () => {
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
    const payload = res;
    vendorList.value = payload;
    page.value.totalElements = res.data?.page?.totalElements ?? payload.length ?? 0;

    // if a partner was selected but is no longer in list, unselect
    if (selectedVendor.value) {
      const key = selectedVendor.value.vendorId ?? selectedVendor.value.businessRegistration;
      const found = vendorList.value.find((p) => (p.vendorId ?? p.businessRegistration) === key);
      if (!found) handleUnselect();
    }
  } catch (e) {
    console.error('fetchVendorList error', e);
    toast('error', '조회 실패', '거래처 목록 조회 중 오류가 발생했습니다.');
  } finally {
    loading.value = false;
  }
};

// 선택건 단건 조회
const fetchVendorDetail = async (id) => {
  if (!id) return;
  try {
    const res = await axios.get(`/api/vendor/${encodeURIComponent(id)}`);
    const data = Array.isArray(res.data) ? res.data[0] : res.data;
    if (data) {
      Object.assign(vendorDetail, {
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
      Object.assign(vendorForm, vendorDetail);
      selectedVendor.value = data;
      mode.value = 'view';
    } else {
      // clear
      Object.keys(vendorDetail).forEach((k) => (vendorDetail[k] = ''));
      Object.keys(vendorForm).forEach((k) => (vendorForm[k] = ''));
      selectedVendor.value = null;
      mode.value = 'create';
    }
  } catch (e) {
    console.error('fetchVendorDetail error', e);
    toast('error', '조회 실패', '거래처 상세 조회 중 오류가 발생했습니다.');
  }
};

// 거래처 등록
const addPartner = async () => {
  if (!vendorForm.companyName) return toast('warn', '등록 실패', '업체명을 입력하세요.');
  try {
    const payload = {
      vendorId: vendorForm.vendorId || vendorForm.businessRegistration || undefined,
      businessRegistration: vendorForm.businessRegistration,
      companyName: vendorForm.companyName,
      ceoName: vendorForm.ceoName,
      phoneNumber: vendorForm.phoneNumber,
      address: vendorForm.address,
      isActive: vendorForm.isActive,
      type: vendorForm.type,
      ownerName: vendorForm.ownerName,
      ownerEmail: vendorForm.ownerEmail,
      ownerPhone: vendorForm.ownerPhone
    };
    const res = await axios.post('/api/vendor', payload);
    toast('success', '등록 성공', '거래처가 등록되었습니다.');
    await fetchVendorList();
    // try to open newly added detail (API may return id)
    const newId = res.data?.vendorId ?? res.data?.businessRegistration ?? payload.vendorId;
    if (newId) await fetchVendorDetail(newId);
    else {
      Object.keys(vendorForm).forEach((k) => (vendorForm[k] = ''));
      mode.value = 'create';
    }
  } catch (e) {
    console.error('addPartner error', e);
    toast('error', '등록 실패', '거래처 등록 중 오류가 발생했습니다.');
  }
};

// 거래처 수정
const modifyPartner = async () => {
  if (!vendorDetail.vendorId && !vendorDetail.businessRegistration) return toast('warn', '수정 실패', '수정할 거래처를 선택하세요.');
  try {
    const id = vendorDetail.vendorId || vendorDetail.businessRegistration;
    const payload = {
      vendorId: vendorForm.vendorId || id,
      businessRegistration: vendorForm.businessRegistration,
      companyName: vendorForm.companyName,
      ceoName: vendorForm.ceoName,
      phoneNumber: vendorForm.phoneNumber,
      address: vendorForm.address,
      isActive: vendorForm.isActive,
      type: vendorForm.type,
      ownerName: vendorForm.ownerName,
      ownerEmail: vendorForm.ownerEmail,
      ownerPhone: vendorForm.ownerPhone
    };
    await axios.put(`/api/vendor/${encodeURIComponent(id)}`, payload);
    toast('success', '수정 성공', '거래처 정보가 수정되었습니다.');
    await fetchVendorList();
    await fetchVendorDetail(id);
  } catch (e) {
    console.error('modifyPartner error', e);
    toast('error', '수정 실패', '거래처 수정 중 오류가 발생했습니다.');
  }
};

// 거래처 삭제
const deletePartner = async () => {
  const id = vendorDetail.vendorId || vendorForm.businessRegistration || vendorForm.vendorId;
  if (!id) return toast('warn', '삭제 실패', '삭제할 거래처를 선택하세요.');
  if (!confirm(`거래처 [${vendorDetail.companyName || vendorForm.companyName}] 삭제하시겠습니까?`)) return;
  try {
    await axios.delete(`/api/vendor/${encodeURIComponent(id)}`);
    toast('success', '삭제 성공', '거래처가 삭제되었습니다.');
    handleUnselect();
    await fetchVendorList();
  } catch (e) {
    console.error('deletePartner error', e);
    toast('error', '삭제 실패', '거래처 삭제 중 오류가 발생했습니다.');
  }
};

/* ========================================== */
/* ========================================== */
/* ========================================== */





/* 이벤트 핸들러 ==================================== */

// 목록 거래처 행 선택
const handleRowSelect = async (row) => {
  const id = row.vendorId ?? row.businessRegistration;
  await fetchVendorDetail(id);
};

// 목록 거래처 행 선택 해제
const handleUnselect = () => {
  Object.keys(vendorDetail).forEach((k) => (vendorDetail[k] = ''));
  Object.keys(vendorForm).forEach((k) => (vendorForm[k] = ''));
  selectedVendor.value = null;
  mode.value = 'create';
};

// 수정 버튼 (수정 모드로 변경)
const handleEdit = () => {
  mode.value = 'edit';
};

// 폼 초기화 버튼 (입력값 초기화)
const handleResetForm = () => {
  if (mode.value === 'view' && (vendorDetail.vendorId || vendorDetail.businessRegistration)) {
    Object.assign(vendorForm, vendorDetail);
  } else {
    Object.keys(vendorForm).forEach((k) => (vendorForm[k] = ''));
  }
};

// 검색 버튼
const handleSearch = () => {
  page.value.page = 1;
  handleUnselect();
  fetchVendorList();
};

// 검색 조건 초기화 (초기화된 검색조건으로 목록 다시 불러옴)
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

/* ========================================== */
/* ========================================== */
/* ========================================== */





// 초기 init (훅)
onMounted(fetchVendorList);


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
            <label><input type="checkbox" value="0" v-model="searchParams.type" /> 공급처</label>
            <label><input type="checkbox" value="1" v-model="searchParams.type" /> 판매처</label>
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
        <div class="w-full xl:w-5/12 lg:w-1/2">
        <div class="card flex flex-col">

          <div class="font-semibold text-lg sm:text-xl flex items-center justify-between gap-4 h-10">
            <div class="flex items-center gap-4">
              <span :class="iconList"></span>
              거래처 목록
            </div>
            <div class="text-sm text-gray-400">총 {{ page.totalElements || vendorList.length }} 건</div>
          </div>

          <Divider />
          <DTable
            :columns="columns"
            :data="vendorList"
            :page="page"
            :loading="loading"
            dataKey="businessRegistration"
            v-model:selected="selectedVendor"
            @page-change="handlePageChange"
            @row-select="handleRowSelect"
            @row-unselect="handleUnselect"
          />

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
              <InputText v-model="vendorForm.businessRegistration" class="w-full h-10" />
            </div>

            <div>
              <label class="block text-sm">업체명</label>
              <InputText v-model="vendorForm.companyName" class="w-full h-10" />
            </div>

            <div>
              <label class="block text-sm">대표자명</label>
              <InputText v-model="vendorForm.ceoName" class="w-full h-10" />
            </div>

            <div>
              <label class="block text-sm">전화번호</label>
              <InputText v-model="vendorForm.phoneNumber" class="w-full h-10" />
            </div>

            <div class="col-span-2">
              <label class="block text-sm">소재지</label>
              <InputText v-model="vendorForm.address" class="w-full h-10" />
            </div>

            <div>
              <label class="block text-sm">거래처 유형</label>
              <InputText v-model="vendorForm.type" class="w-full h-10" placeholder="공급처/판매처" />
            </div>

            <div>
              <label class="block text-sm">사용여부</label>
              <div class="flex gap-2 items-center">
                <label><input type="radio" value="Y" v-model="vendorForm.isActive" /> 사용</label>
                <label><input type="radio" value="N" v-model="vendorForm.isActive" /> 미사용</label>
              </div>
            </div>
          </div>

          <Divider />

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm">담당자 이름</label>
              <InputText v-model="vendorForm.ownerName" class="w-full h-10" />
            </div>

            <div>
              <label class="block text-sm">담당자 이메일</label>
              <InputText v-model="vendorForm.ownerEmail" class="w-full h-10" />
            </div>

            <div class="col-span-2">
              <label class="block text-sm">담당자 연락처</label>
              <InputText v-model="vendorForm.ownerPhone" class="w-full h-10" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </Fluid>
</template>
