<script setup lang="ts">
import { computed } from 'vue';
import type { RequirementListItem } from '../types/requirement';

interface Props {
  requirements: ReadonlyArray<RequirementListItem>;
}

const props = defineProps<Props>();
const emit = defineEmits<{ select: [id: string] }>();

/**
 * Map requirements to quadrants based on priority (proxy for impact)
 * and completeness inverted (proxy for effort — lower completeness = higher remaining effort).
 * Critical/High → high impact; Medium/Low → low impact
 * Completeness > 50% → low remaining effort; <= 50% → high remaining effort
 */
interface QuadrantItem {
  readonly req: RequirementListItem;
  readonly quadrant: 'quick-wins' | 'strategic' | 'fill-ins' | 'deprioritize';
}

const quadrantItems = computed<ReadonlyArray<QuadrantItem>>(() => {
  return props.requirements.map(req => {
    const highImpact = req.priority === 'Critical' || req.priority === 'High';
    const lowEffort = req.completeness > 50;

    let quadrant: QuadrantItem['quadrant'];
    if (highImpact && lowEffort) quadrant = 'quick-wins';
    else if (highImpact && !lowEffort) quadrant = 'strategic';
    else if (!highImpact && lowEffort) quadrant = 'fill-ins';
    else quadrant = 'deprioritize';

    return { req, quadrant };
  });
});

function getQuadrant(q: QuadrantItem['quadrant']) {
  return quadrantItems.value.filter(i => i.quadrant === q);
}

const QUADRANT_META = {
  'quick-wins': { label: 'Quick Wins', subtitle: 'High Impact · Low Effort', cssClass: 'q-quick' },
  'strategic': { label: 'Strategic', subtitle: 'High Impact · High Effort', cssClass: 'q-strategic' },
  'fill-ins': { label: 'Fill-ins', subtitle: 'Low Impact · Low Effort', cssClass: 'q-fill' },
  'deprioritize': { label: 'Deprioritize', subtitle: 'Low Impact · High Effort', cssClass: 'q-depri' },
} as const;

const PRIORITY_COLORS: Record<string, string> = {
  Critical: 'dot--critical',
  High: 'dot--high',
  Medium: 'dot--medium',
  Low: 'dot--low',
};
</script>

<template>
  <div class="priority-matrix">
    <!-- Y-axis label -->
    <div class="y-label">
      <span>Impact</span>
      <span class="axis-arrow">↑</span>
    </div>

    <!-- 2x2 Grid -->
    <div class="matrix-grid">
      <div
        v-for="q in (['quick-wins', 'strategic', 'fill-ins', 'deprioritize'] as const)"
        :key="q"
        class="quadrant"
        :class="QUADRANT_META[q].cssClass"
      >
        <div class="quadrant-header">
          <span class="quadrant-label">{{ QUADRANT_META[q].label }}</span>
          <span class="quadrant-subtitle">{{ QUADRANT_META[q].subtitle }}</span>
        </div>
        <div class="quadrant-dots">
          <div
            v-for="item in getQuadrant(q)"
            :key="item.req.id"
            class="dot"
            :class="PRIORITY_COLORS[item.req.priority]"
            :title="`${item.req.id}: ${item.req.title}`"
            @click="emit('select', item.req.id)"
          >
            <span class="dot-id">{{ item.req.id }}</span>
          </div>
          <div v-if="getQuadrant(q).length === 0" class="quadrant-empty">—</div>
        </div>
      </div>
    </div>

    <!-- X-axis label -->
    <div class="x-label">
      <span>Effort</span>
      <span class="axis-arrow">→</span>
    </div>
  </div>
</template>

<style scoped>
.priority-matrix {
  display: flex;
  flex-direction: column;
  gap: 8px;
  position: relative;
}

.y-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-on-surface-variant);
  align-self: flex-start;
}

.x-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-on-surface-variant);
  align-self: flex-end;
}

.axis-arrow { font-size: 0.75rem; }

.matrix-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  gap: 8px;
  min-height: 360px;
}

/* Quadrant order: top-left=quick-wins, top-right=strategic, bottom-left=fill-ins, bottom-right=deprioritize */
.q-quick { grid-area: 1 / 1; }
.q-strategic { grid-area: 1 / 2; }
.q-fill { grid-area: 2 / 1; }
.q-depri { grid-area: 2 / 2; }

.quadrant {
  background: var(--color-surface-container-high);
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.quadrant-header {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.quadrant-label {
  font-family: var(--font-ui);
  font-size: 0.625rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--color-on-surface);
}

.quadrant-subtitle {
  font-family: var(--font-ui);
  font-size: 0.5rem;
  color: var(--color-on-surface-variant);
  opacity: 0.6;
}

.quadrant-dots {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  flex: 1;
  align-content: flex-start;
}

.dot {
  display: flex;
  align-items: center;
  padding: 3px 8px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.dot:hover { transform: scale(1.05); box-shadow: var(--shadow-card-hover); }

.dot-id {
  font-family: var(--font-tech);
  font-size: 0.5rem;
  font-weight: 600;
}

.dot--critical { background: var(--color-incident-crimson); color: #fff; }
.dot--high { background: rgba(245, 158, 11, 0.2); color: var(--color-approval-amber); }
.dot--medium { background: var(--color-secondary-tint); color: var(--color-secondary); }
.dot--low { background: rgba(148, 163, 184, 0.15); color: var(--color-on-surface-variant); }

.quadrant-empty {
  font-family: var(--font-ui);
  font-size: 0.625rem;
  color: var(--color-on-surface-variant);
  opacity: 0.3;
}

/* Quadrant accent borders */
.q-quick { border-left: 2px solid var(--color-health-emerald); }
.q-strategic { border-left: 2px solid var(--color-secondary); }
.q-fill { border-left: 2px solid var(--color-on-surface-variant); opacity: 0.9; }
.q-depri { border-left: 2px solid var(--color-on-surface-variant); opacity: 0.7; }
</style>
