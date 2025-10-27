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
import SearchCard from '@/components/card/SearchCard.vue';
import DatePicker from 'primevue/datepicker';

// Pinia Store
// (userStore.name)이름
// (userStore.code)코드 - 계정기준으로
const userStore = useUserStore();
const vendorId = userStore.code;
const vanEmpName = '배송담당';

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

const shipmentDate = ref(getNowDate()); //출고일
const deliveryPlace = ref(''); //배송창고 바인딩용
const carrier = ref()//운송업체
const trackingNo = ref() //운송번호
const carNo = ref()
const approveShipData = ref(); //페이지로드시 목록
const warehoustListOpt = ref([]); //창고드롭다운용

const page = ref({ page: 1, size: 10, totalElements: 0 });

const shipDetailList = ref([])
//검색필드
const searchFilter = ref({
  startDate: '',
  endDate: '',
  matName: '',
  toWarehouse: ''
});

//datePicker날짜변환
const formatDate = (date) => {
   if (!date) return '';
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

//페이지로드시 목록출력
const pageLoad = async () => {

    //검색필터 기간 유효성검사
    if (searchFilter.value.startDate && searchFilter.value.endDate) {
        if (new Date(searchFilter.value.startDate) > new Date(searchFilter.value.endDate)) {
            toast('warn', '기간 오류', '시작일은 종료일보다 이전이어야 합니다.', '3000');
            return;
        }
        }

    const params = {
        startDate: formatDate(searchFilter.value.startDate),
        endDate: formatDate(searchFilter.value.endDate),
        matName: searchFilter.value.matName,
        toWarehouse: searchFilter.value.toWarehouse
    }
    try {
      const list = await axios.get(`/api/sApprovedList/${vendorId}`, { params });
      console.log(list);

      approveShipData.value = list.data.map((item) => ({
        id: item.purStatusId,
        expectDate: useDateFormat(item.expectDate).value, // Date → 문자열 변환
        shipOrderNo: item.logShipOrderNo, //출고지시번호
        matId: item.matId,
        matName: item.matName,
        buyerName: item.empName,
        ortQty: item.outQty, // 출고수량
        unit: item.stockUnit, // VO에서 unit
        vendorId: item.vendorId, // 거래처코드
        purId: item.purId, // 주문테이블아이디
        toWarehouse: item.warehouseName
    }));

    page.value.totalElements = approveShipData.value.length;

  } catch (error) {
    toast('error', '리스트 로드 실패', '리스트 불러오기 실패:', '3000');
  }
};

const submit = async () => {

    //유효성 검사
    if (!deliveryPlace.value || !carrier.value || !trackingNo.value || !carNo.value) {
      toast('warn', '유효성 검사', '배송지, 운송업체, 운송번호는 필수 입력값입니다.', '3000');
      return;
    }
    if (shipDetailList.value.length === 0 ) {
      toast('warn', '유효성 검사', '출고할 자재를 선택해주세요.', '3000');
      return;
    }
    if(!confirm('출고등록 하시겠습니까?')) {
      return;
    }

  const list = JSON.parse(JSON.stringify(shipDetailList.value));
  console.log(list);
  //마스터 필요데이터들 [공급처 코드, 운송업체코드, 차량번호, 공장코드, 운송번호, 거래처 담당자이름]
  //디테일 필요데이터 [출고수량, 공급처코드, 자재코드, 주문이력아이디]
  const payload = ({
  //입고마스터
  venName: vanEmpName,
  vendorId: vendorId,
  //입고상세
  details: list.map((row) => ({
                    purId: row.purId,               //주문테이블 아이디
                    matId: row.matId,               // 자재 코드
                    outQty: row.ortQty,             // 공급처 출고수량
                    purStatusId: row.purStatusId,   //주문로그T아이디 : : 출고상태값 제어필수
                    vendorId: vendorId        // 공급처코드 (공통)
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

  // try {
  //   await axios.post('/api/sshipment', payload);
  //   toast('info', '등록 성공', '출고등록  성공:', '3000');

  //   await pageLoad();
  //   deliveryPlace.value = '';
  //   carrier.value = '';
  //   trackingNo.value = '';
  //   carNo.value = '';
  //   shipDetailList.value = [{ matId: '', matName: '', ortQty: null, unit: ''}];

  // } catch (error) {
  //   toast('error', '등록 실패', '출고등록  실패:', '3000');
  // }
};

//선택토글
const toggleSelection = (row) => {
  const existingIndex = shipDetailList.value.findIndex((r) => r.purStatusId === row.id);

  if (existingIndex !== -1) {
    shipDetailList.value.splice(existingIndex, 1);
  } else {
    shipDetailList.value.push({
      id: row.id,
      purStatusId: row.id,
      matId: row.matId,
      matName: row.matName,
      ortQty: row.ortQty,
      unit: row.unit,
      purName: row.buyerName,
      shipOrderNo: row.logShipOrderNo,
      purId: row.purId,
      buyerName: row.empName,
    });
  }
};

//창고리스트: 드롭다운용
const warehouseList = async () => {
  try {
    const res = await axios.get('/api/mwarehouseList');
    warehoustListOpt.value = res.data.map((item) => ({
      name: item.whName,
      value: item.whId
    }));
    console.log(warehoustListOpt.value);
  } catch (error) {
    toast('error', '리스트 로드 실패', '창고 리스트 불러오기 실패:', '3000');
  }
};

const onPage = (event) => {
  const startRow = event.page * event.rows + 1;
  const endRow = (event.page + 1) * event.rows;
  pageLoad({startRow, endRow});
};

const resetSearch = () => {
  searchFilter.value = {
    startDate: '',
    endDate: '',
    matName: '',
    toWarehouse: ''
  };
  pageLoad();
};

const formreset = () => {
  shipmentDate.value = getNowDate();
  deliveryPlace.value = '';
  carrier.value = '';
  trackingNo.value = '';
  carNo.value = '';
  shipDetailList.value = [];
};

onMounted(() => {
  pageLoad();
  warehouseList();
});


const approveShipColumns = [
  { label: '출고예정일', field: 'expectDate' },
  { label: '출고지시번호', field: 'shipOrderNo' },
  { label: '자재코드', field: 'matId' },
  { label: '자재명', field: 'matName' },
  //{ label: '구매처 담당자', field: 'buyerName' },
  { label: '도착지', field: 'toWarehouse' },
  //{ label: '출고수량', field: 'ortQty' },
  //{ label: '단위', field: 'unit' }
];

const addShipColumns = [
  { label: '자재코드', field: 'matId' },
  { label: '자재명', field: 'matName' },
  { label: '출고수량', field: 'ortQty', style: 'text-align: right'},
  { label: '단위', field: 'unit' },
  { label: '구매처 담당자', field: 'purName' }
];

</script>

<template>
  <div class="container">
      <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    <!--검색영역-->
      <div class="card flex flex-col gap-4 mt-4">
        <SearchCard title="출고 검색" @search="pageLoad" @reset="resetSearch">
            <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">

                <InputGroup>
                    <InputGroupAddon><i :class="useIcon('calendar')" /></InputGroupAddon>
                    <IftaLabel>
                        <DatePicker v-model="searchFilter.startDate" inputId="searchStart" />
                        <label for="searchStart">시작일</label>
                    </IftaLabel>
                </InputGroup>

                <InputGroup>
                    <InputGroupAddon><i :class="useIcon('calendar')" /></InputGroupAddon>
                    <IftaLabel>
                        <DatePicker v-model="searchFilter.endDate" inputId="searchEnd" />
                        <label for="searchEnd">종료일</label>
                    </IftaLabel>
                </InputGroup>

                <InputGroup>
                    <InputGroupAddon><i :class="useIcon('box')" /></InputGroupAddon>
                    <IftaLabel>
                        <InputText v-model="searchFilter.matName" inputId="searchMatName" />
                        <label for="searchMatName">자재명</label>
                    </IftaLabel>
                </InputGroup>

                 <InputGroup>
                    <InputGroupAddon><i :class="useIcon('truck')" /></InputGroupAddon>
                    <IftaLabel>
                        <InputText v-model="searchFilter.toWarehouse" inputId="searchTowarehouse" />
                        <label for="searchTowarehouse">도착지</label>
                    </IftaLabel>
                </InputGroup>

            </div>
        </SearchCard>
    </div>
   <!--테이블영역--><!--테이블영역-->
    <div class="flex flex-col md:flex-row gap-8">
      <div class="md:w-1/2">
        <div class="card flex flex-col gap-4 h-full">
          <!-- h-full 고정 -->
          <div class="card flex flex-col gap-4">
            <div class="font-semibold text-xl flex items-center justify-between gap-4 h-10">
            <div class="flex items-center gap-4"><span :class="useIcon('list')"></span> 출고대기 목록</div>
            </div>
            <Divider />
            <selectTable :columns="approveShipColumns" :data="approveShipData" :paginator="true" :rows="15" :showCheckbox="false"  @page-change="onPage" :page="page" @row-click="toggleSelection"/>
          </div>
        </div>
      </div>
      <div class="md:w-1/2 h-full">
        <div class="card flex flex-col gap-4 h-full">
            <div class="flex items-center justify-between my-3">
                <div class="font-semibold text-xl flex items-center justify-between gap-4 h-10">
                <div class="flex items-center gap-4"><span :class="useIcon('info')"></span> 상세정보</div>
                </div>
                <div class="flex gap-2">
                    <btn color="secondary" icon="refresh" label="초기화" class="whitespace-nowrap" outlined @click="formreset" />
                    <btn color="info" icon="check" label="배송 등록" class="whitespace-nowrap" outlined @click="submit" />
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
                        <searchField type="dropDown" label="운송업체" v-model="carrier" class="w-full":options="[ { name: '대한물류', value: '대한물류' },
                                                                                                                 { name: '경동운송', value: '경동운송' },
                                                                                                                 { name: '신속배송', value: '신속배송' },
                                                                                                                 { name: '정확물류', value: '정확물류' }]" />
                        <SearchField type="text" label="운송번호" v-model="trackingNo" />
                        <SearchField type="text" label="차량번호" v-model="carNo" visibility: hidden/>
                    </div>
                </div>
                <!-- 하단 테이블 (6 비율) -->
                <div class="flex-[6] overflow-auto">
                    <selectTable :selectionMode="'single'" :columns="addShipColumns" :data="shipDetailList" :paginator="false" :showCheckbox="false" class="h-full"/>
                </div>
            </div>
        </div>
    </div>
</div>
</div>
</template>
