<script setup lang="ts">
import { useRoute } from 'vue-router';

const route = useRoute();
</script>

<template>
  <div class="placeholder-page">
    <div class="page-info">
      <h2 class="text-tech">{{ route.meta.navKey }}</h2>
      <p v-if="route.meta.comingSoon" class="text-label status-tag">Coming Soon</p>
      <p v-else class="text-label">Phase A Implementation Target</p>
    </div>
    
    <div class="mock-content">
      <div class="mock-card section-high">
        <p class="text-tech">DATA_FEED_ID: {{ route.name }}-MOCK-001</p>
        <div class="mock-line"></div>
        <div class="mock-line w-3/4"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.placeholder-page {
  padding: 24px;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.status-tag {
  color: var(--color-approval-amber);
}

.mock-card {
  padding: 16px;
  border-radius: var(--radius-sm);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mock-line {
  height: 8px;
  background: var(--color-surface-container-highest);
  opacity: 0.3;
  border-radius: 4px;
}

.w-3\/4 { width: 75%; }
</style>
