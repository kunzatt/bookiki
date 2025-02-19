import { defineStore } from 'pinia';
import { ref } from 'vue';

export type ModalType = 'logout' | 'deleteProfileImage';

interface ModalState {
  isOpen: boolean;
  type: ModalType | null;
}

export const useModalStore = defineStore('modal', () => {
  const modalState = ref<ModalState>({
    isOpen: false,
    type: null,
  });

  const getModalConfig = (type: ModalType) => {
    switch (type) {
      case 'logout':
        return {
          message: '로그아웃 하시겠습니까?',
          icon: 'logout',
        };
      case 'deleteProfileImage':
        return {
          message: '프로필 사진을 삭제하시겠습니까?',
          icon: 'person_remove',
        };
      default:
        return {
          message: '',
          icon: 'help',
        };
    }
  };

  const openModal = (type: ModalType) => {
    modalState.value = {
      isOpen: true,
      type,
    };
  };

  const closeModal = () => {
    modalState.value = {
      isOpen: false,
      type: null,
    };
  };

  return {
    modalState,
    getModalConfig,
    openModal,
    closeModal,
  };
});
