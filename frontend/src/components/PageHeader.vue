<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import type { ShellPageConfig } from '@/types/shell';

const route = useRoute();

const config = computed<ShellPageConfig>(() => ({
  navKey: (route.meta.navKey as string) ?? '',
  title: (route.meta.title as string) ?? '',
  subtitle: route.meta.subtitle as string | undefined,
  actions: route.meta.actions as ShellPageConfig['actions'] | undefined
}));
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
        :class="{ 'btn-ai': action.key === 'ai' }"
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
