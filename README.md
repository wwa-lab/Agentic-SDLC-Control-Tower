# Agentic SDLC Control Tower

AI-native enterprise software delivery control tower. A unified operating plane across all SDLC stages — from requirement through deployment, incident, and learning — with AI agents, workflow orchestration, and cross-team governance.

## Stack

- **Frontend** — Vue 3 + Vite + Vue Router + Pinia + TypeScript
- **Backend** — Spring Boot 3.x + Java 21 + JPA
- **Database** — H2 (local) / Oracle (prod), schema managed via Flyway migrations
- **Dev model** — Spec Driven Development (SDD): documents before code
- **Frontend dev tool** — Gemini,Claude,Codex (Phase A, mocks-first)
- **Backend dev tool** — Claude,Codex (Phase B)
- **Claude Code** — SDD pipeline, doc quality gates, review, orchestration

## Repository Layout

```
.
├── CLAUDE.md                 # Claude Code project instructions and Lessons Learned
├── GEMINI.md                 # Gemini dev-tool brief
├── design.md                 # Product-wide visual design system
├── frontend/                 # Vue 3 + Vite + TS app
├── backend/                  # Spring Boot 3 + Java 21 service
└── docs/
    ├── SDD-BOOTSTRAP.md      # How the SDD loop works + current slice roadmap
    ├── 00-context/
    ├── 01-requirements/      # PRD + per-slice requirements
    ├── 02-user-stories/      # Agile stories with acceptance criteria
    ├── 03-spec/              # Implementation-facing contracts
    ├── 04-architecture/      # Architecture + data-flow + data-model (per slice)
    ├── 05-design/            # Design docs + contracts/ (API implementation guides)
    ├── 06-tasks/             # Phased implementation breakdown
    └── 07-prompts/           # Prompts for external tools (Gemini / Codex)
```

## Spec Driven Development (SDD) Loop

Every feature slice produces a complete **9-document set** before a line of code is written.

**Core (6):** requirements → stories → spec → architecture → design → tasks.
**Supplementary (3):** data-flow, data-model, API implementation guide.

See [`docs/SDD-BOOTSTRAP.md`](docs/SDD-BOOTSTRAP.md) for the full pipeline, minimum gate, and quality rules. See [`CLAUDE.md`](CLAUDE.md) for the enforcement rules (#6–#9).

## Slice Roadmap

| Order | Slice                     | Status                                          |
| ----- | ------------------------- | ----------------------------------------------- |
| 0     | `shared-app-shell`      | Completed (docs + code)                         |
| 1     | `dashboard`             | Completed (docs + code)                         |
| 2     | `requirement`           | Completed (docs + code)                         |
| 3     | `incident`              | Completed (docs + code)                         |
| 4     | `team-space`            | Docs complete; implementation pending           |
| 5     | `project-space`         | **Docs complete; implementation pending** |
| 6     | `project-management`    | Not started                                     |
| 7     | `design-management`     | Not started                                     |
| 8     | `code-build-management` | Not started                                     |
| 9     | `testing-management`    | Not started                                     |
| 10    | `deployment-management` | Not started                                     |
| 11    | `ai-center`             | Not started                                     |
| 12    | `report-center`         | Not started                                     |
| 13    | `platform-center`       | Not started                                     |

## Running Locally

### Frontend

```bash
cd frontend
npm install
npm run dev
```

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

Dev proxy: Vite forwards `/api/*` to the backend. H2 is used for local dev; all schema changes go through Flyway migrations under `backend/src/main/resources/db/migration/` (convention: `V{version}__{description}.sql`). Never rely on `ddl-auto: update` for shared or production environments.

## Key Conventions

- **Package-by-feature** on the backend: `com.controltower.domain.{slice}.{api,service,infra}` plus `platform/`, `domain/`, `shared/` top-level groupings.
- **Flyway-only** for schema; `ddl-auto` is limited to local H2 throwaway.
- **Mermaid 8.x-compatible** diagrams only (no `C4Context`, no `direction` inside subgraphs, no cylinder `[(...)]` notation, no `→` in flowchart node text).
- **SectionResult`<T>`** envelope for per-card partial-failure isolation on aggregate endpoints.
- **Phase A / Phase B** delivery: Gemini builds the frontend against mocks first, then Codex implements the backend against the sealed API contracts.

## License

See [`LICENSE`](LICENSE).
