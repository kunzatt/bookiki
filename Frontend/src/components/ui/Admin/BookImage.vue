<script setup lang="ts">
defineProps<{
  image: string;
  title: string;
}>();
</script>

<template>
  <div class="bg-white rounded-lg shadow">
    <img :src="image" :alt="title" class="w-full h-full object-contain rounded-lg p-4" />
  </div>
</template>
