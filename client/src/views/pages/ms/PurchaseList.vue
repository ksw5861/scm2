<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import btn from '@/components/common/Btn.vue';
import selectTable from '@/components/common/checkBoxTable.vue';
import SearchField from '@/components/common/SearchBox.vue';
import { useAppToast } from '@/composables/useAppToast';
import { useRoute } from 'vue-router';
import { useIcon } from '@/composables/useIcon';
import { useDateFormat, useNumberFormat } from '@/composables/useFormat';
import Timeline from 'primevue/timeline';

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
const purchaseList = ref();
const selectedRows = ref();
const codeMap = ref({}); // codeId → codeName 매핑용

const pageLoad = async () => {
  try {
    const list = await axios.get('/api/mat/purchaseList');
    purchaseList.value = list.data.map((row) => ({
      id: row.purId,
      regDate: useDateFormat(row.regDate).value,
      purNo: row.purNo,
      matName: row.materialVO.matName,
      reqQty: row.reqQty,
      companyName: row.vendorVO.companyName,
      empName: row.empName
    }));
  } catch (error) {
    toast('error', '리스트 로드 실패', '주문 리스트 불러오기 실패:', '3000');
  }
};

onMounted(() => {
  pageLoad();
  loadStatusCodes();
});

const loadStatusCodes = async () => {
  try {
    const res = await axios.get('/api/mat/status/m01');
    // {"ms1":"요청등록","ms2":"요청승인",...} 형태로 변환
    codeMap.value = res.data.reduce((acc, cur) => {
      acc[cur.codeId] = cur.codeName;
      return acc;
    }, {});
  } catch (err) {
    toast('error', '공통코드 로드 실패', '상태명 불러오기 실패', '3000');
  }
};

const detailInfo = async () => {
  const purId = selectedRows.value.id; //주문테이블 id get!
  console.log(purId);
  try {
    const list = await axios.get('/api/mat/purchaseListStatus', { params: { purId } });
 console.log(list )
    //타임라인
    events.value = list.data.map((item) => ({
      date: useDateFormat(item.reDate).value,
      status: codeMap.value[item.logPurMatStatus] || item.logPurMatStatus
    }));
    //상세테이블
    statusList.value = list.data.map((item) => ({
        updateDate: useDateFormat(item.reDate).value,
        chargeName: item.logName,
        status: codeMap.value[item.logPurMatStatus] || item.logPurMatStatus,
        supOutQty: item.logSupOutQty,
        expectDate: useDateFormat(item.logExpectDate).value
    }));
  } catch (error) {
    toast('error', '상세정보 실패', '상세정보 불러오기 실패:', '3000');
  }
};

const purchaseListColumn = [
  { label: '주문등록일', field: 'regDate' },
  { label: '주문번호', field: 'purNo' },
  { label: '자재명', field: 'matName' },
  { label: '주문수량', field: 'reqQty' },
  { label: '공급처', field: 'companyName' },
  { label: '담당자', field: 'empName' }
];

const statusColumn = [
  { label: '변경일', field: 'updateDate' },
  { label: '담당자', field: 'chargeName' },
  { label: '상태', field: 'status' },
  { label: '공급처 출고수량', field: 'supOutQty' },
  { label: '출고예정일', field: 'expectDate' }
];
</script>

<template>
  <div class="container">
    <div class="p-4">
      <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    </div>
    <div class="card flex flex-col gap-4">
      <div class="font-semibold text-xl">자재주문 조회</div>
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
            <div class="font-semibold text-m">자재주문 목록</div>
            <Divider />
            <selectTable v-model:selection="selectedRows" :selectionMode="'single'" :columns="purchaseListColumn" :data="purchaseList" :paginator="false" :showCheckbox="false" @row-select="detailInfo" />
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
                <template #content="slotProps"> {{ slotProps.item.status }} </template>
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
</template>

<style scoped></style>
