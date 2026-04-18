<script setup lang="ts">
import type { ReportExportHistoryEntry } from '../../types';
import { formatTimestamp } from '../../utils';

defineProps<{
  entries: ReportExportHistoryEntry[];
  loading: boolean;
  error: string | null;
}>();

defineEmits<{
  retry: [];
}>();
</script>

<template>
  <section class="exports-list card-shell">
    <div class="exports-list__header">
      <div>
        <p class="text-label">Exports</p>
        <h3>Recent downloads</h3>
      </div>
      <button class="btn-machined" @click="$emit('retry')">Refresh</button>
    </div>

    <p v-if="loading" class="text-body-sm">Loading recent exports…</p>
    <p v-else-if="error" class="text-body-sm">{{ error }}</p>
    <p v-else-if="entries.length === 0" class="text-body-sm">No export jobs in the 7-day window.</p>

    <div v-for="entry in entries" v-else :key="entry.exportId" class="export-row">
      <div>
        <strong>{{ entry.reportName }}</strong>
        <p class="text-body-sm">{{ entry.format.toUpperCase() }} · {{ entry.status }}</p>
      </div>
      <div class="export-row__meta">
        <span class="text-body-sm">{{ formatTimestamp(entry.createdAt) }}</span>
        <a v-if="entry.downloadUrl" class="export-row__link" :href="entry.downloadUrl" target="_blank" rel="noreferrer">
          Download
        </a>
      </div>
    </div>
  </section>
</template>

<style scoped>
.card-shell {
  display: grid;
  gap: 14px;
  border: var(--border-ghost);
  border-radius: 16px;
  background: linear-gradient(180deg, color-mix(in srgb, var(--surface-report) 90%, transparent), var(--color-surface-container-low));
  padding: 18px;
}

.exports-list__header,
.export-row {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: center;
}

.export-row {
  border: var(--border-ghost);
  border-radius: 12px;
  background: color-mix(in srgb, var(--color-surface-container-high) 84%, transparent);
  padding: 14px;
}

.export-row__meta {
  display: grid;
  gap: 6px;
  justify-items: end;
}

.export-row__link {
  color: var(--color-secondary);
  font-size: 0.875rem;
  text-decoration: none;
}
</style>
