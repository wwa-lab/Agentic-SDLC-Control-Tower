# Multi-Tenancy Foundation Requirements

## Purpose

Rebase the existing 13 SDLC slices onto a multi-tenant foundation so multiple
teams can run independent SDD workflows in parallel from Day 1. The slice
covers every domain controller, every workspace-scoped entity, and every
frontend feature simultaneously. Partial migration (some slices multi-tenant,
others single-tenant) is explicitly out of scope: a mixed-tenant test surface
doubles QA burden and breaks the showcase narrative.

The slice name is "foundation" because the work establishes the platform
contract; in implementation terms it is a baseline rebase, not an additive
feature.

## Day 1 Success Goal

A real IBM-i team can be onboarded to a dedicated workspace and run their
existing Jira-driven workflow end-to-end through the IBM-i SDD profile —
Requirement Normalizer → Functional Spec → Technical Design → Program Spec →
File Spec → Code Generator → DDS / UT Plan / Test Scaffold — parallel to the
existing Payment Gateway Pro workspace, with zero cross-team data leakage and
no operator manually editing requests.

This is the concrete acceptance demo for the slice and the basis for the
seed-workspace strategy in REQ-MTF-15.

## Product Context

The Control Tower web UI is a showcase / onboarding / change-management
surface, **not** an AI execution surface. AI work runs in CLI agents
(Codex / Gemini / Claude Code) per CLAUDE.md Principle 6.
`Requirement Management` (`frontend/src/features/requirement/`) is the
canonical reference: the UI prepares an `AgentRun` manifest and a copyable
CLI prompt, the user runs the CLI in a terminal, the CLI opens a GitHub PR,
the user returns to confirm the merge, and the platform re-indexes
GitHub-truth documents and updates freshness.

This slice is **not** about gating AI execution. It scopes every existing
control-plane primitive — AgentRun, SourceReference, SddDocumentIndex,
DocumentReview, AgentStageEvent, ArtifactLink, FreshnessStatus,
PipelineProfile binding — to the correct workspace so that:

- A team lead new to AI workflows can land in a demo workspace and learn the
  pattern with zero risk to other teams' data
- A real team (IBM-i) can be given a dedicated workspace with their own
  profile, sources, documents, and runs
- Cross-team showcase comparisons happen by switching workspace, not by
  inventing custom AI controls

## Profile

`platform-foundation-sdd` — backend-first, no business UI flows. Stories use
technical actors. Engineering review in GitHub.

## Source

- Audit finding: 16 of 16 domain controllers do not call `requireUser` and do
  not filter by workspace scope
- Audit finding: `WorkspaceContextService.getCurrentWorkspaceContext` returns
  `repository.findTopByOrderByIdAsc()` for every caller (single-tenant)
- Audit finding: 22 of 84 entities carry `workspace_id`; control-plane
  entities (AgentRun, SourceReference, SddDocumentIndex, DocumentReview,
  AgentStageEvent, QualityGateRun, ArtifactLink) do not, and currently scope
  transitively through `requirement.workspace_id`
- Existing IBM-i profile: `frontend/src/features/requirement/profiles/ibmIProfile.ts`
  defines the 13-skill chain end to end; backend `RequirementService` already
  routes `ibm-i-workflow-orchestrator`. The profile is implemented but never
  bound to a real workspace.
- CLAUDE.md "Platform Design Principles" §1, §3, §6, §9, §10
- Existing schema: `PLATFORM_USER`, `PLATFORM_ROLE_ASSIGNMENT` (V90),
  `PLATFORM_WORKSPACE` / `PLATFORM_APPLICATION` / `PLATFORM_SNOW_GROUP` (V86),
  `workspace_context.workspace_id` (V95)

## Scope

### In Scope

1. All 16 domain controllers gated on session + workspace scope
2. Every workspace-scoped entity (existing + new control-plane additions)
   constrained by current workspace through a single auto-filter mechanism
3. Path-based URL design: API at `/api/v1/workspaces/{workspaceId}/…`,
   UI at `/{workspaceKey}/…`, with stable ID server-side and human-friendly
   key client-side
4. `workspace_id` column added to control-plane entities that today scope
   transitively: `requirement_agent_run`,
   `requirement_control_plane_source`, `requirement_control_plane_document`,
   `requirement_document_review`, `requirement_agent_stage_event`,
   `requirement_document_quality_gate_run`, `artifact_link`
5. Frontend Vue Router rebased so every domain route is nested under
   `:workspaceKey`
6. Frontend apiClient that translates UI key-based routes to ID-based
   API URLs
7. `WorkspaceContextHolder` (ThreadLocal) + Hibernate `@Filter` on every
   filtered entity
8. Workspace switcher in the shell, driven by `currentUser.scopes`
9. Demo / guest mode preserved as a first-class onboarding surface
10. IBM-i seed workspace `ws-ibm-i-team` with the full IBM-i profile linked
    and a sample requirement seeded into the first stage so the Day 1 demo
    runs without manual setup
11. Cross-workspace allowlist: `/api/v1/auth/*`, `/api/v1/health`,
    `/api/v1/integration/webhooks/*`, `/api/v1/platform/*`,
    `/api/v1/reports/fleet`
12. Audit events for workspace switch and access denial
13. Migration to add `workspace_id` to the 7 control-plane tables, backfill
    from `requirement.workspace_id`, and add composite indexes

### Out of Scope

- Team and Membership entities — separate `team-membership` slice
- Invitations, self-signup, password flows
- Tenant / Organization layer above workspace (deferred until ≥2 customers)
- Cross-workspace sharing of records
- Per-workspace quotas, limits, billing
- Data residency / regional partitioning
- Refactoring the IBM-i profile chain or the Payment Gateway profile
- Adding new SDD profiles beyond the two seeded
- Any change to the `AgentRun` lifecycle, the next-action engine, the
  `PipelineProfile` model, or the GitHub-as-truth indexing — see Product
  Context. This slice scopes those primitives to a workspace; it does not
  modify them.
- Adding "Generate" / "Run AI" / "Ask AI" buttons to any UI surface. AI
  execution stays in CLI agents.
- Backwards compatibility with the old non-prefixed API URLs. Old URLs
  return `404` after rebase; clients are versioned together.

## Requirements

### REQ-MTF-01: Path-based workspace identity

Every authenticated request to a domain endpoint must address a workspace
through the URL path:

- API canonical form: `/api/v1/workspaces/{workspaceId}/<resource>...`
- UI canonical form: `/{workspaceKey}/<feature>...`

`workspaceId` is the stable database key (e.g., `ws-default-001`).
`workspaceKey` is the human-friendly slug stored on `PLATFORM_WORKSPACE`
(e.g., `payment-gateway-pro`). Workspace keys must be unique, lower-kebab,
and stable for the lifetime of the workspace; renames produce a one-time
`410 Gone` redirect (REQ-MTF-07).

The frontend resolves `workspaceKey` to `workspaceId` once per session via
`GET /api/v1/auth/workspaces` and caches the mapping.

### REQ-MTF-02: Allowlist of cross-workspace endpoints

The following paths do **not** carry a workspace prefix and apply their own
authentication / authorization rules:

| Path | Why exempt | Auth required |
|---|---|---|
| `/api/v1/auth/**` | Login / session / workspace listing | Public + post-login |
| `/api/v1/health` | Liveness probe | None |
| `/api/v1/integration/webhooks/**` | Workspace resolved from payload | Webhook signature |
| `/api/v1/platform/**` | Platform Center catalog management | `PLATFORM_ADMIN` |
| `/api/v1/reports/fleet/**` | Cross-workspace fleet reports | `AUDITOR` or `PLATFORM_ADMIN` |

All other `/api/v1/**` paths are required to be under
`/api/v1/workspaces/{workspaceId}/…` and to satisfy REQ-MTF-03.

### REQ-MTF-03: Workspace authorization is mandatory

A request authenticated as user `U` with claimed workspace `W` is authorized
only if `U.scopes` contains at least one scope that covers `W`:

- `platform:*` → covers any workspace
- `application:{appId}` → covers any workspace whose `application_id = appId`
- `snow_group:{groupId}` → covers any workspace whose `snow_group_id = groupId`
- `workspace:{workspaceId}` → covers exactly that workspace
- `project:{projectId}` → covers any workspace that owns that project

Unauthorized requests return `403 WORKSPACE_SCOPE_REQUIRED`. Unknown
`workspaceId` returns `404 WORKSPACE_NOT_FOUND`. Both are logged with
`staffId`, `claimedWorkspaceId`, and `reason`.

### REQ-MTF-04: Domain repositories must not require manual workspace filtering

Any JPA entity with a `workspace_id` column must be readable through a
repository contract that automatically constrains queries to the current
`WorkspaceContext.workspaceId`. New domain code must not be allowed to query
workspace-scoped tables without going through this contract.

A regression test must scan all `@Entity` classes and fail if any class with
a `workspace_id` column lacks the auto-filter wiring.

### REQ-MTF-05: Webhooks resolve workspace from payload

Inbound webhooks (Jenkins, GitHub) live under `/api/v1/integration/webhooks/**`,
authenticate via webhook signature, and derive workspace identity from the
payload reference (GitHub `repository.id` → `repos.workspace_id`; Jenkins
`instance_id` → `jenkins_instance.workspace_id`). They run outside the
session interceptor and use the explicit cross-workspace escape hatch from
REQ-MTF-11 with a stable reason code.

### REQ-MTF-06: Frontend knows the user's workspaces

After login, the frontend must call `GET /api/v1/auth/workspaces` and
render a workspace switcher in the shell whose entries derive from
`currentUser.scopes`. The current selection persists across page reloads
(localStorage keyed by `staffId`) and is validated against the latest
backend response on every fresh session.

### REQ-MTF-07: UI URLs are key-based and shareable

UI canonical form is `/{workspaceKey}/<feature>...`. Examples:

- `/payment-gateway-pro/requirements`
- `/payment-gateway-pro/requirements/REQ-0001`
- `/ibm-i-team/dashboard`

Behavior:

- A signed-in user opening a URL whose key resolves to a workspace they
  cannot access → redirect to `/no-access?attempted={key}` with a toast
- Anonymous user opening any `/{key}/...` → redirect to `/login?next=…`
- Workspace key renamed → previous key returns a one-time `301` to the new
  key for 30 days; after that `404`
- Demo / guest mode uses the synthetic key `demo` (REQ-MTF-09)

### REQ-MTF-08: Workspace switch updates the URL

Switching workspaces in the switcher must:

1. Call `POST /api/v1/auth/workspace` with the new `workspaceId`
2. Update Vue Router to the same feature route under the new
   `workspaceKey` (e.g., `/payment-gateway-pro/requirements` →
   `/ibm-i-team/requirements`)
3. Persist the selection in localStorage and the `sdlc_workspace` cookie
4. Trigger a fresh data fetch for the current view

The browser back button restores the previous workspace correctly.

### REQ-MTF-09: Demo mode is a first-class onboarding surface

The single-row `workspace_context` table and the `demoMode` flag exist
**primarily** to make the platform legible to a team lead who has not yet
been provisioned. When `currentUser.mode = "guest"` AND the deployment
exposes `demoMode = true`:

- API canonical form becomes `/api/v1/workspaces/demo/…` (synthetic id)
- UI canonical form becomes `/demo/…`
- Backend resolves the demo workspace from `workspace_context.workspace_id`
- The workspace switcher is hidden
- The user lands in a curated demo workspace that exercises the canonical
  CLI handoff pattern end-to-end

This path MUST NOT apply to authenticated staff users.

### REQ-MTF-10: Workspace context is observable

The runtime context (`workspaceId`, `workspaceKey`, `applicationId`,
`snowGroupId`, `staffId`) must be exposed to logs, audit writers, and
exception reporters via a single, process-wide accessor. Every audit row
written by this slice must include the active `scopeType` and `scopeId`.

### REQ-MTF-11: Cross-workspace access is explicit and audited

Code paths that legitimately read or write across workspaces (cross-team
metrics in Report Center, platform admin operations, Flyway-driven seed
loaders, webhook adapters before workspace resolution) must use a named
escape hatch with a stable reason code. Calls without context to a
workspace-scoped repository must throw `MISSING_WORKSPACE_CONTEXT` rather
than silently return all rows.

### REQ-MTF-12: Control-plane entities carry `workspace_id`

The following entities currently scope transitively through
`requirement.workspace_id` and must be denormalized to carry their own
`workspace_id` column:

- `requirement_agent_run`
- `requirement_control_plane_source`
- `requirement_control_plane_document`
- `requirement_document_review`
- `requirement_agent_stage_event`
- `requirement_document_quality_gate_run`
- `artifact_link`

A backfill migration must populate `workspace_id` from the parent
requirement before the auto-filter is enabled. The application code that
inserts these rows must set `workspace_id` from `WorkspaceContextHolder`,
not via re-derivation.

### REQ-MTF-13: Frontend HTTP client always sends workspace identity

Every request from the frontend `apiClient` to a domain endpoint must:

- Read the active `workspaceId` from `workspaceStore.context`
- Rewrite the request URL to inject the path prefix
  (`/requirements/{id}` → `/workspaces/{workspaceId}/requirements/{id}`)
- Throw client-side if no workspace is selected (rather than reach the
  server with a malformed URL)

The auth allowlist (REQ-MTF-02) skips the rewrite.

### REQ-MTF-14: Project filter is a query parameter within workspace

Projects are an in-workspace dimension, not a URL nesting level. Any
endpoint that exposes a project filter does so via a `?projectId={pid}`
query parameter on a workspace-prefixed URL, e.g.,
`GET /api/v1/workspaces/ws-ibm-i-team/requirements?projectId=proj-ibmi-001`.

### REQ-MTF-15: IBM-i seed workspace ready for Day 1 demo

A migration must seed:

- `PLATFORM_WORKSPACE` row `ws-ibm-i-team` with key `ibm-i-team` and a
  matching `PLATFORM_APPLICATION` and `PLATFORM_SNOW_GROUP`
- A staff user `43929999` with `WORKSPACE_VIEWER` scope on `ws-ibm-i-team`
  and `WORKSPACE_MEMBER` for the existing seed admin
- `pipeline_profile` binding for `ws-ibm-i-team` → IBM-i profile
- One sample requirement (`REQ-IBMI-0001`) in the IBM-i workspace at the
  first chain stage (Requirement Normalizer), with one Jira-style
  `SourceReference` so the CLI handoff demo runs without operator setup
- Default `ws-default-001` continues to bind to the standard Java profile

After the migration, logging in as either user shows their respective
workspace populated and ready to demo.

## Success Criteria

The foundation is considered ready when all of the following are true:

1. Two seeded workspaces (`ws-default-001` and `ws-ibm-i-team`) hold
   distinct requirements, dashboard, and incident records, and each binds
   to its own `PipelineProfile`
2. User A holding scope `workspace:ws-default-001` only, calling
   `GET /api/v1/workspaces/ws-ibm-i-team/requirements`, is rejected
   `403 WORKSPACE_SCOPE_REQUIRED`
3. User A switching to a workspace they hold scope on re-routes the URL,
   re-fetches data, and shows only that workspace's content across all 13
   slices
4. Removing the path prefix from any non-allowlisted URL yields `404`
5. A new repository on a `workspace_id`-bearing table that does not declare
   the auto-filter fails the regression test in REQ-MTF-04
6. Day 1 demo: logging in as `43929999` and stepping through the IBM-i
   chain on `REQ-IBMI-0001` produces an `AgentRun` with the correct
   workspace, sources, and CLI prompt; nothing leaks into `ws-default-001`
7. Existing local-dev guest mode renders against the demo workspace
   without the user touching any URL or header

## Non-Functional Requirements

- **Performance**: The resolution path adds at most one
  `PLATFORM_WORKSPACE` lookup per request, with a 60-second in-memory
  cache. p95 latency regression budget vs current code is ≤10 ms on local
  H2 seed data.
- **Backwards compatibility**: All 13 existing slices keep functioning
  against their seeded workspace after migration; no data loss; no manual
  re-import.
- **Observability**: Every rejected request logs `staffId`,
  `claimedWorkspaceId`, `reason`. Successful requests include
  `workspaceId` in MDC for log correlation.
- **Testing**: Repository auto-filter regression test must cover
  representative entities from each slice; webhook resolution must be
  covered by an integration test asserting cross-workspace payloads land
  in distinct workspaces.
- **Migration safety**: The control-plane backfill (REQ-MTF-12) must be
  idempotent and runnable on a populated H2 / Oracle without downtime.

## Dependencies

- `platform-access-audit-persistence` — provides `PLATFORM_USER`,
  `PLATFORM_ROLE_ASSIGNMENT`, `AuditWriter`
- `platform-center-data-model` — defines the workspace catalog
  (`PLATFORM_WORKSPACE`, `PLATFORM_APPLICATION`, `PLATFORM_SNOW_GROUP`)
- `shared-app-shell` — owns the top bar where the workspace switcher renders
- IBM-i SDD profile — `ibmIProfile.ts` and `RequirementService`'s
  `ibm-i-workflow-orchestrator` route already exist
