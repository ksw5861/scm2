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
import Timeline from 'primevue/timeline';

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

const purchaseListColumn = [
    { label: '주문등록일', field: 'dueDate' },
    { label: '주문번호', field: 'dueDate' },
    { label: '자재명', field: 'dueDate' },
    { label: '주문수량', field: 'dueDate' },
    { label: '공급처', field: 'dueDate' },
    { label: '진행상태', field: 'dueDate' },
    { label: '담당자', field: 'dueDate' },
]

const statusColumn = [
    { label: '변경일', field: 'dueDate' },
    { label: '담당자', field: 'dueDate' },
    { label: '상태', field: 'dueDate' },
    { label: '공급처 출고수량', field: 'dueDate' },
]

const events = ref([
  { status: "주문등록", date: "2025-09-01" },
  { status: "승인완료", date: "2025-09-03" },
  { status: "출고진행", date: "2025-09-05" },
  { status: "입고완료", date: "2025-09-07" },
  { status: "입고완료", date: "2025-09-07" }
]);

</script>

<template>
<div class="container">
    <div class="p-4">
        <Breadcrumb class="rounded-lg" :home="breadcrumbHome" :model="breadcrumbItems" />
    </div>
    <div class="card flex flex-col gap-4">
        <div class="font-semibold text-xl">자재주문 조회</div>
        <Divider />
        <!--search BOX 영역-->
        <div class="flex flex-col gap-4 md:flex-row md:items-end md:gap-6 mt-5 mb-10">
            <SearchField type="dateRange" label="구매요청일자" v-model="dateRange" />
            <SearchField type="textIcon" label="자재명" v-model="materialName" />
            <SearchField type="date" label="등록일" v-model="registerDate" />
            <SearchField type="checkbox" label="상태" v-model="statusList" :options="[ { label: '대기', value: 'WAIT' },
                                                                                       { label: '진행중', value: 'PROGRESS' },
                                                                                       { label: '완료', value: 'DONE' },
                                                                                       { label: '취소', value: 'CANCEL' }]"/>
        <!-- 버튼 영역 -->
        <div class="flex flex-wrap items-center gap-2">
          <btn color="secondary" icon="pi pi-undo" label="초기화" />
          <btn color="contrast" icon="pi pi-search" label="조회" />
        </div>
      </div>
    </div>
    <!--테이블영역--><!--테이블영역-->
       <div class="flex flex-col md:flex-row gap-8">
        <div class="md:w-1/2 planList">
          <div class="card flex flex-col gap-4 h-full">
            <!-- h-full 고정 -->
            <div class="card flex flex-col gap-4">
              <div class="font-semibold text-m">자재주문 목록</div>
              <Divider />
              <selectTable :columns="purchaseListColumn" :data="planMasterList" :paginator="false" :showCheckbox="false" />
            </div>
          </div>
        </div>
        <!--하단우측-->
        <div class="md:w-1/2">
            <div class="card flex flex-col gap-4 h-full">
                <!-- 타이틀 -->
                 <div class="card flex flex-col gap-4">
                    <div class="font-semibold text-m">상세정보</div>
                    <Divider />
                </div>
                <!--타임라인-->
                <div class="card">
                    <Timeline :value="events">
                        <template #opposite="slotProps">
                            <small class="text-surface-500 dark:text-surface-400">{{slotProps.item.date}}</small>
                        </template>
                        <template #content="slotProps">
                            {{slotProps.item.status}}
                        </template>
                    </Timeline>
                </div>
                <div>
                    <selectTable :columns="statusColumn" :data="planMasterList" :paginator="false" :showCheckbox="false" />
                </div>
            </div>
        </div>
    </div>
    </div>
</template>

<style scoped>
</style>
