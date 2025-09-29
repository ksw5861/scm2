<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue';
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

const loadRecaptchaScript = () => {
  if (!document.getElementById('recaptcha-script')) {
    const script = document.createElement('script');
    script.id = 'recaptcha-script';
    script.src = "https://www.google.com/recaptcha/api.js?render=6Lc0RdgrAAAAALppPI0JOpsn5neddYJ9okesIWPn";
    script.async = true;
    document.body.appendChild(script);
  }
};

const getRecaptchaToken = async () => {
  while (!window.grecaptcha) {
    await new Promise(resolve => setTimeout(resolve, 100));
  }

  try {
    await window.grecaptcha.ready(async () => {});
    const token = await window.grecaptcha.execute(
      '6Lc0RdgrAAAAALppPI0JOpsn5neddYJ9okesIWPn',
      { action: 'login' }
    );
    return token;
  } catch (err) {
    console.error('reCAPTCHA 토큰 발급 실패:', err);
    throw err;
  }
};

const login = async () => {
  isDisabled.value = true;

  if (!email.value || !password.value) {
    isDisabled.value = false;
    return toast("warn", "로그인 실패", "이메일과 비밀번호를 모두 입력해주세요.");
  }

  try {
    const token = await getRecaptchaToken();

    if (!token) {
      isDisabled.value = false;
      return toast("warn", "로그인 실패", "reCAPTCHA 토큰 발급에 실패했습니다. 새로고침 후 다시 시도해주세요.");
    }

    const param = {
      email: email.value.trim(),
      password: password.value,
      recaptcha: token
    };
    console.log("서버 전송 데이터:", param);

    const result = await axios.post('/api/auth', param, { withCredentials: true });

    if (result.status === 200) {
      try {
        const { data } = await axios.get('/api/auth/me', { withCredentials: true });
        userStore.setUserInfo(data);
        console.log('사용자 정보:', data);
        toast("success", "로그인 성공", `${data.name}님 환영합니다!`);

        if (data.tempPassword === 'Y') {
          router.push('/change-password');
        } else {
          router.push('/');
        }
      } catch (e) {
        console.error('사용자 정보 불러오기 실패:', e);
        toast("success", "로그인 성공", `환영합니다.!`);
        router.push('/');
      }
    }
  } catch (e) {
    if (e.response) {
      console.error('서버 응답 에러:', e.response);

      switch (e.response.status) {
        case 400:
          toast("error", "보안 경고", "reCAPTCHA 검증 실패. 새로고침 후 다시 시도해주세요.");
          break;
        case 401:
          toast("error", "로그인 실패", "이메일 또는 비밀번호가 올바르지 않습니다.");
          break;
        default:
          toast("error", "로그인 실패", "알 수 없는 서버 오류가 발생했습니다.");
      }
    } else {
      console.error('네트워크 오류:', e);
      toast("error", "로그인 실패", "서버에 연결할 수 없습니다. 네트워크를 확인해주세요.");
    }

    password.value = '';
    isDisabled.value = false;
  }
};

onMounted(() => {
  nextTick(() => {
    emailInput.value?.querySelector('input')?.focus();
  });
  loadRecaptchaScript();
});

onBeforeUnmount(() => {
  const script = document.getElementById('recaptcha-script');
  if (script) {
    script.remove();
  }

  const badge = document.querySelector('.grecaptcha-badge');
  if (badge) {
    badge.remove();
  }
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
          label="로그인하기"
          type="submit"
          class="w-full h-14 rounded-md text-lg"
          :disabled="isDisabled"
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
