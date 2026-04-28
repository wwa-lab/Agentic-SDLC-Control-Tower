<script setup lang="ts">
import { computed } from 'vue';
import { RefreshCw, ExternalLink } from 'lucide-vue-next';
import type { SourceReference } from '../types/requirement';
import FreshnessChip from './FreshnessChip.vue';

const props = defineProps<{ source: SourceReference }>();
defineEmits<{ refresh: [sourceId: string] }>();

const linkTarget = computed(() => {
  const rawUrl = props.source.url?.trim();
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

const sourceMeta = computed(() => props.source.externalId || props.source.url || 'No source id');
</script>

<template>
  <article class="source-card">
    <div class="source-main">
      <span class="source-type">{{ source.sourceType }}</span>
      <div class="source-copy">
        <a v-if="linkTarget" class="source-title" :href="linkTarget" target="_blank" rel="noreferrer">
          {{ source.title }}
          <ExternalLink :size="12" />
        </a>
        <span v-else class="source-title source-title--plain">
          {{ source.title }}
          <span class="source-link-state">Link not configured</span>
        </span>
        <span class="source-meta">{{ sourceMeta }}</span>
      </div>
    </div>
    <div class="source-actions">
      <FreshnessChip :status="source.freshnessStatus" />
      <button class="icon-btn" type="button" title="Refresh source metadata" @click="$emit('refresh', source.id)">
        <RefreshCw :size="14" />
      </button>
    </div>
    <p v-if="source.errorMessage" class="source-error">{{ source.errorMessage }}</p>
  </article>
</template>

<style scoped>
.source-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container);
}

.source-main, .source-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.source-type {
  min-width: 76px;
  font-family: var(--font-tech);
  font-size: 0.6875rem;
  color: var(--color-secondary);
}

.source-copy {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
  gap: 2px;
}

.source-title {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  max-width: 100%;
  color: var(--color-on-surface);
  font-family: var(--font-ui);
  font-size: 0.8125rem;
  text-decoration: none;
}

.source-title--plain {
  color: var(--color-on-surface);
}

.source-link-state {
  flex-shrink: 0;
  padding: 2px 5px;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  color: var(--color-on-surface-variant);
  font-family: var(--font-tech);
  font-size: 0.625rem;
  text-transform: uppercase;
}

.source-meta {
  overflow: hidden;
  color: var(--color-on-surface-variant);
  font-family: var(--font-tech);
  font-size: 0.6875rem;
  text-overflow: ellipsis;
  white-space: nowrap;
}

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

.icon-btn:hover { color: var(--color-secondary); border-color: var(--color-secondary); }
.source-error { margin: 0; color: var(--color-incident-crimson); font-size: 0.6875rem; }
</style>
