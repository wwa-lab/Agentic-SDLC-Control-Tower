<script setup lang="ts">
defineProps<{
  nextCursor: string | null;
  total: number | null;
  loadedCount: number;
}>();

const emit = defineEmits<{
  loadMore: [];
}>();
</script>

<template>
  <div v-if="nextCursor" class="cursor-pager">
    <span v-if="total != null" class="count">{{ loadedCount }} of {{ total }}</span>
    <button class="btn-machined" @click="emit('loadMore')">Load more</button>
  </div>
  <div v-else-if="total != null && total > 0" class="cursor-pager">
    <span class="count">{{ loadedCount }} of {{ total }}</span>
  </div>
</template>

<style scoped>
.cursor-pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 12px 0;
}
.count { font-size: 12px; color: var(--color-on-surface-variant, #999); }
</style>
