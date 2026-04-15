<script setup lang="ts">
import { useWorkspaceContext } from '@/composables/useWorkspaceContext';
import { useTheme } from '@/composables/useTheme';
import { ChevronRight, Search, Bell, History, Sun, Moon } from 'lucide-vue-next';

const { context } = useWorkspaceContext();
const { theme, toggleTheme } = useTheme();
</script>

<template>
  <header class="top-context-bar section-low">
    <div class="context-chain">
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

    <div class="utility-actions">
      <div class="theme-toggle" :class="{ dark: theme === 'dark' }" @click="toggleTheme">
        <Sun :size="12" class="toggle-icon sun-icon" />
        <Moon :size="12" class="toggle-icon moon-icon" />
        <div class="toggle-thumb"></div>
      </div>
      <button class="icon-btn"><Search :size="18" /></button>
      <button class="icon-btn"><Bell :size="18" /></button>
      <button class="icon-btn"><History :size="18" /></button>
    </div>
  </header>
</template>

<style scoped>
.top-context-bar {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  border-bottom: var(--border-ghost);
}

.context-chain {
  display: flex;
  align-items: center;
  gap: 12px;
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

.utility-actions {
  display: flex;
  gap: 8px;
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

/* Theme toggle slider */
.theme-toggle {
  width: 48px;
  height: 24px;
  border-radius: 12px;
  background: var(--color-surface-container-high);
  cursor: pointer;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 6px;
  transition: background 0.3s;
}

.toggle-icon {
  position: relative;
  z-index: 1;
  transition: color 0.3s;
}

.sun-icon {
  color: var(--color-approval-amber);
}

.moon-icon {
  color: var(--color-on-surface-variant);
}

.theme-toggle.dark .sun-icon {
  color: var(--color-on-surface-variant);
}

.theme-toggle.dark .moon-icon {
  color: var(--color-secondary);
}

.toggle-thumb {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--color-on-surface);
  transition: transform 0.3s;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
}

.theme-toggle.dark .toggle-thumb {
  transform: translateX(24px);
}
</style>
