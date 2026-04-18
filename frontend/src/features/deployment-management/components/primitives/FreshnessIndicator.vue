<script setup lang="ts">
import { computed } from 'vue';
import type { FreshnessTier } from '../../types/enums';

const props = defineProps<{ lastIngestedAt: string | null }>();

const tier = computed<FreshnessTier>(() => {
  if (!props.lastIngestedAt) return 'STALE';
  const ageSec = (Date.now() - new Date(props.lastIngestedAt).getTime()) / 1000;
  if (ageSec <= 45) return 'FRESH';
  if (ageSec <= 300) return 'DEGRADED';
  return 'STALE';
});

const CONFIG: Record<FreshnessTier, { label: string; color: string }> = {
  FRESH: { label: 'Fresh', color: 'var(--dp-freshness-fresh)' },
  DEGRADED: { label: 'Degraded', color: 'var(--dp-freshness-degraded)' },
  STALE: { label: 'Stale', color: 'var(--dp-freshness-stale)' },
};

const cfg = computed(() => CONFIG[tier.value]);
</script>

<template>
  <span class="freshness" :style="{ color: cfg.color }" :aria-label="`Data freshness: ${cfg.label}`">
    <span class="dot" :style="{ background: cfg.color }" />
    {{ cfg.label }}
  </span>
</template>

<style scoped>
.freshness {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-family: var(--font-ui);
  font-size: 0.65rem;
  font-weight: 600;
  text-transform: uppercase;
}
.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}
</style>
