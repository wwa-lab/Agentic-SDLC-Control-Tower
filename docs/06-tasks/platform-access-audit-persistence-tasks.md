# Platform Access + Audit Persistence Tasks

## Milestone Goal

Platform Admins can manage users and role assignments using persistent database
state, and every successful access mutation appears in the read-only Audit log.

## Phase PAA-0: Document Baseline

- [x] Add focused requirements document.
- [x] Add focused user stories.
- [x] Add focused implementation spec.
- [x] Add focused architecture/data-flow document.
- [x] Review docs against existing Platform Center API guide and data model.

## Phase PAA-1: Persistence Model

- [x] Add `PlatformUserEntity` mapped to `PLATFORM_USER`.
- [x] Add `RoleAssignmentEntity` mapped to `PLATFORM_ROLE_ASSIGNMENT`.
- [x] Add `AuditRecordEntity` mapped to `PLATFORM_AUDIT`.
- [x] Add repositories for users, role assignments, and audit records.
- [x] Verify existing Flyway migrations create every mapped column.

## Phase PAA-2: Audit Writer

- [x] Add `AuditEvent` value object.
- [x] Add `AuditWriter` with mandatory transaction propagation.
- [x] Add JSON payload serialization for before/after mutation snapshots.
- [x] Add test proving `AuditWriter.write` fails outside a transaction.
- [x] Add test proving access mutation rollback also rolls back audit.

## Phase PAA-3: Repository-backed Access Service

- [x] Replace in-memory user map with `PlatformUserRepository`.
- [x] Replace in-memory assignment map with `RoleAssignmentRepository`.
- [x] Implement user create/update/deactivate with audit actions:
  `user.create`, `user.update`, `user.deactivate`.
- [x] Implement role grant/revoke with audit actions:
  `role.grant`, `role.revoke`.
- [x] Preserve active-user validation.
- [x] Preserve last-admin guard for revoke and user deactivation.
- [x] Return stable API errors for not found, duplicate grant, invalid role,
  invalid scope, inactive user, and last admin.

## Phase PAA-4: Audit Read Service

- [x] Replace static audit catalog rows with `AuditRepository` reads.
- [x] Support filters already declared in the Platform Center API guide.
- [x] Add `GET /platform/audit/{id}` if missing from the controller.
- [x] Keep audit HTTP surface read-only.

## Phase PAA-5: Frontend Wiring Check

- [x] Confirm Access store uses live `GET/POST/PUT/DELETE` endpoints when
  `VITE_USE_MOCK_API` is not true.
- [x] Confirm Access view displays API errors, including `LAST_PLATFORM_ADMIN`.
- [x] Confirm Audit store reads persisted rows in live mode.
- [x] Add or update frontend tests only if API shape changes.

## Phase PAA-6: Verification

- [x] Backend: run `./mvnw test`.
- [x] Backend: add focused controller tests for user lifecycle and role lifecycle.
- [x] Backend: add repository/service tests for last-admin guard.
- [x] Frontend: run `npm run build`.
- [ ] Manual smoke: create user, grant role, revoke role, refresh Audit, restart
  backend, confirm persisted state remains.

## Definition of Done

- Access users and role assignments are durable.
- Audit rows are created by real access mutations.
- The last Platform Admin cannot be revoked or deactivated.
- Audit remains append-only and read-only via HTTP.
- Tests cover happy paths, rejected paths, and audit atomicity.
