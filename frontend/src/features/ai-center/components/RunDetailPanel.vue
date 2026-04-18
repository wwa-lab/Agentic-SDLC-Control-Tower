<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ExternalLink, Paperclip, ScrollText } from 'lucide-vue-next';
import DetailPanelFrame from './DetailPanelFrame.vue';
import { useAiCenterStore } from '../stores/aiCenterStore';
import { formatDateTime, formatDuration, prettyJson, statusColor, statusLabel } from '../utils';

const route = useRoute();
const router = useRouter();
const store = useAiCenterStore();
const showAuditRecord = ref(false);

const executionId = computed(() => String(route.params.executionId ?? ''));
const detailState = computed(() => store.runDetail);
const notFound = computed(() => detailState.value.error?.toLowerCase().includes('not found') ?? false);

function closePanel() {
  store.clearSelectedRun();
  void router.push('/ai-center');
}

watch(
  () => executionId.value,
  value => {
    if (value) {
      void store.selectRun(value);
    }
  },
  { immediate: true },
);
</script>

<template>
  <DetailPanelFrame title="Run Detail" width="720px" @close="closePanel">
    <div v-if="detailState.loading" class="panel-skeleton"></div>

    <div v-else-if="detailState.error" class="panel-message">
      <p class="text-label">{{ notFound ? 'Run not found' : 'Run detail unavailable' }}</p>
      <p class="text-body-sm">{{ detailState.error }}</p>
      <button class="btn-machined" type="button" @click="closePanel">Back to AI Center</button>
    </div>

    <template v-else-if="detailState.data">
      <section class="panel-section">
        <div class="panel-title-row">
          <div>
            <h3>{{ detailState.data.skillName }}</h3>
            <p class="text-tech">{{ detailState.data.id }}</p>
          </div>
          <span class="panel-chip" role="status" :style="{ '--chip-color': statusColor(detailState.data.status) }">
            {{ statusLabel(detailState.data.status) }}
          </span>
        </div>
        <div class="panel-grid">
          <div>
            <p class="text-label">Started</p>
            <strong>{{ formatDateTime(detailState.data.startedAt) }}</strong>
          </div>
          <div>
            <p class="text-label">Duration</p>
            <strong>{{ formatDuration(detailState.data.durationMs) }}</strong>
          </div>
          <div>
            <p class="text-label">Triggered By</p>
            <strong>{{ detailState.data.triggeredBy }}</strong>
          </div>
          <div>
            <p class="text-label">Autonomy</p>
            <strong>{{ detailState.data.autonomyLevel }}</strong>
          </div>
        </div>
      </section>

      <section class="panel-section">
        <p class="text-label">Trigger Source</p>
        <div class="panel-title-row">
          <div>
            <strong>{{ detailState.data.triggerSourcePage ?? 'Scheduled or manual' }}</strong>
            <p class="text-body-sm">{{ detailState.data.outcomeSummary }}</p>
          </div>
          <button v-if="detailState.data.triggerSourceUrl" class="btn-machined" type="button" @click="router.push(detailState.data.triggerSourceUrl)">
            Go to source
          </button>
        </div>
      </section>

      <section class="panel-grid">
        <div class="panel-section">
          <p class="text-label">Input Summary</p>
          <pre class="panel-pre">{{ prettyJson(detailState.data.inputSummary) }}</pre>
        </div>
        <div class="panel-section">
          <p class="text-label">Output Summary</p>
          <pre class="panel-pre">{{ prettyJson(detailState.data.outputSummary) }}</pre>
        </div>
      </section>

      <section class="panel-section">
        <p class="text-label">Step Breakdown</p>
        <div class="panel-list">
          <div v-for="step in detailState.data.stepBreakdown" :key="step.ordinal" class="panel-list__item panel-list__item--static">
            <div>
              <strong>{{ step.ordinal }}. {{ step.name }}</strong>
              <p class="text-body-sm">{{ formatDateTime(step.startedAt) }} → {{ step.endedAt ? formatDateTime(step.endedAt) : 'Running' }}</p>
            </div>
            <span class="panel-chip" role="status" :style="{ '--chip-color': statusColor(step.status) }">
              {{ statusLabel(step.status) }}
            </span>
          </div>
        </div>
      </section>

      <section class="panel-section">
        <p class="text-label">Policy Trail</p>
        <div class="panel-list">
          <div v-for="entry in detailState.data.policyTrail" :key="`${entry.rule}-${entry.at}`" class="panel-list__item panel-list__item--static">
            <div>
              <strong>{{ entry.rule }}</strong>
              <p class="text-body-sm">{{ entry.note ?? 'No additional note' }}</p>
            </div>
            <span class="panel-chip">{{ entry.decision }}</span>
          </div>
        </div>
      </section>

      <section class="panel-section">
        <div class="panel-section__heading">
          <Paperclip :size="16" />
          <p class="text-label">Evidence Links</p>
        </div>
        <div class="panel-list">
          <a
            v-for="link in detailState.data.evidenceLinks"
            :key="`${link.sourceSystem}-${link.url}`"
            class="panel-list__item panel-list__item--link"
            :href="link.url"
            target="_blank"
            rel="noreferrer"
          >
            <div>
              <strong>{{ link.title }}</strong>
              <p class="text-body-sm">{{ link.sourceSystem }} · {{ link.type }}</p>
            </div>
            <ExternalLink :size="14" />
          </a>
        </div>
      </section>

      <section class="panel-section">
        <div class="panel-section__heading">
          <ScrollText :size="16" />
          <p class="text-label">Audit Record</p>
        </div>
        <button class="btn-machined" type="button" @click="showAuditRecord = !showAuditRecord">
          {{ showAuditRecord ? 'Hide Audit ID' : 'Open Audit Record' }}
        </button>
        <pre v-if="showAuditRecord" class="panel-pre">{{ detailState.data.auditRecordId ?? 'No audit record' }}</pre>
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
  animation: run-detail-pulse 1.2s ease-in-out infinite;
}

.panel-grid,
.panel-title-row,
.panel-list,
.panel-list__item,
.panel-section__heading {
  display: flex;
  gap: 12px;
}

.panel-grid {
  flex-wrap: wrap;
}

.panel-grid > * {
  flex: 1 1 260px;
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
.panel-list__item {
  justify-content: space-between;
}

.panel-list {
  flex-direction: column;
}

.panel-list__item {
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  padding: 12px;
  background: rgba(255, 255, 255, 0.02);
}

.panel-list__item--static {
  cursor: default;
}

.panel-list__item--link {
  color: inherit;
  text-decoration: none;
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

.panel-pre {
  margin: 0;
  padding: 12px;
  border-radius: var(--radius-sm);
  background: rgba(6, 14, 32, 0.72);
  color: var(--color-on-surface);
  white-space: pre-wrap;
  word-break: break-word;
}

@keyframes run-detail-pulse {
  0% { background-position: 100% 0; }
  100% { background-position: -100% 0; }
}
</style>
