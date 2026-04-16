<script setup lang="ts">
import type { SectionResult, RequirementDescription } from '../types/requirement';
import RequirementCard from './RequirementCard.vue';

interface Props {
  description: SectionResult<RequirementDescription>;
  isLoading?: boolean;
}

defineProps<Props>();
</script>

<template>
  <RequirementCard
    title="Description"
    :is-loading="isLoading"
    :error="description.error"
  >
    <div v-if="description.data" class="desc-content">
      <!-- Summary -->
      <div class="desc-section">
        <h4 class="section-label">Summary</h4>
        <p class="section-text">{{ description.data.summary }}</p>
      </div>

      <!-- Business Justification -->
      <div class="desc-section">
        <h4 class="section-label">Business Justification</h4>
        <p class="section-text">{{ description.data.businessJustification }}</p>
      </div>

      <!-- Acceptance Criteria -->
      <div class="desc-section">
        <h4 class="section-label">Acceptance Criteria</h4>
        <ul class="criteria-list">
          <li
            v-for="ac in description.data.acceptanceCriteria"
            :key="ac.id"
            class="criterion"
          >
            <span class="criterion-check" :class="{ 'criterion--met': ac.isMet }">
              {{ ac.isMet ? '✓' : '○' }}
            </span>
            <span class="criterion-text">{{ ac.text }}</span>
            <span class="criterion-id">{{ ac.id }}</span>
          </li>
        </ul>
      </div>

      <!-- Assumptions -->
      <div v-if="description.data.assumptions.length > 0" class="desc-section">
        <h4 class="section-label">Assumptions</h4>
        <ul class="bullet-list">
          <li v-for="(a, i) in description.data.assumptions" :key="i">{{ a }}</li>
        </ul>
      </div>

      <!-- Constraints -->
      <div v-if="description.data.constraints.length > 0" class="desc-section">
        <h4 class="section-label">Constraints</h4>
        <ul class="bullet-list">
          <li v-for="(c, i) in description.data.constraints" :key="i">{{ c }}</li>
        </ul>
      </div>
    </div>
  </RequirementCard>
</template>

<style scoped>
.desc-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.desc-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.section-label {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-on-surface-variant);
  margin: 0;
}

.section-text {
  font-family: var(--font-ui);
  font-size: 0.75rem;
  line-height: 1.5;
  color: var(--color-on-surface);
  margin: 0;
}

.criteria-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.criterion {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-family: var(--font-ui);
  font-size: 0.75rem;
  color: var(--color-on-surface);
}

.criterion-check {
  flex-shrink: 0;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.625rem;
  border-radius: 2px;
  border: 1px solid var(--color-on-surface-variant);
  color: var(--color-on-surface-variant);
}

.criterion--met {
  color: var(--color-health-emerald);
  border-color: var(--color-health-emerald);
  background: rgba(78, 222, 163, 0.1);
}

.criterion-text {
  flex: 1;
  line-height: 1.4;
}

.criterion-id {
  font-family: var(--font-tech);
  font-size: 0.5625rem;
  color: var(--color-on-surface-variant);
  opacity: 0.6;
}

.bullet-list {
  padding-left: 16px;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.bullet-list li {
  font-family: var(--font-ui);
  font-size: 0.75rem;
  line-height: 1.4;
  color: var(--color-on-surface);
}
</style>
