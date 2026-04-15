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
  actions?: Array<{ key: string; label: string }>;
}

/**
 * Navigation item structure.
 */
export interface NavItem {
  key: string;
  label: string;
  path: string;
  comingSoon?: boolean;
}
