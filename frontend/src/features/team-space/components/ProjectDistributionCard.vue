<script setup lang="ts">
import { LayoutTemplate } from 'lucide-vue-next';
import { ref } from 'vue';
import type { SectionResult } from '@/shared/types/section';
import type { ProjectDistribution } from '../types/projects';
import type { ProjectHealthStratum } from '../types/enums';
import TeamSpaceCardShell from './TeamSpaceCardShell.vue';
import ProjectCard from './ProjectCard.vue';

interface Props {
  section: SectionResult<ProjectDistribution>;
  isLoading?: boolean;
}

withDefaults(defineProps<Props>(), {
  isLoading: false,
});

defineEmits<{
  retry: [];
  openProject: [url: string];
}>();

const activeTab = ref<ProjectHealthStratum>('HEALTHY');
const tabs: ReadonlyArray<ProjectHealthStratum> = ['HEALTHY', 'AT_RISK', 'CRITICAL', 'ARCHIVED'];
</script>

<template>
  <TeamSpaceCardShell
    title="Project Distribution"
    :loading="isLoading"
    :error="section.error"
    @retry="$emit('retry')"
  >
    <template #icon>
      <LayoutTemplate :size="16" />
    </template>

    <template v-if="section.data">
      <div class="project-tabs">
        <button
          v-for="tab in tabs"
          :key="tab"
          class="project-tab"
          :class="{ 'project-tab--active': activeTab === tab }"
          @click="activeTab = tab"
        >
          {{ tab }}
          <span class="text-tech">{{ section.data.totals[tab] }}</span>
        </button>
      </div>
      <div class="project-grid">
        <ProjectCard
          v-for="project in section.data.groups[activeTab]"
          :key="project.id"
          :project="project"
          @open="$emit('openProject', $event)"
        />
        <p v-if="section.data.groups[activeTab].length === 0" class="text-body-sm project-empty">
          No projects currently fall into this stratum.
        </p>
      </div>
    </template>
  </TeamSpaceCardShell>
</template>

<style scoped>
.project-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.project-tab {
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: transparent;
  color: inherit;
  border-radius: 999px;
  padding: 6px 12px;
  cursor: pointer;
  display: inline-flex;
  gap: 8px;
  align-items: center;
}

.project-tab--active {
  border-color: rgba(137, 206, 255, 0.26);
  background: rgba(137, 206, 255, 0.08);
}

.project-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.project-empty {
  color: var(--color-on-surface-variant);
}

@media (max-width: 1200px) {
  .project-grid {
    grid-template-columns: 1fr;
  }
}
</style>
