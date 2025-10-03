<template>
  <div class="page">
    <header class="header">
      <div class="title">
        <h1>배송 준비중</h1>
        <p class="subtitle">바코드를 스캔해 출고완료 처리하세요</p>
      </div>
      <div class="header-actions">
        <Button
          label="새로고침"
          icon="pi pi-refresh"
          :loading="isLoading"
          class="p-button-text"
          @click="refresh"
        />
      </div>
    </header>

    <section class="card">
      <DataTable
        :value="orders"
        dataKey="shipId"
        :loading="isLoading"
        :paginator="true"
        :rows="10"
        :rowsPerPageOptions="[10, 20, 50]"
        responsiveLayout="scroll"
        class="order-table"
        emptyMessage="‘배송준비중’ 상태의 주문이 없습니다."
      >
        <Column field="shipId" header="출하번호" :sortable="true">
          <template #body="{ data }">
            <span class="mono">{{ data.SHIPID }}</span>
          </template>
        </Column>

        <Column field="status" header="상태" :sortable="true" style="width: 180px">
          <template #body="{ data }">
            <Tag :value="data.STATUS" severity="info" class="pill" />
          </template>
        </Column>

        <Column header="출고 바코드" style="width: 180px">
          <template #body="{ data }">
            <Button
              label="바코드 보기"
              icon="pi pi-qrcode"
              :disabled="!isJsBarcodeReady"
              @click="showBarcode(data.SHIPID)"
            />
          </template>
        </Column>
      </DataTable>
    </section>

    <Dialog
      v-model:visible="barcodeVisible"
      :modal="true"
      :closable="true"
      :dismissableMask="true"
      :style="{ width: '520px' }"
      header="주문 바코드"
      class="barcode-dialog"
    >
      <div class="barcode-wrap barcode-print" ref="barcodeArea">
        <svg id="barcode-selected"></svg>
        <div class="barcode-caption mono">{{ selectedShipId }}</div>
      </div>
      <template #footer>
        <Button label="닫기" class="p-button-text" @click="closeModal" />
        <Button label="인쇄" icon="pi pi-print" @click="printBarcode" />
      </template>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import axios from 'axios'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import Tag from 'primevue/tag'

const orders = ref([])
const isLoading = ref(true)
const isJsBarcodeReady = ref(false)

const barcodeVisible = ref(false)
const selectedShipId = ref(null)
const barcodeArea = ref(null)

async function fetchOrders() {
  try {
    isLoading.value = true
    const { data } = await axios.get('/api/goDel/pending')
    orders.value = Array.isArray(data) ? data : []
  } catch (err) {
    console.error('주문 목록 조회 실패:', err)
    orders.value = []
  } finally {
    isLoading.value = false
    isJsBarcodeReady.value = !!(window && window.JsBarcode)
  }
}

onMounted(fetchOrders)

const refresh = () => fetchOrders()

const showBarcode = async (shipId) => {
  selectedShipId.value = shipId
  barcodeVisible.value = true
  await nextTick()
  if (!window || !window.JsBarcode) return
  const svg = document.querySelector('#barcode-selected')
  if (svg) svg.innerHTML = ''
  window.JsBarcode('#barcode-selected', shipId, {
    format: 'CODE128',
    width: 2,
    height: 60,
    displayValue: false,
    margin: 8
  })
}

const closeModal = () => {
  barcodeVisible.value = false
  const svg = document.querySelector('#barcode-selected')
  if (svg) svg.innerHTML = ''
}

const printBarcode = () => {
  if (!barcodeArea.value) return

  const content = barcodeArea.value.outerHTML

  // 원하는 창 크기
  const width = 1600
  const height = 900

  // 가운데 위치 계산
  const left = (window.screen.width / 2) - (width / 2)
  const top = (window.screen.height / 2) - (height / 2)

  const printWindow = window.open(
    '',
    '',
    `width=${width},height=${height},left=${left},top=${top}`
  )

  printWindow.document.write(`
    <html>
      <head>
        <title>바코드 출력</title>
        <style>
          @page { size: 80mm 40mm; margin: 0; }
          body { margin: 0; display: flex; align-items: center; justify-content: center; }
          svg { width: 100%; height: auto; }
          .barcode-caption { font-size: 12px; text-align: center; margin-top: 4px; }
        </style>
      </head>
      <body>${content}</body>
    </html>
  `)

  printWindow.document.close()
  printWindow.focus()
  printWindow.print()
  printWindow.close()
}
</script>

<style scoped>
:root {
  --bg: #f6f7f9;
  --card: #ffffff;
  --text: #0b0c0e;
  --muted: #6b7280;
  --line: #e5e7eb;
  --shadow: 0 8px 30px rgba(0,0,0,0.04);
  --blue: #0a84ff;
}
@media (prefers-color-scheme: dark) {
  :root {
    --bg: #0b0c0e;
    --card: #121416;
    --text: #f2f3f5;
    --muted: #a1a1aa;
    --line: #1f2328;
    --shadow: 0 8px 30px rgba(0,0,0,0.4);
    --blue: #0a84ff;
  }
}

.page { max-width: 1000px; margin: 48px auto; padding: 0 20px; color: var(--text); }
.header { display: flex; align-items: end; justify-content: space-between; margin-bottom: 16px; }
.title h1 { font-weight: 700; letter-spacing: -0.02em; margin: 0 0 6px; font-size: 28px; }
.subtitle { margin: 0; color: var(--muted); font-size: 14px; }
.header-actions { display: flex; gap: 8px; }
.card { background: var(--card); border: 1px solid var(--line); border-radius: 16px; box-shadow: var(--shadow); padding: 12px; }

.order-table :deep(.p-datatable-thead > tr > th) {
  background: linear-gradient(to bottom, rgba(0,0,0,0.02), transparent);
  border-bottom: 1px solid var(--line);
  color: var(--muted);
  font-weight: 600;
}
.order-table :deep(.p-datatable-tbody > tr > td) {
  border-bottom: 1px solid var(--line);
  vertical-align: middle;
  padding: 14px 16px;
}
.order-table :deep(.p-datatable-tbody > tr:hover) { background: rgba(0,0,0,0.03); }

.mono { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; }
.pill { font-weight: 700; letter-spacing: 0.02em; }

.barcode-dialog :deep(.p-dialog-content) { padding-top: 8px; }
.barcode-wrap { display: grid; place-items: center; padding: 16px 8px; }
.barcode-caption { margin-top: 8px; font-size: 12px; color: var(--muted); }
</style>
