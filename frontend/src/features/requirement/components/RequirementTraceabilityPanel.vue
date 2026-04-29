<script setup lang="ts">
import { computed } from 'vue';
import type { FreshnessStatus, RequirementTraceability } from '../types/requirement';
import RequirementCard from './RequirementCard.vue';
import FreshnessChip from './FreshnessChip.vue';

const props = defineProps<{ traceability: RequirementTraceability | null; isLoading?: boolean }>();

const sources = computed(() => props.traceability?.sources ?? []);
const documents = computed(() => props.traceability?.documents.stages ?? []);
const reviews = computed(() => props.traceability?.reviews ?? []);

const freshSources = computed(() => sources.value.filter(source => source.freshnessStatus === 'FRESH'));
const sourceIssues = computed(() => sources.value.filter(source => source.freshnessStatus !== 'FRESH'));
const readyDocuments = computed(() => documents.value.filter(document => !document.missing));
const missingDocuments = computed(() => documents.value.filter(document => document.missing || document.freshnessStatus === 'MISSING_DOCUMENT'));
const staleReviews = computed(() => reviews.value.filter(review => review.stale));
const qualityBlockedDocuments = computed(() => documents.value.filter(document => document.qualityGate && (!document.qualityGate.passed || document.qualityGate.stale)));

const readinessStatus = computed<FreshnessStatus>(() => {
  if (!props.traceability) return 'UNKNOWN';
  if (sourceIssues.value.length > 0) return sourceIssues.value[0]?.freshnessStatus ?? 'UNKNOWN';
  if (missingDocuments.value.length > 0) return 'MISSING_DOCUMENT';
  if (staleReviews.value.length > 0) return 'DOCUMENT_CHANGED_AFTER_REVIEW';
  return 'FRESH';
});

const readinessLabel = computed(() => {
  if (!props.traceability) return 'No readiness signals';
  if (missingDocuments.value.length > 0) return 'Documents needed';
  if (qualityBlockedDocuments.value.length > 0) return 'Quality gate blocked';
  if (sourceIssues.value.length > 0) return 'Source needs sync';
  if (staleReviews.value.length > 0) return 'Review needs refresh';
  return 'Ready for review';
});

const blockers = computed(() => {
  const items: string[] = [];
  if (!props.traceability) return items;
  if (sources.value.length === 0) items.push('No source reference linked');
  if (sourceIssues.value.length > 0) items.push(`${sourceIssues.value.length} source reference needs refresh`);
  if (missingDocuments.value.length > 0) items.push(`${missingDocuments.value.length} expected SDD documents are missing`);
  if (qualityBlockedDocuments.value.length > 0) items.push(`${qualityBlockedDocuments.value.length} document quality gate needs attention`);
  if (staleReviews.value.length > 0) items.push(`${staleReviews.value.length} business reviews are stale`);
  return items;
});

function formatCount(ready: number, total: number, emptyLabel: string) {
  return total === 0 ? emptyLabel : `${ready} / ${total}`;
}
</script>

<template>
  <RequirementCard title="Review Readiness" :is-loading="isLoading">
    <div class="readiness-panel">
      <div class="readiness-top">
        <div>
          <span class="readiness-label">Current State</span>
          <strong>{{ readinessLabel }}</strong>
        </div>
        <FreshnessChip :status="readinessStatus" />
      </div>

      <div class="readiness-metrics">
        <div class="metric">
          <span>Sources</span>
          <strong>{{ formatCount(freshSources.length, sources.length, 'None') }}</strong>
          <small>current</small>
        </div>
        <div class="metric">
          <span>SDD Documents</span>
          <strong>{{ formatCount(readyDocuments.length, documents.length, 'None') }}</strong>
          <small>available</small>
        </div>
        <div class="metric">
          <span>Business Review</span>
          <strong>{{ reviews.length ? `${reviews.length - staleReviews.length} / ${reviews.length}` : 'Pending' }}</strong>
          <small>{{ reviews.length ? 'current' : 'not started' }}</small>
        </div>
      </div>

      <div class="blockers">
        <span class="blockers-label">Blocking Items</span>
        <div v-if="blockers.length === 0" class="blocker-row blocker-row--clear">No blocking items</div>
        <div v-for="item in blockers" v-else :key="item" class="blocker-row">
          {{ item }}
        </div>
      </div>
    </div>
  </RequirementCard>
</template>

<style scoped>
.readiness-panel { display: flex; flex-direction: column; gap: 12px; }
.readiness-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 10px;
  border-bottom: var(--border-ghost);
}
.readiness-label,
.blockers-label,
.metric span,
.metric small {
  display: block;
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-on-surface-variant);
}
.readiness-top strong {
  display: block;
  margin-top: 4px;
  color: var(--color-on-surface);
  font-size: 0.9375rem;
}
.readiness-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}
.metric {
  min-width: 0;
  padding: 10px;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
}
.metric strong {
  display: block;
  margin: 5px 0 2px;
  color: var(--color-on-surface);
  font-family: var(--font-tech);
  font-size: 0.9375rem;
}
.blockers { display: flex; flex-direction: column; gap: 6px; }
.blocker-row {
  padding: 8px 10px;
  border-radius: var(--radius-sm);
  color: var(--color-incident-crimson);
  background: rgba(239, 68, 68, 0.1);
  font-size: 0.75rem;
}
.blocker-row--clear {
  color: var(--color-health-emerald);
  background: rgba(78, 222, 163, 0.1);
}
@media (max-width: 720px) {
  .readiness-metrics { grid-template-columns: 1fr; }
}
</style>
