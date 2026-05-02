# Platform Access + Audit Persistence Requirements

## Purpose

This document defines the next Platform Center milestone: turn Access Management
from an in-memory administrative demo into a persistent, auditable control
surface. The milestone is intentionally narrow: staff users, role assignments,
last-admin safety, and audit records for access mutations.

## Source

- [platform-center-requirements.md](platform-center-requirements.md)
- [platform-center-spec.md](../03-spec/platform-center-spec.md)
- [platform-center-data-model.md](../04-architecture/platform-center-data-model.md)
- Product decision: SDD-first implementation for the next Platform Center slice

## Scope

### In Scope

1. Persist Platform Access users in `PLATFORM_USER`.
2. Persist Platform Access role assignments in `PLATFORM_ROLE_ASSIGNMENT`.
3. Persist Platform Audit records in `PLATFORM_AUDIT`.
4. Replace `PlatformAccessService` in-memory maps with repositories.
5. Write audit records for user create/update/deactivate, role grant, and role revoke.
6. Preserve `PLATFORM_ADMIN` route/API gating and the last-admin guard.
7. Keep Audit Management read-only.

### Out of Scope

- TeamBook or SCIM synchronization workers.
- Password reset, password policy, or identity-provider configuration.
- ABAC rule editing.
- Audit export and long-term archival.
- Persisting templates, configurations, policies, or integrations beyond their
  existing milestone state.

## Requirements

### REQ-PAA-01: Staff user records are durable

Platform Admins must be able to create, update, and deactivate staff users. User
records must survive application restarts by being stored in `PLATFORM_USER`.
Staff id remains the primary identity key.

### REQ-PAA-02: Role assignments are durable

Role assignments must be stored in `PLATFORM_ROLE_ASSIGNMENT` and remain
available after application restart. The uniqueness rule is:
`staffId + role + scopeType + scopeId`.

### REQ-PAA-03: Active user validation

Only active users may log in or receive new role assignments. Inactive users may
remain in historical role assignment and audit records for traceability.

### REQ-PAA-04: Last platform admin guard

The system must reject any revoke or deactivate operation that would leave zero
active users with an active `PLATFORM_ADMIN` assignment at `platform:*`.

### REQ-PAA-05: Access mutations are audited atomically

Every successful access mutation must write a `permission_change` audit record in
the same transaction as the mutation. If the audit insert fails, the access
mutation must roll back.

Required access audit actions:

- `user.create`
- `user.update`
- `user.deactivate`
- `role.grant`
- `role.revoke`

### REQ-PAA-06: Audit records are immutable

Audit records remain append-only. The HTTP API must expose no write, update, or
delete endpoint for `/api/v1/platform/audit`.

### REQ-PAA-07: Audit viewer reflects real mutations

After a Platform Admin mutates users or role assignments, the Audit page must be
able to display the resulting audit rows from `PLATFORM_AUDIT` without requiring
mock data.

### REQ-PAA-08: Seed data preserves local operability

Local development seed data must include at least two active Platform Admins, one
non-admin active user, and baseline audit rows so the app is immediately usable
after Flyway migration.

## Acceptance Summary

- Restarting the backend does not lose users or assignments.
- Role grant/revoke and user update/deactivation appear in the audit log.
- Revoking or deactivating the final active Platform Admin returns `409
  LAST_PLATFORM_ADMIN`.
- `./mvnw test` covers repository-backed access behavior and atomic audit writes.
