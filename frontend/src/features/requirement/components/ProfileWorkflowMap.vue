<script setup lang="ts">
import { computed } from 'vue';
import { RefreshCw } from 'lucide-vue-next';
import type { PipelineProfile, SddDocumentIndex, SddDocumentStage, SpecTier } from '../types/requirement';
import RequirementCard from './RequirementCard.vue';

interface Props {
  profile: PipelineProfile;
  documents?: SddDocumentIndex | null;
  fullWidth?: boolean;
  compact?: boolean;
  primaryActionLoading?: boolean;
  primaryActionDisabled?: boolean;
}

const props = defineProps<Props>();
const emit = defineEmits<{ primaryAction: [] }>();

const executionMode = computed(() => props.profile.usesOrchestrator ? 'Orchestrator routed' : 'Direct skill actions');
const tieringLabel = computed(() => {
  if (!props.profile.specTiering) return 'No tiering';
  return `${props.profile.specTiering.tiers.join('/')} by orchestrator`;
});
const traceabilityLabel = computed(() => props.profile.traceabilityMode === 'shared-br' ? 'Shared BR' : 'Per layer');
const skillSectionLabel = computed(() => props.profile.usesOrchestrator ? 'Agent Entry Point' : 'Available Skills');
const routeSectionLabel = computed(() => props.profile.usesOrchestrator ? 'Possible Orchestrator Routes' : 'Entry Paths');
const routeHint = computed(() => props.profile.usesOrchestrator ? 'Auto-selected by orchestrator' : 'Profile-defined path');
const executionHub = computed(() => props.profile.chainNodes.find(node => node.isExecutionHub));
const primaryActionLabel = computed(() => props.primaryActionLoading ? 'Refreshing GitHub' : 'Refresh GitHub');
const hasProjectProgress = computed(() => Boolean(props.documents?.stages?.length));

type NodeProgressState = 'done' | 'current' | 'attention' | 'pending' | 'template';

interface ChainNodeProgress {
  readonly node: PipelineProfile['chainNodes'][number];
  readonly index: number;
  readonly stage: SddDocumentStage | null;
  readonly state: NodeProgressState;
  readonly label: string;
}

function needsAttention(stage: SddDocumentStage | null): boolean {
  return Boolean(stage && ['ERROR', 'SOURCE_CHANGED', 'DOCUMENT_CHANGED_AFTER_REVIEW'].includes(stage.freshnessStatus));
}

function isMissing(stage: SddDocumentStage | null): boolean {
  return !stage || stage.missing || stage.freshnessStatus === 'MISSING_DOCUMENT';
}

function findStageForNode(node: PipelineProfile['chainNodes'][number]): SddDocumentStage | null {
  const stages = props.documents?.stages ?? [];
  return stages.find(stage => stage.sddType === node.id)
    ?? stages.find(stage => stage.stageLabel.toLowerCase() === node.label.toLowerCase())
    ?? null;
}

const chainProgress = computed<ReadonlyArray<ChainNodeProgress>>(() => {
  const mapped = props.profile.chainNodes.map((node, index) => ({
    node,
    index,
    stage: findStageForNode(node),
  }));
  const firstMissingIndex = mapped.findIndex(item => isMissing(item.stage));
  const currentIndex = firstMissingIndex >= 0 ? firstMissingIndex : Math.max(0, mapped.length - 1);

  return mapped.map(item => {
    let state: NodeProgressState = 'template';
    let label = item.node.isExecutionHub ? 'Hub' : 'Template';

    if (hasProjectProgress.value) {
      if (needsAttention(item.stage)) {
        state = 'attention';
        label = 'Attention';
      } else if (item.index < currentIndex || (firstMissingIndex < 0 && item.index < mapped.length - 1)) {
        state = 'done';
        label = 'Done';
      } else if (item.index === currentIndex) {
        state = firstMissingIndex >= 0 ? 'current' : 'done';
        label = firstMissingIndex >= 0 ? 'Current' : 'Done';
      } else {
        state = 'pending';
        label = 'Pending';
      }
    }

    return { ...item, state, label };
  });
});

const currentProgress = computed(() => {
  if (!hasProjectProgress.value) return null;
  return chainProgress.value.find(item => item.state === 'attention')
    ?? chainProgress.value.find(item => item.state === 'current')
    ?? chainProgress.value[chainProgress.value.length - 1]
    ?? null;
});

const previousProgress = computed(() => {
  if (!currentProgress.value) return null;
  return [...chainProgress.value]
    .slice(0, currentProgress.value.index)
    .reverse()
    .find(item => item.state === 'done')
    ?? null;
});

const nextProgress = computed(() => {
  if (!currentProgress.value) return null;
  return chainProgress.value.find(item => item.index > currentProgress.value!.index && item.state === 'pending')
    ?? null;
});

const progressTitle = computed(() => {
  if (!currentProgress.value) return 'Profile template';
  if (currentProgress.value.state === 'attention') return `${currentProgress.value.node.label} needs attention`;
  return currentProgress.value.state === 'done'
    ? `${currentProgress.value.node.label} complete`
    : currentProgress.value.node.label;
});

const progressSubtitle = computed(() => {
  if (!hasProjectProgress.value) {
    return 'Template route only; open a requirement to see project progress.';
  }
  if (currentProgress.value?.state === 'attention') {
    return 'Freshness or review status needs attention before continuing.';
  }
  const previous = previousProgress.value?.node.label ?? 'None';
  const next = nextProgress.value?.node.label ?? 'Complete';
  return `Last completed: ${previous} · Next: ${next}`;
});

const compactChainNodes = computed<ReadonlyArray<PipelineProfile['chainNodes'][number]>>(() => {
  const nodes = props.profile.chainNodes;
  const hubIndex = nodes.findIndex(node => node.isExecutionHub);
  const indexes = new Set<number>();
  [0, 1, hubIndex, nodes.length - 1].forEach(index => {
    if (index >= 0 && index < nodes.length) indexes.add(index);
  });
  return [...indexes].sort((left, right) => left - right).map(index => nodes[index]);
});

const compactChainProgress = computed(() => {
  const visibleIds = new Set(compactChainNodes.value.map(node => node.id));
  return chainProgress.value.filter(item => visibleIds.has(item.node.id));
});

const hiddenCompactNodeCount = computed(() => {
  return Math.max(0, props.profile.chainNodes.length - compactChainNodes.value.length);
});

function stageThresholdLabel(tier?: SpecTier | null): string {
  if (!tier) return '';
  return tier === 'L3' ? 'From L3' : `From ${tier}+`;
}
</script>

<template>
  <RequirementCard :title="compact ? '' : 'Profile Workflow Map'" :full-width="fullWidth">
    <div v-if="compact" class="workflow-compact">
      <div class="compact-primary">
        <div class="compact-title-block">
          <span class="compact-kicker">Active Workflow</span>
          <strong>{{ profile.name }}</strong>
        </div>

        <div class="compact-meta">
          <span>{{ executionMode }}</span>
          <span>{{ profile.chainNodes.length }} steps</span>
          <span>{{ profile.documentStages.length }} docs</span>
          <span v-if="executionHub">Hub: {{ executionHub.label }}</span>
          <span class="compact-progress-meta">{{ progressTitle }}</span>
        </div>

        <div class="progress-summary" :class="{ 'progress-summary--template': !hasProjectProgress }">
          <span class="progress-summary-label">{{ hasProjectProgress ? 'Current Stage' : 'Progress Source' }}</span>
          <strong>{{ progressTitle }}</strong>
          <small>{{ progressSubtitle }}</small>
        </div>

        <ol class="compact-chain">
          <li
            v-for="item in compactChainProgress"
            :key="item.node.id"
            :class="[
              `compact-chain-node--${item.state}`,
              { 'compact-chain-node--hub': item.node.isExecutionHub },
            ]"
          >
            <span>{{ item.node.label }}</span>
            <small>{{ item.label }}</small>
          </li>
          <li v-if="hiddenCompactNodeCount > 0" class="compact-chain-more">
            +{{ hiddenCompactNodeCount }}
          </li>
        </ol>
      </div>

      <button class="primary-action" :disabled="primaryActionDisabled || primaryActionLoading" @click="emit('primaryAction')">
        <RefreshCw :size="15" />
        <span>{{ primaryActionLabel }}</span>
      </button>

      <details class="workflow-details">
        <summary>Workflow details</summary>
        <div class="workflow-details-body">
          <section class="workflow-section">
            <div class="section-heading">
              <span>Chain</span>
              <span>{{ profile.chainNodes.length }} nodes</span>
            </div>
            <ol class="chain-track chain-track--compact">
              <li
                v-for="(node, index) in profile.chainNodes"
                :key="node.id"
                class="chain-node"
                :class="{ 'chain-node--hub': node.isExecutionHub }"
              >
                <span class="node-index">{{ index + 1 }}</span>
                <span class="node-copy">
                  <span class="node-label">{{ node.label }}</span>
                </span>
              </li>
            </ol>
          </section>

          <section class="workflow-section">
            <div class="section-heading">
              <span>Documents</span>
              <span>{{ profile.documentStages.length }} stages</span>
            </div>
            <div class="document-grid document-grid--compact">
              <div v-for="stage in profile.documentStages" :key="stage.sddType" class="document-stage">
                <div class="document-main">
                  <span class="document-key">{{ stage.traceabilityKey ?? stage.sddType }}</span>
                  <span
                    v-if="stage.expectedTier"
                    class="threshold-chip"
                    :title="`${stage.label} applies ${stageThresholdLabel(stage.expectedTier).toLowerCase()}; the orchestrator determines the actual tier per requirement.`"
                  >
                    {{ stageThresholdLabel(stage.expectedTier) }}
                  </span>
                </div>
                <span class="document-label">{{ stage.label }}</span>
              </div>
            </div>
          </section>
        </div>
      </details>
    </div>

    <div v-else class="workflow-map">
      <div class="summary-strip">
        <div class="summary-item">
          <span class="summary-label">Active Profile</span>
          <strong>{{ profile.name }}</strong>
        </div>
        <div class="summary-item">
          <span class="summary-label">Execution</span>
          <strong>{{ executionMode }}</strong>
        </div>
        <div class="summary-item">
          <span class="summary-label">Traceability</span>
          <strong>{{ traceabilityLabel }}</strong>
        </div>
        <div class="summary-item">
          <span class="summary-label">Tiering</span>
          <strong>{{ tieringLabel }}</strong>
        </div>
        <div class="summary-item summary-item--progress">
          <span class="summary-label">{{ hasProjectProgress ? 'Current Stage' : 'Progress Source' }}</span>
          <strong>{{ progressTitle }}</strong>
          <small>{{ progressSubtitle }}</small>
        </div>
      </div>

      <section class="workflow-section">
        <div class="section-heading">
          <span>Workflow Chain</span>
          <span>{{ profile.chainNodes.length }} nodes</span>
        </div>
        <ol class="chain-track">
          <li
            v-for="item in chainProgress"
            :key="item.node.id"
            class="chain-node"
            :class="[
              `chain-node--${item.state}`,
              { 'chain-node--hub': item.node.isExecutionHub },
            ]"
          >
            <span class="node-index">{{ item.index + 1 }}</span>
            <span class="node-copy">
              <span class="node-label">{{ item.node.label }}</span>
              <span class="node-type">{{ item.label }} · {{ item.node.artifactType }}</span>
            </span>
          </li>
        </ol>
      </section>

      <section class="workflow-section">
        <div class="section-heading">
          <span>Profile Document Catalog</span>
          <span>{{ profile.documentStages.length }} stages</span>
        </div>
        <div class="document-grid">
          <div v-for="stage in profile.documentStages" :key="stage.sddType" class="document-stage">
            <div class="document-main">
              <span class="document-key">{{ stage.traceabilityKey ?? stage.sddType }}</span>
              <span
                v-if="stage.expectedTier"
                class="threshold-chip"
                :title="`${stage.label} applies ${stageThresholdLabel(stage.expectedTier).toLowerCase()}; the orchestrator determines the actual tier per requirement.`"
              >
                {{ stageThresholdLabel(stage.expectedTier) }}
              </span>
            </div>
            <span class="document-label">{{ stage.label }}</span>
            <span class="document-path">{{ stage.pathPattern }}</span>
          </div>
        </div>
      </section>

      <section class="workflow-section execution-section">
        <div class="execution-block">
          <span class="section-heading-inline">{{ skillSectionLabel }}</span>
          <span v-if="profile.usesOrchestrator" class="execution-hint">Request prepared by Control Tower; executed by CLI agent</span>
          <div class="pill-row">
            <span v-for="skill in profile.skills" :key="skill.skillId" class="rule-pill">
              {{ skill.label }}
            </span>
          </div>
        </div>
        <div class="execution-block">
          <span class="section-heading-inline">{{ routeSectionLabel }}</span>
          <span class="execution-hint">{{ routeHint }}</span>
          <div class="pill-row">
            <span v-for="path in profile.entryPaths" :key="path.id" class="rule-pill" :title="path.description">
              {{ path.label }}
            </span>
          </div>
        </div>
      </section>
    </div>
  </RequirementCard>
</template>

<style scoped>
.workflow-map {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-width: 0;
}

.workflow-compact {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 14px;
  align-items: start;
  min-width: 0;
}

.compact-primary {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.compact-title-block {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.compact-kicker {
  font-family: var(--font-ui);
  font-size: 0.6875rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--color-on-surface-variant);
}

.compact-title-block strong {
  color: var(--color-on-surface);
  font-family: var(--font-ui);
  font-size: 1rem;
  font-weight: 700;
}

.compact-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.compact-meta span {
  padding: 5px 8px;
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
  border: var(--border-ghost);
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.75rem;
}

.compact-meta .compact-progress-meta {
  color: var(--color-on-surface);
  border-color: rgba(129, 199, 132, 0.28);
  background: rgba(129, 199, 132, 0.08);
}

.progress-summary {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 3px 10px;
  align-items: baseline;
  padding: 9px 10px;
  border-radius: var(--radius-sm);
  border: 1px solid rgba(129, 199, 132, 0.3);
  background: rgba(129, 199, 132, 0.08);
}

.progress-summary--template {
  border-color: rgba(137, 206, 255, 0.22);
  background: rgba(137, 206, 255, 0.07);
}

.progress-summary-label {
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.625rem;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.progress-summary strong {
  min-width: 0;
  color: var(--color-on-surface);
  font-family: var(--font-ui);
  font-size: 0.875rem;
  font-weight: 700;
  overflow-wrap: anywhere;
}

.progress-summary small {
  grid-column: 1 / -1;
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.75rem;
  overflow-wrap: anywhere;
}

.compact-chain {
  list-style: none;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  padding: 0;
  margin: 0;
}

.compact-chain li {
  position: relative;
  padding: 7px 10px;
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
  border: var(--border-ghost);
  color: var(--color-on-surface);
  font-family: var(--font-ui);
  font-size: 0.8125rem;
  font-weight: 600;
}

.compact-chain li span,
.compact-chain li small {
  display: block;
}

.compact-chain li small {
  margin-top: 2px;
  color: var(--color-on-surface-variant);
  font-family: var(--font-tech);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.compact-chain li:not(:last-child)::after {
  content: "";
  position: absolute;
  top: 50%;
  right: -8px;
  width: 8px;
  height: 1px;
  background: rgba(137, 206, 255, 0.3);
}

.compact-chain .compact-chain-node--hub {
  border-color: var(--color-secondary);
  box-shadow: inset 0 0 0 1px rgba(137, 206, 255, 0.12);
}

.compact-chain .compact-chain-node--done {
  border-color: rgba(129, 199, 132, 0.35);
  background: rgba(129, 199, 132, 0.1);
}

.compact-chain .compact-chain-node--current {
  border-color: rgba(255, 202, 40, 0.5);
  background: rgba(255, 202, 40, 0.1);
}

.compact-chain .compact-chain-node--attention {
  border-color: rgba(239, 83, 80, 0.45);
  background: rgba(239, 83, 80, 0.1);
}

.compact-chain .compact-chain-node--pending {
  opacity: 0.72;
}

.compact-chain .compact-chain-more {
  color: var(--color-secondary);
  font-family: var(--font-tech);
}

.primary-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 38px;
  padding: 0 14px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-secondary);
  background: var(--color-secondary);
  color: var(--color-on-secondary-container);
  font-family: var(--font-ui);
  font-size: 0.8125rem;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
}

.primary-action:hover {
  box-shadow: 0 0 12px var(--color-secondary-glow);
}

.primary-action:disabled {
  cursor: progress;
  opacity: 0.65;
  box-shadow: none;
}

.workflow-details {
  grid-column: 1 / -1;
  min-width: 0;
}

.workflow-details summary {
  width: max-content;
  cursor: pointer;
  color: var(--color-secondary);
  font-family: var(--font-ui);
  font-size: 0.75rem;
  font-weight: 700;
}

.workflow-details-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 12px;
}

.summary-strip {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 8px;
}

.summary-item {
  min-width: 0;
  padding: 10px;
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
  border: var(--border-ghost);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.summary-label,
.section-heading,
.section-heading-inline {
  font-family: var(--font-ui);
  font-size: 0.6875rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-on-surface-variant);
}

.summary-item strong {
  min-width: 0;
  color: var(--color-on-surface);
  font-family: var(--font-ui);
  font-size: 0.875rem;
  font-weight: 600;
  overflow-wrap: anywhere;
}

.summary-item small {
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.6875rem;
  line-height: 1.35;
  overflow-wrap: anywhere;
}

.summary-item--progress {
  border-color: rgba(129, 199, 132, 0.3);
  background: rgba(129, 199, 132, 0.08);
}

.workflow-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.section-heading {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.chain-track {
  list-style: none;
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 2px 0 8px;
  margin: 0;
}

.chain-node {
  position: relative;
  flex: 0 0 132px;
  min-height: 58px;
  padding: 10px;
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
  border: var(--border-ghost);
  display: flex;
  align-items: center;
  gap: 8px;
}

.chain-node:not(:last-child)::after {
  content: "";
  position: absolute;
  top: 50%;
  right: -8px;
  width: 8px;
  height: 1px;
  background: rgba(137, 206, 255, 0.28);
}

.chain-node--hub {
  border-color: var(--color-secondary);
  box-shadow: inset 0 0 0 1px rgba(137, 206, 255, 0.12);
}

.chain-node--done {
  border-color: rgba(129, 199, 132, 0.35);
  background: rgba(129, 199, 132, 0.1);
}

.chain-node--current {
  border-color: rgba(255, 202, 40, 0.5);
  background: rgba(255, 202, 40, 0.1);
}

.chain-node--attention {
  border-color: rgba(239, 83, 80, 0.45);
  background: rgba(239, 83, 80, 0.1);
}

.chain-node--pending {
  opacity: 0.72;
}

.node-index {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(137, 206, 255, 0.12);
  color: var(--color-secondary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-family: var(--font-tech);
  font-size: 0.6875rem;
  font-weight: 700;
}

.node-copy {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.node-label,
.document-label {
  color: var(--color-on-surface);
  font-family: var(--font-ui);
  font-size: 0.75rem;
  font-weight: 600;
  overflow-wrap: anywhere;
}

.node-type,
.document-path {
  color: var(--color-on-surface-variant);
  font-family: var(--font-tech);
  font-size: 0.625rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.document-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 8px;
}

.document-stage {
  min-width: 0;
  padding: 9px;
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
  border: var(--border-ghost);
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.document-main {
  display: flex;
  justify-content: space-between;
  gap: 6px;
}

.document-key,
.threshold-chip,
.rule-pill {
  font-family: var(--font-tech);
  font-size: 0.625rem;
  color: var(--color-secondary);
}

.threshold-chip {
  padding: 1px 5px;
  border-radius: 2px;
  background: rgba(137, 206, 255, 0.1);
  white-space: nowrap;
}

.execution-section {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.execution-block {
  min-width: 0;
  padding: 10px;
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
  border: var(--border-ghost);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.execution-hint {
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.75rem;
}

.pill-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.rule-pill {
  padding: 4px 7px;
  border-radius: 2px;
  background: rgba(137, 206, 255, 0.08);
  border: 1px solid rgba(137, 206, 255, 0.12);
  overflow-wrap: anywhere;
}

.chain-track--compact .chain-node {
  flex-basis: 118px;
  min-height: 48px;
}

.document-grid--compact {
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
}

.document-grid--compact .document-stage {
  padding: 8px;
}

@media (max-width: 1180px) {
  .summary-strip,
  .execution-section {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .workflow-compact {
    grid-template-columns: 1fr;
  }

  .primary-action {
    width: 100%;
  }

  .compact-title-block {
    align-items: flex-start;
    flex-direction: column;
    gap: 4px;
  }

  .summary-strip,
  .execution-section {
    grid-template-columns: 1fr;
  }
}
</style>
