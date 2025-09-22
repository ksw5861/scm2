<script setup>
import { ref } from 'vue';
import { h } from 'vue';
import Modal from '@/components/common/Modal.vue';
import InputText from 'primevue/inputtext';
import InputGroup from 'primevue/inputgroup';
import InputGroupAddon from 'primevue/inputgroupaddon';

// ================== 검색 조건 ==================
const search = ref({
  orderNo: '',
  vendorName: '',
  statuses: [] // ['출고대기','부분출고','출고완료']
});

// ================== 더미 데이터 ==================
const allShipments = ref([
  { orderNo:'AB-001', vendor:'스타벅스', product:'아라비카 원두', code:'AB-001', qty:100, shipped:50, remain:50, orderDate:'2025-08-01', shipDate:'2025-08-04', staff:'김관리', status:'부분출고', shipQty:0 },
  { orderNo:'AB-002', vendor:'스타벅스', product:'리베리카 원두', code:'AB-002', qty:200, shipped:100, remain:100, orderDate:'2025-08-02', shipDate:'2025-08-04', staff:'김사원', status:'출고완료', shipQty:0 },
  { orderNo:'AB-003', vendor:'스타벅스', product:'리베리카 원두', code:'AB-003', qty:150, shipped:50, remain:100, orderDate:'2025-08-03', shipDate:'2025-08-05', staff:'김오원', status:'출고대기', shipQty:0 },
]);

const list = ref([...allShipments.value]);
const selectedRows = ref([]);

// ================== 모달 ==================
const showVendorModal = ref(false);
const showOrderNoModal = ref(false);

const fetchVendors = async () => [
  { vendorId:'V001', vendorName:'스타벅스' },
  { vendorId:'V002', vendorName:'이디야' }
];

const fetchOrders = async () => [
  { orderNo:'AB-001', vendorName:'스타벅스' },
  { orderNo:'AB-002', vendorName:'스타벅스' },
  { orderNo:'AB-003', vendorName:'스타벅스' },
];

function pickVendor(row){
  search.value.vendorName = row.vendorName;
  showVendorModal.value = false;
}
function pickOrder(row){
  search.value.orderNo = row.orderNo;
  showOrderNoModal.value = false;
}

// ================== 검색/리셋 ==================
function applySearch(){
  const no = (search.value.orderNo || '').toLowerCase();
  const v  = (search.value.vendorName || '').toLowerCase();
  const sts= search.value.statuses;

  list.value = allShipments.value.filter(o=>{
    const noOk = no ? o.orderNo.toLowerCase().includes(no) : true;
    const vOk  = v  ? o.vendor.toLowerCase().includes(v) : true;
    const sOk  = sts.length ? sts.includes(o.status) : true;
    return noOk && vOk && sOk;
  });
}

function resetSearch(){
  search.value = { orderNo:'', vendorName:'', statuses:[] };
  list.value = allShipments.value.map(o => ({...o, shipQty:0}));
  selectedRows.value = [];
}

// ================== 출고 처리 ==================
function doShipment(){
  selectedRows.value.forEach(row=>{
    if (!row.shipQty || row.shipQty <= 0){
      alert(`출고수량을 입력하세요: ${row.orderNo}`);
      return;
    }
    if (row.shipQty > row.remain){
      alert(`출고수량이 잔여수량보다 큽니다: ${row.orderNo}`);
      return;
    }
    row.shipped += row.shipQty;
    row.remain  -= row.shipQty;
    row.status   = row.remain > 0 ? '부분출고' : '출고완료';
    row.shipQty  = 0;
  });
  alert('출고 처리 완료');
}
</script>

<template>
  <div class="page-wrap">
    <div class="page-title">제품출고</div>

    <!-- 검색폼 -->
    <div class="box">
      <div class="box-title">제품출고 검색</div>
      <div class="form-grid-4">
        <!-- 주문번호 -->
        <div class="field">
          <label>주문번호</label>
          <InputGroup>
            <InputText v-model="search.orderNo" placeholder="주문번호" />
            <InputGroupAddon>
              <Button icon="pi pi-search" text @click="showOrderNoModal = true"/>
            </InputGroupAddon>
          </InputGroup>
        </div>

        <!-- 거래처명 -->
        <div class="field">
          <label>거래처명</label>
          <InputGroup>
            <InputText v-model="search.vendorName" placeholder="거래처명" />
            <InputGroupAddon>
              <Button icon="pi pi-search" text @click="showVendorModal = true"/>
            </InputGroupAddon>
          </InputGroup>
        </div>

        <!-- 출고 상태 -->
        <div class="field">
          <label>출고 상태</label>
          <div class="flex gap-3">
            <Checkbox v-model="search.statuses" inputId="st1" value="출고대기" /><label for="st1">출고대기</label>
            <Checkbox v-model="search.statuses" inputId="st2" value="부분출고" /><label for="st2">부분출고</label>
            <Checkbox v-model="search.statuses" inputId="st3" value="출고완료" /><label for="st3">출고완료</label>
          </div>
        </div>

        <!-- 버튼 -->
        <div class="field actions-top">
          <div class="flex gap-2 justify-end">
            <Button label="초기화" icon="pi pi-refresh" outlined @click="resetSearch"/>
            <Button label="조회" icon="pi pi-search" severity="success" @click="applySearch"/>
          </div>
        </div>
      </div>
    </div>

    <!-- 목록 -->
    <div class="list-box">
      <DataTable
        :value="list"
        dataKey="orderNo"
        selectionMode="multiple"
        :selection="selectedRows"
        @update:selection="val => selectedRows = val"
        paginator :rows="7"
      >
        <Column selectionMode="multiple" headerStyle="width:3rem" />
        <Column field="orderNo" header="주문번호"/>
        <Column field="vendor" header="거래처명"/>
        <Column field="product" header="제품명"/>
        <Column field="code" header="제품코드"/>
        <Column field="qty" header="주문수량"/>
        <Column header="출고수량" :body="slotProps => 
          h(InputText, {
            modelValue: slotProps.data.shipQty,
            'onUpdate:modelValue': v => slotProps.data.shipQty = Number(v)
          })"/>
        <Column field="remain" header="잔여수량"/>
        <Column field="orderDate" header="주문일자"/>
        <Column field="shipDate" header="출고일자"/>
        <Column field="staff" header="담당자"/>
        <Column field="status" header="출고상태"/>
      </DataTable>

      <div class="flex justify-end mt-2">
        <Button label="출고" icon="pi pi-check" :disabled="!selectedRows.length" @click="doShipment"/>
      </div>
    </div>

    <!-- 거래처 모달 -->
    <Modal
      :visible="showVendorModal"
      title="거래처 검색"
      idField="vendorId"
      :columns="[{key:'vendorId',label:'거래처ID'},{key:'vendorName',label:'거래처명'}]"
      :fetchData="fetchVendors"
      :page-size="5"
      @select="pickVendor"
      @close="showVendorModal=false"
    />

    <!-- 주문번호 모달 -->
    <Modal
      :visible="showOrderNoModal"
      title="주문번호 검색"
      idField="orderNo"
      :columns="[{key:'orderNo',label:'주문번호'},{key:'vendorName',label:'거래처명'}]"
      :fetchData="fetchOrders"
      :page-size="5"
      @select="pickOrder"
      @close="showOrderNoModal=false"
    />
  </div>
</template>

<style scoped>
.page-wrap{padding:16px;background:#f5f7fb;}
.page-title{font-weight:700;font-size:18px;margin-bottom:12px;}
.box{background:#fff;border:1px solid #e5e7eb;border-radius:10px;padding:16px;margin-bottom:12px;}
.box-title{font-weight:700;margin-bottom:6px;}
.form-grid-4{display:grid;grid-template-columns:repeat(3,1fr);gap:12px;align-items:end;}
.field{display:flex;flex-direction:column;gap:6px;}
.actions-top{grid-column:3/4;}
.list-box{background:#fff;border:1px solid #e5e7eb;border-radius:10px;padding:12px;margin-top:12px;}
</style>
