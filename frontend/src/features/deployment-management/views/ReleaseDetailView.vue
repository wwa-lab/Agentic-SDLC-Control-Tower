<script setup lang="ts">
import { computed, onMounted, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useDeploymentStore } from '../stores/deploymentStore';
import ReleaseHeaderCard from '../components/release/ReleaseHeaderCard.vue';
import ReleaseCompositionCard from '../components/release/ReleaseCompositionCard.vue';
import ReleaseDeploysCard from '../components/release/ReleaseDeploysCard.vue';
import ReleaseLinkedStoriesCard from '../components/release/ReleaseLinkedStoriesCard.vue';
import ReleaseAiNotesCard from '../components/release/ReleaseAiNotesCard.vue';

const route = useRoute();
const router = useRouter();
const store = useDeploymentStore();
const releaseId = computed(() => route.params.releaseId as string);

const canRegenerate = computed(() => store.viewerContext.role === 'PM' || store.viewerContext.role === 'TECH_LEAD');

function openDeploy(deployId: string) { void router.push({ name: 'deployment-deploy', params: { deployId } }); }

onMounted(() => { void store.openRelease(releaseId.value); });
onBeforeUnmount(() => { store.reset(); });
</script>

<template>
  <div class="release-detail-view">
    <ReleaseHeaderCard :section="store.releaseDetail?.header ?? { data: null, error: null }" />
    <div class="detail-grid">
      <ReleaseCompositionCard
        :section="store.releaseDetail?.commits ?? { data: null, error: null }"
        :cap-notice="store.releaseDetail?.capNotice"
      />
      <ReleaseLinkedStoriesCard :section="store.releaseDetail?.linkedStories ?? { data: null, error: null }" />
    </div>
    <ReleaseDeploysCard
      :section="store.releaseDetail?.deploys ?? { data: null, error: null }"
      @open-deploy="openDeploy"
    />
    <ReleaseAiNotesCard
      :section="store.releaseDetail?.aiNotes ?? { data: null, error: null }"
      :can-regenerate="canRegenerate"
      @regenerate="store.regenerateReleaseNotes(releaseId)"
    />
  </div>
</template>

<style scoped>
.release-detail-view { display: flex; flex-direction: column; gap: 12px; padding: 16px; }
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
@media (max-width: 1023px) { .detail-grid { grid-template-columns: 1fr; } }
</style>
