# Shared App Shell Architecture

## Purpose

This document defines the technical boundary for implementing the shared shell
as the first foundation slice.

## 1. Architecture Goal

Create one reusable shell that all Round 1 pages can mount into, instead of
copying layout structure per page.

## 2. System Context

The shared app shell is the outermost UI frame of the Agentic SDLC Control Tower
web application. It runs in the browser and is served by a Vue 3 SPA. In the full
system, the SPA communicates with a Spring Boot backend that persists data in
Oracle (production) or H2 (local development and testing).

For this foundation slice, the shell has no backend dependency. All context data
is static or mocked. Backend integration is introduced in later slices.

## 3. Technology Stack

| Layer | Technology | Rationale |
|-------|-----------|-----------|
| Frontend framework | Vue 3 (Composition API, `<script setup>`) | PRD specifies Vue 3; Composition API enables composable-based state sharing |
| Build tool | Vite | Standard Vue 3 toolchain; fast HMR for development |
| Routing | Vue Router | Standard SPA routing for Vue; supports route metadata for nav mapping |
| Client state | Pinia | Official Vue state management; supports module-scoped stores |
| Backend framework | Spring Boot (Java 21) | Enterprise-grade, team familiarity, ecosystem maturity |
| ORM | JPA / Hibernate | Standard Spring Boot data access layer |
| Database (production) | Oracle | Enterprise requirement |
| Database (local/test) | H2 (in-memory) | Zero-setup local testing via Spring profiles |

Note: The backend stack is recorded here for full-system context. This shell
slice does not implement any backend components.

## 4. Proposed Component Breakdown

Shell components:

- App Shell (root layout frame)
- Primary Navigation
- Top Context Bar
- Global Action Bar
- AI Command Panel

Page view components consume the shell:

- Dashboard View
- Project Space View
- Incident Detail View
- Platform Center View

Concrete file names and component APIs are defined in the design document.

## 5. Responsibility Boundaries

### App Shell

Owns:

- page frame composition
- layout slot structure
- placement of nav, top bar, content, and AI panel

Does not own:

- business content for a specific page
- page-specific metrics, tables, or workflows

### Primary Navigation

Owns:

- rendering of navigation entries
- active item visualization
- route-to-nav-key mapping input

Does not own:

- permission policy logic in V1 foundation

### Top Context Bar

Owns:

- display of normalized workspace context
- layout-safe fallback rendering for missing fields

Does not own:

- source-of-truth fetching logic beyond receiving prepared context state

### AI Command Panel

Owns:

- persistent panel container
- structural sections for summary, reasoning, evidence, and actions

Does not own:

- page-specific AI logic
- global AI administration behavior

## 6. Data Flow

Baseline flow:

1. Route selects current page view.
2. Page view provides shell config such as title and nav key.
3. Shared context source provides `WorkspaceContext`.
4. Shell renders shared frame.
5. Page body renders inside shell content region.
6. Page-scoped AI content renders inside AI panel container.

## 7. State Boundaries

Shared shell state:

- current route key
- normalized workspace context
- utility visibility state
- AI panel open content state if needed in later slices

Page-local state:

- page business data
- page module loading/error state
- page-specific AI content payload

## 8. Integration Notes

This slice should avoid coupling shell rendering to live backend contracts.

Preferred approach:

- use static or mocked `WorkspaceContext`
- use route metadata or page config for nav key and title
- keep external integration concerns outside the shell foundation

## 9. Security Considerations

- The shell itself does not enforce authentication or authorization in V1
- Authentication and session management will be handled by the Spring Boot backend in later slices
- The shell must not expose workspace-scoped data without backend-enforced context validation
- Navigation entries render the full set in V1; permission-based filtering is deferred but the architecture must not make it structurally difficult to add

## 10. Risks

- If shell components fetch their own business data, boundaries will blur
- If pages own shell structure, layout duplication will return quickly
- If AI panel behavior is coupled too early to one page, reuse will weaken
- If the technology stack is changed after the shell is built, the shell abstraction should remain portable (avoid deep coupling to a specific UI library beyond Vue 3 core)

## 11. Architecture Exit Criteria

This slice is ready to implement when:

- component ownership is accepted
- route-to-nav mapping is accepted
- shared context shape is accepted
- page views are confirmed to render through one shell abstraction
