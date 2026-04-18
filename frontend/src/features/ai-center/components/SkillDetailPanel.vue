<script setup lang="ts">
import { computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ExternalLink, Shield, Activity } from 'lucide-vue-next';
import DetailPanelFrame from './DetailPanelFrame.vue';
import { useAiCenterStore } from '../stores/aiCenterStore';
import { formatDateTime, formatDuration, formatPercent, statusColor, statusLabel } from '../utils';

const route = useRoute();
const router = useRouter();
const store = useAiCenterStore();

const skillKey = computed(() => String(route.params.skillKey ?? ''));
const detailState = computed(() => store.skillDetail);
const notFound = computed(() => detailState.value.error?.toLowerCase().includes('not found') ?? false);

function closePanel() {
  store.clearSelectedSkill();
  void router.push('/ai-center');
}

watch(
  () => skillKey.value,
  value => {
    if (value) {
      void store.selectSkill(value);
    }
  },
  { immediate: true },
);
</script>

<template>
  <DetailPanelFrame title="Skill Detail" width="640px" @close="closePanel">
    <div v-if="detailState.loading" class="panel-skeleton"></div>

    <div v-else-if="detailState.error" class="panel-message">
      <p class="text-label">{{ notFound ? 'Skill not found' : 'Skill detail unavailable' }}</p>
      <p class="text-body-sm">{{ detailState.error }}</p>
      <button class="btn-machined" type="button" @click="closePanel">Back to AI Center</button>
    </div>

    <template v-else-if="detailState.data">
      <section class="panel-section">
        <p class="text-label">Identity</p>
        <div class="panel-title-row">
          <div>
            <h3>{{ detailState.data.name }}</h3>
            <p class="text-tech">{{ detailState.data.key }}</p>
          </div>
          <div class="panel-chip-group">
            <span class="panel-chip" role="status" :style="{ '--chip-color': statusColor(detailState.data.status) }">
              {{ statusLabel(detailState.data.status) }}
            </span>
            <span class="panel-chip" role="status">{{ detailState.data.defaultAutonomy }}</span>
          </div>
        </div>
        <p class="text-body-sm">{{ detailState.data.description }}</p>
      </section>

      <section class="panel-section panel-grid">
        <div>
          <p class="text-label">Input Contract</p>
          <p>{{ detailState.data.inputContract }}</p>
        </div>
        <div>
          <p class="text-label">Output Contract</p>
          <p>{{ detailState.data.outputContract }}</p>
        </div>
      </section>

      <section class="panel-section">
        <div class="panel-section__heading">
          <Shield :size="16" />
          <p class="text-label">Current Policy</p>
        </div>
        <div class="panel-grid">
          <div>
            <p class="text-label">Autonomy</p>
            <strong>{{ detailState.data.policy.autonomyLevel }}</strong>
          </div>
          <div>
            <p class="text-label">Changed</p>
            <strong>{{ formatDateTime(detailState.data.policy.lastChangedAt) }}</strong>
          </div>
        </div>
        <p class="text-label">Approval Required Actions</p>
        <div class="panel-chip-group">
          <span v-for="action in detailState.data.policy.approvalRequiredActions" :key="action" class="panel-chip">{{ action }}</span>
        </div>
        <p class="text-label">Authorized Roles</p>
        <div class="panel-chip-group">
          <span v-for="role in detailState.data.policy.authorizedApproverRoles" :key="role" class="panel-chip">{{ role }}</span>
        </div>
      </section>

      <section class="panel-section">
        <div class="panel-section__heading">
          <Activity :size="16" />
          <p class="text-label">Aggregate Metrics</p>
        </div>
        <div class="panel-grid">
          <div>
            <p class="text-label">Success Rate</p>
            <strong>{{ formatPercent(detailState.data.aggregateMetrics.successRate) }}</strong>
          </div>
          <div>
            <p class="text-label">Avg Duration</p>
            <strong>{{ formatDuration(detailState.data.aggregateMetrics.avgDurationMs) }}</strong>
          </div>
          <div>
            <p class="text-label">Trend</p>
            <strong>{{ detailState.data.aggregateMetrics.adoptionTrend }}</strong>
          </div>
          <div>
            <p class="text-label">Runs / 30d</p>
            <strong>{{ detailState.data.aggregateMetrics.totalRuns30d }}</strong>
          </div>
        </div>
      </section>

      <section class="panel-section">
        <p class="text-label">Recent Runs</p>
        <div class="panel-list">
          <button
            v-for="run in detailState.data.recentRuns"
            :key="run.id"
            class="panel-list__item"
            type="button"
            @click="router.push(`/ai-center/runs/${run.id}`)"
          >
            <div>
              <strong>{{ run.outcomeSummary }}</strong>
              <p class="text-body-sm">{{ formatDateTime(run.startedAt) }} · {{ formatDuration(run.durationMs) }}</p>
            </div>
            <div class="panel-list__meta">
              <span class="panel-chip" role="status" :style="{ '--chip-color': statusColor(run.status) }">{{ statusLabel(run.status) }}</span>
              <ExternalLink :size="14" />
            </div>
          </button>
        </div>
      </section>
    </template>
  </DetailPanelFrame>
</template>

<style scoped>
.panel-skeleton,
.panel-message {
  min-height: 200px;
  border-radius: var(--radius-md);
  border: var(--border-ghost);
}

.panel-skeleton {
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.03), rgba(137, 206, 255, 0.08), rgba(255, 255, 255, 0.03));
  background-size: 220% 100%;
  animation: skill-detail-pulse 1.2s ease-in-out infinite;
}

.panel-message,
.panel-section {
  padding: 16px;
  border-radius: var(--radius-md);
  border: var(--border-ghost);
  background: rgba(255, 255, 255, 0.03);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.panel-title-row,
.panel-grid,
.panel-section__heading,
.panel-chip-group,
.panel-list__item,
.panel-list__meta {
  display: flex;
  gap: 12px;
}

.panel-title-row,
.panel-list__item {
  justify-content: space-between;
}

.panel-grid {
  flex-wrap: wrap;
}

.panel-grid > div {
  min-width: 180px;
  flex: 1;
}

.panel-chip-group {
  flex-wrap: wrap;
}

.panel-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 9px;
  border-radius: 999px;
  border: 1px solid color-mix(in srgb, var(--chip-color, var(--color-secondary)) 40%, transparent);
  background: color-mix(in srgb, var(--chip-color, var(--color-secondary)) 10%, transparent);
  color: var(--chip-color, var(--color-on-surface));
  width: fit-content;
}

.panel-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.panel-list__item {
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  padding: 12px;
  background: rgba(255, 255, 255, 0.02);
  color: var(--color-on-surface);
  text-align: left;
  cursor: pointer;
}

@keyframes skill-detail-pulse {
  0% { background-position: 100% 0; }
  100% { background-position: -100% 0; }
}
</style>
