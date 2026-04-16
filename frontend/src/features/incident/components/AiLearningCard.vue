<script setup lang="ts">
import type { AiLearning, SectionResult } from '../types/incident';
import IncidentCard from './IncidentCard.vue';

interface Props {
  learning: SectionResult<AiLearning>;
  isLoading?: boolean;
  isResolved?: boolean;
}

defineProps<Props>();
</script>

<template>
  <IncidentCard title="AI Learning & Prevention" :is-loading="isLoading" :error="learning.error" full-width>
    <div v-if="learning.data" class="learning-content">
      <div class="learning-section">
        <span class="section-label">Root Cause (Confirmed)</span>
        <p class="section-text">{{ learning.data.rootCause }}</p>
      </div>

      <div class="learning-section">
        <span class="section-label">Pattern Identified</span>
        <p class="section-text">{{ learning.data.patternIdentified }}</p>
      </div>

      <div class="learning-section">
        <span class="section-label">Prevention Recommendations</span>
        <ul class="prevention-list">
          <li v-for="(rec, i) in learning.data.preventionRecommendations" :key="i">{{ rec }}</li>
        </ul>
      </div>

      <div class="kb-indicator" :class="{ 'kb--created': learning.data.knowledgeBaseEntryCreated }">
        <span class="kb-dot"></span>
        <span class="kb-text">
          {{ learning.data.knowledgeBaseEntryCreated ? 'Knowledge base entry created' : 'Knowledge base entry pending' }}
        </span>
      </div>
    </div>

    <div v-else-if="!isResolved && !learning.error" class="not-available">
      Learning will be available after resolution
    </div>
  </IncidentCard>
</template>

<style scoped>
.learning-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.learning-section { display: flex; flex-direction: column; gap: 4px; }

.section-label {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-on-surface-variant);
}

.section-text {
  font-family: var(--font-ui);
  font-size: 0.75rem;
  color: var(--color-on-surface);
  margin: 0;
  line-height: 1.5;
}

.prevention-list {
  margin: 0;
  padding-left: 16px;
  font-family: var(--font-ui);
  font-size: 0.75rem;
  color: var(--color-on-surface);
  line-height: 1.6;
}

.prevention-list li { margin-bottom: 4px; }

.kb-indicator {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--color-surface-container-low);
  border-radius: var(--radius-sm);
}

.kb-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-on-surface-variant);
  opacity: 0.4;
}

.kb--created .kb-dot {
  background: var(--color-health-emerald);
  opacity: 1;
}

.kb-text {
  font-family: var(--font-ui);
  font-size: 0.6875rem;
  color: var(--color-on-surface-variant);
}

.kb--created .kb-text { color: var(--color-health-emerald); }

.not-available {
  font-family: var(--font-ui);
  font-size: 0.75rem;
  color: var(--color-on-surface-variant);
  text-align: center;
  padding: 24px;
  opacity: 0.6;
}
</style>
