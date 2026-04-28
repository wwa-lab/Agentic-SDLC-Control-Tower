<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowLeft, FileInput, FileOutput, GitBranch, Workflow } from 'lucide-vue-next';
import ProfileSelector from '../components/ProfileSelector.vue';
import { useRequirementStore } from '../stores/requirementStore';
import type { DocumentDependencyDefinition, DocumentStageDefinition, SkillDocumentContract } from '../types/requirement';

const router = useRouter();
const store = useRequirementStore();
const MAX_VISIBLE_DOCS = 3;
const MAX_VISIBLE_SKILLS = 2;
const ARTIFACT_TYPE_ORDER = ['source', 'requirement', 'analysis', 'spec', 'design', 'code', 'test', 'review', 'manifest', 'artifact'];
const ARTIFACT_TYPE_LABELS: Record<string, string> = {
  source: 'Sources',
  requirement: 'Requirements',
  analysis: 'Analysis',
  spec: 'Specs',
  design: 'Design',
  code: 'Code',
  test: 'Tests',
  review: 'Reviews',
  manifest: 'Manifests',
  artifact: 'Artifacts',
};

onMounted(() => {
  store.loadActiveProfile();
});

const profile = computed(() => store.activeProfile);
const flowDocuments = computed(() => profile.value.skillFlowDocuments?.length
  ? profile.value.skillFlowDocuments
  : profile.value.documentStages);

const stageByType = computed(() => new Map(flowDocuments.value.map(stage => [stage.sddType, stage])));

const skillContracts = computed<ReadonlyArray<SkillDocumentContract>>(() => {
  if (profile.value.skillDocumentContracts?.length) return profile.value.skillDocumentContracts;
  return profile.value.skills.map(skill => ({
    skillId: skill.skillId,
    label: skill.label,
    description: `Runs at ${skill.triggerPoint}`,
    inputDocuments: [skill.triggerPoint],
    outputDocuments: [],
    dependsOnSkills: [],
  }));
});

const documentDependencies = computed<ReadonlyArray<DocumentDependencyDefinition>>(() => {
  if (profile.value.documentDependencies?.length) return profile.value.documentDependencies;
  return flowDocuments.value.slice(1).map((stage, index) => ({
    from: flowDocuments.value[index]?.sddType ?? stage.sddType,
    to: stage.sddType,
    reason: 'Sequential profile stage dependency',
  }));
});

const producedBy = computed(() => {
  const byDoc = new Map<string, string[]>();
  for (const contract of skillContracts.value) {
    for (const docType of contract.outputDocuments) {
      byDoc.set(docType, [...(byDoc.get(docType) ?? []), contract.skillId]);
    }
  }
  return byDoc;
});

const consumedBy = computed(() => {
  const byDoc = new Map<string, string[]>();
  for (const contract of skillContracts.value) {
    for (const docType of contract.inputDocuments) {
      byDoc.set(docType, [...(byDoc.get(docType) ?? []), contract.skillId]);
    }
  }
  return byDoc;
});

const upstreamDocs = computed(() => {
  const byDoc = new Map<string, string[]>();
  for (const dependency of documentDependencies.value) {
    byDoc.set(dependency.to, [...(byDoc.get(dependency.to) ?? []), dependency.from]);
  }
  return byDoc;
});

const downstreamDocs = computed(() => {
  const byDoc = new Map<string, string[]>();
  for (const dependency of documentDependencies.value) {
    byDoc.set(dependency.from, [...(byDoc.get(dependency.from) ?? []), dependency.to]);
  }
  return byDoc;
});

const graphColumns = computed(() => {
  const nodeTypes = [
    ...new Set([
      ...flowDocuments.value.map(stage => stage.sddType),
      ...documentDependencies.value.flatMap(dependency => [dependency.from, dependency.to]),
    ]),
  ];
  const order = new Map(flowDocuments.value.map((stage, index) => [stage.sddType, index]));
  const levels = new Map(nodeTypes.map(nodeType => [nodeType, 0]));

  for (let pass = 0; pass < nodeTypes.length; pass += 1) {
    for (const dependency of documentDependencies.value) {
      const fromLevel = levels.get(dependency.from) ?? 0;
      const toLevel = levels.get(dependency.to) ?? 0;
      if (toLevel <= fromLevel) {
        levels.set(dependency.to, fromLevel + 1);
      }
    }
  }

  const columns = new Map<number, string[]>();
  for (const nodeType of nodeTypes) {
    const level = levels.get(nodeType) ?? 0;
    columns.set(level, [...(columns.get(level) ?? []), nodeType]);
  }

  return [...columns.entries()]
    .sort(([left], [right]) => left - right)
    .map(([level, nodes]) => ({
      level,
      nodes: nodes
        .sort((left, right) => (order.get(left) ?? 999) - (order.get(right) ?? 999))
        .map(nodeType => ({
          type: nodeType,
          label: labelForDoc(nodeType),
          artifactType: stageByType.value.get(nodeType)?.artifactType ?? 'artifact',
          upstream: upstreamDocs.value.get(nodeType) ?? [],
          downstream: downstreamDocs.value.get(nodeType) ?? [],
        })),
    }));
});

function skillGroupFor(skill: SkillDocumentContract) {
  const id = skill.skillId;
  if (id.includes('normalizer') || id.includes('analyzer')) return 'intake-analysis';
  if (id.includes('functional-spec') || id.includes('technical-design') || id.includes('program-spec') || id.includes('file-spec')) {
    return 'spec-design';
  }
  if (id.includes('generator') || id.includes('test-scaffold')) return 'build-test';
  if (id.includes('reviewer') || id.includes('precheck') || id.includes('orchestrator')) return 'review-routing';
  return 'skills';
}

const skillGroups = computed(() => {
  const labels: Record<string, string> = {
    'intake-analysis': 'Intake & Analysis',
    'spec-design': 'Spec & Design',
    'build-test': 'Build & Test',
    'review-routing': 'Review & Routing',
    skills: 'Skills',
  };
  const order = ['intake-analysis', 'spec-design', 'build-test', 'review-routing', 'skills'];
  const groups = new Map<string, SkillDocumentContract[]>();

  for (const skill of skillContracts.value) {
    const groupId = skillGroupFor(skill);
    groups.set(groupId, [...(groups.get(groupId) ?? []), skill]);
  }

  return [...groups.entries()]
    .sort(([left], [right]) => order.indexOf(left) - order.indexOf(right))
    .map(([id, skills]) => ({
      id,
      label: labels[id] ?? id,
      skills,
    }));
});

const artifactGroups = computed(() => {
  const groups = new Map<string, Array<{
    stage: DocumentStageDefinition;
    producedCount: number;
    consumedCount: number;
    upstreamCount: number;
    downstreamCount: number;
  }>>();

  for (const stage of flowDocuments.value) {
    const type = stage.artifactType || 'artifact';
    groups.set(type, [
      ...(groups.get(type) ?? []),
      {
        stage,
        producedCount: producedBy.value.get(stage.sddType)?.length ?? 0,
        consumedCount: consumedBy.value.get(stage.sddType)?.length ?? 0,
        upstreamCount: upstreamDocs.value.get(stage.sddType)?.length ?? 0,
        downstreamCount: downstreamDocs.value.get(stage.sddType)?.length ?? 0,
      },
    ]);
  }

  return [...groups.entries()]
    .sort(([left], [right]) => {
      const leftIndex = ARTIFACT_TYPE_ORDER.indexOf(left);
      const rightIndex = ARTIFACT_TYPE_ORDER.indexOf(right);
      return (leftIndex === -1 ? 999 : leftIndex) - (rightIndex === -1 ? 999 : rightIndex);
    })
    .map(([type, items]) => ({
      type,
      label: ARTIFACT_TYPE_LABELS[type] ?? type,
      items,
    }));
});

function goBack() {
  router.push({ name: 'requirements' });
}

function labelForDoc(docType: string) {
  return stageByType.value.get(docType)?.label ?? docType;
}

function pathForDoc(docType: string) {
  return stageByType.value.get(docType)?.pathPattern ?? 'Not configured';
}

function labelForSkill(skillId: string) {
  return skillContracts.value.find(skill => skill.skillId === skillId)?.label
    ?? profile.value.skills.find(skill => skill.skillId === skillId)?.label
    ?? skillId;
}

function docListLabel(docTypes: ReadonlyArray<string>) {
  return docTypes.length ? docTypes.map(labelForDoc).join(', ') : 'None';
}

function skillListLabel(skillIds: ReadonlyArray<string>) {
  return skillIds.length ? skillIds.map(labelForSkill).join(', ') : 'None';
}

function visibleDocs(docTypes: ReadonlyArray<string>) {
  return docTypes.slice(0, MAX_VISIBLE_DOCS);
}

function extraDocCount(docTypes: ReadonlyArray<string>) {
  return Math.max(0, docTypes.length - MAX_VISIBLE_DOCS);
}

function visibleSkills(skillIds: ReadonlyArray<string>) {
  return skillIds.slice(0, MAX_VISIBLE_SKILLS);
}

function extraSkillCount(skillIds: ReadonlyArray<string>) {
  return Math.max(0, skillIds.length - MAX_VISIBLE_SKILLS);
}
</script>

<template>
  <div class="skill-flow-view">
    <button class="back-btn" type="button" @click="goBack">
      <ArrowLeft :size="14" />
      <span>Back to Requirements</span>
    </button>

    <section class="flow-header">
      <div>
        <span class="eyebrow">Requirement Capability Map</span>
        <h1>Skill & Document Flow</h1>
        <p>Skill input/output and dependency map for the active profile.</p>
      </div>
      <ProfileSelector
        :profiles="store.availableProfiles"
        :model-value="profile.id"
        @update:model-value="store.setActiveProfile"
      />
    </section>

    <section class="summary-grid">
      <div class="summary-item">
        <span>Profile</span>
        <strong>{{ profile.name }}</strong>
      </div>
      <div class="summary-item">
        <span>Skills</span>
        <strong>{{ skillContracts.length }}</strong>
      </div>
      <div class="summary-item">
        <span>Flow Docs</span>
        <strong>{{ flowDocuments.length }}</strong>
      </div>
      <div class="summary-item">
        <span>Dependencies</span>
        <strong>{{ documentDependencies.length }}</strong>
      </div>
    </section>

    <section class="flow-section">
      <div class="section-heading">
        <Workflow :size="16" />
        <span>Skills</span>
      </div>
      <div class="skill-groups">
        <section v-for="group in skillGroups" :key="group.id" class="skill-group">
          <div class="skill-group-head">
            <span>{{ group.label }}</span>
            <strong>{{ group.skills.length }}</strong>
          </div>
          <div class="skill-list">
            <article v-for="skill in group.skills" :key="skill.skillId" class="skill-row">
              <div class="skill-row-top">
                <div class="skill-title">
                  <strong>{{ skill.label }}</strong>
                  <span>{{ skill.skillId }}</span>
                </div>
                <span class="skill-deps">{{ skill.dependsOnSkills.length ? `${skill.dependsOnSkills.length} deps` : 'entry' }}</span>
              </div>
              <div class="io-flow">
                <div class="chip-row" :title="docListLabel(skill.inputDocuments)">
                  <span class="io-label"><FileInput :size="12" /> In</span>
                  <span v-for="docType in visibleDocs(skill.inputDocuments)" :key="`${skill.skillId}-in-${docType}`" class="flow-chip">
                    {{ labelForDoc(docType) }}
                  </span>
                  <span v-if="extraDocCount(skill.inputDocuments)" class="flow-chip flow-chip--more">
                    +{{ extraDocCount(skill.inputDocuments) }}
                  </span>
                  <span v-if="!skill.inputDocuments.length" class="muted">None</span>
                </div>
                <span class="flow-arrow">→</span>
                <div class="chip-row" :title="docListLabel(skill.outputDocuments)">
                  <span class="io-label"><FileOutput :size="12" /> Out</span>
                  <span v-for="docType in visibleDocs(skill.outputDocuments)" :key="`${skill.skillId}-out-${docType}`" class="flow-chip flow-chip--output">
                    {{ labelForDoc(docType) }}
                  </span>
                  <span v-if="extraDocCount(skill.outputDocuments)" class="flow-chip flow-chip--more">
                    +{{ extraDocCount(skill.outputDocuments) }}
                  </span>
                  <span v-if="!skill.outputDocuments.length" class="muted">None</span>
                </div>
              </div>
              <div v-if="skill.dependsOnSkills.length" class="upstream-line" :title="skillListLabel(skill.dependsOnSkills)">
                <span>After</span>
                <span v-for="skillId in visibleSkills(skill.dependsOnSkills)" :key="`${skill.skillId}-dep-${skillId}`" class="skill-chip">
                  {{ labelForSkill(skillId) }}
                </span>
                <span v-if="extraSkillCount(skill.dependsOnSkills)" class="skill-chip skill-chip--more">
                  +{{ extraSkillCount(skill.dependsOnSkills) }}
                </span>
              </div>
            </article>
          </div>
        </section>
      </div>
    </section>

    <section class="flow-section">
      <div class="section-heading">
        <GitBranch :size="16" />
        <span>Document Flow Map</span>
      </div>
      <div class="flow-map">
        <div v-for="(column, columnIndex) in graphColumns" :key="column.level" class="flow-column">
          <div class="column-label">Step {{ columnIndex + 1 }}</div>
          <article
            v-for="node in column.nodes"
            :key="node.type"
            class="flow-node"
            :title="pathForDoc(node.type)"
          >
            <div class="node-top">
              <strong>{{ node.label }}</strong>
              <span>{{ node.artifactType }}</span>
            </div>
            <div class="node-counts">
              <span v-if="node.upstream.length">in {{ node.upstream.length }}</span>
              <span v-if="node.downstream.length">out {{ node.downstream.length }}</span>
              <span v-if="!node.upstream.length && !node.downstream.length">standalone</span>
            </div>
            <div v-if="node.downstream.length" class="node-next" :title="docListLabel(node.downstream)">
              <span class="next-label">to</span>
              <span v-for="target in visibleDocs(node.downstream)" :key="`${node.type}-${target}`" class="next-chip">
                {{ labelForDoc(target) }}
              </span>
              <span v-if="extraDocCount(node.downstream)" class="next-chip next-chip--more">
                +{{ extraDocCount(node.downstream) }}
              </span>
            </div>
          </article>
        </div>
      </div>
    </section>

    <section class="flow-section">
      <div class="section-heading">
        <FileOutput :size="16" />
        <span>Artifact Summary</span>
      </div>
      <div class="artifact-groups">
        <section v-for="group in artifactGroups" :key="group.type" class="artifact-group">
          <div class="artifact-group-head">
            <span>{{ group.label }}</span>
            <strong>{{ group.items.length }}</strong>
          </div>
          <div class="artifact-list">
            <article
              v-for="item in group.items"
              :key="item.stage.sddType"
              class="artifact-pill"
              :title="pathForDoc(item.stage.sddType)"
            >
              <div class="artifact-main">
                <strong>{{ item.stage.label }}</strong>
                <span>{{ item.stage.sddType }}</span>
              </div>
              <div class="artifact-stats">
                <span>P {{ item.producedCount }}</span>
                <span>U {{ item.consumedCount }}</span>
                <span>In {{ item.upstreamCount }}</span>
                <span>Out {{ item.downstreamCount }}</span>
              </div>
            </article>
          </div>
        </section>
      </div>
    </section>
  </div>
</template>

<style scoped>
.skill-flow-view {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 0 24px 24px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  align-self: flex-start;
  border: none;
  background: none;
  color: var(--color-secondary);
  cursor: pointer;
  font-family: var(--font-ui);
  font-size: 0.8125rem;
  padding: 4px 0;
}

.flow-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 18px;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-high);
}

.eyebrow,
.section-heading,
.summary-item span,
.skill-id,
.skill-deps,
.dependency-line span,
.io-box span,
.artifact-main span,
.artifact-group-head {
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.625rem;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

h1 {
  margin: 4px 0 6px;
  color: var(--color-on-surface);
  font-size: 1.25rem;
  line-height: 1.25;
}

p {
  margin: 0;
  max-width: 760px;
  color: var(--color-on-surface-variant);
  font-size: 0.8125rem;
  line-height: 1.5;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.summary-item,
.flow-section,
.skill-group {
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container);
}

.summary-item {
  padding: 12px;
}

.summary-item strong {
  display: block;
  margin-top: 5px;
  color: var(--color-on-surface);
  font-family: var(--font-tech);
  font-size: 1rem;
}

.flow-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 14px;
}

.section-heading {
  display: flex;
  align-items: center;
  gap: 8px;
}

.skill-groups {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.skill-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
  padding: 10px;
  background: var(--color-surface-container-low);
}

.skill-group-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.skill-group-head span {
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.625rem;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.skill-group-head strong {
  padding: 1px 6px;
  border-radius: 2px;
  background: rgba(148, 163, 184, 0.12);
  color: var(--color-on-surface);
  font-family: var(--font-tech);
  font-size: 0.625rem;
}

.skill-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.skill-row {
  display: flex;
  flex-direction: column;
  gap: 7px;
  min-width: 0;
  padding: 9px;
  border-radius: var(--radius-sm);
  background: var(--color-surface-container);
}

.skill-row-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
}

.skill-title {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 2px;
}

.skill-title strong,
.skill-title span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.skill-title strong {
  color: var(--color-on-surface);
  font-family: var(--font-ui);
  font-size: 0.8125rem;
  line-height: 1.25;
}

.skill-title span {
  color: var(--color-on-surface-variant);
  font-family: var(--font-tech);
  font-size: 0.5625rem;
  text-transform: uppercase;
}

.skill-deps {
  flex: 0 0 auto;
  padding: 2px 6px;
  border-radius: 2px;
  background: rgba(137, 206, 255, 0.1);
  color: var(--color-secondary);
}

.io-flow {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 16px minmax(0, 1fr);
  gap: 6px;
  align-items: start;
}

.chip-row {
  grid-column: 1 / -1;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  min-width: 0;
}

.io-label {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 3px 0;
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  line-height: 1.2;
  text-transform: uppercase;
}

.flow-arrow {
  color: var(--color-on-surface-variant);
  font-family: var(--font-tech);
  font-size: 0.75rem;
  line-height: 1.8;
  text-align: center;
}

.flow-chip,
.skill-chip,
.muted {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  border-radius: 2px;
  font-family: var(--font-ui);
  font-size: 0.625rem;
  line-height: 1.2;
}

.flow-chip,
.skill-chip {
  padding: 3px 6px;
  color: var(--color-secondary);
  background: rgba(137, 206, 255, 0.1);
}

.flow-chip--output {
  color: var(--color-health-emerald);
  background: rgba(78, 222, 163, 0.1);
}

.flow-chip--more,
.skill-chip--more {
  color: var(--color-on-surface-variant);
  background: rgba(148, 163, 184, 0.12);
}

.skill-chip {
  color: #c4b5fd;
  background: rgba(167, 139, 250, 0.1);
}

.muted {
  color: var(--color-on-surface-variant);
}

.upstream-line {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
  padding-top: 2px;
}

.upstream-line > span:first-child {
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
}

.flow-map {
  display: flex;
  gap: 14px;
  overflow-x: auto;
  padding: 4px 2px 8px;
}

.flow-column {
  position: relative;
  display: flex;
  flex: 1 0 214px;
  min-width: 214px;
  flex-direction: column;
  gap: 8px;
}

.flow-column:not(:last-child)::after {
  content: '→';
  position: absolute;
  top: 42px;
  right: -13px;
  color: var(--color-on-surface-variant);
  font-family: var(--font-tech);
  font-size: 0.75rem;
  opacity: 0.7;
}

.column-label {
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.flow-node {
  display: flex;
  min-height: 92px;
  flex-direction: column;
  gap: 7px;
  padding: 10px;
  border: var(--border-ghost);
  border-left: 2px solid var(--color-secondary);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
}

.node-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.node-top strong {
  min-width: 0;
  overflow: hidden;
  color: var(--color-on-surface);
  font-family: var(--font-ui);
  font-size: 0.8125rem;
  line-height: 1.25;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.node-top span {
  flex: 0 0 auto;
  max-width: 76px;
  overflow: hidden;
  padding: 2px 5px;
  border-radius: 2px;
  background: rgba(148, 163, 184, 0.12);
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  line-height: 1.2;
  text-overflow: ellipsis;
  text-transform: uppercase;
  white-space: nowrap;
}

.node-counts {
  display: flex;
  gap: 6px;
  color: var(--color-on-surface-variant);
  font-family: var(--font-tech);
  font-size: 0.625rem;
}

.node-counts span {
  padding: 1px 5px;
  border-radius: 2px;
  background: rgba(148, 163, 184, 0.08);
}

.node-next {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: auto;
}

.next-label,
.next-chip {
  border-radius: 2px;
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  line-height: 1.2;
}

.next-label {
  padding: 3px 0;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
}

.next-chip {
  max-width: 132px;
  overflow: hidden;
  padding: 3px 6px;
  background: rgba(78, 222, 163, 0.1);
  color: var(--color-health-emerald);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.next-chip--more {
  background: rgba(148, 163, 184, 0.12);
  color: var(--color-on-surface-variant);
}

.artifact-groups {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.artifact-group {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 8px;
  padding: 10px;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
}

.artifact-group-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.artifact-group-head strong {
  padding: 1px 6px;
  border-radius: 2px;
  background: rgba(148, 163, 184, 0.12);
  color: var(--color-on-surface);
  font-family: var(--font-tech);
  font-size: 0.625rem;
}

.artifact-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.artifact-pill {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 8px;
  align-items: center;
  padding: 8px;
  border-radius: var(--radius-sm);
  background: var(--color-surface-container);
}

.artifact-main {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 2px;
}

.artifact-main strong,
.artifact-main span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.artifact-main strong {
  color: var(--color-on-surface);
  font-family: var(--font-ui);
  font-size: 0.75rem;
}

.artifact-stats {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 3px;
  max-width: 146px;
}

.artifact-stats span {
  padding: 2px 5px;
  border-radius: 2px;
  background: rgba(137, 206, 255, 0.08);
  color: var(--color-secondary);
  font-family: var(--font-tech);
  font-size: 0.5625rem;
  line-height: 1.2;
}

@media (max-width: 1180px) {
  .skill-contracts,
  .skill-groups,
  .summary-grid,
  .artifact-groups {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .flow-header,
  .skill-row-top {
    flex-direction: column;
  }

  .summary-grid,
  .skill-contracts,
  .skill-groups,
  .io-flow,
  .artifact-groups {
    grid-template-columns: 1fr;
  }

  .flow-column {
    flex-basis: 200px;
    min-width: 200px;
  }
}
</style>
