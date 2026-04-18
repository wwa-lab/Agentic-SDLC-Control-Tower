<script setup lang="ts">
import { computed, onMounted, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useDeploymentStore } from '../stores/deploymentStore';
import ApplicationHeaderCard from '../components/application/ApplicationHeaderCard.vue';
import ApplicationEnvironmentsCard from '../components/application/ApplicationEnvironmentsCard.vue';
import ApplicationRecentReleasesCard from '../components/application/ApplicationRecentReleasesCard.vue';
import ApplicationRecentDeploysCard from '../components/application/ApplicationRecentDeploysCard.vue';
import ApplicationMetricsCard from '../components/application/ApplicationMetricsCard.vue';
import ApplicationAiInsightsCard from '../components/application/ApplicationAiInsightsCard.vue';

const route = useRoute();
const router = useRouter();
const store = useDeploymentStore();
const applicationId = computed(() => route.params.applicationId as string);

const canRegenerate = computed(() => store.viewerContext.role === 'PM' || store.viewerContext.role === 'TECH_LEAD');

function openRelease(releaseId: string) { void router.push({ name: 'deployment-release', params: { releaseId } }); }
function openDeploy(deployId: string) { void router.push({ name: 'deployment-deploy', params: { deployId } }); }
function openEnvironment(_appId: string, envName: string) { void router.push({ name: 'deployment-environment', params: { applicationId: applicationId.value, environmentName: envName } }); }

onMounted(() => { void store.openApplication(applicationId.value); });
onBeforeUnmount(() => { store.reset(); });
</script>

<template>
  <div class="app-detail-view">
    <ApplicationHeaderCard :section="store.applicationDetail?.header ?? { data: null, error: null }" />
    <ApplicationEnvironmentsCard
      :section="store.applicationDetail?.environments ?? { data: null, error: null }"
      @open-environment="openEnvironment"
    />
    <div class="detail-grid">
      <ApplicationRecentReleasesCard
        :section="store.applicationDetail?.recentReleases ?? { data: null, error: null }"
        @open-release="openRelease"
      />
      <ApplicationRecentDeploysCard
        :section="store.applicationDetail?.recentDeploys ?? { data: null, error: null }"
        @open-deploy="openDeploy"
      />
    </div>
    <div class="detail-grid">
      <ApplicationMetricsCard :section="store.applicationDetail?.traceSummary ?? { data: null, error: null }" />
      <ApplicationAiInsightsCard
        :section="store.applicationDetail?.aiInsights ?? { data: null, error: null }"
        :can-regenerate="canRegenerate"
      />
    </div>
  </div>
</template>

<style scoped>
.app-detail-view { display: flex; flex-direction: column; gap: 12px; padding: 16px; }
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
@media (max-width: 1023px) { .detail-grid { grid-template-columns: 1fr; } }
</style>
