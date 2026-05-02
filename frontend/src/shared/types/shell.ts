/**
 * Contract for the global workspace context bar.
 * Defined in docs/03-spec/shared-app-shell-spec.md §4.3
 */
export interface WorkspaceContext {
  workspaceId?: string | null;
  workspace: string;
  applicationId?: string | null;
  application: string;
  snowGroupId?: string | null;
  snowGroup?: string | null;
  projectId?: string | null;
  project?: string | null;
  environment?: string | null;
  demoMode?: boolean;
}

export type UserMode = 'staff' | 'guest';
export type AuthProvider = 'manual' | 'teambook' | 'guest';

export interface CurrentUser {
  mode: UserMode;
  authProvider: AuthProvider;
  staffId: string | null;
  displayName: string;
  staffName?: string | null;
  avatarUrl?: string | null;
  roles: string[];
  readOnly: boolean;
  scopes: Array<{ scopeType: string; scopeId: string }>;
}

export interface AuthProviderOption {
  provider: AuthProvider;
  label: string;
  enabled: boolean;
  startUrl: string | null;
}

export interface HelpLinks {
  userGuidelineUrl: string | null;
}

export interface SupportRequest {
  title: string;
  category: 'access' | 'data' | 'bug' | 'question' | 'enhancement';
  description: string;
  route: string;
  context: WorkspaceContext;
  reporterStaffId: string | null;
  reporterMode: UserMode;
}

export interface SupportRequestResult {
  requestId: string;
  status: 'created' | 'pending';
  jiraKey: string | null;
  jiraUrl: string | null;
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
