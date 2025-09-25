<script setup>
import { ref, watch } from 'vue'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'

const props = defineProps({
  visible: Boolean,
  title: String,
  columns: Array,
  fetchData: Function,
  pageSize: { type: Number, default: 5 },
  frontPagination: { type: Boolean, default: false }
})

const emit = defineEmits(['close', 'select'])

const dialog = ref(false)
const search = ref('')
const data = ref([])
const allData = ref([])
const loading = ref(false)

const page = ref({
  page: 1,
  size: props.pageSize,
  totalElements: 0
})

const sortField = ref(null)
const sortOrder = ref(null)

const getFilteredSortedData = () => {
  let result = [...allData.value]

  // 검색
  if (search.value) {
    result = result.filter(item =>
      Object.values(item).some(val =>
        String(val).toLowerCase().includes(search.value.toLowerCase())
      )
    )
  }

  // 정렬
  if (sortField.value) {
    result.sort((a, b) => {
      const valA = a[sortField.value]
      const valB = b[sortField.value]
      if (valA < valB) return sortOrder.value === 1 ? -1 : 1
      if (valA > valB) return sortOrder.value === 1 ? 1 : -1
      return 0
    })
  }

  return result
}

const loadData = async () => {
  loading.value = true

  try {
    if (props.frontPagination) {
      if (allData.value.length === 0) {
        const res = await props.fetchData()
        allData.value = res.items || []
      }

      const filteredSorted = getFilteredSortedData()
      page.value.totalElements = filteredSorted.length

      const start = (page.value.page - 1) * page.value.size
      const end = start + page.value.size
      data.value = filteredSorted.slice(start, end)
    } else {
      const res = await props.fetchData({
        page: page.value.page,
        size: page.value.size,
        search: search.value,
        sortField: sortField.value,
        sortOrder: sortOrder.value
      })
      data.value = res.items || []
      page.value.totalElements = res.total || 0
    }
  } catch (error) {
    console.error('데이터 로딩 오류:', error)
  } finally {
    loading.value = false
  }
}

const close = () => {
  dialog.value = false
  search.value = ''
  page.value.page = 1
  page.value.totalElements = 0
  allData.value = []
  emit('close')
}

const selectItem = (item) => {
  emit('select', item)
  close()
}

const onPageChange = (event) => {
  page.value.page = event.page
  page.value.size = event.size
  loadData()
}

const onSortChange = (event) => {
  sortField.value = event.sortField
  sortOrder.value = event.sortOrder
  page.value.page = 1
  loadData()
}

watch(() => props.visible, (val) => {
  dialog.value = val
  if (val) loadData()
})

watch(search, () => {
  page.value.page = 1
  loadData()
})
</script>

<template>
  <Dialog
    :visible="dialog"
    modal
    :style="{ width: '800px' }"
    :closable="false"
    @hide="close"
  >
    <template #header>
      <div class="text-lg font-bold flex justify-between items-center w-full">
        {{ title }}
        <Button icon="pi pi-times" class="p-button-text" @click="close" />
      </div>
    </template>

    <div class="mb-3">
      <InputText v-model="search" placeholder="검색" class="w-full" />
    </div>

    <DTable
      :columns="columns"
      :data="data"
      :page="page"
      :loading="loading"
      :sortField="sortField"
      :sortOrder="sortOrder"
      dataKey="id"
      @row-select="selectItem"
      @page-change="onPageChange"
      @sort-change="onSortChange"
    />
  </Dialog>
</template>
