<script setup lang="ts">
import type { StoryChip as StoryChipType } from '../../types/traceability';

defineProps<{ chip: StoryChipType }>();

const COLOR_MAP: Record<StoryChipType['status'], string> = {
  VERIFIED: 'var(--dp-state-succeeded)',
  UNVERIFIED: 'var(--color-approval-amber)',
  UNKNOWN_STORY: 'var(--dp-state-cancelled)',
};
</script>

<template>
  <router-link
    v-if="chip.status !== 'UNKNOWN_STORY'"
    :to="`/requirements/${chip.storyId}`"
    class="story-chip"
    :style="{ '--chip-color': COLOR_MAP[chip.status] }"
    :title="chip.title ?? chip.storyId"
  >
    {{ chip.storyId }}
  </router-link>
  <span
    v-else
    class="story-chip unknown"
    :style="{ '--chip-color': COLOR_MAP[chip.status] }"
    :title="`Unknown story: ${chip.storyId}`"
  >
    {{ chip.storyId }}
  </span>
</template>

<style scoped>
.story-chip {
  display: inline-block;
  padding: 1px 6px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--chip-color);
  color: var(--chip-color);
  font-family: var(--font-tech);
  font-size: 0.7rem;
  font-weight: 500;
  text-decoration: none;
  white-space: nowrap;
}
.story-chip:hover:not(.unknown) {
  background: rgba(255, 255, 255, 0.05);
}
.unknown {
  opacity: 0.6;
  cursor: default;
}
</style>
