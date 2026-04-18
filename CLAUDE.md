# CLAUDE.md

## Project

Agentic SDLC Control Tower — AI-native enterprise software delivery control tower.

- Frontend: Vue 3 / Vite / Vue Router / Pinia / TypeScript
- Backend: Spring Boot 3.x / Java 21 / JPA / H2 (local) / Oracle (prod)
- Dev model: SDD (Spec Driven Development) — documents before code
- Frontend dev tool: Gemini
- Backend dev tool: Codex
- Claude Code: SDD pipeline, doc quality gates, review, orchestration

## Current State (2026-04-18)

All 13 SDLC slices are implemented. The app is a fully navigable control tower with frontend views, backend APIs, Flyway migrations (V1-V77), and seeded local data for every domain. Platform Center backend is the only remaining planned work.

### Implemented Slices

| Slice | Frontend Feature | Backend Domain | Migrations |
|-------|-----------------|----------------|------------|
| Shared App Shell | `shell/` | `shared/` | V1-V2 |
| Dashboard | `features/dashboard` | `domain/dashboard` | V3 |
| Requirement Management | `features/requirement` | `domain/requirement` | V5-V6 |
| Incident Management | `features/incident` | `domain/incident` | V4 |
| Team Space | `features/team-space` | `domain/teamspace` | V7-V8 |
| Project Space | `features/project-space` | `domain/projectspace` | V9-V11 |
| Project Management | `features/project-management` | `domain/projectmanagement` | V20-V26 |
| Design Management | `features/design-management` | `domain/designmanagement` | V30-V36 |
| Code & Build | `features/code-build-management` | `domain/codebuildmanagement` | V40-V47 |
| Testing Management | `features/testing-management` | `domain/testingmanagement` | V50-V53 |
| AI Center | `features/ai-center` | `domain/aicenter` | V60-V61 |
| Deployment Management | `features/deployment-management` | `domain/deploymentmanagement` | V70-V77 |
| Report Center | `features/reportcenter` | `domain/reportcenter` | V37-V39 |
| Platform Center | `features/platform` | -- (planned) | -- |

### Backend Conventions

- Package-by-feature: `com.sdlctower.domain.{slice}/{controller,service,persistence,dto,policy,...}`
- Bean name conflicts: when class names collide across slices (e.g. `CatalogService`), use `@Service("slicePrefixClassName")`
- All endpoints return `ApiResponse<T>` envelope; card-based views use `SectionResultDto<T>`
- Flyway migration numbering: V1-V11 (foundation), V20-V26 (project mgmt), V30-V36 (design), V37-V39 (reports), V40-V47 (code & build), V50-V53 (testing), V60-V61 (AI center), V70-V77 (deployment)
- Table name prefix: `dp_` for deployment management tables (avoids reserved word conflicts with `release`, `deploy`, etc.)

## Lessons Learned (Session 2026-04-18)

### 10. Bean name conflicts require explicit naming when class names collide across slices

**What happened:** Multiple slices define classes with the same simple name (`CatalogService`, `TraceabilityService`, `AiAutonomyPolicy`, `LogRedactor`). Spring's default bean naming (`catalogService`) causes `ConflictingBeanDefinitionException` at startup because the component scan finds two beans with the same name in different packages.

**Rule:** When adding a `@Service`, `@Component`, or similar annotation to a class whose simple name already exists in another domain package, always provide an explicit bean name prefixed with the slice: `@Service("deploymentCatalogService")`. Check for conflicts by searching `public class {ClassName}` across `src/main/java/com/sdlctower/domain/` before creating new beans.

### 11. Flyway migration version numbers must not collide with existing migrations

**What happened:** The tasks doc specified V60-V67 for Deployment Management, but V60-V61 were already taken by AI Center. Using duplicate version numbers causes Flyway to fail at startup.

**Rule:** Before assigning migration version numbers, always `ls backend/src/main/resources/db/migration/` to check existing versions. Use the next available range. Current allocation: V1-V11 (foundation), V20-V26 (project mgmt), V30-V36 (design), V37-V39 (reports), V40-V47 (code & build), V50-V53 (testing), V60-V61 (AI center), V70-V77 (deployment).

### 12. Use `dp_` table prefix for Deployment Management to avoid SQL reserved words

**What happened:** Table names `release`, `deploy`, and `application` are reserved words or common names in H2/Oracle. Using them directly causes SQL parsing errors.

**Rule:** Prefix all Deployment Management tables with `dp_` (e.g., `dp_release`, `dp_deploy`, `dp_application`). Other slices can use their own prefix if needed. Always test migrations on H2 before committing.

## Lessons Learned (Session 2026-04-15)

### 1. Search the full project before assuming file locations

**What happened:** When creating the Gemini prompt, I only referenced `docs/05-design/design.md` (product module design) and missed `design.md` at the project root (visual design system). The user had to correct me twice.

**Rule:** Before referencing any document, always `Glob` for all matching files across the entire project. Do not assume a file only exists in one location. Ask if ambiguous.

### 2. Do not assume tech versions — use what the user specifies

**What happened:** I defaulted to Java 17 in the backend prompt. The user's actual version is Java 21.

**Rule:** Never assume language/runtime versions. If the user has stated a version, use it exactly. If not stated, ask before proceeding.

### 3. Use package-by-feature for multi-domain systems, not package-by-layer

**What happened:** The initial backend structure used flat `controller/`, `service/`, `model/` packages. The user immediately identified this won't scale for a system with 13+ SDLC domains plus shared platform capabilities.

**Rule:** When the PRD or architecture describes multiple business domains with shared platform capabilities, always propose package-by-feature (`platform/`, `domain/`, `shared/`) from the start. Package-by-layer only works for trivially small projects.

### 4. All database schema changes must use Flyway migrations

**Rule:** Never rely on `ddl-auto: create-drop` or `update` for schema changes. All DB changes (tables, columns, indexes, seeds) must be recorded as Flyway migration scripts under `src/main/resources/db/migration/` following the naming convention `V{version}__{description}.sql`. `ddl-auto` is only acceptable for local H2 throwaway dev; production and shared environments must use Flyway exclusively.

### 5. Prompts for external tools should be lightweight pointers, not content duplication

**What happened:** The first Gemini prompt was a massive document that duplicated content already in the repo's design docs. The user wanted a short prompt that points to the existing files.

**Rule:** When creating prompts for external tools (Gemini, Codex, etc.), prefer short prompts that reference existing repo documents rather than duplicating their content. The external tool can read the files directly. Only inline content that cannot be found in the repo (e.g., acceptance criteria, scope boundaries, constraints).

## Lessons Learned (Session 2026-04-16)

### 6. Every SDD slice must produce a complete 6-doc set

**What happened:** The shared-app-shell slice was missing a per-slice requirements doc (`01-requirements/shared-app-shell-requirements.md`) and a per-slice design doc (`05-design/shared-app-shell-design.md`). The product-level `design.md` existed but it covers all modules, not just the shell. The user had to ask why these were missing.

**Rule:** When executing the SDD pipeline for any slice, always produce or verify **all 9 documents** (6 core + 3 supplementary):

**Core Documents (6):**

| # | Stage | File pattern | Content |
|---|-------|-------------|---------|
| 1 | Requirements | `01-requirements/{slice}-requirements.md` | Requirements extracted from PRD with REQ-IDs and PRD section refs |
| 2 | User Stories | `02-user-stories/{slice}-stories.md` | Agile stories with acceptance criteria |
| 3 | Spec | `03-spec/{slice}-spec.md` | Implementation-facing contracts |
| 4 | Architecture | `04-architecture/{slice}-architecture.md` | System context, components, data flow, state, integration |
| 5 | Design | `05-design/{slice}-design.md` | Concrete APIs, file structure, data model, visual decisions, DB schema |
| 6 | Tasks | `06-tasks/{slice}-tasks.md` | Phased implementation breakdown |

**Supplementary Artifacts (3):**

| # | Stage | File pattern | Content |
|---|-------|-------------|---------|
| 7 | Data Flow | `04-architecture/{slice}-data-flow.md` | Runtime data flows, sequence diagrams, state machines, error cascade, refresh strategy |
| 8 | Data Model | `04-architecture/{slice}-data-model.md` | Domain model ER diagram, frontend types, backend DTOs/entities, DB schema DDL, type mapping |
| 9 | API Guide | `05-design/contracts/{slice}-API_IMPLEMENTATION_GUIDE.md` | Full endpoint contracts with JSON examples, backend/frontend implementation guide, testing contracts |

Before starting any implementation work, check that all 9 docs exist for the current slice. If any are missing, create them first. Do not start coding with an incomplete doc set.

### 7. Architecture docs must include Mermaid flow diagrams

**What happened:** The architecture doc was pure prose — no visual diagrams. The user asked why there were no flows. Architecture without diagrams is incomplete.

**Rule:** Every architecture doc must include at minimum these Mermaid diagrams:

1. **System context** — actors, systems, databases, and their relationships
2. **Component breakdown** — shell/module components and how they compose
3. **Data flow** — sequence diagram showing the runtime flow
4. **State boundaries** — what state lives where
5. **Integration** — how frontend and backend connect

Use only Mermaid 8.x-compatible syntax: `graph LR/TD/TB`, `sequenceDiagram`, `flowchart`. Do NOT use `C4Context`, `direction` inside subgraphs, `[( )]` cylinder notation, or `→` in node text — these require Mermaid 10+ and break in GitHub/IDE renderers.

### 8. Design docs must include concrete implementation details

**Rule:** A per-slice design doc is not a copy of the architecture. It must include:

- File structure map (actual paths)
- Component API contracts (props, inputs, sources)
- Data model (frontend types AND backend entity/table mapping)
- API contracts (endpoints, request/response JSON, status codes)
- Visual design decisions (tokens, typography, animation specifics)
- Database schema (DDL with column types)
- Error and empty state design
- Integration boundary diagram

### 9. Every slice must have data-flow, data-model, and API implementation guide

**What happened:** The dashboard and shared-app-shell slices had all 6 core SDD docs but were missing the 3 supplementary artifacts: `data-flow.md`, `data-model.md`, and `API_IMPLEMENTATION_GUIDE.md`. The user had to point this out after reviewing the folder structure.

**Rule:** Beyond the 6 core docs, every slice must also produce these 3 supplementary artifacts:

1. **`04-architecture/{slice}-data-flow.md`** — Runtime data flows with Mermaid sequence diagrams covering: page load (Phase A + B), error isolation, navigation, state machine, refresh strategy, API client chain
2. **`04-architecture/{slice}-data-model.md`** — Domain model ER diagram, complete frontend type catalog, backend DTO definitions, DB schema DDL (current + future), frontend-to-backend type mapping table
3. **`05-design/contracts/{slice}-API_IMPLEMENTATION_GUIDE.md`** — Full endpoint contract with complete JSON request/response examples, error handling, backend implementation skeleton, frontend integration guide (API client + store + proxy config), testing contracts, versioning policy

These are not optional — they are the bridge between design docs and implementation. Without them, Gemini and Codex have to guess at contracts.
