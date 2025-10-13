<script setup>
import { useRoute } from 'vue-router';
import { computed, ref, reactive, watchEffect, onMounted } from 'vue';
import axios from 'axios';
import { useIcon } from '@/composables/useIcon';
import { useAppToast } from '@/composables/useAppToast';

/* ───────────────────────────────
 *  공통 composable
 * ─────────────────────────────── */
const route = useRoute();
const { toast } = useAppToast();

/* ───────────────────────────────
 *  아이콘 세트
 * ─────────────────────────────── */
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
};

/* ───────────────────────────────
 *  Breadcrumb 구성
 * ─────────────────────────────── */
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

/* ───────────────────────────────
 *  권한 옵션
 * ─────────────────────────────── */
const TYPE_OPTIONS = [
  { label: '관리자', value: [0] },
  { label: '사원', value: [1] },
  { label: '거래처', value: [2, 3, 4] }
];

/* ───────────────────────────────
 *  상태 / 이벤트
 * ─────────────────────────────── */
const type = ref([0, 1, 2, 3, 4, 5]); // 실제 검색용 권한 배열
const dummyChecked = reactive({ 관리자: false, 사원: false, 거래처: false }); // UI 표시용

const searchParams = reactive({
  isActive: ['Y'],
  name: null,
  email: null,
  phone: null
});

const page = reactive({
  page: 1,
  size: 10,
  totalElements: 0
});

const loading = ref(false);
const account = ref([]);

/* ───────────────────────────────
 *  권한 체크 핸들러
 * ─────────────────────────────── */
const isTypeChecked = (value) => {
  if (Array.isArray(value)) {
    return value.every(v => type.value.includes(v));
  } else {
    return type.value.includes(value);
  }
};

const toggleType = (value) => {
  const values = Array.isArray(value) ? value : [value];
  const allIncluded = values.every(v => type.value.includes(v));
  if (allIncluded) {
    type.value = type.value.filter(v => !values.includes(v));
  } else {
    type.value = [...new Set([...type.value, ...values])];
  }
};

/* UI 체크박스와 데이터 동기화 */
watchEffect(() => {
  TYPE_OPTIONS.forEach(opt => {
    dummyChecked[opt.label] = isTypeChecked(opt.value);
  });
});

/* ───────────────────────────────
 *  데이터 조회
 * ─────────────────────────────── */
const handleSearch = async () => {
  loading.value = true;
  try {
    const params = {
      page: page.page,
      size: page.size,
      ...searchParams,
      type: type.value.length ? type.value : null
    };
    const res = await axios.get('/api/account', { params });
    account.value = res.data?.data || [];
    if (res.data?.page) Object.assign(page, res.data.page);
  } catch (err) {
    console.error(err);
    toast('error', '조회 실패', '계정 목록 조회 중 오류가 발생했습니다.');
  } finally {
    loading.value = false;
  }
};

const handleReset = () => {
  searchParams.isActive = ['Y'];
  type.value = [0, 1, 2, 3, 4, 5];
  searchParams.name = null;
  searchParams.email = null;
  searchParams.phone = null;
  page.page = 1;
  handleSearch();
};

const handlePageChange = (event) => {
  page.page = event.page + 1;
  page.size = event.rows;
  handleSearch();
};

/* ───────────────────────────────
 *  액션 핸들러
 * ─────────────────────────────── */
const toggleActive = async (row) => {
  try {
    const newStatus = row.isActive === 'Y' ? 'N' : 'Y';
    if (!confirm(`정말 ${newStatus === 'Y' ? '활성화' : '비활성화'}하시겠습니까?`)) return;
    const { status } = await axios.put(`/api/account/${row.accountId}/active`, { isActive: newStatus });
    if (status === 200) {
      handleSearch();
      toast('success', '변경 완료', `계정이 ${newStatus === 'Y' ? '활성화' : '비활성화'}되었습니다.`);
    } else {
      toast('error', '변경 실패', '예상치 못한 응답입니다.');
    }
  } catch (err) {
    toast('error', '변경 실패', '상태 변경 중 오류가 발생했습니다.');
  }
};

const resetPassword = async (row) => {
  try {
    if (!confirm("임시 비밀번호를 재설정하여 사용자의 이메일로 발송합니다.")) return;
    const { status } = await axios.post(`/api/account/${row.accountId}/reset-password`);
    if (status === 200) {
      toast('success', '비밀번호 재설정', `${row.name}님의 임시 비밀번호가 이메일로 발송되었습니다.`);
    } else {
      toast('error', '실패', '비밀번호 재설정 중 오류가 발생했습니다.');
    }
  } catch (err) {
    toast('error', '실패', '비밀번호 재설정 중 오류가 발생했습니다.');
  }
};

onMounted(() => handleSearch());
</script>

<template>
  <Fluid>
    <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />

    <!-- 검색 -->
    <SearchCard title="계정 검색" @search="handleSearch" @reset="handleReset">
      <div class="flex flex-wrap w-full">

        <!-- 1행: 권한 / 사용 여부 -->
        <div class="flex flex-wrap p-2 gap-4 xl:gap-0 w-full xl:w-full">
          <!-- 권한 -->
          <div class="flex flex-col gap-2 w-full xl:w-1/4 lg:w-1/2">
            <label class="font-semibold mb-1">권한</label>
            <div class="flex flex-wrap gap-4">
              <div
                v-for="opt in TYPE_OPTIONS"
                :key="opt.label"
                class="flex items-center gap-2"
              >
                <Checkbox
                  v-model="dummyChecked[opt.label]"
                  :inputId="'searchType-' + opt.label"
                  :binary="true"
                  @change="toggleType(opt.value)"
                />
                <label :for="'searchType-' + opt.label" class="select-none cursor-pointer">
                  {{ opt.label }}
                </label>
              </div>
            </div>
          </div>

          <!-- 활성화 여부 -->
          <div class="flex flex-col gap-2 w-full xl:w-1/4 lg:w-1/2">
            <label class="font-semibold mb-1">활성화 여부</label>
            <div class="flex flex-wrap gap-4">
              <div
                v-for="opt in [{label:'활성', value:'Y'}, {label:'비활성', value:'N'}]"
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

        <!-- 2행: 소유자명 / 이메일 / 연락처 -->
        <div class="flex flex-col gap-2 p-2 w-full xl:w-1/4 lg:w-1/2">
          <InputGroup>
            <InputGroupAddon><i :class="icons.employee" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchParams.name" inputId="searchName" />
              <label for="searchName">소유자명</label>
            </IftaLabel>
          </InputGroup>
        </div>

        <div class="flex flex-col gap-2 p-2 w-full xl:w-1/4 lg:w-1/2">
          <InputGroup>
            <InputGroupAddon><i :class="icons.email" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchParams.email" inputId="searchEmail" />
              <label for="searchEmail">이메일</label>
            </IftaLabel>
          </InputGroup>
        </div>

        <div class="flex flex-col gap-2 p-2 w-full xl:w-1/4 lg:w-1/2">
          <InputGroup>
            <InputGroupAddon><i :class="icons.phone" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchParams.phone" inputId="searchPhone" maxlength="13" />
              <label for="searchPhone">연락처</label>
            </IftaLabel>
          </InputGroup>
        </div>
      </div>
    </SearchCard>

    <!-- 목록 -->
    <div class="w-full mt-4">
      <div class="card flex flex-col">
        <div class="flex justify-between items-center mb-2">
          <div class="flex items-center gap-2 font-semibold text-lg">
            <span :class="icons.list"></span> 계정 목록
          </div>
          <div class="text-sm text-gray-500">
            총 <b class="text-gray-800">{{ page.totalElements }}</b>건
          </div>
        </div>

        <Divider />

        <DataTable
          :value="account"
          :paginator="true"
          :lazy="true"
          :rows="page.size"
          :totalRecords="page.totalElements"
          :loading="loading"
          dataKey="accountId"
          :first="(page.page - 1) * page.size"
          :stripedRows="true"
          :rowHover="true"
          showGridlines
          responsiveLayout="scroll"
          @page="handlePageChange"
        >
          <Column field="division" header="권한">
            <template #body="{ data }">
              {{ data.division === '0' ? '관리자' : data.division === '1' ? '사원' : '거래처' }}
            </template>
          </Column>
          <Column field="name" header="소유자명" />
          <Column field="email" header="이메일" />
          <Column field="isActive" header="활성화 여부">
            <template #body="{ data }">
              {{ data.isActive === 'Y' ? '활성' : '비활성' }}
            </template>
          </Column>
          <Column field="phone" header="연락처" />
          <Column field="createdAt" header="생성일자" />
          <Column field="code" header="고유 번호" />
          <Column field="accountId" header="계정 번호" />
          <Column bodyClass="text-center whitespace-nowrap text-xs xl:text-sm">
            <template #body="{ data }">
              <div class="flex justify-center gap-2 flex-wrap">
                <Button
                  size="small"
                  variant="outlined"
                  :icon="data.isActive === 'Y' ? 'pi pi-ban' : 'pi pi-check'"
                  :label="data.isActive === 'Y' ? '비활성화' : '활성화'"
                  :severity="data.isActive === 'Y' ? 'danger' : 'success'"
                  @click="toggleActive(data)"
                />
                <Button
                  size="small"
                  variant="outlined"
                  icon="pi pi-key"
                  label="비밀번호 재설정"
                  :disabled="data.isActive !== 'Y'"
                  @click="resetPassword(data)"
                />
              </div>
            </template>
          </Column>
        </DataTable>
      </div>
    </div>
  </Fluid>
</template>
