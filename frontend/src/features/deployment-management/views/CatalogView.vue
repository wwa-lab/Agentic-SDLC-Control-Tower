<script setup lang="ts">
import { computed, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import { useDeploymentStore } from '../stores/deploymentStore';
import CatalogSummaryBarCard from '../components/catalog/CatalogSummaryBarCard.vue';
import CatalogFilterBar from '../components/catalog/CatalogFilterBar.vue';
import CatalogGridCard from '../components/catalog/CatalogGridCard.vue';
import CatalogAiSummaryCard from '../components/catalog/CatalogAiSummaryCard.vue';
import type { CatalogFilters } from '../types/catalog';

const router = useRouter();
const store = useDeploymentStore();

const summarySection = computed(() => store.catalog?.summary ?? { data: null, error: null });
const gridSection = computed(() => store.catalog?.grid ?? { data: null, error: null });
const aiSection = computed(() => store.catalog?.aiSummary ?? { data: null, error: null });
const canRegenerate = computed(() => store.viewerContext.role === 'PM' || store.viewerContext.role === 'TECH_LEAD');

function onFilterChange(filters: CatalogFilters) {
  void store.initCatalog(filters);
}

function onOpenApplication(applicationId: string) {
  void router.push({ name: 'deployment-application', params: { applicationId } });
}

onMounted(() => { void store.initCatalog(); });
onBeforeUnmount(() => { store.reset(); });
</script>

<template>
  <div class="catalog-view">
    <CatalogSummaryBarCard :section="summarySection" />
    <CatalogFilterBar :filters="store.catalogFilters" @change="onFilterChange" />
    <div class="catalog-body">
      <CatalogGridCard :section="gridSection" @open-application="onOpenApplication" />
      <CatalogAiSummaryCard
        :section="aiSection"
        :can-regenerate="canRegenerate"
        @regenerate="store.regenerateWorkspaceSummary()"
      />
    </div>
  </div>
</template>

<style scoped>
.catalog-view {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
}
.catalog-body {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 12px;
}
@media (max-width: 1279px) {
  .catalog-body { grid-template-columns: 1fr; }
}
</style>
