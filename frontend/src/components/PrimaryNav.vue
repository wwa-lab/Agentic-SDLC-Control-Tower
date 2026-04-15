<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';
import { NAVIGATION_ITEMS } from '@/router';
import { 
  LayoutDashboard, 
  Users, 
  Box, 
  FileText, 
  GitBranch, 
  Layers, 
  Code, 
  TestTube, 
  Send, 
  AlertTriangle, 
  Cpu, 
  BarChart, 
  Settings 
} from 'lucide-vue-next';

const route = useRoute();
const router = useRouter();

// Map keys to icons
const ICON_MAP: Record<string, any> = {
  dashboard: LayoutDashboard,
  team: Users,
  'project-space': Box,
  requirements: FileText,
  'project-management': GitBranch,
  design: Layers,
  code: Code,
  testing: TestTube,
  deployment: Send,
  incidents: AlertTriangle,
  'ai-center': Cpu,
  reports: BarChart,
  platform: Settings
};

const navigate = (path: string) => {
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
        :class="{ active: route.meta.navKey === item.key, 'coming-soon': item.comingSoon }"
        @click="navigate(item.path)"
      >
        <div class="active-indicator"></div>
        <component :is="ICON_MAP[item.key]" class="nav-icon" :size="18" />
        <span class="nav-label">{{ item.label }}</span>
      </div>
    </div>

    <div class="nav-footer">
      <div class="system-status">
        <span class="led led-emerald"></span>
        <span class="text-label">SYSTEM_READY</span>
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

/* Hide scrollbar */
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
</style>
