<script setup>
import { useAppToast } from '@/composables/useAppToast'
import InputText from 'primevue/inputtext'
import { ref } from 'vue'
import DTable from '@/components/common/DTable.vue'
import Modal from '@/components/common/Modal.vue'
import axios from 'axios'

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
