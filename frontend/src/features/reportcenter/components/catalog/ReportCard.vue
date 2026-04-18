<script setup lang="ts">
import type { ReportDefinitionDto } from '../../types';

const props = defineProps<{
  report?: ReportDefinitionDto | null;
  categoryLabel: string;
}>();

defineEmits<{
  open: [reportKey: string];
}>();

const isPlaceholder = !props.report || props.report.status === 'coming-soon';
</script>

<template>
  <button
    class="report-card"
    :class="{ 'report-card--placeholder': isPlaceholder }"
    :disabled="isPlaceholder"
    @click="report && $emit('open', report.reportKey)"
  >
    <div class="report-card__header">
      <span class="text-label">{{ categoryLabel }}</span>
      <span class="report-card__status">{{ isPlaceholder ? 'Coming soon' : 'Ready' }}</span>
    </div>

    <div class="report-card__body">
      <strong>{{ report?.name ?? `${categoryLabel} reports` }}</strong>
      <p class="text-body-sm">
        {{ report?.description ?? `This category is defined in the spec and reserved for the next milestone.` }}
      </p>
    </div>

    <div class="report-card__footer">
      <span class="text-tech">{{ report?.chartType ?? 'future-slice' }}</span>
      <span>{{ isPlaceholder ? 'Waiting for V2' : 'Open report' }}</span>
    </div>
  </button>
</template>

<style scoped>
.report-card {
  width: 100%;
  border: var(--border-ghost);
  border-radius: 16px;
  background:
    radial-gradient(circle at top right, color-mix(in srgb, var(--accent-history) 16%, transparent), transparent 42%),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-report) 92%, transparent), var(--color-surface-container-low));
  color: var(--color-on-surface);
  padding: 18px;
  display: grid;
  gap: 16px;
  text-align: left;
  cursor: pointer;
  box-shadow: var(--shadow-card);
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
}

.report-card:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card-hover);
}

.report-card:disabled {
  cursor: default;
  opacity: 0.72;
}

.report-card--placeholder {
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--color-surface-container-high) 88%, transparent), var(--color-surface-container-low));
}

.report-card__header,
.report-card__footer {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.report-card__status {
  border-radius: 999px;
  padding: 4px 10px;
  background: var(--accent-history-bg);
  color: var(--accent-history);
  font-size: 0.75rem;
  font-weight: 600;
}

.report-card__body {
  display: grid;
  gap: 8px;
}

.report-card__body strong {
  font-size: 1rem;
}

.report-card__footer {
  font-size: 0.75rem;
  color: var(--color-on-surface-variant);
}
</style>
