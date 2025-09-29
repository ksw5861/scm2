<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import btn from '@/components/common/Btn.vue';
import selectTable from '@/components/common/checkBoxTable.vue';
import SearchField from '@/components/common/SearchBox.vue';
import { useAppToast } from '@/composables/useAppToast';
import { useRoute } from 'vue-router';
import { useIcon } from '@/composables/useIcon';
import { useDateFormat, useNumberFormat } from '@/composables/useFormat';

// 반려처리방식!

const route = useRoute();
const { toast } = useAppToast();

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

//공급처코드
const vendorId = ref('VEN005');
const dateRange = ref({ start: null, end: null });
const materialName = ref();
const statusList = ref([]);
const matOrderData = ref([]);
const selectedRows = ref([]);

const matOrderColumns = [
  { label: '주문일자', field: 'orderDate', sortable: true },
  { label: '구매요청번호', field: 'orderNo' },
  { label: '납기요청일', field: 'dueDate', sortable: true },
  { label: '자재코드', field: 'matId' },
  { label: '자재명', field: 'matName', sortable: true },
  { label: '주문수량', field: 'orderQty', sortable: true },
  { label: '단위', field: 'unit' },
  { label: '단가', field: 'price' },
  { label: '총 금액', field: 'total', sortable: true },
  { label: '구매처 담당자', field: 'buyerName', sortable: true }
];

//주문목록
const pageLoad = onMounted(async () => {
  try {
    const list = await axios.get(`/api/supplier/OrderList/${vendorId.value}`);
    matOrderData.value = list.data.map((item) => ({
      id: item.purId,
      orderDate: useDateFormat(item.regDate).value,
      orderNo: item.purNo,
      dueDate: useDateFormat(item.dueDate).value,
      matId: item.matId,
      matName: item.materialVO.matName,
      orderQty: item.reqQty,
      unit: item.materialVO.unit,
      price: item.materialVO.matUnitPrice,
      total: item.total,
      buyerName: item.empName
    }));
  } catch (error) {
    toast('error', '리스트 로드 실패', '주문 리스트 불러오기 실패:', '3000');
  }
});

const approve = async () => {
  const list = JSON.parse(JSON.stringify(selectedRows.value));
  const idList = list.map((row) => row.id);

  try {
    const res = await axios.post('/api/supplier/approve', { purId: idList, name: vendorId.value });
    toast('info', '승인 성공', res.value + '건 주문 승인 되었습니다.', '3000');

    setTimeout(() => {
      window.location.reload();
    }, 500); // 0.5초 후 새로고침
  } catch (error) {
    toast('error', '승인 실패', '주문 승인 실패:', '3000');
  }
};
</script>

<template>
  <div class="container">
    <div class="p-4">
      <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    </div>
    <div class="card flex flex-col gap-4">
      <div class="font-semibold text-xl">자재요청 조회</div>
      <Divider />
      <!--search BOX 영역-->
      <div class="flex flex-col gap-4 md:flex-row md:items-end md:gap-6 mt-5 mb-10">
        <SearchField type="dateRange" label="요청일자" v-model="dateRange" />
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
    <!--검색박스 end-->

    <!--중간버튼영역-->
    <div class="card flex flex-col gap-4">
      <div class="my-3 flex flex-wrap items-center justify-end gap-2">
        <btn color="contrast" icon="pi pi-plus" label="Excel 다운로드" />
        <btn color="warn" icon="pi pi-file-excel" label="반려" />
        <btn color="info" icon="pi pi-file-pdf" @click="approve" label="승인" />
      </div>
      <div class="font-semibold text-xl mb-5">조회 내역</div>
      <selectTable v-model:selection="selectedRows" :columns="matOrderColumns" :data="matOrderData" :paginator="true" :rows="15" />
    </div>
  </div>
</template>

<style scoped></style>
