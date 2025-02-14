<script setup lang="ts">
interface NavigationMenuProps {
  /**
   * 메뉴 아이템의 제목
   */
  title: string;
  /**
   * 링크 주소 (선택적)
   */
  to?: string;
  /**
   * 강조 표시 여부
   * @default false
   */
  isHighlighted?: boolean;
}

const props = withDefaults(defineProps<NavigationMenuProps>(), {
  to: '',
  isHighlighted: false
});
</script>

<template>
  <component 
    :is="to ? 'router-link' : 'div'"
    :to="to"
    :class="[
      'flex items-center justify-between py-4 cursor-pointer',
      isHighlighted ? 'text-[#FF6B6B]' : 'text-gray-900',
      to && 'hover:bg-gray-50'
    ]"
  >
    <span class="text-base">{{ title }}</span>
    <i class="material-icons text-gray-400">chevron_right</i>
  </component>
</template>