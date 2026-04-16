<script setup lang="ts">
interface Props {
  title: string;
  isLoading?: boolean;
  error?: string | null;
  fullWidth?: boolean;
}

defineProps<Props>();
</script>

<template>
  <div class="dashboard-card" :class="{ 'card--full-width': fullWidth }">
    <header v-if="title">
      <span class="card-title">{{ title }}</span>
      <div v-if="isLoading" class="loading-indicator">
        <div class="skeleton-pulse"></div>
      </div>
    </header>
    
    <div class="card-content">
      <div v-if="error" class="card-error">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"/>
          <line x1="12" y1="8" x2="12" y2="12"/>
          <line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
        <span>{{ error }}</span>
      </div>
      <div v-else-if="isLoading" class="card-loading">
        <slot name="loading">
          <div class="skeleton-content"></div>
        </slot>
      </div>
      <slot v-else></slot>
    </div>
  </div>
</template>

<style scoped>
.dashboard-card {
  background: var(--color-surface-container-high);
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  box-shadow: var(--shadow-card);
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 120px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.dashboard-card:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-card-hover);
}

.card--full-width {
  grid-column: 1 / -1;
}

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

.card-content {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

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

.card-error span {
  font-size: 0.75rem;
}

.skeleton-pulse {
  width: 40px;
  height: 4px;
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

@keyframes pulse {
  0% { opacity: 0.4; }
  50% { opacity: 0.8; }
  100% { opacity: 0.4; }
}
</style>
