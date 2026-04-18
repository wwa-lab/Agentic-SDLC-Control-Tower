<script setup lang="ts">
import type { HealthLed as HealthLedType } from '../../types/enums';

defineProps<{ led: HealthLedType }>();

const COLOR: Record<HealthLedType, string> = {
  GREEN: 'var(--color-health-emerald)',
  AMBER: 'var(--color-approval-amber)',
  RED: 'var(--color-incident-crimson)',
  UNKNOWN: 'var(--dp-state-pending)',
};
</script>

<template>
  <span class="health-led" :style="{ '--led-color': COLOR[led] }" :aria-label="`Health: ${led}`">
    <span class="dot" />
  </span>
</template>

<style scoped>
.health-led { display: inline-flex; align-items: center; }
.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--led-color);
  box-shadow: 0 0 6px var(--led-color);
}
</style>
