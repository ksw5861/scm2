<script setup>
import { onMounted, ref, computed } from 'vue';
import axios from 'axios';
import btn from '@/components/common/Btn.vue';
import selectTable from '@/components/common/checkBoxTable.vue';
import { useDateFormat, useNumberFormat } from '@/composables/useFormat';
import { useIcon } from '@/composables/useIcon';
import { useAppToast } from '@/composables/useAppToast';
import { useRoute } from 'vue-router';
import CommonModal from '@/components/common/Modal.vue';
import { useUserStore } from '@/stores/user';

// Pinia Store
const userStore = useUserStore();
const empName = userStore.name;
const empId = userStore.code;

const route = useRoute();
const { toast } = useAppToast();

// breadcrumb
const breadcrumbHome = { icon: useIcon('home'), to: '/' };
const breadcrumbItems = computed(() => {
  const matched = route.matched.filter((r) => r.meta);
  if (!matched.length) return [];
  const current = matched[matched.length - 1];
  const parentLabel = current.meta?.breadcrumb?.parent || '자재 주문';
  const currentLabel = current.name || '';
  return [{ label: parentLabel }, { label: currentLabel, to: route.fullPath }];
});

//데이터
const planMasterList = ref([]);
const planList = ref([
  { planDueDate: '', prodId: '', prodName: '', planQty: '', unit: '' },
  { planDueDate: '', prodId: '', prodName: '', planQty: '', unit: '' },
  { planDueDate: '', prodId: '', prodName: '', planQty: '', unit: '' }
]);
const mrpList = ref([
  { matId: '', matName: '', mrpQty: '', unit: '', leadTime: '' },
  { matId: '', matName: '', mrpQty: '', unit: '', leadTime: '' },
  { matId: '', matName: '', mrpQty: '', unit: '', leadTime: '' }
]);

const purchaseList = ref([
  { matId: '', matName: '', reqQty: null, unit: '', vendorId: null, price: null, total: null, dueDate: null },
  { matId: '', matName: '', reqQty: null, unit: '', vendorId: null, price: null, total: null, dueDate: null },
  { matId: '', matName: '', reqQty: null, unit: '', vendorId: null, price: null, total: null, dueDate: null }
]);

const warehouseOptions = ref([]); //창고옵션
const codeMap = ref({}); //공통코드용(생산유형)
const monthCodeMap = ref({}); //공통코드용(월)
const weekCodeMap = ref({}); //공통코드용(주차)

//모달
const showPlanModal = ref(false);
const selectedPlan = ref(null);

const openPlanModal = () => {
  showPlanModal.value = true;
};

const closePlanModal = () => {
  showPlanModal.value = false;
};

// 모달내부 생산계획 데이터 로드 + 날짜/코드 변환
const fetchPlanMaster = async () => {
  // 코드맵이 아직 비어있다면 먼저 로드 (중복 요청 방지)
  if (!Object.keys(codeMap.value).length) {
    await loadStatusCodes();
  }

  try {
    const res = await axios.get('/api/mplanMasterList');
    return {
      items: res.data.map((item) => ({
        ...item,
        startDate: monthCodeMap.value[item.startDate],
        endDate: weekCodeMap.value[item.endDate],
        planType: codeMap.value[item.planType] || item.planType
      }))
    };
  } catch (error) {
    toast('error', '리스트 로드 실패', '생산계획 리스트 불러오기 실패', '3000');
    return { items: [] };
  }
};

//모달내부 계획선택
const onSelectPlan = async (plan) => {
  selectedPlan.value = plan;
  closePlanModal();

  // 상세 불러오기
  const res = await axios.get(`/api/mplanDetailList/${plan.plId}`);
  planList.value = res.data.map((item) => ({
    id: item.plDetId,
    prodId: item.prodId,
    prodName: item.productVO.prodName,
    planQty: item.proQty,
    unit: item.productVO.unit,
    planDueDate: useDateFormat(item.proDate).value
  }));

  toast('success', '선택된 생산계획 불러오기 완료', `${plan.planNo}의 상세를 로드했습니다.`);
};

//mrp산출
const calculatMrp = async () => {
  if (!selectedPlan.value?.plId) {
    toast('warn', '생산계획을 먼저 선택하세요.');
    return;
  }

  try {
    await axios.post(`/api/mcalcMrp/${selectedPlan.value.plId}`, null, {
      params: { empName: empName }
    });
    toast('success', 'MRP 산출 완료', '산출 결과가 저장되었습니다.');
    await pageLoadMrp(); // 최신 MRP_DETAIL 로드
  } catch (error) {
    toast('error', 'MRP 산출 실패', '프로시저 실행 중 오류 발생.');
  }
};

//mrp목록
const pageLoadMrp = async () => {
  const res = await axios.get('/api/mmrpList');
  mrpList.value = res.data.map((item) => ({
    id: item.mrpDetId,
    matId: item.matId,
    matName: item.materialVO.matName,
    mrpQty: item.shortageQty,
    unit: item.unit,
    leadTime: item.leadTime
  }));
};

//mrp행 선택/해제(토글)
const toggleMrpSelection = (row) => {
  // 이미 존재하면 제거
  const existingIndex = purchaseList.value.findIndex((r) => r.matId === row.matId);
  if (existingIndex !== -1) {
    if (purchaseList.value.length > 3) {
      purchaseList.value.splice(existingIndex, 1);
    } else {
      purchaseList.value[existingIndex] = {
        matId: '',
        matName: '',
        reqQty: null,
        unit: '',
        vendorId: null,
        price: null,
        total: null,
        dueDate: null
      };
    }
    return;
  }

  //새로 추가할 행 데이터 정의
  const newRow = {
    id: row.id,
    matId: row.matId,
    matName: row.matName,
    reqQty: row.mrpQty,
    unit: row.unit,
    vendorId: null,
    price: null,
    total: null,
    dueDate: null
  };

  console.log('추가할 행:', newRow);
  // 3️동일 자재 중복 방지
  if (purchaseList.value.some((r) => r.matId === newRow.matId)) {
    toast('warn', '중복 항목', '이미 자재 주문 목록에 있는 자재입니다.', 2000);
    return;
  }

  //빈행 찾기
  const emptyIndex = purchaseList.value.findIndex((r) => !r.matId);

  if (emptyIndex !== -1) {
    // 빈행이 있으면 그 자리에 교체
    purchaseList.value[emptyIndex] = newRow;
  } else {
    // 빈행이 없으면 새 행 push
    purchaseList.value.push(newRow);
  }
};

//주문등록
const reqSubmit = async () => {

  //유효성 검사: 필수 입력값 확인
  const invalidRows = purchaseList.value.filter(
    (row) =>
      // matId가 존재하는(=실제 입력된) 행만 검사
      row.matId && (!row.reqQty || !row.vendorId || !row.dueDate || !row.toWarehouse)
  );

  if (invalidRows.length > 0) {
    toast('warn', '유효성 검사', '입력된 자재 항목 중 필수값이 누락된 행이 있습니다.', 3000);
    return;
  }

  if (!confirm('자재주문을 등록하시겠습니까?')) {
    return;
  }

  const reqList = purchaseList.value.map((row) => ({
    mrpDetId: row.id,
    matId: row.matId,
    reqQty: row.reqQty,
    vendorId: row.vendorId,
    total: row.reqQty * row.price,
    dueDate: row.dueDate,
    toWarehouse: row.toWarehouse,
    empName: empName
  }));
  console.log(reqList);
  try {
    await axios.post('/api/mreqMaterial', reqList);

    toast('success', '등록 성공', '자재주문 등록 성공', '5000');
    await pageLoadMrp();
    purchaseList.value = [
      { matId: '', matName: '', reqQty: null, unit: '', vendorId: null, price: null, total: null, dueDate: null },
      { matId: '', matName: '', reqQty: null, unit: '', vendorId: null, price: null, total: null, dueDate: null },
      { matId: '', matName: '', reqQty: null, unit: '', vendorId: null, price: null, total: null, dueDate: null }
    ];
  } catch (error) {
    toast('error', '등록 실패', '자재주문 등록 실패:', '500');
  }
};

//자재별 공급처
const selectVendor = async (row) => {
  try {
    const res = await axios.get('/api/mmatVendorList', { params: { matId: row.matId } });

    row.vendorOptions = res.data.map((item) => ({
      contractPrice: item.contractPrice,
      value: item.vendorVO.vendorId, // <-- PrimeVue가 인식하는 필드
      label: item.vendorVO.companyName
    }));
    console.log(row.vendorOptions);
  } catch (error) {
    toast('error', '리스트 로드 실패', '자재별 공급처 불러오기 실패:', '3000');
  }
};

//공급처 선택시 총금액 계산
const selectOpt = (row, value) => {
  const selected = (row.vendorOptions || []).find((opt) => opt.value === value);
  if (selected) {
    row.vendorId = selected.value; // DB 저장용
    row.price = selected.contractPrice; // 단가
    row.total = useNumberFormat(row.reqQty * selected.contractPrice);
  }
};

//배송지(창고)
const loadWarehouseList = async () => {
  try {
    const res = await axios.get('/api/mwarehouseList');
    warehouseOptions.value = res.data.map((item) => ({
      value: item.whId,
      label: item.whName
    }));
  } catch (error) {
    toast('error', '불러오기', '창고 목록 불러오기 실패', '3000');
  }
};

// 도착지 선택시 row에 반영
const selectWarehouseOpt = (row, value) => {
  row.toWarehouse = value; // 선택된 창고ID 저장
  const wh = warehouseOptions.value.find((w) => w.value === value);
  row.toWarehouseName = wh ? wh.label : '';
};

//공통코드
const loadStatusCodes = async () => {
  try {
    const res = await axios.get('/api/mstatus/p03');
    // {"pt1": "정규생산", "pt2": "특별생산", ...} 형태로 변환
    codeMap.value = res.data.reduce((acc, cur) => {
      acc[cur.codeId] = cur.codeName;
      return acc;
    }, {});
  } catch (err) {
    toast('error', '공통코드 로드 실패', '상태명 불러오기 실패', '3000');
  }
};

const monthCodes = async () => {
  try {
    const res = await axios.get('/api/mstatus/month');
    monthCodeMap.value = res.data.reduce((acc, cur) => {
      acc[cur.codeId] = cur.codeName;
      return acc;
    }, {});
  } catch (err) {
    toast('error', '공통코드 로드 실패', '상태명 불러오기 실패', '3000');
  }
};

const weekCodes = async () => {
  try {
    const res = await axios.get('/api/mstatus/week');
    weekCodeMap.value = res.data.reduce((acc, cur) => {
      acc[cur.codeId] = cur.codeName;
      return acc;
    }, {});
  } catch (err) {
    toast('error', '공통코드 로드 실패', '상태명 불러오기 실패', '3000');
  }
};

onMounted(() => {
  pageLoadMrp();
  loadWarehouseList();
  loadStatusCodes();
  monthCodes();
  weekCodes();
});

const planMasterColumns = [
  { field: 'planNo', label: '계획번호', style: 'width: 10rem' },
  { field: 'planType', label: '유형', style: 'width: 8rem' },
  { field: 'startDate', label: '시작일', style: 'width: 10rem' },
  { field: 'endDate', label: '종료일', style: 'width: 10rem' },
  { field: 'empName', label: '담당자', style: 'width: 10rem' },
  { field: 'memo', label: '비고', style: 'width: 20rem' }
];

const planColumns = [
  { field: 'planDueDate', label: '생산예정일', style: 'width: 15rem' },
  { field: 'prodId', label: '제품코드', style: 'width: 8rem' },
  { field: 'prodName', label: '제품명', style: 'width: 20rem' },
  { field: 'planQty', label: '생산수량', style: 'width: 10rem; text-align: right' },
  { field: 'unit', label: '단위', style: 'width: 5rem' }
];

const mrpColumns = [
  { field: 'matId', label: '자재코드', style: 'width: 8rem' },
  { field: 'matName', label: '자재명', style: 'width: 20rem', sortable: true },
  { field: 'mrpQty', label: '필요수량', style: 'width: 10rem; text-align: right', sortable: true },
  { field: 'unit', label: '단위', style: 'width: 8rem' },
  { field: 'leadTime', label: '리드타임(일)', style: 'width: 10rem' }
];

const purchaseColumns = [
  { field: 'matId', label: '자재코드', style: 'width: 10rem' },
  { field: 'matName', label: '자재명', style: 'width: 20rem' },
  { field: 'reqQty', label: '주문수량', style: 'width: 10rem; text-align: right' },
  { field: 'unit', label: '단위', style: 'width: 8rem' },
  { field: 'vendor', label: '공급처', style: 'width: 15rem', select: true, option: (row) => row.vendorOptions || [], change: selectOpt },
  { field: 'price', label: '단가', style: 'width: 10rem; text-align: right' },
  { field: 'total', label: '총 금액', style: 'width: 12rem; text-align: right' },
  { field: 'toWarehouse', label: '도착지', style: 'width: 15rem', select: true, option: () => warehouseOptions.value, change: selectWarehouseOpt },
  { field: 'dueDate', label: '납기요청일', style: 'width: 12rem', datePicker: true }
];
</script>

<template>
  <div class="container">
    <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    <div class="flex flex-col gap-8 mt-4">
      <!-- 하단 -->
      <div class="flex flex-col md:flex-row gap-8">
        <div class="md:w-1/2 planList">
          <div class="card flex flex-col gap-4 h-full">
            <div class="flex items-center justify-between font-semibold text-m">
              <div class="font-semibold text-xl flex items-center justify-between gap-4 h-10">
                <div class="flex items-center gap-4"><span :class="useIcon('list')"></span>생산계획 상세</div>
              </div>
              <div class="flex gap-2">
                <btn color="secondary" icon="check" label="생산계획불러오기" class="whitespace-nowrap" outlined @click="openPlanModal" />
                <btn color="secondary" icon="check" label="MRP계산" class="whitespace-nowrap" outlined @click="calculatMrp" />
              </div>
            </div>
            <Divider />
            <selectTable :columns="planColumns" :data="planList" :paginator="false" :showCheckbox="false" />
          </div>
        </div>

        <!-- 우측 -->
        <div class="md:w-1/2">
          <div class="card flex flex-col gap-4 h-full">
            <div class="font-semibold text-xl flex items-center justify-between gap-4 h-10">
              <div class="flex items-center gap-4"><span :class="useIcon('list')"></span>MRP 합산 자재 소요량</div>
            </div>
            <Divider />
            <selectTable :columns="mrpColumns" :data="mrpList" :paginator="false" :showCheckbox="false" @row-click="toggleMrpSelection" />
          </div>
        </div>
      </div>
      <!-- 상단 -->
      <div class="header">
        <div class="card flex flex-col gap-4">
          <div class="font-semibold text-xl flex items-center justify-between gap-4 h-10">
            <div class="flex items-center gap-4">
              <span :class="useIcon('cart')"></span>
              <span>자재 발주</span>
            </div>
            <btn color="info" icon="check" label="자재주문" @click="reqSubmit" class="whitespace-nowrap" outlined />
          </div>
          <Divider />
          <selectTable :columns="purchaseColumns" :scrollable="true" :data="purchaseList" :paginator="false" :showCheckbox="false" @row-select="selectVendor" />
        </div>
      </div>
    </div>

    <!-- 생산계획 선택 모달 -->
    <CommonModal :visible="showPlanModal" title="생산계획 선택" :columns="planMasterColumns" :fetchData="fetchPlanMaster" @close="closePlanModal" @select="onSelectPlan" />
  </div>
</template>
