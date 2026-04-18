<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import { X } from 'lucide-vue-next';

interface Props {
  title: string;
  width?: '640px' | '720px';
}

const props = withDefaults(defineProps<Props>(), {
  width: '640px',
});

const emit = defineEmits<{
  close: [];
}>();

const panelRef = ref<HTMLElement | null>(null);
let previousFocus: HTMLElement | null = null;

function getFocusableElements(): HTMLElement[] {
  if (!panelRef.value) {
    return [];
  }
  return Array.from(panelRef.value.querySelectorAll<HTMLElement>(
    'button:not([disabled]), [href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])',
  ));
}

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    event.preventDefault();
    emit('close');
    return;
  }

  if (event.key !== 'Tab') {
    return;
  }

  const focusable = getFocusableElements();
  if (!focusable.length) {
    return;
  }

  const first = focusable[0];
  const last = focusable.at(-1)!;
  const active = document.activeElement;

  if (event.shiftKey && active === first) {
    event.preventDefault();
    last.focus();
  } else if (!event.shiftKey && active === last) {
    event.preventDefault();
    first.focus();
  }
}

onMounted(async () => {
  previousFocus = document.activeElement instanceof HTMLElement ? document.activeElement : null;
  await nextTick();
  const focusable = getFocusableElements();
  (focusable[0] ?? panelRef.value)?.focus();
});

onBeforeUnmount(() => {
  previousFocus?.focus?.();
});
</script>

<template>
  <Teleport to="body">
    <div class="detail-overlay" @click.self="emit('close')">
      <aside
        ref="panelRef"
        class="detail-panel section-high"
        :style="{ width: props.width }"
        role="dialog"
        aria-modal="true"
        :aria-label="title"
        tabindex="-1"
        @keydown="handleKeydown"
      >
        <header class="detail-panel__header">
          <div>
            <p class="text-label">AI Center Detail</p>
            <h2>{{ title }}</h2>
          </div>
          <button class="detail-panel__close" type="button" aria-label="Close detail panel" @click="emit('close')">
            <X :size="18" />
          </button>
        </header>

        <div class="detail-panel__body">
          <slot />
        </div>
      </aside>
    </div>
  </Teleport>
</template>

<style scoped>
.detail-overlay {
  position: fixed;
  inset: 0;
  z-index: 1100;
  display: flex;
  justify-content: flex-end;
  background: rgba(6, 14, 32, 0.52);
  backdrop-filter: blur(8px);
}

.detail-panel {
  height: 100vh;
  max-width: calc(100vw - 64px);
  border-left: var(--border-ghost);
  background: linear-gradient(180deg, rgba(34, 42, 61, 0.96), rgba(19, 27, 46, 0.98));
  box-shadow: var(--shadow-ambient);
  display: flex;
  flex-direction: column;
  animation: detail-slide-in 0.18s cubic-bezier(.2, .8, .2, 1);
}

.detail-panel__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 20px 16px;
  border-bottom: var(--border-ghost);
}

.detail-panel__body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.detail-panel__close {
  border: var(--border-ghost);
  background: rgba(255, 255, 255, 0.04);
  color: var(--color-on-surface);
  width: 36px;
  height: 36px;
  border-radius: 999px;
  cursor: pointer;
}

.detail-panel__close:focus-visible,
.detail-panel:focus-visible {
  outline: 2px solid var(--color-secondary);
  outline-offset: 2px;
}

@keyframes detail-slide-in {
  from {
    transform: translateX(20px);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@media (max-width: 960px) {
  .detail-panel {
    max-width: 100vw;
    width: 100vw !important;
  }
}
</style>
