<script setup>
import { useAppToast } from '@/composables/useAppToast';
import InputText from 'primevue/inputtext';
import { ref } from 'vue';

const { toast } = useAppToast();
const input = ref(null);

const toastTest = () => {
  if (!input.value) return toast('error', '필수 입력(제목)', '값을 입력하여주세요(내용)');
  console.log('제츨됨');
};
</script>

<template>
  <InputText placeholder="입력" v-model="input" />
  <Btn color="primary" icon="check" outlined @click="toastTest">제출</Btn>
  <br />
  <br />
  <Btn icon="check">테스트</Btn>
  <br />
  <br />
  <Btn icon="check" disabled outlined>테스트</Btn>
  <br />
  <br />
  <Btn color="primary" icon="check">테스트</Btn>
  <br />
  <br />
  <Btn color="secondary" icon="check">테스트</Btn>
  <br />
  <br />
  <Btn color="primary">테스트</Btn>
  <br />
  <br />
  <Btn color="danger">테스트</Btn>
  <br />
  <br />
  <Btn color="warn">테스트</Btn>
  <br />
  <br />
  <Btn color="success">테스트</Btn>
  <br />
  <br />
  <Btn color="info">테스트</Btn>
  <br />
  <br />
  <Btn color="help">테스트</Btn>
  <br />
  <br />
  <Btn color="contrast">테스트</Btn>
  <br />
  <br />
</template>
