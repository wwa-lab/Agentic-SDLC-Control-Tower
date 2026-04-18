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
  <section class="tm-card section-high">
    <header class="tm-card__header">
      <div>
        <p class="text-label">{{ title }}</p>
        <p v-if="subtitle" class="tm-card__subtitle">{{ subtitle }}</p>
      </div>
      <div class="tm-card__actions">
        <slot name="actions" />
      </div>
    </header>

    <div v-if="loading" class="tm-card__body tm-card__loading">
      <div class="tm-skeleton tm-skeleton--short"></div>
      <div class="tm-skeleton"></div>
      <div class="tm-skeleton tm-skeleton--medium"></div>
    </div>

    <div v-else-if="error" class="tm-card__body tm-card__error">
      <p class="text-body-sm">{{ error }}</p>
      <button class="btn-machined" @click="$emit('retry')">Retry</button>
    </div>

    <div v-else-if="empty" class="tm-card__body tm-card__empty">
      <p class="text-body-sm">{{ emptyMessage }}</p>
    </div>

    <div v-else class="tm-card__body">
      <slot />
    </div>
  </section>
</template>

<style scoped>
.tm-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 220px;
  padding: 18px;
  border-radius: var(--radius-md);
}

.tm-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.tm-card__subtitle {
  margin-top: 4px;
  font-size: 0.8125rem;
  color: var(--color-on-surface-variant);
}

.tm-card__actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.tm-card__body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
}

.tm-card__loading,
.tm-card__error,
.tm-card__empty {
  justify-content: center;
}

.tm-skeleton {
  height: 12px;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(137, 206, 255, 0.05), rgba(137, 206, 255, 0.24), rgba(137, 206, 255, 0.05));
  background-size: 200% 100%;
  animation: tm-shimmer 1.3s linear infinite;
}

.tm-skeleton--short {
  width: 35%;
}

.tm-skeleton--medium {
  width: 68%;
}

@keyframes tm-shimmer {
  from { background-position: 200% 0; }
  to { background-position: -200% 0; }
}
</style>
