<script setup lang="ts">
import { useFontScale } from '@/shared/composables/useFontScale';
import { useTheme } from '@/shared/composables/useTheme';
import { Search, Bell, History, Sun, Moon, Type } from 'lucide-vue-next';

const emit = defineEmits<{
  search: [];
  notifications: [];
  audit: [];
}>();

const { theme, toggleTheme } = useTheme();
const { currentFontScale, cycleFontScale } = useFontScale();
</script>

<template>
  <div class="global-action-bar">
    <div class="theme-toggle" :class="{ dark: theme === 'dark' }" @click="toggleTheme">
      <Sun :size="12" class="toggle-icon sun-icon" />
      <Moon :size="12" class="toggle-icon moon-icon" />
      <div class="toggle-thumb"></div>
    </div>
    <button
      class="font-scale-btn"
      :class="{ 'font-scale-btn--active': currentFontScale.id !== 'default' }"
      :title="`Font size: ${currentFontScale.label}`"
      aria-label="Cycle global font size"
      @click="cycleFontScale"
    >
      <Type :size="15" class="font-scale-icon" />
      <span class="font-scale-label">Font</span>
      <span class="font-scale-value">{{ currentFontScale.label }}</span>
    </button>
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

.font-scale-btn {
  min-width: 108px;
  height: 34px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: 1px solid rgba(137, 206, 255, 0.55);
  border-radius: var(--radius-sm);
  background:
    linear-gradient(180deg, rgba(137, 206, 255, 0.16), rgba(137, 206, 255, 0.06)),
    var(--color-surface-container-high);
  color: var(--color-on-surface);
  cursor: pointer;
  font-family: var(--font-ui);
  padding: 0 10px;
  box-shadow: 0 0 0 1px rgba(137, 206, 255, 0.08), 0 0 12px rgba(137, 206, 255, 0.08);
  transition: background 0.2s, border-color 0.2s, color 0.2s, box-shadow 0.2s, transform 0.2s;
}

.font-scale-btn:hover {
  background:
    linear-gradient(180deg, rgba(137, 206, 255, 0.24), rgba(137, 206, 255, 0.1)),
    var(--nav-hover-bg);
  border-color: var(--color-secondary);
  box-shadow: 0 0 0 1px rgba(137, 206, 255, 0.18), 0 0 18px rgba(137, 206, 255, 0.18);
  transform: translateY(-1px);
}

.font-scale-btn--active {
  border-color: var(--color-secondary);
  color: var(--color-secondary);
  box-shadow: 0 0 0 1px rgba(137, 206, 255, 0.18), 0 0 16px rgba(137, 206, 255, 0.18);
}

.font-scale-icon {
  flex: 0 0 auto;
}

.font-scale-label {
  font-size: 0.75rem;
  font-weight: 700;
  line-height: 1;
}

.font-scale-value {
  font-family: var(--font-tech);
  font-size: 0.625rem;
  line-height: 1;
  opacity: 0.86;
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
