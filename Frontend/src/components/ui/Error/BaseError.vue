<script setup lang="ts">
interface ErrorProps {
  modelValue: boolean;
  title?: string;
  content?: string;
  confirmText?: string;
}

withDefaults(defineProps<ErrorProps>(), {
  title: '오류가 발생했습니다',
  content: '',
  confirmText: '확인'
});

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
  (e: 'confirm'): void;
}>();

const handleConfirm = () => {
  emit('confirm');
  emit('update:modelValue', false);
};
</script>

<template>
  <Transition
    enter-active-class="transition duration-300 ease-out"
    enter-from-class="transform scale-95 opacity-0"
    enter-to-class="transform scale-100 opacity-100"
    leave-active-class="transition duration-200 ease-in"
    leave-from-class="transform scale-100 opacity-100"
    leave-to-class="transform scale-95 opacity-0"
  >
    <div
      v-if="modelValue"
      class="fixed inset-0 flex items-center justify-center z-50"
    >
      <!-- Backdrop -->
      <div 
        class="absolute inset-0 bg-black bg-opacity-40"
        @click="$emit('update:modelValue', false)"
      />
      
      <!-- Modal Content -->
      <div class="relative bg-white rounded-2xl p-8 max-w-md w-full mx-4 z-10">
        <!-- Error Icon -->
        <div class="flex justify-center mb-6">
          <span class="material-icons text-red-500 text-4xl">
            error_outline
          </span>
        </div>
        
        <!-- Title -->
        <h2 v-if="title" class="text-xl text-center font-medium mb-4 text-gray-800">
          {{ title }}
        </h2>

        <!-- Content -->
        <div class="text-center mb-8 text-gray-600">
          <slot>{{ content }}</slot>
        </div>

        <!-- Footer -->
        <div class="flex justify-center gap-3">
          <slot name="footer">
            <button 
              class="px-6 py-2.5 bg-red-500 hover:bg-red-600 text-white rounded-lg text-sm transition-colors"
              @click="handleConfirm"
            >
              {{ confirmText }}
            </button>
          </slot>
        </div>
      </div>
    </div>
  </Transition>
</template>