# Multi-Tenancy Foundation Data Flow

## Purpose

Document every runtime data flow that touches the workspace boundary so
implementers (and Codex) have a single reference for the order of
operations, the location of state, and where errors fall.

## Source

- [multi-tenancy-foundation-architecture.md](multi-tenancy-foundation-architecture.md)
- [multi-tenancy-foundation-spec.md](../03-spec/multi-tenancy-foundation-spec.md)

---

## 1. Cold Login → First Page Load

```mermaid
sequenceDiagram
    participant U as User
    participant B as Browser
    participant SHELL as Shell.vue
    participant SESS as sessionStore
    participant WS as workspaceStore
    participant API as apiClient
    participant BE as Backend

    U->>B: open /payment-gateway-pro/dashboard
    B->>SHELL: mount
    SHELL->>SESS: init()
    SESS->>API: GET /auth/me
    API->>BE: dispatch
    BE-->>API: { staffId, scopes, mode, demoMode }
    API-->>SESS: CurrentUser
    SHELL->>WS: load()
    WS->>API: GET /auth/workspaces
    API->>BE: dispatch
    BE-->>API: [ ws-default-001, ws-ibm-i-team ]
    API-->>WS: workspaces
    SHELL->>WS: resolveKey("payment-gateway-pro")
    WS-->>SHELL: ws-default-001 (cache hit)
    SHELL->>WS: setActive(ws-default-001)
    SHELL->>API: GET /dashboard/summary
    API->>API: rewrite -> /workspaces/ws-default-001/dashboard/summary
    API->>BE: dispatch (cookie + path)
    BE-->>API: dashboard data
    API-->>SHELL: render
```

Key invariants:

- `GET /auth/me` and `GET /auth/workspaces` are the **only** un-prefixed
  domain calls before workspace resolution
- After resolution, every domain call carries the path prefix
- A failed `resolveKey` redirects to `/no-access` before any domain call

## 2. Workspace Switch

```mermaid
stateDiagram-v2
    [*] --> Idle: workspace = ws-A
    Idle --> Selecting: user picks ws-B
    Selecting --> Authorizing: POST /auth/workspace
    Authorizing --> Idle: 403 (revert dropdown)
    Authorizing --> Routing: 200 { workspaceKey, ... }
    Routing --> Refetching: router.push(/keyB/<feature>)
    Refetching --> Idle: workspace = ws-B
    Refetching --> Idle: data loaded
```

Sequence detail:

1. User clicks new workspace in `WorkspaceSwitcher`
2. `apiClient.post('/auth/workspace', { workspaceId })` (allowlisted, no
   prefix injection)
3. Backend writes audit `workspace.switch`, sets cookie, returns key
4. Frontend updates `workspaceStore.context`
5. `localStorage.workspace.{staffId}` updated
6. Vue Router `push('/{newKey}/{currentFeature}')`
7. Parent route guard `resolveWorkspaceKey` is a cache hit; sets context
8. Child view re-mounts (router `key` rotation), triggers fresh fetch

## 3. Hard Reload of `/{key}/<feature>`

```mermaid
sequenceDiagram
    participant B as Browser
    participant ROUTER as Vue Router
    participant GUARD as resolveWorkspaceKey
    participant WS as workspaceStore
    participant API
    participant BE

    B->>ROUTER: GET /ibm-i-team/requirements
    ROUTER->>GUARD: beforeEnter
    GUARD->>WS: maybeContextForKey("ibm-i-team")
    WS-->>GUARD: null (cold)
    GUARD->>API: GET /auth/workspaces/by-key/ibm-i-team
    API->>BE: dispatch
    alt key valid + authorized
        BE-->>API: 200 row
        GUARD->>WS: setActive(ws-ibm-i-team)
        GUARD-->>ROUTER: next()
    else key valid + unauthorized
        BE-->>API: 403
        GUARD-->>ROUTER: next('/no-access?attempted=ibm-i-team')
    else key alias hit
        BE-->>API: 301 redirectTo=ibm-i-team-v2
        GUARD-->>ROUTER: next('/ibm-i-team-v2/requirements', replace=true)
    else key unknown
        BE-->>API: 404
        GUARD-->>ROUTER: next('/no-access?attempted=ibm-i-team')
    end
```

## 4. Backend Request Lifecycle (filter + holder)

```mermaid
sequenceDiagram
    participant CLI as HTTP request
    participant ICEPT as WorkspaceContextInterceptor
    participant HOLDER as WorkspaceContextHolder
    participant ASPECT as WorkspaceFilterAspect
    participant TX as @Transactional method
    participant SESS as Hibernate Session
    participant DB

    CLI->>ICEPT: preHandle(req)
    ICEPT->>ICEPT: parse path, auth, resolve, authorize
    ICEPT->>HOLDER: set(ctx)
    ICEPT-->>CLI: continue
    CLI->>TX: controller -> service
    TX->>ASPECT: around()
    ASPECT->>HOLDER: maybeCurrent()
    alt holder set, cross-flag OFF
        ASPECT->>SESS: enableFilter("workspace_filter").setParameter("workspaceId", ctx.workspaceId)
    else holder set, cross-flag ON
        ASPECT->>SESS: disableFilter("workspace_filter")
    else holder empty
        ASPECT->>SESS: leave disabled (queries throw via canary check)
    end
    TX->>DB: SELECT ...
    DB-->>TX: rows
    TX-->>CLI: response
    ICEPT->>HOLDER: afterCompletion clear()
```

Notes:

- The aspect runs once per `@Transactional` boundary, not per query, so
  the filter persists for the duration of the transaction
- The aspect is idempotent — re-entering a `@Transactional` method does
  not double-toggle the filter
- A canary `@PrePersist` listener fails fast if a workspace-scoped row
  is inserted with a null `workspace_id`

## 5. Webhook Resolution

```mermaid
sequenceDiagram
    participant J as Jenkins
    participant CTRL as JenkinsWebhookController
    participant HOLDER as WorkspaceContextHolder
    participant REPO as JenkinsInstanceRepository
    participant SVC as DeploymentService
    participant DB

    J->>CTRL: POST /integration/webhooks/jenkins (HMAC)
    CTRL->>CTRL: verifyHmac()
    CTRL->>HOLDER: runWithCrossWorkspaceAccess("webhook.jenkins.ingest")
    note over HOLDER: cross-flag ON, filter disabled
    CTRL->>REPO: findByExternalKey(payload.instanceId)
    REPO->>DB: SELECT (no workspace filter)
    DB-->>REPO: jenkins_instance row
    REPO-->>CTRL: { workspace_id: ws-ibm-i-team }
    CTRL->>HOLDER: set(WorkspaceContext{ws-ibm-i-team})
    note over HOLDER: cross-flag OFF, filter ON
    CTRL->>SVC: ingestBuild(payload)
    SVC->>DB: INSERT dp_release (workspace_id auto-set)
    CTRL->>HOLDER: clear()
    CTRL-->>J: 202
```

Failure: if `findByExternalKey` returns nothing → `404` + audit
`webhook.unknown_source`; no domain write occurs.

## 6. Demo / Guest Path

```mermaid
sequenceDiagram
    participant U as Anonymous
    participant B as Browser
    participant SESS as sessionStore
    participant WS as workspaceStore
    participant API
    participant BE

    U->>B: open /
    B->>SESS: init()
    SESS->>API: GET /auth/me
    API->>BE: (no cookie)
    BE-->>API: 401
    SESS->>API: POST /auth/guest
    API->>BE: dispatch
    BE-->>API: { mode: "guest", scopes: [] }
    SESS-->>B: guest user
    B->>B: router.push("/demo/dashboard")
    B->>API: GET /dashboard/summary
    API->>API: rewrite -> /workspaces/demo/dashboard/summary
    API->>BE: dispatch
    BE->>BE: workspaceId == "demo" + guest + demoMode -> resolve from workspace_context
    BE-->>API: dashboard data (ws-default-001 content)
    API-->>B: render
```

Guards:

- The synthetic `demo` id never exists in `PLATFORM_WORKSPACE`
- A staff user hitting `/api/v1/workspaces/demo/...` gets `403` and
  redirect to their default workspace
- Workspace switcher hidden under `/demo/*`

## 7. Error Cascade

```mermaid
flowchart TD
    REQ[HTTP request] --> ALLOW{allowlisted?}
    ALLOW -- yes --> ALLOWHANDLE[per-path auth]
    ALLOW -- no --> SHAPE{path matches /workspaces/{id}/...?}
    SHAPE -- no --> R404a[404]
    SHAPE -- yes --> COOKIE{has session?}
    COOKIE -- no --> R401[401 AUTH_REQUIRED]
    COOKIE -- yes --> DEMO{demo + guest + demoMode?}
    DEMO -- yes --> RESOLVED[ctx from workspace_context]
    DEMO -- no --> LOOKUP{PLATFORM_WORKSPACE.findById hit?}
    LOOKUP -- no --> R404b[404 WORKSPACE_NOT_FOUND]
    LOOKUP -- yes --> SCOPE{scope match?}
    SCOPE -- no --> R403[403 WORKSPACE_SCOPE_REQUIRED + audit access_denied]
    SCOPE -- yes --> RESOLVED
    RESOLVED --> CTRL[controller]
    CTRL --> ASPECT{filter aspect}
    ASPECT --> RESULT[200 / domain error]
```

## 8. Refresh Strategy

| Cache | TTL | Invalidated by |
|---|---|---|
| `workspaceById` | 60 s | `PLATFORM_WORKSPACE` insert / update / delete |
| `workspaceByKey` | 60 s | same + alias insert |
| `projectToWorkspace` | 60 s | `project` insert / update of `workspace_id` |
| Frontend `workspaceStore.workspaces` | session | login, workspace.switch, manual reload |
| Frontend `localStorage.workspace.{staffId}` | 30 days | switch, logout |

Stale-cache safety: cache hits are validated against the request's
scope at every interceptor pass. A stale row that no longer matches the
user's scopes will fail authorization, not silently authorize.

## 9. Async / Background Tasks

`@Async` methods that read or write workspace-scoped data inherit the
parent request's `WorkspaceContext` via a Spring `TaskDecorator`:

```mermaid
sequenceDiagram
    participant REQ as HTTP request
    participant HOLDER as WorkspaceContextHolder
    participant DECO as TaskDecorator
    participant ASYNC as @Async method
    participant DB

    REQ->>HOLDER: set(ctx)
    REQ->>DECO: submit(task)
    DECO->>DECO: capture holder snapshot
    DECO->>ASYNC: run on pool thread
    ASYNC->>HOLDER: set(snapshot)
    ASYNC->>DB: SELECT (filter active)
    ASYNC->>HOLDER: clear()
    REQ->>HOLDER: clear()
```

Background scheduled jobs (`@Scheduled`) without a request context must
explicitly call `runWithCrossWorkspaceAccess` for system-wide work, or
loop over `PLATFORM_WORKSPACE` and `set` per workspace for per-tenant work.
