<script setup lang="ts">
import { FileText, Github } from 'lucide-vue-next';
import type { SddDocumentStage } from '../types/requirement';
import FreshnessChip from './FreshnessChip.vue';

const props = defineProps<{
  stage: SddDocumentStage;
  selected?: boolean;
  loading?: boolean;
}>();
const emit = defineEmits<{ open: [documentId: string] }>();

function openStage() {
  if (props.stage.id) {
    emit('open', props.stage.id);
  }
}
</script>

<template>
  <div
    class="stage-row"
    :class="{
      'stage-row--missing': stage.missing,
      'stage-row--clickable': !!stage.id,
      'stage-row--selected': selected,
    }"
    :role="stage.id ? 'button' : undefined"
    :tabindex="stage.id ? 0 : undefined"
    @click="openStage"
    @keydown.enter.prevent="openStage"
    @keydown.space.prevent="openStage"
  >
    <FileText :size="15" />
    <div class="stage-copy">
      <div class="stage-title">
        <span>{{ stage.title }}</span>
        <small v-if="stage.title !== stage.stageLabel" class="stage-kind">{{ stage.stageLabel }}</small>
        <FreshnessChip :status="stage.freshnessStatus" />
        <span v-if="loading" class="stage-loading">Loading...</span>
      </div>
      <span class="stage-path">{{ stage.path }}</span>
      <span v-if="!stage.missing" class="stage-meta">{{ stage.repoFullName }} · {{ stage.latestCommitSha }} · {{ stage.latestBlobSha }}</span>
    </div>
    <div class="stage-actions">
      <a v-if="stage.githubUrl" class="icon-btn" :href="stage.githubUrl" target="_blank" rel="noreferrer" title="Open in GitHub" @click.stop>
        <Github :size="14" />
      </a>
      <button v-if="stage.id" class="icon-btn" type="button" title="View Markdown" @click.stop="openStage">
        <FileText :size="14" />
      </button>
    </div>
  </div>
</template>

<style scoped>
.stage-row {
  display: grid;
  grid-template-columns: 18px minmax(0, 1fr) auto;
  gap: 10px;
  align-items: center;
  padding: 9px 10px;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container);
  color: var(--color-on-surface);
}

.stage-row--missing { opacity: 0.72; }
.stage-row--clickable {
  cursor: pointer;
  transition: border-color 0.2s ease, background 0.2s ease, transform 0.2s ease;
}
.stage-row--clickable:hover,
.stage-row--clickable:focus-visible {
  border-color: rgba(137, 206, 255, 0.45);
  background: var(--color-surface-container-high);
  outline: none;
}
.stage-row--selected {
  border-color: var(--color-secondary);
  background: var(--color-secondary-tint);
}
.stage-copy { display: flex; min-width: 0; flex-direction: column; gap: 3px; }
.stage-title { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; font-size: 0.75rem; font-weight: 600; }
.stage-loading {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  font-weight: 600;
  color: var(--color-secondary);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.stage-kind {
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--color-on-surface-variant);
  font-family: var(--font-tech);
  font-size: 0.5625rem;
  font-weight: 600;
}
.stage-path, .stage-meta {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: var(--font-tech);
  font-size: 0.5625rem;
  color: var(--color-on-surface-variant);
}
.stage-actions { display: flex; gap: 6px; }
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
</style>
