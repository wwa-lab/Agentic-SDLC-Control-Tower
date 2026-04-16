import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import type { AiPanelContent } from '@/shared/types/shell';

export interface BreadcrumbItem {
  readonly label: string;
  readonly path?: string;
}

const DEFAULT_AI_PANEL: AiPanelContent = {
  summary: 'Observing shell deployment. Workspace context is stable. AI is ready for command injection.',
  reasoning: [
    { text: 'Verified "No-Line" Rule compliance.', status: 'ok' },
    { text: 'Tonal hierarchy active.', status: 'ok' },
  ],
  evidence: JSON.stringify({ status: 'ready', components: 5, routes: 13, style: 'tactical_cmd' }, null, 2),
};

export const useShellUiStore = defineStore('shellUi', () => {
  const breadcrumbs = ref<ReadonlyArray<BreadcrumbItem>>([]);
  const aiPanelContent = ref<AiPanelContent | null>(null);

  const resolvedAiPanelContent = computed(() => aiPanelContent.value ?? DEFAULT_AI_PANEL);

  function setBreadcrumbs(next: ReadonlyArray<BreadcrumbItem>) {
    breadcrumbs.value = next;
  }

  function clearBreadcrumbs() {
    breadcrumbs.value = [];
  }

  function setAiPanelContent(next: AiPanelContent) {
    aiPanelContent.value = next;
  }

  function clearAiPanelContent() {
    aiPanelContent.value = null;
  }

  return {
    breadcrumbs,
    resolvedAiPanelContent,
    setBreadcrumbs,
    clearBreadcrumbs,
    setAiPanelContent,
    clearAiPanelContent,
  };
});
