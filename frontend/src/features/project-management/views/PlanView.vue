<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import type { SectionResult } from '@/shared/types/section';
import { useWorkspaceStore } from '@/shared/stores/workspaceStore';
import { useShellUiStore } from '@/shell/stores/shellUiStore';
import ProjectManagementCardShell from '../components/ProjectManagementCardShell.vue';
import { useProjectManagementStore } from '../stores/projectManagementStore';
import {
  PM_DEFAULT_PROJECT_ID,
  PM_PROJECT_ID_PATTERN,
  type AiSuggestion,
  type CapacityCell,
  type ChangeLogEntry,
  type Dependency,
  type DependencyResolutionState,
  type HealthIndicator,
  type Milestone,
  type MilestoneStatus,
  type PlanCapacityMatrix,
  type PlanHeader,
  type ProgressNode,
  type RiskItem,
  type RiskState,
} from '../types';

const route = useRoute();
const router = useRouter();
const workspaceStore = useWorkspaceStore();
const shellUiStore = useShellUiStore();
const projectManagementStore = useProjectManagementStore();

function emptySection<T>(): SectionResult<T> {
  return {
    data: null,
    error: null,
  };
}

const resolvedProjectId = computed(() => {
  const paramValue = route.params.projectId;
  return typeof paramValue === 'string' && paramValue.trim() ? paramValue : PM_DEFAULT_PROJECT_ID;
});

const currentWorkspaceId = computed(() => {
  const queryValue = route.query.workspaceId;
  if (typeof queryValue === 'string' && queryValue.trim()) {
    return queryValue;
  }
  return projectManagementStore.plan?.header.data?.workspaceId ?? projectManagementStore.workspaceId ?? null;
});

const isInvalidProjectId = computed(() => !PM_PROJECT_ID_PATTERN.test(resolvedProjectId.value));

const headerSection = computed(() => projectManagementStore.plan?.header ?? emptySection<PlanHeader>());
const milestonesSection = computed(() => projectManagementStore.plan?.milestones ?? emptySection<ReadonlyArray<Milestone>>());
const capacitySection = computed(() => projectManagementStore.plan?.capacity ?? emptySection<PlanCapacityMatrix>());
const risksSection = computed(() => projectManagementStore.plan?.risks ?? emptySection<ReadonlyArray<RiskItem>>());
const dependenciesSection = computed(() => projectManagementStore.plan?.dependencies ?? emptySection<ReadonlyArray<Dependency>>());
const progressSection = computed(() => projectManagementStore.plan?.progress ?? emptySection<ReadonlyArray<ProgressNode>>());
const changeLogSection = computed(() => projectManagementStore.plan?.changeLog ?? emptySection<{ entries: ReadonlyArray<ChangeLogEntry>; page: number; total: number }>());
const aiSuggestionsSection = computed(() => projectManagementStore.plan?.aiSuggestions ?? emptySection<ReadonlyArray<AiSuggestion>>());

const milestoneDrafts = reactive<Record<string, { to: MilestoneStatus; reason: string; newTargetDate: string }>>({});
const riskDrafts = reactive<Record<string, { to: RiskState; mitigationNote: string; resolutionNote: string; linkedIncidentId: string }>>({});
const dependencyDrafts = reactive<Record<string, { to: DependencyResolutionState; rejectionReason: string; contractCommitment: string }>>({});
const capacityDrafts = reactive<Record<string, { percent: number; justification: string }>>({});
const suggestionDismissReasons = reactive<Record<string, string>>({});

const currentRevision = computed(() => headerSection.value.data?.planRevision ?? 0);
const originalCapacityMap = computed(() => {
  const map = new Map<string, CapacityCell>();
  capacitySection.value.data?.cells.forEach(cell => {
    map.set(`${cell.memberId}:${cell.milestoneId}`, cell);
  });
  return map;
});

const governanceItems = computed(() => {
  const items: Array<{ label: string; detail: string; severity: 'warning' | 'danger' | 'ok' }> = [];
  milestonesSection.value.data?.forEach(milestone => {
    if (milestone.status === 'SLIPPED') {
      items.push({
        label: `Milestone slipped: ${milestone.label}`,
        detail: milestone.slippageReason ?? 'No reason recorded yet.',
        severity: 'danger',
      });
    }
  });
  dependenciesSection.value.data?.forEach(dependency => {
    if (dependency.resolutionState === 'NEGOTIATING') {
      items.push({
        label: `Counter-sign pending for ${dependency.targetDescriptor}`,
        detail: dependency.blockerReason ?? 'Awaiting target-side commitment.',
        severity: 'warning',
      });
    }
  });
  aiSuggestionsSection.value.data?.forEach(suggestion => {
    items.push({
      label: `AI review pending: ${suggestion.kind}`,
      detail: suggestion.payload,
      severity: 'ok',
    });
  });
  return items.slice(0, 6);
});

function formatTimestamp(value: string | null | undefined) {
  if (!value) {
    return 'n/a';
  }
  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

function formatDate(value: string | null | undefined) {
  if (!value) {
    return 'n/a';
  }
  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
  }).format(new Date(value));
}

function healthClass(status: string | HealthIndicator) {
  return `pm-health--${status.toLowerCase()}`;
}

function severityClass(severity: string) {
  return `pm-pill--${severity.toLowerCase()}`;
}

function dependencyStateOptions(dependency: Dependency): DependencyResolutionState[] {
  return dependency.external
    ? ['PROPOSED', 'NEGOTIATING', 'AT_RISK', 'RESOLVED']
    : ['PROPOSED', 'NEGOTIATING', 'APPROVED', 'REJECTED', 'AT_RISK', 'RESOLVED'];
}

function capacityKey(memberId: string, milestoneId: string) {
  return `${memberId}:${milestoneId}`;
}

function seedDrafts() {
  milestonesSection.value.data?.forEach(milestone => {
    milestoneDrafts[milestone.id] = {
      to: milestone.status,
      reason: milestone.slippageReason ?? '',
      newTargetDate: milestone.targetDate,
    };
  });
  risksSection.value.data?.forEach(risk => {
    riskDrafts[risk.id] = {
      to: risk.state,
      mitigationNote: risk.mitigationNote ?? '',
      resolutionNote: risk.resolutionNote ?? '',
      linkedIncidentId: risk.linkedIncidentId ?? '',
    };
  });
  dependenciesSection.value.data?.forEach(dependency => {
    dependencyDrafts[dependency.id] = {
      to: dependency.resolutionState,
      rejectionReason: dependency.rejectionReason ?? '',
      contractCommitment: dependency.contractCommitment ?? '',
    };
  });
  capacitySection.value.data?.cells.forEach(cell => {
    capacityDrafts[capacityKey(cell.memberId, cell.milestoneId)] = {
      percent: cell.percent,
      justification: cell.justification ?? '',
    };
  });
}

function openBackToPortfolio() {
  void router.push(currentWorkspaceId.value ? `/project-management?workspaceId=${currentWorkspaceId.value}` : '/project-management');
}

function retryCard(cardKey: Parameters<typeof projectManagementStore.refreshPlanCard>[0]) {
  void projectManagementStore.refreshPlanCard(cardKey);
}

async function loadPlan(nextProjectId: string) {
  if (!PM_PROJECT_ID_PATTERN.test(nextProjectId)) {
    return;
  }
  await projectManagementStore.initPlan(nextProjectId);
}

function saveMilestone(milestone: Milestone) {
  const draft = milestoneDrafts[milestone.id];
  if (!draft) {
    return;
  }
  void projectManagementStore.transitionMilestone(milestone.id, {
    to: draft.to,
    slippageReason: draft.reason || null,
    newTargetDate: draft.newTargetDate || null,
    planRevision: currentRevision.value,
  }).catch(() => undefined);
}

function saveCapacity() {
  const edits = capacitySection.value.data?.cells
    .map(cell => {
      const draft = capacityDrafts[capacityKey(cell.memberId, cell.milestoneId)];
      if (!draft) {
        return null;
      }
      const changed = draft.percent !== cell.percent || draft.justification !== (cell.justification ?? '');
      if (!changed) {
        return null;
      }
      return {
        memberId: cell.memberId,
        milestoneId: cell.milestoneId,
        percent: draft.percent,
        justification: draft.justification || null,
        planRevision: currentRevision.value,
      };
    })
    .filter((edit): edit is NonNullable<typeof edit> => Boolean(edit));

  if (!edits?.length) {
    return;
  }

  void projectManagementStore.saveCapacity({ edits }).catch(() => undefined);
}

function saveRisk(risk: RiskItem) {
  const draft = riskDrafts[risk.id];
  if (!draft) {
    return;
  }
  void projectManagementStore.transitionRisk(risk.id, {
    to: draft.to,
    mitigationNote: draft.mitigationNote || null,
    resolutionNote: draft.resolutionNote || null,
    linkedIncidentId: draft.linkedIncidentId || null,
    planRevision: currentRevision.value,
  }).catch(() => undefined);
}

function saveDependency(dependency: Dependency) {
  const draft = dependencyDrafts[dependency.id];
  if (!draft) {
    return;
  }
  void projectManagementStore.transitionDependency(dependency.id, {
    to: draft.to,
    rejectionReason: draft.rejectionReason || null,
    contractCommitment: draft.contractCommitment || null,
    planRevision: currentRevision.value,
  }).catch(() => undefined);
}

function counterSign(dependency: Dependency) {
  void projectManagementStore.counterSignDependency(dependency.id, currentRevision.value).catch(() => undefined);
}

function acceptSuggestion(suggestionId: string) {
  void projectManagementStore.acceptAiSuggestion(suggestionId).catch(() => undefined);
}

function dismissSuggestion(suggestionId: string) {
  void projectManagementStore.dismissAiSuggestion(suggestionId, suggestionDismissReasons[suggestionId] ?? null).catch(() => undefined);
}

watch(
  () => resolvedProjectId.value,
  projectId => {
    void loadPlan(projectId);
  },
  { immediate: true },
);

watch(
  () => projectManagementStore.plan,
  aggregate => {
    seedDrafts();
    const header = aggregate?.header.data;
    workspaceStore.setRouteContext({
      workspace: header?.workspaceName ?? workspaceStore.context.workspace,
      application: header?.applicationName ?? workspaceStore.context.application,
      snowGroup: workspaceStore.context.snowGroup,
      project: header?.projectName ?? resolvedProjectId.value,
      environment: header?.lifecycleStage ?? null,
    });

    shellUiStore.setBreadcrumbs([
      { label: 'Dashboard', path: '/' },
      { label: 'Project Management', path: currentWorkspaceId.value ? `/project-management?workspaceId=${currentWorkspaceId.value}` : '/project-management' },
      { label: header?.projectName ?? resolvedProjectId.value },
    ]);

    shellUiStore.setAiPanelContent({
      summary: header
        ? `${header.projectName} is ${header.planHealth} with revision ${header.planRevision} and ${aiSuggestionsSection.value.data?.length ?? 0} AI suggestions waiting.`
        : 'Plan view is loading milestone, dependency, and risk execution context.',
      reasoning: [
        {
          text: `Plan scope: ${resolvedProjectId.value}`,
          status: 'ok',
        },
        {
          text: header?.planHealthFactors[0] ?? 'Deriving plan health factors.',
          status: header?.planHealth === 'RED' ? 'error' : header?.planHealth === 'YELLOW' ? 'warning' : 'ok',
        },
        {
          text: governanceItems.value.length ? `${governanceItems.value.length} governance signals visible` : 'No governance queue currently open',
          status: governanceItems.value.length ? 'warning' : 'ok',
        },
      ],
      evidence: JSON.stringify(
        {
          projectId: resolvedProjectId.value,
          workspaceId: currentWorkspaceId.value,
          route: route.fullPath,
          planRevision: header?.planRevision ?? null,
          nextMilestone: header?.nextMilestone ?? null,
          visibleSuggestions: aiSuggestionsSection.value.data?.map(item => item.id) ?? [],
        },
        null,
        2,
      ),
    });
  },
  { immediate: true, deep: true },
);

onMounted(() => {
  shellUiStore.setBreadcrumbs([
    { label: 'Dashboard', path: '/' },
    { label: 'Project Management', path: '/project-management' },
    { label: resolvedProjectId.value },
  ]);
});

onBeforeUnmount(() => {
  workspaceStore.clearRouteContext();
  shellUiStore.clearBreadcrumbs();
  shellUiStore.clearAiPanelContent();
  projectManagementStore.clearMutationFeedback();
  projectManagementStore.reset();
});
</script>

<template>
  <div class="pm-page">
    <div v-if="isInvalidProjectId" class="pm-page-error">
      <p class="text-label">Invalid Project ID</p>
      <p class="text-body-sm">Expected a value like <span class="text-tech">proj-42</span>.</p>
      <button class="btn-machined" @click="router.push(`/project-management/plan/${PM_DEFAULT_PROJECT_ID}`)">Open Default Plan</button>
    </div>

    <template v-else>
      <div class="pm-hero">
        <div>
          <p class="text-label">Plan Execution</p>
          <h2>{{ headerSection.data?.projectName ?? resolvedProjectId }}</h2>
          <p class="pm-hero__copy">
            Operational plan view with live revision fencing, AI suggestions, dependency posture, and change history.
          </p>
        </div>
        <div class="pm-hero__actions">
          <button class="btn-machined btn-secondary" @click="openBackToPortfolio">Back to Portfolio</button>
          <span v-if="headerSection.data" class="pm-pill" :class="healthClass(headerSection.data.planHealth)">
            {{ headerSection.data.planHealth }}
          </span>
        </div>
      </div>

      <div v-if="projectManagementStore.planError && !projectManagementStore.plan" class="pm-page-error">
        <p class="text-label">
          {{
            projectManagementStore.isPlanForbidden
              ? 'Access Denied'
              : projectManagementStore.isPlanNotFound
                ? 'Project Not Found'
                : 'Plan Unavailable'
          }}
        </p>
        <p class="text-body-sm">{{ projectManagementStore.planError }}</p>
        <div class="pm-page-error__actions">
          <button class="btn-machined" @click="loadPlan(resolvedProjectId)">Retry</button>
          <button class="btn-machined btn-secondary" @click="openBackToPortfolio">Back to Portfolio</button>
        </div>
      </div>

      <template v-else>
        <div v-if="projectManagementStore.mutationMessage || projectManagementStore.mutationError" class="pm-feedback" :class="{ 'pm-feedback--error': projectManagementStore.mutationError }">
          <span>{{ projectManagementStore.mutationError ?? projectManagementStore.mutationMessage }}</span>
          <button class="pm-feedback__close" @click="projectManagementStore.clearMutationFeedback()">Close</button>
        </div>

        <ProjectManagementCardShell
          title="Plan Header"
          :loading="projectManagementStore.planLoading || projectManagementStore.planLoadingCards.header"
          :error="headerSection.error"
          :full-width="true"
          @retry="retryCard('header')"
        >
          <template #subtitle>
            <p class="pm-card-subtitle">Project identity, ownership, revision, and health drivers</p>
          </template>

          <div v-if="headerSection.data" class="pm-header-grid">
            <div class="pm-header-grid__identity">
              <p class="text-label">Application</p>
              <strong>{{ headerSection.data.applicationName }}</strong>
              <p class="text-body-sm">{{ headerSection.data.lifecycleStage }} · {{ headerSection.data.autonomyLevel }}</p>
            </div>
            <div class="pm-header-grid__identity">
              <p class="text-label">PM Owner</p>
              <strong>{{ headerSection.data.pmDisplayName }}</strong>
              <p class="text-body-sm">Backup {{ headerSection.data.pmBackupMemberId ?? 'n/a' }}</p>
            </div>
            <div class="pm-header-grid__identity">
              <p class="text-label">Plan Revision</p>
              <strong class="text-tech">REV-{{ headerSection.data.planRevision }}</strong>
              <p class="text-body-sm">Updated {{ formatTimestamp(headerSection.data.lastUpdatedAt) }}</p>
            </div>
            <div class="pm-header-grid__identity">
              <p class="text-label">Next Milestone</p>
              <strong>{{ headerSection.data.nextMilestone?.label ?? 'All milestones complete' }}</strong>
              <p class="text-body-sm">{{ formatDate(headerSection.data.nextMilestone?.targetDate) }}</p>
            </div>
          </div>

          <div v-if="headerSection.data?.planHealthFactors.length" class="pm-factor-list">
            <span v-for="factor in headerSection.data.planHealthFactors" :key="factor" class="pm-factor-item">{{ factor }}</span>
          </div>
        </ProjectManagementCardShell>

        <div class="pm-grid">
          <ProjectManagementCardShell
            title="Milestone Planner"
            :loading="projectManagementStore.planLoading || projectManagementStore.planLoadingCards.milestones"
            :error="milestonesSection.error"
            :empty="!milestonesSection.data?.length"
            empty-message="No milestones are available for this project."
            @retry="retryCard('milestones')"
          >
            <template #subtitle>
              <p class="pm-card-subtitle">Inline transitions with revision fencing</p>
            </template>

            <div v-if="milestonesSection.data" class="pm-list">
              <div v-for="milestone in milestonesSection.data" :key="milestone.id" class="pm-list-item">
                <div class="pm-list-item__header">
                  <div>
                    <strong>{{ milestone.label }}</strong>
                    <p class="text-body-sm">{{ formatDate(milestone.targetDate) }} · {{ milestone.ownerDisplayName ?? 'Unassigned' }}</p>
                  </div>
                  <span class="pm-pill" :class="healthClass(milestone.status)">{{ milestone.status }}</span>
                </div>
                <p class="text-body-sm">{{ milestone.description }}</p>
                <div class="pm-inline-grid">
                  <label>
                    <span class="text-label">State</span>
                    <select v-model="milestoneDrafts[milestone.id].to">
                      <option value="NOT_STARTED">NOT_STARTED</option>
                      <option value="IN_PROGRESS">IN_PROGRESS</option>
                      <option value="AT_RISK">AT_RISK</option>
                      <option value="COMPLETED">COMPLETED</option>
                      <option value="SLIPPED">SLIPPED</option>
                    </select>
                  </label>
                  <label>
                    <span class="text-label">Target Date</span>
                    <input v-model="milestoneDrafts[milestone.id].newTargetDate" type="date" />
                  </label>
                </div>
                <label class="pm-field">
                  <span class="text-label">Reason</span>
                  <textarea v-model="milestoneDrafts[milestone.id].reason" rows="2"></textarea>
                </label>
                <button class="btn-machined" :disabled="projectManagementStore.mutating" @click="saveMilestone(milestone)">Apply Transition</button>
              </div>
            </div>
          </ProjectManagementCardShell>

          <ProjectManagementCardShell
            title="Capacity Allocation"
            :loading="projectManagementStore.planLoading || projectManagementStore.planLoadingCards.capacity"
            :error="capacitySection.error"
            :empty="!capacitySection.data?.cells.length"
            empty-message="No capacity allocations recorded yet."
            @retry="retryCard('capacity')"
          >
            <template #subtitle>
              <p class="pm-card-subtitle">Batch save with overallocation justification</p>
            </template>

            <div v-if="capacitySection.data" class="pm-capacity-editor">
              <div class="pm-capacity-editor__header">
                <span class="text-label">Member</span>
                <span v-for="milestone in capacitySection.data.milestones" :key="milestone.id" class="text-label">{{ milestone.label }}</span>
                <span class="text-label">Total</span>
              </div>

              <div v-for="member in capacitySection.data.members" :key="member.id" class="pm-capacity-editor__row">
                <strong>{{ member.displayName }}</strong>
                <div v-for="milestone in capacitySection.data.milestones" :key="`${member.id}-${milestone.id}`" class="pm-capacity-editor__cell">
                  <input
                    v-model.number="capacityDrafts[capacityKey(member.id, milestone.id)].percent"
                    type="number"
                    min="0"
                    max="500"
                  />
                  <textarea
                    v-if="capacityDrafts[capacityKey(member.id, milestone.id)].percent > 100"
                    v-model="capacityDrafts[capacityKey(member.id, milestone.id)].justification"
                    rows="2"
                    placeholder="Justification required above 100%"
                  ></textarea>
                </div>
                <span class="pm-total" :class="{ 'pm-total--warning': (capacitySection.data.rowTotals[member.id] ?? 0) > 100 }">
                  {{ capacitySection.data.rowTotals[member.id] ?? 0 }}%
                </span>
              </div>

              <button class="btn-machined" :disabled="projectManagementStore.mutating" @click="saveCapacity">Save Capacity Batch</button>
            </div>
          </ProjectManagementCardShell>

          <ProjectManagementCardShell
            title="Risk Registry"
            :loading="projectManagementStore.planLoading || projectManagementStore.planLoadingCards.risks"
            :error="risksSection.error"
            :empty="!risksSection.data?.length"
            empty-message="No project risks registered."
            @retry="retryCard('risks')"
          >
            <template #subtitle>
              <p class="pm-card-subtitle">Severity, owner, mitigation, and escalation controls</p>
            </template>

            <div v-if="risksSection.data" class="pm-list">
              <div v-for="risk in risksSection.data" :key="risk.id" class="pm-list-item">
                <div class="pm-list-item__header">
                  <div>
                    <strong>{{ risk.title }}</strong>
                    <p class="text-body-sm">{{ risk.category }} · {{ risk.ownerDisplayName ?? 'Unassigned' }} · {{ risk.ageDays }}d</p>
                  </div>
                  <div class="pm-list-item__badges">
                    <span class="pm-pill" :class="severityClass(risk.severity)">{{ risk.severity }}</span>
                    <span class="pm-pill" :class="healthClass(risk.state)">{{ risk.state }}</span>
                  </div>
                </div>
                <div class="pm-inline-grid">
                  <label>
                    <span class="text-label">State</span>
                    <select v-model="riskDrafts[risk.id].to">
                      <option value="IDENTIFIED">IDENTIFIED</option>
                      <option value="ACKNOWLEDGED">ACKNOWLEDGED</option>
                      <option value="MITIGATING">MITIGATING</option>
                      <option value="RESOLVED">RESOLVED</option>
                      <option value="ESCALATED">ESCALATED</option>
                    </select>
                  </label>
                  <label>
                    <span class="text-label">Incident</span>
                    <input v-model="riskDrafts[risk.id].linkedIncidentId" type="text" placeholder="INC-####" />
                  </label>
                </div>
                <label class="pm-field">
                  <span class="text-label">Mitigation</span>
                  <textarea v-model="riskDrafts[risk.id].mitigationNote" rows="2"></textarea>
                </label>
                <label class="pm-field">
                  <span class="text-label">Resolution Note</span>
                  <textarea v-model="riskDrafts[risk.id].resolutionNote" rows="2"></textarea>
                </label>
                <button class="btn-machined" :disabled="projectManagementStore.mutating" @click="saveRisk(risk)">Save Risk Transition</button>
              </div>
            </div>
          </ProjectManagementCardShell>

          <ProjectManagementCardShell
            title="Dependency Resolver"
            :loading="projectManagementStore.planLoading || projectManagementStore.planLoadingCards.dependencies"
            :error="dependenciesSection.error"
            :empty="!dependenciesSection.data?.length"
            empty-message="No dependencies linked to this plan."
            @retry="retryCard('dependencies')"
          >
            <template #subtitle>
              <p class="pm-card-subtitle">Internal and external edges with counter-sign workflow</p>
            </template>

            <div v-if="dependenciesSection.data" class="pm-list">
              <div v-for="dependency in dependenciesSection.data" :key="dependency.id" class="pm-list-item">
                <div class="pm-list-item__header">
                  <div>
                    <strong>{{ dependency.targetDescriptor }}</strong>
                    <p class="text-body-sm">{{ dependency.direction }} · {{ dependency.relationship }} · {{ dependency.ownerTeam }}</p>
                  </div>
                  <div class="pm-list-item__badges">
                    <span class="pm-pill" :class="healthClass(dependency.health)">{{ dependency.health }}</span>
                    <span class="pm-pill" :class="healthClass(dependency.resolutionState)">{{ dependency.resolutionState }}</span>
                  </div>
                </div>
                <p class="text-body-sm">{{ dependency.blockerReason ?? 'No blocker reason logged.' }}</p>
                <div class="pm-inline-grid">
                  <label>
                    <span class="text-label">State</span>
                    <select v-model="dependencyDrafts[dependency.id].to">
                      <option v-for="state in dependencyStateOptions(dependency)" :key="state" :value="state">{{ state }}</option>
                    </select>
                  </label>
                  <label>
                    <span class="text-label">Contract</span>
                    <input v-model="dependencyDrafts[dependency.id].contractCommitment" type="text" placeholder="Commitment note" />
                  </label>
                </div>
                <label class="pm-field">
                  <span class="text-label">Rejection Reason</span>
                  <textarea v-model="dependencyDrafts[dependency.id].rejectionReason" rows="2"></textarea>
                </label>
                <div class="pm-action-row">
                  <button class="btn-machined" :disabled="projectManagementStore.mutating" @click="saveDependency(dependency)">Save Dependency</button>
                  <button
                    v-if="!dependency.external && dependency.resolutionState === 'NEGOTIATING'"
                    class="btn-machined btn-secondary"
                    :disabled="projectManagementStore.mutating"
                    @click="counterSign(dependency)"
                  >
                    Counter-sign
                  </button>
                </div>
              </div>
            </div>
          </ProjectManagementCardShell>

          <ProjectManagementCardShell
            title="AI Suggestions"
            :loading="projectManagementStore.planLoading || projectManagementStore.planLoadingCards.aiSuggestions"
            :error="aiSuggestionsSection.error"
            :empty="!aiSuggestionsSection.data?.length"
            empty-message="No pending AI interventions at the moment."
            @retry="retryCard('aiSuggestions')"
          >
            <template #subtitle>
              <p class="pm-card-subtitle">Accept to apply; dismiss to suppress for 24 hours</p>
            </template>

            <div v-if="aiSuggestionsSection.data" class="pm-list">
              <div v-for="suggestion in aiSuggestionsSection.data" :key="suggestion.id" class="pm-list-item">
                <div class="pm-list-item__header">
                  <div>
                    <strong>{{ suggestion.kind }}</strong>
                    <p class="text-body-sm">{{ suggestion.targetType }} · {{ Math.round(suggestion.confidence * 100) }}% confidence</p>
                  </div>
                  <span class="pm-pill pm-pill--ok">{{ suggestion.state }}</span>
                </div>
                <p class="text-body-sm">{{ suggestion.payload }}</p>
                <label class="pm-field">
                  <span class="text-label">Dismiss Reason</span>
                  <textarea v-model="suggestionDismissReasons[suggestion.id]" rows="2"></textarea>
                </label>
                <div class="pm-action-row">
                  <button class="btn-machined" :disabled="projectManagementStore.mutating" @click="acceptSuggestion(suggestion.id)">Accept</button>
                  <button class="btn-machined btn-secondary" :disabled="projectManagementStore.mutating" @click="dismissSuggestion(suggestion.id)">Dismiss</button>
                </div>
              </div>
            </div>
          </ProjectManagementCardShell>

          <ProjectManagementCardShell
            title="Governance Panel"
            :empty="!governanceItems.length"
            empty-message="No governance gates are currently open."
          >
            <template #subtitle>
              <p class="pm-card-subtitle">Derived approval and escalation queue</p>
            </template>

            <div class="pm-list">
              <div v-for="item in governanceItems" :key="`${item.label}-${item.detail}`" class="pm-list-item">
                <div class="pm-list-item__header">
                  <strong>{{ item.label }}</strong>
                  <span class="pm-pill" :class="item.severity === 'danger' ? 'pm-pill--danger' : item.severity === 'warning' ? 'pm-pill--warning' : 'pm-pill--ok'">
                    {{ item.severity }}
                  </span>
                </div>
                <p class="text-body-sm">{{ item.detail }}</p>
              </div>
            </div>
          </ProjectManagementCardShell>

          <ProjectManagementCardShell
            title="Delivery Progress Strip"
            :loading="projectManagementStore.planLoading || projectManagementStore.planLoadingCards.progress"
            :error="progressSection.error"
            :empty="!progressSection.data?.length"
            empty-message="No delivery progress nodes available."
            :full-width="true"
            @retry="retryCard('progress')"
          >
            <template #subtitle>
              <p class="pm-card-subtitle">11-node SDLC chain with deep links</p>
            </template>

            <div v-if="progressSection.data" class="pm-progress-strip">
              <button
                v-for="node in progressSection.data"
                :key="node.node"
                class="pm-progress-node"
                :class="healthClass(node.health)"
                @click="router.push(node.deepLink)"
              >
                <span class="text-label">{{ node.node }}</span>
                <strong>{{ node.throughput }}</strong>
                <p class="text-body-sm">Prev {{ node.priorThroughput }}<span v-if="node.slipped"> · slipped</span></p>
              </button>
            </div>
          </ProjectManagementCardShell>

          <ProjectManagementCardShell
            title="Plan Change Log"
            :loading="projectManagementStore.planLoading || projectManagementStore.planLoadingCards.changeLog"
            :error="changeLogSection.error"
            :empty="!changeLogSection.data?.entries.length"
            empty-message="No change log entries captured yet."
            :full-width="true"
            @retry="retryCard('changeLog')"
          >
            <template #subtitle>
              <p class="pm-card-subtitle">Reverse chronological plan audit trail</p>
            </template>

            <div v-if="changeLogSection.data" class="pm-change-log">
              <div v-for="entry in changeLogSection.data.entries" :key="entry.id" class="pm-change-log__row">
                <div class="pm-change-log__meta">
                  <span class="text-label">{{ entry.action }}</span>
                  <strong>{{ entry.targetType }} / {{ entry.targetId }}</strong>
                  <p class="text-body-sm">{{ entry.actorDisplayName ?? entry.actorType }} · {{ formatTimestamp(entry.at) }}</p>
                </div>
                <p class="text-body-sm">{{ entry.reason ?? 'No reason supplied.' }}</p>
              </div>
            </div>
          </ProjectManagementCardShell>
        </div>
      </template>
    </template>
  </div>
</template>

<style scoped>
.pm-page {
  padding: 0 24px 24px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.pm-hero,
.pm-page-error,
.pm-feedback {
  border: var(--border-ghost);
  border-radius: var(--radius-md);
  background:
    radial-gradient(circle at top right, color-mix(in srgb, var(--color-secondary) 16%, transparent), transparent 45%),
    linear-gradient(180deg, color-mix(in srgb, var(--color-surface-container-high) 92%, transparent), var(--color-surface-container-low));
  padding: 22px 24px;
}

.pm-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.pm-hero h2 {
  font-size: 2rem;
}

.pm-hero__copy {
  margin-top: 8px;
  color: var(--color-on-surface-variant);
  line-height: 1.6;
  max-width: 760px;
}

.pm-hero__actions,
.pm-page-error__actions,
.pm-action-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.pm-feedback {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.pm-feedback--error {
  border-color: color-mix(in srgb, var(--color-incident-crimson) 38%, transparent);
}

.pm-feedback__close {
  border: 0;
  background: transparent;
  color: var(--color-on-surface-variant);
  cursor: pointer;
}

.pm-grid {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 18px;
}

.pm-grid > * {
  grid-column: span 6;
}

.pm-card-subtitle {
  margin-top: 6px;
  color: var(--color-on-surface-variant);
  font-size: 0.8rem;
}

.pm-header-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.pm-header-grid__identity {
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  padding: 14px;
  background: color-mix(in srgb, var(--color-surface-container-highest) 72%, transparent);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.pm-factor-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pm-factor-item,
.pm-total {
  border: var(--border-ghost);
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 0.76rem;
}

.pm-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.pm-list-item,
.pm-change-log__row {
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  padding: 14px;
  background: color-mix(in srgb, var(--color-surface-container-highest) 72%, transparent);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pm-list-item__header,
.pm-list-item__badges,
.pm-change-log__meta {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
}

.pm-inline-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.pm-field,
.pm-inline-grid label {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

select,
input,
textarea {
  width: 100%;
  border-radius: var(--radius-sm);
  border: var(--border-ghost);
  background: color-mix(in srgb, var(--color-surface-container-lowest) 55%, transparent);
  color: var(--color-on-surface);
  padding: 10px 12px;
  font: inherit;
}

textarea {
  resize: vertical;
}

.pm-capacity-editor {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pm-capacity-editor__header,
.pm-capacity-editor__row {
  display: grid;
  grid-template-columns: 1.2fr repeat(3, minmax(0, 1fr)) 72px;
  gap: 10px;
  align-items: flex-start;
}

.pm-capacity-editor__cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.pm-total {
  text-align: center;
  align-self: center;
}

.pm-total--warning {
  color: var(--color-approval-amber);
}

.pm-progress-strip {
  display: grid;
  grid-template-columns: repeat(11, minmax(0, 1fr));
  gap: 10px;
}

.pm-progress-node {
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: color-mix(in srgb, var(--color-surface-container-highest) 75%, transparent);
  padding: 12px;
  text-align: left;
  color: inherit;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.pm-progress-node strong {
  font-size: 1.2rem;
}

.pm-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 56px;
  padding: 4px 8px;
  border-radius: 999px;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.05em;
}

.pm-health--green,
.pm-pill--ok {
  background: color-mix(in srgb, var(--color-health-emerald) 18%, transparent);
}

.pm-health--yellow,
.pm-pill--warning {
  background: color-mix(in srgb, var(--color-approval-amber) 22%, transparent);
}

.pm-health--red,
.pm-pill--danger,
.pm-pill--critical,
.pm-pill--high {
  background: color-mix(in srgb, var(--color-incident-crimson) 20%, transparent);
}

.pm-health--unknown,
.pm-pill--medium,
.pm-pill--low {
  background: color-mix(in srgb, var(--color-secondary) 16%, transparent);
}

@media (max-width: 1280px) {
  .pm-progress-strip {
    grid-template-columns: repeat(6, minmax(0, 1fr));
  }
}

@media (max-width: 1120px) {
  .pm-header-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .pm-capacity-editor__header,
  .pm-capacity-editor__row {
    grid-template-columns: 1.1fr repeat(3, minmax(0, 1fr));
  }

  .pm-total {
    grid-column: 1 / -1;
    justify-self: start;
  }
}

@media (max-width: 980px) {
  .pm-grid > * {
    grid-column: 1 / -1;
  }

  .pm-hero {
    flex-direction: column;
    align-items: stretch;
  }
}

@media (max-width: 760px) {
  .pm-page {
    padding: 0 16px 20px;
  }

  .pm-header-grid,
  .pm-inline-grid,
  .pm-progress-strip {
    grid-template-columns: 1fr;
  }

  .pm-capacity-editor__header {
    display: none;
  }

  .pm-capacity-editor__row {
    grid-template-columns: 1fr;
    border: var(--border-ghost);
    border-radius: var(--radius-sm);
    padding: 12px;
    background: color-mix(in srgb, var(--color-surface-container-highest) 72%, transparent);
  }
}
</style>
