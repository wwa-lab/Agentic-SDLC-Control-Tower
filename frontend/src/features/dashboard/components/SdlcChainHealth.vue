<script setup lang="ts">
import type { SdlcStageHealth } from '../types/dashboard';
import SdlcStageNode from './SdlcStageNode.vue';

interface Props {
  stages: ReadonlyArray<SdlcStageHealth>;
}

defineProps<Props>();
const emit = defineEmits(['navigate']);
</script>

<template>
  <div class="sdlc-chain-container">
    <div class="chain-wrapper">
      <template v-for="(stage, index) in stages" :key="stage.key">
        <SdlcStageNode
          :stage="stage"
          @click="emit('navigate', stage.key)"
        />
        <div v-if="index < stages.length - 1" class="connector">
          <svg width="28" height="12" viewBox="0 0 28 12" fill="none">
            <line x1="0" y1="6" x2="22" y2="6" stroke="var(--color-secondary)" stroke-opacity="0.3" stroke-width="1" stroke-dasharray="3 2"/>
            <path d="M22 6L17 1M22 6L17 11" stroke="var(--color-secondary)" stroke-opacity="0.4" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.sdlc-chain-container {
  width: 100%;
  overflow-x: auto;
  padding: 16px 12px;
  scrollbar-width: thin;
  scrollbar-color: var(--color-surface-container-highest) transparent;
  background: var(--color-surface-container-high);
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  box-shadow:
    var(--shadow-card),
    inset 0 1px 0 var(--color-secondary-tint);
}

.sdlc-chain-container::-webkit-scrollbar {
  height: 4px;
}

.sdlc-chain-container::-webkit-scrollbar-track {
  background: transparent;
}

.sdlc-chain-container::-webkit-scrollbar-thumb {
  background: var(--color-surface-container-highest);
  border-radius: 2px;
}

.chain-wrapper {
  display: flex;
  align-items: center;
  min-width: max-content;
  padding: 0 4px;
}

.connector {
  display: flex;
  align-items: center;
  padding: 0 4px;
  flex-shrink: 0;
}
</style>
