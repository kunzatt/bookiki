<script setup lang="ts">
  import BasicButton from '../Button/BasicButton.vue';

  interface BaseModalProps {
  modelValue: boolean;
  title?: string;
  content?: string;
  icon?: string;
  confirmText?: string;
}

withDefaults(defineProps<BaseModalProps>(), {
  title: '',
  content: '',
  icon: 'info',
  confirmText: '확인'
});

defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
}>();
</script>

<template>
    <Transition
    enter-active-class="transition duration-300 ease-out"
    enter-from-class="opacity-0"
    enter-to-class="opacity-100"
    leave-active-class="transition duration-200 ease-in"
    leave-from-class="opacity-100"
    leave-to-class="opacity-0"
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
        <div v-if="title" class="flex justify-center mb-6">
          <span class="material-icons text-[#698469] text-4xl">
            {{ icon }}
          </span>
        </div>
        
        <h2 v-if="title" class="text-xl text-center font-medium mb-8">
          {{ title }}
        </h2>

        <!-- Modal Body -->
        <div class="text-center mb-8">
          <slot>{{ content }}</slot>
        </div>

        <!-- Modal Footer -->
        <div class="flex justify-center gap-3">
          <slot name="footer">
            <BasicButton 
              size="M"
              :text="confirmText"
              @click="$emit('update:modelValue', false)"
            />
          </slot>
        </div>
      </div>
    </div>
  </Transition>
</template>
