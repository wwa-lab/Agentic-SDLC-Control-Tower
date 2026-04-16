<script setup lang="ts">
import { ArrowRight } from 'lucide-vue-next';
import type { ProjectCardDto } from '../types/projects';

interface Props {
  project: ProjectCardDto;
}

defineProps<Props>();

defineEmits<{
  open: [url: string];
}>();
</script>

<template>
  <button class="project-card" @click="$emit('open', project.projectSpaceUrl)">
    <div class="project-card__header">
      <div>
        <span class="text-label">{{ project.lifecycleStage }} / {{ project.healthStratum }}</span>
        <strong>{{ project.name }}</strong>
      </div>
      <ArrowRight :size="14" />
    </div>
    <p class="text-body-sm">{{ project.primaryRisk ?? 'No primary risk flagged' }}</p>
    <div class="project-card__meta text-tech">
      <span>{{ project.activeSpecCount }} specs</span>
      <span>{{ project.openIncidentCount }} incidents</span>
    </div>
  </button>
</template>

<style scoped>
.project-card {
  border: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(255, 255, 255, 0.03);
  border-radius: var(--radius-sm);
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  color: inherit;
  text-align: left;
  cursor: pointer;
}

.project-card__header,
.project-card__meta {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}
</style>
