<script setup lang="ts">
import { computed } from 'vue';
import type { ActivityEntry } from '../types/dashboard';

interface Props {
  entry: ActivityEntry;
}

const props = defineProps<Props>();

const timeAgo = computed(() => {
  const diff = Date.now() - new Date(props.entry.timestamp).getTime();
  const minutes = Math.floor(diff / 60000);
  if (minutes < 1) return 'just now';
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  return `${Math.floor(hours / 24)}d ago`;
});
</script>

<template>
  <div class="activity-entry" :class="[`actor--${entry.actorType}`]">
    <div class="actor-icon">
      <svg v-if="entry.actorType === 'ai'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2zm0 18a8 8 0 1 1 8-8 8 8 0 0 1-8 8z"/>
        <path d="M12 6v6l4 2"/>
        <circle cx="12" cy="12" r="3"/>
      </svg>
      <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
        <circle cx="12" cy="7" r="4"/>
      </svg>
    </div>
    
    <div class="entry-content">
      <span class="actor-name">{{ entry.actor }}</span>
      <span class="action-text">{{ entry.action }}</span>
    </div>
    
    <div class="entry-meta">
      <div class="stage-badge">{{ entry.stageKey }}</div>
      <div class="timestamp">{{ timeAgo }}</div>
    </div>
  </div>
</template>

<style scoped>
.activity-entry {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  gap: 12px;
  border-bottom: 1px solid var(--border-separator);
  font-family: var(--font-ui);
}

.activity-entry:last-child {
  border-bottom: none;
}

.actor-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--color-surface-container-low);
  color: var(--color-on-surface-variant);
  flex-shrink: 0;
}

.actor--ai .actor-icon {
  background: var(--color-secondary-tint);
  color: var(--color-secondary);
}

.entry-content {
  flex-grow: 1;
  font-size: 0.75rem;
  display: flex;
  gap: 6px;
}

.actor-name {
  font-weight: 600;
  color: var(--color-on-surface);
}

.actor--ai .actor-name {
  color: var(--color-secondary);
}

.action-text {
  color: var(--color-on-surface-variant);
}

.entry-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.stage-badge {
  font-size: 0.625rem;
  text-transform: uppercase;
  color: var(--color-on-surface-variant);
  background: var(--color-surface-container-high);
  padding: 2px 6px;
  border-radius: 2px;
  letter-spacing: 0.05em;
}

.timestamp {
  font-family: var(--font-tech);
  font-size: 0.625rem;
  color: var(--color-on-surface-variant);
  min-width: 60px;
  text-align: right;
}
</style>
