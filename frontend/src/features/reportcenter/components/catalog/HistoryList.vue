<script setup lang="ts">
import type { ReportRunHistoryEntry } from '../../types';
import { formatTimestamp } from '../../utils';

defineProps<{
  entries: ReportRunHistoryEntry[];
  loading: boolean;
  error: string | null;
}>();

defineEmits<{
  open: [entry: ReportRunHistoryEntry];
  retry: [];
}>();
</script>

<template>
  <section class="history-list card-shell">
    <div class="history-list__header">
      <div>
        <p class="text-label">History</p>
        <h3>Recent runs</h3>
      </div>
      <button class="btn-machined" @click="$emit('retry')">Refresh</button>
    </div>

    <p v-if="loading" class="text-body-sm">Loading recent report runs…</p>
    <p v-else-if="error" class="text-body-sm">{{ error }}</p>
    <p v-else-if="entries.length === 0" class="text-body-sm">No report runs yet for this caller.</p>

    <button
      v-for="entry in entries"
      v-else
      :key="entry.runId"
      class="history-row"
      @click="$emit('open', entry)"
    >
      <div>
        <strong>{{ entry.reportName }}</strong>
        <p class="text-body-sm">{{ entry.scopeSummary }} · {{ entry.grouping }}</p>
      </div>
      <div class="history-row__meta">
        <span class="text-body-sm">{{ entry.timeRangeLabel }}</span>
        <span class="text-body-sm">{{ formatTimestamp(entry.runAt) }}</span>
      </div>
    </button>
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

.history-list__header,
.history-row {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: center;
}

.history-row {
  border: var(--border-ghost);
  border-radius: 12px;
  background: color-mix(in srgb, var(--color-surface-container-high) 84%, transparent);
  padding: 14px;
  color: inherit;
  text-align: left;
  cursor: pointer;
}

.history-row__meta {
  display: grid;
  gap: 4px;
  justify-items: end;
}
</style>
