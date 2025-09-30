<script setup>
import { ref, onMounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { useAppToast } from '@/composables/useAppToast';
import axios from 'axios';

const { toast } = useAppToast();
const router = useRouter();
const userStore = useUserStore();

const email = ref('');
const password = ref('');
const capsLockOn = ref(false);
const emailInput = ref(null);
const isDisabled = ref(false);

const handleKey = (event) => {
  capsLockOn.value = event.getModifierState && event.getModifierState('CapsLock');
};

const login = async () => {
  isDisabled.value = true;

  if (!email.value || !password.value) {
    isDisabled.value = false;
    return toast("warn", "로그인 실패", "이메일과 비밀번호를 모두 입력해주세요.");
  }

  try {
    const response = await axios.post('/api/auth/devlogin', {
      email: email.value.trim(),
      password: password.value
    }, { withCredentials: true });

    if (response.status === 200 && response.data?.accessToken) {
      const meRes = await axios.get('/api/auth/me', { withCredentials: true });
      const user = meRes.data;

      toast('success', '로그인 성공', `관리자 계정으로 로그인되었습니다.`);

      userStore.setUserInfo(user);

      if (user.tempPassword === 'Y') {
        router.push('/change-password');
      } else {
        router.push('/');
      }
    } else {
      toast('error', '로그인 실패', '로그인 정보를 확인할 수 없습니다.');
    }

  } catch (err) {
    const msg = err.response?.data?.message || '서버 오류가 발생했습니다.';
    toast('error', '로그인 실패', msg);
    password.value = '';
  } finally {
    isDisabled.value = false;
  }
};

onMounted(() => {
  nextTick(() => {
    emailInput.value?.querySelector('input')?.focus();
  });
});
</script>

<template>
  <div class="flex items-center justify-center min-h-screen min-w-full bg-white sm:bg-surface-50 dark:bg-surface-900 sm:dark:bg-surface-950">
    <div class="flex flex-col items-center justify-center w-full h-screen sm:w-[36rem] sm:h-auto bg-surface-0 dark:bg-surface-900 py-10 px-12 sm:py-20 sm:px-20 rounded-none sm:rounded-lg shadow-[0_6px_60px_rgba(0,0,0,0.02)]">

      <form @submit.prevent="login" class="w-full max-w-md">
        <div class="mb-4">
            <i class="pi pi-user text-primary p-1" style="font-size: 2.5rem;"></i>
        </div>
        <div class="text-left mb-10">
          <div class="flex items-center text-surface-900 dark:text-surface-0 text-3xl font-semibold mb-4">
            관리자 로그인
          </div>
          <span class="text-muted-color font-medium">2조 SCM</span>
        </div>

        <label for="email" class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-3">
          이메일
        </label>
        <div ref="emailInput">
          <InputText
            id="email"
            type="email"
            placeholder="이메일 주소를 입력해주세요"
            class="w-full mb-6 h-12 px-4 rounded-md text-lg"
            v-model="email"
            autocomplete="email"
            :disabled="isDisabled"
          />
        </div>

        <label for="password" class="block text-surface-900 dark:text-surface-0 font-medium text-xl mb-3">
          비밀번호
        </label>
        <Password
          id="password"
          v-model="password"
          placeholder="비밀번호를 입력해주세요"
          :toggleMask="true"
          class="mb-2 w-full rounded-md text-lg"
          fluid
          :feedback="false"
          @keydown="handleKey"
          autocomplete="current-password"
          :disabled="isDisabled"
        />

        <p v-if="capsLockOn" class="text-red-500 text-sm mt-1 mb-4">
          현재 Caps Lock이 켜져 있습니다.
        </p>

        <div class="flex items-center justify-between mt-2 mb-8 gap-8">
          <span class="font-medium no-underline ml-2 text-right cursor-pointer text-primary hidden">
            비밀번호를 잊으셨나요?
          </span>
        </div>

        <Button
          :label="isDisabled ? '로그인 중...' : '로그인하기'"
          type="submit"
          class="w-full h-14 rounded-md text-lg"
          :loading="isDisabled"
          :disabled="isDisabled"
        />
      </form>

    </div>
  </div>
</template>

