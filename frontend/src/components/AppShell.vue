<script setup lang="ts">
import PrimaryNav from './PrimaryNav.vue';
import TopContextBar from './TopContextBar.vue';
import PageHeader from './PageHeader.vue';
import AiCommandPanel from './AiCommandPanel.vue';
</script>

<template>
  <div class="app-shell">
    <!-- Left Nav -->
    <PrimaryNav />

    <!-- Main Stack -->
    <main class="main-stack">
      <!-- Top Bar -->
      <TopContextBar />

      <!-- Content Area -->
      <div class="content-scroll">
        <PageHeader />
        <div class="page-container">
          <router-view />
        </div>
      </div>
    </main>

    <!-- Right Panel -->
    <AiCommandPanel />
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

.content-scroll {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.page-container {
  flex: 1;
}

/* Tonal shift for the main content area background vs sidebar */
.content-scroll {
  background-color: var(--color-surface);
}
</style>
