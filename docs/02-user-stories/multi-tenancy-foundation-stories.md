# Multi-Tenancy Foundation User Stories

## Profile

`platform-foundation-sdd` — actors are technical roles (Platform Operator,
Workspace Member, Backend Developer, Anonymous Caller, IBM-i Team Lead);
acceptance criteria are observable system behavior at the API or component
boundary.

## Source

`multi-tenancy-foundation-requirements.md`

## Epic 1: Workspace identity at the API boundary

### S-MTF-01: Reject anonymous calls to domain APIs

> As an Anonymous Caller
> I want domain endpoints to reject me before any data is fetched
> So that no requirement, dashboard, or incident leaks from a public probe.

**Acceptance Criteria**

- Given no `sdlc_session` cookie
  When I call `GET /api/v1/workspaces/ws-default-001/requirements`
  Then the response is `401 AUTH_REQUIRED` and the request never reaches
  `RequirementService`.
- Given no cookie
  When I call any of the 16 domain controller endpoints under
  `/api/v1/workspaces/{id}/...`
  Then each rejects with `401 AUTH_REQUIRED`.

### S-MTF-02: Reject non-prefixed domain URLs

> As a Platform Operator
> I want the legacy non-prefixed paths to disappear cleanly
> So that no caller can accidentally hit the old single-tenant route.

**Acceptance Criteria**

- Given any path under `/api/v1/<resource>/...` where `<resource>` is not
  in the allowlist (`auth`, `health`, `integration/webhooks`, `platform`,
  `reports/fleet`)
  When I call it
  Then the response is `404`.
- Given paths in the allowlist
  When I call them
  Then they continue to function with their pre-existing auth rules.

### S-MTF-03: Reject calls to workspaces I am not granted

> As a Workspace Member of `ws-default-001`
> I want my requests to `ws-ibm-i-team` rejected
> So that scope leakage is impossible regardless of frontend bugs.

**Acceptance Criteria**

- Given staff `43920001` holds scope `workspace:ws-default-001` only
  When I call `GET /api/v1/workspaces/ws-ibm-i-team/requirements`
  Then the response is `403 WORKSPACE_SCOPE_REQUIRED`.
- Given the same staff
  When I call `GET /api/v1/workspaces/ws-does-not-exist/requirements`
  Then the response is `404 WORKSPACE_NOT_FOUND`.
- Given the rejection above
  When I open the Audit page
  Then I find a `permission_change / workspace.access_denied` event with
  payload `{ "claimedWorkspaceId": "ws-ibm-i-team" }`.

### S-MTF-04: Wide scopes (`platform` / `application` / `snow_group` / `project`) work

> As a Platform Operator with `platform:*` scope
> I want my single grant to authorize any workspace
> So that I can investigate cross-team incidents without per-workspace grants.

**Acceptance Criteria**

- Given a staff user with `platform:*`
  When I call any domain endpoint under any workspace path
  Then the response is `200`.
- Given a staff user with `application:app-payment-gateway-pro` only
  When I call any endpoint under
  `/api/v1/workspaces/ws-default-001/...` (which has matching
  `application_id`)
  Then the response is `200`.
- Given a staff user with `project:proj-42` only
  When I call any endpoint under the workspace owning `proj-42`
  Then the response is `200`.

### S-MTF-05: Project filter is a query parameter within workspace

> As a Workspace Member with multiple projects
> I want to filter by project within my workspace
> So that I can focus on one project at a time without leaving my workspace.

**Acceptance Criteria**

- Given a staff user authorized for `ws-ibm-i-team`
  When I call
  `GET /api/v1/workspaces/ws-ibm-i-team/requirements?projectId=proj-ibmi-001`
  Then the response contains only requirements whose
  `project_id = proj-ibmi-001`.
- Given a `projectId` parameter that points to a project owned by a
  different workspace
  Then the response is `400 PROJECT_NOT_IN_WORKSPACE`.

## Epic 2: Repository auto-filtering across all 16 slices

### S-MTF-06: Domain repositories return only current-workspace rows

> As a Backend Developer
> I want every repository on a `workspace_id` table to filter automatically
> So that I cannot leak cross-tenant rows by forgetting a manual filter.

**Acceptance Criteria**

- Given two requirements `R-A` (`workspace_id = ws-default-001`) and `R-B`
  (`workspace_id = ws-ibm-i-team`)
  When `RequirementService.list()` is invoked under
  `WorkspaceContextHolder` set to `ws-default-001`
  Then the result contains `R-A` and not `R-B`.
- Given the same setup
  When the service is invoked under `ws-ibm-i-team`
  Then the result contains `R-B` and not `R-A`.
- Given a representative entity from each slice (Dashboard
  `risk_signals`, Incident, ProjectSpace, Design `design_artifact`,
  CodeBuild `repos`, Testing `test_plan`, Deployment `dp_application`,
  AI Center `skill_executions`)
  Then each shows the same isolation behavior.

### S-MTF-07: Control-plane entities are workspace-scoped after migration

> As a Backend Developer
> I want `AgentRun`, `SourceReference`, `SddDocumentIndex`,
> `DocumentReview`, `AgentStageEvent`, `QualityGateRun`, `ArtifactLink`
> filtered by their own `workspace_id`
> So that cross-workspace runs cannot leak through the control plane.

**Acceptance Criteria**

- Given migration `V97__add_workspace_id_to_control_plane.sql` has run
  Then each of the 7 control-plane tables carries a `workspace_id` column.
- Given existing rows
  Then the backfill populates `workspace_id` from
  `requirement.workspace_id` and a verification query reports zero
  null values.
- Given the auto-filter is enabled
  When I create an `AgentRun` from a request bound to `ws-ibm-i-team`
  Then the row's `workspace_id` is `ws-ibm-i-team` (set from the
  context holder, not derived at insert time).

### S-MTF-08: Calls without workspace context throw rather than leak

> As a Backend Developer
> I want missing context to fail loudly
> So that a forgotten interceptor cannot silently return all rows.

**Acceptance Criteria**

- Given a thread with no `WorkspaceContext` set
  When `RequirementRepository.findAll()` is called
  Then a `MISSING_WORKSPACE_CONTEXT` exception is thrown.

### S-MTF-09: All `workspace_id` entities are filter-protected

> As a Backend Developer
> I want a regression guard against new repositories that bypass the filter
> So that the foundation cannot rot as the codebase grows.

**Acceptance Criteria**

- Given a test that scans all classes annotated with `@Entity`
  When any class with a `workspace_id` column lacks the
  `workspace_filter` Hibernate filter
  Then the test fails with the list of offending entities.

### S-MTF-10: Webhook ingestion resolves workspace from payload

> As a Backend Developer
> I want Jenkins and GitHub webhooks to write into the right workspace
> So that ingested data does not collapse into a single workspace.

**Acceptance Criteria**

- Given two `JenkinsInstanceEntity` rows in distinct workspaces
  When a Jenkins webhook arrives for `jenkins-default`
  (`workspace_id = ws-default-001`)
  Then the resulting `dp_release` row carries
  `workspace_id = ws-default-001`.
- Given a Jenkins webhook arrives for `jenkins-ibmi`
  (`workspace_id = ws-ibm-i-team`)
  Then the resulting row carries `workspace_id = ws-ibm-i-team`.
- Given the webhook handler reads scoped repositories during resolution
  Then those reads happen inside an explicit
  `runWithCrossWorkspaceAccess(...)` block whose reason is
  `webhook.jenkins.ingest`, logged at INFO.

## Epic 3: Path-based URLs and frontend rebase

### S-MTF-11: Workspace switcher lists my granted workspaces

> As a Workspace Member
> I want to see exactly the workspaces I have access to
> So that I know my visibility scope.

**Acceptance Criteria**

- Given staff `43920001` holds `workspace:ws-default-001`
  When I log in
  Then the top-bar workspace switcher contains `ws-default-001` (label
  "Payment Gateway Pro") and no other.
- Given staff with `platform:*`
  Then the switcher lists every active row in `PLATFORM_WORKSPACE`,
  including `ws-default-001` and `ws-ibm-i-team`.

### S-MTF-12: UI URLs are key-based and shareable

> As a Workspace Member
> I want to share a URL with a teammate
> So that we can both land on the same view of the same workspace.

**Acceptance Criteria**

- Given I am viewing
  `/payment-gateway-pro/requirements/REQ-0001` as user A
  When user B (also authorized for `ws-default-001`) opens that URL
  Then user B lands on the same requirement detail.
- Given user B does not have scope on `ws-default-001`
  When user B opens the URL
  Then user B is redirected to
  `/no-access?attempted=payment-gateway-pro` with a toast.
- Given anonymous browser
  When the URL is opened
  Then the response is a redirect to
  `/login?next=/payment-gateway-pro/requirements/REQ-0001`.

### S-MTF-13: Switching workspace updates the URL and reloads data

> As a Workspace Member with two workspaces
> I want to switch in one click
> So that I can compare team views.

**Acceptance Criteria**

- Given I am at `/payment-gateway-pro/requirements`
  When I select `IBM-i Team` in the switcher
  Then the URL changes to `/ibm-i-team/requirements`,
  `workspaceStore.context.workspaceId === "ws-ibm-i-team"`,
  Subsequent API calls go to
  `/api/v1/workspaces/ws-ibm-i-team/requirements`,
  And the requirement list shows the IBM-i seeded data.
- Given I press the browser back button
  Then the URL returns to
  `/payment-gateway-pro/requirements` and the previous workspace's data
  is shown.

### S-MTF-14: Workspace key rename returns a one-time redirect

> As a Platform Operator
> I want stable URLs even after a workspace is renamed
> So that bookmarks and Slack links don't break overnight.

**Acceptance Criteria**

- Given workspace `ws-default-001` had key `payment-gateway-pro` and was
  renamed to `pgw-pro` 5 days ago
  When a user opens `/payment-gateway-pro/requirements`
  Then the response is `301` to `/pgw-pro/requirements`.
- Given the rename is more than 30 days old
  Then the old key returns `404`.

### S-MTF-15: Frontend HTTP client always sends path-prefixed URL

> As a Frontend Developer
> I want my code to call domain APIs without thinking about workspace
> So that it is impossible to call the wrong workspace by accident.

**Acceptance Criteria**

- Given `workspaceStore.context.workspaceId = "ws-default-001"` and a
  call `apiClient.get('/requirements')`
  When the request is sent
  Then the actual URL is
  `/api/v1/workspaces/ws-default-001/requirements`.
- Given no workspace selected and a call to a domain endpoint
  Then the call throws `NO_WORKSPACE_SELECTED` client-side and the
  request is never sent.
- Given a call to `/auth/me` or `/health`
  Then no rewrite occurs and the request goes through verbatim.

### S-MTF-16: Workspace switch is audited

> As a Platform Operator
> I want workspace switches recorded
> So that I can investigate context shifts before an incident.

**Acceptance Criteria**

- Given staff `43920001` switches from `ws-default-001` to `ws-ibm-i-team`
  When the switch succeeds
  Then `PLATFORM_AUDIT` contains a `permission_change / workspace.switch`
  row with payload
  `{ "before": "ws-default-001", "after": "ws-ibm-i-team" }` and
  `actor_staff_id = "43920001"`.

## Epic 4: Demo and IBM-i Day 1 readiness

### S-MTF-17: Demo / guest mode runs against the synthetic `demo` workspace

> As a Platform Operator
> I want demo and guest mode to remain usable for sales and onboarding
> So that we don't break the demo while hardening multi-tenancy.

**Acceptance Criteria**

- Given `currentUser.mode = "guest"` AND deployment `demoMode = true`
  When I open `/`
  Then I am redirected to `/demo/requirements` and all subsequent API
  calls go through `/api/v1/workspaces/demo/...`.
- Given the synthetic `demo` workspace
  When the backend resolves it
  Then the workspace is the row pointed to by
  `workspace_context.workspace_id` and the data shown is the existing
  seed data.
- Given an authenticated staff user (mode `staff`)
  Then `/demo/...` is not allowed; the URL redirects to the user's
  default workspace.

### S-MTF-18: IBM-i workspace is seeded and demo-ready

> As an IBM-i Team Lead
> I want my workspace to exist on Day 1 with a sample requirement
> So that I can run the Jira → code chain end to end without setup.

**Acceptance Criteria**

- Given migrations have run
  When I list `PLATFORM_WORKSPACE`
  Then `ws-ibm-i-team` exists with key `ibm-i-team` and a matching
  `PLATFORM_APPLICATION` and `PLATFORM_SNOW_GROUP`.
- Given staff user `43929999` exists with
  `WORKSPACE_VIEWER:workspace:ws-ibm-i-team`
  When I log in as that user
  Then the switcher selects `IBM-i Team` and the URL is
  `/ibm-i-team/dashboard`.
- Given the IBM-i workspace
  When I navigate to `/ibm-i-team/requirements`
  Then I see exactly one seeded requirement `REQ-IBMI-0001` with one
  Jira-style `SourceReference` and the IBM-i `PipelineProfile` bound.

### S-MTF-19: IBM-i CLI handoff produces a workspace-scoped AgentRun

> As an IBM-i Team Lead
> I want the CLI handoff to carry my workspace identity
> So that the manifest the CLI consumes points at my repo, my profile,
> and my requirement.

**Acceptance Criteria**

- Given I am on
  `/ibm-i-team/requirements/REQ-IBMI-0001` and the next-action panel
  shows `Generate Functional Spec`
  When I click `Prepare Prompt`
  Then a row is created in `requirement_agent_run` with
  `workspace_id = ws-ibm-i-team`,
  `profileId = ibm-i`, and a `command` referencing the IBM-i skill
  (e.g., `/ibm-i-functional-spec ...`).
- Given the same CLI run
  Then the manifest payload references the IBM-i workspace's repo and
  branch (not Payment Gateway Pro's), and no field leaks from another
  workspace.
- Given the user copies the CLI prompt and merges a PR
  When the user confirms the merge
  Then the `SddDocumentIndex` row created carries
  `workspace_id = ws-ibm-i-team`.

### S-MTF-20: Default seed admin keeps working after migration

> As a Platform Operator
> I want the local dev experience unchanged after migration
> So that engineers can keep working.

**Acceptance Criteria**

- Given the seed admin user has `WORKSPACE_MEMBER` on
  `ws-default-001` after migration
  When I log in
  Then the switcher selects `Payment Gateway Pro` (mapped from
  `ws-default-001`) by default,
  And all 13 existing slices render their seeded data,
  And every API call observes the path-prefixed URL.

## Epic 5: Cross-workspace operations

### S-MTF-21: Cross-workspace report queries remain possible but explicit

> As a Platform Operator
> I want platform-wide reporting to keep working
> So that Report Center can show fleet-level metrics.

**Acceptance Criteria**

- Given a `GET /api/v1/reports/fleet/lead-time` request
  When the service runs
  Then the query is wrapped in
  `runWithCrossWorkspaceAccess("report.center.fleet", ...)`,
  And the wrapping is logged at INFO with the same reason code,
  And the response includes data from both `ws-default-001` and
  `ws-ibm-i-team`.
- Given the user lacks `AUDITOR` or `PLATFORM_ADMIN`
  Then the request is rejected `403`.

### S-MTF-22: Platform admin endpoints stay outside the workspace prefix

> As a Platform Administrator
> I want to manage users, roles, and the workspace catalog without
> picking a workspace first
> So that platform-wide operations are not confused with tenant-scoped data.

**Acceptance Criteria**

- Given a `PLATFORM_ADMIN` user
  When I call `/api/v1/platform/access/users` or
  `/api/v1/platform/workspaces`
  Then the response is `200` and no workspace header / prefix is
  required.
- Given a non-admin user
  Then the same calls return `403`.
