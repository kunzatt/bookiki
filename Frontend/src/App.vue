<!-- App.vue -->
<script setup lang="ts">
// App 레벨에서 필요한 설정이나 전역 상태 관리가 있다면 여기에 추가
</script>

<template>
  <div id="app">
    <router-view></router-view>
  </div>
</template>

<style>
/* 전역 스타일이 필요하다면 여기에 추가 */
#app {
  @apply min-h-screen;
}
</style>