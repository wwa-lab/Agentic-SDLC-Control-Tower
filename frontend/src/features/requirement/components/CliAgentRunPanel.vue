<script setup lang="ts">
import { computed, ref } from 'vue';
import { CheckCircle2, Clipboard, FileText, Play, RefreshCw, RotateCcw, Terminal, X } from 'lucide-vue-next';
import type { AgentRun, PipelineProfile, SddDocumentIndex, SourceReference } from '../types/requirement';
import RequirementCard from './RequirementCard.vue';

const props = defineProps<{
  profile: PipelineProfile;
  documents: SddDocumentIndex | null;
  sources: ReadonlyArray<SourceReference>;
  agentRuns: ReadonlyArray<AgentRun>;
  isLoading?: boolean;
}>();

const emit = defineEmits<{
  prepareRun: [skillKey: string, targetStage: string];
  refreshStatus: [];
  refreshSource: [sourceId: string];
  openDocument: [documentId: string];
  refreshDocuments: [];
  confirmMerge: [executionId: string, prUrl: string];
}>();

const copied = ref(false);
const showMergeForm = ref(false);
const mergePrUrl = ref('');
const mergeError = ref<string | null>(null);

const profileRuns = computed(() => props.agentRuns.filter(run => run.profileId === props.profile.id));
const latestRun = computed(() => profileRuns.value[0] ?? null);
const latestEvents = computed(() => latestRun.value?.stageEvents ?? []);
const cliPrompt = computed(() => latestRun.value?.command ?? null);
const isMerged = computed(() => latestRun.value?.status === 'COMPLETED' || latestEvents.value.some(event => event.state === 'DONE' || event.state === 'COMPLETED'));

const sourceIssue = computed(() => props.sources.find(source => !['FRESH', 'UNKNOWN'].includes(source.freshnessStatus)) ?? null);
const staleDocument = computed(() => props.documents?.stages.find(stage => !stage.missing && ['DOCUMENT_CHANGED_AFTER_REVIEW', 'SOURCE_CHANGED', 'ERROR'].includes(stage.freshnessStatus)) ?? null);
const missingDocument = computed(() => props.documents?.stages.find(stage => stage.missing || stage.freshnessStatus === 'MISSING_DOCUMENT') ?? null);
const fallbackStage = computed(() => props.documents?.stages[0] ?? props.profile.documentStages[0] ?? null);

const actionStage = computed(() => missingDocument.value ?? staleDocument.value ?? fallbackStage.value);
const targetStage = computed(() => actionStage.value?.sddType ?? props.profile.chainNodes.find(node => node.isExecutionHub)?.id ?? 'spec');
const targetStageLabel = computed(() => {
  const stage = actionStage.value;
  if (!stage) return targetStage.value;
  return 'stageLabel' in stage ? stage.stageLabel : stage.label;
});

const selectedSkill = computed(() => {
  const stageId = targetStage.value;
  return props.profile.skills.find(skill => skill.triggerPoint === stageId)
    ?? props.profile.skills.find(skill => skill.skillId.includes(stageId))
    ?? props.profile.skills.find(skill => skill.triggerPoint === 'any-stage')
    ?? (props.profile.id === 'standard-sdd' && stageId === 'architecture'
      ? { skillId: 'architecture-blueprint', label: 'Architecture Blueprint', triggerPoint: 'architecture' }
      : null)
    ?? null;
});

const nextAction = computed(() => {
  if (isMerged.value) {
    return {
      kind: 'refresh-documents',
      kicker: 'GitHub Sync',
      title: 'Refresh GitHub Docs',
      description: 'PR merge has been confirmed. Refresh the SDD index so the new documents become visible here.',
      primaryLabel: 'Refresh GitHub',
    };
  }
  if (sourceIssue.value) {
    return {
      kind: 'refresh-source',
      kicker: 'Source Blocker',
      title: `Refresh ${sourceIssue.value.sourceType} source`,
      description: `${sourceIssue.value.title} changed or could not be verified. Downstream document work is paused until the source is current.`,
      primaryLabel: 'Refresh Source',
    };
  }
  if (staleDocument.value) {
    const blockedStage = missingDocument.value?.stageLabel ? ` before generating ${missingDocument.value.stageLabel}` : '';
    return {
      kind: 'review-document',
      kicker: 'Review Blocker',
      title: `Review latest ${staleDocument.value.stageLabel}`,
      description: `${staleDocument.value.title} changed after review. Open the latest version, rerun the gate if needed, then review it${blockedStage}.`,
      primaryLabel: 'Open Document',
    };
  }
  if (latestRun.value && cliPrompt.value && !isMerged.value) {
    return {
      kind: 'continue-run',
      kicker: 'CLI Handoff',
      title: `Continue ${latestRun.value.targetStage ?? targetStageLabel.value}`,
      description: 'Copy this prompt into your CLI. After the PR is merged, confirm it here with the GitHub PR URL.',
      primaryLabel: copied.value ? 'Copied' : 'Copy Prompt',
    };
  }
  if (missingDocument.value) {
    return {
      kind: 'generate-document',
      kicker: 'CLI Handoff',
      title: `Generate ${missingDocument.value.stageLabel}`,
      description: selectedSkill.value
        ? 'No upstream blockers detected. Prepare a short prompt for the current missing document, then paste it into your CLI.'
        : 'No CLI skill is configured for this stage in the active profile.',
      primaryLabel: 'Prepare Prompt',
    };
  }
  return {
    kind: 'ready',
    kicker: 'Ready',
    title: 'Ready for review',
    description: 'Sources and SDD documents are current. Open a document below to continue business review.',
    primaryLabel: 'Refresh',
  };
});

async function copyCommand() {
  if (!cliPrompt.value) return;
  await navigator.clipboard.writeText(cliPrompt.value);
  copied.value = true;
  window.setTimeout(() => {
    copied.value = false;
  }, 1800);
}

function prepareRun() {
  if (!selectedSkill.value) return;
  emit('prepareRun', selectedSkill.value.skillId, targetStage.value);
}

function runPrimaryAction() {
  switch (nextAction.value.kind) {
    case 'refresh-source':
      if (sourceIssue.value) emit('refreshSource', sourceIssue.value.id);
      break;
    case 'review-document':
      if (staleDocument.value?.id) emit('openDocument', staleDocument.value.id);
      break;
    case 'refresh-documents':
    case 'ready':
      emit('refreshDocuments');
      break;
    case 'continue-run':
      void copyCommand();
      break;
    case 'generate-document':
      prepareRun();
      break;
  }
}

function openMergeForm() {
  showMergeForm.value = true;
  mergeError.value = null;
}

function closeMergeForm() {
  showMergeForm.value = false;
  mergePrUrl.value = '';
  mergeError.value = null;
}

function confirmMerge() {
  if (!latestRun.value) return;
  const prUrl = mergePrUrl.value.trim();
  if (!/^https:\/\/github\.com\/[^/]+\/[^/]+\/pull\/\d+\/?$/.test(prUrl)) {
    mergeError.value = 'Enter a GitHub PR URL.';
    return;
  }
  emit('confirmMerge', latestRun.value.executionId, prUrl);
  closeMergeForm();
}
</script>

<template>
  <RequirementCard title="Next Action" :is-loading="isLoading" :full-width="true">
    <div class="next-action-panel">
      <div class="action-summary">
        <div class="action-kicker">
          <RotateCcw v-if="nextAction.kind === 'refresh-source' || nextAction.kind === 'refresh-documents' || nextAction.kind === 'ready'" :size="17" />
          <FileText v-else-if="nextAction.kind === 'review-document'" :size="17" />
          <Terminal v-else :size="17" />
          <span>{{ nextAction.kicker }}</span>
        </div>
        <strong>{{ nextAction.title }}</strong>
        <p>{{ nextAction.description }}</p>
      </div>

      <div class="action-buttons">
        <button
          class="primary-btn"
          type="button"
          :disabled="(nextAction.kind === 'generate-document' && !selectedSkill) || (nextAction.kind === 'review-document' && !staleDocument?.id)"
          @click="runPrimaryAction"
        >
          <RotateCcw v-if="nextAction.kind === 'refresh-source' || nextAction.kind === 'refresh-documents' || nextAction.kind === 'ready'" :size="14" />
          <FileText v-else-if="nextAction.kind === 'review-document'" :size="14" />
          <Clipboard v-else-if="nextAction.kind === 'continue-run'" :size="14" />
          <Play v-else-if="nextAction.kind === 'generate-document'" :size="14" />
          <span>{{ nextAction.primaryLabel }}</span>
        </button>
        <button v-if="latestRun && !isMerged && nextAction.kind === 'continue-run'" class="secondary-btn" type="button" @click="openMergeForm">
          <CheckCircle2 :size="14" />
          <span>Confirm PR Merge</span>
        </button>
        <button v-if="latestRun" class="icon-text-btn" type="button" @click="emit('refreshStatus')">
          <RefreshCw :size="14" />
          <span>Refresh</span>
        </button>
      </div>

      <div v-if="cliPrompt" class="prompt-box" aria-label="CLI prompt">
        <code>{{ cliPrompt }}</code>
      </div>

      <form v-if="showMergeForm" class="merge-form" @submit.prevent="confirmMerge">
        <label for="merge-pr-url">GitHub PR URL</label>
        <div class="merge-input-row">
          <input
            id="merge-pr-url"
            v-model="mergePrUrl"
            type="url"
            placeholder="https://github.com/org/repo/pull/123"
            autocomplete="off"
          />
          <button class="prepare-btn" type="submit">
            <CheckCircle2 :size="14" />
            <span>Mark Merged</span>
          </button>
          <button class="icon-btn" type="button" aria-label="Cancel merge confirmation" @click="closeMergeForm">
            <X :size="14" />
          </button>
        </div>
        <small v-if="mergeError">{{ mergeError }}</small>
      </form>

      <details v-if="profileRuns.length" class="run-history">
        <summary>
          <span>Run history</span>
          <strong>{{ profileRuns.length }} for {{ profile.name }}</strong>
        </summary>
        <div class="history-list">
          <div v-for="run in profileRuns" :key="run.executionId" class="history-row">
            <div>
              <strong>{{ run.targetStage ?? targetStage }}</strong>
              <small>{{ run.executionId }}</small>
            </div>
            <span>{{ run.status }}</span>
          </div>
          <div v-if="latestEvents.length" class="event-list">
            <span class="event-heading">Latest Stage Events</span>
            <div v-for="event in latestEvents" :key="event.id" class="event-row">
              <strong>{{ event.stageLabel ?? event.stageId }}</strong>
              <span>{{ event.state }}</span>
              <small>{{ event.message ?? event.outputPath ?? event.errorMessage ?? 'Callback received' }}</small>
            </div>
          </div>
        </div>
      </details>
    </div>
  </RequirementCard>
</template>

<style scoped>
.next-action-panel {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: start;
}

.action-summary {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.action-kicker {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--color-secondary);
}

.action-kicker span,
.merge-form label,
.event-heading,
.run-history summary span {
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.625rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.action-summary > strong {
  color: var(--color-on-surface);
  font-family: var(--font-ui);
  font-size: 1rem;
}

.action-summary p {
  max-width: 760px;
  margin: 0;
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.75rem;
  line-height: 1.5;
}

.action-buttons {
  display: flex;
  align-items: flex-end;
  flex-direction: column;
  gap: 6px;
}

.primary-btn,
.secondary-btn,
.icon-text-btn,
.prepare-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  min-height: 34px;
  padding: 7px 12px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-secondary);
  cursor: pointer;
  font-family: var(--font-ui);
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.primary-btn,
.prepare-btn {
  background: var(--color-secondary);
  color: var(--color-on-secondary-container);
}

.primary-btn:disabled,
.prepare-btn:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.secondary-btn,
.icon-text-btn {
  background: transparent;
  color: var(--color-secondary);
}

.prompt-box {
  grid-column: 1 / -1;
  padding: 10px 12px;
  border: 1px solid rgba(137, 206, 255, 0.22);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
}

.prompt-box code {
  display: block;
  min-width: 0;
  overflow-x: auto;
  color: var(--color-on-surface);
  font-family: var(--font-tech);
  font-size: 0.8125rem;
  line-height: 1.5;
  white-space: pre-wrap;
}

.merge-form {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  gap: 7px;
  padding: 10px;
  border: 1px solid rgba(125, 211, 166, 0.24);
  border-radius: var(--radius-sm);
  background: rgba(125, 211, 166, 0.08);
}

.merge-input-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  gap: 8px;
}

.merge-input-row input {
  min-width: 0;
  padding: 8px 10px;
  border: 1px solid rgba(137, 206, 255, 0.22);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
  color: var(--color-on-surface);
  font-family: var(--font-tech);
  font-size: 0.75rem;
}

.merge-form small {
  color: var(--color-error);
  font-family: var(--font-ui);
  font-size: 0.6875rem;
}

.icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 34px;
  width: 34px;
  border: 1px solid rgba(137, 206, 255, 0.22);
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-on-surface-variant);
  cursor: pointer;
}

.run-history {
  grid-column: 1 / -1;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: rgba(137, 206, 255, 0.04);
}

.run-history summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-height: 36px;
  padding: 8px 10px;
  cursor: pointer;
}

.run-history summary strong {
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.75rem;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 0 10px 10px;
}

.history-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 8px;
  align-items: center;
  padding: 8px 10px;
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
}

.history-row strong,
.history-row span,
.history-row small {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-row strong {
  display: block;
  color: var(--color-on-surface);
  font-family: var(--font-ui);
  font-size: 0.75rem;
}

.history-row small {
  display: block;
  margin-top: 2px;
  color: var(--color-on-surface-variant);
  font-family: var(--font-tech);
  font-size: 0.6875rem;
}

.history-row span {
  color: var(--color-secondary);
  font-family: var(--font-tech);
  font-size: 0.6875rem;
}

.event-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.event-row {
  display: grid;
  grid-template-columns: minmax(120px, 0.8fr) 90px minmax(0, 1.4fr);
  gap: 8px;
  align-items: center;
  padding: 8px 10px;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
}

.event-row strong,
.event-row span,
.event-row small {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.event-row strong {
  color: var(--color-on-surface);
  font-family: var(--font-ui);
  font-size: 0.75rem;
}

.event-row span {
  color: var(--color-secondary);
  font-family: var(--font-tech);
  font-size: 0.6875rem;
}

.event-row small {
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.6875rem;
}

@media (max-width: 1180px) {
  .next-action-panel,
  .merge-input-row,
  .history-row,
  .event-row {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    align-items: stretch;
  }
}
</style>
