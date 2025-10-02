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

const vendorId = ref('VEN005');
const dateRange = ref({ start: null, end: null }); // 초기값을 객체로
const materialName = ref();
const statusList = ref();
const matOutData = ref();
const selectedRows = ref();

// breadcrumb
const breadcrumbHome = { icon: useIcon('home'), to: '/' };
const breadcrumbItems = computed(() => {
  const matched = route.matched.filter((r) => r.meta);
  if (!matched.length) return [];
  const current = matched[matched.length - 1];
  const parentLabel = current.meta?.breadcrumb?.parent || '주문 조회';
  const currentLabel = current.name || '';
  return [{ label: parentLabel }, { label: currentLabel, to: route.fullPath }];
});

const matOutColumns = [
  { label: '납기요청일', field: 'dueDate' },
  { label: '주문번호', field: 'purNo' },
  { label: '자재코드', field: 'matId' },
  { label: '자재명', field: 'matName' },
  { label: '구매처 담당자', field: 'buyerName' },
  { label: '주문수량', field: 'orderQty' },
  { label: '단위', field: 'unit' },
  { label: '잔여수량', field: 'restQty' },
  { label: '출고수량', field: 'outQty', inputText: true },
  //{ label: '배송지', field: 'outQty', select: true },
  { label: '누적출고수량', field: 'outTotalQty' },
  { label: '상태', field: 'releaseStatus' },
  { label: '출고승인일', field: 'approveDate' }
];

const pageLoad = async () => {
  try {
    const list = await axios.get(`/api/supplier/releaseList/${vendorId.value}`);
    console.log(list);

    matOutData.value = list.data.map((item) => ({
      id: item.purId,
      dueDate: useDateFormat(item.dueDate).value,
      purNo: item.purNo,
      matId: item.matId,
      matName: item.materialVO.matName,
      buyerName: item.empName,
      orderQty: item.reqQty,
      unit: item.materialVO.unit,
      outQty: '',
      outTotalQty: item.outTotalQty,
      restQty: item.reqQty - item.outTotalQty,
      releaseStatus: item.purMatStatus,
      approveDate: useDateFormat(item.purStatusLogVO.reDate).value
    }));
  } catch (error) {
    toast('error', '리스트 로드 실패', '리스트 불러오기 실패:', '3000');
  }
};
//vp
onMounted(() => {
  pageLoad();
});

const shipment = async () => {
  //선택행 없으면 출고처리 선택 안내
  //   if (!matOutData.value.orderQty) {
  //   toast('info', '유효성 검사', '출고 수량을 입력해 주세요.', '3000');
  //   return;
  // }

  // if (matOutData.value.outQty > matOutData.value.orderQty) {
  //   toast('info', '유효성 검사', '주문수량 대비 출고수량이 많습니다.', '3000');
  //   return;
  // }

  //잔여수량대비 많으면 안됨.

  const list = JSON.parse(JSON.stringify(selectedRows.value));

  const payload = list.map((row) => ({
    purId: row.id,
    matId: row.matId,
    purNo: row.purNo,
    purStatusLogVO: { supOutQty: row.outQty }, //이걸 백 로그VO에 넣기위해!
    vendorId: vendorId.value //이렇게 넣으면 행 마다 다 들어감.
  }));

  console.log(payload);

  try {
    const res = await axios.post('/api/supplier/shipMaterial', payload);
    toast('info', '등록 성공', '출고등록  성공:', '3000');
    selectedRows = [];
    pageLoad();

  } catch (error) {
    toast('error', '등록 실패', '출고등록  실패:', '3000');
  }
};
</script>

<template>
  <div class="container">
    <div class="p-4">
      <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    </div>
    <div class="card flex flex-col gap-4">
      <div class="font-semibold text-xl">자재공급</div>
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
    <div class="card flex flex-col gap-4">
      <div class="my-3 flex flex-wrap items-center justify-end gap-2">
        <btn color="info" icon="pi pi-file-pdf" @click="shipment" label="승인" />
      </div>
      <div class="font-semibold text-xl mb-5">출고대기 목록</div>
      <selectTable v-model:selection="selectedRows" :columns="matOutColumns" :data="matOutData" :paginator="true" :rows="15" />
    </div>
  </div>
</template>
