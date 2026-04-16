<script setup lang="ts">
import type { AiParticipation, SectionResult } from '../types/dashboard';
import DashboardCard from './DashboardCard.vue';
import MetricCard from './MetricCard.vue';

interface Props {
  section: SectionResult<AiParticipation>;
  isLoading?: boolean;
}

defineProps<Props>();
</script>

<template>
  <DashboardCard title="AI Participation" :is-loading="isLoading" :error="section.error">
    <div v-if="section.data" class="ai-participation-content">
      <div class="metrics-grid">
        <MetricCard v-bind="section.data.usageRate" />
        <MetricCard v-bind="section.data.adoptionRate" />
        <MetricCard v-bind="section.data.timeSaved" />
      </div>
      
      <div class="involvement-strip">
        <div class="strip-label">Stage Involvement</div>
        <div class="strip-nodes">
          <div 
            v-for="stage in section.data.stageInvolvement" 
            :key="stage.stageKey"
            class="involvement-node"
            :class="{ 'node--active': stage.involved }"
            :title="`${stage.stageKey}: ${stage.actionsCount} actions`"
          ></div>
        </div>
      </div>
    </div>
  </DashboardCard>
</template>

<style scoped>
.ai-participation-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.involvement-strip {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.strip-label {
  font-family: var(--font-ui);
  font-size: 0.625rem;
  text-transform: uppercase;
  color: var(--color-on-surface-variant);
  letter-spacing: 0.05em;
}

.strip-nodes {
  display: flex;
  gap: 4px;
}

.involvement-node {
  height: 6px;
  flex-grow: 1;
  background: var(--color-surface-container-low);
  border-radius: 1px;
}

.involvement-node.node--active {
  background: var(--color-secondary);
  box-shadow: 0 0 6px var(--color-secondary-glow);
}
</style>
