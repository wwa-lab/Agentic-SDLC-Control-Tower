# Multi-Tenancy Foundation — API Implementation Guide

## Purpose

Complete REST contract for the slice: every new endpoint, every changed
URL convention, request / response JSON examples, error codes, backend
implementation notes, frontend integration notes, and testing
contracts. Primary source of truth for Codex when implementing.

## Source

- [spec](../../03-spec/multi-tenancy-foundation-spec.md)
- [data-model](../../04-architecture/multi-tenancy-foundation-data-model.md)
- [design](../multi-tenancy-foundation-design.md)

## Traceability

| Spec FR | Endpoint / Convention |
|---|---|
| FR-MTF-01, FR-MTF-02 | All non-allowlisted routes move to `/api/v1/workspaces/{workspaceId}/...` |
| FR-MTF-04 | Allowlist of un-prefixed endpoints |
| FR-MTF-09 | `POST /auth/workspace`, `GET /auth/workspaces`, `GET /auth/workspaces/by-key/{key}` |
| FR-MTF-10 | Alias `301` on `GET /auth/workspaces/by-key/{key}` |
| FR-MTF-15 | `GET /auth/me` carries `demoMode`; `GET /workspaces/demo/...` resolves via `workspace_context` |
| FR-MTF-17 | 16 domain controllers gain workspace prefix |

---

## 1. URL Convention

### Canonical workspace-scoped form

```
/api/v1/workspaces/{workspaceId}/<resource>...
```

`workspaceId` is the stable id (`ws-default-001`). Examples:

```
GET    /api/v1/workspaces/ws-default-001/requirements
POST   /api/v1/workspaces/ws-default-001/requirements
GET    /api/v1/workspaces/ws-default-001/requirements/REQ-0001
GET    /api/v1/workspaces/ws-default-001/dashboard/summary
GET    /api/v1/workspaces/ws-default-001/incidents?status=open
GET    /api/v1/workspaces/ws-default-001/requirements?projectId=proj-42
```

### Allowlist (no workspace prefix)

| Path | Auth |
|---|---|
| `/api/v1/auth/**` | Public + post-login |
| `/api/v1/health` | None |
| `/api/v1/integration/webhooks/**` | Webhook signature |
| `/api/v1/platform/**` | `PLATFORM_ADMIN` |
| `/api/v1/reports/fleet/**` | `AUDITOR` or `PLATFORM_ADMIN` |

### Demo synthetic id

`workspaceId == "demo"` is allowed only when `currentUser.mode == "guest"`
and `AuthProperties.demoMode == true`. Resolves server-side from
`workspace_context.workspace_id`. Staff users hitting this path receive
`403` with `Location: /api/v1/workspaces/{their-default}/...`.

---

## 2. New / Changed Endpoints

### POST `/api/v1/auth/workspace` — switch workspace

Request:
```json
{ "workspaceId": "ws-ibm-i-team" }
```

Response 200:
```json
{
  "success": true,
  "data": {
    "workspaceId": "ws-ibm-i-team",
    "workspaceKey": "ibm-i-team",
    "workspaceName": "IBM-i Team",
    "applicationId": "app-ibmi-core",
    "applicationName": "IBM-i Core",
    "snowGroupId": "snow-ibmi-ops",
    "snowGroupName": "IBM-i Operations",
    "profileId": "ibm-i",
    "demoMode": false
  }
}
```

Errors:

| HTTP | Code | When |
|---|---|---|
| 401 | `AUTH_REQUIRED` | No session |
| 403 | `WORKSPACE_SCOPE_REQUIRED` | User lacks scope |
| 404 | `WORKSPACE_NOT_FOUND` | Unknown id |
| 409 | `GUEST_CANNOT_SWITCH` | mode == guest |

Side effects:

- Persists `permission_change / workspace.switch` audit row
- Sets cookie `sdlc_workspace=ws-ibm-i-team` (HttpOnly, SameSite=Lax)

### GET `/api/v1/auth/workspaces` — list my workspaces

Response 200:
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

Resolution:
- `platform:*` users → all active rows in `PLATFORM_WORKSPACE`
- Other users → intersection of their scopes
- Sorted by `name`

Errors: `401 AUTH_REQUIRED`.

### GET `/api/v1/auth/workspaces/by-key/{key}` — resolve key

Response 200:
```json
{ "success": true, "data": { /* WorkspaceListItemDto */ } }
```

Response 301 (alias hit):
```
HTTP/1.1 301 Moved Permanently
Location: /api/v1/auth/workspaces/by-key/ibm-i-team-v2
```

Body (optional, for clients that don't follow redirects):
```json
{ "success": false, "error": { "code": "WORKSPACE_KEY_REDIRECTED", "message": "...", "details": { "newKey": "ibm-i-team-v2" } } }
```

Errors:

| HTTP | Code |
|---|---|
| 401 | `AUTH_REQUIRED` |
| 403 | `WORKSPACE_SCOPE_REQUIRED` (live key, unauthorized) |
| 404 | `WORKSPACE_KEY_NOT_FOUND` |

### GET `/api/v1/auth/me` — current user (existing, extended)

Response 200 now includes `demoMode`:
```json
{
  "success": true,
  "data": {
    "mode": "staff",
    "authProvider": "manual",
    "staffId": "43920001",
    "displayName": "Seed Admin",
    "staffName": "Seed Admin",
    "avatarUrl": null,
    "roles": ["WORKSPACE_MEMBER"],
    "readOnly": false,
    "scopes": [
      { "scopeType": "workspace", "scopeId": "ws-default-001" },
      { "scopeType": "workspace", "scopeId": "ws-ibm-i-team" }
    ],
    "demoMode": false
  }
}
```

### GET `/api/v1/workspaces/{workspaceId}/context` — current workspace context (was `/workspace-context`)

Response 200:
```json
{
  "success": true,
  "data": {
    "workspaceId": "ws-default-001",
    "workspaceKey": "payment-gateway-pro",
    "workspaceName": "Payment Gateway Pro",
    "applicationId": "app-payment-gateway-pro",
    "applicationName": "Payment Gateway Pro",
    "snowGroupId": "snow-fin-tech-ops",
    "snowGroupName": "Finance Tech Ops",
    "projectId": null,
    "projectName": null,
    "environment": null,
    "demoMode": false,
    "profileId": "standard-java-sdd"
  }
}
```

The legacy unprefixed `/api/v1/workspace-context` is removed.

### Domain endpoints — path migration only (16 controllers)

No body / payload change. Concrete examples:

| Before | After |
|---|---|
| `GET /api/v1/requirements` | `GET /api/v1/workspaces/{workspaceId}/requirements` |
| `POST /api/v1/requirements/import` | `POST /api/v1/workspaces/{workspaceId}/requirements/import` |
| `GET /api/v1/requirements/{id}` | `GET /api/v1/workspaces/{workspaceId}/requirements/{id}` |
| `GET /api/v1/dashboard/summary` | `GET /api/v1/workspaces/{workspaceId}/dashboard/summary` |
| `GET /api/v1/incidents` | `GET /api/v1/workspaces/{workspaceId}/incidents` |
| `GET /api/v1/team-space/...` | `GET /api/v1/workspaces/{workspaceId}/team-space/...` |
| `GET /api/v1/project-space/...` | `GET /api/v1/workspaces/{workspaceId}/project-space/...` |
| `GET /api/v1/projects/...` (mgmt/plan/portfolio) | `GET /api/v1/workspaces/{workspaceId}/projects/...` |
| `GET /api/v1/design-management/...` | `GET /api/v1/workspaces/{workspaceId}/design-management/...` |
| `GET /api/v1/code-build/...` | `GET /api/v1/workspaces/{workspaceId}/code-build/...` |
| `GET /api/v1/testing/...` | `GET /api/v1/workspaces/{workspaceId}/testing/...` |
| `GET /api/v1/ai-center/...` | `GET /api/v1/workspaces/{workspaceId}/ai-center/...` |
| `GET /api/v1/deployment/...` | `GET /api/v1/workspaces/{workspaceId}/deployment/...` |
| `GET /api/v1/reports/...` (workspace-scoped) | `GET /api/v1/workspaces/{workspaceId}/reports/...` |
| `GET /api/v1/knowledge-graph/...` | `GET /api/v1/workspaces/{workspaceId}/knowledge-graph/...` |

Cross-workspace endpoints stay un-prefixed:
- `GET /api/v1/reports/fleet/lead-time` — fleet-level
- `GET /api/v1/platform/access/users` etc. — platform admin

### Project filter — query parameter (FR-MTF-14)

```
GET /api/v1/workspaces/ws-ibm-i-team/requirements?projectId=proj-ibmi-001
```

Validation: `projectId` must belong to `ws-ibm-i-team`; otherwise
`400 PROJECT_NOT_IN_WORKSPACE`. Lookup uses the cached
`projectToWorkspace` map.

---

## 3. Backend Implementation Notes

### Constants

```java
public final class ApiConstants {
    public static final String API_V1 = "/api/v1";
    public static final String AUTH = API_V1 + "/auth";
    public static final String WORKSPACES = API_V1 + "/workspaces";
    public static final String WORKSPACE_BASE = WORKSPACES + "/{workspaceId}";
    public static final String WORKSPACE_REQUIREMENTS = WORKSPACE_BASE + "/requirements";
    public static final String WORKSPACE_DASHBOARD = WORKSPACE_BASE + "/dashboard";
    public static final String WORKSPACE_INCIDENTS = WORKSPACE_BASE + "/incidents";
    /* ... 13 more ... */
    public static final String WEBHOOKS = API_V1 + "/integration/webhooks";
    public static final String PLATFORM = API_V1 + "/platform";
    public static final String REPORTS_FLEET = API_V1 + "/reports/fleet";
    public static final String HEALTH = API_V1 + "/health";
}
```

Each domain controller updates its `@RequestMapping`:

```java
@RestController
@RequestMapping(ApiConstants.WORKSPACE_REQUIREMENTS)
public class RequirementController {
    /* methods unchanged in body; @PathVariable for resource id only */
}
```

The `{workspaceId}` path variable is consumed by the interceptor;
controllers do not declare it.

### Interceptor registration

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final WorkspaceContextInterceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns(
                    "/api/v1/auth/**",
                    "/api/v1/health",
                    "/api/v1/integration/webhooks/**",
                    "/api/v1/platform/**",
                    "/api/v1/reports/fleet/**"
                );
    }
}
```

### `WorkspaceContextInterceptor.preHandle`

```java
public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
    String path = req.getRequestURI();
    String workspaceId = extractWorkspaceId(path);                  // expects /api/v1/workspaces/{id}/...
    if (workspaceId == null) {
        throw new ResourceNotFoundException();                       // 404
    }
    CurrentUserDto user = authService.requireUser(req);

    if ("demo".equals(workspaceId)) {
        if (!"guest".equals(user.mode()) || !authProps.isDemoMode()) {
            throw new WorkspaceScopeException("WORKSPACE_SCOPE_REQUIRED");
        }
        WorkspaceContext ctx = resolver.demoContext();
        WorkspaceContextHolder.set(ctx);
        return true;
    }

    PlatformWorkspaceEntity ws = resolver.requireWorkspace(workspaceId);  // 404 if absent
    resolver.authorizeOrThrow(user, ws);                                  // 403 if no scope
    WorkspaceContextHolder.set(resolver.toContext(ws));
    return true;
}

public void afterCompletion(...) {
    WorkspaceContextHolder.clear();
}
```

### Hibernate `@Filter`

Defined once on a "package-info" or shared annotation source:

```java
@FilterDef(name = "workspace_filter", parameters = @ParamDef(name = "workspaceId", type = String.class))
package com.sdlctower.shared.persistence;
```

Each scoped entity:

```java
@Entity
@Filter(name = "workspace_filter", condition = "workspace_id = :workspaceId")
public class RequirementEntity {
    @Column(name = "workspace_id", nullable = false)
    private String workspaceId;
    /* ... */
}
```

Aspect:

```java
@Aspect
@Component
public class WorkspaceFilterAspect {

    private final EntityManagerFactory emf;

    @Around("@within(org.springframework.transaction.annotation.Transactional) || @annotation(org.springframework.transaction.annotation.Transactional)")
    public Object filter(ProceedingJoinPoint pjp) throws Throwable {
        Optional<WorkspaceContext> ctx = WorkspaceContextHolder.maybeCurrent();
        boolean cross = WorkspaceContextHolder.isCrossWorkspaceAccess();
        Session session = emf.createEntityManager().unwrap(Session.class);
        if (ctx.isPresent() && !cross) {
            session.enableFilter("workspace_filter").setParameter("workspaceId", ctx.get().workspaceId());
        } else {
            session.disableFilter("workspace_filter");
        }
        try {
            return pjp.proceed();
        } finally {
            // session lifecycle is managed by Spring; do not close here
        }
    }
}
```

> The exact aspect implementation may differ: in many Spring Boot apps
> the simpler choice is a `HibernateInterceptor` registered through a
> `HibernatePropertiesCustomizer`. Either is acceptable; the contract is
> that `workspace_filter` is enabled on the session iff the holder is
> set and the cross-flag is OFF.

### Cross-workspace escape hatch

```java
public static <T> T runWithCrossWorkspaceAccess(WorkspaceCrossAccessReason reason, Supplier<T> action) {
    boolean prior = isCrossWorkspaceAccess();
    setCrossFlag(true);
    log.info("workspace.cross.access reason={} staffId={} from={}",
        reason.code(),
        maybeCurrent().map(WorkspaceContext::staffId).orElse("system"),
        maybeCurrent().map(WorkspaceContext::workspaceId).orElse("none"));
    try {
        return action.get();
    } finally {
        setCrossFlag(prior);
    }
}
```

### Webhook resolver pattern

```java
@PostMapping("/jenkins")
public ResponseEntity<Void> jenkinsWebhook(@RequestBody String body, @RequestHeader("X-Hub-Signature-256") String sig) {
    verifyHmac(body, sig);
    JenkinsPayload payload = parse(body);

    String workspaceId = WorkspaceContextHolder.runWithCrossWorkspaceAccess(
        WorkspaceCrossAccessReason.WEBHOOK_JENKINS_INGEST,
        () -> jenkinsInstanceRepository.findByExternalKey(payload.instanceId())
                .map(JenkinsInstanceEntity::getWorkspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Unknown Jenkins instance"))
    );

    WorkspaceContext ctx = resolver.toContext(workspaceId);
    WorkspaceContextHolder.set(ctx);
    try {
        deploymentService.ingest(payload);
    } finally {
        WorkspaceContextHolder.clear();
    }
    return ResponseEntity.accepted().build();
}
```

---

## 4. Frontend Integration

### `apiClient`

See [design](../multi-tenancy-foundation-design.md#apiclient-interceptor)
for the interceptor body. Key behaviors:

- Allowlist matched against URL prefix; matches skip rewrite
- `NO_WORKSPACE_SELECTED` thrown synchronously if `workspaceId` empty;
  caller catches and shows the shell overlay
- The base URL `/api/v1` is configured separately so the interceptor
  only injects the workspace path

### `workspaceApi`

```ts
export const workspaceApi = {
  async listMyWorkspaces(): Promise<Workspace[]> {
    const res = await apiClient.get<ApiResponse<Workspace[]>>('/auth/workspaces');
    return res.data.data;
  },

  async switchWorkspace(workspaceId: string): Promise<WorkspaceContextDto> {
    const res = await apiClient.post<ApiResponse<WorkspaceContextDto>>('/auth/workspace', { workspaceId });
    return res.data.data;
  },

  async resolveByKey(key: string): Promise<KeyResolution> {
    try {
      const res = await apiClient.get<ApiResponse<Workspace>>(`/auth/workspaces/by-key/${encodeURIComponent(key)}`, {
        validateStatus: (s) => s === 200 || s === 301,
        maxRedirects: 0,
      });
      if (res.status === 301) {
        const newKey = extractKeyFromLocation(res.headers.location);
        return { kind: 'redirect', newKey };
      }
      return { kind: 'ok', workspace: res.data.data };
    } catch (e) {
      throw asApiError(e);
    }
  },
};

type KeyResolution =
  | { kind: 'ok'; workspace: Workspace }
  | { kind: 'redirect'; newKey: string };
```

### Vue Router rebase

```ts
const routes: RouteRecordRaw[] = [
  { path: '/login', component: LoginView },
  { path: '/no-access', component: NoAccessView },
  { path: '/demo', redirect: '/demo/dashboard' },
  { path: '/demo/:feature(.*)*', component: DemoShell },
  { path: '/platform/:feature(.*)*', component: PlatformAdminShell, beforeEnter: requirePlatformAdmin },

  {
    path: '/:workspaceKey',
    component: WorkspaceShell,
    beforeEnter: resolveWorkspaceKey,
    children: [
      { path: '', redirect: { name: 'dashboard' } },
      { path: 'dashboard', name: 'dashboard', component: DashboardView },
      { path: 'requirements', name: 'requirements', component: RequirementListView },
      { path: 'requirements/:requirementId', name: 'requirement-detail', component: RequirementManagementView },
      { path: 'incidents', name: 'incidents', component: IncidentListView },
      /* ... 13 features ... */
    ],
  },

  { path: '/', redirect: () => {
      const ws = useWorkspaceStore();
      const fallback = ws.workspaces[0]?.workspaceKey;
      return fallback ? `/${fallback}/dashboard` : '/login';
    },
  },
  { path: '/:pathMatch(.*)*', redirect: '/' },
];
```

### `workspaceStore` extensions

```ts
export const useWorkspaceStore = defineStore('workspace', () => {
  const workspaces = ref<Workspace[]>([]);
  const context = ref<WorkspaceContext>(emptyContext());
  const loading = ref(false);

  async function load(): Promise<void> {
    loading.value = true;
    try {
      workspaces.value = await workspaceApi.listMyWorkspaces();
    } finally {
      loading.value = false;
    }
  }

  function setActive(workspaceId: string) {
    const ws = workspaces.value.find(w => w.workspaceId === workspaceId);
    if (!ws) throw new Error(`unknown workspace ${workspaceId}`);
    context.value = toContext(ws);
    persistSelection(ws);
  }

  async function switchTo(workspaceId: string) {
    await workspaceApi.switchWorkspace(workspaceId);
    setActive(workspaceId);
  }

  return { workspaces, context, loading, load, setActive, switchTo };
});
```

---

## 5. Testing Contracts

### Backend

```java
@SpringBootTest
class WorkspaceContextResolutionTest {
    @Test void rejects_anonymous();
    @Test void rejects_non_prefixed_path();
    @Test void rejects_unknown_workspace();
    @Test void allows_authorized_user();
    @Test void rejects_user_lacking_scope();
    @Test void allows_demo_for_guest();
    @Test void rejects_demo_for_staff();
}

@SpringBootTest
class RepositoryAutoFilterTest {
    @Test void requirement_isolation_between_two_workspaces();
    @Test void agent_run_isolation();
    @Test void source_reference_isolation();
    @Test void cross_workspace_access_disables_filter();
    @Test void missing_context_throws();
    @Test void each_slice_representative_entity_is_isolated();   // parameterized over 13 slices
}

class AllWorkspaceScopedEntitiesAreFiltered {
    @Test void every_entity_with_workspace_id_carries_filter_annotation();
}

class AuthWorkspaceControllerTest {
    @Test void switch_happy_path_writes_audit_and_cookie();
    @Test void switch_returns_403_when_user_lacks_scope();
    @Test void list_returns_intersection_of_user_scopes();
    @Test void by_key_returns_alias_redirect_within_30_days();
    @Test void by_key_returns_404_after_alias_expiry();
}

class JenkinsWebhookWorkspaceResolutionTest {
    @Test void payload_routes_to_correct_workspace();
    @Test void unknown_instance_returns_404();
}
```

### Frontend

```ts
describe('apiClient interceptor', () => {
  it('rewrites domain URLs with workspace prefix');
  it('passes allowlisted URLs through');
  it('throws NO_WORKSPACE_SELECTED when context is empty');
});

describe('resolveWorkspaceKey guard', () => {
  it('uses cached workspace on hit');
  it('calls by-key on cold');
  it('follows 301 alias');
  it('redirects to no-access on 404');
  it('redirects to login on 401');
});

describe('WorkspaceSwitcher', () => {
  it('lists my workspaces sorted by name');
  it('shows active workspace with accent bar');
  it('triggers POST /auth/workspace and router.push on select');
  it('reverts dropdown on 403');
  it('hides under /demo route');
});
```

### End-to-end (Playwright)

```ts
test('IBM-i Day 1 — Jira → CLI prompt for functional spec', async ({ page }) => {
  await loginAs(page, '43929999');
  await expect(page).toHaveURL(/\/ibm-i-team\/dashboard$/);

  await page.click('a[href="/ibm-i-team/requirements"]');
  await page.click('a[href="/ibm-i-team/requirements/REQ-IBMI-0001"]');

  await page.click('button:has-text("Prepare Prompt")');
  const prompt = await page.locator('.prompt-box code').textContent();
  expect(prompt).toContain('ibm-i-functional-spec');
  expect(prompt).toContain('REQ-IBMI-0001');

  // Verify the AgentRun row was scoped correctly
  const runRow = await dbQueryFirst('SELECT workspace_id FROM requirement_agent_run WHERE requirement_id = ?', 'REQ-IBMI-0001');
  expect(runRow.workspace_id).toBe('ws-ibm-i-team');
});

test('Cross-workspace leakage attempt', async ({ page }) => {
  await loginAs(page, '43920001');                      // member of both seeded workspaces
  await page.goto('/payment-gateway-pro/requirements');
  const beforeIds = await page.locator('[data-test=req-row]').allTextContents();

  await page.click('button[data-test=switch]');
  await page.click('text=IBM-i Team');
  await expect(page).toHaveURL(/\/ibm-i-team\/requirements$/);
  const afterIds = await page.locator('[data-test=req-row]').allTextContents();

  expect(beforeIds).not.toEqual(afterIds);
  expect(afterIds).toContain('REQ-IBMI-0001');
});
```

---

## 6. Versioning Policy

This slice is breaking. There is no `v0` shim for old non-prefixed
URLs. Frontend, backend, tests, seed data, and any Codex-generated code
must move together. Webhook URLs, auth URLs, `/health`, and
`/api/v1/platform/*` are unchanged.
