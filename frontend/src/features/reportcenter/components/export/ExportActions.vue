<script setup lang="ts">
import { ref } from 'vue';
import { useReportCenterStore } from '../../stores/reportCenterStore';
import type { ExportFormat, ReportRunRequest } from '../../types';

const props = defineProps<{
  request: ReportRunRequest;
  reportKey: string;
  disabled?: boolean;
}>();

const emit = defineEmits<{
  exported: [payload: { exportId: string; format: ExportFormat }];
}>();

const store = useReportCenterStore();
const submitting = ref<ExportFormat | null>(null);

async function requestExport(format: ExportFormat) {
  submitting.value = format;
  try {
    const job = await store.requestExport(props.reportKey, props.request, format);
    emit('exported', { exportId: job.id, format });
  } finally {
    submitting.value = null;
  }
}
</script>

<template>
  <div class="export-actions">
    <button class="btn-machined" :disabled="disabled || Boolean(submitting)" @click="requestExport('csv')">
      {{ submitting === 'csv' ? 'Preparing CSV…' : 'Export CSV' }}
    </button>
    <button class="btn-machined" :disabled="disabled || Boolean(submitting)" @click="requestExport('pdf')">
      {{ submitting === 'pdf' ? 'Preparing PDF…' : 'Export PDF' }}
    </button>
  </div>
</template>

<style scoped>
.export-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
