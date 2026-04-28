# Agentic SDLC Control Tower

AI-native enterprise software delivery control tower. A full-stack application with 13 SDLC domain slices, each with frontend views, backend APIs, and seeded local data, driven by a complete Spec Driven Development (SDD) document set.

## Implementation Status

All 13 navigation slices are implemented with frontend and backend.

| Slice                   | Frontend | Backend | Flyway  | Key Capabilities                                                                 |
| ----------------------- | -------- | ------- | ------- | -------------------------------------------------------------------------------- |
| Shared App Shell        | Yes      | Yes     | V1-V2   | 13-route shell, workspace context, global UI                                     |
| Dashboard               | Yes      | Yes     | V3      | Cross-stage health summary                                                       |
| Requirement Management  | Yes      | Yes     | V5-V6   | List/detail, AI analysis, story/spec generation, normalize/import                |
| Incident Management     | Yes      | Yes     | V4      | List/detail, approve/reject actions                                              |
| Team Space              | Yes      | Yes     | V7-V8   | Aggregate cards, access guards, metrics/risk                                     |
| Project Space           | Yes      | Yes     | V9-V11  | Project execution, environment status                                            |
| Project Management      | Yes      | Yes     | V20-V26 | Portfolio command center, plan execution, AI suggestions                         |
| Design Management       | Yes      | Yes     | V30-V36 | Artifact catalog, versioning, spec traceability, AI summary                      |
| Code & Build Management | Yes      | Yes     | V40-V47 | Repo/PR/run observability, story linking, AI triage                              |
| Testing Management      | Yes      | Yes     | V50-V53 | Plan/case/run lifecycle, coverage, traceability                                  |
| AI Center               | Yes      | Yes     | V60-V61 | Skill registry, run history, adoption metrics, stage coverage                    |
| Deployment Management   | Yes      | Yes     | V70-V77 | Jenkins observability, release notes, rollback detection, traceability           |
| Report Center           | Yes      | Yes     | V37-V39 | Efficiency reports, export (CSV/PDF), run history                                |
| Platform Center         | Yes      | Yes     | V80-V87 | 6 sub-sections: templates, configurations, audit, access, policies, integrations |

## Stack

- Frontend: Vue 3.4, Vite 5, Vue Router 4, Pinia, TypeScript, Vitest
- Backend: Spring Boot 3.4.4, Java 21, Spring Web, Spring Data JPA, Actuator
- Data: H2 in local profile, Oracle in prod profile, Flyway migrations (V1-V77, V80-V87 planned)
- Integration: optional Dify-backed requirement import; stub provider enabled by default
- Delivery model: Spec Driven Development (SDD) — documents before code
- Dev tools: Claude Code (SDD pipeline, orchestration), Gemini (frontend), Codex (backend)

## Repository Layout

```text
.
├── frontend/                 # Vue 3 app
│   ├── src/shell/            #   App shell, navigation, global stores
│   ├── src/shared/           #   API client, shared types, components
│   └── src/features/         #   13 feature modules (one per slice)
├── backend/                  # Spring Boot app
│   ├── src/main/java/.../domain/       # 12 domain packages (package-by-feature)
│   ├── src/main/java/.../platform/    # Platform-level services (workspace, navigation, profile)
│   ├── src/main/java/.../shared/       # ApiResponse, SectionResultDto, audit, integration
│   └── src/main/resources/db/migration/ # 40+ Flyway migrations (V1-V77)
├── docs/                     # SDD artifacts (9 docs per slice)
│   ├── 01-requirements/      #   Requirements with REQ-IDs
│   ├── 02-user-stories/      #   Agile stories with acceptance criteria
│   ├── 03-spec/              #   Implementation-facing specifications
│   ├── 04-architecture/      #   Architecture, data flow, data model
│   ├── 05-design/            #   Design docs + API implementation guides
│   ├── 06-tasks/             #   Phased task breakdowns
│   └── 07-prompts/           #   External tool prompts (Gemini, Codex)
├── CLAUDE.md                 # Project instructions and lessons learned
├── GEMINI.md                 # Gemini workflow brief
├── design.md                 # Shared visual design system
└── README.md
```

## What The App Exposes

- **13 frontend routes** with nested child routes for detail views
- Default seeded workspace: `ws-default-001`
- Default seeded project: `proj-42`
- API base path: `/api/v1`
- Response contract: all endpoints return `ApiResponse<T>`; aggregate card pages use nested `SectionResultDto<T>` sections so one card can fail without blanking the whole page

### API Surface by Slice

| Slice         | Base Path                  | Endpoints                                                                          |
| ------------- | -------------------------- | ---------------------------------------------------------------------------------- |
| Workspace     | `/workspace-context`     | GET context                                                                        |
| Dashboard     | `/dashboard`             | GET summary                                                                        |
| Requirements  | `/requirements`          | CRUD, analysis, story/spec gen, normalize, import                                  |
| Incidents     | `/incidents`             | List, detail, approve/reject actions                                               |
| Team Space    | `/team-space`            | Aggregate + 8 section routes                                                       |
| Project Space | `/project-space`         | Aggregate + 7 section routes                                                       |
| Project Mgmt  | `/project-management`    | Portfolio, plan, milestones, AI suggestions                                        |
| Design Mgmt   | `/design-management`     | Catalog, viewer, traceability, AI summary                                          |
| Code & Build  | `/code-build-management` | Catalog, repo/PR/run detail, traceability, AI triage                               |
| Testing       | `/testing-management`    | Catalog, plan/case/run detail, traceability                                        |
| AI Center     | `/ai-center`             | Metrics, skills, runs, stage coverage                                              |
| Deployment    | `/deployment-management` | Catalog, app/release/deploy/env detail, traceability, Jenkins webhook              |
| Reports       | `/reports`               | Catalog, run, export, history                                                      |
| Platform      | `/platform`              | Templates, configurations, audit, access, policies, integrations (Phase B planned) |

## Running Locally

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

- Runs on `http://localhost:8080`
- Local profile uses in-memory H2 with Flyway migrations and `ddl-auto: validate`
- H2 console is available at `http://localhost:8080/h2-console`

### Frontend

```bash
cd frontend
npm install
npm run dev
```

- Runs on `http://localhost:5173`
- Vite proxies `/api` to `http://localhost:8080`
- `frontend/.env.development` currently sets `VITE_USE_BACKEND=true`, so local dev talks to the backend by default
- To force mock mode for supported slices:

```bash
cd frontend
VITE_USE_BACKEND=false npm run dev
```

### Local Neo4j

For local SDD knowledge-graph experiments, run Neo4j as an optional Docker
service:

```bash
docker compose -f docker-compose.neo4j.yml up -d
```

- Browser UI: `http://localhost:7474`
- Bolt URI: `bolt://localhost:7687`
- Username: `neo4j`
- Default local password: `local-dev-password`

To use a different local password:

```bash
NEO4J_PASSWORD='change-me-locally' docker compose -f docker-compose.neo4j.yml up -d
```

The container stores data in a named Docker volume, so graph data survives
container restarts. Remove the volume only when you intentionally want a clean
graph database.

Suggested backend graph environment for local experiments:

```bash
GRAPH_PROVIDER=neo4j
NEO4J_URI=bolt://localhost:7687
NEO4J_USERNAME=neo4j
NEO4J_PASSWORD=local-dev-password
NEO4J_DATABASE=neo4j
```

### Useful Local URLs

- Dashboard: `http://localhost:5173/`
- Team Space: `http://localhost:5173/team?workspaceId=ws-default-001`
- Project Space: `http://localhost:5173/project-space/proj-42?workspaceId=ws-default-001`
- Requirements: `http://localhost:5173/requirements`
- Project Management: `http://localhost:5173/project-management`
- Design Management: `http://localhost:5173/design-management`
- Code & Build: `http://localhost:5173/code-build-management`
- Testing: `http://localhost:5173/testing`
- Deployment: `http://localhost:5173/deployment`
- Incidents: `http://localhost:5173/incidents`
- AI Center: `http://localhost:5173/ai-center`
- Reports: `http://localhost:5173/reports`
- Platform Center: `http://localhost:5173/platform`

## Configuration Notes

- Requirement import defaults to `REQUIREMENT_IMPORT_KB_PROVIDER=stub`
- Optional Dify settings:
  - `DIFY_BASE_URL`
  - `DIFY_API_KEY`
  - `DIFY_DATASET_LOOKUP_LIMIT`
  - `DIFY_SEGMENT_PAGE_SIZE`
  - `DIFY_INDEXING_TECHNIQUE`
- Prod database settings:
  - `ORACLE_HOST`
  - `ORACLE_PORT`
  - `ORACLE_SID`
  - `ORACLE_USER`
  - `ORACLE_PASSWORD`

## Tests

```bash
cd backend
./mvnw test
```

```bash
cd frontend
npm test
```

Backend: 136 tests across all domain slices (including 14 AI Center MockMvc tests). Frontend: Vitest unit and component tests.

## SDD Workflow

This repo follows Spec Driven Development. Each slice is expected to have requirements, stories, spec, architecture, design, tasks, and supporting contract/data docs under [`docs/`](docs/).

- Start here: [`docs/SDD-BOOTSTRAP.md`](docs/SDD-BOOTSTRAP.md)
- Full doc tree overview: [`docs/README.md`](docs/README.md)
- Project guardrails and workflow rules: [`CLAUDE.md`](CLAUDE.md)

## License

See [`LICENSE`](LICENSE).
