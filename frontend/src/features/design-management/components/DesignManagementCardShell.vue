<script setup lang="ts">
interface Props {
  title: string;
  loading?: boolean;
  error?: string | null;
  empty?: boolean;
  emptyMessage?: string;
  fullWidth?: boolean;
}

withDefaults(defineProps<Props>(), {
  loading: false,
  error: null,
  empty: false,
  emptyMessage: 'No data available for this section.',
  fullWidth: false,
});

defineEmits<{
  retry: [];
}>();
</script>

<template>
  <section class="dm-card" :class="{ 'dm-card--full': fullWidth }">
    <header class="dm-card__header">
      <div>
        <p class="text-label">{{ title }}</p>
        <slot name="subtitle" />
      </div>
      <div class="dm-card__actions">
        <slot name="actions" />
      </div>
    </header>

    <div v-if="loading" class="dm-card__state dm-card__state--loading">
      <div class="dm-card__skeleton dm-card__skeleton--short"></div>
      <div class="dm-card__skeleton"></div>
      <div class="dm-card__skeleton dm-card__skeleton--medium"></div>
    </div>

    <div v-else-if="error" class="dm-card__state dm-card__state--error">
      <p class="text-body-sm">{{ error }}</p>
      <button class="btn-machined" @click="$emit('retry')">Retry</button>
    </div>

    <div v-else-if="empty" class="dm-card__state dm-card__state--empty">
      <p class="text-body-sm">{{ emptyMessage }}</p>
    </div>

    <div v-else class="dm-card__body">
      <slot />
    </div>
  </section>
</template>

<style scoped>
.dm-card {
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--color-surface-container-high) 90%, transparent), var(--color-surface-container-low));
  border: var(--border-ghost);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 180px;
}

.dm-card--full {
  grid-column: 1 / -1;
}

.dm-card__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.dm-card__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dm-card__body,
.dm-card__state {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
}

.dm-card__skeleton {
  height: 10px;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(137, 206, 255, 0.05), rgba(137, 206, 255, 0.16), rgba(137, 206, 255, 0.05));
  background-size: 200% 100%;
  animation: dm-shimmer 1.4s linear infinite;
}

.dm-card__skeleton--short {
  width: 30%;
}

.dm-card__skeleton--medium {
  width: 56%;
}

@keyframes dm-shimmer {
  from { background-position: 200% 0; }
  to { background-position: -200% 0; }
}
</style>
