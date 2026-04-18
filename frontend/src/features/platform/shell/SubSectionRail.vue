<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { PLATFORM_SUBSECTIONS } from '../shared/constants';

const route = useRoute();
const router = useRouter();

const activeKey = computed(() => {
  const path = route.path;
  const match = PLATFORM_SUBSECTIONS.find(s => path.startsWith(s.path));
  return match?.key ?? 'templates';
});

function navigate(path: string) {
  router.push(path);
}
</script>

<template>
  <nav class="sub-rail" aria-label="Platform sub-sections">
    <button
      v-for="section in PLATFORM_SUBSECTIONS"
      :key="section.key"
      class="rail-item"
      :class="{ active: activeKey === section.key }"
      :aria-current="activeKey === section.key ? 'page' : undefined"
      @click="navigate(section.path)"
    >
      {{ section.label }}
    </button>
  </nav>
</template>

<style scoped>
.sub-rail {
  width: 224px;
  min-width: 224px;
  padding: 12px 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  border-right: 1px solid var(--color-border, #2a2a2a);
}
.rail-item {
  display: block;
  width: 100%;
  padding: 8px 12px;
  border: none;
  border-radius: var(--radius-sm, 4px);
  background: transparent;
  color: var(--color-on-surface-variant, #999);
  font-size: 13px;
  font-weight: 500;
  text-align: left;
  cursor: pointer;
  transition: background-color 80ms ease-out;
}
.rail-item:hover {
  background: var(--color-surface-hover, rgba(255,255,255,0.04));
}
.rail-item.active {
  background: var(--color-surface-active, rgba(255,255,255,0.08));
  color: var(--color-accent-primary, #60a5fa);
}
</style>
