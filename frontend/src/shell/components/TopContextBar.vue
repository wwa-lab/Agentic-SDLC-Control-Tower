<script setup lang="ts">
import { useWorkspaceStore } from '@/shared/stores/workspaceStore';
import { ChevronRight, RefreshCw } from 'lucide-vue-next';
import WorkspaceSwitcher from './WorkspaceSwitcher.vue';

const store = useWorkspaceStore();
</script>

<template>
  <div class="top-context-bar glass-panel">
    <!-- Loading state -->
    <div v-if="store.loading" class="context-chain animate-fade-in">
      <span class="text-label">Loading workspace context...</span>
    </div>

    <!-- Error state -->
    <div v-else-if="store.error" class="context-chain context-error animate-fade-in">
      <span class="text-label">Context unavailable</span>
      <button class="icon-btn" @click="store.load" title="Retry">
        <RefreshCw :size="14" />
      </button>
    </div>

    <!-- Normal state -->
    <div v-else class="context-chain animate-fade-in">
      <WorkspaceSwitcher />

      <ChevronRight :size="14" class="separator" />

      <div class="context-item">
        <span class="text-label">Application</span>
        <span class="text-tech">{{ store.context.application }}</span>
      </div>

      <ChevronRight :size="14" class="separator" />

      <div class="context-item">
        <span class="text-label">Group</span>
        <span class="text-tech">{{ store.context.snowGroup || '---' }}</span>
      </div>

      <ChevronRight :size="14" class="separator" />

      <div class="context-item">
        <span class="text-label">Project</span>
        <span class="text-tech">{{ store.context.project || '---' }}</span>
      </div>

      <ChevronRight :size="14" class="separator" />

      <div class="context-item">
        <span class="text-label">Environment</span>
        <span class="text-tech">{{ store.context.environment || '---' }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.top-context-bar {
  flex: 1 1 auto;
  display: flex;
  align-items: center;
  min-width: 0;
  padding: 0 24px;
  height: 48px;
  overflow: hidden;
}

.context-chain {
  flex: 1 1 auto;
  display: flex;
  align-items: center;
  min-width: 0;
  gap: 12px;
  overflow: hidden;
}

.context-error {
  color: var(--color-incident-crimson);
}

.context-item {
  display: flex;
  flex-direction: column;
  min-width: 0;
  max-width: 132px;
}

.context-item .text-label {
  font-size: 0.625rem;
  line-height: 1;
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.context-item .text-tech {
  font-size: 0.75rem;
  color: var(--color-on-surface);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.separator {
  color: var(--color-on-surface-variant);
  opacity: 0.5;
  margin-top: 10px;
  flex-shrink: 0;
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

@media (max-width: 720px) {
  .top-context-bar {
    padding: 0 12px;
  }

  .context-chain {
    gap: 8px;
  }

  .context-item {
    max-width: 96px;
  }

  .context-item:nth-of-type(n + 3),
  .separator:nth-of-type(n + 2) {
    display: none;
  }
}

@media (max-width: 520px) {
  .context-item:nth-of-type(n + 2),
  .separator {
    display: none;
  }
}
</style>
