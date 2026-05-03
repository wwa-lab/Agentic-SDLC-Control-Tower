<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useWorkspaceStore } from '@/shared/stores/workspaceStore';
import { ChevronDown } from 'lucide-vue-next';

const workspaceStore = useWorkspaceStore();
const router = useRouter();
const route = useRoute();
const open = ref(false);
const container = ref<HTMLElement | null>(null);

function toggle() {
  if (workspaceStore.workspaces.length > 1) {
    open.value = !open.value;
  }
}

function handleDocClick(e: MouseEvent) {
  if (open.value && container.value && !container.value.contains(e.target as Node)) {
    open.value = false;
  }
}

onMounted(() => document.addEventListener('click', handleDocClick));
onBeforeUnmount(() => document.removeEventListener('click', handleDocClick));

function switchTo(key: string) {
  open.value = false;
  const currentWsKey = route.params.workspaceKey as string | undefined;
  if (currentWsKey === key) return;

  const currentPath = route.path;
  const newPath = currentWsKey
    ? currentPath.replace(`/${currentWsKey}`, `/${key}`)
    : `/${key}`;
  router.push(newPath);
}
</script>

<template>
  <div ref="container" class="workspace-switcher">
    <button
      class="switcher-btn"
      :class="{ 'has-options': workspaceStore.workspaces.length > 1 }"
      @click="toggle"
      :title="workspaceStore.context.workspace"
    >
      <span class="ws-name">{{ workspaceStore.context.workspace || 'No workspace' }}</span>
      <ChevronDown
        v-if="workspaceStore.workspaces.length > 1"
        :size="12"
        class="chevron"
        :class="{ open }"
      />
    </button>

    <div v-if="open" class="switcher-dropdown">
      <button
        v-for="ws in workspaceStore.workspaces"
        :key="ws.workspaceId"
        class="ws-option"
        :class="{ active: ws.workspaceKey === workspaceStore.activeWorkspaceKey }"
        @click="switchTo(ws.workspaceKey)"
      >
        <span class="ws-option-name">{{ ws.name }}</span>
        <span class="ws-option-key">{{ ws.workspaceKey }}</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.workspace-switcher {
  position: relative;
  flex-shrink: 0;
}

.switcher-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: transparent;
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  padding: 4px 8px;
  cursor: default;
  color: var(--color-on-surface);
  transition: all 0.15s;
  white-space: nowrap;
}

.switcher-btn.has-options {
  cursor: pointer;
}

.switcher-btn.has-options:hover {
  background: var(--nav-hover-bg);
  border-color: var(--color-border);
}

.ws-name {
  font-size: 0.75rem;
  font-weight: 600;
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chevron {
  transition: transform 0.15s;
  opacity: 0.7;
  flex-shrink: 0;
}

.chevron.open {
  transform: rotate(180deg);
}

.switcher-dropdown {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  min-width: 200px;
  background: var(--color-surface-container);
  border: var(--border-ghost);
  border-radius: var(--radius-md);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
  z-index: 100;
  overflow: hidden;
}

.ws-option {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  width: 100%;
  padding: 8px 12px;
  background: transparent;
  border: none;
  cursor: pointer;
  text-align: left;
  transition: background 0.1s;
  gap: 2px;
}

.ws-option:hover {
  background: var(--nav-hover-bg);
}

.ws-option.active {
  background: var(--nav-active-bg);
}

.ws-option-name {
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--color-on-surface);
}

.ws-option-key {
  font-size: 0.6875rem;
  color: var(--color-on-surface-variant);
  font-family: var(--font-mono);
}
</style>
