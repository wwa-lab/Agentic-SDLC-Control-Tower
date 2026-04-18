<script setup lang="ts">
import type { ApplicationAiInsight } from '../../types/application';
import type { SectionResult } from '@/shared/types/section';
import AiRowStatusBanner from '../primitives/AiRowStatusBanner.vue';

defineProps<{
  section: SectionResult<ApplicationAiInsight>;
  canRegenerate: boolean;
}>();
const emit = defineEmits<{ regenerate: [] }>();
</script>

<template>
  <div class="ai-card card">
    <div class="card-title">
      <span>AI Insights</span>
      <button
        v-if="canRegenerate"
        class="regen-btn"
        @click="emit('regenerate')"
      >Regenerate</button>
    </div>
    <div v-if="section.error" class="card-error">{{ section.error }}</div>
    <div v-else-if="!section.data" class="card-skeleton">Loading AI insights...</div>
    <template v-else>
      <AiRowStatusBanner
        :status="section.data.status"
        :error-message="section.data.error?.message"
      />
      <p v-if="section.data.narrative" class="narrative">{{ section.data.narrative }}</p>
      <div v-if="section.data.evidence?.length" class="evidence-list">
        <span class="evidence-label">Evidence</span>
        <span
          v-for="item in section.data.evidence"
          :key="`${item.kind}-${item.id}`"
          class="evidence-link"
        >
          <span class="evidence-kind">{{ item.kind }}</span>
          <span class="evidence-id">{{ item.label }}</span>
        </span>
      </div>
      <p v-if="section.data.generatedAt" class="timestamp">
        Generated {{ new Date(section.data.generatedAt).toLocaleString() }}
        <span v-if="section.data.skillVersion" class="skill-ver">(v{{ section.data.skillVersion }})</span>
      </p>
    </template>
  </div>
</template>

<style scoped>
.ai-card { padding: 16px; }
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
  margin: 8px 0;
  font-family: var(--font-ui);
  font-size: 0.85rem;
  line-height: 1.5;
  color: var(--color-on-surface);
}
.evidence-list {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin: 8px 0;
}
.evidence-label {
  font-family: var(--font-ui);
  font-size: 0.65rem;
  font-weight: 700;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.evidence-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  border: var(--border-ghost);
  background: var(--color-surface-container-high);
}
.evidence-kind {
  font-family: var(--font-ui);
  font-size: 0.6rem;
  font-weight: 700;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
}
.evidence-id {
  font-family: var(--font-tech);
  font-size: 0.75rem;
  color: var(--color-on-surface);
}
.timestamp {
  margin: 8px 0 0;
  font-family: var(--font-ui);
  font-size: 0.7rem;
  color: var(--color-on-surface-variant);
}
.skill-ver {
  font-family: var(--font-tech);
  font-size: 0.65rem;
  opacity: 0.7;
}
</style>
