<script setup lang="ts">
import { CalendarClock, ExternalLink, ShieldAlert } from 'lucide-vue-next';
import type { SectionResult } from '@/shared/types/section';
import type { ProjectSummary } from '../types/summary';
import HealthFactorPopover from './HealthFactorPopover.vue';

interface Props {
  section: SectionResult<ProjectSummary>;
  isLoading?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  isLoading: false,
});

defineEmits<{
  retry: [];
  openLink: [url: string];
}>();

function formatDate(value: string | null | undefined): string {
  if (!value) {
    return 'TBD';
  }
  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
  }).format(new Date(value));
}

function formatTimestamp(value: string): string {
  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

function initials(name: string): string {
  return name
    .split(/\s+/)
    .map(token => token[0] ?? '')
    .join('')
    .slice(0, 2)
    .toUpperCase();
}

function healthClass(health: ProjectSummary['healthAggregate']): string {
  if (health === 'RED') {
    return 'summary-bar__health--red';
  }
  if (health === 'YELLOW') {
    return 'summary-bar__health--yellow';
  }
  if (health === 'GREEN') {
    return 'summary-bar__health--green';
  }
  return 'summary-bar__health--unknown';
}
</script>

<template>
  <section class="summary-bar section-high">
    <div v-if="isLoading" class="summary-bar__loading">
      <div class="skeleton-line w-40"></div>
      <div class="skeleton-line w-85"></div>
      <div class="skeleton-grid">
        <div class="skeleton-line w-65"></div>
        <div class="skeleton-line w-65"></div>
        <div class="skeleton-line w-65"></div>
      </div>
    </div>

    <div v-else-if="section.error" class="summary-bar__error">
      <div>
        <p class="text-label">Project Summary Unavailable</p>
        <p class="text-body-sm">{{ section.error }}</p>
      </div>
      <button class="btn-machined" @click="$emit('retry')">Retry</button>
    </div>

    <template v-else-if="section.data">
      <div class="summary-bar__topline">
        <div class="summary-bar__identity">
          <button class="workspace-link text-label" @click="$emit('openLink', section.data.teamSpaceLink)">
            {{ section.data.workspaceName }}
            <ExternalLink :size="12" />
          </button>
          <div>
            <h2>{{ section.data.name }}</h2>
            <p class="summary-bar__subline text-tech">{{ section.data.id }} / {{ section.data.applicationName }}</p>
          </div>
        </div>

        <div class="summary-bar__health">
          <span class="summary-bar__stage text-label">{{ section.data.lifecycleStage }}</span>
          <div class="summary-bar__health-indicator" :class="healthClass(section.data.healthAggregate)">
            <div class="summary-bar__health-copy">
              <span class="text-label">Aggregate Health</span>
              <strong>{{ section.data.healthAggregate }}</strong>
            </div>
            <div class="summary-bar__popover-anchor">
              <ShieldAlert :size="16" />
              <HealthFactorPopover class="summary-bar__popover" :factors="section.data.healthFactors" />
            </div>
          </div>
        </div>
      </div>

      <div class="summary-bar__meta-grid">
        <div class="summary-bar__owners">
          <div class="owner-chip">
            <span class="owner-chip__avatar">{{ initials(section.data.pm.displayName) }}</span>
            <div>
              <p class="text-label">Project Manager</p>
              <strong>{{ section.data.pm.displayName }}</strong>
            </div>
          </div>
          <div class="owner-chip">
            <span class="owner-chip__avatar">{{ initials(section.data.techLead.displayName) }}</span>
            <div>
              <p class="text-label">Tech Lead</p>
              <strong>{{ section.data.techLead.displayName }}</strong>
            </div>
          </div>
        </div>

        <div class="summary-bar__milestone">
          <div class="summary-bar__milestone-header">
            <CalendarClock :size="14" />
            <span class="text-label">Active Milestone</span>
          </div>
          <strong>{{ section.data.activeMilestone?.label ?? 'No active milestone' }}</strong>
          <span class="text-tech">
            {{ section.data.activeMilestone ? formatDate(section.data.activeMilestone.targetDate) : 'TBD' }}
          </span>
        </div>

        <div class="summary-bar__timestamp">
          <p class="text-label">Last Updated</p>
          <strong class="text-tech">{{ formatTimestamp(section.data.lastUpdatedAt) }}</strong>
        </div>
      </div>

      <div class="summary-bar__counters">
        <div class="counter-block">
          <span class="text-label">Active Specs</span>
          <strong>{{ section.data.counters.activeSpecs }}</strong>
        </div>
        <div class="counter-block">
          <span class="text-label">Open Incidents</span>
          <strong>{{ section.data.counters.openIncidents }}</strong>
        </div>
        <div class="counter-block">
          <span class="text-label">Pending Approvals</span>
          <strong>{{ section.data.counters.pendingApprovals }}</strong>
        </div>
        <div class="counter-block">
          <span class="text-label">High Risks</span>
          <strong>{{ section.data.counters.criticalHighRisks }}</strong>
        </div>
      </div>
    </template>
  </section>
</template>

<style scoped>
.summary-bar {
  border-radius: var(--radius-sm);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 18px;
  border: var(--border-ghost);
  box-shadow: var(--shadow-card);
}

.summary-bar__topline,
.summary-bar__meta-grid,
.summary-bar__counters {
  display: grid;
  gap: 16px;
}

.summary-bar__topline {
  grid-template-columns: minmax(0, 1.4fr) minmax(280px, 0.8fr);
  align-items: start;
}

.summary-bar__identity,
.summary-bar__health,
.summary-bar__owners,
.summary-bar__milestone,
.summary-bar__timestamp,
.counter-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.workspace-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  width: fit-content;
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--color-secondary);
}

.workspace-link:hover {
  opacity: 0.9;
}

.summary-bar__subline {
  opacity: 0.8;
  margin-top: 6px;
}

.summary-bar__stage {
  align-self: flex-end;
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid rgba(137, 206, 255, 0.22);
  background: rgba(137, 206, 255, 0.08);
}

.summary-bar__health {
  align-items: flex-end;
}

.summary-bar__health-indicator {
  display: flex;
  gap: 16px;
  align-items: center;
  padding: 14px 16px;
  border-radius: var(--radius-sm);
  min-width: 100%;
  justify-content: space-between;
}

.summary-bar__health--green {
  background: rgba(78, 222, 163, 0.1);
  border: 1px solid rgba(78, 222, 163, 0.24);
}

.summary-bar__health--yellow {
  background: rgba(245, 158, 11, 0.12);
  border: 1px solid rgba(245, 158, 11, 0.24);
}

.summary-bar__health--red {
  background: rgba(255, 180, 171, 0.12);
  border: 1px solid rgba(255, 180, 171, 0.24);
}

.summary-bar__health--unknown {
  background: rgba(148, 163, 184, 0.1);
  border: 1px solid rgba(148, 163, 184, 0.2);
}

.summary-bar__health-copy strong {
  font-family: var(--font-tech);
}

.summary-bar__popover-anchor {
  position: relative;
  display: flex;
  align-items: center;
  cursor: help;
}

.summary-bar__popover {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  opacity: 0;
  visibility: hidden;
  transform: translateY(4px);
  transition: all 0.2s ease;
  z-index: 5;
}

.summary-bar__popover-anchor:hover .summary-bar__popover,
.summary-bar__popover-anchor:focus-within .summary-bar__popover {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.summary-bar__meta-grid {
  grid-template-columns: 1.2fr 0.9fr 0.7fr;
  align-items: stretch;
}

.summary-bar__owners {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.owner-chip {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 12px;
  border-radius: var(--radius-sm);
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.04);
}

.owner-chip__avatar {
  width: 36px;
  height: 36px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--color-secondary-tint);
  color: var(--color-secondary);
  font-family: var(--font-tech);
}

.summary-bar__milestone,
.summary-bar__timestamp {
  padding: 14px;
  border-radius: var(--radius-sm);
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.04);
}

.summary-bar__milestone-header {
  display: flex;
  gap: 8px;
  align-items: center;
}

.summary-bar__counters {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.counter-block {
  padding: 14px;
  border-radius: var(--radius-sm);
  background: rgba(255, 255, 255, 0.03);
}

.counter-block strong {
  font-size: 1.5rem;
  font-family: var(--font-tech);
}

.summary-bar__loading,
.summary-bar__error {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.summary-bar__error {
  align-items: flex-start;
}

.skeleton-line {
  height: 10px;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(137, 206, 255, 0.05), rgba(137, 206, 255, 0.18), rgba(137, 206, 255, 0.05));
  background-size: 200% 100%;
  animation: shimmer 1.4s linear infinite;
}

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.w-40 { width: 40%; }
.w-85 { width: 85%; }
.w-65 { width: 65%; }

@keyframes shimmer {
  from { background-position: 200% 0; }
  to { background-position: -200% 0; }
}

@media (max-width: 1200px) {
  .summary-bar__topline,
  .summary-bar__meta-grid,
  .summary-bar__counters,
  .summary-bar__owners,
  .skeleton-grid {
    grid-template-columns: 1fr;
  }

  .summary-bar__health {
    align-items: stretch;
  }

  .summary-bar__stage {
    align-self: flex-start;
  }
}
</style>
