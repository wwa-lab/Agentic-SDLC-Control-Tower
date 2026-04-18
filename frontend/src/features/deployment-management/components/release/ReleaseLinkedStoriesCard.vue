<script setup lang="ts">
import type { SectionResult } from '@/shared/types/section';
import type { StoryChip as StoryChipType } from '../../types/traceability';
import StoryChip from '../primitives/StoryChip.vue';

defineProps<{
  section: SectionResult<ReadonlyArray<StoryChipType>>;
}>();
</script>

<template>
  <div class="linked-stories-card card">
    <div class="card-title">Linked Stories</div>

    <div v-if="section.error" class="card-error">{{ section.error }}</div>
    <div v-else-if="!section.data" class="card-skeleton">Loading linked stories...</div>
    <div v-else-if="section.data.length === 0" class="card-empty">No stories linked to this release.</div>
    <div v-else class="chip-list">
      <StoryChip
        v-for="chip in section.data"
        :key="chip.storyId"
        :chip="chip"
      />
    </div>
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

.chip-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
