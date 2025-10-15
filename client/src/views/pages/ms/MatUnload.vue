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

const empName = ref('최길동');

const shipedListData = ref();
const selectedRows = ref();
const selectedDeRow = ref();
const shipDetailListData = ref([{ matId: '', matName: '', ortQty: null, unit: '' }]);
//모달
const returnModal = ref(false);
const returnMemo = ref('');

const pageLoad = async () => {
  try {
    const list = await axios.get(`/api/mat/shipedList`);
    console.log(list);
    shipedListData.value = list.data.map((item) => ({
      id: item.inboundId,
      shipedDate: useDateFormat(item.vendorOutDate).value,
      vanOutNo: item.venOutNo,
      companyName: item.vendorVO.companyName,
      inStatus: item.inboundStatus,
      vanEmpName: item.venName
    }));
  } catch (err) {
    toast('error', '리스트 로드 실패', '주문 리스트 불러오기 실패:', '3000');
  }
};

const detailInfo = async () => {
  try {
    const list = await axios.get('/api/mat/shipedDetailList', { params: { inboundId: selectedRows.value.id } });
    console.log(list);
    //상세테이블
    shipDetailListData.value = list.data.map((item) => ({
      id: item.inboundDetId,
      matId: item.matId,
      matName: item.materialVO.matName,
      outQty: item.outQty,
      unit: item.materialVO.unit,
      purStatusId: item.purStatusId,
      purId: item.purId
    }));
  } catch (error) {
    toast('error', '상세정보 실패', '상세정보 불러오기 실패:', '3000');
  }
};

const approve = async () => {
  try {
    await axios.post('/api/mat/approveUnload', null, { params: { inboundId: selectedRows.value.id, unloadEmp: empName.value } });
    toast('info', '하차승인 성공', '하차승인 성공:', '3000');
    await pageLoad();
    shipDetailListData.value = [{ matId: '', matName: '', ortQty: null, unit: '' }];
  } catch (error) {
    toast('error', '하차승인 실패', '하차승인 실패:', '3000');
  }
};

const returnSubmit = async () => {
  //사유, 담당자, 하차등록시 해당 마스터와 디테일 모두 상태값 변경하고 기록해줘야함. [입고마스터 + 디테일 + 상태변경로그] + @ 발주상태값 반품으로도 가능??/??
  try {
    await axios.post('/api/mat/unloadReturn', null, { params: { inboundId: selectedRows.value.id, unloadEmp: empName.value, rejMemo: returnMemo.value } });
    toast('info', '반품등록 성공', '반품등록 성공:', '3000');
    await pageLoad();
    closeReturnModal();
  } catch (error) {
    toast('error', '반품등록 실패', '반품등록 실패:', '3000');
  }
};

onMounted(() => {
  pageLoad();
});

const openRetrunModal = () => {
  if (selectedRows.value.length === 0) {
    toast('info', '선택 필요', '반품등록할 항목을 출고번호를 선택해주세요:', '3000');
    return;
  }

  toast('info', '반품등록', '반품등록시 전량 반품처리됩니다.', '5000');

  returnModal.value = true;
};

const closeReturnModal = () => {
  returnModal.value = false;
};
const shipedColumn = [
  { label: '출고일', field: 'shipedDate' },
  { label: '출고번호', field: 'vanOutNo' },
  { label: '공급처', field: 'companyName' },
  // { label: '상태', field: 'inStatus' }, //출고 ->  미도착 -> 1) 하차완료 2) 하차거부 -> 입고대기
  { label: '공급처 담당자', field: 'vanEmpName' }
  //운송정보 (운송사, 차량번호, 기사 연락처 등), 송장번호
];

const shipDetailColumn = [
  { label: '자재코드', field: 'matId' },
  { label: '자재명', field: 'matName' },
  { label: '수량', field: 'outQty' },
  { label: '단위', field: 'unit' }
];
</script>

<template>
  <div class="container">
    <div class="p-4">
      <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    </div>
    <div class="card flex flex-col gap-4">
      <div class="font-semibold text-xl">하차 등록</div>
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
    <!--테이블영역-->
    <div class="flex flex-col md:flex-row gap-8">
      <div class="md:w-1/2">
        <div class="card flex flex-col gap-4 h-full">
          <!-- h-full 고정 -->
          <div class="card flex flex-col gap-4">
            <div class="font-semibold text-m">하차대기 목록</div>
            <Divider />
            <selectTable v-model:selection="selectedRows" :selectionMode="'single'" :columns="shipedColumn" :data="shipedListData" :paginator="true" :rows="15" @row-select="detailInfo" />
          </div>
        </div>
      </div>
      <!--하단우측-->
      <div class="md:w-1/2">
        <div class="card flex flex-col gap-4">
          <!-- 버튼 + 제목을 같은 행에 배치 -->
          <div class="flex items-center justify-between my-3">
            <!-- 왼쪽: 제목 -->
            <div class="font-semibold text-m">상세정보</div>
            <!-- 오른쪽: 버튼 -->
            <div class="flex gap-2">
              <btn color="warn" icon="pi pi-file-excel" label="반송" @click="openRetrunModal" />
              <btn color="info" icon="pi pi-file-pdf" label="승인" @click="approve" />
            </div>
          </div>

          <Divider />
          <selectTable v-model:selection="selectedDeRow" :selectionMode="'single'" :columns="shipDetailColumn" :data="shipDetailListData" :paginator="false" :showCheckbox="false" />
        </div>
      </div>
    </div>
  </div>

  <!--반품모달-->
  <Dialog v-model:visible="returnModal" modal header="반송 사유" :style="{ width: '500px' }">
    <div class="card flex justify-center">
      <Textarea v-model="returnMemo" rows="5" cols="100" />
    </div>
    <div class="flex justify-center gap-2">
      <btn color="warn" icon="pi pi-file-excel" label="취소" @click="closeReturnModal" />
      <btn color="warn" icon="pi pi-file-excel" label="등록" @click="returnSubmit" />
    </div>
  </Dialog>
</template>

<scoped>
</scoped>
