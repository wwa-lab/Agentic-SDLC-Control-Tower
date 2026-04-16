<script setup lang="ts">
interface Props {
  title: string;
  loading?: boolean;
  error?: string | null;
  empty?: boolean;
  emptyMessage?: string;
}

withDefaults(defineProps<Props>(), {
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
  <section class="team-card section-high">
    <header class="team-card__header">
      <div class="team-card__title">
        <slot name="icon" />
        <span class="text-label">{{ title }}</span>
      </div>
      <div class="team-card__actions">
        <slot name="actions" />
      </div>
    </header>

    <div v-if="loading" class="team-card__body team-card__loading">
      <div class="skeleton-line w-40"></div>
      <div class="skeleton-line w-85"></div>
      <div class="skeleton-line w-65"></div>
    </div>

    <div v-else-if="error" class="team-card__body team-card__error">
      <p class="error-copy">{{ error }}</p>
      <button class="btn-machined btn-retry" @click="$emit('retry')">Retry</button>
    </div>

    <div v-else-if="empty" class="team-card__body team-card__empty">
      <p class="text-body-sm">{{ emptyMessage }}</p>
    </div>

    <div v-else class="team-card__body">
      <slot />
    </div>
  </section>
</template>

<style scoped>
.team-card {
  border-radius: var(--radius-sm);
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 220px;
}

.team-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.team-card__title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.team-card__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.team-card__body {
  display: flex;
  flex-direction: column;
  gap: 14px;
  flex: 1;
}

.team-card__loading,
.team-card__error,
.team-card__empty {
  justify-content: center;
  min-height: 150px;
}

.skeleton-line {
  height: 10px;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(137, 206, 255, 0.05), rgba(137, 206, 255, 0.18), rgba(137, 206, 255, 0.05));
  background-size: 200% 100%;
  animation: shimmer 1.4s linear infinite;
}

.w-40 { width: 40%; }
.w-65 { width: 65%; }
.w-85 { width: 85%; }

.error-copy {
  color: var(--color-incident-crimson);
  font-size: 0.8125rem;
  line-height: 1.5;
}

.btn-retry {
  align-self: flex-start;
}

@keyframes shimmer {
  from { background-position: 200% 0; }
  to { background-position: -200% 0; }
}
</style>
