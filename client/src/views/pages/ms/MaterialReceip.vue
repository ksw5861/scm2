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
import FileUpload from 'primevue/fileupload';
import Dialog from 'primevue/dialog';
import Textarea from 'primevue/textarea';
import { useUserStore } from '@/stores/user';
import SearchCard from '@/components/card/SearchCard.vue';
import Select from 'primevue/select';
import DatePicker from 'primevue/datepicker';

// (userStore.name)이름
// (userStore.code)코드 - 계정기준으로

// Pinia Store
const userStore = useUserStore();
const empName = userStore.name;

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
// codeId → codeName 매핑용
const codeMap = ref({});
//반품모달용
const defectModal = ref(false);
const previewUrl = ref(null);
const selectedFile = ref(null);
// pagination
const page = ref({ page: 1, size: 10, totalElements: 0 });
//검색필드
const searchFilter = ref({
  startDate: '',
  endDate: '',
  vendorName: '',
  status: ''
});

//불량정보를 백단에서 받을 inboundLogVO랑 바로 매핑가능하도록 사용!
const defectForm = ref({
  matName: '',
  inboundDetId: null,
  logRejQty: '',
  logMemo: '',
  logName: empName
});

//datePicker날짜변환
const formatDate = (date) => {
  if (!date) return '';
  const d = new Date(date);
  return d.toISOString().slice(0, 10);
};

const pageLoad = async () => {

    const params = {
        page: page.value.page,
        size: page.value.size,
        startDate: formatDate(searchFilter.value.startDate),
        endDate: formatDate(searchFilter.value.endDate),
        vendorName: searchFilter.value.vendorName,
        status:searchFilter.value.status
  };


  try {
    const res = await axios.get('/api/mat/unloadList', { params });
    const { list, page: pageInfo } = res.data;
    approveUnloadList.value = list.map((item) => ({
      id: item.inboundId,
      unloadDate: useDateFormat(item.unloadDate).value,
      inboundNo: item.inboundNo,
      companyName: item.vendorVO.companyName,
      inboundStatus: codeMap.value[item.inboundStatus] || item.inboundStatus,
      unloadEmp: item.unloadEmp
    }));
    page.value.totalElements = pageInfo.totalElements;
    console.log(list);
  } catch (error) {
    toast('error', '리스트 로드 실패', '입고대기 리스트 불러오기 실패:', '3000');
  }
};

const detailInfo = async () => {
  try {
    console.log(selectedMaster.value.id);
    const list = await axios.get('/api/mat/unloadDetailList', { params: { inboundId: selectedMaster.value.id } });
    console.log(list);
    //상세테이블
    approveUnloadDetailList.value = list.data.map((item) => ({
      id: item.inboundDetId,
      matId: item.matId,
      matName: item.materialVO.matName,
      outQty: item.outQty,
      unit: item.materialVO.stockUnit,
      restQty: item.outQty - item.inTotalQty,
      inTotalQty: item.inTotalQty,
      purStatusId: item.purStatusId
    }));
    inputMatName.value = '';
  } catch (error) {
    toast('error', '상세정보 실패', '상세정보 불러오기 실패:', '3000');
  }
};

//행선택시 자재명 인풋박스 바인딩용!
const inputInfo = () => {
  const row = selectedDetail.value; // 선택된 행 데이터

  if (row.outQty === row.inTotalQty) {
    toast('info', '입고등록 완료', '입고등록이 완료된 자재입니다.', '3000');
    return;
  }

  inputMatName.value = row.matName;
  console.log('선택된 자재명:', row.matName);
};

const submit = async () => {
  //불합격시 등록버튼 제어
  if (result.value === 'N') {
    toast('info', '입고등록 실패', '검수결과 합격자재만 입고가능합니다.', '3000');
    return;
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
      logName: empName
    }
  };
  console.log(payload);
  try {
    await axios.post('/api/mat/matInStock', payload);
    await detailInfo();
    toast('success', '입고등록 성공', '입고등록 성공:', '3000');
    expDate.value = null;
    inQty.value = null;
    result.value = null;
    inputMatName.value = '';
  } catch (error) {
    toast('error', '입고등록 실패', '입고등록 실패:', '3000');
  }
};

//반품모달open
const opendefectModal = () => {
  const row = selectedDetail.value;
  if (!row) {
    toast('info', '선택 필요', '반품할 자재를 선택하세요.', '3000');
    return;
  }

  if (result.value === 'Y') {
    toast('info', '확인 필요', '검수결과 불합격자재만 입고가능합니다.', '3000');
    return;
  }
  defectForm.value = {
    matName: row.matName,
    inboundDetId: row.id,
    logRejQty: '',
    logMemo: '',
    logName: empName
  };
  defectModal.value = true;
};

const closeDefectModal = () => {
  defectModal.value = false;
};
//이미지
const onFileSelect = (event) => {
  // PrimeVue는 배열 형태로 전달됨
  selectedFile.value = event.files[0];
  console.log('선택된 파일:', selectedFile.value);

  // 미리보기용 Blob URL 생성
  if (selectedFile.value) {
    previewUrl.value = URL.createObjectURL(selectedFile.value);
  }
};

const defectSubmit = async () => {
  //[메모, 불량수량, 상세아이디, 담당자]
  const defectPayload = new FormData();

  //JSON 포장해서 FormData에 추가
  defectPayload.append(
    'data',
    new Blob(
      [JSON.stringify(defectForm.value)], // 이미 VO 구조랑 같음
      { type: 'application/json' }
    )
  );
  //이미지도 추가
  if (selectedFile.value) {
    defectPayload.append('file', selectedFile.value);
  }

  for (const [key, value] of defectPayload.entries()) {
    console.log(key, value);
  }

  defectPayload.get('data').text().then(console.log);
  try {
    await axios.post('/api/mat/defect', defectPayload);
    toast('success', '불량 등록 완료', '불량 정보가 성공적으로 저장되었습니다.');
    closeDefectModal();
  } catch (error) {
    toast('error', '불량 등록 실패', '서버 오류가 발생했습니다.');
  }
};

//공통코드
const loadStatusCodes = async () => {
  try {
    const res = await axios.get('/api/mat/status/inm');
    // {"ms1":"요청등록","ms2":"요청승인",...} 형태로 변환
    codeMap.value = res.data.reduce((acc, cur) => {
      acc[cur.codeId] = cur.codeName;
      return acc;
    }, {});
  } catch (err) {
    toast('error', '공통코드 로드 실패', '상태명 불러오기 실패', '3000');
  }
};

const onPage = (event) => {
  const startRow = event.page * event.rows + 1;
  const endRow = (event.page + 1) * event.rows;

  pageLoad({ startRow, endRow }); // 여기서 axios 호출
};

const resetSearch = () => {
  searchFilter.value = {
    startDate: '',
    endDate: '',
    vendorName: ''
  };
  pageLoad();
};

onMounted(() => {
  pageLoad();
  loadStatusCodes();
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
    <!--검색영역-->
    <div class="card flex flex-col gap-4">
      <SearchCard title="입고 조회" @search="pageLoad" @reset="resetSearch">
        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
          <InputGroup>
            <InputGroupAddon><i :class="useIcon('box')" /></InputGroupAddon>
            <IftaLabel>
              <DatePicker v-model="searchFilter.startDate" inputId="searchStart" />
              <label for="searchStart">시작일</label>
            </IftaLabel>
          </InputGroup>

          <InputGroup>
            <InputGroupAddon><i :class="useIcon('box')" /></InputGroupAddon>
            <IftaLabel>
              <DatePicker v-model="searchFilter.endDate" inputId="searchEnd" />
              <label for="searchEnd">종료일</label>
            </IftaLabel>
          </InputGroup>

          <InputGroup>
            <InputGroupAddon><i :class="useIcon('box')" /></InputGroupAddon>
            <IftaLabel>
              <InputText v-model="searchFilter.vendorName" inputId="searchVenName" />
              <label for="searchVendor">공급처</label>
            </IftaLabel>
          </InputGroup>

          <div class="flex flex-col w-full">
            <InputGroup>
              <InputGroupAddon><i :class="useIcon('box')" /></InputGroupAddon>
              <Select v-model="searchFilter.status" :options="statusOptions" optionLabel="name" optionValue="value" placeholder="입고 상태" class="w-full h-[48px] text-base" />
            </InputGroup>
          </div>
        </div>
      </SearchCard>
    </div>
    <!--테이블영역--><!--테이블영역-->
    <div class="flex flex-col md:flex-row gap-8">
      <div class="md:w-1/2">
        <div class="card flex flex-col gap-4 h-full">
          <!-- h-full 고정 -->
          <div class="card flex flex-col gap-4">
            <div class="font-semibold text-m">입고대기 목록</div>
            <Divider />
            <selectTable v-model:selection="selectedMaster" :selectionMode="'single'" :columns="approveUnloadColumn" :data="approveUnloadList" :paginator="true" :showCheckbox="false" :page="page" @row-select="detailInfo" @page-change="onPage" />
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
              <btn color="warn" icon="pi pi-file-excel" label="불량등록" @click="opendefectModal" />
              <btn color="info" icon="pi pi-file-pdf" label="입고등록" @click="submit" />
            </div>
          </div>

          <Divider />
          <selectTable v-model:selection="selectedDetail" :selectionMode="'single'" :columns="approveUnloadDetaiColumn" :data="approveUnloadDetailList" :paginator="false" :showCheckbox="false" @row-select="inputInfo" />
          <Divider />
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 h-full">
            <SearchField type="readOnly" label="자재명" v-model="inputMatName" />
            <SearchField type="date" label="유통기한" v-model="expDate" />
            <SearchField type="text" label="입고수량" v-model="inQty" />
            <searchField
              type="dropDown"
              label="검수결과"
              v-model="result"
              class="w-full"
              :options="[
                { name: '합격', value: 'Y' },
                { name: '불합격', value: 'N' }
              ]"
            />
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 반품 모달 -->
  <Dialog v-model:visible="defectModal" modal header="불량등록" :style="{ width: '500px' }">
    <div class="flex justify-center gap-2 mb-3">
      <SearchField type="readOnly" label="자재명" v-model="defectForm.matName" />
      <SearchField type="text" label="불량수량" v-model="defectForm.logRejQty" />
    </div>
    <div class="flex justify-center gap-2 mb-3">
      <Textarea type="text" label="불량사유" v-model="defectForm.logMemo" placeholder="불량사유를 입력하세요" rows="5" cols="53" />
    </div>

    <div>
      <FileUpload mode="basic" name="file" chooseLabel="파일 선택" accept="image/*" @select="onFileSelect" />
      <div v-if="previewUrl" class="mt-2">
        <img :src="previewUrl" alt="미리보기" class="w-32 rounded-lg border" />
      </div>
    </div>

    <template #footer>
      <btn color="contrast" label="취소" @click="closeDefectModal" />
      <btn color="warn" label="등록" @click="defectSubmit" />
    </template>
  </Dialog>
</template>

<scoped>
</scoped>
