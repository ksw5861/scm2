<template>
  <div class="p-5">
    <!-- HEADER -->
    <div class="flex items-center justify-between mb-4">
      <h1 class="text-2xl font-semibold">원두 판매가 설정</h1>
      <div class="flex gap-2">
        <Button icon="pi pi-plus" label="원두 추가" outlined @click="openModal" />
        <Button icon="pi pi-save" label="저장" @click="onSave" />
      </div>
    </div>

    <p class="text-sm text-600 mb-4">본사 구매가에 마진율을 적용하여 판매가를 설정하세요</p>

    <!-- SUMMARY -->
    <div class="grid gap-4 md:grid-cols-4 mb-4">
      <Card class="shadow-sm">
        <template #title><div class="text-sm text-600">총 원두 품목</div></template>
        <template #content><div class="text-2xl font-bold">{{ rows.length }}개</div></template>
      </Card>

      <Card class="shadow-sm">
        <template #title><div class="text-sm text-600">POS 노출 품목</div></template>
        <template #content><div class="text-2xl font-bold">{{ posCount }}개</div></template>
      </Card>

      <Card class="shadow-sm">
        <template #title><div class="text-sm text-600">평균 마진율</div></template>
        <template #content><div class="text-2xl font-bold">{{ avgMargin }}%</div></template>
      </Card>

      <Card class="shadow-sm">
        <template #title><div class="text-sm text-600">총 구매 원가</div></template>
        <template #content><div class="text-2xl font-bold">{{ fmt(totalCost) }}</div></template>
      </Card>
    </div>

    <!-- 일괄조정 -->
    <div class="grid gap-4 md:grid-cols-2 mb-3">
      <Card class="shadow-sm">
        <template #title><div class="text-sm text-600">예상 총 이익</div></template>
        <template #content><div class="text-2xl font-bold text-primary">+{{ fmt(totalProfit) }}</div></template>
      </Card>

      <div class="flex items-center gap-2">
        <span class="text-600 text-sm">일괄 마진율 적용:</span>
        <div class="flex gap-2 flex-wrap">
          <Button
            v-for="r in [20, 30, 35, 40, 50]"
            :key="r"
            :label="r + '%'"
            size="small"
            outlined
            @click="applyBulkMargin(r)"
          />
        </div>
      </div>
    </div>

    <!-- DATATABLE -->
    <Card class="shadow-sm">
      <template #content>
        <DataTable :value="rows" dataKey="id" responsiveLayout="scroll">
          <Column header="No." style="width: 60px" bodyClass="text-center">
            <template #body="slotProps">{{ slotProps.index + 1 }}</template>
          </Column>

          <Column field="name" header="원두명" style="min-width: 180px" />
          <Column field="cost" header="본사 구매가" style="min-width: 130px; text-align:right" :body="(r) => fmt(r.cost)" />

          <Column header="마진율 (%)" style="min-width: 150px">
            <template #body="{ data, index }">
              <InputNumber
                :modelValue="data.margin"
                :min="0"
                :max="90"
                suffix="%"
                inputClass="w-20 text-right"
                showButtons
                buttonLayout="horizontal"
                decrementButtonClass="p-button-text"
                incrementButtonClass="p-button-text"
                @update:modelValue="(val) => updateMargin(index, val)"
              />
            </template>
          </Column>

          <Column header="예상 이익" style="min-width: 120px; text-align:right">
            <template #body="{ data }">+{{ fmt(profitOf(data)) }}</template>
          </Column>

          <Column header="판매가" style="min-width: 130px; text-align:right">
            <template #body="{ data }">{{ fmt(priceOf(data)) }}</template>
          </Column>

          <!-- ✅ ToggleSwitch 적용 -->
          <Column header="POS 노출" style="width: 110px" bodyClass="text-center">
            <template #body="{ data, index }">
              <ToggleSwitch
                :modelValue="data.pos"
                @update:modelValue="(val) => updatePos(index, val)"
              />
            </template>
          </Column>

          <Column header="순서" style="width: 110px" bodyClass="text-center">
            <template #body="{ index }">
              <div class="flex justify-center gap-2">
                <Button icon="pi pi-angle-up" rounded text severity="secondary" @click="moveUp(index)" />
                <Button icon="pi pi-angle-down" rounded text severity="secondary" @click="moveDown(index)" />
              </div>
            </template>
          </Column>

          <Column header="삭제" style="width: 90px" bodyClass="text-center">
            <template #body="{ data }">
              <Button icon="pi pi-trash" rounded text severity="danger" @click="onDelete(data)" />
            </template>
          </Column>
        </DataTable>
      </template>
    </Card>

    <!-- 🔍 제품검색 모달 -->
    <Dialog
      v-model:visible="isShowModal"
      header="제품 검색"
      :modal="true"
      :draggable="false"
      :style="{ width: '600px' }"
    >
      <InputText v-model="search" placeholder="제품명 검색" class="w-full mb-3" />

      <DataTable
        :value="filteredProducts"
        paginator
        :rows="15"
        responsiveLayout="scroll"
        selectionMode="single"
        @rowClick="handleSelect"
      >
        <Column field="prodId" header="제품코드" />
        <Column field="prodName" header="제품명" />
        <Column field="spec" header="규격" />
        <Column field="unit" header="단위" />
        <Column field="prodUnitPrice" header="가격">
          <template #body="{ data }">{{ fmt(data.prodUnitPrice) }}</template>
        </Column>
      </DataTable>
    </Dialog>

    <Toast />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { useToast } from 'primevue/usetoast'

import Button from 'primevue/button'
import Card from 'primevue/card'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import ToggleSwitch from 'primevue/toggleswitch'
import InputNumber from 'primevue/inputnumber'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Toast from 'primevue/toast'

interface Row {
  id: number
  name: string
  origin: string
  cost: number
  margin: number
  pos: boolean
}

const rows = ref<Row[]>([])
const isShowModal = ref(false)
const search = ref('')
const productList = ref<any[]>([])
const toast = useToast()

const fetchProducts = async () => {
  try {
    const { data } = await axios.get('/api/products')
    productList.value = data.items || []
  } catch (err) {
    console.error('❌ 상품 목록 로드 실패:', err)
  }
}

onMounted(async () => {
  await fetchProducts()
  try {
    const { data } = await axios.get('/api/sales/margin/list')
    rows.value = data.map((r: any) => ({
      id: Number(r.saleProdId),
      name: r.saleProdName,
      origin: '-',
      cost: r.prodUnitPrice,
      margin: r.saleMargin,
      pos: r.posShowYn === 'Y'
    }))
  } catch (err) {
    toast.add({ severity: 'error', summary: '로드 실패', detail: '데이터를 불러오지 못했습니다.', life: 3000 })
  }
})

const fmt = (n: number) =>
  new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW', maximumFractionDigits: 0 }).format(n)
const profitOf = (r: Row) => Math.round(r.cost * (r.margin / 100))
const priceOf = (r: Row) => r.cost + profitOf(r)
const totalCost = computed(() => rows.value.reduce((a, r) => a + r.cost, 0))
const totalProfit = computed(() => rows.value.reduce((a, r) => a + profitOf(r), 0))
const avgMargin = computed(() =>
  rows.value.length ? (rows.value.reduce((a, r) => a + r.margin, 0) / rows.value.length).toFixed(1) : '0.0'
)
const posCount = computed(() => rows.value.filter((r) => r.pos).length)

const updateMargin = (index: number, val: number) => {
  const copy = [...rows.value]
  copy[index].margin = val
  rows.value = copy
}

const updatePos = (index: number, val: boolean) => {
  const copy = [...rows.value]
  copy[index].pos = val
  rows.value = copy
}

const openModal = () => (isShowModal.value = true)

const filteredProducts = computed(() =>
  search.value
    ? productList.value.filter((p: any) =>
        p.prodName.toLowerCase().includes(search.value.toLowerCase())
      )
    : productList.value
)

// ✅ 클릭 시 자동 추가 (중복 방지 포함)
const handleSelect = (event: any) => {
  const p = event.data
  if (!p) return

  if (rows.value.some((r) => r.id === p.prodId)) {
    toast.add({ severity: 'warn', summary: '중복 항목', detail: '이미 추가된 제품입니다.', life: 2000 })
    return
  }

  rows.value.push({
    id: p.prodId,
    name: p.prodName,
    origin: p.origin || '-',
    cost: p.prodUnitPrice,
    margin: 30,
    pos: false
  })

  isShowModal.value = false
  toast.add({ severity: 'success', summary: '추가 완료', detail: `"${p.prodName}" 추가됨`, life: 2000 })
}

const applyBulkMargin = (rate: number) => {
  rows.value = rows.value.map((r) => ({ ...r, margin: rate }))
  toast.add({ severity: 'info', summary: '일괄 적용', detail: `${rate}% 적용 완료`, life: 2000 })
}

const moveUp = (i: number) => {
  if (i <= 0) return
  const arr = [...rows.value]
  ;[arr[i - 1], arr[i]] = [arr[i], arr[i - 1]]
  rows.value = arr
}

const moveDown = (i: number) => {
  const arr = [...rows.value]
  if (i >= arr.length - 1) return
  ;[arr[i + 1], arr[i]] = [arr[i], arr[i + 1]]
  rows.value = arr
}

const onDelete = async (row: Row) => {
  if (!confirm(`"${row.name}" 삭제하시겠습니까?`)) return
  await axios.delete(`/sales/margin/${row.id}`)
  rows.value = rows.value.filter((r) => r.id !== row.id)
  toast.add({ severity: 'warn', summary: '삭제됨', detail: `"${row.name}"가 삭제되었습니다.`, life: 2000 })
}

const onSave = async () => {
  const payload = rows.value.map((r, i) => ({
    saleProdId: String(r.id),
    saleProdName: r.name,
    prodUnitPrice: r.cost,
    saleMargin: r.margin,
    posShowYn: r.pos ? 'Y' : 'N',
    sortNo: i + 1,
    saleProdPrice: priceOf(r)
  }))
  await axios.post('/api/sales/margin/save-all', payload)
  toast.add({ severity: 'success', summary: '저장 완료', detail: '저장 성공', life: 2000 })
}
</script>

<style scoped>
.text-600 { color: var(--surface-600); }
.shadow-sm { box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06); }
</style>
