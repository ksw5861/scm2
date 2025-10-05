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

const empName = ref('박길동');
//테이블 데이터
const approveUnloadList = ref();
const approveUnloadDetailList = ref([]);
//선택row
const selectedMaster = ref();
const selectedDetail = ref();
//인풋박스 바인딩
const inputMatName = ref();
//상세입력값
const expDate = ref();
const inQty = ref();
const result = ref();
const rejMemo = ref();
const restQty = ref(0);

 const pageLoad = async () => {
  try {
    const list = await axios.get('/api/mat/unloadList');
     approveUnloadList.value = list.data.map((item) => ({
        id: item.inboundId,
        unloadDate: useDateFormat(item.unloadDate).value,
        inboundNo: item.inboundNo,
        companyName: item.vendorVO.companyName,
        inboundStatus: item.inboundStatus,
        unloadEmp: item.unloadEmp
    }));
   } catch (error) {
     toast('error', '리스트 로드 실패', '입고대기 리스트 불러오기 실패:', '3000');
   }
 };

const detailInfo = async () => {
  try {
    console.log(selectedMaster.value.id)
    const list = await axios.get('/api/mat/unloadDetailList', { params: {inboundId : selectedMaster.value.id} });
    console.log(list)
    //상세테이블
    approveUnloadDetailList.value = list.data.map((item) => ({
        id: item.inboundDetId,
        matId: item.matId ,
        matName: item.materialVO.matName,
        outQty: item.outQty ,
        unit: item.materialVO.unit,
        restQty: item.outQty - (item.inTotalQty),
        inTotalQty: item.inTotalQty,
        purStatusId: item.purStatusId
    }));
    inputMatName.value ='';
  } catch (error) {
    toast('error', '상세정보 실패', '상세정보 불러오기 실패:', '3000');
  }
};

const inputInfo = () => {
    if(outQty.value == inTotalQty.value){
        toast('info', '입고등록 완료', '입고등록이 완료된 자재입니다.', '3000');
        return
    }
  const row = selectedDetail.value; // 선택된 행 데이터
  console.log(row.matName)
  inputMatName.value = row.matName
};


const submit = async () => {

    //불합격시 등록버튼 제어
    if(result.value === 'N'){
        toast('info', '입고등록 실패', '검수결과 합격자재만 입고가능합니다.', '3000');
        return
    }
    //입고수량 > 입고잔여수량 초과제어
    // if(Number(inQty.value) > Number(restQty.value)){
    //     toast('info', '입고등록 실패', '입고수량이 입고잔여수량을 초과할 수 없습니다.', '3000');
    //     return
    // }

    const payload = {
         inboundDetId: selectedDetail.value.id,
         inboundId: selectedMaster.value.id,
         inQty: inQty.value,
         inboundLogVO: {
            logExpDate: expDate.value,
            logName: empName.value
        }
    }
    console.log(payload);
    try{
       await axios.post('/api/mat/matInStock', payload)
        toast('success', '입고등록 성공', '입고등록 성공:', '3000');
    } catch(error) {
        toast('error', '입고등록 실패', '입고등록 실패:', '3000');
    }
}


onMounted(() => {
   pageLoad();
});

const approveUnloadColumn = [
  { label: '하차일', field: 'unloadDate' },
  { label: '입고번호', field: 'inboundNo' },
  { label: '공급처', field: 'companyName' },
  { label: '상태', field: 'inboundStatus' }, //입고대기, 입고처리중, 입고완료
  { label: '하차 담당자', field: 'unloadEmp' }
];

const approveUnloadDetaiColumn = [
  { label: '자재코드', field: 'matId' },
  { label: '자재명', field: 'matName' },
  { label: '입고대기수량', field: 'outQty' },
  { label: '단위', field: 'unit' },
  { label: '잔여수량', field: 'restQty' },
  { label: '누적처리수량', field: 'inTotalQty' }
];
</script>

<template>
  <div class="container">
    <div class="p-4">
      <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    </div>
    <div class="card flex flex-col gap-4">
      <div class="font-semibold text-xl">입고 등록</div>
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
            <div class="font-semibold text-m">입고대기 목록</div>
            <Divider />
            <selectTable v-model:selection="selectedMaster" :selectionMode="'single'" :columns="approveUnloadColumn" :data="approveUnloadList" :paginator="false" :showCheckbox="false" @row-select="detailInfo" />
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
              <btn color="warn" icon="pi pi-file-excel" label="반품" />
              <btn color="info" icon="pi pi-file-pdf" label="등록" @click="submit"/>
            </div>
          </div>

          <Divider />
          <selectTable v-model:selection="selectedDetail" :selectionMode="'single'" :columns="approveUnloadDetaiColumn" :data="approveUnloadDetailList" :paginator="false" :showCheckbox="false"  @row-select="inputInfo"/>
          <Divider />
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 h-full">
            <SearchField type="readOnly" label="자재명" v-model="inputMatName" />
            <SearchField type="date" label="유통기한" v-model="expDate" />
            <SearchField type="text" label="입고수량" v-model="inQty" />
            <!-- <searchField type="dropDown" label="창고" v-model="deliveryPlace" class="w-full" :options="warehoustListOpt" /> -->
            <searchField type="dropDown" label="검수결과" v-model="result" class="w-full" :options="[ { name: '합격', value: 'Y' },
                                                                                                      { name: '불합격', value: 'N' }]" />
        </div>
            <SearchField type="text" label="비고" v-model="rejMemo" />
        </div>
      </div>
    </div>
  </div>
</template>

<scoped>
</scoped>
