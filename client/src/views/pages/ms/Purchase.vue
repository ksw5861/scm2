<script setup>
import { onMounted, ref, computed } from 'vue';
import axios from 'axios';
import selectTable from '@/components/common/checkBoxTable.vue';
import { useDateFormat, useNumberFormat } from '@/composables/useFormat';
import { useIcon } from '@/composables/useIcon';
import { useAppToast } from '@/composables/useAppToast';
import { useRoute } from 'vue-router';

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
const mrpList = ref([]);
const purchaseList = ref([
  { matId: '', matName: '', reqQty: null, unit: '', vendorId: null, price: null, total: null, dueDate: null },
  { matId: '', matName: '', reqQty: null, unit: '', vendorId: null, price: null, total: null, dueDate: null },
  { matId: '', matName: '', reqQty: null, unit: '', vendorId: null, price: null, total: null, dueDate: null }
]);
const empName = ref('로그인');

//생산계획목록
onMounted( async () => {
  try {
    const list = await axios.get('/api/mat/planMasterList');
    planMasterList.value = list.data.map((item) => ({
      id: item.plId,
      planType: item.planType,
      planNo: item.planNo,
      startDate: useDateFormat(item.startDate).value,
      endDate: useDateFormat(item.endDate).value,
      memo: item.memo,
      empName: item.empName
    }));
  } catch (error) {
    toast('error', '리스트 로드 실패', '생산계획 리스트 불러오기 실패:', '3000');
  }
});

//mrp목록
onMounted( async () => {
  try {
    const list = await axios.get('/api/mat/mrpList');
    mrpList.value = list.data.map((item) => ({
      id: item.mrpDetId,
      matId: item.matId,
      matName: item.materialVO.matName,
      mrpQty: item.needsQty,
      unit: item.materialVO.unit,
      leadTime: item.materialVO.leadTime
    }));

  } catch (error) {
    toast('error', '리스트 로드 실패', 'mrp 리스트 불러오기 실패:', '3000');
  }
});

//자재별 공급처
const selectVendor = async (row) => {
      try{
        const res = await axios.get ('/api/mat/matVendorList',  {params: { matId: row.matId }})

        row.vendorOptions = res.data.map((item) => ({
            contractPrice: item.contractPrice,
            value: item.vendorVO.vendorId,   // <-- PrimeVue가 인식하는 필드
            label: item.vendorVO.companyName
        }));
         console.log(row.vendorOptions);
      } catch(error) {
            toast('error', '리스트 로드 실패', '자재별 공급처 불러오기 실패:', '3000');
        }
}

//공급처 선택시 총금액 계산
const selectOpt = (row, value) => {
  const selected = (row.vendorOptions || []).find(opt => opt.value === value);
  if (selected) {
    row.vendorId = selected.value;      // DB 저장용
    row.price = selected.contractPrice; // 단가
    row.total = useNumberFormat(row.reqQty * selected.contractPrice);
  }
};


//mrp리스트 행 선택
const addToPurchase = (row) => {
    const emptyRowIndex = purchaseList.value.findIndex(r => !r.matId);
    const newRow = {
        id: row.id, //mrp상세테이블 아이디값 넘겨줘야 함.
        matId: row.matId,
        matName: row.matName,
        reqQty: row.mrpQty,
        unit: row.unit,
        vendorId: null,   //드롭다운 선택시 채워짐
        price: null,
        total: null
  };
    if (emptyRowIndex !== -1) {
    // 빈행 있으면 교체
    purchaseList.value[emptyRowIndex] = newRow;
  } else {
    // 빈행 없으면 push
    purchaseList.value.push(newRow);
  }
}

const calculatMrp = () => {
    console.log('mrp산출')
}

const reqSubmit = async() => {

    const reqList = purchaseList.value.map(row => ({
        mrpDetId: row.id,
        matId: row.matId,
        reqQty: row.reqQty,
        vendorId: row.vendorId,
        total: row.reqQty * row.price,
        dueDate: row.dueDate,
        empName: empName.value
    }));
    console.log(reqList);
    try{
        await axios.post('/api/mat/reqMaterial', reqList)

       toast('info', '등록 성공', '자재주문 등록 성공', '5000');
       setTimeout(() => {
        window.location.reload();
       }, 1500); // 1.5초 후 새로고침

    } catch(error){
        toast('error', '등록 실패', '자재주문 등록 실패:', '500');
    }

}

//테이블 컬럼
const planMasterColumns = [
  { field: 'planType', label: '계획유형', style: 'width: 8rem' },
  { field: 'startDate', label: '생산시작일', style: 'width: 15rem', sortable: true},
  { field: 'endDate', label: '생산종료일', style: 'width: 15rem',  sortable: true},
  { field: 'memo', label: '비고', style: 'width: 20rem' },
  { field: 'planNo', label: '계획번호', style: 'width: 15rem' },
  { field: 'empName', label: '담당자', style: 'width: 10rem' }
];

const mrpColumns = [
  { field: 'matId', label: '자재코드', style: 'width: 8rem'},
  { field: 'matName', label: '자재명', style: 'width: 20rem', sortable: true },
  { field: 'mrpQty', label: '소요수량', style: 'width: 10rem', sortable: true },
  { field: 'unit', label: '단위', style: 'width: 8rem' },
  { field: 'leadTime', label: '리드타임(일)', style: 'width: 10rem' }
];

const purchaseColumns = [
  { field: 'matId', label: '자재코드', style: 'width: 10rem'},
  { field: 'matName', label: '자재명', style: 'width: 20rem' },
  { field: 'reqQty', label: '주문수량', style: 'width: 10rem' },
  { field: 'unit', label: '단위', style: 'width: 8rem' },
  { field: 'vendor', label: '공급처', style: 'width: 15rem', select:true, option: (row) => row.vendorOptions || [], change: selectOpt  },
  { field: 'price', label: '단가', style: 'width: 10rem' },
  { field: 'total', label: '총 금액', style: 'width: 12rem' },
  { field: 'dueDate', label: '납기요청일', style: 'width: 12rem', datePicker: true },
];

//계획상세
// const planList = ref([]);
// const planColumns = [
//   { field: 'planDueDate', label: '생산예정일', style: 'width: 15rem' },
//   { field: 'prodId', label: '제품코드', style: 'width: 8rem' },
//   { field: 'prodName', label: '제품명', style: 'width: 20rem' },
//   { field: 'planQty', label: '생산수량', style: 'width: 10rem' },
//   { field: 'unit', label: '단위', style: 'width: 5rem' }
// ];
// onMounted(async () => {
//   try {
//     const list = await axios.get('/api/mat/planList');
//     planList.value = list.data.map((item) => ({
//       id: item.prodNo,
//       planId: item.prodNo,
//       planDueDate: useDateFormat(item.proDate).value,
//       prodId: item.prodId,
//       prodName: item.productVO.prodName,
//       planQty: item.proQty,
//       unit: item.productVO.unit
//     }));
//   } catch (error) {
//     toast('제품별 생산계획 리스트 불러오기 실패:', error);
//   }
// });
</script>

<template>
  <div class="container">
    <div class="p-4">
      <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    </div>
    <div class="flex flex-col gap-8">
     <!-- 상단-->
        <div class="header">
            <div class="card flex flex-col gap-4">
                <div class="flex items-center justify-between font-semibold text-m">
                    <span>주문 등록</span>
                    <btn color="secondary" icon="pi pi-file-excel" @click="reqSubmit" label="주문"/>
                </div>
                <Divider />
                <selectTable :columns="purchaseColumns" :scrollable="true" :data="purchaseList" :paginator="false" :showCheckbox="false" @row-select="selectVendor" />
            </div>
        </div>
       <!--하단-->
        <div class="flex flex-col md:flex-row gap-8">
            <div class="md:w-1/2 planList">
                <div class="card flex flex-col gap-4 h-full"> <!-- h-full 고정 -->
                    <div class="card flex flex-col gap-4">
                        <div class="flex items-center justify-between font-semibold text-m">
                            <span>생산계획 목록</span>
                            <btn color="secondary" icon="pi pi-file-excel" @click="calculatMrp" label="mrp산출"/>
                        </div>
                        <Divider />
                        <selectTable :columns="planMasterColumns" :data="planMasterList" :paginator="false" :showCheckbox="false" />
                    </div>
                </div>
            </div>
            <!--하단우측-->
            <div class="md:w-1/2">
                <div class="card flex flex-col gap-4 h-full"> <!-- h-full 고정 -->
                    <div class="card flex flex-col gap-4">
                        <div class="font-semibold text-m">MRP 합산자재 소요량</div>
                        <Divider />
                        <selectTable :columns="mrpColumns" :data="mrpList" :paginator="false" :showCheckbox="false" @row-select="addToPurchase"/>
                    </div>
                </div>
            </div>
        </div>

    </div>
    </div>
    </template>

<style scoped></style>
