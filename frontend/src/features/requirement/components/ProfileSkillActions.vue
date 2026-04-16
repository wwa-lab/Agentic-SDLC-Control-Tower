<script setup lang="ts">
import type { PipelineProfile } from '../types/requirement';

interface Props {
  profile: PipelineProfile;
}

defineProps<Props>();
const emit = defineEmits<{ invokeSkill: [skillId: string] }>();
</script>

<template>
  <div class="skill-actions">
    <!-- Standard SDD: render action buttons from profile skill bindings -->
    <template v-if="!profile.usesOrchestrator">
      <button
        v-for="skill in profile.skills"
        :key="skill.skillId"
        class="skill-btn"
        @click="emit('invokeSkill', skill.skillId)"
      >
        {{ skill.label }}
      </button>
    </template>

    <!-- IBM i: single "Send to Orchestrator" button -->
    <template v-else>
      <button
        class="skill-btn skill-btn--orchestrator"
        @click="emit('invokeSkill', profile.skills[0]?.skillId ?? '')"
      >
        {{ profile.skills[0]?.label ?? 'Send to Orchestrator' }}
      </button>
    </template>
  </div>
</template>

<style scoped>
.skill-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.skill-btn {
  background: none;
  border: 1px solid var(--color-secondary);
  color: var(--color-secondary);
  padding: 4px 12px;
  border-radius: var(--radius-sm);
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  cursor: pointer;
  transition: all 0.2s ease;
}

.skill-btn:hover {
  background: var(--color-secondary);
  color: var(--color-on-secondary-container);
}

.skill-btn--orchestrator {
  border-color: #a78bfa;
  color: #a78bfa;
}

.skill-btn--orchestrator:hover {
  background: #a78bfa;
  color: #1a1f2e;
}
</style>
