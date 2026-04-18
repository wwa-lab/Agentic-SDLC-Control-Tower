<script setup lang="ts">
import { computed } from 'vue';
import { useReportCenterStore } from '../../stores/reportCenterStore';

const store = useReportCenterStore();

const statusText = computed(() => {
  const job = store.exportJob;
  if (!job) {
    return null;
  }
  if (job.status === 'ready') {
    return `${job.format.toUpperCase()} ready`;
  }
  if (job.status === 'failed') {
    return job.errorMessage ?? 'Export failed';
  }
  return `Generating ${job.format.toUpperCase()}…`;
});
</script>

<template>
  <teleport to="body">
    <div v-if="store.exportJob && statusText" class="export-toast">
      <strong>{{ statusText }}</strong>
      <a
        v-if="store.exportJob.downloadUrl && store.exportJob.status === 'ready'"
        :href="store.exportJob.downloadUrl"
        target="_blank"
        rel="noreferrer"
      >
        Download
      </a>
      <p v-else-if="store.exportJob.errorMessage" class="text-body-sm">{{ store.exportJob.errorMessage }}</p>
    </div>
  </teleport>
</template>

<style scoped>
.export-toast {
  position: fixed;
  right: 20px;
  bottom: 20px;
  display: grid;
  gap: 8px;
  min-width: 240px;
  padding: 14px 16px;
  border-radius: 14px;
  border: var(--border-ghost);
  background: linear-gradient(180deg, color-mix(in srgb, var(--surface-report) 92%, transparent), var(--color-surface-container-high));
  box-shadow: var(--shadow-card-hover);
  z-index: 30;
}

.export-toast a {
  color: var(--color-secondary);
  text-decoration: none;
}
</style>
