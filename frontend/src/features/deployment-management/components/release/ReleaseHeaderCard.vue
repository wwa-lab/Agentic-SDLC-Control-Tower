<script setup lang="ts">
import type { SectionResult } from '@/shared/types/section';
import type { ReleaseHeader } from '../../types/release';
import ReleaseVersionPill from '../primitives/ReleaseVersionPill.vue';
import ReleaseStateBadge from '../primitives/ReleaseStateBadge.vue';
import JenkinsLinkOut from '../primitives/JenkinsLinkOut.vue';

defineProps<{ section: SectionResult<ReleaseHeader> }>();

function formatDate(iso: string): string {
  return new Date(iso).toLocaleString();
}
</script>

<template>
  <div class="release-header-card card">
    <div v-if="section.error" class="card-error">{{ section.error }}</div>
    <div v-else-if="!section.data" class="card-skeleton">Loading release header...</div>
    <template v-else>
      <div class="top-row">
        <ReleaseVersionPill
          :version="section.data.releaseVersion"
          :build-artifact-id="section.data.buildArtifactRef.buildArtifactId"
        />
        <ReleaseStateBadge :state="section.data.state" />
      </div>

      <dl class="meta-grid">
        <div class="meta-item">
          <dt>Application</dt>
          <dd>{{ section.data.applicationId }}</dd>
        </div>
        <div class="meta-item">
          <dt>Created</dt>
          <dd>{{ formatDate(section.data.createdAt) }}</dd>
        </div>
        <div class="meta-item">
          <dt>Build Artifact</dt>
          <dd class="mono">{{ section.data.buildArtifactRef.buildArtifactId }}</dd>
        </div>
        <div class="meta-item">
          <dt>Jenkins</dt>
          <dd>
            <JenkinsLinkOut :url="section.data.jenkinsSourceUrl" label="View Job" />
          </dd>
        </div>
      </dl>
    </template>
  </div>
</template>

<style scoped>
.card {
  background: var(--color-surface-container-low);
  border: var(--border-ghost);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  padding: 16px;
}
.card-error { color: var(--color-incident-crimson); font-size: 0.85rem; }
.card-skeleton { color: var(--color-on-surface-variant); font-size: 0.85rem; }

.top-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
  margin: 0;
}
.meta-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.meta-item dt {
  font-family: var(--font-ui);
  font-size: 0.65rem;
  font-weight: 600;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.meta-item dd {
  margin: 0;
  font-family: var(--font-ui);
  font-size: 0.85rem;
  color: var(--color-on-surface);
}
.mono {
  font-family: var(--font-tech);
  font-size: 0.8rem;
}
</style>
