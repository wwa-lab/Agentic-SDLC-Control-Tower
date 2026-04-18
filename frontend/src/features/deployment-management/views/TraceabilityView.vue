<script setup lang="ts">
import { computed, onMounted, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useDeploymentStore } from '../stores/deploymentStore';
import TraceabilityInputCard from '../components/traceability/TraceabilityInputCard.vue';
import TraceabilityReleasesCard from '../components/traceability/TraceabilityReleasesCard.vue';
import TraceabilityDeploysCard from '../components/traceability/TraceabilityDeploysCard.vue';
import { MOCK_STORY_IDS } from '../mock/traceability.mock';

const route = useRoute();
const router = useRouter();
const store = useDeploymentStore();

const initialStoryId = computed(() => (route.query.storyId as string) ?? '');

function onLookup(storyId: string) {
  void router.replace({ query: { ...route.query, storyId } });
  void store.lookupStory(storyId);
}
function openRelease(releaseId: string) { void router.push({ name: 'deployment-release', params: { releaseId } }); }
function openDeploy(deployId: string) { void router.push({ name: 'deployment-deploy', params: { deployId } }); }

onMounted(() => { if (initialStoryId.value) void store.lookupStory(initialStoryId.value); });
onBeforeUnmount(() => { store.reset(); });
</script>

<template>
  <div class="traceability-view">
    <TraceabilityInputCard
      :story-ids="MOCK_STORY_IDS"
      :current-story-id="store.activeStoryId ?? ''"
      @lookup-story="onLookup"
    />
    <template v-if="store.traceability">
      <TraceabilityReleasesCard
        :section="store.traceability.releases"
        @open-release="openRelease"
      />
      <TraceabilityDeploysCard
        :section="store.traceability.deploysByEnvironment"
        @open-deploy="openDeploy"
      />
    </template>
  </div>
</template>

<style scoped>
.traceability-view { display: flex; flex-direction: column; gap: 12px; padding: 16px; }
</style>
