<script setup lang="ts">
import { computed } from 'vue';
import type { SourceReference } from '../types/requirement';
import RequirementCard from './RequirementCard.vue';
import SourceReferenceCard from './SourceReferenceCard.vue';

const props = defineProps<{
  sources: ReadonlyArray<SourceReference>;
  isLoading?: boolean;
  error?: string | null;
}>();

defineEmits<{ refresh: [sourceId: string]; retry: [] }>();

const freshCount = computed(() => props.sources.filter(source => source.freshnessStatus === 'FRESH').length);
const attentionCount = computed(() => props.sources.length - freshCount.value);
</script>

<template>
  <RequirementCard title="Source Evidence" :is-loading="isLoading" :error="error">
    <div class="panel-list">
      <div v-if="error" class="section-error">
        <span>{{ error }}</span>
        <button type="button" @click="$emit('retry')">Retry</button>
      </div>
      <div v-else-if="sources.length === 0" class="empty">No source evidence linked</div>
      <div v-else class="evidence-strip">
        <span>{{ sources.length }} linked sources</span>
        <span>{{ freshCount }} fresh</span>
        <span v-if="attentionCount > 0" class="strip-warn">{{ attentionCount }} need attention</span>
      </div>
      <SourceReferenceCard
        v-for="source in sources"
        :key="source.id"
        :source="source"
        @refresh="$emit('refresh', $event)"
      />
    </div>
  </RequirementCard>
</template>

<style scoped>
.panel-list { display: flex; flex-direction: column; gap: 8px; }
.empty { padding: 16px 0; text-align: center; color: var(--color-on-surface-variant); font-size: 0.75rem; }

.evidence-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.evidence-strip span {
  padding: 4px 7px;
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
  border: var(--border-ghost);
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
  font-size: 0.6875rem;
  font-weight: 600;
}

.evidence-strip .strip-warn {
  color: var(--color-approval-amber);
  background: rgba(245, 158, 11, 0.1);
  border-color: rgba(245, 158, 11, 0.22);
}

.section-error {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  color: var(--color-incident-crimson);
  font-size: 0.75rem;
}
.section-error button {
  border: 1px solid var(--color-incident-crimson);
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-incident-crimson);
  cursor: pointer;
}
</style>
