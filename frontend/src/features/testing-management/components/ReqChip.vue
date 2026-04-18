<script setup lang="ts">
import { computed } from 'vue';
import { RouterLink } from 'vue-router';
import type { RequirementChip } from '../types';
import { coverageLabel, toCoverageVars } from '../utils';

interface Props {
  chip: RequirementChip;
}

const props = defineProps<Props>();
const colors = computed(() => toCoverageVars(props.chip.chipColor));
const tooltip = computed(() => `${props.chip.reqId} • ${props.chip.title} • ${coverageLabel(props.chip.chipColor)} • ${props.chip.linkStatus}`);
</script>

<template>
  <RouterLink
    :to="chip.routePath"
    class="tm-req-chip"
    :style="{ '--tm-tone': colors.tone, '--tm-background': colors.background }"
    :title="tooltip"
  >
    <span class="tm-req-chip__dot"></span>
    <span>{{ chip.reqId }}</span>
  </RouterLink>
</template>

<style scoped>
.tm-req-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  border-radius: 999px;
  background: var(--tm-background);
  color: var(--tm-tone);
  text-decoration: none;
  border: 1px solid color-mix(in srgb, var(--tm-tone) 24%, transparent);
  font-size: 0.75rem;
  font-weight: 600;
}

.tm-req-chip__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--tm-tone);
}
</style>
