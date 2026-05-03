# Platform Access + Audit Persistence Spec

## Overview

This spec implements the next Platform Center milestone described in
`platform-access-audit-persistence-requirements.md`. It narrows the existing
Platform Center spec to a concrete backend-first increment: repository-backed
Access Management plus atomic audit writing.

## Functional Rules

### FR-PAA-01: Repository-backed user catalog

`GET /api/v1/platform/access/users` reads from `PLATFORM_USER`.

Supported filters:

- `status`
- `profileSource`
- `q` over staff id, display name, staff name, and email

`POST /api/v1/platform/access/users` inserts a new row or returns a conflict if
the staff id already exists. `PUT /api/v1/platform/access/users/{staffId}`
updates mutable metadata and status.

### FR-PAA-02: User status transition

Allowed user statuses are `active` and `inactive`. Setting a user to `inactive`
must run the last-admin guard before saving when the user currently holds
`PLATFORM_ADMIN`.

### FR-PAA-03: Repository-backed role assignments

`GET /api/v1/platform/access/assignments` reads from
`PLATFORM_ROLE_ASSIGNMENT`.

Supported filters:

- `staffId`
- `role`
- `scopeType`
- `scopeId`

Default sort is `grantedAt DESC`.

### FR-PAA-04: Assign role

`POST /api/v1/platform/access/assignments` validates:

- target user exists
- target user is active
- role is one of `PLATFORM_ADMIN`, `WORKSPACE_ADMIN`, `WORKSPACE_MEMBER`,
  `WORKSPACE_VIEWER`, `AUDITOR`
- scope type is one of `platform`, `application`, `snow_group`, `workspace`,
  `project`
- `platform` scope uses `scopeId = "*"`
- assignment uniqueness by `staffId + role + scopeType + scopeId`

On success, it writes `permission_change / role.grant`.

### FR-PAA-05: Revoke role

`DELETE /api/v1/platform/access/assignments/{id}` deletes the assignment if it
exists. If the target is the final active `PLATFORM_ADMIN` grant, return `409`
with code `LAST_PLATFORM_ADMIN`.

On success, it writes `permission_change / role.revoke`.

### FR-PAA-06: Atomic audit writer

`AuditWriter.write(AuditEvent event)` inserts exactly one `PLATFORM_AUDIT` row
and must be called inside the caller's transaction. The implementation uses
mandatory transaction propagation so accidental out-of-transaction writes fail
fast during tests.

### FR-PAA-07: Access mutation audit payloads

Audit payloads are JSON and must include enough context for review without
exposing secrets.

Required payload shape:

```json
{
  "before": {},
  "after": {},
  "request": {},
  "source": "platform-access"
}
```

For deletes, `after` is `null`. For creates, `before` is `null`.

### FR-PAA-08: Audit read model

`GET /api/v1/platform/audit` reads from `PLATFORM_AUDIT` and supports existing
filters from the Platform Center API guide. `GET /api/v1/platform/audit/{id}`
returns one persisted row or 404.

## Error Contract

| Code | HTTP | Meaning |
|---|---:|---|
| `USER_ALREADY_EXISTS` | 409 | Create target staff id already exists |
| `USER_NOT_FOUND` | 404 | Role assignment target or update target does not exist |
| `INVALID_USER_STATUS` | 422 | User status is outside `active` or `inactive` |
| `USER_INACTIVE` | 409 | Cannot assign a role to an inactive user |
| `ROLE_ASSIGNMENT_EXISTS` | 409 | Duplicate staff/role/scope grant |
| `ROLE_ASSIGNMENT_NOT_FOUND` | 404 | Revoke target does not exist |
| `LAST_PLATFORM_ADMIN` | 409 | Mutation would remove the final active Platform Admin |
| `INVALID_ROLE` | 422 | Role is outside the V1 enum |
| `INVALID_SCOPE` | 422 | Scope type/id pair is invalid |

## Non-Functional Rules

- All write endpoints remain `PLATFORM_ADMIN` gated.
- No audit write/update/delete endpoint is introduced.
- Access list queries must remain p95 <= 500ms for 500 local rows.
- Tests must prove audit rollback semantics for at least one access mutation.
