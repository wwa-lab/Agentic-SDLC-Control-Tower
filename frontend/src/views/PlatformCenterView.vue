<script setup lang="ts">
import { Settings, Shield, FileCheck, Users } from 'lucide-vue-next';
</script>

<template>
  <div class="view-container">
    <div class="platform-grid">
      <section class="admin-card section-high">
        <header><Shield :size="14" /> <span class="text-label">POLICY_ENGINE</span></header>
        <div class="policy-status">
          <div class="policy-row"><span class="text-body-sm">SOC2_COMPLIANCE</span> <span class="led led-emerald"></span></div>
          <div class="policy-row"><span class="text-body-sm">AWS_SECURITY_GROUPS</span> <span class="led led-emerald"></span></div>
          <div class="policy-row"><span class="text-body-sm">DEPLOY_GATED_PR</span> <span class="led led-emerald"></span></div>
        </div>
      </section>

      <section class="admin-card section-high">
        <header><FileCheck :size="14" /> <span class="text-label">AUDIT_LOGS</span></header>
        <div class="mock-audit section-lowest text-tech text-xs">
          <div>[08:12] USER_ADMIN updated SNOW Group FIN-TECH</div>
          <div>[09:05] AI_ENGINE updated policy DEPLOY_GATED</div>
          <div>[09:42] SYSTEM accessed Project Space v2.4</div>
        </div>
      </section>

      <section class="admin-card section-high">
        <header><Users :size="14" /> <span class="text-label">ACCESS_MANAGEMENT</span></header>
        <div class="access-controls">
          <button class="btn-machined">MANAGE_ROLES</button>
          <button class="btn-machined">INVITE_TEAM</button>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.view-container { padding: 0 24px 24px; }
.platform-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 24px; }
.admin-card { padding: 16px; border-radius: var(--radius-sm); display: flex; flex-direction: column; gap: 16px; }
.admin-card header { display: flex; align-items: center; gap: 8px; color: var(--color-on-surface-variant); }
.policy-row { display: flex; justify-content: space-between; align-items: center; padding: 4px 0; }
.mock-audit { padding: 12px; border-radius: 2px; opacity: 0.7; height: 100px; overflow: hidden; line-height: 1.8; }
.access-controls { display: flex; gap: 12px; }
.text-xs { font-size: 0.75rem; }
</style>
