<script setup lang="ts">
defineProps<{
  open: boolean;
  title: string;
  width?: 'sm' | 'md' | 'lg';
}>();

const emit = defineEmits<{
  close: [];
}>();
</script>

<template>
  <Teleport to="body">
    <Transition name="panel">
      <div v-if="open" class="panel-overlay" @click.self="emit('close')">
        <aside
          class="panel"
          :class="[`panel--${width ?? 'md'}`]"
          role="dialog"
          :aria-label="title"
        >
          <header class="panel-header">
            <h2>{{ title }}</h2>
            <slot name="header-actions" />
            <button class="close-btn" aria-label="Close" @click="emit('close')">✕</button>
          </header>
          <div class="panel-body">
            <slot />
          </div>
        </aside>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.panel-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  justify-content: flex-end;
  z-index: 100;
}
.panel {
  background: var(--color-surface, #111);
  height: 100%;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}
.panel--sm { width: 480px; }
.panel--md { width: 600px; }
.panel--lg { width: 720px; }
.panel-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border, #2a2a2a);
}
.panel-header h2 { flex: 1; font-size: 16px; font-weight: 600; margin: 0; }
.close-btn {
  background: none;
  border: none;
  color: var(--color-on-surface-variant, #999);
  font-size: 16px;
  cursor: pointer;
  padding: 4px;
}
.panel-body { flex: 1; padding: 20px; overflow-y: auto; }
.panel-enter-active, .panel-leave-active { transition: transform 160ms ease-out; }
.panel-enter-from, .panel-leave-to { transform: translateX(100%); }
</style>
