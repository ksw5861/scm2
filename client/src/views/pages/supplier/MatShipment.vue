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

const vendorId = ref('VEN001');
const dateRange = ref({ start: null, end: null }); // 초기값을 객체로
const materialName = ref();
const statusList = ref();
const approveShipData = ref();
const selectedRows = ref();
const warehoustListOpt = ref([]); //창고드롭다운용

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

const approveShipColumns = [
  { label: '출고예정일', field: 'expectDate' },
  { label: '출고지시번호', field: 'shipOrderNo' },
  { label: '자재코드', field: 'matId' },
  { label: '자재명', field: 'matName' },
  { label: '구매처 담당자', field: 'buyerName' },
  { label: '출고수량', field: 'ortQty' },
  { label: '단위', field: 'unit' }
  // { label: '배송지', field: 'toShip', select: true, option: warehoustListOpt },
  // { label: '차량번호', field: 'vehicleNo', inputText: true },
  // { label: '운송번호', field: 'trackingNo', inputText: true }
];

const pageLoad = async () => {
  try {
    const list = await axios.get(`/api/supplier/ApprovedList/${vendorId.value}`);
    console.log(list);

    approveShipData.value = list.data.map((item) => ({
      id: item.purStatusId,
      expectDate: useDateFormat(item.expectDate).value, // Date → 문자열 변환
      shipOrderNo: item.logShipOrderNo, //출고지시번호
      matId: item.matId,
      matName: item.matName,
      buyerName: item.empName,
      ortQty: item.outQty, // 출고수량
      unit: item.unit, // VO에서 unit
      vendorId: item.vendorId, // 거래처코드
      purId: item.purId // 주문테이블아이디
    }));
  } catch (error) {
    toast('error', '리스트 로드 실패', '리스트 불러오기 실패:', '3000');
  }
};

const shipment = async () => {
  const list = JSON.parse(JSON.stringify(selectedRows.value));
  console.log(list);
  // const list = JSON.parse(JSON.stringify(selectedRows.value));

  // const payload = list.map((row) => ({
  //   purId: row.id,
  //   matId: row.matId,
  //   purNo: row.purNo,
  //   expectDate: row.expectDate,
  //   purStatusLogVO: { logSupOutQty: row.outQty }, //이걸 백 로그VO에 넣기위해!
  //   vendorId: vendorId.value //이렇게 넣으면 행 마다 다 들어감.
  // }));

  // console.log(payload);

  // try {
  //   await axios.post('/api/supplier/shipMaterial', payload);
  //   toast('info', '등록 성공', '출고등록  성공:', '3000');
  //   selectedRows.value = [];
  //   await pageLoad();
  // } catch (error) {
  //   toast('error', '등록 실패', '출고등록  실패:', '3000');
  // }
};

//창고리스트: 드롭다운용
const warehouseList = async () => {
  try {
    const res = await axios.get('/api/mat/warehouseList');
    warehoustListOpt.value = res.data.map((item) => ({
      label: item.whName,
      value: item.whId
    }));
    console.log(warehoustListOpt.value);
  } catch (error) {
    toast('error', '리스트 로드 실패', '창고 리스트 불러오기 실패:', '3000');
  }
};

onMounted(() => {
  pageLoad();
  warehouseList();
});
</script>

<template>
  <div class="container">
    <div class="p-4">
      <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    </div>
    <div class="card flex flex-col gap-4">
      <div class="font-semibold text-xl">출고 등록</div>
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
        <btn color="info" icon="pi pi-file-pdf" @click="shipment" label="배송등록" />
      </div>
      <div class="font-semibold text-xl mb-5">출고대기 목록</div>
      <selectTable v-model:selection="selectedRows" :columns="approveShipColumns" :data="approveShipData" :paginator="true" :rows="15" />
    </div>
  </div>
</template>
