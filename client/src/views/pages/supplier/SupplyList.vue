<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import btn from '@/components/common/Btn.vue';
import Button from 'primevue/button';
import selectTable from '@/components/common/checkBoxTable.vue';
import SearchField from '@/components/common/SearchBox.vue';
import { useAppToast } from '@/composables/useAppToast';
import { useRoute } from 'vue-router';
import { useIcon } from '@/composables/useIcon';
import { useDateFormat, useNumberFormat } from '@/composables/useFormat';
import Timeline from 'primevue/timeline';
import Dialog from 'primevue/dialog';
import { useUserStore } from '@/stores/user';

// Pinia Store
const userStore = useUserStore();
// const vendorId = userStore.code;
const vendorId = ref('V800');
const vendorName = ref('홍길동'); //공급처담당자이름으로 로그찍히니 변경xxx


const route = useRoute();
const { toast } = useAppToast();

// breadcrumb
const breadcrumbHome = { icon: useIcon('home'), to: '/' };
const breadcrumbItems = computed(() => {
  const matched = route.matched.filter((r) => r.meta);
  if (!matched.length) return [];
  const current = matched[matched.length - 1];
  const parentLabel = current.meta?.breadcrumb?.parent || '주문 목록';
  const currentLabel = current.name || '';
  return [{ label: parentLabel }, { label: currentLabel, to: route.fullPath }];
});


const events = ref(); //타임라인용!
const statusList = ref();
const supplyList = ref();
const selectedRows = ref();
const codeMap = ref({}); // codeId → codeName 매핑용
//이미지모달
const imgModal = ref(false);
const defectImg = ref();

const page = ref({ page: 1, size: 10, totalElements: 0 });

const fetchSuppliyList = async () => {
  try {
    const list = await axios.get(`/api/supplier/supplyList/${vendorId.value}`);
    console.log(list);
    // flatMap으로 평탄화
    const flattened = list.data.flatMap((row) =>
      row.details.map((detail) => ({
        id: detail.inboundDetId,
        outDate: useDateFormat(row.vendorOutDate).value,
        outNo: row.venOutNo,
        matName: detail.materialVO.matName,
        outQty: detail.outQty,
        unit: detail.materialVO.stockUnit,
        empName: row.venName
      }))
    );
    //총 데이터 수 계산해서 paginator에 반영
    supplyList.value = flattened;
    page.value.totalElements = flattened.length;
  } catch (error) {
    toast('error', '리스트 로드 실패', '주문 리스트 불러오기 실패:', '3000');
  }
};

const detailInfo = async () => {
  const inboundDetId = selectedRows.value.id;
  console.log(inboundDetId);
  try {
    const list = await axios.get('/api/supplier/supplyDetailList', { params: { inboundDetId } });
    console.log(list);
    //타임라인
    events.value = list.data.map((item) => ({
      date: useDateFormat(item.reDate).value,
      status: codeMap.value[item.logInboundStatus] || item.logInboundStatus,
      rejMemo: item.logMemo,
      logId: item.inLogId
    }));
    //상세테이블
    statusList.value = list.data.map((item) => ({
      id: item.inLogId,
      updateDate: useDateFormat(item.reDate).value,
      chargeName: item.logName,
      status: codeMap.value[item.logInboundStatus] || item.logInboundStatus,
      rejQty: item.logRejQty
    }));
  } catch (error) {
    toast('error', '상세정보 실패', '상세정보 불러오기 실패:', '3000');
  }
};

const loadStatusCodes = async () => {
  try {
    const res = await axios.get('/api/mat/status/ind');
    // {"ms1":"요청등록","ms2":"요청승인",...} 형태로 변환
    codeMap.value = res.data.reduce((acc, cur) => {
      acc[cur.codeId] = cur.codeName;
      return acc;
    }, {});
  } catch (err) {
    toast('error', '공통코드 로드 실패', '상태명 불러오기 실패', '3000');
  }
};

const openImgModal = async (inLogId) => {
  try {
    const res = await axios.get(`/api/img/defect/${inLogId}`, {
      //이미지 데이터는 바이너리로 responseType: 'blob' 옵션을 줘야함!
      responseType: 'blob'
    });
    // blob → 브라우저 URL로 변환
    defectImg.value = URL.createObjectURL(res.data);
  } catch (error) {
    toast('error', '이미지 로드 실패', '이미지불러오기 실패', '3000');
  }

  imgModal.value = true;
};

const closeRejModal = () => {
  imgModal.value = false;
};

const onPage = (event) => {
  const startRow = event.page * event.rows + 1;
  const endRow = (event.page + 1) * event.rows;
  fetchSuppliyList({ startRow, endRow });
};

onMounted(() => {
  fetchSuppliyList();
  loadStatusCodes();
});

const supplyListColumn = [
  { label: '출고일', field: 'outDate' },
  { label: '출고번호', field: 'outNo' },
  { label: '자재명', field: 'matName' },
  { label: '출고수량', field: 'outQty' },
  { label: '단위', field: 'unit' },
  { label: '담당자', field: 'empName' }
];

const statusColumn = [
  { label: '변경일', field: 'updateDate' },
  { label: '발주처 담당자', field: 'chargeName' },
  { label: '상태', field: 'status' },
  { label: '반송 및 불량수량', field: 'rejQty' }
];
</script>

<template>
  <div class="container">
    <div class="p-4">
      <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    </div>
    <div class="card flex flex-col gap-4">
      <div class="font-semibold text-xl">공급 조회</div>
      <Divider />
      <!--search BOX 영역-->
      <div class="flex flex-col gap-4 md:flex-row md:items-end md:gap-6 mt-5 mb-10">
        <SearchField type="dateRange" label="구매요청일자" v-model="dateRange" />
        <SearchField type="textIcon" label="자재명" v-model="materialName" />
        <SearchField type="date" label="등록일" v-model="registerDate" />
        <SearchField
          type="checkbox"
          label="상태"
          v-model="statusList"
          :options="[
            { label: '대기', value: 'WAIT' },
            { label: '진행중', value: 'PROGRESS' },
            { label: '완료', value: 'DONE' },
            { label: '취소', value: 'CANCEL' }
          ]"
        />
        <!-- 버튼 영역 -->
        <div class="flex flex-wrap items-center gap-2">
          <btn color="secondary" icon="pi pi-undo" label="초기화" />
          <btn color="contrast" icon="pi pi-search" label="조회" />
        </div>
      </div>
    </div>
    <!--테이블영역--><!--테이블영역-->
    <div class="flex flex-col md:flex-row gap-8">
      <div class="md:w-1/2 planList">
        <div class="card flex flex-col gap-4 h-full">
          <!-- h-full 고정 -->
          <div class="card flex flex-col gap-4">
            <div class="font-semibold text-m">자재공급 목록</div>
            <Divider />
            <selectTable v-model:selection="selectedRows" :selectionMode="'single'" :columns="supplyListColumn" :data="supplyList" :paginator="true" :showCheckbox="false" @row-select="detailInfo" @page-change="onPage" :page="page" />
          </div>
        </div>
      </div>
      <!--하단우측-->
      <div class="md:w-1/2">
        <div class="card flex flex-col gap-4 h-full">
          <!-- 타이틀 -->
          <div class="card flex flex-col gap-4">
            <div class="font-semibold text-m">상세정보</div>
            <Divider />
            <!--타임라인-->
            <div class="card">
              <Timeline :value="events">
                <template #opposite="slotProps">
                  <small class="text-surface-500 dark:text-surface-400">{{ slotProps.item.date }}</small>
                </template>
                <template #content="slotProps"> [{{ slotProps.item.status }}] {{ slotProps.item.rejMemo }} <btn v-if="slotProps.item.status === '불량'" icon="clip" color="secondary" variant="text" @click="openImgModal(slotProps.item.logId)" outlined :size="size" /> </template>
                <!--d4[불량일경우] 돋보기 아이콘???-->
              </Timeline>
            </div>
            <div>
              <selectTable :columns="statusColumn" :data="statusList" :paginator="false" :showCheckbox="false" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <Dialog v-model:visible="imgModal" modal header="불량 이미지(발주처)" :style="{ width: '500px' }">
    <div class="card flex justify-center">
      <img :src="defectImg" alt="불량 이미지" class="w-full rounded-lg" />
    </div>
    <div class="flex justify-center">
      <btn color="warn" icon="cancel" label="닫기" @click="closeRejModal" />
    </div>
  </Dialog>
</template>

<style scoped></style>
