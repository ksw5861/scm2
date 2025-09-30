<script setup>
import { ref } from 'vue';
import Modal from '@/components/common/Modal.vue';
import axios from 'axios';

// 모달 visible 상태
const isWarehouseModalVisible = ref(false);
const isEmployeeModalVisible = ref(false);

// 선택된 아이템들
const selectedWarehouseItem = ref(null);
const selectedEmployeeItem = ref(null);

// 창고 컬럼 정의
const warehouseColumns = [
  { label: '창고코드', field: 'whId', sortable: true },
  { label: '창고명', field: 'whName', sortable: true }
];

// 사원 컬럼 정의
const employeeColumns = [
  { label: '사원번호', field: 'employeeId', sortable: true },
  { label: '사원명', field: 'name', sortable: true },
  { label: '연락처', field: 'phone' }
];

// 창고 전체 데이터 캐싱용
const warehouseListCache = ref([]);

// 창고 목록 조회 (프론트엔드 페이징)
const fetchWarehouseList = async () => {
  if (warehouseListCache.value.length === 0) {
    try {
      const response = await axios.get('/api/warehouse1');
      warehouseListCache.value = response.data;
    } catch (e) {
      console.error('창고 데이터 조회 실패:', e);
      return { items: [], total: 0 };
    }
  }
  return {
    items: warehouseListCache.value,
    total: warehouseListCache.value.length
  };
};

// 사원 목록 조회 (백엔드 페이징)
const fetchEmployeeList = async ({ page = 1, size = 3, sortField = null, sortOrder = null } = {}) => {
  try {
    const params = { page, size };
    if (sortField) params.sortField = sortField;
    if (sortOrder) params.sortOrder = sortOrder;

    const response = await axios.get('/api/employee', { params });
    return {
      items: response.data.data || response.data.items || [],
      total: response.data.page?.totalElements || 0
    };
  } catch (e) {
    console.error('사원 데이터 조회 실패:', e);
    return { items: [], total: 0 };
  }
};

// 창고 선택 핸들러
const handleWarehouseSelect = (item) => {
  selectedWarehouseItem.value = item;
  isWarehouseModalVisible.value = false;
};

// 사원 선택 핸들러
const handleEmployeeSelect = (item) => {
  selectedEmployeeItem.value = item;
  isEmployeeModalVisible.value = false;
};
</script>

<template>
  <Fluid>
    <div class="card flex flex-col gap-4">
      <div class="font-semibold text-xl">창고선택</div>
      <Btn @click="isWarehouseModalVisible = true">창고 모달 열기</Btn>
    </div>

    <div class="card flex flex-col gap-4">
      <div class="font-semibold text-xl">사원선택</div>
      <Btn @click="isEmployeeModalVisible = true">사원 모달 열기</Btn>
    </div>
  </Fluid>

  <pre class="mt-4">
        선택된 창고: {{ selectedWarehouseItem }}
    </pre
  >

  <pre class="mt-4">
        선택된 사원: {{ selectedEmployeeItem }}
    </pre
  >

  <!-- 창고 모달 (프론트엔드 페이징) -->
  <Modal :visible="isWarehouseModalVisible"
  title="창고 검색"
  :columns="warehouseColumns"
   dataKey="whId" :fetchData="fetchWarehouseList" :pageSize="5" :frontPagination="true"
    @select="handleWarehouseSelect" @close="isWarehouseModalVisible = false" />

  <!-- 사원 모달 (백엔드 페이징) -->
  <Modal :visible="isEmployeeModalVisible" title="사원 검색" :columns="employeeColumns"
   dataKey="employeeId" :fetchData="fetchEmployeeList" :pageSize="3" :frontPagination="false"
  @select="handleEmployeeSelect"
  @close="isEmployeeModalVisible = false" />
</template>
