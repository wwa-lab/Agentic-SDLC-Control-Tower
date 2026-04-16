<script setup lang="ts">
import type { DiagnosisFeed, SectionResult } from '../types/incident';
import IncidentCard from './IncidentCard.vue';

interface Props {
  diagnosis: SectionResult<DiagnosisFeed>;
  isLoading?: boolean;
}

defineProps<Props>();
</script>

<template>
  <IncidentCard title="AI Diagnosis" :is-loading="isLoading" :error="diagnosis.error">
    <div v-if="diagnosis.data" class="diagnosis-content">
      <div class="feed">
        <div
          v-for="(entry, i) in diagnosis.data.entries"
          :key="i"
          class="feed-entry"
          :class="`entry--${entry.entryType}`"
        >
          <span class="entry-time">[{{ entry.timestamp }}]</span>
          <span class="entry-text">{{ entry.text }}</span>
        </div>
      </div>

      <div v-if="diagnosis.data.rootCause" class="root-cause">
        <div class="rc-header">
          <span class="rc-label">Root Cause Hypothesis</span>
          <span class="confidence-badge" :class="`confidence--${diagnosis.data.rootCause.confidence.toLowerCase()}`">
            {{ diagnosis.data.rootCause.confidence }}
          </span>
        </div>
        <p class="rc-text">{{ diagnosis.data.rootCause.hypothesis }}</p>
      </div>

      <div v-if="diagnosis.data.affectedComponents.length" class="affected">
        <span class="affected-label">Affected Components</span>
        <div class="component-tags">
          <span v-for="comp in diagnosis.data.affectedComponents" :key="comp" class="component-tag">
            {{ comp }}
          </span>
        </div>
      </div>
    </div>
  </IncidentCard>
</template>

<style scoped>
.diagnosis-content { display: flex; flex-direction: column; gap: 16px; }

.feed {
  background: var(--color-surface-container-low);
  border-radius: 2px;
  padding: 12px;
  font-family: var(--font-tech);
  font-size: 0.75rem;
  line-height: 1.6;
  color: var(--color-on-surface-variant);
  max-height: 300px;
  overflow-y: auto;
}

.feed-entry { margin-bottom: 2px; }
.entry-time { color: var(--color-on-surface-variant); opacity: 0.6; }
.entry--suggestion { color: var(--color-secondary); }
.entry--conclusion { color: var(--color-on-surface); font-weight: 600; }
.entry--finding { color: var(--color-on-surface); }

.root-cause {
  padding: 12px;
  background: var(--color-surface-container-low);
  border-radius: var(--radius-sm);
}

.rc-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }

.rc-label {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-on-surface-variant);
}

.confidence-badge {
  font-family: var(--font-tech);
  font-size: 0.5625rem;
  padding: 1px 6px;
  border-radius: 2px;
  font-weight: 600;
}

.confidence--high { color: var(--color-health-emerald); background: rgba(78, 222, 163, 0.1); }
.confidence--medium { color: var(--color-approval-amber); background: rgba(245, 158, 11, 0.1); }
.confidence--low { color: var(--color-incident-crimson); background: var(--color-incident-tint); }

.rc-text {
  font-family: var(--font-ui);
  font-size: 0.75rem;
  color: var(--color-on-surface);
  margin: 0;
  line-height: 1.5;
}

.affected { display: flex; flex-direction: column; gap: 6px; }

.affected-label {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-on-surface-variant);
}

.component-tags { display: flex; gap: 6px; flex-wrap: wrap; }

.component-tag {
  font-family: var(--font-tech);
  font-size: 0.625rem;
  padding: 2px 8px;
  background: var(--color-surface-container-highest);
  border-radius: 2px;
  color: var(--color-on-surface-variant);
}
</style>
