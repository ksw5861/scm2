<script setup>
import { useAppToast } from '@/composables/useAppToast'
import InputText from 'primevue/inputtext'
import { ref } from 'vue'
import DTable from '@/components/common/DTable.vue'
import Modal from '@/components/common/Modal.vue'
import axios from 'axios'
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';
import { useIcon } from '@/composables/useIcon';
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'

const displayConfirmation = ref(false);

const closeConfirmation = () => {
  displayConfirmation.value = false;
};

const openConfirmation = () => {
  displayConfirmation.value = true;
};



const showDialog = ref(false)

const onConfirm = () => {
  alert('확인 클릭됨!')
}

const onCancel = () => {
  alert('취소 클릭됨!')
}



const { toast } = useAppToast()

const isShowModal = ref(false)
const selectedItem = ref(null)

const columns = [
  { label: '창고코드', field: 'whId', sortable: true },
  { label: '창고명', field: 'whName', sortable: true }
]

const data = [
  { whId: 'WH001', whName: '창고1' },
  { whId: 'WH002', whName: '창고2' },
  { whId: 'WH003', whName: '창고3' },
  { whId: 'WH004', whName: '창고4' },
  { whId: 'WH005', whName: '창고5' },
  { whId: 'WH006', whName: '창고6' },
  { whId: 'WH007', whName: '창고7' },
  { whId: 'WH008', whName: '창고8' },
  { whId: 'WH009', whName: '창고9' },
  { whId: 'WH010', whName: '창고10' }
]

const fetchWarehouseData = async ({ page, size, search, sortField, sortOrder }) => {
  const response = await axios.get('/api/warehouse', {
    params: {
      page,
      size,
      search,
      sortField,
      sortOrder
    }
  })
  return {
    items: response.data.items,
    total: response.data.total
  }
}

const handleSelect = (item) => {
  selectedItem.value = item
  isShowModal.value = false
}
</script>

<template>
  <Fluid>
    <div class="card flex flex-col gap-4">
      <div class="font-semibold text-xl">창고관리</div>
      <div class="flex flex-col gap-2">
        <label for="name1">입력</label>
        <InputText id="name1" type="text" />
      </div>
    </div>

    <div class="card flex flex-col gap-4">
      <div class="font-semibold text-xl">창고목록</div>
      <Btn @click="isShowModal = true">모달</Btn>
      <div class="flex flex-col gap-2">
        <DTable :columns="columns" :data="data" :paginator="true" :rows="10" />
      </div>
    </div>
  </Fluid>

  <pre class="mt-4">선택된 창고: {{ selectedItem }}</pre>

  <div class="card">
    <div class="font-semibold text-xl mb-4">사원 등록</div>
    <Button label="Delete" icon="pi pi-trash" severity="danger" style="width: auto" @click="openConfirmation" />

    <Dialog header="사원 등록" v-model:visible="displayConfirmation" :style="{ width: '350px' }" :modal="true">
      <div class="flex items-center justify-center">
        <i class="pi pi-exclamation-triangle mr-4" style="font-size: 2rem" />
        <span>등록하시겠습니까?</span>
      </div>
      <template #footer>
        <Button label="취소" :icon="useIcon('cancel')" @click="closeConfirmation" text severity="secondary" />
        <Button label="등록" :icon="useIcon('check')" @click="closeConfirmation" severity="primary" outlined autofocus />
      </template>
    </Dialog>

  <Button label="등록" @click="showDialog = true" />

  <ConfirmDialog
    v-model="showDialog"
    header="사원 등록"
    message="등록하시겠습니까?"
    cancelLabel="취소"
    confirmLabel="등록"
    cancelIcon="cancel"
    confirmIcon="check"
    confirmSeverity="primary"
    @confirm="onConfirm"
    @cancel="onCancel"
  />

  </div>

  <Modal
    :visible="isShowModal"
    title="창고 검색"
    idField="whId"
    :columns="[
      { key: 'whId', label: '창고번호' },
      { key: 'whName', label: '창고명' }
    ]"
    :fetchData="fetchWarehouseData"
    :page-size="5"
    @select="handleSelect"
    @close="isShowModal = false"
  />
</template>
