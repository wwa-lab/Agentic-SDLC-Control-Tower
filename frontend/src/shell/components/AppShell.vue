<script setup lang="ts">
import { onMounted } from 'vue';
import { useWorkspaceStore } from '@/shared/stores/workspaceStore';
import { useShellUiStore } from '@/shell/stores/shellUiStore';
import { useShellConfig } from '@/shell/composables/useShellConfig';
import PrimaryNav from './PrimaryNav.vue';
import TopContextBar from './TopContextBar.vue';
import GlobalActionBar from './GlobalActionBar.vue';
import PageHeader from './PageHeader.vue';
import AiCommandPanel from './AiCommandPanel.vue';
import DataRibbon from '@/shared/components/DataRibbon.vue';

const workspaceStore = useWorkspaceStore();
const shellUiStore = useShellUiStore();
const { config } = useShellConfig();

onMounted(() => {
  workspaceStore.load();
});
</script>

<template>
  <div class="app-shell">
    <!-- Left Nav (overridable) -->
    <slot name="nav">
      <PrimaryNav />
    </slot>

    <!-- Main Stack -->
    <main class="main-stack">
      <!-- Top Bar: Context + Global Actions -->
      <div class="top-bar section-low glass-panel">
        <TopContextBar />
        <GlobalActionBar />
      </div>

      <!-- Operational Data Ribbon -->
      <DataRibbon />

      <!-- Content Area -->
      <div class="content-scroll animate-fade-in">
        <slot name="header">
          <PageHeader />
        </slot>
        <div class="page-container">
          <slot>
            <router-view />
          </slot>
        </div>
      </div>
    </main>

    <!-- Right Panel (overridable) -->
    <slot name="ai-panel">
      <AiCommandPanel v-if="config.showAiPanel" :content="shellUiStore.resolvedAiPanelContent" />
    </slot>
  </div>
</template>

<style scoped>
.app-shell {
  display: flex;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background-color: var(--color-surface);
}

.main-stack {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: var(--color-surface);
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: var(--border-ghost);
  flex-shrink: 0;
}

.content-scroll {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  background-color: var(--color-surface);
}

.page-container {
  flex: 1;
}
</style>
