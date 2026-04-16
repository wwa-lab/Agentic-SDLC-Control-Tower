<script setup lang="ts">
import type { SectionResult, AiAnalysis } from '../types/requirement';
import RequirementCard from './RequirementCard.vue';

interface Props {
  aiAnalysis: SectionResult<AiAnalysis>;
  isLoading?: boolean;
}

defineProps<Props>();
const emit = defineEmits<{ runAnalysis: [] }>();
</script>

<template>
  <RequirementCard
    title="AI Analysis"
    :is-loading="isLoading"
    :error="aiAnalysis.error"
  >
    <div v-if="aiAnalysis.data" class="analysis-content">
      <!-- Completeness Ring -->
      <div class="completeness-section">
        <div class="ring-container">
          <svg class="ring" viewBox="0 0 36 36">
            <path
              class="ring-bg"
              d="M18 2.5 a 15.5 15.5 0 0 1 0 31 a 15.5 15.5 0 0 1 0 -31"
            />
            <path
              class="ring-fill"
              :stroke-dasharray="`${aiAnalysis.data.completenessScore}, 100`"
              d="M18 2.5 a 15.5 15.5 0 0 1 0 31 a 15.5 15.5 0 0 1 0 -31"
            />
          </svg>
          <span class="ring-value">{{ aiAnalysis.data.completenessScore }}%</span>
        </div>
        <span class="ring-label">Completeness</span>
      </div>

      <!-- Missing Elements -->
      <div v-if="aiAnalysis.data.missingElements.length > 0" class="section">
        <h4 class="section-label">Missing Elements</h4>
        <ul class="item-list missing-list">
          <li v-for="(el, i) in aiAnalysis.data.missingElements" :key="i">{{ el }}</li>
        </ul>
      </div>

      <!-- Similar Requirements -->
      <div v-if="aiAnalysis.data.similarRequirements.length > 0" class="section">
        <h4 class="section-label">Similar Requirements</h4>
        <div class="similar-items">
          <span
            v-for="sim in aiAnalysis.data.similarRequirements"
            :key="sim.id"
            class="similar-item"
          >
            {{ sim.id }} ({{ sim.similarity }}%)
          </span>
        </div>
      </div>

      <!-- Impact Assessment -->
      <div class="section">
        <h4 class="section-label">Impact Assessment</h4>
        <p class="section-text">{{ aiAnalysis.data.impactAssessment }}</p>
      </div>

      <!-- Suggestions -->
      <div v-if="aiAnalysis.data.suggestions.length > 0" class="section">
        <h4 class="section-label">Suggestions</h4>
        <ul class="item-list suggestion-list">
          <li v-for="(s, i) in aiAnalysis.data.suggestions" :key="i">{{ s }}</li>
        </ul>
      </div>

      <button class="run-btn" @click="emit('runAnalysis')">Re-Run Analysis</button>
    </div>

    <!-- No analysis available -->
    <div v-else class="no-analysis">
      <span class="no-analysis-text">Run AI analysis to assess requirement quality</span>
      <button class="run-btn" @click="emit('runAnalysis')">Run Analysis</button>
    </div>
  </RequirementCard>
</template>

<style scoped>
.analysis-content {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.completeness-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ring-container {
  position: relative;
  width: 48px;
  height: 48px;
}

.ring {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.ring-bg, .ring-fill {
  fill: none;
  stroke-width: 3;
  stroke-linecap: round;
}

.ring-bg { stroke: var(--color-surface-container-low); }
.ring-fill { stroke: var(--color-secondary); transition: stroke-dasharray 0.6s ease; }

.ring-value {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-family: var(--font-tech);
  font-size: 0.625rem;
  color: var(--color-secondary);
  font-weight: 700;
}

.ring-label {
  font-family: var(--font-ui);
  font-size: 0.625rem;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.section {
  display: flex;
  flex-direction: column;
  gap: 4px;
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
  font-size: 0.6875rem;
  line-height: 1.4;
  color: var(--color-on-surface);
  margin: 0;
}

.item-list {
  padding-left: 14px;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.item-list li {
  font-family: var(--font-ui);
  font-size: 0.6875rem;
  color: var(--color-on-surface);
  line-height: 1.3;
}

.missing-list li { color: var(--color-approval-amber); }

.similar-items {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.similar-item {
  font-family: var(--font-tech);
  font-size: 0.5625rem;
  color: var(--color-secondary);
  background: var(--color-secondary-tint);
  padding: 2px 6px;
  border-radius: 2px;
}

.run-btn {
  background: none;
  border: 1px solid var(--color-secondary);
  color: var(--color-secondary);
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  cursor: pointer;
  transition: all 0.2s ease;
  align-self: flex-start;
}

.run-btn:hover {
  background: var(--color-secondary);
  color: var(--color-on-secondary-container);
}

.no-analysis {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px 0;
}

.no-analysis-text {
  font-family: var(--font-ui);
  font-size: 0.75rem;
  color: var(--color-on-surface-variant);
  opacity: 0.6;
}
</style>
