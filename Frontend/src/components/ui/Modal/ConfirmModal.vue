<script setup lang="ts">
  import BaseModal from './BaseModal.vue';
  import BasicButton from '../Button/BasicButton.vue';

  interface ConfirmModalProps {
  modelValue: boolean;
  title?: string;
  content?: string;
  icon?: string;
  confirmText?: string;
  cancelText?: string;
}

const confirmModalProps = withDefaults(defineProps<ConfirmModalProps>(), {
  title: '',
  content: '',
  icon: 'help',
  confirmText: '확인',
  cancelText: '취소'
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
    <BaseModal
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="title"
    :icon="icon"
  >
    <template #default>
      {{ content }}
    </template>

    <template #footer>
      <BasicButton
        size="M"
        :is-enabled="false"
        :text="cancelText"
        @click="$emit('update:modelValue', false)"
      />
      <BasicButton
        size="M"
        :text="confirmText"
        @click="handleConfirm"
      />
    </template>
  </BaseModal>
</template>
