<script setup lang="ts">
import type { SectionResult } from '@/shared/types/section';
import type { DeployArtifactRefCard } from '../../types/deploy';

defineProps<{ section: SectionResult<DeployArtifactRefCard> }>();
const emit = defineEmits<{ openBuildArtifact: [artifactId: string] }>();

function shortenSha(sha: string): string {
  return sha.slice(0, 8);
}
</script>

<template>
  <div class="deploy-change-summary-card card">
    <div class="card-title">Change Summary</div>
    <div v-if="section.error" class="card-error">{{ section.error }}</div>
    <div v-else-if="!section.data" class="card-skeleton">Loading change summary...</div>
    <template v-else>
      <div class="artifact-ref">
        <span class="ref-label">Build Artifact</span>
        <button
          class="artifact-link"
          @click="emit('openBuildArtifact', section.data.buildArtifactRef.buildArtifactId)"
        >
          {{ section.data.buildArtifactRef.buildArtifactId }}
        </button>
        <span v-if="!section.data.buildArtifactResolved" class="unresolved-tag">unresolved</span>
      </div>

      <div v-if="section.data.buildSummary" class="summary-grid">
        <div class="summary-item">
          <span class="summary-label">Pipeline</span>
          <span class="summary-value">{{ section.data.buildSummary.pipelineName }}</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">Commits</span>
          <span class="summary-value summary-value--tech">{{ section.data.buildSummary.commitCount }}</span>
        </div>
        <div class="summary-item sha-range">
          <span class="summary-label">SHA Range</span>
          <span class="summary-value summary-value--tech">
            {{ shortenSha(section.data.buildSummary.commitRangeBaseSha) }}..{{ shortenSha(section.data.buildSummary.commitRangeHeadSha) }}
          </span>
        </div>
      </div>

      <div v-else class="card-empty">Build summary not available.</div>
    </template>
  </div>
</template>

<style scoped>
.deploy-change-summary-card { padding: 16px; }
.card {
  background: var(--color-surface-container-low);
  border: var(--border-ghost);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
}
.card-title {
  margin-bottom: 12px;
  font-family: var(--font-ui);
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--color-on-surface);
  text-transform: uppercase;
  letter-spacing: 0.03em;
}
.card-error { color: var(--color-incident-crimson); font-size: 0.85rem; }
.card-skeleton { color: var(--color-on-surface-variant); font-size: 0.85rem; }
.card-empty { color: var(--color-on-surface-variant); font-size: 0.85rem; margin-top: 8px; }

.artifact-ref {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.ref-label {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-on-surface-variant);
}
.artifact-link {
  background: none;
  border: none;
  padding: 0;
  font-family: var(--font-tech);
  font-size: 0.8rem;
  color: var(--color-secondary);
  cursor: pointer;
  text-decoration: none;
}
.artifact-link:hover { text-decoration: underline; }
.unresolved-tag {
  padding: 1px 6px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-approval-amber);
  color: var(--color-approval-amber);
  font-family: var(--font-ui);
  font-size: 0.6rem;
  font-weight: 600;
  text-transform: uppercase;
}

.summary-grid {
  display: flex;
  gap: 24px;
  margin-top: 12px;
  padding: 10px 12px;
  background: var(--color-surface-container-high);
  border-radius: var(--radius-sm);
  flex-wrap: wrap;
}
.summary-item { display: flex; flex-direction: column; gap: 2px; }
.summary-label {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-on-surface-variant);
}
.summary-value {
  font-family: var(--font-ui);
  font-size: 0.8rem;
  color: var(--color-on-surface);
}
.summary-value--tech { font-family: var(--font-tech); }
</style>
