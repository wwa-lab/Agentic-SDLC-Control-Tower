<script setup lang="ts">
import type { ApplicationHeader } from '../../types/application';
import type { SectionResult } from '@/shared/types/section';
import JenkinsLinkOut from '../primitives/JenkinsLinkOut.vue';

defineProps<{ section: SectionResult<ApplicationHeader> }>();

function formatDate(iso: string): string {
  return new Date(iso).toLocaleString();
}
</script>

<template>
  <div class="header-card card">
    <div v-if="section.error" class="card-error">{{ section.error }}</div>
    <div v-else-if="!section.data" class="card-skeleton">Loading application header...</div>
    <template v-else>
      <div class="top-row">
        <h2 class="app-name">{{ section.data.name }}</h2>
        <JenkinsLinkOut
          :url="section.data.jenkinsFolderUrl"
          :label="section.data.jenkinsFolderPath"
        />
      </div>
      <div class="meta-row">
        <span class="meta-item">
          <span class="meta-label">Project</span>
          <span class="meta-value">{{ section.data.projectId }}</span>
        </span>
        <span class="meta-item">
          <span class="meta-label">Runtime</span>
          <span class="meta-value runtime">{{ section.data.runtimeLabel }}</span>
        </span>
        <span v-if="section.data.lastDeployAt" class="meta-item">
          <span class="meta-label">Last Deploy</span>
          <span class="meta-value">{{ formatDate(section.data.lastDeployAt) }}</span>
        </span>
      </div>
      <p v-if="section.data.description" class="description">
        {{ section.data.description }}
      </p>
    </template>
  </div>
</template>

<style scoped>
.header-card { padding: 16px; }
.card {
  background: var(--color-surface-container-low);
  border: var(--border-ghost);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
}
.card-error { color: var(--color-incident-crimson); font-size: 0.85rem; }
.card-skeleton { color: var(--color-on-surface-variant); font-size: 0.85rem; }
.top-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}
.app-name {
  margin: 0;
  font-family: var(--font-ui);
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--color-on-surface);
}
.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 8px;
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
.runtime {
  font-family: var(--font-tech);
}
.description {
  margin: 8px 0 0;
  font-family: var(--font-ui);
  font-size: 0.85rem;
  line-height: 1.5;
  color: var(--color-on-surface-variant);
}
</style>
