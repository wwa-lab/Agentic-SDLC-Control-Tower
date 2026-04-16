<script setup lang="ts">
import type { SectionResult, LinkedStoriesSection } from '../types/requirement';
import RequirementCard from './RequirementCard.vue';

interface Props {
  requirementId: string;
  linkedStories: SectionResult<LinkedStoriesSection>;
  isLoading?: boolean;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  generateStories: [];
  navigate: [path: string];
}>();

const STATUS_COLORS: Record<string, string> = {
  Draft: 'status--muted',
  Ready: 'status--amber',
  'In Progress': 'status--amber',
  Done: 'status--green',
};
</script>

<template>
  <RequirementCard
    title="Linked Stories"
    :is-loading="isLoading"
    :error="linkedStories.error"
  >
    <div v-if="linkedStories.data" class="stories-content">
      <div class="stories-header">
        <span class="stories-count">{{ linkedStories.data.totalCount }} stories</span>
        <button class="action-btn" @click="emit('generateStories')">Generate Stories</button>
      </div>

      <div v-if="linkedStories.data.stories.length === 0" class="empty">
        No stories derived yet
      </div>

      <div v-else class="stories-list">
        <div
          v-for="story in linkedStories.data.stories"
          :key="story.id"
          :id="`story-${story.id}`"
          class="story-item"
          @click="emit('navigate', `/requirements/${props.requirementId}#story-${story.id}`)"
        >
          <span class="story-id">{{ story.id }}</span>
          <span class="story-title">{{ story.title }}</span>
          <span class="story-status" :class="STATUS_COLORS[story.status]">{{ story.status }}</span>
          <span v-if="story.specId" class="spec-link">
            → {{ story.specId }}
          </span>
        </div>
      </div>
    </div>
  </RequirementCard>
</template>

<style scoped>
.stories-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.stories-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stories-count {
  font-family: var(--font-tech);
  font-size: 0.625rem;
  color: var(--color-on-surface-variant);
}

.action-btn {
  background: none;
  border: 1px solid var(--color-secondary);
  color: var(--color-secondary);
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background: var(--color-secondary);
  color: var(--color-on-secondary-container);
}

.stories-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.story-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background 0.15s ease;
}

.story-item:hover { background: var(--nav-hover-bg); }

.story-id {
  font-family: var(--font-tech);
  font-size: 0.625rem;
  color: var(--color-secondary);
  min-width: 50px;
}

.story-title {
  font-family: var(--font-ui);
  font-size: 0.6875rem;
  color: var(--color-on-surface);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.story-status {
  font-family: var(--font-ui);
  font-size: 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  padding: 1px 6px;
  border-radius: 2px;
}

.status--green { color: var(--color-health-emerald); background: rgba(78, 222, 163, 0.1); }
.status--amber { color: var(--color-approval-amber); background: rgba(245, 158, 11, 0.1); }
.status--muted { color: var(--color-on-surface-variant); background: rgba(148, 163, 184, 0.1); }

.spec-link {
  font-family: var(--font-tech);
  font-size: 0.5625rem;
  color: var(--color-secondary);
  opacity: 0.7;
}

.empty {
  font-family: var(--font-ui);
  font-size: 0.75rem;
  color: var(--color-on-surface-variant);
  opacity: 0.5;
  text-align: center;
  padding: 16px 0;
}
</style>
