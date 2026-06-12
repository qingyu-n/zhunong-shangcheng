<template>
  <div class="art-card">
    <div v-if="title" class="art-card-header">
      <span class="art-card-title">{{ title }}</span>
    </div>
    <div class="art-card-body">
      <slot></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({
  name: 'ArtCard'
})

interface Props {
  title?: string
}

withDefaults(defineProps<Props>(), {
  title: ''
})
</script>

<style scoped lang="scss">
.art-card {
  background: var(--el-bg-color);
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

  .art-card-header {
    padding: 16px 20px;
    border-bottom: 1px solid var(--el-border-color-lighter);

    .art-card-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }
  }

  .art-card-body {
    padding: 20px;
  }
}
</style>