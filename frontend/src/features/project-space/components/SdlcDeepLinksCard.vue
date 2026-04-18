<script setup lang="ts">
import { Network } from 'lucide-vue-next';
import type { SectionResult } from '@/shared/types/section';
import type { SdlcChainState } from '../types/chain';
import TeamSpaceCardShell from '@/features/team-space/components/TeamSpaceCardShell.vue';
import ChainNodeTile from './ChainNodeTile.vue';

interface Props {
  section: SectionResult<SdlcChainState>;
  isLoading?: boolean;
}

withDefaults(defineProps<Props>(), {
  isLoading: false,
});

defineEmits<{
  retry: [];
  navigate: [url: string];
}>();
</script>

<template>
  <TeamSpaceCardShell
    title="SDLC Deep Links"
    :loading="isLoading"
    :error="section.error"
    @retry="$emit('retry')"
  >
    <template #icon>
      <Network :size="16" />
    </template>

    <div v-if="section.data" class="chain-grid">
      <ChainNodeTile
        v-for="node in section.data.nodes"
        :key="node.nodeKey"
        :node="node"
        @navigate="$emit('navigate', $event)"
      />
    </div>
  </TeamSpaceCardShell>
</template>

<style scoped>
.chain-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;
}

@media (max-width: 1400px) {
  .chain-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 1000px) {
  .chain-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
