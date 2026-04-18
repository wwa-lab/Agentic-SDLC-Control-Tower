<script setup lang="ts">
import type { CatalogSection } from '../../types/catalog';
import type { SectionResult } from '@/shared/types/section';
import HealthLed from '../primitives/HealthLed.vue';
import DeployStateBadge from '../primitives/DeployStateBadge.vue';
import ReleaseVersionPill from '../primitives/ReleaseVersionPill.vue';

defineProps<{ section: SectionResult<ReadonlyArray<CatalogSection>> }>();
const emit = defineEmits<{ openApplication: [applicationId: string] }>();
</script>

<template>
  <div class="grid-card card">
    <div v-if="section.error" class="card-error">{{ section.error }}</div>
    <div v-else-if="!section.data" class="card-skeleton">Loading catalog grid...</div>
    <div v-else-if="section.data.length === 0" class="card-empty">No applications match your filters.</div>
    <template v-else>
      <div v-for="group in section.data" :key="group.projectId" class="project-group">
        <div class="project-header">{{ group.projectName }}</div>
        <div class="tiles">
          <button
            v-for="app in group.applications"
            :key="app.applicationId"
            class="tile"
            @click="emit('openApplication', app.applicationId)"
          >
            <div class="tile-top">
              <HealthLed :led="app.aggregateLed" />
              <span class="tile-name">{{ app.name }}</span>
              <span class="tile-runtime">{{ app.runtimeLabel }}</span>
            </div>
            <div class="env-pills">
              <span
                v-for="env in app.environmentRevisions"
                :key="env.environmentName"
                class="env-pill"
                :class="{ 'rolled-back': env.isRolledBack }"
              >
                <span class="env-label">{{ env.environmentName }}</span>
                <DeployStateBadge v-if="env.deployState" :state="env.deployState" />
                <ReleaseVersionPill v-if="env.revisionReleaseVersion" :version="env.revisionReleaseVersion" />
                <span v-else class="no-deploy">&mdash;</span>
              </span>
            </div>
          </button>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.grid-card { padding: 16px; }
.card { background: var(--color-surface-container-low); border: var(--border-ghost); border-radius: var(--radius-md); box-shadow: var(--shadow-card); }
.card-error { color: var(--color-incident-crimson); font-size: 0.85rem; }
.card-skeleton, .card-empty { color: var(--color-on-surface-variant); font-size: 0.85rem; }
.project-group { margin-bottom: 16px; }
.project-group:last-child { margin-bottom: 0; }
.project-header {
  position: sticky; top: 0; z-index: 2;
  padding: 6px 0;
  font-family: var(--font-ui); font-size: 0.75rem; font-weight: 700;
  color: var(--color-on-surface-variant);
  text-transform: uppercase; letter-spacing: 0.05em;
  background: var(--color-surface-container-low);
}
.tiles { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 8px; }
.tile {
  all: unset;
  cursor: pointer;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  border: var(--border-ghost);
  background: var(--color-surface-container-high);
  transition: box-shadow 0.15s;
}
.tile:hover { box-shadow: var(--shadow-card-hover); }
.tile-top { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.tile-name { font-family: var(--font-ui); font-size: 0.85rem; font-weight: 600; color: var(--color-on-surface); }
.tile-runtime { font-family: var(--font-tech); font-size: 0.65rem; color: var(--color-on-surface-variant); margin-left: auto; }
.env-pills { display: flex; flex-wrap: wrap; gap: 6px; }
.env-pill { display: flex; align-items: center; gap: 4px; font-size: 0.7rem; }
.env-label { font-family: var(--font-tech); font-size: 0.6rem; color: var(--color-on-surface-variant); text-transform: uppercase; min-width: 36px; }
.rolled-back { opacity: 0.7; }
.no-deploy { color: var(--color-on-surface-variant); }
</style>
