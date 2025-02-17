<script setup lang="ts">
defineProps<{
  image: string;
  title: string;
}>();
</script>

<template>
  <div class="w-full h-64 bg-white rounded-lg shadow">
    <img :src="image" :alt="title" class="w-full h-full object-contain rounded-lg" />
  </div>
</template>
