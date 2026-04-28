<script setup lang="ts">
import { computed } from 'vue';
import { ExternalLink, RefreshCw } from 'lucide-vue-next';
import type { SectionResult, LinkedStoriesSection, SourceReference } from '../types/requirement';
import RequirementCard from './RequirementCard.vue';
import FreshnessChip from './FreshnessChip.vue';

interface Props {
  requirementId: string;
  linkedStories: SectionResult<LinkedStoriesSection>;
  jiraSources?: ReadonlyArray<SourceReference>;
  isLoading?: boolean;
  isRefreshing?: boolean;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  navigate: [path: string];
  refreshJira: [sourceId: string];
}>();

const STATUS_COLORS: Record<string, string> = {
  Draft: 'status--muted',
  Ready: 'status--amber',
  'In Progress': 'status--amber',
  Done: 'status--green',
};

const primaryJiraSource = computed(() => props.jiraSources?.find(source => source.sourceType === 'JIRA') ?? null);

const jiraLinkTarget = computed(() => {
  const rawUrl = primaryJiraSource.value?.url?.trim();
  if (!rawUrl) return null;

  try {
    const parsed = new URL(rawUrl);
    const isHttp = parsed.protocol === 'http:' || parsed.protocol === 'https:';
    const isPlaceholderHost = parsed.hostname === 'example.com' || parsed.hostname.endsWith('.example.com');
    return isHttp && !isPlaceholderHost ? rawUrl : null;
  } catch {
    return null;
  }
});

const syncLabel = computed(() => {
  const fetchedAt = primaryJiraSource.value?.fetchedAt;
  if (!primaryJiraSource.value) return 'No Jira source linked';
  if (!fetchedAt) return 'Not synced yet';
  return `Last synced ${formatSyncTime(fetchedAt)}`;
});

function formatSyncTime(value: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString(undefined, {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function refreshJira() {
  const sourceId = primaryJiraSource.value?.id;
  if (sourceId) emit('refreshJira', sourceId);
}
</script>

<template>
  <RequirementCard
    title="Jira Stories"
    :is-loading="isLoading"
    :error="linkedStories.error"
  >
    <div v-if="linkedStories.data" class="stories-content">
      <div class="jira-sync">
        <div class="jira-sync-copy">
          <span class="sync-kicker">Synced from Jira</span>
          <span class="sync-label">{{ syncLabel }}</span>
        </div>
        <div class="jira-sync-actions">
          <FreshnessChip v-if="primaryJiraSource" :status="primaryJiraSource.freshnessStatus" />
          <a
            v-if="jiraLinkTarget"
            class="icon-btn"
            :href="jiraLinkTarget"
            target="_blank"
            rel="noreferrer"
            title="Open source in Jira"
          >
            <ExternalLink :size="14" />
          </a>
          <button
            class="icon-btn"
            type="button"
            title="Refresh Jira stories"
            :disabled="!primaryJiraSource || isRefreshing"
            @click="refreshJira"
          >
            <RefreshCw :size="14" />
          </button>
        </div>
      </div>

      <div class="stories-header">
        <span class="stories-count">{{ linkedStories.data.totalCount }} Jira stories</span>
      </div>

      <div v-if="linkedStories.data.stories.length === 0" class="empty">
        No Jira stories linked yet
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
          <span class="story-source">Jira status</span>
          <span class="story-status" :class="STATUS_COLORS[story.status]">{{ story.status }}</span>
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

.jira-sync {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
}

.jira-sync-copy {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 3px;
}

.sync-kicker {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.sync-label {
  overflow: hidden;
  color: var(--color-on-surface);
  font-family: var(--font-tech);
  font-size: 0.6875rem;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.jira-sync-actions {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 6px;
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

.story-source {
  color: var(--color-on-surface-variant);
  font-family: var(--font-tech);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
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

.icon-btn {
  display: inline-grid;
  place-items: center;
  width: 28px;
  height: 28px;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-on-surface-variant);
  cursor: pointer;
}

.icon-btn:hover:not(:disabled) {
  color: var(--color-secondary);
  border-color: var(--color-secondary);
}

.icon-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
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
