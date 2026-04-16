<script setup lang="ts">
import { computed } from 'vue';
import type { Lineage } from '@/shared/types/lineage';

interface Props {
  lineage: Lineage;
  compact?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  compact: true,
});

const originLabel = computed(() => props.lineage.origin.replace('_', ' '));
const badgeLabel = computed(() => {
  if (props.lineage.overridden) {
    return props.compact ? `${originLabel.value} override` : `${originLabel.value} override active`;
  }
  return props.compact ? originLabel.value : `Inherited from ${originLabel.value}`;
});

const tooltip = computed(() => {
  if (props.lineage.chain.length === 0) {
    return badgeLabel.value;
  }
  return props.lineage.chain
    .map(hop => {
      const meta = [hop.setBy, hop.setAt].filter(Boolean).join(' @ ');
      return `${hop.origin}: ${hop.value}${meta ? ` (${meta})` : ''}`;
    })
    .join('\n');
});
</script>

<template>
  <span class="lineage-badge text-label" :title="tooltip">
    {{ badgeLabel }}
  </span>
</template>

<style scoped>
.lineage-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 999px;
  border: 1px solid rgba(137, 206, 255, 0.22);
  background: rgba(137, 206, 255, 0.08);
  color: var(--color-secondary);
  white-space: nowrap;
}
</style>
