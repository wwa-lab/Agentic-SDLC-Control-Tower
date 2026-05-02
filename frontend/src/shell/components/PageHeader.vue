<script setup lang="ts">
import { useShellConfig } from '@/shell/composables/useShellConfig';
import { useShellUiStore } from '@/shell/stores/shellUiStore';

const emit = defineEmits<{
  action: [key: string];
}>();

const { config } = useShellConfig();
const shellUiStore = useShellUiStore();
</script>

<template>
  <header class="page-header">
    <div class="title-section">
      <div v-if="shellUiStore.breadcrumbs.length" class="breadcrumb-row">
        <span
          v-for="(item, index) in shellUiStore.breadcrumbs"
          :key="`${item.label}-${index}`"
          class="breadcrumb-item text-label"
        >
          {{ item.label }}
        </span>
      </div>
      <h1 class="page-title">{{ config.title }}</h1>
      <p v-if="config.subtitle" class="page-subtitle text-label">{{ config.subtitle }}</p>
      <p v-else class="page-subtitle text-label">{{ config.navKey.toUpperCase() }} OPERATIONAL VIEW</p>
    </div>

    <div v-if="config.actions?.length" class="action-section">
      <button
        v-for="action in config.actions"
        :key="action.key"
        class="btn-machined"
        :class="{ 'btn-ai': action.variant === 'ai' }"
        @click="emit('action', action.key)"
      >
        {{ action.label }}
      </button>
    </div>
  </header>
</template>

<style scoped>
.page-header {
  min-width: 0;
  padding: 24px clamp(12px, 2vw, 24px);
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
}

.title-section {
  min-width: 0;
}

.page-title {
  font-size: 1.75rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  overflow-wrap: anywhere;
}

.breadcrumb-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.breadcrumb-item {
  padding: 4px 8px;
  border-radius: 999px;
  background: rgba(137, 206, 255, 0.08);
  border: 1px solid rgba(137, 206, 255, 0.12);
}

.page-subtitle {
  margin-top: 4px;
}

.action-section {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 720px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .page-title {
    font-size: 1.5rem;
  }

  .action-section {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
