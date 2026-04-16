<script setup lang="ts">
interface Props {
  title: string;
  isLoading?: boolean;
  error?: string | null;
  fullWidth?: boolean;
}

defineProps<Props>();
const emit = defineEmits(['retry']);
</script>

<template>
  <div class="incident-card" :class="{ 'card--full-width': fullWidth }">
    <header v-if="title">
      <span class="card-title">{{ title }}</span>
      <div v-if="isLoading" class="skeleton-pulse"></div>
    </header>
    <div class="card-content">
      <div v-if="error" class="card-error">
        <span class="error-text">{{ error }}</span>
        <button class="retry-btn" @click="emit('retry')">Retry</button>
      </div>
      <div v-else-if="isLoading" class="card-loading">
        <slot name="loading"><div class="skeleton-content"></div></slot>
      </div>
      <slot v-else></slot>
    </div>
  </div>
</template>

<style scoped>
.incident-card {
  background: var(--color-surface-container-high);
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  box-shadow: var(--shadow-card);
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 100px;
}

.card--full-width { grid-column: 1 / -1; }

header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-family: var(--font-ui);
  font-size: 0.625rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--color-on-surface-variant);
}

.card-content { flex-grow: 1; display: flex; flex-direction: column; }

.card-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 100%;
  color: var(--color-on-surface-variant);
  opacity: 0.6;
}

.error-text { font-size: 0.75rem; }

.retry-btn {
  background: var(--color-surface-container-highest);
  border: 1px solid var(--color-secondary);
  color: var(--color-secondary);
  padding: 4px 12px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-family: var(--font-ui);
  font-size: 0.625rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.retry-btn:hover { background: var(--color-secondary); color: var(--color-on-secondary-container); }

.skeleton-pulse {
  width: 40px; height: 4px;
  background: var(--color-surface-container-highest);
  border-radius: 2px;
  animation: pulse 1.5s infinite ease-in-out;
}

.skeleton-content {
  height: 60px;
  background: var(--color-surface-container-low);
  border-radius: var(--radius-sm);
  animation: pulse 1.5s infinite ease-in-out;
}

@keyframes pulse { 0% { opacity: 0.4; } 50% { opacity: 0.8; } 100% { opacity: 0.4; } }
</style>
