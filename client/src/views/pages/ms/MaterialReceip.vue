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
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const pageLoad = async () => {
  //검색필터 기간 유효성검사
  if (searchFilter.value.startDate && searchFilter.value.endDate) {
    if (new Date(searchFilter.value.startDate) > new Date(searchFilter.value.endDate)) {
      toast('warn', '기간 오류', '시작일은 종료일보다 이전이어야 합니다.', '3000');
      return;
    }
  }

  const params = {
    page: page.value.page,
    size: page.value.size,
    startDate: formatDate(searchFilter.value.startDate),
    endDate: formatDate(searchFilter.value.endDate),
    vendorName: searchFilter.value.vendorName,
    status: searchFilter.value.status
  };

  try {
    const res = await axios.get('/api/munloadList', { params });
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
    const list = await axios.get('/api/munloadDetailList', { params: { inboundId: selectedMaster.value.id } });
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
  // 인풋박스 초기화
  inQty.value = null;
  expDate.value = null;
  result.value = null;

  inputMatName.value = row.matName;
  console.log('선택된 자재명:', row.matName);
};

const submit = async () => {
  //불합격시 등록버튼 제어
  if (result.value === 'N') {
    toast('warn', '입고등록 불가', '검수결과 합격자재만 입고가능합니다.', '3000');
    return;
  }
  //유효성 검사
  if (!inQty.value || isNaN(inQty.value) || Number(inQty.value) <= 0) {
    toast('warn', '유효성 검사', '처리수량은 0보다 큰 숫자로 입력해주세요.', '3000');
    return;
  }
  if (Number(inQty.value) > selectedDetail.value.restQty) {
    toast('warn', '유효성 검사', '입고수량은 잔여수량을 초과할 수 없습니다.', '3000');
    return;
  }
  if (!expDate.value) {
    toast('warn', '유효성 검사', '유통기한을 입력해주세요.', '3000');
    return;
  }
  if (result.value !== 'Y') {
    toast('warn', '유효성 검사', '검수결과를 선택해주세요.', '3000');
    return;
  }
  if (!confirm('입고등록을 진행하시겠습니까?')) {
    return;
  }
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
    await axios.post('/api/mmatInStock', payload);
    await detailInfo();
    await pageLoad();
    toast('success', '입고등록 성공', '입고등록 성공:', '3000');
    expDate.value = null;
    inQty.value = null;
    result.value = null;
    inputMatName.value = '';
    selectedDetail.value = null;
  } catch (error) {
    toast('error', '입고등록 실패', '입고등록 실패:', '3000');
  }
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
  defectForm.value.inboundDetId = selectedDetail.value.id;
  defectForm.value.logRejQty = inQty.value; //불량수량을 입고수량으로 설정

  if (result.value === 'Y') {
    toast('warn', '등록 불가', '검수결과 불합격자재만 등록가능합니다.', '3000');
    return;
  }

  if (!defectForm.value.logRejQty || isNaN(defectForm.value.logRejQty) || Number(defectForm.value.logRejQty) <= 0) {
    toast('warn', '유효성 검사', '불량수량은 0보다 큰 숫자로 입력해주세요.', '3000');
    return;
  }
  if (Number(defectForm.value.logRejQty) > selectedDetail.value.restQty) {
    toast('warn', '유효성 검사', '불량수량은 잔여수량을 초과할 수 없습니다.', '3000');
    return;
  }
  if (!defectForm.value.logMemo) {
    toast('warn', '유효성 검사', '불량사유를 입력해주세요.', '3000');
    return;
  }
  if (!selectedFile.value) {
    toast('warn', '유효성 검사', '불량 이미지 파일을 선택해주세요.', '3000');
    return;
  }

  if (!confirm('불량등록을 진행하시겠습니까?')) {
    return;
  }

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
    await axios.post('/api/mdefect', defectPayload);
    toast('success', '불량 등록 완료', '불량 정보가 성공적으로 저장되었습니다.');
    await detailInfo();
    await pageLoad();
    defectForm.value = {
      matName: '',
      inboundDetId: null,
      logRejQty: '',
      logMemo: '',
      logName: empName
    };
    selectedFile.value = null;
    selectedDetail.value.id = null;
    inQty.value = null;
    previewUrl.value = null;
    result.value = null;
  } catch (error) {
    toast('error', '불량 등록 실패', '서버 오류가 발생했습니다.');
  }
};

//공통코드
const loadStatusCodes = async () => {
  try {
    const res = await axios.get('/api/mstatus/inm');
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

//반품모달open
// const opendefectModal = () => {
//   const row = selectedDetail.value;
//   if (!row) {
//     toast('info', '선택 필요', '반품할 자재를 선택하세요.', '3000');
//     return;
//   }

//   if (result.value === 'Y') {
//     toast('info', '확인 필요', '검수결과 불합격자재만 입고가능합니다.', '3000');
//     return;
//   }
//   defectForm.value = {
//     matName: row.matName,
//     inboundDetId: row.id,
//     logRejQty: '',
//     logMemo: '',
//     logName: empName
//   };
//   defectModal.value = true;
// };

// const closeDefectModal = () => {
//   defectModal.value = false;
// };
</script>

<template>
  <div class="container">
    <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    <!--검색영역-->
    <div class="card flex flex-col gap-4 mt-4">
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
            <div class="font-semibold text-xl flex items-center justify-between gap-4 h-10">
              <div class="flex items-center gap-4"><span :class="useIcon('list')"></span> 입고대기 목록</div>
            </div>
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
            <div class="font-semibold text-xl flex items-center justify-between gap-4 h-10">
              <div class="flex items-center gap-4"><span :class="useIcon('openfolder')"></span> 상세정보</div>
            </div>
            <!-- 오른쪽: 버튼 -->
            <div class="flex gap-2">
              <btn color="warn" icon="check" label="불량등록" @click="defectSubmit" class="whitespace-nowrap" outlined />
              <btn color="info" icon="check" label="입고등록" @click="submit" lass="whitespace-nowrap" outlined />
            </div>
          </div>

          <Divider />
          <selectTable v-model:selection="selectedDetail" :selectionMode="'single'" :columns="approveUnloadDetaiColumn" :data="approveUnloadDetailList" :paginator="false" :showCheckbox="false" @row-select="inputInfo" />
          <Divider />
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 h-full">
            <SearchField type="readOnly" label="자재명" v-model="inputMatName" />
            <SearchField type="date" label="유통기한" v-model="expDate" width="30rem" />
            <SearchField type="number" label="처리수량" v-model="inQty" width="30rem" />
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
          <!--검수결과가 불합격시-->
          <div v-if="result === 'N'" class="text-black-500 mt-5">
            검수결과 불합격 시 입고등록이 불가능하며, 불량등록을 진행해주세요.
            <Textarea type="text" label="불량사유" v-model="defectForm.logMemo" placeholder="불량사유를 입력하세요" rows="5" cols="95" class="mt-5" />
            <FileUpload mode="basic" name="file" chooseLabel="파일 선택" accept="image/*" @select="onFileSelect" class="mt-3 flex justify-center" />
            <div v-if="previewUrl" class="mt-2">
              <img :src="previewUrl" alt="미리보기" class="w-32 rounded-lg border" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 반품 모달
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
  </Dialog> -->
</template>

<scoped>
</scoped>
