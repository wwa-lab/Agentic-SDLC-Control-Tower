# CLAUDE.md

## Project

Agentic SDLC Control Tower — AI-native enterprise software delivery control tower.

- Frontend: Vue 3 / Vite / Vue Router / Pinia / TypeScript
- Backend: Spring Boot 3.x / Java 21 / JPA / H2 (local) / Oracle (prod)
- Dev model: SDD (Spec Driven Development) — documents before code
- Frontend dev tool: Gemini
- Backend dev tool: Codex
- Claude Code: SDD pipeline, doc quality gates, review, orchestration

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

**Rule:** When executing the SDD pipeline for any slice, always produce or verify **all 6 documents**:

| # | Stage | File pattern | Content |
|---|-------|-------------|---------|
| 1 | Requirements | `01-requirements/{slice}-requirements.md` | Requirements extracted from PRD with REQ-IDs and PRD section refs |
| 2 | User Stories | `02-user-stories/{slice}-stories.md` | Agile stories with acceptance criteria |
| 3 | Spec | `03-spec/{slice}-spec.md` | Implementation-facing contracts |
| 4 | Architecture | `04-architecture/{slice}-architecture.md` | System context, components, data flow, state, integration |
| 5 | Design | `05-design/{slice}-design.md` | Concrete APIs, file structure, data model, visual decisions, DB schema |
| 6 | Tasks | `06-tasks/{slice}-tasks.md` | Phased implementation breakdown |

Before starting any implementation work, check that all 6 docs exist for the current slice. If any are missing, create them first. Do not start coding with an incomplete doc set.

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
