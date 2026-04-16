<script setup lang="ts">
import { computed } from 'vue';
import type { SdlcStageHealth } from '../types/dashboard';

interface Props {
  stage: SdlcStageHealth;
}

const props = defineProps<Props>();
const emit = defineEmits(['click']);

const statusClass = computed(() => `status-led--${props.stage.status}`);
</script>

<template>
  <div 
    class="stage-node" 
    :class="{ 'stage-node--hub': stage.isHub }"
    @click="emit('click', stage.key)"
  >
    <div class="node-header">
      <div class="status-led" :class="statusClass"></div>
      <div v-if="stage.isHub" class="hub-badge">HUB</div>
    </div>
    
    <div class="node-content">
      <div class="node-label">{{ stage.label }}</div>
      <div class="node-count">{{ stage.itemCount }}</div>
    </div>
  </div>
</template>

<style scoped>
.stage-node {
  display: flex;
  flex-direction: column;
  padding: 8px;
  background: var(--color-surface-container-high);
  border-radius: var(--radius-sm);
  min-width: 90px;
  cursor: pointer;
  transition: transform 0.2s ease, background 0.2s ease;
  user-select: none;
  border: 1px solid transparent; /* No-Line Rule: use for highlighting only */
}

.stage-node:hover {
  background: var(--color-surface-container-highest);
  transform: translateY(-2px);
}

.stage-node--hub {
  min-width: 100px;
  border-color: var(--color-secondary-glow);
  box-shadow: 0 0 12px var(--color-secondary-tint);
}

.node-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.status-led {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-led--healthy { 
  background-color: var(--color-health-emerald); 
  box-shadow: 0 0 6px var(--color-health-emerald);
}
.status-led--warning {
  background-color: var(--color-approval-amber);
  box-shadow: 0 0 6px var(--color-approval-amber);
}
.status-led--critical { 
  background-color: var(--color-incident-crimson); 
  box-shadow: 0 0 6px var(--color-incident-crimson);
}
.status-led--inactive { 
  background-color: var(--color-on-surface-variant); 
}

.hub-badge {
  font-family: var(--font-ui);
  font-size: 0.5rem;
  font-weight: 700;
  padding: 2px 4px;
  background: var(--color-secondary-subtle);
  color: var(--color-secondary);
  border-radius: 2px;
  letter-spacing: 0.05em;
}

.node-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.node-label {
  font-family: var(--font-ui);
  font-size: 0.625rem;
  text-transform: uppercase;
  color: var(--color-on-surface-variant);
  letter-spacing: 0.05em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.node-count {
  font-family: var(--font-tech);
  font-size: 1rem;
  color: var(--color-on-surface);
}

.stage-node--hub .node-count {
  color: var(--color-secondary);
}
</style>
