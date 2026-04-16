<script setup lang="ts">
import { ArrowUpDown, ShieldCheck } from 'lucide-vue-next';
import { computed, ref } from 'vue';
import type { SectionResult } from '@/shared/types/section';
import type { MemberMatrix } from '../types/members';
import CoverageGapBanner from './CoverageGapBanner.vue';
import TeamSpaceCardShell from './TeamSpaceCardShell.vue';

interface Props {
  section: SectionResult<MemberMatrix>;
  isLoading?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  isLoading: false,
});

defineEmits<{
  retry: [];
  navigateAccess: [url: string];
}>();

const newestFirst = ref(true);

const sortedMembers = computed(() => {
  if (!props.section.data) {
    return [];
  }
  const members = [...props.section.data.members];
  members.sort((left, right) => {
    const leftValue = left.lastActiveAt ? new Date(left.lastActiveAt).getTime() : 0;
    const rightValue = right.lastActiveAt ? new Date(right.lastActiveAt).getTime() : 0;
    return newestFirst.value ? rightValue - leftValue : leftValue - rightValue;
  });
  return members;
});
</script>

<template>
  <TeamSpaceCardShell
    title="Member & Role Matrix"
    :loading="props.isLoading"
    :error="props.section.error"
    :empty="Boolean(props.section.data && props.section.data.members.length === 0)"
    empty-message="No members are currently assigned to this workspace."
    @retry="$emit('retry')"
  >
    <template #icon>
      <ShieldCheck :size="16" />
    </template>

    <template #actions>
      <button class="link-button" @click="newestFirst = !newestFirst">
        <ArrowUpDown :size="14" />
        Sort by activity
      </button>
    </template>

    <template v-if="props.section.data">
      <CoverageGapBanner :gaps="props.section.data.coverageGaps" />

      <div class="member-table">
        <div class="member-header text-label">
          <span>Member</span>
          <span>Roles</span>
          <span>Oncall</span>
          <span>Permissions</span>
          <span>Last Active</span>
        </div>
        <div v-for="member in sortedMembers" :key="member.memberId" class="member-row">
          <span>{{ member.displayName }}</span>
          <span>{{ member.roles.join(', ') }}</span>
          <span>{{ member.oncallStatus }}</span>
          <span>{{ member.keyPermissions.join(', ') }}</span>
          <span class="text-tech">{{ member.lastActiveAt ?? 'n/a' }}</span>
        </div>
      </div>

      <button
        class="link-button"
        :disabled="!props.section.data.accessManagementLink.enabled"
        @click="$emit('navigateAccess', props.section.data.accessManagementLink.url)"
      >
        Manage in Access Management
      </button>
    </template>
  </TeamSpaceCardShell>
</template>

<style scoped>
.member-table {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.member-header,
.member-row {
  display: grid;
  grid-template-columns: 1.1fr 1.4fr 0.7fr 1fr 1.1fr;
  gap: 12px;
  align-items: start;
}

.member-row {
  padding: 10px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  font-size: 0.78rem;
}

.link-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
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

@media (max-width: 1300px) {
  .member-header,
  .member-row {
    grid-template-columns: 1fr;
  }
}
</style>
