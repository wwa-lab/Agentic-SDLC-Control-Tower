<script setup lang="ts">
import { computed } from 'vue';
import type { SectionResult } from '@/shared/types/section';
import type { TraceabilityReleaseRow } from '../../types/traceability';
import ReleaseVersionPill from '../primitives/ReleaseVersionPill.vue';
import ReleaseStateBadge from '../primitives/ReleaseStateBadge.vue';

const props = defineProps<{
  section: SectionResult<ReadonlyArray<TraceabilityReleaseRow>>;
}>();
const emit = defineEmits<{ openRelease: [releaseId: string] }>();

interface AppGroup {
  readonly applicationId: string;
  readonly applicationName: string;
  readonly releases: ReadonlyArray<TraceabilityReleaseRow>;
}

const grouped = computed<ReadonlyArray<AppGroup>>(() => {
  const data = props.section.data;
  if (!data) return [];

  const map = new Map<string, { applicationName: string; releases: TraceabilityReleaseRow[] }>();
  for (const row of data) {
    const existing = map.get(row.applicationId);
    if (existing) {
      existing.releases.push(row);
    } else {
      map.set(row.applicationId, { applicationName: row.applicationName, releases: [row] });
    }
  }

  return Array.from(map.entries()).map(([applicationId, value]) => ({
    applicationId,
    applicationName: value.applicationName,
    releases: value.releases,
  }));
});

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString();
}
</script>

<template>
  <div class="releases-card card">
    <div v-if="section.error" class="card-error">{{ section.error }}</div>
    <div v-else-if="!section.data" class="card-skeleton">Loading releases...</div>
    <div v-else-if="section.data.length === 0" class="card-empty">No releases found for this story.</div>
    <template v-else>
      <h3 class="card-title">Releases</h3>
      <div v-for="group in grouped" :key="group.applicationId" class="app-group">
        <div class="group-header">{{ group.applicationName }}</div>
        <ul class="release-list">
          <li
            v-for="row in group.releases"
            :key="row.releaseId"
            class="release-row"
          >
            <button class="row-button" @click="emit('openRelease', row.releaseId)">
              <ReleaseVersionPill :version="row.releaseVersion" />
              <ReleaseStateBadge :state="row.state" />
              <span class="release-date">{{ formatDate(row.createdAt) }}</span>
            </button>
          </li>
        </ul>
      </div>
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
.card-skeleton, .card-empty { color: var(--color-on-surface-variant); font-size: 0.85rem; }
.card-title {
  margin: 0 0 12px;
  font-family: var(--font-ui);
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.app-group {
  margin-bottom: 12px;
}
.app-group:last-child {
  margin-bottom: 0;
}
.group-header {
  font-family: var(--font-ui);
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding-bottom: 6px;
  border-bottom: 1px solid var(--border-separator);
  margin-bottom: 6px;
}
.release-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.release-row {
  border-bottom: 1px solid var(--border-separator);
}
.release-row:last-child {
  border-bottom: none;
}
.row-button {
  all: unset;
  cursor: pointer;
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 4px;
  border-radius: var(--radius-sm);
  transition: background 0.15s;
}
.row-button:hover {
  background: var(--nav-hover-bg);
}
.release-date {
  font-family: var(--font-tech);
  font-size: 0.7rem;
  color: var(--color-on-surface-variant);
  margin-left: auto;
}
</style>
