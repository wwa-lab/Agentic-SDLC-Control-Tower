<script setup lang="ts">
import type { SectionResult, RequirementHeader } from '../types/requirement';
import RequirementCard from './RequirementCard.vue';
import PriorityBadge from './PriorityBadge.vue';
import RequirementStatusBadge from './RequirementStatusBadge.vue';
import CategoryBadge from './CategoryBadge.vue';

interface Props {
  header: SectionResult<RequirementHeader>;
  isLoading?: boolean;
}

defineProps<Props>();

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('en-US', {
    year: 'numeric', month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
}
</script>

<template>
  <RequirementCard
    title="Requirement"
    :is-loading="isLoading"
    :error="header.error"
    :full-width="true"
  >
    <div v-if="header.data" class="header-content">
      <div class="header-top">
        <span class="req-id">{{ header.data.id }}</span>
        <h2 class="req-title">{{ header.data.title }}</h2>
      </div>
      <div class="header-badges">
        <PriorityBadge :priority="header.data.priority" />
        <RequirementStatusBadge :status="header.data.status" />
        <CategoryBadge :category="header.data.category" />
        <span class="source-badge">{{ header.data.source }}</span>
      </div>
      <div class="header-meta">
        <span class="meta-item">
          <span class="meta-label">Coverage</span>
          <span class="meta-value">{{ header.data.completenessScore }}%</span>
        </span>
        <span class="meta-item">
          <span class="meta-label">Stories</span>
          <span class="meta-value">{{ header.data.storyCount }}</span>
        </span>
        <span class="meta-item">
          <span class="meta-label">Specs</span>
          <span class="meta-value">{{ header.data.specCount }}</span>
        </span>
        <span class="meta-item">
          <span class="meta-label">Assignee</span>
          <span class="meta-value">{{ header.data.assignee }}</span>
        </span>
        <span class="meta-item">
          <span class="meta-label">Created</span>
          <span class="meta-value">{{ formatDate(header.data.createdAt) }}</span>
        </span>
        <span class="meta-item">
          <span class="meta-label">Updated</span>
          <span class="meta-value">{{ formatDate(header.data.updatedAt) }}</span>
        </span>
      </div>
    </div>
  </RequirementCard>
</template>

<style scoped>
.header-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.header-top {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.req-id {
  font-family: var(--font-tech);
  font-size: 0.875rem;
  color: var(--color-secondary);
  font-weight: 600;
}

.req-title {
  font-family: var(--font-ui);
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-on-surface);
  margin: 0;
}

.header-badges {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.source-badge {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  padding: 2px 8px;
  border-radius: 2px;
  color: var(--color-on-surface-variant);
  background: rgba(148, 163, 184, 0.1);
}

.header-meta {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.meta-label {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-on-surface-variant);
}

.meta-value {
  font-family: var(--font-ui);
  font-size: 0.75rem;
  color: var(--color-on-surface);
}
</style>
