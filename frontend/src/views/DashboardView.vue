<script setup lang="ts">
import { LayoutDashboard, TrendingUp, Cpu } from 'lucide-vue-next';
</script>

<template>
  <div class="view-container">
    <div class="grid-layout">
      <!-- High Level Signals -->
      <section class="signal-card section-high">
        <header><TrendingUp :size="14" /> <span class="text-label">VALUE_STORY</span></header>
        <div class="mock-stats">
          <div class="stat-item"><span class="text-label">MTTR</span> <span class="text-tech">12.4m</span></div>
          <div class="stat-item"><span class="text-label">AI_CONF</span> <span class="text-tech">98.2%</span></div>
        </div>
      </section>

      <section class="signal-card section-high">
        <header><Cpu :size="14" /> <span class="text-label">AI_OPERATIONS</span></header>
        <div class="mock-bars">
          <div class="bar-row"><div class="bar-label text-tech">FIX_GEN</div> <div class="bar-track"><div class="bar-fill" style="width: 80%"></div></div></div>
          <div class="bar-row"><div class="bar-label text-tech">ROOT_CAUSE</div> <div class="bar-track"><div class="bar-fill" style="width: 65%"></div></div></div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.view-container { padding: 0 24px 24px; }
.grid-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
.signal-card { padding: 16px; border-radius: var(--radius-sm); display: flex; flex-direction: column; gap: 16px; }
.signal-card header { display: flex; align-items: center; gap: 8px; color: var(--color-on-surface-variant); }
.mock-stats { display: flex; gap: 32px; }
.stat-item { display: flex; flex-direction: column; }
.stat-item .text-tech { font-size: 1.5rem; color: var(--color-secondary); }
.mock-bars { display: flex; flex-direction: column; gap: 12px; }
.bar-row { display: flex; align-items: center; gap: 12px; }
.bar-label { font-size: 0.625rem; width: 60px; }
.bar-track { flex: 1; height: 4px; background: var(--color-surface-container-highest); border-radius: 2px; }
.bar-fill { height: 100%; background: var(--color-secondary); border-radius: 2px; }
</style>
