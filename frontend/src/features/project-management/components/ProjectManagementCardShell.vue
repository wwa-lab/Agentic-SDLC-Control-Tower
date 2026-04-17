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
  <section class="pm-card" :class="{ 'pm-card--full': fullWidth }">
    <header class="pm-card__header">
      <div>
        <p class="text-label">{{ title }}</p>
        <slot name="subtitle" />
      </div>
      <div class="pm-card__actions">
        <slot name="actions" />
      </div>
    </header>

    <div v-if="loading" class="pm-card__state pm-card__state--loading">
      <div class="pm-card__skeleton pm-card__skeleton--short"></div>
      <div class="pm-card__skeleton"></div>
      <div class="pm-card__skeleton pm-card__skeleton--medium"></div>
    </div>

    <div v-else-if="error" class="pm-card__state pm-card__state--error">
      <p class="pm-card__message">{{ error }}</p>
      <button class="btn-machined" @click="$emit('retry')">Retry</button>
    </div>

    <div v-else-if="empty" class="pm-card__state pm-card__state--empty">
      <p class="text-body-sm">{{ emptyMessage }}</p>
    </div>

    <div v-else class="pm-card__body">
      <slot />
    </div>
  </section>
</template>

<style scoped>
.pm-card {
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--color-surface-container-high) 92%, transparent), var(--color-surface-container-low));
  border: var(--border-ghost);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 210px;
}

.pm-card--full {
  grid-column: 1 / -1;
}

.pm-card__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.pm-card__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pm-card__body {
  display: flex;
  flex-direction: column;
  gap: 14px;
  flex: 1;
}

.pm-card__state {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
  min-height: 150px;
}

.pm-card__state--error .pm-card__message {
  color: var(--color-incident-crimson);
}

.pm-card__skeleton {
  height: 10px;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(137, 206, 255, 0.05), rgba(137, 206, 255, 0.16), rgba(137, 206, 255, 0.05));
  background-size: 200% 100%;
  animation: pm-shimmer 1.4s linear infinite;
}

.pm-card__skeleton--short {
  width: 34%;
}

.pm-card__skeleton--medium {
  width: 62%;
}

@keyframes pm-shimmer {
  from { background-position: 200% 0; }
  to { background-position: -200% 0; }
}
</style>
