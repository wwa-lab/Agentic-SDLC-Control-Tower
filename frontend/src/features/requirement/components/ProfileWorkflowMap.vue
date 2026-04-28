<script setup lang="ts">
import { computed } from 'vue';
import { RefreshCw } from 'lucide-vue-next';
import type { PipelineProfile, SpecTier } from '../types/requirement';
import RequirementCard from './RequirementCard.vue';

interface Props {
  profile: PipelineProfile;
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

const compactChainNodes = computed<ReadonlyArray<PipelineProfile['chainNodes'][number]>>(() => {
  const nodes = props.profile.chainNodes;
  const hubIndex = nodes.findIndex(node => node.isExecutionHub);
  const indexes = new Set<number>();
  [0, 1, hubIndex, nodes.length - 1].forEach(index => {
    if (index >= 0 && index < nodes.length) indexes.add(index);
  });
  return [...indexes].sort((left, right) => left - right).map(index => nodes[index]);
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
        </div>

        <ol class="compact-chain">
          <li v-for="node in compactChainNodes" :key="node.id" :class="{ 'compact-chain-node--hub': node.isExecutionHub }">
            {{ node.label }}
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
      </div>

      <section class="workflow-section">
        <div class="section-heading">
          <span>Workflow Chain</span>
          <span>{{ profile.chainNodes.length }} nodes</span>
        </div>
        <ol class="chain-track">
          <li
            v-for="(node, index) in profile.chainNodes"
            :key="node.id"
            class="chain-node"
            :class="{ 'chain-node--hub': node.isExecutionHub }"
          >
            <span class="node-index">{{ index + 1 }}</span>
            <span class="node-copy">
              <span class="node-label">{{ node.label }}</span>
              <span class="node-type">{{ node.artifactType }}</span>
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
  background: rgba(137, 206, 255, 0.1);
  box-shadow: 0 0 8px var(--color-secondary-glow);
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
  grid-template-columns: repeat(4, minmax(0, 1fr));
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
  background: rgba(137, 206, 255, 0.1);
  box-shadow: 0 0 10px var(--color-secondary-glow);
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
