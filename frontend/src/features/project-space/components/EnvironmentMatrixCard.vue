<script setup lang="ts">
import { Server } from 'lucide-vue-next';
import type { SectionResult } from '@/shared/types/section';
import type { EnvironmentMatrix } from '../types/environments';
import TeamSpaceCardShell from '@/features/team-space/components/TeamSpaceCardShell.vue';
import EnvironmentTile from './EnvironmentTile.vue';

interface Props {
  section: SectionResult<EnvironmentMatrix>;
  isLoading?: boolean;
}

withDefaults(defineProps<Props>(), {
  isLoading: false,
});

defineEmits<{
  retry: [];
  open: [url: string];
}>();
</script>

<template>
  <TeamSpaceCardShell
    title="Environment & Version Matrix"
    :loading="isLoading"
    :error="section.error"
    :empty="Boolean(section.data && section.data.environments.length === 0)"
    empty-message="No environments are registered for this project."
    @retry="$emit('retry')"
  >
    <template #icon>
      <Server :size="16" />
    </template>

    <div v-if="section.data && section.data.environments.length > 0" class="environment-grid">
      <EnvironmentTile
        v-for="environment in section.data.environments"
        :key="environment.id"
        :environment="environment"
        @open="$emit('open', $event)"
      />
    </div>
  </TeamSpaceCardShell>
</template>

<style scoped>
.environment-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

@media (max-width: 1200px) {
  .environment-grid {
    grid-template-columns: 1fr;
  }
}
</style>
