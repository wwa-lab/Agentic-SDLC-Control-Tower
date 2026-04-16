<script setup lang="ts">
import { useTheme } from '@/shared/composables/useTheme';
import { Search, Bell, History, Sun, Moon } from 'lucide-vue-next';

const emit = defineEmits<{
  search: [];
  notifications: [];
  audit: [];
}>();

const { theme, toggleTheme } = useTheme();
</script>

<template>
  <div class="global-action-bar">
    <div class="theme-toggle" :class="{ dark: theme === 'dark' }" @click="toggleTheme">
      <Sun :size="12" class="toggle-icon sun-icon" />
      <Moon :size="12" class="toggle-icon moon-icon" />
      <div class="toggle-thumb"></div>
    </div>
    <button class="icon-btn" title="Search" @click="emit('search')"><Search :size="18" /></button>
    <button class="icon-btn" title="Notifications" @click="emit('notifications')"><Bell :size="18" /></button>
    <button class="icon-btn" title="Audit / History" @click="emit('audit')"><History :size="18" /></button>
  </div>
</template>

<style scoped>
.global-action-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-right: 24px;
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

.sun-icon { color: var(--color-approval-amber); }
.moon-icon { color: var(--color-on-surface-variant); }
.theme-toggle.dark .sun-icon { color: var(--color-on-surface-variant); }
.theme-toggle.dark .moon-icon { color: var(--color-secondary); }

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
