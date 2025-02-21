<script setup lang="ts">
import { ref } from 'vue';
import BasicButton from '../Button/BasicButton.vue';

interface InputProps {
  /**
   * Input type variant
   * - 'withButton': Input with small button (Type A)
   * - 'underline': Underline style input (Type B)
   * - 'full': Full width box input (Type C)
   * - 'password': Password input with toggle (Type D)
   */
  type?: 'withButton' | 'underline' | 'full' | 'password';
  /**
   * Input type (text, email, password, etc)
   */
  inputType?: string;
  /**
   * Model value for v-model binding
   */
  modelValue: string;
  /**
   * Placeholder text
   */
  placeholder?: string;
  /**
   * Label text
   */
  label?: string;
  /**
   * Button text (for type 'withButton')
   */
  buttonText?: string;
  width?: 'full' | 'auto' | 'fixed';
  disabled?: boolean;
  buttonDisabled?: boolean;
}

const props = withDefaults(defineProps<InputProps>(), {
  type: 'full',
  inputType: 'text',
  placeholder: '',
  label: '',
  buttonText: '확인',
  width: 'auto',
  disabled: false,
  buttonDisabled: false,
});

const widthClasses = {
  full: 'w-full',
  auto: 'w-auto',
  fixed: 'w-96', // 또는 원하는 고정 너비
} as const;

defineEmits<{
  (e: 'update:modelValue', value: string): void;
  (e: 'buttonClick'): void;
}>();

// Password visibility toggle
const showPassword = ref(false);
const togglePassword = () => {
  showPassword.value = !showPassword.value;
};
</script>

<template>
  <div :class="widthClasses[width]">
    <!-- Label -->
    <label v-if="label" class="block text-xs md:text-sm text-gray-500 mb-1">{{ label }}</label>

    <!-- Type A: Input with button -->
    <div v-if="type === 'withButton'" class="flex gap-2">
      <div class="flex-1">
        <input
          :type="inputType"
          :value="modelValue"
          :placeholder="placeholder"
          :disabled="disabled"
          class="w-full px-3 md:px-4 h-10 md:h-12 rounded-lg border border-gray-300 focus:border-[#698469] transition-colors duration-200 text-sm md:text-base"
          @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
        />
      </div>
      <BasicButton
        size="S"
        :text="buttonText"
        :disabled="buttonDisabled"
        @click="$emit('buttonClick')"
      />
    </div>

    <!-- Type B: Underline -->
    <div v-else-if="type === 'underline'" class="w-full">
      <input
        :type="inputType"
        :value="modelValue"
        :placeholder="placeholder"
        class="w-full px-3 md:px-4 h-10 md:h-12 border-b border-[#698469] focus:border-b-2 bg-transparent transition-colors duration-200 text-sm md:text-base"
        @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
      />
    </div>

    <!-- Type C: Full width box -->
    <div v-else-if="type === 'full'" class="w-full">
      <input
        :type="inputType"
        :value="modelValue"
        :placeholder="placeholder"
        class="w-full px-3 md:px-4 h-10 md:h-12 rounded-lg border border-gray-300 focus:border-[#698469] transition-colors duration-200 text-sm md:text-base"
        @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
      />
    </div>

    <!-- Type D: Password input -->
    <div v-else-if="type === 'password'" class="w-full relative">
      <input
        :type="showPassword ? 'text' : 'password'"
        :value="modelValue"
        :placeholder="placeholder"
        class="w-full px-3 md:px-4 h-10 md:h-12 rounded-lg border border-gray-300 focus:border-[#698469] transition-colors duration-200 pr-10 text-sm md:text-base"
        @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
      />
      <button
        type="button"
        class="absolute right-3 top-1/2 -translate-y-1/2"
        @click="togglePassword"
      >
        <span class="material-icons text-gray-500" style="font-size: 18px md:text-[20px]">
          {{ showPassword ? 'visibility_off' : 'visibility' }}
        </span>
      </button>
    </div>
  </div>
</template>
