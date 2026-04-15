<script setup lang="ts">
import { useShellConfig } from '@/shell/composables/useShellConfig';

const emit = defineEmits<{
  action: [key: string];
}>();

const { config } = useShellConfig();
</script>

<template>
  <header class="page-header">
    <div class="title-section">
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
  padding: 24px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.page-title {
  font-size: 1.75rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.page-subtitle {
  margin-top: 4px;
}

.action-section {
  display: flex;
  gap: 12px;
}
</style>
