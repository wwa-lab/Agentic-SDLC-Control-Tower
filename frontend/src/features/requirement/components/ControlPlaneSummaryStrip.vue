<script setup lang="ts">
import { GitBranch, FileText, Link2, ShieldCheck } from 'lucide-vue-next';

defineProps<{
  total: number;
  fresh: number;
  stale: number;
  missing: number;
  errors: number;
  sources: number;
  documents: number;
  artifacts: number;
  isLoading?: boolean;
}>();

function pluralize(count: number, noun: string): string {
  return `${count} ${noun}${count === 1 ? '' : 's'}`;
}
</script>

<template>
  <section class="control-strip">
    <div class="strip-title">
      <GitBranch :size="16" />
      <div>
        <span class="eyebrow">Requirement Control Plane</span>
        <strong>
          {{ isLoading ? 'Refreshing traceability signals' : `${fresh}/${total} requirements aligned` }}
        </strong>
        <span class="strip-help">
          {{ pluralize(total - fresh, 'requirement') }} need source, document, or review attention
        </span>
      </div>
    </div>
    <div class="strip-metrics">
      <span class="metric metric--ok" title="Requirements whose linked sources, documents, and reviews are aligned">
        <ShieldCheck :size="14" /> {{ fresh }} aligned
      </span>
      <span class="metric metric--warn" title="Requirements where source or document versions changed after downstream work">
        {{ stale }} stale
      </span>
      <span class="metric metric--miss" title="Requirements missing source references or expected SDD documents">
        {{ missing }} missing links
      </span>
      <span class="metric" :class="{ 'metric--miss': errors > 0 }" title="Requirements whose control-plane summary failed to load">
        {{ errors }} errors
      </span>
      <span class="metric" title="Total linked authoritative sources across this list">
        <Link2 :size="14" /> {{ sources }} sources
      </span>
      <span class="metric" title="Total indexed SDD documents across this list">
        <FileText :size="14" /> {{ documents }} docs indexed
      </span>
      <span class="metric" title="Total downstream artifacts linked back to these requirements">
        {{ artifacts }} artifacts
      </span>
    </div>
  </section>
</template>

<style scoped>
.control-strip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-high);
  box-shadow: var(--shadow-card);
}

.strip-title {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 220px;
  color: var(--color-secondary);
}

.strip-title > div {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.eyebrow {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-on-surface-variant);
}

strong {
  font-family: var(--font-ui);
  font-size: 0.8125rem;
  color: var(--color-on-surface);
}

.strip-help {
  font-family: var(--font-ui);
  font-size: 0.625rem;
  color: var(--color-on-surface-variant);
}

.strip-metrics {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.metric {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  min-height: 24px;
  padding: 3px 8px;
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.625rem;
}

.metric--ok { color: var(--color-health-emerald); background: rgba(78, 222, 163, 0.1); }
.metric--warn { color: var(--color-approval-amber); background: rgba(245, 158, 11, 0.1); }
.metric--miss { color: var(--color-incident-crimson); background: rgba(239, 68, 68, 0.1); }

@media (max-width: 900px) {
  .control-strip {
    align-items: flex-start;
    flex-direction: column;
  }

  .strip-metrics { justify-content: flex-start; }
}
</style>
