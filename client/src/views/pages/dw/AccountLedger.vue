<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Calendar from 'primevue/calendar'
import InputText from 'primevue/inputtext'
import Dropdown from 'primevue/dropdown'
import { useAppToast } from '@/composables/useAppToast'  // 팀 공통토스트
import { useIcon } from '@/composables/useIcon'           // 팀 공통아이콘

const { toastSuccess, toastError } = useAppToast()
const { SearchIcon, ExcelIcon, PdfIcon } = useIcon()

/* 검색 상태 */
const search = ref({
  vendorType: 'all',
  vendorName: '',
  fromDate: null,
  toDate: null,
})

/* 데이터 상태 */
const ledgerList = ref([])       // 표 데이터
const chartLabels = ref([])      // 차트용 라벨
const chartData = ref([])        // 차트용 데이터

/* 로딩 표시 */
const loading = ref(false)

/* 검색 실행 */
async function fetchLedger() {
  try {
    loading.value = true
    const params = {
      vendorType: search.value.vendorType,
      vendorName: search.value.vendorName,
      fromDate: search.value.fromDate,
      toDate: search.value.toDate,
    }
    // ✅ 프론트는 /api, 서버는 /ledger 로 연결됨
    const res = await axios.get('/api/ledger', { params })
    ledgerList.value = res.data?.data || []

    // 그래프용 데이터 구성 (잔액 흐름)
    chartLabels.value = ledgerList.value.map(v => v.vendorName)
    chartData.value = ledgerList.value.map(v => v.balance || 0)
    toastSuccess('거래처원장 조회 완료')
  } catch (err) {
    console.error(err)
    toastError('조회 중 오류 발생')
  } finally {
    loading.value = false
  }
}

/* 초기 실행 */
onMounted(fetchLedger)
</script>

<template>
  <div class="page-wrap">
    <div class="page-title">거래처원장</div>

    <!-- 검색 폼 -->
    <div class="box">
      <div class="box-title">검색 조건</div>
      <div class="form-grid">
        <div class="field">
          <label>거래처구분</label>
          <Dropdown
            v-model="search.vendorType"
            :options="[
              { label: '전체', value: 'all' },
              { label: '판매처', value: 'customer' },
              { label: '공급처', value: 'supplier' }
            ]"
            optionLabel="label" optionValue="value" class="w-full"
          />
        </div>
        <div class="field">
          <label>거래처명</label>
          <InputText v-model="search.vendorName" placeholder="거래처명 입력" class="w-full" />
        </div>
        <div class="field">
          <label>기간</label>
          <div class="flex gap-2">
            <Calendar v-model="search.fromDate" showIcon dateFormat="yy-mm-dd" class="w-full" />
            <Calendar v-model="search.toDate" showIcon dateFormat="yy-mm-dd" class="w-full" />
          </div>
        </div>
      </div>

      <div class="actions">
        <Button :icon="SearchIcon" label="조회" @click="fetchLedger" :loading="loading" />
      </div>
    </div>

    <!-- 요약 표 -->
    <div class="list-box">
      <div class="sub-title">거래처별 원장 목록</div>
      <DataTable :value="ledgerList" paginator :rows="10" dataKey="vendorName">
        <Column field="vendorName" header="거래처명" />
        <Column field="type" header="구분" />
        <Column field="totalAmount" header="총금액">
          <template #body="{ data }">{{ data.totalAmount?.toLocaleString() }}원</template>
        </Column>
        <Column field="receivable" header="미수금">
          <template #body="{ data }">
            <span style="color:red">{{ data.receivable?.toLocaleString() }}</span>
          </template>
        </Column>
        <Column field="payable" header="미지급금">
          <template #body="{ data }">
            <span style="color:blue">{{ data.payable?.toLocaleString() }}</span>
          </template>
        </Column>
        <Column field="remark" header="비고" />
      </DataTable>
    </div>
  </div>
</template>

<style scoped>
.page-wrap { padding: 16px; background: #f5f7fb; }
.page-title { font-size: 1.2rem; font-weight: 700; margin-bottom: 12px; }
.box { background: #fff; border-radius: 10px; padding: 16px; border: 1px solid #e5e7eb; margin-bottom: 12px; }
.box-title { font-weight: 600; margin-bottom: 12px; }
.form-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.field { display: flex; flex-direction: column; gap: 6px; }
.actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 8px; }
.list-box { background: #fff; border-radius: 10px; padding: 16px; border: 1px solid #e5e7eb; }
.sub-title { font-weight: 600; margin-bottom: 8px; }
</style>
