<script setup lang="ts">
import { Cpu, Wifi, Shield } from 'lucide-vue-next';

interface Props {
  node?: string;
  latency?: string;
  version?: string;
  secureChannel?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  node: '---',
  latency: '---',
  version: '---',
  secureChannel: true,
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
      <span class="text-tech">{{ props.version }}</span>
    </div>
    <div class="spacer"></div>
    <div class="ribbon-segment">
      <span class="text-label">{{ props.secureChannel ? 'SECURE_CHANNEL_ACTIVE' : 'CHANNEL_INACTIVE' }}</span>
      <span class="led" :class="props.secureChannel ? 'led-emerald' : 'led-crimson'"></span>
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
