<script setup lang="ts">
import type { StatusDistribution, RequirementStatus } from '../types/requirement';

interface Props {
  distribution: StatusDistribution;
}

defineProps<Props>();
const emit = defineEmits<{ filter: [status: RequirementStatus] }>();

const segments: ReadonlyArray<{ key: keyof StatusDistribution; label: string; status: RequirementStatus; cssClass: string }> = [
  { key: 'draft', label: 'Draft', status: 'Draft', cssClass: 'seg-muted' },
  { key: 'inReview', label: 'In Review', status: 'In Review', cssClass: 'seg-amber' },
  { key: 'approved', label: 'Approved', status: 'Approved', cssClass: 'seg-green' },
  { key: 'inProgress', label: 'In Progress', status: 'In Progress', cssClass: 'seg-cyan' },
  { key: 'delivered', label: 'Delivered', status: 'Delivered', cssClass: 'seg-green' },
  { key: 'archived', label: 'Archived', status: 'Archived', cssClass: 'seg-muted' },
];
</script>

<template>
  <div class="status-distribution">
    <div
      v-for="seg in segments"
      :key="seg.key"
      class="dist-item"
      @click="emit('filter', seg.status)"
    >
      <span class="dist-count" :class="seg.cssClass">
        {{ distribution[seg.key] }}
      </span>
      <span class="dist-label">{{ seg.label }}</span>
    </div>
  </div>
</template>

<style scoped>
.status-distribution {
  display: flex;
  gap: 16px;
  padding: 12px 16px;
  background: var(--color-surface-container-high);
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
}

.dist-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  min-width: 60px;
  cursor: pointer;
  transition: opacity 0.15s ease;
}

.dist-item:hover { opacity: 0.75; }

.dist-count {
  font-family: var(--font-tech);
  font-size: 1.25rem;
  font-weight: 700;
}

.dist-label {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-on-surface-variant);
}

.seg-green { color: var(--color-health-emerald); }
.seg-amber { color: var(--color-approval-amber); }
.seg-cyan { color: var(--color-secondary); }
.seg-muted { color: var(--color-on-surface-variant); opacity: 0.6; }
</style>
