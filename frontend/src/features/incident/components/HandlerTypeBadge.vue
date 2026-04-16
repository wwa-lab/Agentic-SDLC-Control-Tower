<script setup lang="ts">
import type { HandlerType } from '../types/incident';

interface Props {
  handlerType: HandlerType;
}

defineProps<Props>();
</script>

<template>
  <span class="handler-badge" :class="`handler--${handlerType.toLowerCase()}`">
    {{ handlerType }}
  </span>
</template>

<style scoped>
.handler-badge {
  display: inline-flex;
  align-items: center;
  font-family: var(--font-ui);
  font-size: 0.625rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  padding: 2px 8px;
  border-radius: 2px;
}

.handler--ai {
  color: var(--color-secondary);
  background: var(--color-secondary-tint);
}

.handler--human {
  color: var(--color-on-surface-variant);
  background: rgba(148, 163, 184, 0.1);
}

.handler--hybrid {
  color: var(--color-secondary);
  background: linear-gradient(135deg, var(--color-secondary-tint), rgba(148, 163, 184, 0.1));
}
</style>
