<script setup lang="ts">
import type { CatalogCategoryGroup } from '../../types';
import ReportCard from './ReportCard.vue';

defineProps<{
  group: CatalogCategoryGroup;
}>();

defineEmits<{
  open: [reportKey: string];
}>();
</script>

<template>
  <section class="report-group">
    <div class="report-group__header">
      <div>
        <p class="text-label">{{ group.category }}</p>
        <h3>{{ group.label }}</h3>
      </div>
      <span class="text-body-sm">{{ group.reports.length > 0 ? `${group.reports.length} reports` : 'Coming soon' }}</span>
    </div>

    <div class="report-group__grid">
      <ReportCard
        v-for="report in group.reports.length > 0 ? group.reports : [null]"
        :key="report?.reportKey ?? `${group.category}-placeholder`"
        :report="report"
        :category-label="group.label"
        @open="$emit('open', $event)"
      />
    </div>
  </section>
</template>

<style scoped>
.report-group {
  display: grid;
  gap: 14px;
}

.report-group__header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: end;
}

.report-group__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 14px;
}
</style>
