<script setup lang="ts">
import { Siren } from 'lucide-vue-next';
import type { SectionResult } from '@/shared/types/section';
import type { TeamRiskRadar } from '../types/risks';
import type { RiskCategory } from '../types/enums';
import TeamSpaceCardShell from './TeamSpaceCardShell.vue';
import RiskItem from './RiskItem.vue';

interface Props {
  section: SectionResult<TeamRiskRadar>;
  isLoading?: boolean;
}

withDefaults(defineProps<Props>(), {
  isLoading: false,
});

defineEmits<{
  retry: [];
  navigate: [url: string];
}>();

const categoryOrder: ReadonlyArray<RiskCategory> = ['INCIDENT', 'APPROVAL', 'CONFIG_DRIFT', 'DEPENDENCY', 'PROJECT'];
</script>

<template>
  <TeamSpaceCardShell
    title="Risk Radar"
    :loading="isLoading"
    :error="section.error"
    :empty="Boolean(section.data && section.data.total === 0)"
    empty-message="All green. No unresolved risk signals are active for this workspace."
    @retry="$emit('retry')"
  >
    <template #icon>
      <Siren :size="16" />
    </template>

    <template v-if="section.data && section.data.total > 0">
      <div class="risk-groups">
        <div v-for="category in categoryOrder" :key="category" v-show="section.data.groups[category].length" class="risk-group">
          <span class="text-label">{{ category }}</span>
          <div class="risk-group__items">
            <RiskItem
              v-for="item in section.data.groups[category]"
              :key="item.id"
              :item="item"
              @navigate="$emit('navigate', $event)"
            />
          </div>
        </div>
      </div>
      <div class="risk-footer text-tech">Last refreshed {{ section.data.lastRefreshed }}</div>
    </template>
  </TeamSpaceCardShell>
</template>

<style scoped>
.risk-groups {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.risk-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.risk-group__items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.risk-footer {
  color: var(--color-on-surface-variant);
  font-size: 0.75rem;
}
</style>
