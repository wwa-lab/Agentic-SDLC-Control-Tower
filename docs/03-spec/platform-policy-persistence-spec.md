# Platform Policy Persistence Spec

## Overview

This spec implements durable Platform Policy & Governance records. Policies are
stored, versioned, and audited here; runtime policy evaluation remains outside
this milestone.

## Functional Rules

### FR-PPP-01: Repository-backed policy catalog

`GET /api/v1/platform/policies` reads from `PLATFORM_POLICY`.

Supported filters:

- `category`
- `status`
- `scopeType`
- `scopeId`
- `boundTo`
- `q` over policy key and name

Default sort is `createdAt DESC, id DESC`.

### FR-PPP-02: Policy detail

`GET /api/v1/platform/policies/{id}` returns one persisted policy or `404
POLICY_NOT_FOUND`.

### FR-PPP-03: Policy exceptions

`GET /api/v1/platform/policies/{id}/exceptions` returns exceptions for the
policy ordered by `createdAt DESC`. Unknown policy ids return `404
POLICY_NOT_FOUND`.

### FR-PPP-04: Create policy

`POST /api/v1/platform/policies` validates:

- `key` and `name` are present
- `category` is one of `action`, `approval`, `autonomy`, `risk-threshold`,
  `exception`
- `scopeType` is one of `platform`, `application`, `snow_group`, `workspace`,
  `project`
- `platform` scope uses `scopeId = "*"`
- `status` is `draft`, `active`, or `inactive`; missing status defaults to
  `active`
- `body` is a JSON object
- no active row already exists for `key + scopeType + scopeId`

On success, version is `1` and audit action is `policy.create`.

### FR-PPP-05: Edit policy creates a new version

`PUT /api/v1/platform/policies/{id}` validates the request and creates a new
policy row with the same key/scope unless explicitly changed, version
`currentVersion + 1`, and status `active`. The source row is set to `inactive`.

On success, audit action is `policy.update`.

### FR-PPP-06: Activate/deactivate policy

`POST /api/v1/platform/policies/{id}/activate` sets status to `active`.
`POST /api/v1/platform/policies/{id}/deactivate` sets status to `inactive`.

On success, audit actions are `policy.activate` and `policy.deactivate`.

### FR-PPP-07: Add exception

`POST /api/v1/platform/policies/{id}/exceptions` validates:

- policy exists and is active
- `reason`, `requesterId`, `approverId`, and `expiresAt` are present
- `expiresAt` is in the future

On success, audit action is `policy.exception.add`.

### FR-PPP-08: Revoke exception

`DELETE /api/v1/platform/policies/{policyId}/exceptions/{exceptionId}` sets
`revokedAt` to now. Unknown policy or exception ids return 404.

On success, audit action is `policy.exception.revoke`.

### FR-PPP-09: Atomic audit writer

Policy mutations call `AuditWriter.write` inside the policy service
transaction. Audit write failures roll back the policy write.

## Error Contract

| Code | HTTP | Meaning |
|---|---:|---|
| `POLICY_NOT_FOUND` | 404 | Policy id does not exist |
| `POLICY_EXCEPTION_NOT_FOUND` | 404 | Exception id does not exist for policy |
| `POLICY_ALREADY_EXISTS` | 409 | Duplicate active key/scope row |
| `POLICY_NOT_ACTIVE` | 409 | Exception add target is not active |
| `INVALID_POLICY_CATEGORY` | 422 | Category is outside the V1 enum |
| `INVALID_POLICY_SCOPE` | 422 | Scope type/id pair is invalid |
| `INVALID_POLICY_STATUS` | 422 | Status is outside `draft`, `active`, `inactive` |
| `INVALID_POLICY_BODY` | 422 | Body cannot be represented as a JSON object |
| `INVALID_POLICY_EXCEPTION` | 422 | Exception request is missing fields or expires in the past |

## Non-Functional Rules

- All write endpoints remain `PLATFORM_ADMIN` gated.
- Audit category is `config_change`, matching the existing Platform Center
  contract.
- Policy bodies must not include plaintext secrets or credential values.
- Tests must prove policy versioning and exception audit behavior.
