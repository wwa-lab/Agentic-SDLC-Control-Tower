<script setup lang="ts">
import { FolderKanban } from 'lucide-vue-next';
import type { SectionResult } from '@/shared/types/section';
import type { TeamDefaultTemplates } from '../types/templates';
import TemplateGroup from './TemplateGroup.vue';
import ExceptionOverrideList from './ExceptionOverrideList.vue';
import TeamSpaceCardShell from './TeamSpaceCardShell.vue';

interface Props {
  section: SectionResult<TeamDefaultTemplates>;
  isLoading?: boolean;
}

withDefaults(defineProps<Props>(), {
  isLoading: false,
});

defineEmits<{
  retry: [];
}>();
</script>

<template>
  <TeamSpaceCardShell
    title="Team Default Templates"
    :loading="isLoading"
    :error="section.error"
    @retry="$emit('retry')"
  >
    <template #icon>
      <FolderKanban :size="16" />
    </template>

    <template v-if="section.data">
      <div class="template-groups">
        <TemplateGroup label="Pages" :entries="section.data.groups.PAGE" />
        <TemplateGroup label="Policies" :entries="section.data.groups.POLICY" />
        <TemplateGroup label="Workflows" :entries="section.data.groups.WORKFLOW" />
        <TemplateGroup label="Skill Packs" :entries="section.data.groups.SKILL_PACK" />
        <TemplateGroup label="AI Defaults" :entries="section.data.groups.AI_DEFAULT" />
      </div>

      <ExceptionOverrideList :overrides="section.data.exceptionOverrides" />
    </template>
  </TeamSpaceCardShell>
</template>

<style scoped>
.template-groups {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
</style>
