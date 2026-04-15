<script setup lang="ts">
import { Sparkles, Terminal, Info, Database } from 'lucide-vue-next';
</script>

<template>
  <aside class="ai-command-panel section-low">
    <div class="panel-header">
      <Sparkles :size="16" class="text-secondary" />
      <span class="text-label">AI COMMAND PANEL</span>
    </div>

    <div class="panel-content">
      <!-- Summary Zone -->
      <section class="content-zone">
        <header><Info :size="12" /> <span>SUMMARY</span></header>
        <div class="zone-body section-high">
          <p class="text-body-sm">Observing Phase A shell deployment. Workspace context is stable. AI is ready for command injection.</p>
        </div>
      </section>

      <!-- Reasoning Zone -->
      <section class="content-zone">
        <header><Database :size="12" /> <span>REASONING</span></header>
        <div class="zone-body section-high">
          <ul class="reasoning-list">
            <li><span class="led led-emerald"></span> Verified "No-Line" Rule compliance.</li>
            <li><span class="led led-emerald"></span> Tonal hierarchy active.</li>
          </ul>
        </div>
      </section>

      <!-- Evidence Zone -->
      <section class="content-zone flex-1">
        <header><Terminal :size="12" /> <span>EVIDENCE</span></header>
        <div class="zone-body section-high evidence-box">
          <pre class="text-tech text-xs">
{
  "status": "ready",
  "components": 5,
  "routes": 13,
  "style": "tactical_cmd"
}
          </pre>
        </div>
      </section>
    </div>

    <div class="panel-input section-highest">
      <input type="text" placeholder="Type AI Command (/ for skills)..." class="command-input text-tech" />
      <div class="input-glow"></div>
    </div>
  </aside>
</template>

<style scoped>
.ai-command-panel {
  width: 320px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  border-left: var(--border-ghost);
}

.panel-header {
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-bottom: var(--border-ghost);
}

.text-secondary { color: var(--color-secondary); }

.panel-content {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
}

.content-zone {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.content-zone header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.625rem;
  font-weight: 600;
  color: var(--color-on-surface-variant);
  letter-spacing: 0.05em;
}

.zone-body {
  padding: 12px;
  border-radius: var(--radius-sm);
  font-size: 0.8125rem;
  line-height: 1.4;
}

.flex-1 { flex: 1; }

.reasoning-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.reasoning-list li {
  display: flex;
  align-items: center;
  gap: 8px;
}

.evidence-box {
  background-color: var(--color-surface-lowest);
  overflow: auto;
}

.text-xs { font-size: 0.75rem; }

.panel-input {
  margin: 16px;
  padding: 1px; /* For glow effect */
  border-radius: var(--radius-sm);
  position: relative;
}

.command-input {
  width: 100%;
  background: var(--color-surface-container-highest);
  border: none;
  border-radius: var(--radius-sm);
  padding: 12px;
  color: var(--color-on-surface);
  font-size: 0.8125rem;
  outline: none;
}

.input-glow {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  border-radius: var(--radius-sm);
  box-shadow: 0 0 10px rgba(137, 206, 255, 0.1);
  pointer-events: none;
}
</style>
