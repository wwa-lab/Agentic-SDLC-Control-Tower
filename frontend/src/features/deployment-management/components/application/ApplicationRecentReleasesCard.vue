<script setup lang="ts">
import type { RecentReleaseRow } from '../../types/application';
import type { SectionResult } from '@/shared/types/section';
import ReleaseVersionPill from '../primitives/ReleaseVersionPill.vue';
import ReleaseStateBadge from '../primitives/ReleaseStateBadge.vue';

defineProps<{ section: SectionResult<ReadonlyArray<RecentReleaseRow>> }>();
const emit = defineEmits<{ openRelease: [releaseId: string] }>();

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString();
}
</script>

<template>
  <div class="releases-card card">
    <div class="card-title">Recent Releases</div>
    <div v-if="section.error" class="card-error">{{ section.error }}</div>
    <div v-else-if="!section.data" class="card-skeleton">Loading recent releases...</div>
    <div v-else-if="section.data.length === 0" class="card-empty">No releases found.</div>
    <div v-else class="release-list">
      <button
        v-for="row in section.data"
        :key="row.releaseId"
        class="release-row"
        @click="emit('openRelease', row.releaseId)"
      >
        <ReleaseVersionPill
          :version="row.releaseVersion"
          :build-artifact-id="row.buildArtifactRef.buildArtifactId"
        />
        <ReleaseStateBadge :state="row.state" />
        <span class="release-date">{{ formatDate(row.createdAt) }}</span>
        <span class="story-count" :title="`${row.storyCount} stories`">
          {{ row.storyCount }} {{ row.storyCount === 1 ? 'story' : 'stories' }}
        </span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.releases-card { padding: 16px; }
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
.card-skeleton, .card-empty { color: var(--color-on-surface-variant); font-size: 0.85rem; }
.release-list { display: flex; flex-direction: column; }
.release-row {
  all: unset;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 4px;
  border-bottom: 1px solid var(--color-surface-container-high);
  cursor: pointer;
}
.release-row:hover { background: var(--color-surface-container-high); }
.release-row:last-child { border-bottom: none; }
.release-date {
  font-family: var(--font-ui);
  font-size: 0.75rem;
  color: var(--color-on-surface-variant);
  margin-left: auto;
}
.story-count {
  font-family: var(--font-tech);
  font-size: 0.7rem;
  color: var(--color-on-surface-variant);
  min-width: 60px;
  text-align: right;
}
</style>
