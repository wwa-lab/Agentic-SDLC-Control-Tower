<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useShellConfig } from '@/shell/composables/useShellConfig';
import { useWorkspaceStore } from '@/shared/stores/workspaceStore';
import { NAVIGATION_ITEMS, ICON_MAP } from '@/router';
import type { SystemStatus } from '@/shared/types/shell';

interface Props {
  systemStatus?: SystemStatus;
}

const props = withDefaults(defineProps<Props>(), {
  systemStatus: 'ready',
});

const STATUS_LABELS: Record<SystemStatus, string> = {
  ready: 'SYSTEM_READY',
  degraded: 'SYSTEM_DEGRADED',
  offline: 'SYSTEM_OFFLINE',
};

const STATUS_LED: Record<SystemStatus, string> = {
  ready: 'led-emerald',
  degraded: 'led-amber',
  offline: 'led-crimson',
};

const router = useRouter();
const { config } = useShellConfig();
const workspaceStore = useWorkspaceStore();

const navigate = (featurePath: string) => {
  const key = workspaceStore.activeWorkspaceKey;
  if (!key) return;
  const path = featurePath === '/' ? `/${key}` : `/${key}${featurePath}`;
  router.push(path);
};
</script>

<template>
  <nav class="primary-nav section-low">
    <div class="logo">
      <span class="text-tech">SDLC</span>
      <span class="logo-accent">TOWER</span>
    </div>

    <div class="nav-scroll">
      <div
        v-for="item in NAVIGATION_ITEMS"
        :key="item.key"
        class="nav-item"
        :class="{ active: config.navKey === item.key, 'coming-soon': item.comingSoon }"
        @click="navigate(item.path)"
      >
        <div class="active-indicator"></div>
        <component :is="ICON_MAP[item.key]" class="nav-icon" :size="18" />
        <span class="nav-label">{{ item.label }}</span>
      </div>
    </div>

    <div class="nav-footer">
      <div class="system-status">
        <span class="led" :class="STATUS_LED[props.systemStatus]"></span>
        <span class="text-label">{{ STATUS_LABELS[props.systemStatus] }}</span>
      </div>
    </div>
  </nav>
</template>

<style scoped>
.primary-nav {
  width: 240px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  user-select: none;
}

.logo {
  padding: 24px;
  font-size: 1.25rem;
  font-weight: 800;
  letter-spacing: 0.1em;
  display: flex;
  gap: 4px;
}

.logo-accent {
  color: var(--color-on-surface-variant);
  font-weight: 300;
}

.nav-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.nav-scroll::-webkit-scrollbar { width: 0; }

.nav-item {
  height: 40px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 12px;
  cursor: pointer;
  position: relative;
  color: var(--color-on-surface-variant);
  transition: all 0.2s;
}

.nav-item:hover {
  background: var(--nav-hover-bg);
  color: var(--color-on-surface);
}

.nav-item.active {
  background: var(--nav-active-bg);
  color: var(--color-on-surface);
}

.active-indicator {
  position: absolute;
  left: 0;
  top: 15%;
  bottom: 15%;
  width: 2px;
  background: var(--color-secondary);
  opacity: 0;
  transition: opacity 0.2s;
}

.nav-item.active .active-indicator {
  opacity: 1;
}

.nav-icon {
  flex-shrink: 0;
}

.nav-label {
  font-size: 0.8125rem;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.nav-item.coming-soon {
  opacity: 0.5;
  filter: grayscale(1);
}

.nav-footer {
  padding: 16px;
  border-top: var(--border-ghost);
}

.system-status {
  display: flex;
  align-items: center;
  gap: 8px;
}

@media (max-width: 900px) {
  .primary-nav {
    width: 64px;
  }

  .logo {
    padding: 20px 0;
    justify-content: center;
    font-size: 0.875rem;
  }

  .logo-accent,
  .nav-label,
  .system-status span {
    display: none;
  }

  .nav-item {
    justify-content: center;
    padding: 0;
  }

  .nav-footer {
    padding: 16px 0;
    display: flex;
    justify-content: center;
  }
}
</style>
