<script setup lang="ts">
import { ShieldAlert } from 'lucide-vue-next';
import type { SectionResult } from '@/shared/types/section';
import type { RiskRegistry } from '../types/risks';
import TeamSpaceCardShell from '@/features/team-space/components/TeamSpaceCardShell.vue';
import RiskItem from './RiskItem.vue';

interface Props {
  section: SectionResult<RiskRegistry>;
  isLoading?: boolean;
}

withDefaults(defineProps<Props>(), {
  isLoading: false,
});

defineEmits<{
  retry: [];
  open: [url: string];
}>();

function formatTimestamp(value: string): string {
  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}
</script>

<template>
  <TeamSpaceCardShell
    title="Risk & Vulnerability Registry"
    :loading="isLoading"
    :error="section.error"
    :empty="Boolean(section.data && section.data.items.length === 0)"
    empty-message="All green. No active project-scoped risks are open."
    @retry="$emit('retry')"
  >
    <template #icon>
      <ShieldAlert :size="16" />
    </template>

    <template #actions>
      <span v-if="section.data" class="text-tech">Refreshed {{ formatTimestamp(section.data.lastRefreshed) }}</span>
    </template>

    <div v-if="section.data && section.data.items.length > 0" class="risk-list">
      <RiskItem
        v-for="risk in section.data.items"
        :key="risk.id"
        :risk="risk"
        @open="$emit('open', $event)"
      />
    </div>
  </TeamSpaceCardShell>
</template>

<style scoped>
.risk-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
</style>
