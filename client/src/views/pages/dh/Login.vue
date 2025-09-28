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

const handleKey = (event) => {
  capsLockOn.value = event.getModifierState && event.getModifierState('CapsLock');
};

const login = async () => {
  if (!email.value || !password.value) return toast("warn", "로그인 실패", "이메일과 비밀번호를 모두 입력해주세요.");

  const param = {
    email: email.value,
    password: password.value,
  };

  try {
    const result = await axios.post('/api/auth', param, { withCredentials: true });
    if (result.status === 200) {
      try {
        const { data } = await axios.get('/api/auth/me', { withCredentials: true });
        userStore.setUserInfo(data);
        console.log('사용자 정보:', userStore.name, userStore.code, userStore.accountId, userStore.role, userStore.isLoggedIn);
        toast("success", "로그인 성공", `${data.name}님 환영합니다!`);
      } catch (e) {
        console.error('사용자 정보 불러오기 실패:', e);
        toast("success", "로그인 성공", `환영합니다.!`);
      }
      router.push('/');
    } else {
      toast("error", "로그인 실패", "이메일과 비밀번호를 확인해주세요.");
    }
  } catch (e) {
    toast("error", "로그인 실패", "이메일과 비밀번호를 확인해주세요.");
    console.error('로그인 오류:', e);
  } finally {
    password.value = '';
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
    <div
      class="flex flex-col items-center justify-center w-full h-screen sm:w-[36rem] sm:h-auto bg-surface-0 dark:bg-surface-900 py-10 px-12 sm:py-20 sm:px-20 rounded-none sm:rounded-lg shadow-[0_6px_60px_rgba(0,0,0,0.02)]"
    >
      <form @submit.prevent="login" class="w-full max-w-md">
        <div class="text-left mb-10">
          <div class="text-surface-900 dark:text-surface-0 text-3xl font-semibold mb-4">로그인</div>
          <span class="text-muted-color font-medium">2조 SCM</span>
        </div>

        <label for="email" class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-3">
          이메일
        </label>
        <div ref="emailInput">
          <InputText
            id="email"
            type="email"
            placeholder="이메일 주소 입력"
            class="w-full mb-6 h-12 px-4 rounded-md text-lg"
            v-model="email"
            autocomplete="email"
          />
        </div>

        <label for="password" class="block text-surface-900 dark:text-surface-0 font-medium text-xl mb-3">
          비밀번호
        </label>
        <Password
          id="password"
          v-model="password"
          placeholder="비밀번호 입력"
          :toggleMask="true"
          class="mb-2 w-full rounded-md text-lg"
          fluid
          :feedback="false"
          @keydown="handleKey"
          autocomplete="current-password"
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
          label="로그인하기"
          type="submit"
          class="w-full h-14 rounded-md text-lg"
        />
      </form>
    </div>
  </div>
</template>

<style scoped>
.pi-eye {
  transform: scale(1.6);
  margin-right: 1rem;
}

.pi-eye-slash {
  transform: scale(1.6);
  margin-right: 1rem;
}
</style>