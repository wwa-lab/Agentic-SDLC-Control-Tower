# Agentic SDLC Control Tower

AI-native enterprise software delivery control tower. This repository currently contains a working Vue 3 frontend, a Spring Boot backend, seeded local data, and the Spec Driven Development (SDD) document set used to drive each slice.

## Current Implementation Snapshot

| Area | Frontend | Backend | Status |
| --- | --- | --- | --- |
| Shared app shell | Yes | Yes | 13-route shell, workspace context API, global shell UI |
| Dashboard | Yes | Yes | Live summary view backed by `/api/v1/dashboard/summary` |
| Requirement Management | Yes | Yes | List/detail/create flows, AI analysis, story/spec generation, normalize/import APIs |
| Incident Management | Yes | Yes | List/detail flows plus approve/reject action endpoints |
| Team Space | Yes | Yes | Aggregate + per-card APIs, access guards, seeded metrics/risk data |
| Project Space | Yes | Yes | Aggregate + per-card APIs, access guards, seeded project data |
| Platform Center | Yes | No | Current page is mock-backed UI only |
| Remaining navigation slices | Placeholder | No | Project Management, Design, Code & Build, Testing, Deployment, AI Center, Report Center |

## Stack

- Frontend: Vue 3.4, Vite 5, Vue Router 4, Pinia, TypeScript, Vitest
- Backend: Spring Boot 3.4.4, Java 21, Spring Web, Spring Data JPA, Actuator
- Data: H2 in local profile, Oracle in prod profile, Flyway migrations for schema management
- Integration: optional Dify-backed requirement import; stub provider enabled by default
- Delivery model: Spec Driven Development (SDD), with docs living alongside code

## Repository Layout

```text
.
├── frontend/                 # Vue app: shell/, shared/, features/
├── backend/                  # Spring Boot app: config/, domain/, integration/, platform/, shared/
├── docs/                     # SDD artifacts and slice documentation
├── CLAUDE.md                 # Project instructions and lessons learned
├── GEMINI.md                 # Gemini workflow brief
├── design.md                 # Shared visual design guidance
└── README.md
```

## What The App Exposes Today

- Frontend routes: `/`, `/team`, `/project-space/:projectId?`, `/requirements`, `/incidents`, `/platform`, plus placeholder routes for the remaining navigation entries.
- Default seeded workspace: `ws-default-001`
- Default seeded project: `proj-42`
- API base path: `/api/v1`
- Response contract: all endpoints return `ApiResponse<T>`; aggregate card pages use nested `SectionResultDto<T>` sections so one card can fail without blanking the whole page.

### Main API surface

- `GET /api/v1/workspace-context`
- `GET /api/v1/dashboard/summary`
- `GET /api/v1/requirements`
- `GET /api/v1/requirements/{requirementId}`
- `POST /api/v1/requirements/{requirementId}/generate-stories`
- `POST /api/v1/requirements/{requirementId}/generate-spec`
- `POST /api/v1/requirements/{requirementId}/analyze`
- `POST /api/v1/requirements/{requirementId}/invoke-skill`
- `POST /api/v1/requirements/normalize`
- `POST /api/v1/requirements/imports`
- `GET /api/v1/requirements/imports/{importId}`
- `GET /api/v1/pipeline-profiles/active`
- `GET /api/v1/incidents`
- `GET /api/v1/incidents/{incidentId}`
- `POST /api/v1/incidents/{incidentId}/actions/{actionId}/approve`
- `POST /api/v1/incidents/{incidentId}/actions/{actionId}/reject`
- `GET /api/v1/team-space/{workspaceId}` and section routes under `/summary`, `/operating-model`, `/members`, `/templates`, `/pipeline`, `/metrics`, `/risks`, `/projects`
- `GET /api/v1/project-space/{projectId}` and section routes under `/summary`, `/leadership`, `/chain`, `/milestones`, `/dependencies`, `/risks`, `/environments`

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

### Useful local URLs

- Dashboard: `http://localhost:5173/`
- Team Space: `http://localhost:5173/team?workspaceId=ws-default-001`
- Project Space: `http://localhost:5173/project-space/proj-42?workspaceId=ws-default-001`
- Requirement Management: `http://localhost:5173/requirements`
- Incident Management: `http://localhost:5173/incidents`
- Platform Center: `http://localhost:5173/platform?view=overview&workspaceId=ws-default-001`

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

The repository already includes controller/service/repository coverage on the backend and Vitest coverage for Project Space on the frontend.

## SDD Workflow

This repo follows Spec Driven Development. Each slice is expected to have requirements, stories, spec, architecture, design, tasks, and supporting contract/data docs under [`docs/`](docs/).

- Start here: [`docs/SDD-BOOTSTRAP.md`](docs/SDD-BOOTSTRAP.md)
- Full doc tree overview: [`docs/README.md`](docs/README.md)
- Project guardrails and workflow rules: [`CLAUDE.md`](CLAUDE.md)

## License

See [`LICENSE`](LICENSE).
