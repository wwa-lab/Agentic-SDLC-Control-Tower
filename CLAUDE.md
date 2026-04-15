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
