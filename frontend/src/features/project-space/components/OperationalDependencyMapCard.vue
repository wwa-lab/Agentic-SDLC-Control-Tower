<script setup lang="ts">
import { Link2 } from 'lucide-vue-next';
import type { SectionResult } from '@/shared/types/section';
import type { DependencyMap } from '../types/dependencies';
import TeamSpaceCardShell from '@/features/team-space/components/TeamSpaceCardShell.vue';
import DependencyRow from './DependencyRow.vue';

interface Props {
  section: SectionResult<DependencyMap>;
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
    title="Operational Dependency Map"
    :loading="isLoading"
    :error="section.error"
    :empty="Boolean(section.data && section.data.upstream.length === 0 && section.data.downstream.length === 0)"
    empty-message="No dependencies registered for this project."
    @retry="$emit('retry')"
  >
    <template #icon>
      <Link2 :size="16" />
    </template>

    <div v-if="section.data" class="dependency-columns">
      <div class="dependency-column">
        <p class="text-label">Upstream</p>
        <DependencyRow
          v-for="dependency in section.data.upstream"
          :key="dependency.id"
          :dependency="dependency"
          @open="$emit('open', $event)"
        />
      </div>
      <div class="dependency-column">
        <p class="text-label">Downstream</p>
        <DependencyRow
          v-for="dependency in section.data.downstream"
          :key="dependency.id"
          :dependency="dependency"
          @open="$emit('open', $event)"
        />
      </div>
    </div>
  </TeamSpaceCardShell>
</template>

<style scoped>
.dependency-columns {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.dependency-column {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

@media (max-width: 1200px) {
  .dependency-columns {
    grid-template-columns: 1fr;
  }
}
</style>
