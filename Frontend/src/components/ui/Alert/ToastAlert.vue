<script setup lang="ts">
import { ref, watch, onBeforeUnmount } from 'vue';

interface ToastProps {
  isVisible: boolean;
  message: string;
  type?: 'success' | 'info';
  /**
   * Toast가 표시되는 시간 (밀리초)
   * @default 3000
   */
  duration?: number;
}

const toastProps = withDefaults(defineProps<ToastProps>(), {
  type: 'info',
  duration: 3000
});

const localVisible = ref(false);
let timeoutId: number | null = null;

// isVisible prop이 변경될 때마다 타이머 설정
watch(() => toastProps.isVisible, (newValue) => {
  if (newValue) {
    localVisible.value = true;
    
    // 이전 타이머가 있다면 제거
    if (timeoutId !== null) {
      clearTimeout(timeoutId);
    }
    
    // 새로운 타이머 설정
    timeoutId = window.setTimeout(() => {
      localVisible.value = false;
    }, toastProps.duration);
  } else {
    localVisible.value = false;
    if (timeoutId !== null) {
      clearTimeout(timeoutId);
      timeoutId = null;
    }
  }
});

// 컴포넌트가 제거될 때 타이머 정리
onBeforeUnmount(() => {
  if (timeoutId !== null) {
    clearTimeout(timeoutId);
    timeoutId = null;
  }
});
</script>

<template>
  <Transition
    enter-active-class="transition duration-300 ease-out"
    enter-from-class="transform translate-y-2 opacity-0"
    enter-to-class="transform translate-y-0 opacity-100"
    leave-active-class="transition duration-200 ease-in"
    leave-from-class="transform translate-y-0 opacity-100"
    leave-to-class="transform translate-y-2 opacity-0"
  >
    <div
      v-if="localVisible"
      class="fixed bottom-4 left-1/2 transform -translate-x-1/2 z-50"
    >
      <div 
        class="flex items-center px-4 py-3 rounded-lg shadow-lg"
        :class="[
          type === 'success' ? 'bg-[#698469] text-white' : 'bg-white text-[#698469] border border-[#698469]'
        ]"
      >
        <span class="material-icons mr-2">
          {{ type === 'success' ? 'check_circle' : 'info' }}
        </span>
        <p class="text-sm">{{ message }}</p>
      </div>
    </div>
  </Transition>
</template>