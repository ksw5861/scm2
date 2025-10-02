<script setup>
import { onMounted, ref, computed } from 'vue';
import axios from 'axios';
import btn from '@/components/common/Btn.vue';
import selectTable from '@/components/common/checkBoxTable.vue';
import { useDateFormat, useNumberFormat } from '@/composables/useFormat';
import { useIcon } from '@/composables/useIcon';
import { useAppToast } from '@/composables/useAppToast';
import { useRoute } from 'vue-router';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const { toast } = useAppToast();
const userStore = useUserStore();

// breadcrumb
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
  poNo: '', // 발주번호
  matKeyword: '', // 자재코드/자재명
  regDate: null // 등록일 (Date 또는 [start,end])
});
const loading = ref(false);

/* =========================
   데이터
========================= */
// 1) 자재요청 목록(좌측)
const requestList = ref([]);

// 2) 상세 (우측 상단 필드)
const detailHead = ref({
  workOrderNo: '', // 생산지시번호
  workDate: '', // 생산일
  productId: '', // 제품코드
  productName: '' // 제품명
});

// 3) 상세 하단 테이블(요청/출고 수량)
const detailRows = ref([]); // { woNo, matId, matName, reqQty, issueQty, unit }

/* =========================
   컬럼 정의 (selectTable)
========================= */
// 자재요청 목록
const requestColumns = [
  { field: 'workDate', label: '생산일', style: 'width: 10rem', sortable: true },
  { field: 'workOrderNo', label: '생산지시번호', style: 'width: 12rem' },
  { field: 'productId', label: '제품코드', style: 'width: 10rem' },
  { field: 'productName', label: '제품명', style: 'width: 16rem', sortable: true },
  { field: 'reqCount', label: '요청건수', style: 'width: 8rem' },
  { field: 'charger', label: '담당자', style: 'width: 8rem' }
];

// 상세 테이블
const detailColumns = [
  { field: 'woNo', label: '생산지시번호', style: 'width: 12rem' },
  { field: 'matId', label: '자재코드', style: 'width: 10rem' },
  { field: 'matName', label: '자재명', style: 'width: 18rem', sortable: true },
  { field: 'reqQty', label: '요청수량', style: 'width: 9rem' },
  // 요청수량(출고수량) - 사용자가 수정 가능하도록 editable 옵션
  { field: 'issueQty', label: '출고수량', style: 'width: 9rem', number: true, editable: true },
  { field: 'unit', label: '단위', style: 'width: 6rem' }
];

/* =========================
   API 로딩
========================= */
// 좌측 목록 조회
const loadRequestList = async () => {
  loading.value = true;
  try {
    const params = {
      poNo: search.value.poNo || null,
      keyword: search.value.matKeyword || null,
      regDate: Array.isArray(search.value.regDate) ? search.value.regDate.map((d) => useDateFormat(d).value).join(',') : search.value.regDate ? useDateFormat(search.value.regDate).value : null
    };
    const { data } = await axios.get('/api/mat/issue/requests', { params });
    requestList.value = (data || []).map((r) => ({
      id: r.reqId,
      workOrderNo: r.workOrderNo,
      workDate: useDateFormat(r.workDate).value,
      productId: r.productId,
      productName: r.productName,
      reqCount: r.reqCount,
      charger: r.empName
    }));
  } catch (e) {
    toast('error', '리스트 로드 실패', '자재요청 목록 불러오기 실패', '3000');
  } finally {
    loading.value = false;
  }
};

// 목록에서 행 선택 시 상세 채움
const onSelectRequest = async (row) => {
  try {
    const { data } = await axios.get('/api/mat/issue/requestDetail', { params: { workOrderNo: row.workOrderNo } });
    // 우측 상단 필드
    detailHead.value = {
      workOrderNo: row.workOrderNo,
      workDate: row.workDate,
      productId: row.productId,
      productName: row.productName
    };
    // 상세 테이블
    detailRows.value = (data || []).map((d) => ({
      id: d.mrpDetId || `${row.workOrderNo}-${d.matId}`,
      woNo: row.workOrderNo,
      matId: d.matId,
      matName: d.matName,
      reqQty: d.reqQty,
      issueQty: d.issueQty ?? d.reqQty, // 초기값: 요청=출고
      unit: d.unit
    }));
  } catch (e) {
    toast('error', '상세 로드 실패', '자재요청 상세 불러오기 실패', '3000');
  }
};

/* =========================
   액션
========================= */
// 출고 등록
const submitIssue = async () => {
  if (!detailHead.value.workOrderNo) {
    toast('warn', '선택 필요', '좌측 목록에서 출고할 건을 선택하세요.', '3000');
    return;
  }
  const payload = {
    workOrderNo: detailHead.value.workOrderNo,
    workDate: detailHead.value.workDate,
    productId: detailHead.value.productId,
    productName: detailHead.value.productName,
    lines: detailRows.value.map((r) => ({
      matId: r.matId,
      issueQty: Number(r.issueQty || 0),
      unit: r.unit
    }))
  };

  try {
    await axios.post('/api/mat/issue/register', payload);
    toast('info', '출고등록 완료', '자재 출고가 등록되었습니다.', '4000');
    // 성공 후 목록 및 상세 리프레시
    await loadRequestList();
    if (requestList.value.length) onSelectRequest(requestList.value[0]);
  } catch (e) {
    toast('error', '출고등록 실패', '저장 중 오류가 발생했습니다.', '4000');
  }
};

// 생산계획 불러오기 (별도 테이블 없이 생산계획 DB에서 산출)
const pullPlan = async () => {
  try {
    await axios.post('/api/mat/issue/pullPlan'); // 서버에서 생산계획을 기준으로 요청 집계 처리
    toast('info', '생산계획 반영', '생산계획 기준 자재요청이 갱신되었습니다.', '3500');
    loadRequestList();
  } catch (e) {
    toast('error', '실패', '생산계획 불러오기에 실패했습니다.', '3000');
  }
};

// 조회/초기화
const doSearch = () => loadRequestList();
const doReset = () => {
  search.value = { poNo: '', matKeyword: '', regDate: null };
  loadRequestList();
};

onMounted(() => {
  console.log(userStore.name);
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
        <div class="text-sm text-gray-500 -mt-2">자재별로 출고처리합니다.</div>
        <Divider />
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <label class="text-sm block mb-2">발주번호</label>
            <InputText v-model="search.poNo" placeholder="발주번호" class="w-full" />
          </div>
          <div>
            <label class="text-sm block mb-2">자재코드/자재명</label>
            <InputText v-model="search.matKeyword" placeholder="자재코드/자재명" class="w-full" />
          </div>
          <div>
            <label class="text-sm block mb-2">등록일</label>
            <Calendar v-model="search.regDate" dateFormat="yy-mm-dd" showIcon class="w-full" />
          </div>
          <div class="flex items-end gap-2">
            <btn icon="pi pi-search" :loading="loading" label="조회" @click="doSearch" />
            <btn icon="pi pi-refresh" color="secondary" label="초기화" @click="doReset" />
          </div>
        </div>
      </div>

      <!-- 본문 2단 레이아웃 -->
      <div class="flex flex-col md:flex-row gap-8">
        <!-- 1. 자재요청 목록 -->
        <div class="md:w-1/2">
          <div class="card flex flex-col gap-4 h-full">
            <div class="flex items-center justify-between">
              <div class="font-semibold text-m">① 자재요청 목록</div>
              <btn icon="pi pi-database" color="secondary" label="생산계획불러오기" @click="pullPlan" />
            </div>
            <Divider />
            <selectTable :columns="requestColumns" :data="requestList" :scrollable="true" :paginator="false" :showCheckbox="true" @row-select="onSelectRequest" />
          </div>
        </div>

        <!-- 2. 자재요청 상세 정보 -->
        <div class="md:w-1/2">
          <div class="card flex flex-col gap-4 h-full">
            <div class="flex items-center justify-between">
              <div class="font-semibold text-m">② 자재요청 상세 정보</div>
              <btn icon="pi pi-plus" label="출고등록" @click="submitIssue" />
            </div>
            <Divider />
            <!-- 상세 헤더 필드 -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="text-sm block mb-2">생산지시번호</label>
                <InputText v-model="detailHead.workOrderNo" readonly class="w-full" />
              </div>
              <div>
                <label class="text-sm block mb-2">생산일</label>
                <InputText v-model="detailHead.workDate" readonly class="w-full" />
              </div>
              <div>
                <label class="text-sm block mb-2">제품코드</label>
                <InputText v-model="detailHead.productId" readonly class="w-full" />
              </div>
              <div>
                <label class="text-sm block mb-2">제품명</label>
                <InputText v-model="detailHead.productName" readonly class="w-full" />
              </div>
            </div>

            <!-- 상세 테이블 -->
            <div class="mt-2">
              <selectTable :columns="detailColumns" :data="detailRows" :scrollable="true" :paginator="false" :showCheckbox="false" editable />
            </div>
          </div>
        </div>
      </div>

      <!-- 우측 안내 문구 영역 (모형 참고) -->
      <div class="card">
        <div class="text-center py-8 text-lg md:text-2xl font-semibold leading-relaxed">
          별도 테이블이나 페이지 생성없이<br />
          생산계획 DB로 출고처리
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container :deep(.card) {
  @apply rounded-xl shadow-sm p-4 md:p-6;
}
</style>
