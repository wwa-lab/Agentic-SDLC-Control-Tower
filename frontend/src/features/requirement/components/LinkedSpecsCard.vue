<script setup lang="ts">
import type { SectionResult, LinkedSpecsSection } from '../types/requirement';
import RequirementCard from './RequirementCard.vue';

interface Props {
  requirementId: string;
  linkedSpecs: SectionResult<LinkedSpecsSection>;
  isLoading?: boolean;
}

interface GeneratePayload {
  sourceType: 'requirement' | 'story';
  sourceId: string;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  generateSpec: [payload: GeneratePayload];
  navigate: [path: string];
}>();

function handleGenerate() {
  // Find first story without a spec to generate from
  // Falls back to requirement-level generation if no stories exist
  emit('generateSpec', { sourceType: 'requirement', sourceId: '' });
}

const STATUS_COLORS: Record<string, string> = {
  Draft: 'status--muted',
  Review: 'status--amber',
  Approved: 'status--green',
  Implemented: 'status--cyan',
};
</script>

<template>
  <RequirementCard
    title="Linked Specs"
    :is-loading="isLoading"
    :error="linkedSpecs.error"
  >
    <div v-if="linkedSpecs.data" class="specs-content">
      <div class="specs-header">
        <span class="specs-count">{{ linkedSpecs.data.totalCount }} specs</span>
        <button class="action-btn" @click="handleGenerate">Generate Spec</button>
      </div>

      <div v-if="linkedSpecs.data.specs.length === 0" class="empty">
        No specs linked — generate from stories
      </div>

      <div v-else class="specs-list">
        <div
          v-for="spec in linkedSpecs.data.specs"
          :key="spec.id"
          :id="`spec-${spec.id}`"
          class="spec-item"
          @click="emit('navigate', `/requirements/${props.requirementId}#spec-${spec.id}`)"
        >
          <span class="spec-indicator"></span>
          <span class="spec-id">{{ spec.id }}</span>
          <span class="spec-title">{{ spec.title }}</span>
          <span class="spec-version">{{ spec.version }}</span>
          <span class="spec-status" :class="STATUS_COLORS[spec.status]">{{ spec.status }}</span>
        </div>
      </div>
    </div>
  </RequirementCard>
</template>

<style scoped>
.specs-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.specs-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.specs-count {
  font-family: var(--font-tech);
  font-size: 0.625rem;
  color: var(--color-on-surface-variant);
}

.action-btn {
  background: none;
  border: 1px solid var(--color-secondary);
  color: var(--color-secondary);
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background: var(--color-secondary);
  color: var(--color-on-secondary-container);
}

.specs-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.spec-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background 0.15s ease;
  border-left: 2px solid var(--color-secondary);
}

.spec-item:hover { background: var(--nav-hover-bg); }

.spec-indicator {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--color-secondary);
  box-shadow: 0 0 6px var(--color-secondary-glow);
}

.spec-id {
  font-family: var(--font-tech);
  font-size: 0.625rem;
  color: var(--color-secondary);
  min-width: 60px;
}

.spec-title {
  font-family: var(--font-ui);
  font-size: 0.6875rem;
  color: var(--color-on-surface);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.spec-version {
  font-family: var(--font-tech);
  font-size: 0.5625rem;
  color: var(--color-on-surface-variant);
}

.spec-status {
  font-family: var(--font-ui);
  font-size: 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  padding: 1px 6px;
  border-radius: 2px;
}

.status--green { color: var(--color-health-emerald); background: rgba(78, 222, 163, 0.1); }
.status--amber { color: var(--color-approval-amber); background: rgba(245, 158, 11, 0.1); }
.status--cyan { color: var(--color-secondary); background: var(--color-secondary-tint); }
.status--muted { color: var(--color-on-surface-variant); background: rgba(148, 163, 184, 0.1); }

.empty {
  font-family: var(--font-ui);
  font-size: 0.75rem;
  color: var(--color-on-surface-variant);
  opacity: 0.5;
  text-align: center;
  padding: 16px 0;
}
</style>
