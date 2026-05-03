# Shared App Shell Spec

## Purpose

This spec defines the implementation-facing contract for the shared shell used
by Round 1 pages.

## 1. Scope

In scope:

- shared shell layout
- primary navigation region
- top context bar region
- page title and action region contract
- global utility entries
- persistent AI Command Panel region
- route-to-active-nav mapping
- staff-id login and current-user session
- optional TeamBook SSO profile integration for internal deployments
- guest read-only mode
- global Contact Us and User Guideline actions

Out of scope:

- page-specific business modules
- full enterprise SSO / MFA / directory sync
- final visual token system
- responsive mobile behavior
- full AI execution logic

## 2. Layout Contract

The shell must expose these structural regions:

- `PrimaryNav`
- `TopContextBar`
- `PageHeader`
- `PageContent`
- `AiCommandPanel`

The shell is the outer frame for all Round 1 business pages.

## 3. Primary Navigation Contract

### 3.1 Required Entries

The navigation must render the following entries in fixed order:

1. Dashboard
2. Team Space
3. Project Space
4. Requirement Management
5. Project Management
6. Design Management
7. Code & Build
8. Testing
9. Deployment
10. Incident Management
11. AI Center
12. Report Center
13. Platform Center

### 3.2 Active State Rules

- Exactly one item may be active at a time
- Active state is determined by current route identity
- Active state uses the same visual pattern across pages
- V1 baseline pattern is a left-edge active indicator

### 3.3 Behavior Rules

- Navigation labels use PRD vocabulary
- Navigation items are shell-owned, not page-owned
- Navigation renders the full set for authenticated users and guests; write affordances are filtered by user mode / role

## 3A. Authentication Contract

### 3A.1 Login Rules

- Staff id is the primary credential identifier and is treated as a string to preserve leading zeros if they ever exist.
- Password must be non-empty.
- V1 validates password presence only; password storage must still use a hash or dev-only placeholder hash, never plaintext.
- Successful login returns a current-user payload and establishes a backend session.
- Manual login uses `authProvider = "manual"` and remains available for local/external deployments and as an internal fallback.
- Deployed sessions must be multi-instance safe: signed server-validated token or shared session store; no single-node in-memory session dependency.
- Repeated failed login attempts are rate-limited by staff id and source.
- Logout invalidates the current staff or guest session.
- Session expiry is backend-configured; expired sessions return 401 from `/auth/me`.

### 3A.2 TeamBook SSO Rules

- TeamBook is an optional provider enabled only in internal deployments.
- The shell discovers enabled providers from backend configuration; it must not hardcode TeamBook availability.
- TeamBook-authenticated sessions use `authProvider = "teambook"`.
- TeamBook profile data may populate `staffName` and `avatarUrl`; profile enrichment is best-effort and must not store TeamBook tokens.
- Authorization still requires an active `PlatformUser` and role assignments for the returned staff id; just-in-time user creation is disabled by default.

### 3A.3 Guest Rules

- The shell exposes "Continue as Guest" before authentication.
- Guest mode creates a current-user payload with `mode = "guest"` and `readOnly = true`.
- Guest mode uses a demo/public data scope and never real Application + SNOW team data.
- Server-side write guards must reject guest writes with 403.

### 3A.4 Current User Contract

```ts
type UserMode = 'staff' | 'guest';
type AuthProvider = 'manual' | 'teambook' | 'guest';

type CurrentUser = {
  mode: UserMode;
  authProvider: AuthProvider;
  staffId: string | null;
  displayName: string;
  staffName?: string | null;
  avatarUrl?: string | null;
  roles: string[];
  readOnly: boolean;
  scopes: Array<{ scopeType: string; scopeId: string }>;
};
```

## 4. Context Bar Contract

### 4.1 Required Fields

The top context bar must support display of:

- `workspace`
- `application`
- `snowGroup`
- `project`
- `environment`

### 4.2 Display Rules

- Fields are displayed in a fixed order
- Each field has a stable label and value region
- Missing optional values render as empty-safe placeholders rather than breaking layout
- The context bar is visible on all Round 1 business pages

### 4.3 Data Contract

V1 contract:

```ts
type WorkspaceContext = {
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
};
```

## 5. Global Utility Contract

The shell must expose entry points for:

- global search
- notifications
- audit/history
- Contact Us
- User Guideline

### 5.1 Contact Us Contract

`ContactUsButton` opens a modal form. Submit calls `POST /api/v1/support/contact`
and expects either a created Jira story or a persisted pending support request.
The request includes title, description, category, route, context, and reporter
identity. The backend persists the request before calling Jira and uses an
outbox/retry path so Jira downtime does not lose user submissions.

### 5.2 User Guideline Contract

`UserGuidelineButton` fetches `GET /api/v1/shell/help-links` or reads the loaded
shell config and opens `userGuidelineUrl` in a new browser tab. If the URL is
absent, the button renders disabled.

These utilities are shell-level controls and are not page-specific actions.

## 6. AI Command Panel Contract

### 6.1 Structural Requirement

The shell must render a persistent right-side AI panel region in desktop layouts.

### 6.2 Content Zones

The panel should support these zones:

- summary
- reasoning
- evidence
- action or command input area

### 6.3 Ownership Rules

- The shell owns the panel container
- Pages provide panel content or content configuration
- AI Center is the global management page
- AI Command Panel is the current-page projection of AI state
- Pages with no page-scoped AI projection may suppress the panel; the main page content remains the source of truth for review, freshness, and execution status.

## 7. Page Integration Contract

Each page using the shell must be able to provide:

- page title
- page subtitle or context hint
- optional page-level actions
- main body content
- page-scoped AI panel content
- optional AI panel visibility

V1 contract:

```ts
type ShellPageConfig = {
  navKey: string;
  title: string;
  subtitle?: string;
  actions?: Array<{ key: string; label: string }>;
  showAiPanel?: boolean;
};
```

## 8. Error And Empty State Rules

The shell itself must support:

- loading context
- partial context data
- permission-restricted utilities
- missing AI panel content

Fallback behavior:

- shell structure still renders
- unavailable regions degrade with placeholder or disabled state
- page content failures do not remove the surrounding shell
- guest write attempts fail without leaving the current read-only page

## 9. Audit And Traceability Notes

- Shell-level utility actions should be auditable in later slices
- Contact Us submissions write support-request audit events when queued and when the Jira story is created
- Route changes themselves do not require audit for V1 foundation
- Context display must remain consistent with workspace isolation rules

## 10. Risks And Open Questions

- Navigation label vocabulary: the PRD uses full forms (e.g., "Code & Build Management") while some downstream docs use shortened forms. The final nav labels should be confirmed before implementation.
- The AI Command Panel container is defined here but its content delivery contract (how pages provide content to zones) will need resolution before the first page-with-AI slice.
- Exact Jira project key and Confluence guideline URL are environment configuration values.
- Guest mode must remain on demo/public data unless an explicit product decision allows real data exposure.
- TeamBook claim/header names, callback path, and avatar URL lifetime are internal-environment configuration values.

## 11. Exit Criteria

This spec is ready for implementation when:

- the route list is accepted
- context field contract is accepted
- shell regions are accepted
- ownership boundary between shell and page content is accepted
- auth / guest / support / guideline contracts are accepted
