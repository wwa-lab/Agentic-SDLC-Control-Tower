<script setup lang="ts">
interface Props {
  branch: string;
  isDefault?: boolean;
}

withDefaults(defineProps<Props>(), {
  isDefault: false,
});
</script>

<template>
  <span class="branch-pill">
    <span class="branch-pill__name">{{ branch }}</span>
    <span v-if="isDefault" class="branch-pill__marker" aria-label="Default branch">default</span>
  </span>
</template>

<style scoped>
.branch-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  border-radius: 999px;
  background: rgba(137, 206, 255, 0.1);
  color: var(--color-secondary);
  font-size: 0.75rem;
}

.branch-pill__name {
  font-family: var(--font-tech);
}

.branch-pill__marker {
  color: var(--color-on-surface-variant);
  font-size: 0.65rem;
  text-transform: uppercase;
}
</style>

