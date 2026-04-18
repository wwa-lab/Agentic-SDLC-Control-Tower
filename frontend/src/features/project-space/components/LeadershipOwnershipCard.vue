<script setup lang="ts">
import { ShieldCheck } from 'lucide-vue-next';
import type { SectionResult } from '@/shared/types/section';
import type { LeadershipOwnership } from '../types/leadership';
import TeamSpaceCardShell from '@/features/team-space/components/TeamSpaceCardShell.vue';
import RoleRow from './RoleRow.vue';

interface Props {
  section: SectionResult<LeadershipOwnership>;
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
    title="Leadership & Ownership"
    :loading="isLoading"
    :error="section.error"
    @retry="$emit('retry')"
  >
    <template #icon>
      <ShieldCheck :size="16" />
    </template>

    <template #actions>
      <button
        class="link-action"
        :disabled="!section.data?.accessManagementLink.enabled"
        @click="$emit('openLink', section.data!.accessManagementLink.url)"
      >
        Manage Access
      </button>
    </template>

    <template v-if="section.data">
      <RoleRow
        v-for="assignment in section.data.assignments"
        :key="assignment.role"
        :assignment="assignment"
      />
    </template>
  </TeamSpaceCardShell>
</template>

<style scoped>
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
