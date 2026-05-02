# Platform Configuration Persistence Tasks

## Milestone Goal

Platform Admins can browse durable Templates, manage durable Configurations, and
see configuration changes in the persistent Audit log.

## Phase PCP-0: Document Baseline

- [x] Add focused requirements document.
- [x] Add focused user stories.
- [x] Add focused implementation spec.
- [x] Add focused architecture/data-flow document.
- [x] Review docs against existing Platform Center data model and API guide.

## Phase PCP-1: Persistence Model

- [x] Add template and template-version entities mapped to existing tables.
- [x] Add configuration entity mapped to existing table.
- [x] Add repositories for templates, template versions, and configurations.
- [x] Verify existing Flyway migrations create every mapped column.

## Phase PCP-2: Template Read Service

- [x] Replace static template catalog rows with repository reads.
- [x] Implement template detail and version history from persisted rows.
- [x] Return stable `TEMPLATE_NOT_FOUND` errors for unknown ids.

## Phase PCP-3: Configuration Read/Write Service

- [x] Replace static configuration catalog rows with repository reads.
- [x] Implement configuration detail with platform default and drift fields.
- [x] Implement configuration create/update/deactivate.
- [x] Return stable API errors for duplicate, invalid kind, invalid scope,
  invalid status, invalid body, and not found.

## Phase PCP-4: Audit Integration

- [x] Write `config_change` audit records for `configuration.create`.
- [x] Write `config_change` audit records for `configuration.update`.
- [x] Write `config_change` audit records for `configuration.deactivate`.
- [x] Keep configuration mutation and audit write in the same transaction.

## Phase PCP-5: Frontend Wiring

- [x] Confirm Templates store uses live persisted endpoints.
- [x] Confirm Configurations store uses live `GET/POST/PUT` endpoints when
  `VITE_USE_MOCK_API` is not true.
- [x] Add Configuration UI controls for create, edit, and deactivate.
- [x] Confirm API errors are visible in Configurations UI.

## Phase PCP-6: Verification

- [x] Backend: add focused controller tests for template reads and
  configuration lifecycle.
- [x] Backend: add or extend audit tests for configuration mutation audit.
- [x] Backend: run focused tests.
- [x] Frontend: run `npm run build`.
- [x] Backend: run `./mvnw test`.
- [ ] Manual smoke: create configuration override, update body, deactivate row,
  refresh Audit, restart backend, confirm persisted state remains.

## Definition of Done

- Template and configuration reads are repository-backed.
- Configuration mutations are durable and audited.
- Drift fields are computed from persisted JSON bodies.
- The frontend can perform the documented configuration workflow in live mode.
- Tests cover happy paths, rejected paths, and audit creation.
