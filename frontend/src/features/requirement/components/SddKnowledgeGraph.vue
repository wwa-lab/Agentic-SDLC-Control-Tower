<script setup lang="ts">
import { computed, ref } from 'vue';
import type {
  DocumentDependencyDefinition,
  DocumentStageDefinition,
  GraphEdge as BackendGraphEdge,
  GraphNode as BackendGraphNode,
  KnowledgeGraph,
  PipelineProfile,
  RequirementControlPlaneListSummary,
  RequirementListItem,
} from '../types/requirement';

interface RenderGraphNode {
  readonly id: string;
  readonly label: string;
  readonly meta: string;
  readonly tier: string | null;
  readonly artifactType: string;
  readonly path: string | null;
  readonly properties: Record<string, unknown>;
  readonly x: number;
  readonly y: number;
  readonly level: number;
}

interface RenderGraphEdge {
  readonly id: string;
  readonly from: RenderGraphNode;
  readonly to: RenderGraphNode;
  readonly label: string;
  readonly source: string;
}

const props = defineProps<{
  profile: PipelineProfile;
  requirements: ReadonlyArray<RequirementListItem>;
  summaries: Record<string, RequirementControlPlaneListSummary>;
  graph?: KnowledgeGraph | null;
  loading?: boolean;
  error?: string | null;
}>();

const selectedNodeId = ref<string | null>(null);

const columnGap = 296;
const rowGap = 82;
const marginX = 76;
const marginY = 54;

const summaries = computed(() => Object.values(props.summaries));

const graphMetrics = computed(() => {
  const total = summaries.value.length;
  return {
    requirements: props.requirements.length,
    indexedDocs: summaries.value.reduce((sum, item) => sum + item.documentCount, 0),
    missingDocs: summaries.value.reduce((sum, item) => sum + item.missingDocumentCount, 0),
    staleReviews: summaries.value.reduce((sum, item) => sum + item.staleReviewCount, 0),
    aligned: summaries.value.filter(item => item.status === 'FRESH').length,
    total,
  };
});

const stageByType = computed(() =>
  new Map(props.profile.documentStages.map(stage => [stage.sddType, stage]))
);

const graphLevels = computed(() => {
  const incoming = new Map<string, string[]>();
  props.profile.documentDependencies?.forEach(dep => {
    incoming.set(dep.to, [...(incoming.get(dep.to) ?? []), dep.from]);
  });

  const cache = new Map<string, number>();
  const visit = (id: string, stack = new Set<string>()): number => {
    if (cache.has(id)) return cache.get(id)!;
    if (stack.has(id)) return 0;
    const nextStack = new Set(stack).add(id);
    const parents = incoming.get(id)?.filter(parent => stageByType.value.has(parent)) ?? [];
    const level = parents.length === 0 ? 0 : Math.max(...parents.map(parent => visit(parent, nextStack) + 1));
    cache.set(id, level);
    return level;
  };

  props.profile.documentStages.forEach(stage => visit(stage.sddType));
  return cache;
});

const maxLevel = computed(() => Math.max(0, ...graphLevels.value.values()));

const profileNodes = computed<RenderGraphNode[]>(() => {
  const byLevel = new Map<number, DocumentStageDefinition[]>();
  props.profile.documentStages.forEach(stage => {
    const level = graphLevels.value.get(stage.sddType) ?? 0;
    byLevel.set(level, [...(byLevel.get(level) ?? []), stage]);
  });

  return [...byLevel.entries()].flatMap(([level, stages]) => {
    const x = marginX + (maxLevel.value === 0 ? 0 : level * columnGap);
    return stages.map((stage, index) => ({
      id: stage.sddType,
      label: stage.label,
      meta: `${stage.traceabilityKey ?? stage.sddType} · ${stage.artifactType}`,
      tier: stage.expectedTier ?? null,
      artifactType: stage.artifactType,
      path: stage.pathPattern,
      properties: { docType: stage.sddType },
      x,
      y: marginY + index * rowGap,
      level,
    }));
  });
});

function nodeDocType(node: BackendGraphNode): string {
  const docType = node.properties.docType;
  return typeof docType === 'string' ? docType : node.id;
}

const backendLevels = computed(() => {
  const incoming = new Map<string, string[]>();
  props.graph?.edges.forEach(edge => {
    incoming.set(edge.from, [...(incoming.get(edge.from) ?? []), edge.to]);
  });
  const cache = new Map<string, number>();
  const visit = (id: string, stack = new Set<string>()): number => {
    if (cache.has(id)) return cache.get(id)!;
    if (stack.has(id)) return 0;
    const parents = incoming.get(id) ?? [];
    const nextStack = new Set(stack).add(id);
    const level = parents.length === 0 ? 0 : Math.max(...parents.map(parent => visit(parent, nextStack) + 1));
    cache.set(id, level);
    return level;
  };
  props.graph?.nodes.forEach(node => visit(node.id));
  return cache;
});

const backendNodes = computed<RenderGraphNode[]>(() => {
  if (!props.graph?.nodes.length) return [];
  const byLevel = new Map<number, BackendGraphNode[]>();
  props.graph.nodes.forEach(node => {
    const level = backendLevels.value.get(node.id) ?? 0;
    byLevel.set(level, [...(byLevel.get(level) ?? []), node]);
  });
  return [...byLevel.entries()].flatMap(([level, graphNodes]) => {
    const x = marginX + level * columnGap;
    return graphNodes.map((node, index) => ({
      id: node.id,
      label: node.label,
      meta: `${nodeDocType(node)} · ${node.kind}`,
      tier: typeof node.properties.expectedTier === 'string' ? node.properties.expectedTier : null,
      artifactType: typeof node.properties.artifactType === 'string' ? node.properties.artifactType : node.kind.toLowerCase(),
      path: typeof node.properties.path === 'string'
        ? node.properties.path
        : typeof node.properties.pathPattern === 'string'
          ? node.properties.pathPattern
          : null,
      properties: node.properties,
      x,
      y: marginY + index * rowGap,
      level,
    }));
  });
});

const nodes = computed<RenderGraphNode[]>(() => backendNodes.value.length > 0 ? backendNodes.value : profileNodes.value);
const renderMaxLevel = computed(() => Math.max(0, ...nodes.value.map(node => node.level)));

const graphWidth = computed(() => Math.max(980, marginX * 2 + renderMaxLevel.value * columnGap + 260));

const nodeById = computed(() => new Map(nodes.value.map(node => [node.id, node])));

const profileEdges = computed<RenderGraphEdge[]>(() =>
  (props.profile.documentDependencies ?? [])
    .map(dependency => {
      const from = nodeById.value.get(dependency.from);
      const to = nodeById.value.get(dependency.to);
      return from && to ? { id: `${from.id}-${to.id}`, from, to, label: dependency.reason, source: 'profile' } : null;
    })
    .filter((edge): edge is RenderGraphEdge => edge != null)
);

const backendEdges = computed<RenderGraphEdge[]>(() =>
  (props.graph?.edges ?? [])
    .map((edge: BackendGraphEdge) => {
      const from = nodeById.value.get(edge.from);
      const to = nodeById.value.get(edge.to);
      return from && to ? { id: edge.id, from, to, label: edge.type, source: edge.source } : null;
    })
    .filter((edge): edge is RenderGraphEdge => edge != null)
);

const edges = computed(() => backendEdges.value.length > 0 ? backendEdges.value : profileEdges.value);

const graphHeight = computed(() => {
  const maxY = nodes.value.reduce((max, node) => Math.max(max, node.y), 0);
  return Math.max(420, maxY + marginY);
});

const selectedNode = computed(() =>
  selectedNodeId.value ? nodeById.value.get(selectedNodeId.value) ?? null : null
);

const selectedIncoming = computed(() =>
  selectedNode.value ? edges.value.filter(edge => edge.to.id === selectedNode.value?.id) : []
);

const selectedOutgoing = computed(() =>
  selectedNode.value ? edges.value.filter(edge => edge.from.id === selectedNode.value?.id) : []
);

function edgePath(edge: RenderGraphEdge) {
  const startX = edge.from.x + 112;
  const startY = edge.from.y;
  const endX = edge.to.x - 112;
  const endY = edge.to.y;
  const curve = Math.max(52, Math.abs(endX - startX) * 0.5);
  return `M ${startX} ${startY} C ${startX + curve} ${startY}, ${endX - curve} ${endY}, ${endX} ${endY}`;
}

function nodeClass(node: RenderGraphNode) {
  return {
    'graph-node--selected': selectedNodeId.value === node.id,
    'graph-node--review': node.artifactType === 'review',
    'graph-node--code': node.artifactType === 'code',
  };
}
</script>

<template>
  <section class="knowledge-graph">
    <div class="graph-toolbar">
      <div>
        <h2>SDD Knowledge Graph</h2>
        <p>{{ graph?.scope.provider ?? 'profile' }} · {{ profile.name }} · {{ nodes.length }} nodes · {{ edges.length }} relations</p>
      </div>
      <div class="graph-metrics">
        <span><strong>{{ graphMetrics.indexedDocs }}</strong> indexed</span>
        <span><strong>{{ graphMetrics.missingDocs }}</strong> missing</span>
        <span><strong>{{ graphMetrics.staleReviews }}</strong> stale</span>
        <span><strong>{{ graphMetrics.aligned }}/{{ graphMetrics.total }}</strong> aligned</span>
      </div>
    </div>

    <div v-if="error" class="graph-alert graph-alert--warning">{{ error }}</div>
    <div v-else-if="graph?.health.stale" class="graph-alert graph-alert--warning">Graph artifacts are stale.</div>
    <div v-else-if="graph && graph.nodes.length === 0" class="graph-alert">No graph nodes match the current filters.</div>
    <div v-if="graph?.issues.length" class="graph-issues">
      <span v-for="issue in graph.issues.slice(0, 3)" :key="issue.id">{{ issue.severity }} · {{ issue.message }}</span>
    </div>

    <div class="graph-layout">
      <div class="graph-canvas" :class="{ 'graph-canvas--loading': loading }">
        <svg
          class="graph-svg"
          :viewBox="`0 0 ${graphWidth} ${graphHeight}`"
          role="img"
          aria-label="SDD document relationship graph"
        >
          <defs>
            <marker id="graph-arrow" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
              <path d="M0,0 L9,4.5 L0,9 z" class="arrow-head" />
            </marker>
          </defs>

          <path
            v-for="edge in edges"
            :key="`${edge.from.id}-${edge.to.id}`"
            class="graph-edge"
            :class="{ 'graph-edge--active': selectedNodeId === edge.from.id || selectedNodeId === edge.to.id }"
            :d="edgePath(edge)"
            marker-end="url(#graph-arrow)"
          />

          <g
            v-for="node in nodes"
            :key="node.id"
            class="graph-node"
            :class="nodeClass(node)"
            :transform="`translate(${node.x - 112}, ${node.y - 28})`"
            tabindex="0"
            role="button"
            @click="selectedNodeId = node.id"
            @keydown.enter.prevent="selectedNodeId = node.id"
          >
            <rect width="224" height="56" rx="6" />
            <text x="14" y="22" class="node-title">{{ node.label }}</text>
            <text x="14" y="40" class="node-meta">
              {{ node.meta }}
            </text>
            <text v-if="node.tier" x="188" y="22" class="node-tier">{{ node.tier }}</text>
          </g>
        </svg>
      </div>

      <aside class="graph-detail">
        <template v-if="selectedNode">
          <span class="detail-kicker">Selected Document</span>
          <h3>{{ selectedNode.label }}</h3>
          <div class="detail-row"><span>Type</span><strong>{{ selectedNode.properties.docType ?? selectedNode.id }}</strong></div>
          <div class="detail-row"><span>Trace Key</span><strong>{{ selectedNode.properties.traceabilityKey ?? 'N/A' }}</strong></div>
          <div class="detail-row"><span>Tier</span><strong>{{ selectedNode.tier ?? 'Profile default' }}</strong></div>
          <div class="detail-path">{{ selectedNode.path ?? selectedNode.id }}</div>

          <div class="relation-block">
            <span class="detail-kicker">Depends On</span>
            <p v-if="selectedIncoming.length === 0">None</p>
            <button
              v-for="edge in selectedIncoming"
              v-else
              :key="edge.from.id"
              type="button"
              @click="selectedNodeId = edge.from.id"
            >
              {{ edge.from.label }}
            </button>
          </div>

          <div class="relation-block">
            <span class="detail-kicker">Feeds</span>
            <p v-if="selectedOutgoing.length === 0">None</p>
            <button
              v-for="edge in selectedOutgoing"
              v-else
              :key="edge.to.id"
              type="button"
              @click="selectedNodeId = edge.to.id"
            >
              {{ edge.to.label }}
            </button>
          </div>
        </template>

        <template v-else>
          <span class="detail-kicker">Graph Source</span>
          <h3>SDD relationship map</h3>
          <div class="detail-row"><span>Source Repo</span><strong>SDD documents</strong></div>
          <div class="detail-row"><span>Index Repo</span><strong>Structured SDD index</strong></div>
          <div class="detail-row"><span>Graph Engine</span><strong>Neo4j ready</strong></div>
          <div class="detail-path">Click a node to inspect upstream and downstream document impact.</div>
        </template>
      </aside>
    </div>
  </section>
</template>

<style scoped>
.knowledge-graph {
  display: grid;
  gap: 12px;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
  padding: 16px;
}

.graph-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}

.graph-toolbar h2 {
  margin: 0;
  color: var(--color-on-surface);
  font-family: var(--font-ui);
  font-size: 1rem;
  font-weight: 700;
}

.graph-toolbar p {
  margin: 4px 0 0;
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.75rem;
}

.graph-metrics {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.graph-metrics span {
  min-height: 28px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 9px;
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-high);
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.75rem;
}

.graph-metrics strong { color: var(--color-secondary); font-family: var(--font-tech); }

.graph-alert,
.graph-issues {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px 10px;
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-high);
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.75rem;
}

.graph-alert--warning {
  border: 1px solid rgba(245, 158, 11, 0.34);
  color: var(--color-approval-amber);
}

.graph-issues span {
  padding: 3px 7px;
  border-radius: 2px;
  background: rgba(245, 158, 11, 0.1);
  color: var(--color-approval-amber);
}

.graph-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 12px;
}

.graph-canvas {
  min-height: 420px;
  overflow: auto;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
}

.graph-canvas--loading { opacity: 0.62; }

.graph-svg {
  display: block;
  min-width: 940px;
  width: 100%;
  height: auto;
}

.graph-edge {
  fill: none;
  stroke: rgba(137, 206, 255, 0.28);
  stroke-width: 1.4;
}

.graph-edge--active {
  stroke: var(--color-secondary);
  stroke-width: 2.4;
}

.arrow-head { fill: rgba(137, 206, 255, 0.55); }

.graph-node { cursor: pointer; outline: none; }
.graph-node rect {
  fill: var(--color-surface-container-high);
  stroke: rgba(137, 206, 255, 0.28);
  stroke-width: 1;
}

.graph-node:hover rect,
.graph-node:focus rect,
.graph-node--selected rect {
  fill: rgba(137, 206, 255, 0.14);
  stroke: var(--color-secondary);
  stroke-width: 2;
}

.graph-node--review rect { stroke: rgba(245, 158, 11, 0.45); }
.graph-node--code rect { stroke: rgba(78, 222, 163, 0.45); }

.node-title {
  fill: var(--color-on-surface);
  font-family: var(--font-ui);
  font-size: 12px;
  font-weight: 700;
}

.node-meta,
.node-tier {
  fill: var(--color-on-surface-variant);
  font-family: var(--font-tech);
  font-size: 10px;
}

.node-tier { fill: var(--color-secondary); font-weight: 700; }

.graph-detail {
  min-height: 420px;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-high);
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-kicker {
  color: var(--color-on-surface-variant);
  font-family: var(--font-tech);
  font-size: 0.625rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.graph-detail h3 {
  margin: -6px 0 2px;
  color: var(--color-on-surface);
  font-family: var(--font-ui);
  font-size: 0.9375rem;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.75rem;
}

.detail-row strong {
  color: var(--color-on-surface);
  font-weight: 600;
  text-align: right;
}

.detail-path {
  padding: 10px;
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
  color: var(--color-on-surface-variant);
  font-family: var(--font-tech);
  font-size: 0.6875rem;
  line-height: 1.4;
}

.relation-block {
  display: flex;
  flex-direction: column;
  gap: 7px;
  padding-top: 4px;
}

.relation-block p {
  margin: 0;
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.75rem;
}

.relation-block button {
  min-height: 30px;
  border: 1px solid rgba(137, 206, 255, 0.28);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
  color: var(--color-secondary);
  cursor: pointer;
  font-family: var(--font-ui);
  font-size: 0.75rem;
  text-align: left;
  padding: 6px 8px;
}

.relation-block button:hover { background: var(--color-secondary-tint); }

@media (max-width: 980px) {
  .graph-layout { grid-template-columns: 1fr; }
  .graph-detail { min-height: auto; }
}
</style>
