<script setup lang="ts">
import type { RequirementCategory } from '../types/requirement';

interface Props {
  category: RequirementCategory;
}

defineProps<Props>();
</script>

<template>
  <span class="category-badge" :class="`cat--${category.toLowerCase()}`">
    {{ category }}
  </span>
</template>

<style scoped>
.category-badge {
  display: inline-flex;
  align-items: center;
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 2px;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.cat--functional {
  color: var(--color-secondary);
  background: var(--color-secondary-tint);
}

.cat--non-functional {
  color: var(--color-approval-amber);
  background: rgba(245, 158, 11, 0.1);
}

.cat--technical {
  color: #a78bfa;
  background: rgba(167, 139, 250, 0.1);
}

.cat--business {
  color: var(--color-health-emerald);
  background: rgba(78, 222, 163, 0.1);
}
</style>
