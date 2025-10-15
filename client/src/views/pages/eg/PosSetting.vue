<template>
  <div class="p-5">
    <!-- ======================== HEADER ======================== -->
    <div class="flex items-center justify-between mb-4">
      <h1 class="text-2xl font-semibold">원두 판매가 설정</h1>
      <div class="flex gap-2">
        <Button icon="pi pi-plus" label="원두 추가" outlined @click="isShowModal = true" />
        <Button icon="pi pi-save" label="저장" @click="onSave" />
      </div>
    </div>

    <p class="text-sm text-600 mb-4">본사 구매가에 마진율을 적용하여 판매가를 설정하세요</p>

    <!-- ======================== SUMMARY CARDS ======================== -->
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

    <!-- ======================== 하단 통계 및 일괄조정 ======================== -->
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

    <!-- ======================== DATATABLE ======================== -->
    <Card class="shadow-sm">
      <template #content>
        <DataTable :value="rows" dataKey="id" responsiveLayout="scroll">
          <Column header="No." style="width: 60px" bodyClass="text-center">
            <template #body="slotProps">{{ slotProps.index + 1 }}</template>
          </Column>
          <Column field="name" header="원두명" style="min-width: 180px" />
          <Column field="origin" header="원산지" style="min-width: 140px" />
          <Column header="본사 구매가" style="min-width: 130px; text-align:right" :body="(r) => fmt(r.cost)" />
          <Column header="마진율 (%)" style="min-width: 150px">
            <template #body="{ data }">
              <InputNumber
                v-model="data.margin"
                :min="0"
                :max="90"
                suffix="%"
                inputClass="w-20 text-right"
                showButtons
                buttonLayout="horizontal"
                decrementButtonClass="p-button-text"
                incrementButtonClass="p-button-text"
              />
            </template>
          </Column>
          <Column header="예상 이익" style="min-width: 120px; text-align:right">
            <template #body="{ data }">+{{ fmt(profitOf(data)) }}</template>
          </Column>
          <Column header="판매가" style="min-width: 130px; text-align:right">
            <template #body="{ data }">{{ fmt(priceOf(data)) }}</template>
          </Column>
          <Column header="POS 노출" style="width: 110px" bodyClass="text-center">
            <template #body="{ data }"><InputSwitch v-model="data.pos" /></template>
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

    <!-- ======================== 🔍 제품검색 모달 (주문등록.vue 재사용) ======================== -->
    <Dialog v-model:visible="isShowModal" header="제품 검색" :style="{ width: '600px' }" modal>
      <InputText v-model="search" placeholder="제품명 검색" class="w-full mb-3" />

      <DataTable
        :value="filteredProducts"
        paginator
        :rows="15"
        responsiveLayout="scroll"
        selectionMode="single"
        v-model:selection="selectedProduct"
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import Button from 'primevue/button'
import Card from 'primevue/card'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InputSwitch from 'primevue/inputswitch'
import InputNumber from 'primevue/inputnumber'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'

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
const productList = ref([])
const selectedProduct = ref(null)

/* -------------------- 상품 목록 불러오기 -------------------- */
const fetchProducts = async () => {
  try {
    const { data } = await axios.get('/product/list')
    productList.value = data
  } catch (err) {
    console.error('❌ 상품 목록 로드 실패:', err)
  }
}

onMounted(async () => {
  fetchProducts()
  try {
    const { data } = await axios.get('/sales/margin/list')
    rows.value = data.map((r: any) => ({
      id: Number(r.saleProdId),
      name: r.saleProdName,
      origin: '-',
      cost: r.prodUnitPrice,
      margin: r.saleMargin,
      pos: r.posShowYn === 'Y'
    }))
  } catch (err) {
    alert('서버에서 데이터를 불러오지 못했습니다.')
  }
})

/* -------------------- 포맷 및 계산 -------------------- */
const fmt = (n: number) =>
  new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW', maximumFractionDigits: 0 }).format(n)
const profitOf = (r: Row) => Math.round(r.cost * (r.margin / 100))
const priceOf = (r: Row) => r.cost + profitOf(r)
const totalCost = computed(() => rows.value.reduce((a, r) => a + r.cost, 0))
const totalProfit = computed(() => rows.value.reduce((a, r) => a + profitOf(r), 0))
const avgMargin = computed(() => (rows.value.length ? (rows.value.reduce((a, r) => a + r.margin, 0) / rows.value.length).toFixed(1) : '0.0'))
const posCount = computed(() => rows.value.filter((r) => r.pos).length)

/* -------------------- 모달 검색 필터 -------------------- */
const filteredProducts = computed(() =>
  search.value ? productList.value.filter((p: any) => p.prodName.toLowerCase().includes(search.value.toLowerCase())) : productList.value
)

/* -------------------- 제품 선택시 테이블 추가 -------------------- */
function handleSelect() {
  if (!selectedProduct.value) return
  const p: any = selectedProduct.value
  const exists = rows.value.some((r) => r.name === p.prodName)
  if (exists) {
    alert('이미 추가된 제품입니다.')
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
}

/* -------------------- 사용자 액션 -------------------- */
function applyBulkMargin(rate: number) {
  rows.value = rows.value.map((r) => ({ ...r, margin: rate }))
}
function moveUp(i: number) {
  if (i <= 0) return
  const arr = rows.value
  ;[arr[i - 1], arr[i]] = [arr[i], arr[i - 1]]
}
function moveDown(i: number) {
  const arr = rows.value
  if (i >= arr.length - 1) return
  ;[arr[i + 1], arr[i]] = [arr[i], arr[i + 1]]
}
async function onDelete(row: Row) {
  if (!confirm(`"${row.name}" 삭제하시겠습니까?`)) return
  await axios.delete(`/sales/margin/${row.id}`)
  rows.value = rows.value.filter((r) => r.id !== row.id)
}
async function onSave() {
  const payload = rows.value.map((r, i) => ({
    saleProdId: String(r.id),
    saleProdName: r.name,
    prodUnitPrice: r.cost,
    saleMargin: r.margin,
    posShowYn: r.pos ? 'Y' : 'N',
    sortNo: i + 1
  }))
  await axios.post('/sales/margin/save-all', payload)
  alert('저장되었습니다 ✅')
}
</script>

<style scoped>
.text-600 { color: var(--surface-600); }
.text-700 { color: var(--surface-700); }
.shadow-sm { box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06); }
</style>
