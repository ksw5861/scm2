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
  { label: '출고수량', field: 'outQty', inputText: true },
  { label: '남은수량', field: 'restQty' },
  { label: '상태', field: 'releaseStatus' },
  { label: '출고승인일', field: 'approveDate' }
];

onMounted(async () => {
  try {
    const list = await axios.get(`/api/supplier/releaseList/${vendorId.value}`);
    matOutData.value = list.data.map((item) => ({
      id: item.purId,
      dueDate: useDateFormat(item.dueDate).value,
      purNo: item.purNo,
      matId: item.matId,
      matName: item.materialVO.matName,
      buyerName: item.empName,
      orderQty: item.reqQty,
      unit: item.materialVO.unit,
      outQty: item.outTotalQty,
      restQty: item.reqQty - item.outTotalQty,
      releaseStatus: item.purMatStatus,
      approveDate: useDateFormat(item.purStatusLogVO.reDate).value
    }));
  } catch (error) {
    toast('error', '리스트 로드 실패', '리스트 불러오기 실패:', '3000');
  }
});

//1) 전량출고, 2) 부분출고 => 프로시저써서 분기
// 출고시에 입고테이블 데이터 넣고, 입고는 마스터+ 디테일로 나뉨. => 1) 입고마스터 insert하고 데이터 받아서, 2) 디테일을 순환하면서 insert

/*

보내야 할 데이터 [ 거래처코드, [자재코드들(복수)+출고수량] =>  ]


pur_id로 식별
1) 누적출고수량(out_total_qty),
  상태값(pur_mat+status)

=> 유효성검사 -> req값보다 많으면 안됨. (프론트)
=> req랑 (out_total_qty+outQty) 비교후
if 1) 일치= 출고완료'ms4'
    2) 자재구매이력
  pur_id, 거래처코드, 상태값

out_total_qty도 insert

else 2) 불일치 부분출고로 상태값'ms5'
      2) 자재구매이력
         pur_id, 거래처코드, 상태값

out_total_qty도 insert

 */

const outBound = async () => {
  //선택행 없으면 출고처리 선택 안내
  //   if (!matOutData.value.orderQty) {
  //   toast('info', '유효성 검사', '출고 수량을 입력해 주세요.', '3000');
  //   return;
  // }

  // if (matOutData.value.outQty > matOutData.value.orderQty) {
  //   toast('info', '유효성 검사', '주문수량 대비 출고수량이 많습니다.', '3000');
  //   return;
  // }

  const list = JSON.parse(JSON.stringify(selectedRows.value));

  const payload = list.map((row) => ({
    purId: row.id,
    matId: row.matId,
    purNo: row.purNo,
    purStatusLogVO: {supOutQty: row.outQty}, //이걸 백 로그VO에 넣기위해!
    vendorId: vendorId.value //이렇게 넣으면 행 마다 다 들어감.
  }));

  console.log(payload);

  try {
    const res = await axios.post('/api/supplier/shipMaterial', payload);
    //1차 (다품목 출고로 for문 돌면서 작업)
    // 자재요청테이블에 출고수량, 상태값 update
    //=> 프로시저로 파라미터로 들어온 출고수량 비교 후
    // 부분출고(ms5), 전량출고(ms4)
    //상태값 log insert
    //2차 입고테이블 insert [마스터: 입고대기/디테일: 입고대기]
    //1) 마스터테이블 데이터 insert 후 반환값 or 가장최근 마스터테이블 아이디 seq값 받아서
    //2) 디테일 품목들 insert 함.(for문)
  } catch (error) {
    toast('error', '출고등록 실패', '출고등록  실패:', '3000');
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
        <btn color="info" icon="pi pi-file-pdf" @click="outBound" label="승인" />
      </div>
      <div class="font-semibold text-xl mb-5">출고대기 목록</div>
      <selectTable v-model:selection="selectedRows" :columns="matOutColumns" :data="matOutData" :paginator="true" :rows="15" />
    </div>
  </div>
</template>
