<script setup lang="ts">
import { useWorkspaceContext } from '@/composables/useWorkspaceContext';
import { ChevronRight, RefreshCw } from 'lucide-vue-next';

const { context, loading, error, reload } = useWorkspaceContext();
</script>

<template>
  <div class="top-context-bar">
    <!-- Loading state -->
    <div v-if="loading" class="context-chain">
      <span class="text-label">Loading workspace context...</span>
    </div>

    <!-- Error state -->
    <div v-else-if="error" class="context-chain context-error">
      <span class="text-label">Context unavailable</span>
      <button class="icon-btn" @click="reload" title="Retry">
        <RefreshCw :size="14" />
      </button>
    </div>

    <!-- Normal state -->
    <div v-else class="context-chain">
      <div class="context-item">
        <span class="text-label">Workspace</span>
        <span class="text-tech">{{ context.workspace }}</span>
      </div>

      <ChevronRight :size="14" class="separator" />

      <div class="context-item">
        <span class="text-label">Application</span>
        <span class="text-tech">{{ context.application }}</span>
      </div>

      <ChevronRight :size="14" class="separator" />

      <div class="context-item">
        <span class="text-label">Group</span>
        <span class="text-tech">{{ context.snowGroup || '---' }}</span>
      </div>

      <ChevronRight :size="14" class="separator" />

      <div class="context-item">
        <span class="text-label">Project</span>
        <span class="text-tech">{{ context.project || '---' }}</span>
      </div>

      <ChevronRight :size="14" class="separator" />

      <div class="context-item">
        <span class="text-label">Environment</span>
        <span class="text-tech">{{ context.environment || '---' }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.top-context-bar {
  display: flex;
  align-items: center;
  padding: 0 24px;
  height: 48px;
}

.context-chain {
  display: flex;
  align-items: center;
  gap: 12px;
}

.context-error {
  color: var(--color-incident-crimson);
}

.context-item {
  display: flex;
  flex-direction: column;
}

.context-item .text-label {
  font-size: 0.625rem;
  line-height: 1;
  margin-bottom: 2px;
}

.context-item .text-tech {
  font-size: 0.75rem;
  color: var(--color-on-surface);
}

.separator {
  color: var(--color-on-surface-variant);
  opacity: 0.5;
  margin-top: 10px;
}

.icon-btn {
  background: transparent;
  border: none;
  color: var(--color-on-surface-variant);
  cursor: pointer;
  padding: 6px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.icon-btn:hover {
  background: var(--nav-hover-bg);
  color: var(--color-on-surface);
}
</style>
