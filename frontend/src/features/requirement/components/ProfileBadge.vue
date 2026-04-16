<script setup lang="ts">
import type { PipelineProfile } from '../types/requirement';

interface Props {
  profile: PipelineProfile;
}

defineProps<Props>();
</script>

<template>
  <span class="profile-badge">{{ profile.name }}</span>
</template>

<style scoped>
.profile-badge {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  padding: 2px 8px;
  border-radius: 2px;
  color: var(--color-secondary);
  background: var(--color-secondary-tint);
  border: 1px solid rgba(137, 206, 255, 0.15);
}
</style>
