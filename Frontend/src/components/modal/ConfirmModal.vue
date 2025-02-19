<template>
  <Transition name="modal">
    <div v-if="isOpen" class="fixed inset-0 z-[9999] overflow-y-auto">
      <!-- 배경 오버레이 -->
      <div class="fixed inset-0 bg-black bg-opacity-50 transition-opacity"></div>
      
      <!-- 모달 컨테이너 -->
      <div class="fixed inset-0 z-[10000] flex items-center justify-center p-4">
        <div class="relative w-full max-w-md transform overflow-hidden rounded-2xl bg-white shadow-xl transition-all">
          <div class="bg-white px-6 py-6">
            <div class="flex flex-col items-center text-center">
              <div class="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-[#EAEFEA]">
                <span class="material-icons text-[#698469]">{{ icon }}</span>
              </div>
              <h3 class="mt-4 text-xl font-semibold leading-6 text-gray-900">{{ message }}</h3>
            </div>
          </div>
          <div class="bg-gray-50 px-6 py-4 flex flex-row-reverse gap-3">
            <button
              @click="onConfirm"
              class="inline-flex justify-center rounded-lg px-8 py-2.5 text-sm font-medium text-white bg-[#698469] hover:bg-[#4F634F] transition-colors duration-200"
            >
              예
            </button>
            <button
              @click="onCancel"
              class="inline-flex justify-center rounded-lg px-8 py-2.5 text-sm font-medium text-[#698469] bg-[#F8F9F8] border border-[#698469] hover:bg-[#EAEFEA] transition-colors duration-200"
            >
              아니오
            </button>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
defineProps<{
  isOpen: boolean;
  message: string;
  icon: string;
}>();

const emit = defineEmits<{
  (e: 'confirm'): void;
  (e: 'cancel'): void;
}>();

const onConfirm = () => {
  emit('confirm');
};

const onCancel = () => {
  emit('cancel');
};
</script>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active .relative {
  transition: transform 0.2s ease;
}

.modal-enter-from .relative {
  transform: scale(0.95);
}

.modal-leave-to .relative {
  transform: scale(0.95);
}
</style>
