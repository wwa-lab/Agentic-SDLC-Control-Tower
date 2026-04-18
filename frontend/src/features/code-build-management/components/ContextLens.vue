<script setup lang="ts">
import {
  CODE_BUILD_AUTONOMY_OPTIONS,
  CODE_BUILD_ROLE_OPTIONS,
  CODE_BUILD_WORKSPACE_OPTIONS,
  type AiAutonomyLevel,
  type CodeBuildViewerContext,
  type ProjectRole,
} from '../types';

interface Props {
  context: CodeBuildViewerContext;
}

defineProps<Props>();

const emit = defineEmits<{
  updateWorkspace: [workspaceId: string];
  updateRole: [role: ProjectRole];
  updateAutonomy: [level: AiAutonomyLevel];
}>();
</script>

<template>
  <div class="context-lens section-low">
    <div class="context-lens__field">
      <label class="text-label" for="cb-workspace">Workspace</label>
      <select id="cb-workspace" :value="context.workspaceId" @change="emit('updateWorkspace', ($event.target as HTMLSelectElement).value)">
        <option v-for="workspace in CODE_BUILD_WORKSPACE_OPTIONS" :key="workspace.id" :value="workspace.id">
          {{ workspace.label }}
        </option>
      </select>
    </div>

    <div class="context-lens__field">
      <label class="text-label" for="cb-role">Role</label>
      <select id="cb-role" :value="context.role" @change="emit('updateRole', ($event.target as HTMLSelectElement).value as ProjectRole)">
        <option v-for="role in CODE_BUILD_ROLE_OPTIONS" :key="role" :value="role">
          {{ role }}
        </option>
      </select>
    </div>

    <div class="context-lens__field">
      <label class="text-label" for="cb-autonomy">Autonomy</label>
      <select id="cb-autonomy" :value="context.autonomyLevel" @change="emit('updateAutonomy', ($event.target as HTMLSelectElement).value as AiAutonomyLevel)">
        <option v-for="level in CODE_BUILD_AUTONOMY_OPTIONS" :key="level" :value="level">
          {{ level }}
        </option>
      </select>
    </div>
  </div>
</template>

<style scoped>
.context-lens {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
  padding: 12px;
  border-radius: var(--radius-md);
}

.context-lens__field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

select {
  width: 100%;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-high);
  color: var(--color-on-surface);
  padding: 8px 10px;
}
</style>

