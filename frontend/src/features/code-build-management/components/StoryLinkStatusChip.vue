<script setup lang="ts">
import { computed } from 'vue';
import type { StoryLinkStatus } from '../types';

interface Props {
  status: StoryLinkStatus;
}

const props = defineProps<Props>();

const description = computed(() => {
  switch (props.status) {
    case 'KNOWN':
      return 'Story trailer resolved to a known requirement.';
    case 'UNKNOWN_STORY':
      return 'A story id was found, but it does not currently resolve in Requirement Management.';
    case 'AMBIGUOUS':
      return 'More than one distinct story trailer was detected.';
    default:
      return 'No Story-Id or Relates-to trailer was found.';
  }
});
</script>

<template>
  <span class="story-chip" :class="`story-chip--${status.toLowerCase()}`" :title="description">
    {{ status }}
  </span>
</template>

<style scoped>
.story-chip {
  display: inline-flex;
  align-items: center;
  padding: 4px 9px;
  border-radius: 999px;
  font-size: 0.72rem;
  font-weight: 700;
  border: 1px solid transparent;
}

.story-chip--known {
  color: var(--cb-link-ok);
  background: rgba(78, 222, 163, 0.12);
}

.story-chip--unknown_story {
  color: var(--cb-link-unknown);
  background: rgba(245, 158, 11, 0.12);
}

.story-chip--no_story_id {
  color: var(--cb-link-stale);
  background: rgba(148, 163, 184, 0.16);
}

.story-chip--ambiguous {
  color: var(--cb-severity-major);
  background: rgba(255, 180, 171, 0.12);
}
</style>

