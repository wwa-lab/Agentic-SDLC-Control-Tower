<script setup lang="ts">
import type { PipelineBlocker } from '../types/pipeline';

interface Props {
  blockers: ReadonlyArray<PipelineBlocker>;
}

defineProps<Props>();

defineEmits<{
  navigate: [url: string];
}>();
</script>

<template>
  <div class="blocker-list">
    <span class="text-label">Active Blockers</span>
    <div v-if="blockers.length" class="blocker-list__items">
      <button
        v-for="blocker in blockers"
        :key="`${blocker.kind}-${blocker.targetId}`"
        class="blocker-item"
        @click="$emit('navigate', blocker.filterDeeplink)"
      >
        <div>
          <strong>{{ blocker.targetId }}</strong>
          <p class="text-body-sm">{{ blocker.targetTitle }}</p>
        </div>
        <span class="text-tech">{{ blocker.ageDays }}d</span>
      </button>
    </div>
    <p v-else class="text-body-sm blocker-list__empty">No blockers are currently over threshold.</p>
  </div>
</template>

<style scoped>
.blocker-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.blocker-list__items {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.blocker-item {
  border: 1px solid rgba(244, 166, 102, 0.18);
  background: rgba(244, 166, 102, 0.06);
  border-radius: var(--radius-sm);
  padding: 12px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  color: inherit;
  cursor: pointer;
}

.blocker-list__empty {
  color: var(--color-on-surface-variant);
}
</style>
