<script setup lang="ts">
import { GitBranchPlus } from 'lucide-vue-next';
import type { SectionResult } from '@/shared/types/section';
import type { RequirementPipeline } from '../types/pipeline';
import TeamSpaceCardShell from './TeamSpaceCardShell.vue';
import PipelineCounters from './PipelineCounters.vue';
import PipelineBlockerList from './PipelineBlockerList.vue';
import SdlcChainStrip from './SdlcChainStrip.vue';

interface Props {
  section: SectionResult<RequirementPipeline>;
  isLoading?: boolean;
}

withDefaults(defineProps<Props>(), {
  isLoading: false,
});

defineEmits<{
  retry: [];
  navigateFilter: [filter: string];
  navigateLink: [url: string];
}>();
</script>

<template>
  <TeamSpaceCardShell
    title="Requirement & Spec Pipeline"
    :loading="isLoading"
    :error="section.error"
    @retry="$emit('retry')"
  >
    <template #icon>
      <GitBranchPlus :size="16" />
    </template>

    <template v-if="section.data">
      <PipelineCounters :counters="section.data.counters" @navigate-filter="$emit('navigateFilter', $event)" />
      <PipelineBlockerList :blockers="section.data.blockers" @navigate="$emit('navigateLink', $event)" />
      <div class="pipeline-card__chain">
        <span class="text-label">Chain Health</span>
        <SdlcChainStrip :nodes="section.data.chain" compact />
      </div>
    </template>
  </TeamSpaceCardShell>
</template>

<style scoped>
.pipeline-card__chain {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
</style>
