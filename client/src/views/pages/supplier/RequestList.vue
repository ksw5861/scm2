<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import btn from '@/components/common/Btn.vue';
import DataTable from '@/components/common/DTable.vue';

const startDate = ref(null);
const endDate = ref(null);
const materialName = ref('');
const statusList = ref([]);

const masterColumns = [
  { label: '구매요청일자', field: 'requestDate', sortable: true },
  { label: '구매요청번호', field: 'requestId', sortable: true },
  { label: '품목 수', field: 'materialCount', sortable: true },
  { label: '구매처 담당자', field: 'buyerName', sortable: true }
];

const Masterdata = ref([
  { requestDate: '2025-09-22', requestId: 'REQ-20250922-001', materialCount: 5, buyerName: '김민준' },
  { requestDate: '2025-09-21', requestId: 'REQ-20250921-002', materialCount: 3, buyerName: '이서아' },
  { requestDate: '2025-09-21', requestId: 'REQ-20250921-001', materialCount: 8, buyerName: '박서준' },
  { requestDate: '2025-09-20', requestId: 'REQ-20250920-005', materialCount: 2, buyerName: '최지아' },
  { requestDate: '2025-09-20', requestId: 'REQ-20250920-004', materialCount: 1, buyerName: '강하준' },
  { requestDate: '2025-09-19', requestId: 'REQ-20250919-003', materialCount: 12, buyerName: '조은우' },
  { requestDate: '2025-09-18', requestId: 'REQ-20250918-002', materialCount: 4, buyerName: '윤하윤' },
  { requestDate: '2025-09-18', requestId: 'REQ-20250918-001', materialCount: 6, buyerName: '임도윤' },
  { requestDate: '2025-09-17', requestId: 'REQ-20250917-001', materialCount: 9, buyerName: '한지호' },
  { requestDate: '2025-09-16', requestId: 'REQ-20250916-003', materialCount: 7, buyerName: '오서아' },
  { requestDate: '2025-09-16', requestId: 'REQ-20250916-002', materialCount: 2, buyerName: '서예준' },
  { requestDate: '2025-09-16', requestId: 'REQ-20250916-001', materialCount: 1, buyerName: '권도현' },
  { requestDate: '2025-09-15', requestId: 'REQ-20250915-004', materialCount: 15, buyerName: '황시우' },
  { requestDate: '2025-09-15', requestId: 'REQ-20250915-003', materialCount: 3, buyerName: '송지민' },
  { requestDate: '2025-09-14', requestId: 'REQ-20250914-002', materialCount: 5, buyerName: '정유나' },
  { requestDate: '2025-09-14', requestId: 'REQ-20250914-001', materialCount: 8, buyerName: '홍길동' },
  { requestDate: '2025-09-13', requestId: 'REQ-20250913-001', materialCount: 2, buyerName: '배서연' },
  { requestDate: '2025-09-12', requestId: 'REQ-20250912-003', materialCount: 11, buyerName: '신지후' },
  { requestDate: '2025-09-12', requestId: 'REQ-20250912-002', materialCount: 6, buyerName: '안수아' },
  { requestDate: '2025-09-12', requestId: 'REQ-20250912-001', materialCount: 4, buyerName: '유재석' }
]);

const detailColumns = [
  { label: '납기요청일', field: 'reqDate', sortable: true },
  { label: '품목코드', field: 'itemCode', sortable: true },
  { label: '품목명', field: 'itemName', sortable: true },
  { label: '규격', field: 'spec', sortable: true },
  { label: '단위', field: 'unit', sortable: true },
  { label: '요청수량', field: 'requestQty', sortable: true },
  { label: '상태', field: 'status', sortable: true }
];

const detaildata = ref([
  { reqDate: '2025-09-15', itemCode: 'MAT-001', itemName: '철근', spec: 'D10, 6m', unit: '개', requestQty: 100, status: '대기' },
  { reqDate: '2025-09-15', itemCode: 'MAT-002', itemName: '시멘트', spec: '포대, 40kg', unit: '포대', requestQty: 50, status: '진행중' }
]);
</script>

<template>
  <div class="container">
    <div class="card flex flex-col gap-4">
      <div class="font-semibold text-xl">자재구매요청 조회</div>
      <Divider />
      <!-- 한 줄에 나란히: md 이상에서 가로, 모바일은 세로 -->
      <div class="flex flex-col gap-4 md:flex-row md:items-end md:gap-6 mt-5 mb-10">
        <!-- 구매요청일자 -->
        <div class="flex flex-col">
          <label class="whitespace-nowrap mb-3">구매요청일자</label>
          <div class="flex items-center gap-2">
            <DatePicker v-model="startDate" showIcon fluid :showOnFocus="false" />
            <span> ~ </span>
            <DatePicker v-model="endDate" showIcon fluid :showOnFocus="false" />
          </div>
        </div>
        <!-- 자재명 -->
        <div class="flex flex-col">
          <label class="whitespace-nowrap mb-3">자재명</label>
          <IconField iconPosition="left" class="w-64">
            <InputText v-model="materialName" class="w-full" />
            <InputIcon class="pi pi-search" />
          </IconField>
        </div>
        <!--구매처 담당자-->
        <div class="flex flex-col">
          <label class="whitespace-nowrap mb-3">구매처 담당자</label>
          <InputText v-model="materialName" class="w-full" />
        </div>
        <!-- 상태값 (체크박스) -->
        <div class="flex flex-col">
          <label class="whitespace-nowrap mb-7">상태</label>
          <div class="flex flex-wrap items-center gap-4">
            <div class="flex items-center gap-2">
              <Checkbox v-model="statusList" inputId="st_wait" value="WAIT" />
              <label for="st_wait">대기</label>
            </div>
            <div class="flex items-center gap-2">
              <Checkbox v-model="statusList" inputId="st_prog" value="PROGRESS" />
              <label for="st_prog">진행중</label>
            </div>
            <div class="flex items-center gap-2">
              <Checkbox v-model="statusList" inputId="st_done" value="DONE" />
              <label for="st_done">완료</label>
            </div>
            <div class="flex items-center gap-2">
              <Checkbox v-model="statusList" inputId="st_cancel" value="CANCEL" />
              <label for="st_cancel">취소</label>
            </div>
          </div>
        </div>
        <!-- 버튼영역-->
        <div class="flex flex-wrap items-center gap-2">
          <btn color="secondary" icon="pi pi-undo"> 초기화 </btn>
          <btn color="contrast" icon="pi pi-search"> 조회 </btn>
        </div>
      </div>
    </div>
    <!--검색박스 end-->

    <!--중간버튼영역-->
    <div class="my-3 flex flex-wrap items-center justify-end gap-2">
      <btn color="contrast" icon="pi pi-plus"> Excel 다운로드 </btn>
      <btn color="warn" icon="pi pi-file-excel"> 반려 </btn>
      <btn color="info" icon="pi pi-file-pdf"> 승인 </btn>
    </div>

    <!--1/2 영역-->
    <div class="flex flex-col md:flex-row gap-8">
      <div class="md:w-1/2 planList">
        <div class="card flex flex-col gap-4">
          <div class="font-semibold text-xl mb-5">조회 내역</div>
          <DataTable :columns="masterColumns" :data="Masterdata" :paginator="true" :rows="15" />
        </div>
      </div>

      <div class="md:w-1/2 header">
        <div class="card flex flex-col gap-4">
          <div class="font-semibold text-xl mb-5">조회 상세내역</div>
          <DataTable :columns="detailColumns" :data="detaildata" :paginator="true" :rows="15" />
        </div>
      </div>
    </div>
    <!--1/2 영역 end-->
  </div>
</template>

<style scoped></style>
