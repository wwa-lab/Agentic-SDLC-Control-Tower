<script setup lang="ts">
import { onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useWorkspaceStore } from '@/shared/stores/workspaceStore';
import { useShellUiStore } from '@/shell/stores/shellUiStore';
import { useSessionStore } from '@/shell/stores/sessionStore';
import { useShellConfig } from '@/shell/composables/useShellConfig';
import PrimaryNav from './PrimaryNav.vue';
import TopContextBar from './TopContextBar.vue';
import GlobalActionBar from './GlobalActionBar.vue';
import PageHeader from './PageHeader.vue';
import AiCommandPanel from './AiCommandPanel.vue';
import LoginView from './LoginView.vue';
import DataRibbon from '@/shared/components/DataRibbon.vue';

const workspaceStore = useWorkspaceStore();
const shellUiStore = useShellUiStore();
const sessionStore = useSessionStore();
const { config } = useShellConfig();
const router = useRouter();
const route = useRoute();

async function initAndLoad() {
  await workspaceStore.load();
  // After loading workspaces, redirect from root to first available workspace.
  if (route.path === '/' || route.path === '') {
    const key = workspaceStore.workspaces[0]?.workspaceKey ?? 'payment-gateway-pro';
    router.replace(`/${key}`);
  }
}

onMounted(async () => {
  await sessionStore.init();
  if (sessionStore.isAuthenticated) {
    initAndLoad();
  }
});

watch(() => sessionStore.isAuthenticated, (isAuthenticated) => {
  if (isAuthenticated) {
    initAndLoad();
  }
});
</script>

<template>
  <LoginView v-if="sessionStore.initialized && !sessionStore.isAuthenticated" />
  <div v-else-if="sessionStore.initialized" class="app-shell">
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
  <div v-else class="shell-loading">Loading SDLC Tower...</div>
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
  flex: 1 1 auto;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: var(--color-surface);
}

.top-bar {
  min-width: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: var(--border-ghost);
  flex-shrink: 0;
  overflow: hidden;
}

.content-scroll {
  flex: 1;
  min-width: 0;
  overflow-x: hidden;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  background-color: var(--color-surface);
}

.page-container {
  flex: 1;
  min-width: 0;
}

.shell-loading {
  width: 100vw;
  height: 100vh;
  display: grid;
  place-items: center;
  background-color: var(--color-surface);
  color: var(--color-on-surface-variant);
  font-family: var(--font-ui);
}
</style>
