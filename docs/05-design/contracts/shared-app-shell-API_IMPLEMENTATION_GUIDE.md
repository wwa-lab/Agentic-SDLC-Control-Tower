# Shared App Shell API Implementation Guide

## Purpose

This document is the single source of truth for implementing the Shared App Shell APIs.
It defines endpoint contracts, request/response shapes, error handling, and integration
patterns for both frontend and backend developers.

## Traceability

- Spec: [shared-app-shell-spec.md](../../03-spec/shared-app-shell-spec.md)
- Architecture: [shared-app-shell-architecture.md](../../04-architecture/shared-app-shell-architecture.md)
- Design: [shared-app-shell-design.md](../shared-app-shell-design.md)
- Data Model: [shared-app-shell-data-model.md](../../04-architecture/shared-app-shell-data-model.md)
- Data Flow: [shared-app-shell-data-flow.md](../../04-architecture/shared-app-shell-data-flow.md)

---

## 1. API Overview

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/auth/providers` | Discover enabled auth providers |
| POST | `/api/v1/auth/login` | Staff-id login |
| GET | `/api/v1/auth/sso/teambook/start` | Start internal TeamBook SSO flow when enabled |
| GET | `/api/v1/auth/sso/teambook/callback` | Internal TeamBook callback |
| POST | `/api/v1/auth/guest` | Start guest read-only session |
| GET | `/api/v1/auth/me` | Current user identity, mode, roles, scopes |
| POST | `/api/v1/auth/logout` | End current session |
| GET | `/api/v1/workspace-context` | Fetch current workspace context |
| GET | `/api/v1/nav/entries` | Fetch ordered navigation items |
| GET | `/api/v1/shell/help-links` | Fetch configured Confluence guideline URL |
| POST | `/api/v1/support/contact` | Raise Contact Us issue as Jira story |
| GET | `/actuator/health` | Spring Boot health check |

### Base URL

- Local dev: `http://localhost:8080/api/v1`
- Frontend proxy (Vite): `/api/v1` → `http://localhost:8080/api/v1`
- API path constants: `com.sdlctower.shared.ApiConstants`

---

## 2. GET /api/v1/workspace-context

### 2.1 Request

```
GET /api/v1/workspace-context
Accept: application/json
```

**Query Parameters**: None.

**Headers**: authenticated session cookie or guest session cookie. Guest sessions return demo context.

### 2.2 Success Response (200 OK)

```json
{
  "data": {
    "workspaceId": "ws-default-001",
    "workspace": "Global SDLC Tower",
    "applicationId": "app-payment-gateway-pro",
    "application": "Payment-Gateway-Pro",
    "snowGroupId": "snow-fin-tech-ops",
    "snowGroup": "FIN-TECH-OPS",
    "projectId": "proj-42",
    "project": "Q2-Cloud-Migration",
    "environment": "Production",
    "demoMode": false
  },
  "error": null
}
```

### 2.3 Partial Context (200 OK)

Optional fields may be null:

```json
{
  "data": {
    "workspace": "Global SDLC Tower",
    "application": "Payment-Gateway-Pro",
    "snowGroup": null,
    "project": null,
    "environment": null
  },
  "error": null
}
```

### 2.4 Not Found Response (404)

When no workspace context is seeded:

```json
{
  "data": null,
  "error": "Workspace context not found"
}
```

### 2.5 Field Contract

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| `workspaceId` | string | Yes | Canonical workspace id |
| `workspace` | string | No | Workspace name |
| `applicationId` | string | Yes | Canonical application id |
| `application` | string | No | Application name |
| `snowGroupId` | string | Yes | Canonical SNOW group id |
| `snowGroup` | string | Yes | ServiceNow group |
| `projectId` | string | Yes | Canonical project id |
| `project` | string | Yes | Project name |
| `environment` | string | Yes | Environment name |
| `demoMode` | boolean | No | True when guest/demo dataset is active |

---

## 2A. Authentication APIs

### GET /api/v1/auth/providers

Returns auth providers enabled for the current environment. Local/external
environments can omit TeamBook.

```json
{
  "data": [
    {
      "provider": "teambook",
      "label": "TeamBook SSO",
      "enabled": true,
      "startUrl": "/api/v1/auth/sso/teambook/start"
    },
    {
      "provider": "manual",
      "label": "Staff ID",
      "enabled": true,
      "startUrl": null
    },
    {
      "provider": "guest",
      "label": "Guest",
      "enabled": true,
      "startUrl": null
    }
  ],
  "error": null
}
```

### POST /api/v1/auth/login

```json
{
  "staffId": "43910516",
  "password": "non-empty"
}
```

Validation:

- `staffId` required
- `password` required and non-empty
- password is never returned in any response

**Response 200:**

```json
{
  "data": {
    "mode": "staff",
    "authProvider": "manual",
    "staffId": "43910516",
    "displayName": "Staff 43910516",
    "staffName": null,
    "avatarUrl": null,
    "roles": ["WORKSPACE_VIEWER"],
    "readOnly": false,
    "scopes": [
      { "scopeType": "application", "scopeId": "app-payment-gateway-pro" },
      { "scopeType": "snow_group", "scopeId": "snow-fin-tech-ops" }
    ]
  },
  "error": null
}
```

### GET /api/v1/auth/sso/teambook/start

Internal-only endpoint. Redirects the browser to TeamBook SSO when the provider
is enabled. If TeamBook is disabled in the current environment, returns `404` or
an auth-provider-disabled error.

### GET /api/v1/auth/sso/teambook/callback

Internal-only callback. The backend validates the TeamBook response, normalizes
the profile to staff id, nStaff Name, and avatar URL, then checks the staff id
against active `PlatformUser` records. TeamBook authentication alone never grants
Application + SNOW access.

**Redirect on success:** `/`

**Redirect on provision failure:** `/login?error=user_not_provisioned`

### POST /api/v1/auth/guest

Starts a guest read-only session.

**Response 200:**

```json
{
  "data": {
    "mode": "guest",
    "authProvider": "guest",
    "staffId": null,
    "displayName": "Guest",
    "staffName": null,
    "avatarUrl": null,
    "roles": ["GUEST"],
    "readOnly": true,
    "scopes": [
      { "scopeType": "demo", "scopeId": "public-demo" }
    ]
  },
  "error": null
}
```

### GET /api/v1/auth/me

Returns the current authenticated or guest identity. If no session exists, returns
401 and the frontend routes to the login screen.

### POST /api/v1/auth/logout

Invalidates the current staff or guest session.

```json
{
  "data": {
    "loggedOut": true
  },
  "error": null
}
```

---

## 2B. Help And Support APIs

### GET /api/v1/shell/help-links

```json
{
  "data": {
    "userGuidelineUrl": "https://confluence.company.com/display/SDLC/User+Guideline"
  },
  "error": null
}
```

### POST /api/v1/support/contact

Creates a Jira story in the configured support project.

```json
{
  "title": "Cannot find deployment dashboard",
  "category": "question",
  "description": "I need help locating deployment status.",
  "route": "/deployment",
  "context": {
    "workspaceId": "ws-default-001",
    "workspace": "Global SDLC Tower",
    "applicationId": "app-payment-gateway-pro",
    "application": "Payment-Gateway-Pro",
    "snowGroupId": "snow-fin-tech-ops",
    "snowGroup": "FIN-TECH-OPS",
    "projectId": "proj-42",
    "project": "Q2-Cloud-Migration",
    "environment": "Production",
    "demoMode": false
  },
  "reporterStaffId": "43910516",
  "reporterMode": "staff"
}
```

**Response 201:**

```json
{
  "data": {
    "requestId": "support-req-20260502-0001",
    "status": "created",
    "jiraKey": "SDLC-1234",
    "jiraUrl": "https://jira.company.com/browse/SDLC-1234"
  },
  "error": null
}
```

If Jira is unavailable, the backend persists the request and returns `202`:

```json
{
  "data": {
    "requestId": "support-req-20260502-0002",
    "status": "pending",
    "jiraKey": null,
    "jiraUrl": null
  },
  "error": null
}
```

---

## 3. GET /api/v1/nav/entries

### 3.1 Request

```
GET /api/v1/nav/entries
Accept: application/json
```

**Query Parameters**: None.

### 3.2 Success Response (200 OK)

```json
{
  "data": [
    {
      "key": "dashboard",
      "label": "Dashboard",
      "path": "/",
      "comingSoon": false,
      "icon": "LayoutDashboard",
      "order": 1
    },
    {
      "key": "team",
      "label": "Team Space",
      "path": "/team",
      "comingSoon": true,
      "icon": "Users",
      "order": 2
    },
    {
      "key": "project-space",
      "label": "Project Space",
      "path": "/project-space",
      "comingSoon": false,
      "icon": "FolderKanban",
      "order": 3
    },
    {
      "key": "requirements",
      "label": "Requirement Mgmt",
      "path": "/requirements",
      "comingSoon": true,
      "icon": "FileText",
      "order": 4
    },
    {
      "key": "project-management",
      "label": "Project Mgmt",
      "path": "/project-management",
      "comingSoon": true,
      "icon": "Kanban",
      "order": 5
    },
    {
      "key": "design",
      "label": "Design Mgmt",
      "path": "/design",
      "comingSoon": true,
      "icon": "Palette",
      "order": 6
    },
    {
      "key": "code",
      "label": "Code & Build",
      "path": "/code",
      "comingSoon": true,
      "icon": "Code",
      "order": 7
    },
    {
      "key": "testing",
      "label": "Testing",
      "path": "/testing",
      "comingSoon": true,
      "icon": "TestTube",
      "order": 8
    },
    {
      "key": "deployment",
      "label": "Deployment",
      "path": "/deployment",
      "comingSoon": true,
      "icon": "Rocket",
      "order": 9
    },
    {
      "key": "incidents",
      "label": "Incident Mgmt",
      "path": "/incidents",
      "comingSoon": false,
      "icon": "AlertTriangle",
      "order": 10
    },
    {
      "key": "ai-center",
      "label": "AI Center",
      "path": "/ai-center",
      "comingSoon": true,
      "icon": "Bot",
      "order": 11
    },
    {
      "key": "reports",
      "label": "Report Center",
      "path": "/reports",
      "comingSoon": true,
      "icon": "BarChart3",
      "order": 12
    },
    {
      "key": "platform",
      "label": "Platform Center",
      "path": "/platform",
      "comingSoon": false,
      "icon": "Settings",
      "order": 13
    }
  ],
  "error": null
}
```

### 3.3 NavItem Field Contract

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| `key` | string | No | Unique route key, maps to `route.meta.navKey` |
| `label` | string | No | Display label for nav item |
| `path` | string | No | Vue Router path |
| `comingSoon` | boolean | No | If true, nav item renders dimmed |
| `icon` | string | No | Lucide icon component name |
| `order` | int | No | Sort position for display |

---

## 4. GET /actuator/health

### 4.1 Request

```
GET /actuator/health
Accept: application/json
```

### 4.2 Response

```json
{
  "status": "UP"
}
```

Standard Spring Boot Actuator health endpoint. Used by the nav footer system status LED.

---

## 5. Shared Response Envelope

All shell APIs use the same `ApiResponse<T>` envelope:

```java
public record ApiResponse<T>(T data, String error) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(data, null);
    }
    public static <T> ApiResponse<T> fail(String error) {
        return new ApiResponse<>(null, error);
    }
}
```

**Invariant**: Exactly one of `data` or `error` is non-null.

The frontend `fetchJson<T>` client unwraps this envelope automatically:

```typescript
// client.ts — unwraps ApiResponse, throws on error
export async function fetchJson<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`);
  if (!response.ok) {
    throw new ApiError(response.status, response.statusText);
  }
  const envelope: ApiEnvelope<T> = await response.json();
  if (envelope.error) {
    throw new ApiError(response.status, envelope.error, envelope.error);
  }
  return envelope.data as T;
}
```

---

## 6. Backend Implementation Guide

### 6.1 Package Structure

```
backend/src/main/java/com/sdlctower/
├── shared/
│   ├── ApiConstants.java               # API path constants
│   ├── dto/
│   │   └── ApiResponse.java            # Shared response envelope
│   └── exception/
│       ├── ResourceNotFoundException.java
│       └── GlobalExceptionHandler.java
├── platform/
│   ├── workspace/
│   │   ├── WorkspaceContext.java        # JPA entity
│   │   ├── WorkspaceContextDto.java     # API-facing DTO
│   │   ├── WorkspaceContextRepository.java  # Spring Data JPA
│   │   ├── WorkspaceContextService.java
│   │   └── WorkspaceContextController.java
│   └── navigation/
│       ├── NavItem.java                 # Record (no entity)
│       ├── NavigationService.java       # Static list in V1
│       ├── NavigationController.java
│       └── NavigationProperties.java    # Future: externalize to config
├── config/
│   └── CorsConfig.java                 # CORS for localhost:5173
└── SdlcTowerApplication.java
```

### 6.2 API Constants

```java
public final class ApiConstants {
    private ApiConstants() {}

    public static final String API_V1 = "/api/v1";
    public static final String WORKSPACE_CONTEXT = API_V1 + "/workspace-context";
    public static final String NAV_ENTRIES = API_V1 + "/nav/entries";
}
```

All controllers use these constants — no magic strings in `@RequestMapping`.

### 6.3 Workspace Context Controller

```java
@RestController
@RequestMapping(ApiConstants.WORKSPACE_CONTEXT)
public class WorkspaceContextController {

    private final WorkspaceContextService workspaceContextService;

    public WorkspaceContextController(WorkspaceContextService workspaceContextService) {
        this.workspaceContextService = workspaceContextService;
    }

    @GetMapping
    public ApiResponse<WorkspaceContextDto> getWorkspaceContext() {
        return ApiResponse.ok(workspaceContextService.getCurrentWorkspaceContext());
    }
}
```

### 6.4 Navigation Controller

```java
@RestController
@RequestMapping(ApiConstants.NAV_ENTRIES)
public class NavigationController {

    private final NavigationService navigationService;

    public NavigationController(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    @GetMapping
    public ApiResponse<List<NavItem>> getEntries() {
        return ApiResponse.ok(navigationService.getEntries());
    }
}
```

---

## 7. Frontend Integration Guide

### 7.1 API Clients

```typescript
// frontend/src/shared/api/workspaceApi.ts
import { fetchJson } from './client';
import type { WorkspaceContext } from '@/shared/types/shell';

export function getWorkspaceContext(): Promise<WorkspaceContext> {
  return fetchJson<WorkspaceContext>('/workspace-context');
}
```

```typescript
// frontend/src/shared/api/navigationApi.ts
import { fetchJson } from './client';
import type { NavItem } from '@/shared/types/shell';

export function getNavigationEntries(): Promise<NavItem[]> {
  return fetchJson<NavItem[]>('/nav/entries');
}
```

### 7.2 Pinia Store

```typescript
// frontend/src/shared/stores/workspaceStore.ts
import { defineStore } from 'pinia';
import { ref, readonly } from 'vue';
import type { WorkspaceContext } from '@/shared/types/shell';
import { getWorkspaceContext } from '@/shared/api/workspaceApi';

export const useWorkspaceStore = defineStore('workspace', () => {
  const context = ref<WorkspaceContext>({
    workspace: '',
    application: '',
    snowGroup: null,
    project: null,
    environment: null
  });

  const loading = ref(false);
  const error = ref<string | null>(null);

  async function load(): Promise<void> {
    loading.value = true;
    error.value = null;
    try {
      context.value = await getWorkspaceContext();
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to load workspace context';
    } finally {
      loading.value = false;
    }
  }

  return {
    context: readonly(context),
    loading: readonly(loading),
    error: readonly(error),
    load,
  };
});
```

### 7.3 Vite Proxy Configuration

```typescript
// vite.config.ts
export default defineConfig({
  server: {
    proxy: {
      '/api/v1': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
});
```

---

## 8. CORS Configuration

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5173")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*");
    }
}
```

---

## 9. Testing Contracts

### 9.1 Backend Tests

```java
@WebMvcTest(WorkspaceContextController.class)
class WorkspaceContextControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean WorkspaceContextService service;

    @Test
    void getWorkspaceContext_returns200WithFullContext() throws Exception {
        when(service.getCurrentWorkspaceContext())
            .thenReturn(new WorkspaceContextDto(
                "Global SDLC Tower", "Payment-Gateway-Pro",
                "FIN-TECH-OPS", "Q2-Cloud-Migration", "Production"));

        mockMvc.perform(get("/api/v1/workspace-context"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.workspace").value("Global SDLC Tower"))
            .andExpect(jsonPath("$.data.application").value("Payment-Gateway-Pro"))
            .andExpect(jsonPath("$.data.snowGroup").value("FIN-TECH-OPS"))
            .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void getWorkspaceContext_handlesNullOptionalFields() throws Exception {
        when(service.getCurrentWorkspaceContext())
            .thenReturn(new WorkspaceContextDto(
                "Tower", "App", null, null, null));

        mockMvc.perform(get("/api/v1/workspace-context"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.snowGroup").doesNotExist())
            .andExpect(jsonPath("$.data.project").doesNotExist());
    }
}

@WebMvcTest(NavigationController.class)
class NavigationControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean NavigationService service;

    @Test
    void getEntries_returns13Items() throws Exception {
        when(service.getEntries()).thenReturn(/* 13-item list */);

        mockMvc.perform(get("/api/v1/nav/entries"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(13))
            .andExpect(jsonPath("$.data[0].key").value("dashboard"))
            .andExpect(jsonPath("$.data[0].comingSoon").value(false));
    }
}
```

### 9.2 Frontend Assertions

After loading workspace context:
- `context.workspace` is non-empty string
- `context.application` is non-empty string
- Null optional fields render as `---` in TopContextBar

After loading navigation entries:
- Exactly 13 items returned
- First item is `dashboard` with `comingSoon = false`
- Items are sorted by `order`

---

## 10. Versioning and Evolution

| Version | Changes |
|---------|---------|
| V1 (current) | Staff-id login, optional TeamBook provider contract, guest read-only mode, seeded/default workspace context, static nav items, Contact Us, User Guideline |
| V2 | Multi-workspace switching API, additional enterprise identity providers |
| V3 | Dynamic navigation driven by DB, permission-filtered |
| V4 | User preferences API (theme, layout, panel state) |

### Breaking Change Policy

- New nullable fields can be added to responses without version bump
- Removing or renaming fields requires a new API version
- Navigation item additions (new keys) are non-breaking
