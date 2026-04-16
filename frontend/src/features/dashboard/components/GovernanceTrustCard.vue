<script setup lang="ts">
import type { GovernanceMetrics, SectionResult } from '../types/dashboard';
import DashboardCard from './DashboardCard.vue';
import MetricCard from './MetricCard.vue';

interface Props {
  section: SectionResult<GovernanceMetrics>;
  isLoading?: boolean;
}

defineProps<Props>();
const emit = defineEmits(['navigate-governance']);
</script>

<template>
  <DashboardCard title="Governance Trust" :is-loading="isLoading" :error="section.error">
    <div v-if="section.data" class="governance-content" @click="emit('navigate-governance')">
      <div class="metrics-grid">
        <MetricCard v-bind="section.data.templateReuse" />
        <MetricCard v-bind="section.data.configDrift" />
        <MetricCard v-bind="section.data.auditCoverage" />
        <MetricCard v-bind="section.data.policyHitRate" />
      </div>
      <div class="governance-footer">
        <span class="footer-link">View Platform Center →</span>
      </div>
    </div>
  </DashboardCard>
</template>

<style scoped>
.governance-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  justify-content: space-between;
  cursor: pointer;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.governance-footer {
  margin-top: 12px;
  text-align: right;
}

.footer-link {
  font-family: var(--font-ui);
  font-size: 0.625rem;
  color: var(--color-secondary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.footer-link:hover {
  text-decoration: underline;
}
</style>
