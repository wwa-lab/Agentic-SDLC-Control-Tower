<script setup lang="ts">
import { computed } from 'vue';
import type { ChainNodeHealth } from '../types/pipeline';
import type { HealthAggregate, SdlcNodeKey } from '../types/enums';

interface Props {
  nodes: ReadonlyArray<ChainNodeHealth>;
  compact?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  compact: false,
});

const ORDER: ReadonlyArray<SdlcNodeKey> = [
  'REQUIREMENT',
  'USER_STORY',
  'SPEC',
  'ARCHITECTURE',
  'DESIGN',
  'TASKS',
  'CODE',
  'TEST',
  'DEPLOY',
  'INCIDENT',
  'LEARNING',
];

function fallbackNode(nodeKey: SdlcNodeKey): ChainNodeHealth {
  return {
    nodeKey,
    health: 'UNKNOWN',
    isExecutionHub: nodeKey === 'SPEC',
  };
}

function healthClass(health: HealthAggregate): string {
  switch (health) {
    case 'GREEN':
      return 'node--green';
    case 'YELLOW':
      return 'node--yellow';
    case 'RED':
      return 'node--red';
    default:
      return 'node--unknown';
  }
}

const orderedNodes = computed(() =>
  ORDER.map(nodeKey => props.nodes.find(node => node.nodeKey === nodeKey) ?? fallbackNode(nodeKey)),
);
</script>

<template>
  <div class="sdlc-strip" :class="{ 'sdlc-strip--compact': compact }">
    <div
      v-for="node in orderedNodes"
      :key="node.nodeKey"
      class="sdlc-strip__node"
      :class="[healthClass(node.health), { 'sdlc-strip__node--hub': node.isExecutionHub }]"
    >
      <span class="sdlc-strip__led"></span>
      <span class="text-label">{{ node.nodeKey.replace('_', ' ') }}</span>
    </div>
  </div>
</template>

<style scoped>
.sdlc-strip {
  display: grid;
  grid-template-columns: repeat(11, minmax(0, 1fr));
  gap: 10px;
}

.sdlc-strip--compact {
  grid-template-columns: repeat(auto-fit, minmax(90px, 1fr));
}

.sdlc-strip__node {
  min-height: 58px;
  border-radius: var(--radius-sm);
  border: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(255, 255, 255, 0.02);
  padding: 10px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 8px;
}

.sdlc-strip__node--hub {
  box-shadow: inset 0 0 0 1px rgba(137, 206, 255, 0.4);
}

.sdlc-strip__led {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.node--green .sdlc-strip__led {
  background: var(--color-health-emerald);
}

.node--yellow .sdlc-strip__led {
  background: var(--color-approval-amber);
}

.node--red .sdlc-strip__led {
  background: var(--color-incident-crimson);
}

.node--unknown .sdlc-strip__led {
  background: var(--color-on-surface-variant);
  opacity: 0.45;
}

@media (max-width: 1400px) {
  .sdlc-strip {
    grid-template-columns: repeat(auto-fit, minmax(96px, 1fr));
  }
}
</style>
