<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { useAppToast } from '@/composables/useAppToast';
import axios from 'axios';

const router = useRouter();
const { toast } = useAppToast();
const userStore = useUserStore();

const newPassword = ref('');
const confirmPassword = ref('');
const isSubmitting = ref(false);
const capsLockOn = ref(false);

const isValidPassword = (password) => {
  const regex = /^(?=.*[0-9])(?=.*[!@#$%^&*()\-_=+\[\]{};:'",.<>/?\\|`~]).{8,}$/;
  return regex.test(password);
};

const handleKey = (event) => {
  const capsLock = event.getModifierState && event.getModifierState('CapsLock');
  capsLockOn.value = capsLock;
};

const changePassword = async () => {
  if (!newPassword.value || !confirmPassword.value) {
    return toast("warn", "비밀번호 변경 실패", "모든 필드를 입력해주세요.");
  }

  if (newPassword.value !== confirmPassword.value) {
    return toast("warn", "비밀번호 변경 실패", "비밀번호가 일치하지 않습니다.");
  }

  if (!isValidPassword(newPassword.value)) {
    return toast("warn", "비밀번호 변경 실패", "비밀번호는 8자 이상, 숫자와 특수문자를 포함해야 합니다.");
  }

  isSubmitting.value = true;

  try {
    const result = await axios.post(
                            '/api/auth/change-password',
                            {newPassword: newPassword.value},
                            {withCredentials: true}
                        );
    if (result.status === 200) {
      try {
        const { data } = await axios.get('/api/auth/me', { withCredentials: true });
        console.log('auth/me 응답 데이터:', data);
        userStore.setUserInfo(data);
      } catch (e) {
        console.warn('사용자 정보 갱신 실패:', e);
      }

      toast("success", "비밀번호 변경 완료", "비밀번호가 성공적으로 변경되었습니다.");
      router.push('/');
    }

  } catch (e) {
    console.error('비밀번호 변경 오류:', e);
    toast("error", "비밀번호 변경 실패", "서버 오류로 인해 변경에 실패했습니다.");
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<template>
  <div class="flex items-center justify-center min-h-screen min-w-full bg-white sm:bg-surface-50 dark:bg-surface-900 sm:dark:bg-surface-950">
    <div
      class="flex flex-col items-center justify-center w-full h-screen sm:w-[36rem] sm:h-auto bg-surface-0 dark:bg-surface-900 py-10 px-12 sm:py-20 sm:px-20 rounded-none sm:rounded-lg shadow-[0_6px_60px_rgba(0,0,0,0.02)]"
    >
      <form @submit.prevent="changePassword" class="w-full max-w-md">
        <div class="mb-4">
            <i class="pi pi-key text-primary p-1" style="font-size: 2.5rem;"></i>
        </div>
        <div class="text-left mb-10">
          <div class="text-surface-900 dark:text-surface-0 text-3xl font-semibold mb-4">
            비밀번호 변경
          </div>
        </div>

        <p v-if="capsLockOn" class="text-red-500 text-sm mt-1 mb-4">
          현재 Caps Lock이 켜져 있습니다.
        </p>

        <label for="newPassword" class="block text-surface-900 dark:text-surface-0 font-medium text-xl mb-3">
          새 비밀번호
        </label>
        <Password
          id="newPassword"
          v-model="newPassword"
          placeholder="새 비밀번호를 입력해주세요"
          :toggleMask="true"
          class="mb-6 w-full rounded-md text-lg"
          fluid
          :feedback="false"
          @keydown="handleKey"
          autocomplete="new-password"
          :disabled="isSubmitting"
        />

        <label for="confirmPassword" class="block text-surface-900 dark:text-surface-0 font-medium text-xl mb-3">
          새 비밀번호 확인
        </label>
        <Password
          id="confirmPassword"
          v-model="confirmPassword"
          placeholder="새 비밀번호를 다시 입력해주세요"
          :toggleMask="true"
          class="mb-8 w-full rounded-md text-lg"
          fluid
          :feedback="false"
          @keydown="handleKey"
          autocomplete="new-password"
          :disabled="isSubmitting"
        />

        <Button
          label="비밀번호 변경하기"
          type="submit"
          class="w-full h-14 rounded-md text-lg"
          :disabled="isSubmitting"
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
