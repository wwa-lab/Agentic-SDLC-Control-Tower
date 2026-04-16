<script setup lang="ts">
import { Sparkles, Terminal, Info, Database } from 'lucide-vue-next';
import type { AiPanelContent } from '@/shared/types/shell';

interface Props {
  content?: AiPanelContent;
}

const props = withDefaults(defineProps<Props>(), {
  content: () => ({
    summary: 'Observing shell deployment. Workspace context is stable. AI is ready for command injection.',
    reasoning: [
      { text: 'Verified "No-Line" Rule compliance.', status: 'ok' as const },
      { text: 'Tonal hierarchy active.', status: 'ok' as const },
    ],
    evidence: JSON.stringify({ status: 'ready', components: 5, routes: 13, style: 'tactical_cmd' }, null, 2),
  }),
});

const emit = defineEmits<{
  command: [value: string];
}>();

const STATUS_LED: Record<string, string> = {
  ok: 'led-emerald',
  warning: 'led-amber',
  error: 'led-crimson',
};

function onCommand(event: Event) {
  const input = event.target as HTMLInputElement;
  if (input.value.trim()) {
    emit('command', input.value.trim());
    input.value = '';
  }
}
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
          <p class="text-body-sm">{{ props.content.summary }}</p>
        </div>
      </section>

      <!-- Reasoning Zone -->
      <section class="content-zone">
        <header><Database :size="12" /> <span>REASONING</span></header>
        <div class="zone-body section-high">
          <ul class="reasoning-list">
            <li v-for="(item, i) in props.content.reasoning" :key="i">
              <span class="led" :class="STATUS_LED[item.status]"></span>
              {{ item.text }}
            </li>
          </ul>
        </div>
      </section>

      <!-- Evidence Zone -->
      <section class="content-zone flex-1">
        <header><Terminal :size="12" /> <span>EVIDENCE</span></header>
        <div class="zone-body section-high evidence-box">
          <pre class="text-tech text-xs">{{ props.content.evidence }}</pre>
        </div>
      </section>
    </div>

    <div class="panel-input section-highest">
      <div class="input-wrapper">
        <span class="terminal-prompt text-tech">&gt;</span>
        <input
          type="text"
          placeholder="Type AI Command (/ for skills)..."
          class="command-input text-tech"
          @keydown.enter="onCommand"
        />
        <div class="cursor"></div>
      </div>
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
  background: rgba(19, 27, 46, 0.4);
  backdrop-filter: blur(20px);
}

.panel-header {
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-bottom: var(--border-ghost);
  background: rgba(19, 27, 46, 0.6);
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
  animation: fade-in-up 0.5s cubic-bezier(0.16, 1, 0.3, 1) forwards;
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
  padding: 1px;
  border-radius: var(--radius-sm);
  position: relative;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background: var(--color-surface-container-highest);
  border-radius: var(--radius-sm);
  padding: 0 12px;
}

.terminal-prompt {
  color: var(--color-secondary);
  font-weight: 800;
  margin-right: 8px;
}

.command-input {
  flex: 1;
  background: transparent;
  border: none;
  padding: 12px 0;
  color: var(--color-on-surface);
  font-size: 0.8125rem;
  outline: none;
}

.cursor {
  width: 8px;
  height: 14px;
  background: var(--color-secondary);
  animation: cursor-blink 1s infinite;
  margin-left: 4px;
}

.input-glow {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  border-radius: var(--radius-sm);
  box-shadow: 0 0 10px rgba(137, 206, 255, 0.1);
  pointer-events: none;
}
</style>
