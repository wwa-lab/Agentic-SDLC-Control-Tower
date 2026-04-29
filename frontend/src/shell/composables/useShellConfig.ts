import { computed } from 'vue';
import { useRoute } from 'vue-router';
import type { ShellPageConfig, ShellAction } from '@/shared/types/shell';

/**
 * Middleware composable between route.meta and shell components.
 * All shell components read config from here — never directly from route.meta.
 */
export function useShellConfig() {
  const route = useRoute();

  const config = computed<ShellPageConfig>(() => ({
    navKey: (route.meta.navKey as string) ?? '',
    title: (route.meta.title as string) ?? '',
    subtitle: route.meta.subtitle as string | undefined,
    actions: route.meta.actions as ReadonlyArray<ShellAction> | undefined,
    showAiPanel: route.meta.showAiPanel !== false,
  }));

  const isComingSoon = computed(() => route.meta.comingSoon === true);

  return {
    config,
    isComingSoon,
  };
}
