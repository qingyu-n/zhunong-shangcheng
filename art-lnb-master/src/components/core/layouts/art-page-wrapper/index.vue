<template>
  <div class="art-page-wrapper">
    <slot></slot>
  </div>
</template>

<script setup lang="ts">
  defineOptions({
    name: 'ArtPageWrapper'
  })
</script>

<style scoped lang="scss">
  .art-page-wrapper {
    padding: 20px;
    min-height: calc(100vh - 100px);
  }
</style>
