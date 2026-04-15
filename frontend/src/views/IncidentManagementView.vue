<script setup lang="ts">
import { AlertTriangle, Activity, ShieldCheck } from 'lucide-vue-next';
</script>

<template>
  <div class="view-container">
    <div class="incident-grid">
      <!-- Active Incidents -->
      <section class="incident-card section-high">
        <header><AlertTriangle :size="14" class="text-crimson" /> <span class="text-label">CRITICAL_SIGNALS</span></header>
        <div class="incident-list">
          <div class="incident-item">
            <div class="item-head"><span class="text-tech">INC-0422</span> <span class="text-label">P1</span></div>
            <p class="text-body-sm">API Gateway Latency Spike (>500ms)</p>
            <div class="item-foot"><span class="led led-crimson"></span> <span class="text-label">AI_INVESTIGATING</span></div>
          </div>
        </div>
      </section>

      <!-- AI Diagnosis -->
      <section class="incident-card section-high">
        <header><Activity :size="14" /> <span class="text-label">AI_DIAGNOSIS_FEED</span></header>
        <div class="diag-feed section-lowest">
          <div class="feed-entry">
            <span class="text-tech">[09:41:02]</span> Analyzing k8s ingress logs...
          </div>
          <div class="feed-entry">
            <span class="text-tech">[09:41:05]</span> Pattern identified: SSL handshake timeout.
          </div>
          <div class="feed-entry status-suggest">
            <span class="text-tech">[09:41:10]</span> SUGGESTION: Scale pod replicas to 5.
          </div>
        </div>
      </section>

      <!-- Governance -->
      <section class="incident-card section-high">
        <header><ShieldCheck :size="14" /> <span class="text-label">HUMAN_GOVERNANCE</span></header>
        <div class="gov-actions">
          <p class="text-label">PENDING_APPROVAL</p>
          <div class="action-row">
            <span class="text-body-sm">Scale Replicas (v2.4.0)</span>
            <div class="btns">
              <button class="btn-machined btn-xs">REJECT</button>
              <button class="btn-machined btn-ai btn-xs">APPROVE</button>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.view-container { padding: 0 24px 24px; }
.incident-grid { display: grid; grid-template-columns: 1fr 1fr; grid-template-rows: auto auto; gap: 24px; }
.incident-card { padding: 16px; border-radius: var(--radius-sm); display: flex; flex-direction: column; gap: 16px; }
.incident-card:nth-child(1) { grid-row: span 2; }
.incident-card header { display: flex; align-items: center; gap: 8px; color: var(--color-on-surface-variant); }
.text-crimson { color: var(--color-incident-crimson); }
.incident-item { display: flex; flex-direction: column; gap: 8px; padding: 12px; background: var(--color-surface-container-highest); border-radius: var(--radius-sm); }
.item-head { display: flex; justify-content: space-between; align-items: center; }
.item-foot { display: flex; align-items: center; gap: 8px; }
.diag-feed { flex: 1; padding: 12px; border-radius: 2px; font-family: var(--font-tech); font-size: 0.75rem; color: var(--color-on-surface-variant); line-height: 1.6; }
.status-suggest { color: var(--color-secondary); }
.gov-actions { display: flex; flex-direction: column; gap: 12px; }
.action-row { display: flex; justify-content: space-between; align-items: center; padding: 8px; background: rgba(255, 255, 255, 0.03); border-radius: 2px; }
.btns { display: flex; gap: 8px; }
.btn-xs { font-size: 0.625rem; padding: 4px 8px; }
</style>
