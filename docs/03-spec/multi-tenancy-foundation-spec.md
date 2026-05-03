# Multi-Tenancy Foundation Spec

## Overview

This spec implements `multi-tenancy-foundation-requirements.md` and the
stories in `multi-tenancy-foundation-stories.md`. It defines the contracts
for:

1. Path-based workspace identity (API uses stable id; UI uses friendly key)
2. Workspace context resolution at the HTTP boundary
3. Scope-based authorization
4. Repository-level auto-filter via Hibernate `@Filter`
5. Control-plane entity migration to carry their own `workspace_id`
6. Cross-workspace escape hatch
7. Auth API extensions
8. Frontend Vue Router rebase + `apiClient` URL rewriting
9. Workspace switcher behavior, including URL handoff and key rename
10. Demo / guest fallback and IBM-i seed
11. Migration and seed strategy

## Profile

`platform-foundation-sdd` — backend-first, no business UI flows; engineering
review in GitHub.

## Functional Rules

### FR-MTF-01: Canonical URL forms

API canonical form (server-side):

```
/api/v1/workspaces/{workspaceId}/<resource>...
```

UI canonical form (client-side):

```
/{workspaceKey}/<feature>...
```

`workspaceId` is the stable database key (`PLATFORM_WORKSPACE.id`,
e.g., `ws-default-001`). `workspaceKey` is the human-friendly slug
(`PLATFORM_WORKSPACE.workspace_key`, e.g., `payment-gateway-pro`,
`ibm-i-team`). The Vue Router resolves `workspaceKey` to `workspaceId`
once on view mount via the `workspaces` map cached from
`GET /api/v1/auth/workspaces`.

The cross-workspace allowlist (FR-MTF-04) bypasses the path prefix.

### FR-MTF-02: Workspace context resolution order

For every request to `/api/v1/**`, a `WorkspaceContextInterceptor` runs
before any controller method. Resolution proceeds in this order:

1. If the path matches the allowlist (FR-MTF-04) → set context to `null`
   and continue.
2. If the path does not match
   `/api/v1/workspaces/{workspaceId}/...` → throw `404`.
3. If no `sdlc_session` cookie → throw `401 AUTH_REQUIRED`.
4. Resolve `currentUser` via `AuthService.requireUser`.
5. If `workspaceId == "demo"` AND `currentUser.mode == "guest"` AND
   `AuthProperties.demoMode == true` → resolve workspace from
   `workspace_context.workspace_id` and continue (FR-MTF-15).
6. Else look up `PLATFORM_WORKSPACE` row by id; if absent → throw
   `404 WORKSPACE_NOT_FOUND`.
7. Run authorization (FR-MTF-03). On success, set
   `WorkspaceContextHolder.set(...)` for the request lifetime; clear in
   a finally block.

`workspaceId` is extracted from the URL path via
`@PathVariable String workspaceId` on the interceptor's request mapping
inspection, not by string parsing in domain code.

### FR-MTF-03: Scope authorization

Given the resolved workspace
`W = { workspaceId, workspaceKey, applicationId, snowGroupId }` and
`currentUser.scopes`, a request is authorized iff at least one scope
satisfies:

- `platform:*` → always
- `application:{X}` → `X == W.applicationId`
- `snow_group:{X}` → `X == W.snowGroupId`
- `workspace:{X}` → `X == W.workspaceId`
- `project:{X}` → exists `project` row with `id = X` AND
  `workspace_id = W.workspaceId`

If no scope matches, throw `403 WORKSPACE_SCOPE_REQUIRED` and write a
`permission_change / workspace.access_denied` audit row with payload
`{ "claimedWorkspaceId": W.workspaceId, "reason": "no_matching_scope" }`.

Project-scope check uses the `projectToWorkspace` cache (FR-MTF-13).

### FR-MTF-04: Cross-workspace endpoint allowlist

Paths exempt from the workspace prefix:

| Path | Auth |
|---|---|
| `/api/v1/auth/**` | Public + post-login |
| `/api/v1/health` | None |
| `/api/v1/integration/webhooks/**` | Webhook signature |
| `/api/v1/platform/**` | `PLATFORM_ADMIN` |
| `/api/v1/reports/fleet/**` | `AUDITOR` or `PLATFORM_ADMIN` |

The interceptor matches these via Spring `PathPatternParser` and applies
their per-path auth rule. All other `/api/v1/**` paths are required to
match the workspace-prefixed canonical form (FR-MTF-01).

### FR-MTF-05: WorkspaceContextHolder

`com.sdlctower.platform.workspace.WorkspaceContextHolder` exposes a
`ThreadLocal<WorkspaceContext>` plus three operations:

```
WorkspaceContext current()                              // throws MISSING_WORKSPACE_CONTEXT if unset
Optional<WorkspaceContext> maybeCurrent()
<T> T runWithCrossWorkspaceAccess(String reason, Supplier<T> action)
```

`runWithCrossWorkspaceAccess` sets a "cross-workspace" flag on the holder
for the duration of the action. The flag suppresses the Hibernate filter
(FR-MTF-06) and emits one INFO log line per invocation:
`workspace.cross.access reason=<reason> staffId=<id|system> from=<workspaceId|none>`.

The holder propagates across `@Async` boundaries via a `TaskDecorator`.

### FR-MTF-06: Repository auto-filter via Hibernate `@Filter`

Implementation uses Hibernate `@FilterDef` + `@Filter`. Definition lives in
`com.sdlctower.shared.persistence.WorkspaceFilter`:

```
@FilterDef(
    name = "workspace_filter",
    parameters = @ParamDef(name = "workspaceId", type = String.class)
)
```

Every entity with a `workspace_id` column declares:

```
@Filter(name = "workspace_filter", condition = "workspace_id = :workspaceId")
```

A `WorkspaceFilterAspect` (Spring AOP, around the controller layer) checks
the holder per request:

- Holder set, cross-workspace flag OFF → enable session filter with the
  current `workspaceId`
- Holder empty, cross-workspace flag OFF → leave filter disabled; if any
  query against a filtered entity executes, throw
  `MISSING_WORKSPACE_CONTEXT`
- Cross-workspace flag ON → leave filter disabled

A reflection regression test
`AllWorkspaceScopedEntitiesAreFiltered` enumerates `@Entity` classes with
a `workspace_id` column and asserts each carries `@Filter`.

### FR-MTF-07: Control-plane entities denormalize `workspace_id`

The 7 control-plane tables that today scope through
`requirement.workspace_id` get an explicit column:

| Table | Backfill source |
|---|---|
| `requirement_agent_run` | `requirement.workspace_id` via `requirement_id` |
| `requirement_control_plane_source` | same |
| `requirement_control_plane_document` | same |
| `requirement_document_review` | document → requirement |
| `requirement_agent_stage_event` | run → requirement |
| `requirement_document_quality_gate_run` | document → requirement |
| `artifact_link` | run → requirement |

Migration `V97` adds the column (nullable), `V98` backfills, `V99` adds
`NOT NULL` + composite index. Application code that inserts rows must set
`workspace_id` from `WorkspaceContextHolder.current().workspaceId`, not by
re-deriving from the parent. A unit test asserts insertion sets the column.

### FR-MTF-08: Cross-workspace escape hatch

Allowed callers and their reason codes:

| Caller | Reason code |
|---|---|
| `JenkinsWebhookController` ingest | `webhook.jenkins.ingest` |
| `GithubWebhookController` ingest | `webhook.github.ingest` |
| `ReportCenterService` fleet-level reads | `report.center.fleet` |
| `PlatformAccessService` user / role ops | `platform.access.admin` |
| Flyway-driven seed loaders | `seed.bootstrap` |
| Vue Router resolution
  (`workspaceKey → workspaceId`) | `auth.workspace.resolve` |

Any addition to this list updates the spec and the
`CrossWorkspaceReason` enum.

### FR-MTF-09: Auth API extensions

`POST /api/v1/auth/workspace`

Request:
```json
{ "workspaceId": "ws-ibm-i-team" }
```

Response (200):
```json
{
  "workspaceId": "ws-ibm-i-team",
  "workspaceKey": "ibm-i-team",
  "applicationId": "app-ibmi-core",
  "snowGroupId": "snow-ibmi-ops",
  "workspaceName": "IBM-i Team",
  "applicationName": "IBM-i Core",
  "snowGroupName": "IBM-i Operations"
}
```

Behavior:

1. Resolve current user (must be authenticated, not guest).
2. Run scope authorization (FR-MTF-03). On failure → `403`.
3. Persist `permission_change / workspace.switch` audit event with payload
   `{ "before": "<previous-id-or-null>", "after": "<new-id>" }`.
4. Set `sdlc_workspace` cookie (HttpOnly, SameSite=Lax) with the new id.

`GET /api/v1/auth/workspaces`

Returns the list of `PLATFORM_WORKSPACE` rows the current user can access:

```json
{
  "success": true,
  "data": [
    {
      "workspaceId": "ws-default-001",
      "workspaceKey": "payment-gateway-pro",
      "name": "Payment Gateway Pro",
      "applicationId": "app-payment-gateway-pro",
      "applicationName": "Payment Gateway Pro",
      "snowGroupId": "snow-fin-tech-ops",
      "snowGroupName": "Finance Tech Ops",
      "profileId": "standard-java-sdd"
    },
    {
      "workspaceId": "ws-ibm-i-team",
      "workspaceKey": "ibm-i-team",
      "name": "IBM-i Team",
      "applicationId": "app-ibmi-core",
      "applicationName": "IBM-i Core",
      "snowGroupId": "snow-ibmi-ops",
      "snowGroupName": "IBM-i Operations",
      "profileId": "ibm-i"
    }
  ]
}
```

For `platform:*` users: all active rows. Otherwise: the intersection of
their scopes against `PLATFORM_WORKSPACE`. The list is sorted by name.

`GET /api/v1/auth/workspaces/by-key/{workspaceKey}`

Resolves a key to its full row. `404` if the key is unknown or the user
is unauthorized for it. Used by the Vue Router on cold reload.

### FR-MTF-10: Workspace key rename and redirect

`PLATFORM_WORKSPACE` carries a `workspace_key` column (already exists,
unique). A new `platform_workspace_key_alias` table records historical
keys:

```sql
CREATE TABLE platform_workspace_key_alias (
  id              BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  workspace_id    VARCHAR(64) NOT NULL,
  former_key      VARCHAR(128) NOT NULL,
  expires_at      TIMESTAMP WITH TIME ZONE NOT NULL,
  CONSTRAINT uq_alias UNIQUE (former_key)
);
```

`expires_at` defaults to 30 days from the rename event. `GET
/api/v1/auth/workspaces/by-key/{key}` checks the live `workspace_key`
first, then the alias table; an active alias hit returns
`{ "redirectTo": "<new-key>" }` with HTTP `301`. After expiry, `404`.

### FR-MTF-11: Frontend `apiClient` URL rewriting

A single axios interceptor in
`frontend/src/shared/api/apiClient.ts`:

```ts
client.interceptors.request.use((config) => {
  const url = config.url ?? '';
  if (isAllowlisted(url)) return config;             // /auth/*, /health, etc.
  const { workspaceId } = useWorkspaceStore().context;
  if (!workspaceId) {
    throw new ApiClientError('NO_WORKSPACE_SELECTED');
  }
  config.url = `/workspaces/${workspaceId}${url.startsWith('/') ? url : `/${url}`}`;
  return config;
});
```

The base URL prefix `/api/v1` is configured separately so the
interceptor's responsibility is the workspace path only. The shell
catches `NO_WORKSPACE_SELECTED` and renders an
"Awaiting workspace selection" overlay rather than a domain error.

### FR-MTF-12: Vue Router rebase

All domain routes are nested under a `:workspaceKey` parent route in
`frontend/src/router/index.ts`:

```ts
{
  path: '/:workspaceKey',
  component: WorkspaceShell,
  beforeEnter: resolveWorkspaceKey,        // sets workspaceStore.context
  children: [
    { path: '', redirect: 'dashboard' },
    { path: 'dashboard', component: DashboardView },
    { path: 'requirements', component: RequirementListView },
    { path: 'requirements/:requirementId', component: RequirementDetailView },
    { path: 'incidents', component: IncidentListView },
    /* ... 13 features ... */
  ]
}
```

`resolveWorkspaceKey` performs:

1. Look up the key in the cached `workspaces` map; if hit → set
   `workspaceStore.context` and pass.
2. Else call `GET /api/v1/auth/workspaces/by-key/{key}`.
3. On `301` → router replace with the new key (preserves child path).
4. On `404` → redirect to `/no-access?attempted={key}`.
5. On unauthenticated → redirect to `/login?next=<full-url>`.

Top-level routes outside the workspace prefix: `/login`, `/no-access`,
`/demo/...` (FR-MTF-15), `/platform/...` (admin-only).

### FR-MTF-13: Caching

`WorkspaceContextResolver` maintains:

- `workspaceById`: `Map<String, PlatformWorkspaceEntity>`, 60 s TTL
- `workspaceByKey`: `Map<String, PlatformWorkspaceEntity>`, 60 s TTL
- `projectToWorkspace`: `Map<String, String>`, 60 s TTL (used by the
  `project:{X}` scope check)

Caches are invalidated on `PLATFORM_WORKSPACE` mutation, `project`
create / move, and `platform_workspace_key_alias` insert. Local H2 has
no size limit; production uses a 10k LRU cap.

### FR-MTF-14: Webhook adapters

`JenkinsWebhookController` and `GithubWebhookController` are excluded
from `WorkspaceContextInterceptor` via path-based exclusion
(`/api/v1/integration/webhooks/**`). Each handler:

1. Verifies the webhook signature.
2. Resolves `workspaceId` from the payload reference inside
   `runWithCrossWorkspaceAccess("webhook.<source>.ingest", ...)`.
3. After resolution, sets `WorkspaceContextHolder` to the resolved
   workspace for the remainder of the handler so domain writes flow
   through the normal filter path.
4. Clears the holder in finally.

Unknown source → `404` and emit `webhook.unknown_source` audit event
without writing domain data.

### FR-MTF-15: Demo / guest path

`AuthProperties.demoMode` is a Spring property
`sdlctower.platform.demo-mode` (default `false` in production, `true` in
`application-local.yml`). It is exposed to the frontend via
`GET /api/v1/auth/me`.

Synthetic workspace id `demo`:

- Reserved server-side; never written to `PLATFORM_WORKSPACE`
- Backend resolves it to the row pointed to by
  `workspace_context.workspace_id`
- Allowed only when `demoMode == true` AND `currentUser.mode == "guest"`
- Frontend exposes UI route `/demo/...`; the workspace switcher is
  hidden under this route

When `currentUser.mode == "staff"`, any access to `/api/v1/workspaces/demo/...`
or `/demo/...` is rejected with redirect to the user's default workspace.

### FR-MTF-16: Workspace switcher (frontend)

New component: `frontend/src/shell/components/WorkspaceSwitcher.vue`.

Behavior:

- Mounts in the top bar after authentication
- Initial population: `GET /api/v1/auth/workspaces`
- Initial selection: `localStorage.workspace.{staffId}` if present and
  still authorized, else first item
- On select:
  1. Call `POST /api/v1/auth/workspace`
  2. On success: `workspaceStore.setActive(...)`; update
     `localStorage.workspace.{staffId}`; navigate to the same feature
     route under the new key (`router.push('/${newKey}/${currentFeature}')`)
  3. On `403`: toast and revert dropdown
- Hidden when route is under `/demo/*`

### FR-MTF-17: 16-controller migration

Every domain controller's `@RequestMapping` gains the workspace prefix:

```java
// before
@RequestMapping(ApiConstants.REQUIREMENTS)            // /api/v1/requirements

// after
@RequestMapping(ApiConstants.WORKSPACE_REQUIREMENTS)  // /api/v1/workspaces/{workspaceId}/requirements
```

`ApiConstants` adds workspace-scoped path constants. Controllers continue
to use existing `@PathVariable` for resource ids. The `workspaceId` path
variable is consumed by the interceptor; controllers do not need to
declare it.

The 16 controllers in scope:

- `RequirementController`, `KnowledgeGraphController` (requirement)
- `DashboardController`
- `IncidentController`
- `TeamSpaceController`
- `ProjectSpaceController`
- `ProjectManagementController`, `PlanController`,
  `PortfolioController` (project mgmt)
- `DesignManagementController`
- `CodeBuildController`
- `TestingManagementController`
- `AiCenterController`
- `DeploymentController`
- `ReportCenterController` (workspace-scoped reports;
  `/reports/fleet` is separate)

`WorkspaceContextController` itself moves to
`/api/v1/workspaces/{workspaceId}/context`, returning the active context
for the current workspace.

## Error Contract

| Code | HTTP | Meaning |
|---|---|---|
| `AUTH_REQUIRED` | 401 | No session cookie |
| `WORKSPACE_NOT_FOUND` | 404 | Path workspaceId not in `PLATFORM_WORKSPACE` |
| `WORKSPACE_KEY_NOT_FOUND` | 404 | UI key resolution failed |
| `WORKSPACE_SCOPE_REQUIRED` | 403 | User has no scope covering claimed workspace |
| `PROJECT_NOT_IN_WORKSPACE` | 400 | `?projectId=` points outside current workspace |
| `MISSING_WORKSPACE_CONTEXT` | 500 | Server-side bug: scoped repo queried without context |
| `NO_WORKSPACE_SELECTED` | client-side | Frontend tried to call API before workspace pick |

All backend errors use the existing `ApiResponse` envelope:

```json
{ "success": false, "data": null, "error": { "code": "...", "message": "...", "details": {} } }
```

## Migration Rules

| Migration | Purpose |
|---|---|
| `V97__add_workspace_id_to_control_plane.sql` | Add nullable `workspace_id` to the 7 control-plane tables |
| `V98__backfill_control_plane_workspace_id.sql` | Backfill from `requirement.workspace_id`; verify zero null rows |
| `V99__enforce_control_plane_workspace_id.sql` | `NOT NULL` constraint + composite indexes `(workspace_id, primary_temporal)` on each |
| `V100__add_workspace_id_indexes.sql` | Composite indexes on existing `workspace_id`-bearing tables that lack them |
| `V101__create_platform_workspace_key_alias.sql` | Workspace key alias table |
| `V102__seed_ibm_i_workspace.sql` | Seed `ws-ibm-i-team`, `app-ibmi-core`, `snow-ibmi-ops`, staff `43929999`, profile binding, sample requirement `REQ-IBMI-0001` with one Jira-style source ref |
| `V103__update_seed_admin_scopes.sql` | Ensure seed admin user has `WORKSPACE_MEMBER:workspace:ws-default-001` and `WORKSPACE_MEMBER:workspace:ws-ibm-i-team` for the demo |

`workspace_context` single row keeps `workspace_id = ws-default-001` and
is consulted only on the demo / guest fallback path.

## Test Contract

| Test | Class | Asserts |
|---|---|---|
| Resolution branches | `WorkspaceContextResolutionTest` | Each branch of FR-MTF-02 |
| Path enforcement | `PathEnforcementTest` | Non-prefixed `/api/v1/<resource>` returns 404; allowlist still works |
| Scope authorization | `ScopeAuthorizationTest` | Each scope type in FR-MTF-03 |
| Repository isolation | `RepositoryAutoFilterTest` | Two seeded workspaces; `RequirementService.list()` returns only current-workspace rows; representative entity from each slice covered |
| Filter coverage | `AllWorkspaceScopedEntitiesAreFiltered` | Reflection: every `@Entity` with `workspace_id` carries `@Filter` |
| Control-plane backfill | `ControlPlaneBackfillTest` | Migration produces zero null `workspace_id` across 7 tables |
| Webhook resolution | `JenkinsWebhookWorkspaceResolutionTest`, `GithubWebhookWorkspaceResolutionTest` | Two workspaces' webhooks land rows in distinct workspaces |
| Auth API | `AuthWorkspaceControllerTest` | `POST /auth/workspace` happy path + 403; `GET /auth/workspaces` filtering by scope; `by-key` resolution and alias redirect |
| Audit | `WorkspaceSwitchAuditTest` | Switch produces `permission_change / workspace.switch` row |
| Demo | `DemoWorkspaceTest` | Guest mode resolves `demo` to `workspace_context`; staff users blocked |
| Frontend client | `apiClient.spec.ts` | Path prefix injected on every domain call; throws when missing; allowlist passes through |
| Frontend router | `router-workspace-resolve.spec.ts` | Cold reload of `/{key}/...` resolves correctly; alias 301 follows; unauthorized redirect |
| E2E IBM-i | `ibm-i-day1.spec.ts` (Playwright) | Login as `43929999`, land on `/ibm-i-team/dashboard`, navigate to requirement `REQ-IBMI-0001`, click `Prepare Prompt`, verify CLI prompt mentions IBM-i skill, confirm AgentRun row carries `workspace_id = ws-ibm-i-team` |

## Performance Rules

- Resolution path: at most one `PLATFORM_WORKSPACE` lookup per request,
  cached for 60 s
- `project:{X}` scope: at most one `project` lookup per request, cached
- Hibernate filter relies on a `(workspace_id, ...)` composite index on
  every filtered table; missing indexes flagged in V100 verification
- p95 regression budget: ≤10 ms vs the pre-change baseline on local H2

## Open Questions

| ID | Question | Disposition |
|---|---|---|
| Q1 | Should we expose `workspaceKey` in API responses for cross-system linking? | Yes, both id and key in every workspace-related DTO. Already in `GET /auth/workspaces`. |
| Q2 | Does the IBM-i sample requirement need real GitHub repo binding for Day 1? | Out of scope — seeded `SourceReference` is a Jira link; GitHub doc indexing happens after the user runs the CLI for real. |
| Q3 | Should `/demo/*` be its own Vue Router branch, separate from `:workspaceKey`? | Yes. Keeps the guard logic isolated and prevents accidental scope checks. |
| Q4 | How does this interact with the future `team-membership` slice? | Membership becomes an additional dimension on `PLATFORM_ROLE_ASSIGNMENT` and feeds the same scope-resolution logic. No contract change to FR-MTF-03. |

## Out-of-Scope Reminders

These belong to follow-up slices and must NOT be added here even when
convenient:

- `team` and `team_member` tables (`team-membership` slice)
- Invite tokens, self-signup
- `tenant_id` activation (multi-tenant org layer)
- Cross-workspace document sharing
- New SDD profiles beyond IBM-i + standard Java
- Modifying the `AgentRun` lifecycle, next-action engine, or
  `PipelineProfile` model
