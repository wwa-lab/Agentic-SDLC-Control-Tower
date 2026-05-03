# Multi-Tenancy Foundation Tasks

## Milestone Goal

Rebase all 13 SDLC slices onto a multi-tenant foundation with
path-based URLs. The Day 1 acceptance demo is: a real IBM-i team can
log in to `/ibm-i-team/...`, run their existing Jira-driven workflow
end-to-end through the IBM-i SDD profile, and see no data from any
other workspace.

The whole slice ships as one consistent rebase (no partial multi-tenancy)
because mixed-tenant test surfaces double QA burden during intranet
verification.

---

## Phase MTF-0: Document Baseline

- [x] Add focused requirements document.
- [x] Add focused user stories.
- [x] Add focused implementation spec.
- [x] Add architecture, data-flow, data-model documents.
- [x] Add design and API implementation guide.
- [ ] Cross-link this task list back into each domain slice's task doc
  (one bullet per slice noting "controller path migrated by MTF").

---

## Phase MTF-1: Schema Migrations

- [ ] `V97__add_workspace_id_to_control_plane.sql` — add nullable
  `workspace_id` column to 7 control-plane tables.
- [ ] `V98__backfill_control_plane_workspace_id.sql` — backfill from
  parent `requirement.workspace_id`.
- [ ] `V99__enforce_control_plane_workspace_id.sql` — `NOT NULL` +
  composite indexes.
- [ ] `V100__add_workspace_id_indexes.sql` — composite indexes on
  existing scoped tables that lack one.
- [ ] `V101__create_platform_workspace_key_alias.sql` — alias table.
- [ ] `V102__seed_ibm_i_workspace.sql` — IBM-i workspace,
  application, snow group, user, profile binding, sample requirement,
  Jira source ref.
- [ ] `V103__update_seed_admin_scopes.sql` — seed admin gets
  `WORKSPACE_MEMBER` on both seeded workspaces.
- [ ] Local H2 verification: every backfill query reports zero null
  rows after V98.
- [ ] Local H2 verification: V102 produces a queryable
  `REQ-IBMI-0001` with one source reference, profile bound to `ibm-i`.

---

## Phase MTF-2: Backend Foundation

- [ ] Add `WorkspaceContext` record (workspaceId, workspaceKey,
  workspaceName, applicationId, snowGroupId, profileId, demoMode).
- [ ] Add `WorkspaceContextHolder` (ThreadLocal + cross-flag).
- [ ] Add `WorkspaceCrossAccessReason` enum with the 6 reason codes.
- [ ] Add `MissingWorkspaceContextException`,
  `WorkspaceScopeException`, `WorkspaceNotFoundException`.
- [ ] Add `PlatformWorkspaceEntity` JPA mapping for V86 table.
- [ ] Add `PlatformWorkspaceRepository` with `findById`,
  `findByWorkspaceKey`, `findByApplicationId`, `findBySnowGroupId`.
- [ ] Add `WorkspaceContextResolver` with 60s caches: workspaceById,
  workspaceByKey, projectToWorkspace.
- [ ] Add `WorkspaceContextInterceptor` (auth + workspace + scope) per
  FR-MTF-02.
- [ ] Register interceptor in `WebMvcConfig` with allowlist exclusions.
- [ ] Add `WorkspaceFilter` (`@FilterDef("workspace_filter")`).
- [ ] Add `WorkspaceFilterAspect` around `@Transactional`.
- [ ] Add `@Filter("workspace_filter")` to every workspace-scoped
  entity (~22 existing + 7 control-plane).
- [ ] Add `WorkspacePrePersistListener` canary that throws if a row is
  inserted with null `workspace_id` while the holder is set.
- [ ] Add reflection regression test
  `AllWorkspaceScopedEntitiesAreFiltered`.
- [ ] Configure `TaskDecorator` so `@Async` propagates the holder.

---

## Phase MTF-3: Auth API Extensions

- [ ] Add `AuthWorkspaceController` with `POST /auth/workspace`,
  `GET /auth/workspaces`, `GET /auth/workspaces/by-key/{key}`.
- [ ] Add `SwitchWorkspaceRequest`, `WorkspaceListItemDto`.
- [ ] Extend `AuthService` with `switchWorkspace(staffId, workspaceId)`
  that writes `permission_change / workspace.switch` audit row and sets
  `sdlc_workspace` cookie.
- [ ] Extend `AuthProperties` with `demoMode` (default false).
- [ ] Extend `CurrentUserDto` with `demoMode`.
- [ ] Add `WorkspaceKeyAliasService` and repository; alias lookup
  during `by-key`.
- [ ] Add unit + controller tests (`AuthWorkspaceControllerTest`).

---

## Phase MTF-4: Domain Controller Path Migration

- [ ] Add path constants in `ApiConstants` for all 16 controllers.
- [ ] Update `RequirementController` `@RequestMapping` to
  `/api/v1/workspaces/{workspaceId}/requirements`.
- [ ] Update `KnowledgeGraphController`,
  `DashboardController`, `IncidentController`,
  `TeamSpaceController`, `ProjectSpaceController`,
  `ProjectManagementController`, `PlanController`,
  `PortfolioController`, `DesignManagementController`,
  `CodeBuildController`, `TestingManagementController`,
  `AiCenterController`, `DeploymentController`,
  `ReportCenterController` (workspace-scoped only).
- [ ] Move `WorkspaceContextController` to
  `/api/v1/workspaces/{workspaceId}/context`.
- [ ] Verify webhook controllers
  (`JenkinsWebhookController`, `GithubWebhookController`) stay at
  `/api/v1/integration/webhooks/...`.
- [ ] Verify platform controllers stay at `/api/v1/platform/...`.
- [ ] Add `/api/v1/reports/fleet/...` for cross-workspace reports;
  separate from workspace-scoped report endpoints.
- [ ] Update existing controller tests to use the new paths.

---

## Phase MTF-5: Webhook Workspace Resolution

- [ ] Update `JenkinsWebhookController` to resolve `workspace_id`
  from `jenkins_instance` inside
  `runWithCrossWorkspaceAccess(WEBHOOK_JENKINS_INGEST, ...)`.
- [ ] Update `GithubWebhookController` to resolve `workspace_id`
  from `repos` inside
  `runWithCrossWorkspaceAccess(WEBHOOK_GITHUB_INGEST, ...)`.
- [ ] After resolution, set the holder for the remainder of the
  handler so domain writes flow through the auto-filter.
- [ ] Unknown source → `404` + `webhook.unknown_source` audit.
- [ ] Tests: `JenkinsWebhookWorkspaceResolutionTest`,
  `GithubWebhookWorkspaceResolutionTest`.

---

## Phase MTF-6: Frontend `apiClient` and Stores

- [ ] Add request interceptor in `apiClient.ts` per FR-MTF-11.
- [ ] Add `ApiClientError` with code `NO_WORKSPACE_SELECTED`.
- [ ] Update `workspaceApi.ts` with `listMyWorkspaces`,
  `switchWorkspace`, `resolveByKey`.
- [ ] Update `authApi.ts` to read `demoMode` from `/auth/me`.
- [ ] Extend `Workspace`, `WorkspaceContext`, `CurrentUser` types in
  `shell.ts`.
- [ ] Extend `workspaceStore.ts` with `workspaces`, `setActive`,
  `switchTo`, `load`.
- [ ] Extend `sessionStore.ts` to expose `demoMode`.
- [ ] Frontend test `apiClient.spec.ts` covering rewrite + allowlist +
  empty context throws.

---

## Phase MTF-7: Frontend Vue Router Rebase

- [ ] Add `:workspaceKey` parent route in `router/index.ts` with
  `beforeEnter: resolveWorkspaceKey`.
- [ ] Move all 13 feature routes under the parent.
- [ ] Add top-level routes for `/login`, `/no-access`, `/demo/*`,
  `/platform/*`.
- [ ] Add `resolveWorkspaceKey` guard in `router/guards/`.
- [ ] Add `requirePlatformAdmin` guard for `/platform/*`.
- [ ] Add root `/` redirect using `workspaceStore.workspaces[0]`.
- [ ] Update existing internal links across the codebase to use
  `:workspaceKey` (`router-link :to`).
- [ ] Frontend test `resolveWorkspaceKey.spec.ts` covering hit, cold,
  alias, 403, 404, 401.

---

## Phase MTF-8: Workspace Switcher UI

- [ ] Add `WorkspaceSwitcher.vue` in `shell/components/`.
- [ ] Wire it into the existing top bar (`shell/components/...`).
- [ ] Render dropdown of `workspaceStore.workspaces`, current via
  `route.params.workspaceKey`.
- [ ] On select: `switchTo(...)` → `router.push('/{newKey}/...')`.
- [ ] Persist selection in `localStorage.workspace.{staffId}`.
- [ ] Hide under `/demo/*`.
- [ ] Hide when there is exactly one workspace (degenerate; static
  label instead).
- [ ] Frontend test `WorkspaceSwitcher.spec.ts`.

---

## Phase MTF-9: Demo / Guest Path

- [ ] Implement `workspaceId == "demo"` resolution in the
  interceptor (FR-MTF-15).
- [ ] Frontend redirects guest mode to `/demo/dashboard`.
- [ ] Frontend hides switcher under `/demo/*`.
- [ ] Demo-mode banner stays as-is (existing component).
- [ ] Test `DemoWorkspaceTest`: guest with `demoMode=true` works;
  staff is rejected.

---

## Phase MTF-10: IBM-i Day 1 Acceptance

- [ ] Verify migration V102 lands the IBM-i workspace, application,
  snow group, user, profile binding, sample requirement, Jira source
  ref.
- [ ] Log in as `43929999`; land on `/ibm-i-team/dashboard` with seed
  data.
- [ ] Navigate to `/ibm-i-team/requirements/REQ-IBMI-0001`.
- [ ] Click `Prepare Prompt`; verify the CLI prompt mentions
  `ibm-i-functional-spec` and `REQ-IBMI-0001`.
- [ ] Verify `requirement_agent_run.workspace_id` for that run is
  `ws-ibm-i-team`.
- [ ] Switch workspace to Payment Gateway Pro and verify the request
  list changes; no IBM-i requirements appear.
- [ ] Playwright E2E `ibm-i-day1.spec.ts` automates the above.

---

## Phase MTF-11: Cross-Workspace Operations

- [ ] Add `/api/v1/reports/fleet/...` controller with
  `AUDITOR | PLATFORM_ADMIN` gate.
- [ ] Wrap fleet queries in
  `runWithCrossWorkspaceAccess(REPORT_CENTER_FLEET, ...)`.
- [ ] Verify `/api/v1/platform/*` continues to work without the
  workspace prefix.
- [ ] Test that fleet endpoint returns rows from both seeded
  workspaces; that other roles get `403`.

---

## Phase MTF-12: Verification

### Backend

- [ ] `./mvnw test` passes.
- [ ] `WorkspaceContextResolutionTest` covers every branch of
  FR-MTF-02.
- [ ] `ScopeAuthorizationTest` covers every scope type.
- [ ] `RepositoryAutoFilterTest` covers representative entity from
  each of the 13 slices.
- [ ] `AllWorkspaceScopedEntitiesAreFiltered` finds zero offenders.
- [ ] `ControlPlaneBackfillTest` confirms zero null `workspace_id`.
- [ ] `JenkinsWebhookWorkspaceResolutionTest`,
  `GithubWebhookWorkspaceResolutionTest`,
  `AuthWorkspaceControllerTest`, `WorkspaceSwitchAuditTest`,
  `DemoWorkspaceTest` all pass.

### Frontend

- [ ] `npm run build` passes (TypeScript clean).
- [ ] `npm test` covers `apiClient`, `WorkspaceSwitcher`,
  `resolveWorkspaceKey`.
- [ ] Playwright `ibm-i-day1.spec.ts` and
  `multi-tenancy-isolation.spec.ts` pass against local backend.

### Manual smoke

- [ ] Login as seed admin, switch between workspaces, observe data
  changing.
- [ ] Login as `43929999`, run the IBM-i CLI handoff demo end to end.
- [ ] Hit a non-prefixed URL (`/api/v1/requirements`); expect `404`.
- [ ] Hit a workspace I don't have scope on; expect `403`.
- [ ] Restart backend; observe both seeded workspaces still functional.

---

## Definition of Done

- All 16 domain controllers serve workspace-prefixed URLs and reject
  non-prefixed URLs with `404`.
- All workspace-scoped entities are filtered automatically; reflection
  test enforces this.
- Two seeded workspaces (`ws-default-001`, `ws-ibm-i-team`) hold
  isolated data; cross-workspace requests return `403`.
- The workspace switcher works end-to-end including URL update,
  cookie set, audit row written, data re-fetched.
- `/auth/workspaces/by-key/{key}` resolves live keys, follows alias
  redirects within 30 days, and rejects unknown / unauthorized keys.
- IBM-i Day 1 demo passes: login → land on
  `/ibm-i-team/dashboard` → `REQ-IBMI-0001` → `Prepare Prompt` →
  CLI prompt references `ibm-i-functional-spec`, with the resulting
  `AgentRun` row carrying `workspace_id = ws-ibm-i-team`.
- Demo / guest path renders against the synthetic `demo` workspace
  without requiring header or path setup.
- All BE and FE tests pass; `AllWorkspaceScopedEntitiesAreFiltered` is
  green; Playwright IBM-i E2E is green.

---

## Out-of-Scope Reminders

- No `team` / `team_member` tables (next slice: `team-membership`).
- No invitation / self-signup.
- No `tenant_id` activation.
- No cross-workspace document sharing.
- No new SDD profiles beyond IBM-i + standard Java.
- No change to `AgentRun` lifecycle, next-action engine, or
  `PipelineProfile` model.
- No "Generate AI" / "Run AI" buttons in the UI.
