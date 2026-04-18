<script setup lang="ts">
import { computed, onMounted, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useDeploymentStore } from '../stores/deploymentStore';
import DeployHeaderCard from '../components/deploy/DeployHeaderCard.vue';
import DeployStagesCard from '../components/deploy/DeployStagesCard.vue';
import DeployApprovalsCard from '../components/deploy/DeployApprovalsCard.vue';
import DeployChangeSummaryCard from '../components/deploy/DeployChangeSummaryCard.vue';
import DeployAiSummaryCard from '../components/deploy/DeployAiSummaryCard.vue';

const route = useRoute();
const router = useRouter();
const store = useDeploymentStore();
const deployId = computed(() => route.params.deployId as string);

const canRegenerate = computed(() => store.viewerContext.role === 'PM' || store.viewerContext.role === 'TECH_LEAD');

function openRelease(releaseId: string) { void router.push({ name: 'deployment-release', params: { releaseId } }); }

onMounted(() => { void store.openDeploy(deployId.value); });
onBeforeUnmount(() => { store.stopDeployPolling(); store.reset(); });
</script>

<template>
  <div class="deploy-detail-view">
    <DeployHeaderCard
      :section="store.deployDetail?.header ?? { data: null, error: null }"
      @open-release="openRelease"
    />
    <div class="detail-grid">
      <DeployStagesCard :section="store.deployDetail?.stageTimeline ?? { data: null, error: null }" />
      <DeployApprovalsCard :section="store.deployDetail?.approvals ?? { data: null, error: null }" />
    </div>
    <DeployChangeSummaryCard :section="store.deployDetail?.artifactRef ?? { data: null, error: null }" />
    <DeployAiSummaryCard
      :section="{ data: null, error: null }"
      :can-regenerate="canRegenerate"
      @regenerate="store.regenerateDeploySummary(deployId)"
    />
  </div>
</template>

<style scoped>
.deploy-detail-view { display: flex; flex-direction: column; gap: 12px; padding: 16px; }
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
@media (max-width: 1023px) { .detail-grid { grid-template-columns: 1fr; } }
</style>
