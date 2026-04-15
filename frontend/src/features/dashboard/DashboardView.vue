<script setup lang="ts">
import { TrendingUp, Cpu } from 'lucide-vue-next';
import { DASHBOARD_SIGNALS } from './mockData';

const { valueStory, aiOperations } = DASHBOARD_SIGNALS;
</script>

<template>
  <div class="view-container">
    <div class="grid-layout">
      <section class="signal-card section-high">
        <header><TrendingUp :size="14" /> <span class="text-label">{{ valueStory.label }}</span></header>
        <div class="mock-stats">
          <div v-for="m in valueStory.metrics" :key="m.key" class="stat-item">
            <span class="text-label">{{ m.label }}</span>
            <span class="text-tech">{{ m.value }}</span>
          </div>
        </div>
      </section>

      <section class="signal-card section-high">
        <header><Cpu :size="14" /> <span class="text-label">{{ aiOperations.label }}</span></header>
        <div class="mock-bars">
          <div v-for="bar in aiOperations.bars" :key="bar.key" class="bar-row">
            <div class="bar-label text-tech">{{ bar.label }}</div>
            <div class="bar-track"><div class="bar-fill" :style="{ width: bar.percent + '%' }"></div></div>
          </div>
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
