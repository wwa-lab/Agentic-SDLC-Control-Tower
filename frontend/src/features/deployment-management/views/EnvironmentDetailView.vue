<script setup lang="ts">
import { computed, onMounted, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useDeploymentStore } from '../stores/deploymentStore';
import EnvironmentHeaderCard from '../components/environment/EnvironmentHeaderCard.vue';
import EnvironmentCurrentCard from '../components/environment/EnvironmentCurrentCard.vue';
import EnvironmentTimelineCard from '../components/environment/EnvironmentTimelineCard.vue';
import EnvironmentStabilityCard from '../components/environment/EnvironmentStabilityCard.vue';

const route = useRoute();
const router = useRouter();
const store = useDeploymentStore();
const applicationId = computed(() => route.params.applicationId as string);
const environmentName = computed(() => route.params.environmentName as string);

function openDeploy(deployId: string) { void router.push({ name: 'deployment-deploy', params: { deployId } }); }

onMounted(() => { void store.openEnvironment(applicationId.value, environmentName.value); });
onBeforeUnmount(() => { store.reset(); });
</script>

<template>
  <div class="env-detail-view">
    <EnvironmentHeaderCard :section="store.environmentDetail?.header ?? { data: null, error: null }" />
    <EnvironmentCurrentCard
      :section="store.environmentDetail?.revisions ?? { data: null, error: null }"
      @open-deploy="openDeploy"
    />
    <EnvironmentTimelineCard
      :section="store.environmentDetail?.timeline ?? { data: null, error: null }"
      @open-deploy="openDeploy"
    />
    <EnvironmentStabilityCard :section="store.environmentDetail?.metrics ?? { data: null, error: null }" />
  </div>
</template>

<style scoped>
.env-detail-view { display: flex; flex-direction: column; gap: 12px; padding: 16px; }
</style>
