<script setup lang="ts">
import { Box, Layers, Globe } from 'lucide-vue-next';
</script>

<template>
  <div class="view-container">
    <div class="detail-stack">
      <section class="detail-row section-high">
        <header><Box :size="14" /> <span class="text-label">PROJECT_CORE</span></header>
        <div class="grid-3">
          <div class="data-block"><span class="text-label">VERSION</span> <span class="text-tech">v2.4.0-STABLE</span></div>
          <div class="data-block"><span class="text-label">STATUS</span> <span class="status-active"><span class="led led-emerald"></span> IN_FLIGHT</span></div>
          <div class="data-block"><span class="text-label">MILESTONE</span> <span class="text-tech">MS-7: CLOUD_HANDOFF</span></div>
        </div>
      </section>

      <div class="grid-2">
        <section class="detail-row section-high">
          <header><Globe :size="14" /> <span class="text-label">ENVIRONMENTS</span></header>
          <ul class="env-list">
            <li><span class="text-tech text-xs">PROD</span> <span class="led led-emerald"></span></li>
            <li><span class="text-tech text-xs">STAGE</span> <span class="led led-emerald"></span></li>
            <li><span class="text-tech text-xs">UAT</span> <span class="led led-amber"></span></li>
          </ul>
        </section>
        <section class="detail-row section-high">
          <header><Layers :size="14" /> <span class="text-label">RESOURCES</span></header>
          <div class="mock-resources">
            <div class="resource-pill text-tech">EKS_CLUSTER_01</div>
            <div class="resource-pill text-tech">RDS_POSTGRES_MASTER</div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<style scoped>
.view-container { padding: 0 24px 24px; }
.detail-stack { display: flex; flex-direction: column; gap: 24px; }
.detail-row { padding: 16px; border-radius: var(--radius-sm); display: flex; flex-direction: column; gap: 16px; }
.detail-row header { display: flex; align-items: center; gap: 8px; color: var(--color-on-surface-variant); }
.grid-3 { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
.data-block { display: flex; flex-direction: column; }
.status-active { display: flex; align-items: center; gap: 8px; color: var(--color-health-emerald); font-family: var(--font-tech); }
.env-list { list-style: none; display: flex; flex-direction: column; gap: 8px; }
.env-list li { display: flex; justify-content: space-between; align-items: center; width: 80px; }
.resource-pill { font-size: 0.625rem; padding: 4px 8px; background: var(--color-surface-container-highest); border-radius: 2px; }
.mock-resources { display: flex; flex-wrap: wrap; gap: 8px; }
.text-xs { font-size: 0.75rem; }
</style>
