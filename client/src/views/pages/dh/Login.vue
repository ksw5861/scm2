<script setup>
import { ref } from 'vue';

const email = ref('');
const password = ref('');
</script>

<template>
    <div class="bg-surface-50 dark:bg-surface-950 flex items-center justify-center min-h-screen min-w-[100vw] overflow-hidden">
        <div class="flex flex-col items-center justify-center">
            <div style="padding: 0.3rem; background: rgba(33, 150, 243, 0) 30%">
                <div class="w-full bg-surface-0 dark:bg-surface-900 py-20 px-8 sm:px-20" style="border-radius: 8px">
                    <div class="text-left mb-8">
                        <div class="text-surface-900 dark:text-surface-0 text-3xl font-medium mb-4">로그인</div>
                        <span class="text-muted-color font-medium">2조 SCM</span>
                    </div>

                    <div>
                        <label for="email1" class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-2">이메일</label>
                        <InputText id="email1" type="text" placeholder="이메일 주소 입력" class="w-full md:w-[30rem] mb-8" v-model="email" />

                        <label for="password1" class="block text-surface-900 dark:text-surface-0 font-medium text-xl mb-2">비밀번호</label>
                        <Password id="password1" v-model="password" placeholder="비밀번호 입력" :toggleMask="true" class="mb-4" fluid :feedback="false"></Password>

                        <div class="flex items-center justify-between mt-2 mb-8 gap-8">
                            <div class="flex items-center">
                            </div>
                            <span class="font-medium no-underline ml-2 text-right cursor-pointer text-primary hidden">비밀번호를 잊으셨나요?</span>
                        </div>
                        <Button label="로그인하기" class="w-full" as="router-link" to="/"></Button>
                    </div>
                </div>
            </div>
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
