<script setup lang="ts">
import type { SectionResult } from '@/shared/types/section';
import type { EnvironmentHeader } from '../../types/environment';
import EnvironmentChip from '../primitives/EnvironmentChip.vue';

defineProps<{ section: SectionResult<EnvironmentHeader> }>();
</script>

<template>
  <div class="header-card card">
    <div v-if="section.error" class="card-error">{{ section.error }}</div>
    <div v-else-if="!section.data" class="card-skeleton">Loading environment header...</div>
    <template v-else>
      <div class="top-row">
        <EnvironmentChip :name="section.data.environmentName" :kind="section.data.kind" />
        <span class="kind-label">{{ section.data.kind }}</span>
      </div>
      <div class="meta-row">
        <span class="meta-item">
          <span class="meta-label">Application</span>
          <span class="meta-value tech">{{ section.data.applicationId }}</span>
        </span>
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
.card-skeleton { color: var(--color-on-surface-variant); font-size: 0.85rem; }
.top-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.kind-label {
  font-family: var(--font-ui);
  font-size: 0.7rem;
  font-weight: 600;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}
.meta-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.meta-label {
  font-family: var(--font-ui);
  font-size: 0.65rem;
  font-weight: 700;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.meta-value {
  font-family: var(--font-ui);
  font-size: 0.85rem;
  color: var(--color-on-surface);
}
.tech {
  font-family: var(--font-tech);
}
</style>
