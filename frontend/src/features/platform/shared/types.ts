// ── Scope & Auth ─────────────────────────────────────────

export type ScopeType = 'platform' | 'application' | 'workspace' | 'project';

export interface Scope {
  readonly scopeType: ScopeType;
  readonly scopeId: string;
}

export type Role =
  | 'PLATFORM_ADMIN'
  | 'WORKSPACE_ADMIN'
  | 'WORKSPACE_MEMBER'
  | 'WORKSPACE_VIEWER'
  | 'AUDITOR';

export interface CurrentUser {
  readonly userId: string;
  readonly displayName: string;
  readonly roles: Role[];
  readonly scopes: Scope[];
}

// ── Pagination ───────────────────────────────────────────

export interface CursorPagination {
  readonly nextCursor: string | null;
  readonly total: number | null;
}

export interface CursorPage<T> {
  readonly data: T[];
  readonly pagination: CursorPagination;
}

// ── Templates ────────────────────────────────────────────

export type TemplateKind = 'page' | 'flow' | 'project' | 'policy' | 'metric' | 'ai';
export type TemplateStatus = 'draft' | 'published' | 'deprecated';

export interface TemplateSummary {
  readonly id: string;
  readonly key: string;
  readonly name: string;
  readonly kind: TemplateKind;
  readonly status: TemplateStatus;
  readonly version: number;
  readonly ownerId: string;
  readonly lastModifiedAt: string;
}

export interface TemplateVersion {
  readonly id: string;
  readonly templateId: string;
  readonly version: number;
  readonly body: Record<string, unknown>;
  readonly createdAt: string;
  readonly createdBy: string;
}

export interface InheritanceField {
  readonly effectiveValue: unknown;
  readonly winningLayer: 'platform' | 'application' | 'snowGroup' | 'project';
  readonly layers: {
    readonly platform: unknown;
    readonly application: unknown | null;
    readonly snowGroup: unknown | null;
    readonly project: unknown | null;
  };
}

export interface TemplateDetail {
  readonly template: TemplateSummary & { readonly description: string | null };
  readonly version: TemplateVersion;
  readonly inheritance: Record<string, InheritanceField>;
}

// ── Configurations ───────────────────────────────────────

export type ConfigKind = 'page' | 'field' | 'component' | 'flow-rule' | 'view-rule' | 'notification' | 'ai-config';
export type ConfigStatus = 'active' | 'inactive';

export interface ConfigurationSummary {
  readonly id: string;
  readonly key: string;
  readonly kind: ConfigKind;
  readonly scopeType: ScopeType;
  readonly scopeId: string;
  readonly parentId: string | null;
  readonly status: ConfigStatus;
  readonly hasDrift: boolean;
  readonly lastModifiedAt: string;
}

export interface ConfigurationDetail extends ConfigurationSummary {
  readonly body: Record<string, unknown>;
  readonly platformDefaultBody: Record<string, unknown> | null;
  readonly driftFields: string[];
}

export interface DriftInfo {
  readonly hasDrift: boolean;
  readonly driftFields: string[];
  readonly platformDefault: Record<string, unknown>;
  readonly override: Record<string, unknown>;
}

// ── Audit ────────────────────────────────────────────────

export type AuditCategory =
  | 'config_change'
  | 'permission_change'
  | 'ai_suggestion'
  | 'ai_execution'
  | 'approval_decision'
  | 'skill_execution'
  | 'deployment_event'
  | 'incident_event'
  | 'policy_hit'
  | 'integration.test';

export type AuditOutcome = 'success' | 'failure' | 'rejected' | 'rolled_back';
export type ActorType = 'user' | 'system' | 'skill';

export interface AuditRecord {
  readonly id: string;
  readonly timestamp: string;
  readonly actor: string;
  readonly actorType: ActorType;
  readonly category: AuditCategory;
  readonly action: string;
  readonly objectType: string;
  readonly objectId: string;
  readonly scopeType: ScopeType;
  readonly scopeId: string;
  readonly outcome: AuditOutcome;
  readonly evidenceRef: string | null;
  readonly payload: Record<string, unknown>;
}

// ── Access ───────────────────────────────────────────────

export interface RoleAssignment {
  readonly id: string;
  readonly userId: string;
  readonly userDisplayName: string;
  readonly role: Role;
  readonly scopeType: ScopeType;
  readonly scopeId: string;
  readonly grantedBy: string;
  readonly grantedAt: string;
}

// ── Policies ─────────────────────────────────────────────

export type PolicyCategory = 'action' | 'approval' | 'autonomy' | 'risk-threshold' | 'exception';
export type PolicyStatus = 'draft' | 'active' | 'inactive';

export interface Policy {
  readonly id: string;
  readonly key: string;
  readonly name: string;
  readonly category: PolicyCategory;
  readonly scopeType: ScopeType;
  readonly scopeId: string;
  readonly boundTo: string | null;
  readonly version: number;
  readonly status: PolicyStatus;
  readonly body: Record<string, unknown>;
  readonly createdAt: string;
  readonly createdBy: string;
}

export interface PolicyException {
  readonly id: string;
  readonly policyId: string;
  readonly reason: string;
  readonly requesterId: string;
  readonly approverId: string;
  readonly createdAt: string;
  readonly expiresAt: string;
  readonly revokedAt: string | null;
}

// ── Integrations ─────────────────────────────────────────

export type AdapterKind = 'jira' | 'confluence' | 'gitlab' | 'jenkins' | 'servicenow' | 'custom-webhook';
export type SyncMode = 'pull' | 'push' | 'both';
export type ConnectionStatus = 'enabled' | 'disabled' | 'error';

export interface AdapterDescriptor {
  readonly kind: AdapterKind;
  readonly label: string;
  readonly supportedModes: SyncMode[];
  readonly capabilities: string[];
}

export interface Connection {
  readonly id: string;
  readonly kind: AdapterKind;
  readonly scopeWorkspaceId: string;
  readonly applicationId: string | null;
  readonly applicationName: string | null;
  readonly snowGroupId: string | null;
  readonly snowGroupName: string | null;
  readonly baseUrl: string | null;
  readonly credentialRef: string;
  readonly syncMode: SyncMode;
  readonly pullSchedule: string | null;
  readonly pushUrl: string | null;
  readonly status: ConnectionStatus;
  readonly lastSyncAt: string | null;
  readonly lastTestAt: string | null;
  readonly lastTestOk: boolean | null;
}

export interface ConnectionTestResult {
  readonly ok: boolean;
  readonly latencyMs: number;
  readonly message: string | null;
}

// ── Store state helpers ──────────────────────────────────

export type LoadState = 'idle' | 'loading' | 'ready' | 'error';

export interface CatalogState<T> {
  status: LoadState;
  error: string | null;
  items: T[];
  cursor: string | null;
  total: number | null;
  selectedId: string | null;
}
