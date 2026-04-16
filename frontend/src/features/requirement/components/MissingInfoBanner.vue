<script setup lang="ts">
interface Props {
  items: ReadonlyArray<string>;
}

defineProps<Props>();
</script>

<template>
  <div v-if="items.length > 0" class="missing-banner">
    <span class="banner-icon">⚠</span>
    <div class="banner-content">
      <span class="banner-title">Missing Information</span>
      <ul class="banner-list">
        <li v-for="(item, i) in items" :key="i">{{ item }}</li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.missing-banner {
  display: flex;
  gap: 10px;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  background: rgba(245, 158, 11, 0.08);
  border: 1px solid rgba(245, 158, 11, 0.2);
}

.banner-icon {
  font-size: 0.875rem;
  flex-shrink: 0;
}

.banner-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.banner-title {
  font-family: var(--font-ui);
  font-size: 0.625rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--color-approval-amber);
}

.banner-list {
  padding-left: 14px;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.banner-list li {
  font-family: var(--font-ui);
  font-size: 0.6875rem;
  color: var(--color-on-surface);
  line-height: 1.3;
}
</style>
