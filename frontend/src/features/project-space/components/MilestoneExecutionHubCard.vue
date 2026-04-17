<script setup lang="ts">
import { FlagTriangleRight } from 'lucide-vue-next';
import type { SectionResult } from '@/shared/types/section';
import type { MilestoneHub } from '../types/milestones';
import TeamSpaceCardShell from '@/features/team-space/components/TeamSpaceCardShell.vue';
import MilestoneRow from './MilestoneRow.vue';

interface Props {
  section: SectionResult<MilestoneHub>;
  isLoading?: boolean;
}

withDefaults(defineProps<Props>(), {
  isLoading: false,
});

defineEmits<{
  retry: [];
  openLink: [url: string];
}>();
</script>

<template>
  <TeamSpaceCardShell
    title="Milestone Execution Hub"
    :loading="isLoading"
    :error="section.error"
    :empty="Boolean(section.data && section.data.milestones.length === 0)"
    empty-message="No milestones defined yet. Track execution readiness in Project Management when it lands."
    @retry="$emit('retry')"
  >
    <template #icon>
      <FlagTriangleRight :size="16" />
    </template>

    <template #actions>
      <button
        class="link-action"
        :disabled="!section.data?.projectManagementLink.enabled"
        @click="$emit('openLink', section.data!.projectManagementLink.url)"
      >
        Manage Milestones
      </button>
    </template>

    <div v-if="section.data && section.data.milestones.length > 0" class="milestone-list">
      <MilestoneRow
        v-for="milestone in section.data.milestones"
        :key="milestone.id"
        :milestone="milestone"
      />
    </div>
  </TeamSpaceCardShell>
</template>

<style scoped>
.milestone-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.link-action {
  border: 1px solid rgba(137, 206, 255, 0.16);
  background: transparent;
  color: var(--color-secondary);
  border-radius: 999px;
  padding: 6px 10px;
  cursor: pointer;
  font-size: 0.75rem;
}

.link-action:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}
</style>
