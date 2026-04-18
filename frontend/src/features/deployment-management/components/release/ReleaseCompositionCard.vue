<script setup lang="ts">
import type { SectionResult } from '@/shared/types/section';
import type { ReleaseCommitRow } from '../../types/release';
import StoryChip from '../primitives/StoryChip.vue';

defineProps<{
  section: SectionResult<ReadonlyArray<ReleaseCommitRow>>;
  capNotice?: { kind: string; appliedCommitCap: number };
}>();

function formatDate(iso: string): string {
  return new Date(iso).toLocaleString();
}
</script>

<template>
  <div class="composition-card card">
    <div class="card-title">Commits</div>

    <div v-if="capNotice" class="cap-banner">
      Showing first {{ capNotice.appliedCommitCap }} commits ({{ capNotice.kind }}).
    </div>

    <div v-if="section.error" class="card-error">{{ section.error }}</div>
    <div v-else-if="!section.data" class="card-skeleton">Loading commits...</div>
    <div v-else-if="section.data.length === 0" class="card-empty">No commits in this release.</div>
    <table v-else class="commit-table">
      <thead>
        <tr>
          <th>SHA</th>
          <th>Author</th>
          <th>Message</th>
          <th>Date</th>
          <th>Stories</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in section.data" :key="row.sha">
          <td class="mono">{{ row.shortSha }}</td>
          <td class="author">{{ row.author }}</td>
          <td class="message">{{ row.message }}</td>
          <td class="date">{{ formatDate(row.committedAt) }}</td>
          <td class="stories">
            <StoryChip
              v-for="sid in row.storyIds"
              :key="sid"
              :chip="{ storyId: sid, status: 'VERIFIED' }"
            />
            <span v-if="row.storyIds.length === 0" class="no-stories">&mdash;</span>
          </td>
        </tr>
      </tbody>
    </table>
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

.cap-banner {
  padding: 8px 12px;
  margin-bottom: 12px;
  border-radius: var(--radius-sm);
  background: rgba(245, 158, 11, 0.1);
  color: var(--color-approval-amber);
  font-family: var(--font-ui);
  font-size: 0.8rem;
}

.commit-table {
  width: 100%;
  border-collapse: collapse;
  font-family: var(--font-ui);
  font-size: 0.8rem;
}
.commit-table th {
  text-align: left;
  padding: 6px 8px;
  font-size: 0.65rem;
  font-weight: 600;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
  letter-spacing: 0.04em;
  border-bottom: var(--border-ghost);
}
.commit-table td {
  padding: 6px 8px;
  border-bottom: 1px solid var(--border-separator);
  color: var(--color-on-surface);
  vertical-align: middle;
}
.mono { font-family: var(--font-tech); font-size: 0.75rem; }
.author { white-space: nowrap; }
.message { max-width: 320px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.date { font-size: 0.75rem; color: var(--color-on-surface-variant); white-space: nowrap; }
.stories { display: flex; flex-wrap: wrap; gap: 4px; }
.no-stories { color: var(--color-on-surface-variant); }
</style>
