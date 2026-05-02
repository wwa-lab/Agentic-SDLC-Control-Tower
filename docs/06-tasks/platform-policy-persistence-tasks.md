# Platform Policy Persistence Tasks

## Milestone Goal

Platform Admins can browse durable Policies, manage policy versions and
exceptions, and see policy changes in the persistent Audit log.

## Phase PPP-0: Document Baseline

- [x] Add focused requirements document.
- [x] Add focused user stories.
- [x] Add focused implementation spec.
- [x] Add focused architecture/data-flow document.
- [x] Review docs against existing Platform Center data model and API guide.

## Phase PPP-1: Persistence Model

- [x] Add policy entity mapped to `PLATFORM_POLICY`.
- [x] Add policy exception entity mapped to `PLATFORM_POLICY_EXCEPTION`.
- [x] Add repositories for policies and policy exceptions.
- [x] Verify existing Flyway migrations create every mapped column.

## Phase PPP-2: Policy Read Service

- [x] Replace static policy catalog rows with repository reads.
- [x] Implement policy detail from persisted rows.
- [x] Implement exception list from persisted rows.
- [x] Return stable not-found errors for unknown ids.

## Phase PPP-3: Policy Write Service

- [x] Implement policy create.
- [x] Implement policy edit-as-new-version.
- [x] Implement policy activate/deactivate.
- [x] Implement exception add and revoke.
- [x] Return stable API errors for duplicate, invalid category, invalid scope,
  invalid status, invalid body, invalid exception, inactive policy, and not found.

## Phase PPP-4: Audit Integration

- [x] Write `config_change` audit records for `policy.create`.
- [x] Write `config_change` audit records for `policy.update`.
- [x] Write `config_change` audit records for `policy.activate`.
- [x] Write `config_change` audit records for `policy.deactivate`.
- [x] Write `config_change` audit records for `policy.exception.add`.
- [x] Write `config_change` audit records for `policy.exception.revoke`.
- [x] Keep policy mutation and audit write in the same transaction.

## Phase PPP-5: Frontend Wiring

- [x] Confirm Policies store uses live `GET/POST/PUT/DELETE` endpoints when
  `VITE_USE_MOCK_API` is not true.
- [x] Add Policies UI controls for create, edit, deactivate, activate, add
  exception, and revoke exception.
- [x] Confirm API errors are visible in Policies UI.

## Phase PPP-6: Verification

- [x] Backend: add focused controller tests for policy catalog, versioning, and
  exceptions.
- [x] Backend: run focused tests.
- [x] Frontend: run `npm run build`.
- [x] Backend: run `./mvnw test`.
- [ ] Manual smoke: create policy, edit policy, add exception, revoke exception,
  refresh Audit, restart backend, confirm persisted state remains.

## Phase PPP-7: Consistency Review

- [x] Review implementation against SDD docs.
- [x] Fix any code/document mismatches found during review.
- [x] Re-run focused verification after fixes.

## Definition of Done

- Policy and exception reads are repository-backed.
- Policy mutations are durable and audited.
- Policy edit creates a new version and preserves history.
- The frontend can perform the documented policy workflow in live mode.
- Tests cover happy paths, rejected paths, and audit creation.
