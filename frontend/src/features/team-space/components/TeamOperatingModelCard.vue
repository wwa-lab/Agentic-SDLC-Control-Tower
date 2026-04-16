<script setup lang="ts">
import { Cable, ExternalLink } from 'lucide-vue-next';
import LineageBadge from '@/shared/components/LineageBadge.vue';
import type { SectionResult } from '@/shared/types/section';
import type { TeamOperatingModel } from '../types/operatingModel';
import TeamSpaceCardShell from './TeamSpaceCardShell.vue';

interface Props {
  section: SectionResult<TeamOperatingModel>;
  isLoading?: boolean;
}

withDefaults(defineProps<Props>(), {
  isLoading: false,
});

defineEmits<{
  retry: [];
  navigatePlatform: [url: string];
}>();
</script>

<template>
  <TeamSpaceCardShell
    title="Team Operating Model"
    :loading="isLoading"
    :error="section.error"
    @retry="$emit('retry')"
  >
    <template #icon>
      <Cable :size="16" />
    </template>

    <template v-if="section.data">
      <div class="field-grid">
        <div class="field-row">
          <span class="text-label">Operating Mode</span>
          <strong>{{ section.data.operatingMode.value }}</strong>
          <LineageBadge :lineage="section.data.operatingMode.lineage" />
        </div>
        <div class="field-row">
          <span class="text-label">Approval Mode</span>
          <strong>{{ section.data.approvalMode.value }}</strong>
          <LineageBadge :lineage="section.data.approvalMode.lineage" />
        </div>
        <div class="field-row">
          <span class="text-label">AI Autonomy</span>
          <strong>{{ section.data.aiAutonomyLevel.value }}</strong>
          <LineageBadge :lineage="section.data.aiAutonomyLevel.lineage" />
        </div>
        <div class="field-row">
          <span class="text-label">Oncall Owner</span>
          <strong>{{ section.data.oncallOwner.value.displayName }}</strong>
          <LineageBadge :lineage="section.data.oncallOwner.lineage" />
        </div>
      </div>

      <div class="owners-block">
        <span class="text-label">Accountable Owners</span>
        <div class="owners-list">
          <div v-for="owner in section.data.accountableOwners" :key="owner.area" class="owner-row">
            <span>{{ owner.area }}</span>
            <strong>{{ owner.displayName }}</strong>
          </div>
        </div>
      </div>

      <button
        class="link-button"
        :disabled="!section.data.platformCenterLink.enabled"
        @click="$emit('navigatePlatform', section.data.platformCenterLink.url)"
      >
        <ExternalLink :size="14" />
        View in Platform Center
      </button>
    </template>
  </TeamSpaceCardShell>
</template>

<style scoped>
.field-grid,
.owners-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.field-row,
.owner-row {
  display: grid;
  grid-template-columns: 140px 1fr auto;
  gap: 12px;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.owners-list {
  display: flex;
  flex-direction: column;
}

.link-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  align-self: flex-start;
  padding: 0;
  border: none;
  background: transparent;
  color: var(--color-secondary);
  cursor: pointer;
  font-size: 0.8125rem;
}

.link-button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

@media (max-width: 1200px) {
  .field-row,
  .owner-row {
    grid-template-columns: 1fr;
  }
}
</style>
