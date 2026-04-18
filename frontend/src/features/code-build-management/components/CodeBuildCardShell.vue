<script setup lang="ts">
interface Props {
  title: string;
  subtitle?: string | null;
  loading?: boolean;
  error?: string | null;
  empty?: boolean;
  emptyMessage?: string;
}

withDefaults(defineProps<Props>(), {
  subtitle: null,
  loading: false,
  error: null,
  empty: false,
  emptyMessage: 'No data available for this section.',
});

defineEmits<{
  retry: [];
}>();
</script>

<template>
  <section class="cb-card section-high">
    <header class="cb-card__header">
      <div>
        <p class="text-label">{{ title }}</p>
        <p v-if="subtitle" class="cb-card__subtitle">{{ subtitle }}</p>
      </div>
      <div class="cb-card__actions">
        <slot name="actions" />
      </div>
    </header>

    <div v-if="loading" class="cb-card__body cb-card__loading">
      <div class="cb-skeleton cb-skeleton--short"></div>
      <div class="cb-skeleton"></div>
      <div class="cb-skeleton cb-skeleton--medium"></div>
    </div>

    <div v-else-if="error" class="cb-card__body cb-card__error">
      <p class="text-body-sm">{{ error }}</p>
      <button class="btn-machined" @click="$emit('retry')">Retry</button>
    </div>

    <div v-else-if="empty" class="cb-card__body cb-card__empty">
      <p class="text-body-sm">{{ emptyMessage }}</p>
    </div>

    <div v-else class="cb-card__body">
      <slot />
    </div>
  </section>
</template>

<style scoped>
.cb-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 220px;
  padding: 18px;
  border-radius: var(--radius-md);
}

.cb-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.cb-card__subtitle {
  margin-top: 4px;
  font-size: 0.8125rem;
  color: var(--color-on-surface-variant);
}

.cb-card__actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.cb-card__body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
}

.cb-card__loading,
.cb-card__error,
.cb-card__empty {
  justify-content: center;
}

.cb-skeleton {
  height: 12px;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(137, 206, 255, 0.05), rgba(137, 206, 255, 0.24), rgba(137, 206, 255, 0.05));
  background-size: 200% 100%;
  animation: cb-shimmer 1.3s linear infinite;
}

.cb-skeleton--short {
  width: 35%;
}

.cb-skeleton--medium {
  width: 68%;
}

@keyframes cb-shimmer {
  from { background-position: 200% 0; }
  to { background-position: -200% 0; }
}
</style>

