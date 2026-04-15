<script setup lang="ts">
import { Cpu, Wifi, Shield } from 'lucide-vue-next';

interface Props {
  v?: string;
  node?: string;
  latency?: string;
}

const props = withDefaults(defineProps<Props>(), {
  v: 'v1.0.4-LATEST',
  node: 'NY-WEST-04',
  latency: '14ms'
});
</script>

<template>
  <div class="data-ribbon">
    <div class="ribbon-segment">
      <Cpu :size="10" />
      <span class="text-tech">{{ props.node }}</span>
    </div>
    <div class="divider">|</div>
    <div class="ribbon-segment">
      <Wifi :size="10" />
      <span class="text-tech">LATENCY: {{ props.latency }}</span>
    </div>
    <div class="divider">|</div>
    <div class="ribbon-segment">
      <Shield :size="10" />
      <span class="text-tech">{{ props.v }}</span>
    </div>
    <div class="spacer"></div>
    <div class="ribbon-segment">
      <span class="text-label">SECURE_CHANNEL_ACTIVE</span>
      <span class="led led-emerald"></span>
    </div>
  </div>
</template>

<style scoped>
.data-ribbon {
  height: 24px;
  background: var(--color-surface-lowest);
  border-bottom: var(--border-ghost);
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 12px;
  color: var(--color-on-surface-variant);
  user-select: none;
}

.ribbon-segment {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.625rem;
}

.divider {
  opacity: 0.2;
  font-size: 0.75rem;
  font-weight: 200;
}

.spacer {
  flex: 1;
}

.text-tech {
  letter-spacing: 0.02em;
}
</style>
