<script setup lang="ts">
import type { SectionResult } from '@/shared/types/section';
import type { AiRowStatus } from '../../types/enums';
import AiRowStatusBanner from '../primitives/AiRowStatusBanner.vue';

defineProps<{
  section: SectionResult<{ status: AiRowStatus; narrative?: string }>;
  canRegenerate: boolean;
}>();
const emit = defineEmits<{ regenerate: [] }>();
</script>

<template>
  <div class="deploy-ai-summary-card card">
    <div class="card-title">
      <span>AI Deploy Summary</span>
      <button
        v-if="canRegenerate"
        class="regen-btn"
        @click="emit('regenerate')"
      >Regenerate</button>
    </div>
    <div v-if="section.error" class="card-error">{{ section.error }}</div>
    <div v-else-if="!section.data" class="card-skeleton">Loading AI summary...</div>
    <template v-else>
      <AiRowStatusBanner :status="section.data.status" />
      <p v-if="section.data.status === 'SKIPPED'" class="skipped-notice">
        No meaningful changes &mdash; summary skipped.
      </p>
      <p v-else-if="section.data.narrative" class="narrative">{{ section.data.narrative }}</p>
    </template>
  </div>
</template>

<style scoped>
.deploy-ai-summary-card { padding: 16px; }
.card {
  background: var(--color-surface-container-low);
  border: var(--border-ghost);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
}
.card-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-family: var(--font-ui);
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--color-on-surface);
  text-transform: uppercase;
  letter-spacing: 0.03em;
}
.regen-btn {
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-secondary);
  background: transparent;
  color: var(--color-secondary);
  font-size: 0.7rem;
  font-weight: 600;
  cursor: pointer;
  text-transform: uppercase;
}
.regen-btn:hover { background: var(--color-secondary-tint); }
.card-error { color: var(--color-incident-crimson); font-size: 0.85rem; }
.card-skeleton { color: var(--color-on-surface-variant); font-size: 0.85rem; }
.narrative {
  margin: 8px 0 0;
  font-family: var(--font-ui);
  font-size: 0.85rem;
  line-height: 1.5;
  color: var(--color-on-surface);
}
.skipped-notice {
  margin: 8px 0 0;
  font-family: var(--font-ui);
  font-size: 0.85rem;
  color: var(--color-on-surface-variant);
  font-style: italic;
}
</style>
