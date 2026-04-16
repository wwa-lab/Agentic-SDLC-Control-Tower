<script setup lang="ts">
import type { SkillTimeline, SectionResult, SkillExecutionStatus } from '../types/incident';
import IncidentCard from './IncidentCard.vue';

interface Props {
  timeline: SectionResult<SkillTimeline>;
  isLoading?: boolean;
}

defineProps<Props>();

const STATUS_CLASS: Record<SkillExecutionStatus, string> = {
  running: 'status--running',
  completed: 'status--completed',
  failed: 'status--failed',
  pending_approval: 'status--pending',
};

function formatSkillName(name: string): string {
  return name.replace(/^incident-/, '').replace(/-/g, ' ');
}

function formatDuration(start: string, end: string | null): string {
  if (!end) return 'in progress';
  const ms = new Date(end).getTime() - new Date(start).getTime();
  const secs = Math.round(ms / 1000);
  return secs < 60 ? `${secs}s` : `${Math.floor(secs / 60)}m ${secs % 60}s`;
}
</script>

<template>
  <IncidentCard title="Skill Execution Timeline" :is-loading="isLoading" :error="timeline.error">
    <div v-if="timeline.data" class="timeline">
      <div
        v-for="(exec, i) in timeline.data.executions"
        :key="i"
        class="timeline-entry"
      >
        <div class="timeline-dot" :class="STATUS_CLASS[exec.status]"></div>
        <div class="timeline-content">
          <div class="entry-header">
            <span class="skill-name">{{ formatSkillName(exec.skillName) }}</span>
            <span class="skill-duration">{{ formatDuration(exec.startTime, exec.endTime) }}</span>
          </div>
          <div class="entry-status" :class="STATUS_CLASS[exec.status]">
            {{ exec.status.replace(/_/g, ' ') }}
          </div>
          <div class="entry-io">
            <div class="io-line"><span class="io-label">IN:</span> {{ exec.inputSummary }}</div>
            <div class="io-line"><span class="io-label">OUT:</span> {{ exec.outputSummary }}</div>
          </div>
        </div>
      </div>
    </div>
  </IncidentCard>
</template>

<style scoped>
.timeline { display: flex; flex-direction: column; gap: 0; }

.timeline-entry {
  display: flex;
  gap: 12px;
  padding: 10px 0;
  border-left: 2px solid var(--color-surface-container-highest);
  margin-left: 4px;
  padding-left: 16px;
  position: relative;
}

.timeline-dot {
  position: absolute;
  left: -5px;
  top: 14px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status--completed .timeline-dot,
.timeline-dot.status--completed { background: var(--color-health-emerald); }
.status--running .timeline-dot,
.timeline-dot.status--running { background: var(--color-secondary); animation: pulse-dot 2s ease-in-out infinite; }
.status--failed .timeline-dot,
.timeline-dot.status--failed { background: var(--color-incident-crimson); }
.status--pending .timeline-dot,
.timeline-dot.status--pending { background: var(--color-approval-amber); }

.timeline-content { flex: 1; display: flex; flex-direction: column; gap: 4px; }

.entry-header { display: flex; justify-content: space-between; align-items: center; }

.skill-name {
  font-family: var(--font-ui);
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--color-on-surface);
  text-transform: capitalize;
}

.skill-duration {
  font-family: var(--font-tech);
  font-size: 0.625rem;
  color: var(--color-on-surface-variant);
}

.entry-status {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.entry-status.status--completed { color: var(--color-health-emerald); }
.entry-status.status--running { color: var(--color-secondary); }
.entry-status.status--failed { color: var(--color-incident-crimson); }
.entry-status.status--pending { color: var(--color-approval-amber); }

.entry-io {
  font-family: var(--font-tech);
  font-size: 0.6875rem;
  color: var(--color-on-surface-variant);
  line-height: 1.4;
}

.io-label { font-weight: 600; color: var(--color-on-surface-variant); opacity: 0.6; }

@keyframes pulse-dot { 0%, 100% { opacity: 1; } 50% { opacity: 0.4; } }
</style>
