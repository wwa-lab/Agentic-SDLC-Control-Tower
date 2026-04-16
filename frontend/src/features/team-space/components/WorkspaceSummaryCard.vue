<script setup lang="ts">
import { Building2, Gauge, Layers3 } from 'lucide-vue-next';
import type { SectionResult } from '@/shared/types/section';
import type { WorkspaceSummary } from '../types/workspace';
import TeamSpaceCardShell from './TeamSpaceCardShell.vue';

interface Props {
  section: SectionResult<WorkspaceSummary>;
  isLoading?: boolean;
}

withDefaults(defineProps<Props>(), {
  isLoading: false,
});

defineEmits<{
  retry: [];
}>();

function healthLedClass(health: WorkspaceSummary['healthAggregate'] | undefined): string {
  switch (health) {
    case 'GREEN':
      return 'led-emerald';
    case 'YELLOW':
      return 'led-amber';
    case 'RED':
      return 'led-crimson';
    default:
      return 'led-neutral';
  }
}
</script>

<template>
  <TeamSpaceCardShell
    title="Workspace Summary"
    :loading="isLoading"
    :error="section.error"
    @retry="$emit('retry')"
  >
    <template #icon>
      <Building2 :size="16" />
    </template>

    <div v-if="section.data" class="summary-card">
      <div class="summary-card__identity">
        <div>
          <h2 class="summary-card__name">{{ section.data.name }}</h2>
          <p class="summary-card__meta text-body-sm">
            {{ section.data.applicationName }} / {{ section.data.snowGroupName ?? 'Compatibility Mode' }}
          </p>
        </div>
        <div class="summary-card__health">
          <span class="led" :class="healthLedClass(section.data.healthAggregate)"></span>
          <span class="text-label">{{ section.data.healthAggregate }}</span>
        </div>
      </div>

      <div class="summary-card__counters">
        <div class="counter">
          <Gauge :size="14" />
          <div>
            <span class="counter__value">{{ section.data.activeProjectCount }}</span>
            <span class="text-label">Projects</span>
          </div>
        </div>
        <div class="counter">
          <Layers3 :size="14" />
          <div>
            <span class="counter__value">{{ section.data.activeEnvironmentCount }}</span>
            <span class="text-label">Environments</span>
          </div>
        </div>
      </div>

      <div class="boundary-block">
        <span class="text-label">Responsibility Boundary</span>
        <div class="chip-row">
          <span v-for="application in section.data.responsibilityBoundary.applications" :key="application" class="boundary-chip">
            APP {{ application }}
          </span>
          <span
            v-if="section.data.responsibilityBoundary.snowGroups.length"
            v-for="group in section.data.responsibilityBoundary.snowGroups"
            :key="group"
            class="boundary-chip"
          >
            SNOW {{ group }}
          </span>
          <span v-else class="boundary-chip boundary-chip--neutral">Compatibility mode</span>
        </div>
      </div>

      <div class="owner-line text-body-sm">
        Workspace owner: <strong>{{ section.data.ownerDisplayName }}</strong>
      </div>
    </div>
  </TeamSpaceCardShell>
</template>

<style scoped>
.summary-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.summary-card__identity {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.summary-card__name {
  font-size: 1.2rem;
}

.summary-card__meta {
  color: var(--color-on-surface-variant);
}

.summary-card__health {
  display: flex;
  align-items: center;
  gap: 8px;
}

.summary-card__counters {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.counter {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 12px;
  border-radius: var(--radius-sm);
  background: rgba(255, 255, 255, 0.03);
}

.counter__value {
  display: block;
  font-size: 1.25rem;
  font-weight: 700;
}

.boundary-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.boundary-chip {
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(137, 206, 255, 0.08);
  border: 1px solid rgba(137, 206, 255, 0.16);
  font-size: 0.75rem;
  font-family: var(--font-tech);
}

.boundary-chip--neutral {
  background: rgba(255, 255, 255, 0.04);
  border-color: rgba(255, 255, 255, 0.08);
}

.owner-line {
  color: var(--color-on-surface-variant);
}

.led-neutral {
  background: var(--color-on-surface-variant);
  opacity: 0.45;
}
</style>
