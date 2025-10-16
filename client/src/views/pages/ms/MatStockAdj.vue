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
import { useUserStore } from '@/stores/user';

// Pinia Store
const userStore = useUserStore();

const route = useRoute();
const { toast } = useAppToast();

//등록일 날짜출력[페이지로드시 오늘날짜 자동셋팅]
const getNowDate = () => {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, '0');
  const day = String(today.getDate()).padStart(2, '0');
  return `${year}/${month}/${day}`;
};


const vendorId = ref(userStore.code);
const vanEmpName = ref(userStore.name)
const shipmentDate = ref(getNowDate()); //출고일
const deliveryPlace = ref(''); //배송창고 바인딩용
const carrier = ref()//운송업체
const trackingNo = ref() //운송번호
const carNo = ref()//
//검색
const dateRange = ref({ start: null, end: null }); // 초기값을 객체로
const materialName = ref();
const statusList = ref();

const approveShipData = ref(); //페이지로드시 목록
const warehoustListOpt = ref([]); //창고드롭다운용


const shipDetailList = ref([
  { matId: '', matName: '', ortQty: null, unit: ''}
]);


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
];

const addShipColumns = [
  { label: '자재코드', field: 'matId' },
  { label: '자재명', field: 'matName' },
  { label: '출고수량', field: 'ortQty' },
  { label: '단위', field: 'unit' }
];

//페이지로드시 목록출력
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

//(좌측)제품클릭시 우측테이블 행 push
const addTOshipmentList = (row) => {
console.log(row.id)
  const emptyRowIndex = shipDetailList.value.findIndex((r) => !r.matId);
  const newRow = {
     //테이블 출력데이터
      purStatusId: row.id, //주문로그T아이디: 출고상태값 제어필수
      matId: row.matId,
      matName:row.matName,
      ortQty:row.ortQty,
      unit: row.unit,
    //DB입력시 행별 필요데이터[]
      shipOrderNo: row.logShipOrderNo, //출고지시번호
      purId: row.purId,  //주문테이블 아이디
      buyerName: row.empName,
  };
  if (emptyRowIndex !== -1) {
   shipDetailList.value[emptyRowIndex] = newRow;     // 빈행 있으면 교체
  } else {
   shipDetailList.value.push(newRow);     // 빈행 없으면 push
  }
};

const submit = async () => {
  const list = JSON.parse(JSON.stringify(shipDetailList.value));
  console.log(list);
  //마스터 필요데이터들 [공급처 코드, 운송업체코드, 차량번호, 공장코드, 운송번호, 거래처 담당자이름]
  //디테일 필요데이터 [출고수량, 공급처코드, 자재코드, 주문이력아이디]
  const payload = ({
  //입고마스터
  venName: vanEmpName.value,
  vendorId: vendorId.value,
  //입고상세
  details: list.map((row) => ({
      purId: row.purId,               //주문테이블 아이디
      matId: row.matId,               // 자재 코드
      outQty: row.ortQty,             // 공급처 출고수량
      purStatusId: row.purStatusId,   //주문로그T아이디 : : 출고상태값 제어필수
      vendorId: vendorId.value        // 공급처코드 (공통)
    })),
  //배송정보
  shipmentInfoVO: {
    carrierName: carrier.value,
    vehicleNo: carNo.value,
    shipTo: deliveryPlace.value,
    trackingNo: trackingNo.value
   }
  });
  console.log(payload);

  try {
    await axios.post('/api/supplier/shipment', payload);
    toast('info', '등록 성공', '출고등록  성공:', '3000');

    await pageLoad();
    deliveryPlace.value = '';
    carrier.value = '';
    trackingNo.value = '';
    carNo.value = '';

  } catch (error) {
    toast('error', '등록 실패', '출고등록  실패:', '3000');
  }
};


//창고리스트: 드롭다운용
const warehouseList = async () => {
  try {
    const res = await axios.get('/api/mat/warehouseList');
    warehoustListOpt.value = res.data.map((item) => ({
      name: item.whName,
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
    <div class="flex flex-col md:flex-row gap-8">
      <div class="md:w-1/2">
        <div class="card flex flex-col gap-4 h-full">
          <!-- h-full 고정 -->
          <div class="card flex flex-col gap-4">
            <div class="font-semibold text-m">출고대기 목록</div>
            <Divider />
            <selectTable v-model:selection="selectedRows" :columns="approveShipColumns" :data="approveShipData" :paginator="true" :rows="15" @row-select="addTOshipmentList" :showCheckbox="false"/>
          </div>
        </div>
      </div>
      <div class="md:w-1/2 h-full">
        <div class="card flex flex-col gap-4 h-full">
            <div class="flex items-center justify-between my-3">
                <div class="font-semibold text-m">상세정보</div>
                <div class="flex gap-2">
                    <btn color="warn" icon="pi pi-file-excel" @click="submit" label="배송등록" />
                </div>
            </div>
            <Divider />
            <!-- 상/하단 영역을 flex로 나눔 -->
            <div class="flex flex-col gap-4 h-[600px]"> <!-- 카드 전체 높이 지정 -->
                <!-- 상단 입력폼 (4 비율) -->
                <div class="flex-[4]">
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4 h-full">
                        <SearchField type="readOnly" label="출고일" v-model="shipmentDate" />
                        <SearchField type="readOnly" label="담당자" v-model="vanEmpName" />
                        <searchField type="dropDown" label="배송지" v-model="deliveryPlace" class="w-full" :options="warehoustListOpt" />
                        <searchField type="dropDown" label="운송업체" v-model="carrier" class="w-full":options="[ { name: '대한물류', value: 'Log001' },
                                                                                                                 { name: '경동운송', value: 'Log002' },
                                                                                                                 { name: '신속배송', value: 'Log003' },
                                                                                                                 { name: '정확물류', value: 'Log004' }]" />
                        <SearchField type="text" label="운송번호" v-model="trackingNo" />
                        <SearchField type="text" label="차량번호" v-model="carNo" visibility: hidden/>
                    </div>
                </div>
                <!-- 하단 테이블 (6 비율) -->
                <div class="flex-[6] overflow-auto">
                    <selectTable v-model:selection="selectedRows" :selectionMode="'single'" :columns="addShipColumns" :data="shipDetailList" :paginator="false" :showCheckbox="false" class="h-full"/>
                </div>
            </div>
        </div>
    </div>
</div>
</div>
</template>
