<script setup>
import { onMounted, ref, computed, watch } from 'vue';
import axios from 'axios';

// 공용 컴포넌트
import btn from '@/components/common/Btn.vue';
import selectTable from '@/components/common/checkBoxTable.vue';

// PrimeVue
import Breadcrumb from 'primevue/breadcrumb';
import Calendar from 'primevue/calendar';
import InputText from 'primevue/inputtext';
import Divider from 'primevue/divider';

import { useDateFormat } from '@/composables/useFormat';
import { useIcon } from '@/composables/useIcon';
import { useAppToast } from '@/composables/useAppToast';
import { useRoute } from 'vue-router';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const { toast } = useAppToast();
const userStore = useUserStore();

/* =========================
   breadcrumb
========================= */
const breadcrumbHome = { icon: useIcon('home'), to: '/' };
const breadcrumbItems = computed(() => {
  const matched = route.matched.filter((r) => r.meta);
  if (!matched.length) return [];
  const current = matched[matched.length - 1];
  const parentLabel = current.meta?.breadcrumb?.parent || '자재 출고';
  const currentLabel = current.name || '제품생산번호기반 자재출고등록';
  return [{ label: parentLabel }, { label: currentLabel, to: route.fullPath }];
});

/* =========================
   검색/상태
========================= */
const search = ref({
  plId: '', // PL_ID
  prodNo: '', // PROD_NO
  regDate: null // Date | [Date, Date] (PRO_DATE 기준)
});
const loading = ref(false);

/* =========================
   좌측 목록 페이지네이션
========================= */
const page = ref(1);
const pageSize = 5;
const total = ref(0);
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)));
const canPrev = computed(() => page.value > 1 && !loading.value);
const canNext = computed(() => page.value < totalPages.value && !loading.value);

/* =========================
   데이터
========================= */
// 좌측: 생산계획 상세 목록 (PRODUCT_PLAN_DETAIL)
const requestList = ref([]);
// 단일 선택
const selectedRow = ref(null);

// 우측 상단: 선택된 상세 기본 정보
const detailHead = ref({
  plDetId: '',
  plId: '',
  prodNo: '',
  prodId: '',
  proDate: '',
  matStatus: ''
});

// 우측 하단: 요청 상세(REQ_MAT)
const detailRows = ref([]); // { id, woNo, matId, matName?, reqQty, issueQty, unit? }

/* =========================
   우측 상세 페이지네이션 (최대 2건)
========================= */
const dPage = ref({ page: 1, size: 2 }); // size 고정: 2
const dTotal = computed(() => detailRows.value.length);
const dTotalPages = computed(() => Math.max(1, Math.ceil(dTotal.value / dPage.value.size)));
const pagedDetailRows = computed(() => {
  const start = (dPage.value.page - 1) * dPage.value.size;
  return detailRows.value.slice(start, start + dPage.value.size);
});
const canDPrev = computed(() => dPage.value.page > 1);
const canDNext = computed(() => dPage.value.page < dTotalPages.value);
const dPrev = () => {
  if (canDPrev.value) dPage.value.page -= 1;
};
const dNext = () => {
  if (canDNext.value) dPage.value.page += 1;
};

// 합계(요청/출고)
const sumReqQty = computed(() => detailRows.value.reduce((acc, r) => acc + Number(r.reqQty || 0), 0));
const sumIssueQty = computed(() => detailRows.value.reduce((acc, r) => acc + Number(r.issueQty || 0), 0));

/* =========================
   컬럼 정의
========================= */
// ① 좌측: 생산계획 상세 목록
const requestColumns = [
  { field: 'proDate', label: '생산일', style: 'width: 10rem', sortable: true },
  { field: 'prodNo', label: '생산번호', style: 'width: 12rem', sortable: true },
  { field: 'prodId', label: '제품코드', style: 'width: 12rem' },
  { field: 'proQty', label: '계획수량', style: 'width: 10rem' },
  { field: 'matStatus', label: '상태', style: 'width: 10rem' }
];

// ② 우측: 요청 상세(REQ_MAT)
const detailColumns = [
  { field: 'woNo', label: '계획ID', style: 'width: 10rem' },
  { field: 'matId', label: '자재코드', style: 'width: 12rem' },
  { field: 'matName', label: '자재명', style: 'width: 16rem', sortable: true },
  { field: 'reqQty', label: '요청중량', style: 'width: 10rem' },
  { field: 'issueQty', label: '출고중량', style: 'width: 10rem', number: true, editable: true },
  { field: 'unit', label: '단위', style: 'width: 6rem' }
];

/* =========================
   유틸
========================= */
const fmtDate = (d) => (d ? useDateFormat(d).value : null);

/* =========================
   API 로딩
========================= */
/**
 * 좌측 목록: PRODUCT_PLAN_DETAIL 기준 조회 (페이지네이션)
 * GET  http://localhost:8080/api/mat/issue/planDetails
 * q   plId, prodNo, dateFrom, dateTo, page, size
 * res { items: [...], total: number }  또는  [...array]
 */
const loadRequestList = async () => {
  loading.value = true;
  try {
    const params = {
      plId: search.value.plId || null,
      prodNo: search.value.prodNo || null,
      dateFrom: Array.isArray(search.value.regDate) && search.value.regDate[0] ? fmtDate(search.value.regDate[0]) : search.value.regDate ? fmtDate(search.value.regDate) : null,
      dateTo: Array.isArray(search.value.regDate) && search.value.regDate[1] ? fmtDate(search.value.regDate[1]) : null,
      page: page.value,
      size: pageSize
    };

    const { data } = await axios.get('http://localhost:8080/api/mat/issue/planDetails', { params });

    // 백엔드가 {items,total} 주는 경우
    const items = Array.isArray(data) ? data : (data?.items ?? []);
    total.value = Array.isArray(data) ? data.length : Number(data?.total ?? items.length);

    requestList.value = (items || []).map((r) => ({
      id: r.plDetId,
      plDetId: r.plDetId,
      prodId: r.prodId,
      proQty: Number(r.proQty ?? 0),
      proDate: r.proDate ? useDateFormat(r.proDate).value : '',
      plId: r.plId,
      matStatus: r.matStatus ?? '',
      prodNo: r.prodNo ?? ''
    }));

    // 프론트 컷(백엔드가 배열만 주는 경우 목록 5개로 제한)
    if (Array.isArray(data)) {
      const start = (page.value - 1) * pageSize;
      requestList.value = requestList.value.slice(start, start + pageSize);
    }

    // 목록 갱신 시 단일 선택/상세 초기화
    selectedRow.value = null;
    clearDetail();
  } catch (e) {
    console.error(e);
    toast('error', '리스트 로드 실패', '생산계획 상세 목록 불러오기 실패', '3000');
  } finally {
    loading.value = false;
  }
};

const clearDetail = () => {
  detailHead.value = { plDetId: '', plId: '', prodNo: '', prodId: '', proDate: '', matStatus: '' };
  detailRows.value = [];
  dPage.value.page = 1; // 상세 페이지 리셋
};

/**
 * 우측 상세: REQ_MAT 조회
 * GET  http://localhost:8080/api/mat/issue/reqMat?plDetId=..  (조인: MATERIAL로 자재명 포함)
 * res  REQ_ID, MAT_ID, PL_DET_ID, REQ_WEIGHT, matName?, unit?
 */
const loadReqMatByPlDetId = async (row) => {
  try {
    const { data } = await axios.get('http://localhost:8080/api/mat/issue/reqMat', {
      params: { plDetId: row.plDetId }
    });

    detailRows.value = (data || []).map((m) => ({
      id: m.reqId ?? `${row.plDetId}-${m.matId}`,
      woNo: row.plId,
      matId: m.matId,
      matName: m.matName ?? '',
      reqQty: Number(m.reqWeight ?? 0),
      issueQty: Number(m.reqWeight ?? 0),
      unit: m.unit ?? ''
    }));
    dPage.value.page = 1; // 데이터 로드 후 상세 페이지 1로
  } catch (e) {
    console.error(e);
    toast('error', '요청상세 로드 실패', 'REQ_MAT 불러오기 실패', '3000');
  }
};

/**
 * 목록에서 행 선택 → 단일 선택 유지 + 헤더 채우고 + REQ_MAT 조회
 */
const onSelectRequest = async (row) => {
  selectedRow.value = row;

  detailHead.value = {
    plDetId: row.plDetId,
    plId: row.plId,
    prodNo: row.prodNo,
    prodId: row.prodId,
    proDate: row.proDate,
    matStatus: row.matStatus
  };

  await loadReqMatByPlDetId(row);
};

/* =========================
   액션
========================= */
// 출고 등록
const submitIssue = async () => {
  if (!detailHead.value.plDetId) {
    toast('warn', '선택 필요', '좌측 목록에서 출고할 상세를 선택하세요.', '3000');
    return;
  }

  const payload = {
    workOrderNo: String(detailHead.value.plId || ''),
    workDate: detailHead.value.proDate,
    productId: detailHead.value.prodId,
    productName: detailHead.value.prodNo,
    lines: detailRows.value.map((r) => ({
      matId: r.matId,
      issueQty: Number(r.issueQty || 0),
      unit: r.unit
    })),
    plDetId: detailHead.value.plDetId
  };

  try {
    await axios.post('http://localhost:8080/api/mat/issue/register', payload);
    toast('info', '출고등록 완료', '자재 출고가 등록되었습니다.', '4000');
    await loadRequestList();
  } catch (e) {
    console.error(e);
    toast('error', '출고등록 실패', '저장 중 오류가 발생했습니다.', '4000');
  }
};

// “생산계획 반영” (옵션)
const pullPlan = async () => {
  try {
    await axios.post('http://localhost:8080/api/mat/issue/pullPlan');
    toast('info', '생산계획 반영', '생산계획 기준 자재요청이 갱신되었습니다.', '3500');
    loadRequestList();
  } catch (e) {
    console.error(e);
    toast('error', '실패', '생산계획 불러오기에 실패했습니다.', '3000');
  }
};

// 조회/초기화
const doSearch = () => {
  page.value = 1;
  loadRequestList();
};
const doReset = () => {
  search.value = { plId: '', prodNo: '', regDate: null };
  page.value = 1;
  selectedRow.value = null;
  clearDetail();
  loadRequestList();
};

// 좌측 페이지 이동
const goPrev = () => {
  if (canPrev.value) {
    page.value -= 1;
    loadRequestList();
  }
};
const goNext = () => {
  if (canNext.value) {
    page.value += 1;
    loadRequestList();
  }
};

// 검색값 변경 시 좌측 페이지 1로
watch(
  () => [search.value.plId, search.value.prodNo, search.value.regDate],
  () => {
    page.value = 1;
  }
);

onMounted(() => {
  console.log('[user]', userStore?.name);
  loadRequestList();
});
</script>

<template>
  <div class="container">
    <div class="p-4">
      <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    </div>

    <div class="flex flex-col gap-8">
      <!-- 상단 검색 영역 -->
      <div class="card flex flex-col gap-4">
        <div class="font-semibold text-m">자재출고등록</div>
        <div class="text-sm text-gray-500 -mt-2">생산계획 상세(DETAIL) 기준으로 출고 처리합니다.</div>
        <Divider />
        <div class="grid grid-cols-1 md:grid-cols-5 gap-4">
          <div>
            <label class="text-sm block mb-2">계획ID</label>
            <InputText v-model="search.plId" placeholder="PL_ID" class="w-full" />
          </div>
          <div>
            <label class="text-sm block mb-2">생산번호</label>
            <InputText v-model="search.prodNo" placeholder="PROD_NO" class="w-full" />
          </div>
          <div>
            <label class="text-sm block mb-2">생산일자</label>
            <Calendar v-model="search.regDate" selectionMode="range" dateFormat="yy-mm-dd" showIcon class="w-full" />
          </div>
          <div class="md:col-span-2 flex items-end gap-2">
            <btn icon="pi pi-search" :loading="loading" label="조회" @click="doSearch" />
            <btn icon="pi pi-refresh" color="secondary" label="초기화" @click="doReset" />
          </div>
        </div>
      </div>

      <!-- 본문 2단 레이아웃 -->
      <div class="flex flex-col md:flex-row gap-8">
        <!-- 1. 생산계획 상세 목록 -->
        <div class="md:w-1/2">
          <div class="card flex flex-col gap-4 h-full">
            <div class="flex items-center justify-between">
              <div class="font-semibold text-m">① 생산계획 상세 목록</div>
              <btn icon="pi pi-database" color="secondary" label="생산계획반영" @click="pullPlan" />
            </div>
            <Divider />
            <!-- 좌측: 단일 선택 유지 -->
            <selectTable :columns="requestColumns" :data="requestList" :scrollable="true" :paginator="false" :showCheckbox="false" selection-mode="single" v-model:selected="selectedRow" @row-select="onSelectRequest" />
            <!-- 좌측 페이지네이션 -->
            <div class="flex items-center justify-between pt-2">
              <span class="text-sm text-gray-500">총 {{ total }}건 · {{ page }} / {{ totalPages }} 페이지</span>
              <div class="flex gap-2">
                <btn :disabled="!canPrev" color="secondary" icon="pi pi-angle-left" label="이전" @click="goPrev" />
                <btn :disabled="!canNext" color="secondary" icon="pi pi-angle-right" label="다음" @click="goNext" />
              </div>
            </div>
          </div>
        </div>

        <!-- 2. 요청 상세 정보 (REQ_MAT) -->
        <div class="md:w-1/2">
          <div class="card flex flex-col gap-4 h-full">
            <div class="flex items-center justify-between">
              <div class="font-semibold text-m">② 요청 상세 정보</div>
              <btn icon="pi pi-plus" label="출고등록" @click="submitIssue" />
            </div>
            <Divider />
            <!-- 상세 헤더 필드 -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="text-sm block mb-2">상세ID</label>
                <InputText v-model="detailHead.plDetId" readonly class="w-full" />
              </div>
              <div>
                <label class="text-sm block mb-2">계획ID</label>
                <InputText v-model="detailHead.plId" readonly class="w-full" />
              </div>
              <div>
                <label class="text-sm block mb-2">생산번호</label>
                <InputText v-model="detailHead.prodNo" readonly class="w-full" />
              </div>
              <div>
                <label class="text-sm block mb-2">제품코드</label>
                <InputText v-model="detailHead.prodId" readonly class="w-full" />
              </div>
              <div>
                <label class="text-sm block mb-2">생산일자</label>
                <InputText v-model="detailHead.proDate" readonly class="w-full" />
              </div>
              <div>
                <label class="text-sm block mb-2">상태</label>
                <InputText v-model="detailHead.matStatus" readonly class="w-full" />
              </div>
            </div>

            <!-- 요청 상세 테이블 (우측 2건 페이지네이션 적용)
                 ✅ 우측은 selection 비활성화: selection-mode 를 null 로 명시 -->
            <div class="mt-2">
              <selectTable :columns="detailColumns" :data="pagedDetailRows" :scrollable="true" :paginator="false" :showCheckbox="false" :selection-mode="null" editable />
            </div>

            <!-- 합계 + 우측 페이지네이션 바 -->
            <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-2 pt-3">
              <div class="text-sm text-gray-600">
                전체 요청합계: <span class="font-semibold">{{ sumReqQty.toLocaleString() }}</span> · 출고합계: <span class="font-semibold">{{ sumIssueQty.toLocaleString() }}</span>
              </div>
              <div class="flex items-center gap-2">
                <span class="text-sm text-gray-500">총 {{ dTotal }}건 · {{ dPage.page }} / {{ dTotalPages }} 페이지</span>
                <btn :disabled="!canDPrev" color="secondary" icon="pi pi-angle-left" label="이전" @click="dPrev" />
                <btn :disabled="!canDNext" color="secondary" icon="pi pi-angle-right" label="다음" @click="dNext" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container :deep(.card) {
  @apply rounded-xl shadow-sm p-4 md:p-6;
}
:deep(.ellipsis) {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
</style>
