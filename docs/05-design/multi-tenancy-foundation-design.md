# Multi-Tenancy Foundation Design

## Purpose

Concrete implementation design — file paths, component contracts, visual
decisions, error and empty states, integration boundaries — for the
multi-tenancy foundation slice. Companion to:

- [requirements](../01-requirements/multi-tenancy-foundation-requirements.md)
- [spec](../03-spec/multi-tenancy-foundation-spec.md)
- [architecture](../04-architecture/multi-tenancy-foundation-architecture.md)
- [data-flow](../04-architecture/multi-tenancy-foundation-data-flow.md)
- [data-model](../04-architecture/multi-tenancy-foundation-data-model.md)
- [API guide](contracts/multi-tenancy-foundation-API_IMPLEMENTATION_GUIDE.md)

---

## 1. File Structure

### Backend

```text
backend/src/main/java/com/sdlctower/
├── platform/
│   ├── workspace/
│   │   ├── WorkspaceContext.java                    [new] record
│   │   ├── WorkspaceContextHolder.java              [new]
│   │   ├── WorkspaceContextInterceptor.java         [new]
│   │   ├── WorkspaceContextResolver.java            [new]
│   │   ├── WorkspaceContextDto.java                 [extend]
│   │   ├── WorkspaceContextController.java          [move under prefix]
│   │   ├── PlatformWorkspaceEntity.java             [new] JPA mapping for V86 table
│   │   ├── PlatformWorkspaceRepository.java         [new]
│   │   ├── WorkspaceListItemDto.java                [new]
│   │   ├── WorkspaceKeyAliasEntity.java             [new]
│   │   ├── WorkspaceKeyAliasRepository.java         [new]
│   │   ├── WorkspaceKeyAliasService.java            [new]
│   │   ├── WorkspaceCrossAccessReason.java          [new] enum
│   │   ├── MissingWorkspaceContextException.java    [new]
│   │   └── WorkspaceScopeException.java             [new]
│   └── auth/
│       ├── AuthService.java                         [extend]
│       ├── AuthProperties.java                      [extend: demoMode]
│       ├── AuthWorkspaceController.java             [new]
│       └── CurrentUserDto.java                      [extend: demoMode]
├── shared/
│   └── persistence/
│       ├── WorkspaceFilter.java                     [new] @FilterDef holder
│       ├── WorkspaceFilterAspect.java               [new]
│       └── WorkspacePrePersistListener.java         [new] canary
├── config/
│   └── WebMvcConfig.java                            [extend: register interceptor]
└── domain/                                          [touch all 16 controllers]
    ├── requirement/RequirementController.java       [path prefix]
    ├── requirement/knowledgegraph/KnowledgeGraphController.java
    ├── dashboard/DashboardController.java
    ├── incident/IncidentController.java
    ├── teamspace/TeamSpaceController.java
    ├── projectspace/ProjectSpaceController.java
    ├── projectmanagement/ProjectManagementController.java
    ├── projectmanagement/PlanController.java
    ├── projectmanagement/PortfolioController.java
    ├── designmanagement/DesignManagementController.java
    ├── codebuildmanagement/CodeBuildController.java
    ├── testingmanagement/TestingManagementController.java
    ├── aicenter/AiCenterController.java
    ├── deploymentmanagement/DeploymentController.java
    └── reportcenter/ReportCenterController.java
```

### Frontend

```text
frontend/src/
├── shell/
│   └── components/
│       └── WorkspaceSwitcher.vue                    [new]
├── shared/
│   ├── api/
│   │   ├── apiClient.ts                             [extend: workspace path interceptor]
│   │   ├── authApi.ts                               [extend: switch / by-key endpoints]
│   │   └── workspaceApi.ts                          [extend]
│   ├── stores/
│   │   ├── sessionStore.ts                          [extend: demoMode]
│   │   └── workspaceStore.ts                        [extend: workspaces map]
│   └── types/
│       └── shell.ts                                 [extend: Workspace, CurrentUser]
└── router/
    ├── index.ts                                     [rebase under :workspaceKey]
    └── guards/
        └── resolveWorkspaceKey.ts                   [new]
```

### Migrations

```text
backend/src/main/resources/db/migration/
├── V97__add_workspace_id_to_control_plane.sql
├── V98__backfill_control_plane_workspace_id.sql
├── V99__enforce_control_plane_workspace_id.sql
├── V100__add_workspace_id_indexes.sql
├── V101__create_platform_workspace_key_alias.sql
├── V102__seed_ibm_i_workspace.sql
└── V103__update_seed_admin_scopes.sql
```

---

## 2. Component Contracts

### `WorkspaceContextHolder`

```java
public final class WorkspaceContextHolder {
    public static WorkspaceContext current();                               // throws MissingWorkspaceContextException
    public static Optional<WorkspaceContext> maybeCurrent();
    public static void set(WorkspaceContext ctx);
    public static void clear();
    public static <T> T runWithCrossWorkspaceAccess(String reason, Supplier<T> action);
    public static boolean isCrossWorkspaceAccess();                         // for the aspect
}
```

Reason codes are constrained by `WorkspaceCrossAccessReason` enum
(FR-MTF-08): `WEBHOOK_JENKINS_INGEST`, `WEBHOOK_GITHUB_INGEST`,
`REPORT_CENTER_FLEET`, `PLATFORM_ACCESS_ADMIN`, `SEED_BOOTSTRAP`,
`AUTH_WORKSPACE_RESOLVE`. The string version of each is logged.

### `WorkspaceContextInterceptor`

```java
public class WorkspaceContextInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler);
    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex);
}
```

Behavior follows FR-MTF-02. Allowlist checked first via
`PathPatternMatcher` against the path patterns in FR-MTF-04. URL path
parsing extracts `workspaceId` between `/api/v1/workspaces/` and the
next `/`.

### `WorkspaceFilterAspect`

```java
@Aspect
@Component
public class WorkspaceFilterAspect {
    @Around("@within(org.springframework.transaction.annotation.Transactional) || @annotation(org.springframework.transaction.annotation.Transactional)")
    public Object filter(ProceedingJoinPoint pjp);
}
```

Reads `WorkspaceContextHolder`, enables / disables Hibernate filter on
the active session, executes the join point, and restores the prior
filter state in finally.

### `AuthWorkspaceController` (new)

```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthWorkspaceController {
    @PostMapping("/workspace")
    ApiResponse<WorkspaceContextDto> switchWorkspace(@RequestBody SwitchWorkspaceRequest req, HttpServletRequest http, HttpServletResponse res);

    @GetMapping("/workspaces")
    ApiResponse<List<WorkspaceListItemDto>> listMyWorkspaces(HttpServletRequest http);

    @GetMapping("/workspaces/by-key/{key}")
    ResponseEntity<ApiResponse<WorkspaceListItemDto>> resolveByKey(@PathVariable String key, HttpServletRequest http);
}
```

`resolveByKey` returns:
- `200 + payload` for live key + authorized
- `301` (with `Location: /api/v1/auth/workspaces/by-key/{newKey}`)
  for alias hit
- `403 WORKSPACE_SCOPE_REQUIRED` for unauthorized live key
- `404` for unknown key

### `WorkspaceSwitcher.vue`

```vue
<script setup lang="ts">
interface Props {
  // none — reads from stores
}

interface Emits {
  // none — side effects through stores + router
}
</script>
```

Internal contracts:

- Reads `sessionStore.currentUser`, `workspaceStore.workspaces`,
  `workspaceStore.context`, `route.params.workspaceKey`
- On select calls `workspaceApi.switchWorkspace(workspaceId)`,
  `workspaceStore.setActive(...)`, `router.push(...)`
- Hidden when `route.path.startsWith('/demo')`
- Hidden when `workspaceStore.workspaces.length <= 1`
- Persists last selection in `localStorage.workspace.{staffId}`

### `apiClient` interceptor

```ts
client.interceptors.request.use((config) => {
  const url = config.url ?? '';
  if (isAllowlisted(url)) return config;
  const workspaceId = useWorkspaceStore().context.workspaceId;
  if (!workspaceId) {
    throw new ApiClientError('NO_WORKSPACE_SELECTED');
  }
  config.url = injectWorkspacePrefix(url, workspaceId);
  return config;
});

const ALLOWLIST = ['/auth/', '/health', '/integration/webhooks/', '/platform/', '/reports/fleet/'];

function isAllowlisted(url: string): boolean {
  return ALLOWLIST.some(prefix => url.startsWith(prefix));
}

function injectWorkspacePrefix(url: string, workspaceId: string): string {
  const sep = url.startsWith('/') ? '' : '/';
  return `/workspaces/${workspaceId}${sep}${url}`;
}
```

Tested in `apiClient.spec.ts` (FR-MTF-11).

### `resolveWorkspaceKey` router guard

```ts
export async function resolveWorkspaceKey(to: RouteLocationNormalized): Promise<NavigationGuardReturn> {
  const ws = useWorkspaceStore();
  const session = useSessionStore();
  const key = to.params.workspaceKey as string | undefined;

  if (!key) return '/login';

  // Cache hit
  const cached = ws.workspaces.find(w => w.workspaceKey === key);
  if (cached) {
    ws.setActive(cached.workspaceId);
    return true;
  }

  // Cold lookup
  try {
    const resolved = await workspaceApi.resolveByKey(key);
    if (resolved.kind === 'redirect') {
      const next = to.fullPath.replace(`/${key}/`, `/${resolved.newKey}/`);
      return { path: next, replace: true };
    }
    ws.workspaces = [...ws.workspaces, resolved.workspace];
    ws.setActive(resolved.workspace.workspaceId);
    return true;
  } catch (e) {
    if (e instanceof ApiClientError && e.status === 401) {
      return `/login?next=${encodeURIComponent(to.fullPath)}`;
    }
    return `/no-access?attempted=${encodeURIComponent(key)}`;
  }
}
```

---

## 3. Visual Design Decisions

### Workspace switcher

Location: top bar, left of the user avatar.

Visual:

- Compact dropdown trigger showing `workspaceName` + `workspaceKey`
  in muted small text
- Menu items show `name` (primary) + `applicationName / snowGroupName`
  (subtitle) — helps a `platform:*` viewer disambiguate
- Active workspace marked with a left accent bar in `--color-secondary`
- Two-state interaction: closed (compact), open (full-list dropdown)
- Loading state during `POST /auth/workspace` shows a small spinner
  inside the trigger
- Error state (403) shows a one-line toast and reverts selection

Tokens: reuse existing `--color-on-surface`, `--color-on-surface-variant`,
`--radius-sm`, `--font-ui`. No new tokens introduced.

Hidden when:
- Route is under `/demo/*`
- The user has access to exactly one workspace (degenerate case;
  switcher shows as static label instead)

### URL bar

UI URLs always show the friendly key:
- `/payment-gateway-pro/requirements`
- `/ibm-i-team/dashboard/REQ-IBMI-0001`
- `/demo/dashboard`

Special routes outside the workspace prefix:
- `/login` — public
- `/no-access?attempted={key}` — public (post-redirect)
- `/platform/*` — `PLATFORM_ADMIN` only; rendered without a workspace
  context

### Empty / error states

| State | Surface | Content |
|---|---|---|
| User has zero workspaces | shell empty state | "You haven't been added to a workspace yet. Contact your platform admin." with `staffId` displayed for support |
| Cold load `403 by-key` | redirect to `/no-access` | "You don't have access to {attempted}." + list of accessible workspaces |
| Cold load `404 by-key` | redirect to `/no-access` | "We couldn't find a workspace called {attempted}." + list of accessible workspaces |
| `NO_WORKSPACE_SELECTED` thrown client-side | shell overlay | "Pick a workspace to continue" + the switcher button |
| Switch returned `403` | toast + revert | "You don't have access to {name}." |
| Workspace renamed | silent `301` redirect | URL replaces, view continues |

### Demo banner

When route is under `/demo/*`, the shell shows a thin top banner:

> Demo workspace. Sign in to use Control Tower with your team's data.
> [Sign in]

The banner reuses the existing demo-mode banner tokens.

---

## 4. Integration Boundaries

```mermaid
flowchart LR
    BROWSER[Browser]
    APIC[apiClient]
    BE[Spring Boot]
    DB[(DB)]
    GH[GitHub]
    JEN[Jenkins]

    BROWSER -- /auth/* --> BE
    BROWSER -- /{key}/feature --> APIC
    APIC -- /api/v1/workspaces/{id}/feature --> BE
    BE -- @Filter("workspace_filter") --> DB
    BE -- webhook signature --> GH
    BE -- webhook signature --> JEN
    GH -- POST /integration/webhooks/github --> BE
    JEN -- POST /integration/webhooks/jenkins --> BE
    BE -- runWithCrossWorkspaceAccess --> DB
```

Boundaries this slice changes:

- `Browser ↔ apiClient`: URL is now key-based; key resolution happens
  before any API call
- `apiClient ↔ Spring`: URL is workspace-id-prefixed for non-allowlisted
  routes
- `Spring ↔ DB`: every scoped query carries `workspace_id = ?` from the
  session filter
- `Webhook ↔ Spring`: workspace resolved from payload, not URL

Boundaries explicitly **not** changed:
- CLI ↔ GitHub (CLI handoff lifecycle)
- Spring ↔ GitHub (SDD doc indexing on PR merge)
- Browser ↔ CLI (clipboard handoff via `CliAgentRunPanel`)

---

## 5. Test Surface (per-component)

| Component | Test class | What it asserts |
|---|---|---|
| `WorkspaceContextInterceptor` | `WorkspaceContextResolutionTest` | Each branch of FR-MTF-02 |
| `WorkspaceContextResolver` | `ScopeAuthorizationTest` | Each scope type in FR-MTF-03 |
| `WorkspaceFilterAspect` | `RepositoryAutoFilterTest` | Two-workspace isolation across representative entities |
| `WorkspacePrePersistListener` | `WorkspaceCanaryTest` | Insert without holder throws |
| `AuthWorkspaceController` | `AuthWorkspaceControllerTest` | Switch + list + by-key + alias 301 |
| `WorkspaceKeyAliasService` | `WorkspaceKeyAliasServiceTest` | 30-day window |
| All `@Entity` | `AllWorkspaceScopedEntitiesAreFiltered` | Reflection check |
| `apiClient` interceptor | `apiClient.spec.ts` | Path injection + allowlist |
| `WorkspaceSwitcher.vue` | `WorkspaceSwitcher.spec.ts` | Render + click + emit + revert |
| `resolveWorkspaceKey` | `resolveWorkspaceKey.spec.ts` | Cache hit, cold, alias, 403, 404 |
| End-to-end | `ibm-i-day1.spec.ts` (Playwright) | IBM-i Day 1 success goal |

---

## 6. Performance Budget

- Interceptor cost (cache hit): < 1 ms
- Interceptor cost (cache miss): one DB roundtrip, < 5 ms on local H2
- Hibernate filter cost: zero per query (filter is index-friendly)
- Frontend `apiClient` interceptor: synchronous string ops, < 0.5 ms
- p95 regression on `GET /workspaces/{id}/requirements` vs current
  `GET /requirements`: ≤ 10 ms

---

## 7. Backwards Compatibility

- All non-allowlisted `GET /api/v1/<resource>` URLs return `404` after
  this slice ships. There is no shim. Frontend, tests, and any external
  consumers must be updated together. Webhook URLs, auth URLs,
  `/health`, and `/api/v1/platform/*` are unchanged.
- `workspace_context` table is preserved; only the demo / guest path
  consults it.
- Existing seed data continues to work; the seed admin gains scopes on
  both seeded workspaces (V103) so the demo of "switching" is real.

---

## 8. Rollout Notes (showcase / outside-network context)

This slice is intentionally rolled out in one go because:

- The user is on the public network and is building the foundation for
  later intranet QA — partial migration would multiply test cases for
  no business value
- The rebase is mechanical once the shared infrastructure
  (Holder + Aspect + Filter + interceptor) lands

Local verification path:

1. Run migrations against H2; verify V98 backfill yields zero null
   `workspace_id`
2. Run backend tests; `AllWorkspaceScopedEntitiesAreFiltered` must pass
3. Run frontend `npm run build` then `npm run dev`
4. Log in as seed admin → see both workspaces in switcher → switch →
   data changes
5. Log in as `43929999` → land directly on `/ibm-i-team/dashboard` →
   navigate to `/ibm-i-team/requirements/REQ-IBMI-0001` → click
   "Prepare Prompt" → CLI prompt mentions `ibm-i-functional-spec` and
   `REQ-IBMI-0001`

If any step fails on H2, do not proceed to intranet QA.
