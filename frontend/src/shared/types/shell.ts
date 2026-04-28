/**
 * Contract for the global workspace context bar.
 * Defined in docs/03-spec/shared-app-shell-spec.md §4.3
 */
export interface WorkspaceContext {
  workspace: string;
  application: string;
  snowGroup?: string | null;
  project?: string | null;
  environment?: string | null;
}

/**
 * Contract for page integration with the shell.
 * Defined in docs/03-spec/shared-app-shell-spec.md §7
 */
export interface ShellPageConfig {
  navKey: string;
  title: string;
  subtitle?: string;
  actions?: ReadonlyArray<ShellAction>;
  showAiPanel?: boolean;
}

export interface ShellAction {
  key: string;
  label: string;
  variant?: 'default' | 'ai';
}

/**
 * Navigation item structure.
 */
export interface NavItem {
  key: string;
  label: string;
  path: string;
  icon: string;
  comingSoon?: boolean;
}

/**
 * System status for the nav footer.
 */
export type SystemStatus = 'ready' | 'degraded' | 'offline';

/**
 * AI Command Panel zone content.
 */
export interface AiPanelContent {
  summary: string;
  reasoning: ReadonlyArray<{ text: string; status: 'ok' | 'warning' | 'error' }>;
  evidence: string;
}
