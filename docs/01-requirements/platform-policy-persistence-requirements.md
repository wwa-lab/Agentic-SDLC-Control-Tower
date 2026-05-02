# Platform Policy Persistence Requirements

## Purpose

This milestone moves Platform Center Policy & Governance from static catalog
rows to durable policy and exception records. It builds on the Access, Audit,
Template, and Configuration persistence slices so Platform Admins can manage the
governance rules that downstream SDLC slices will later evaluate.

## Source

- [platform-center-requirements.md](platform-center-requirements.md)
- [platform-center-spec.md](../03-spec/platform-center-spec.md)
- [platform-center-data-model.md](../04-architecture/platform-center-data-model.md)
- [platform-configuration-persistence-requirements.md](platform-configuration-persistence-requirements.md)

## Scope

### In Scope

1. Read Policies from `PLATFORM_POLICY`.
2. Read Policy exceptions from `PLATFORM_POLICY_EXCEPTION`.
3. Create a policy at version 1.
4. Edit a policy by creating a new active version and inactivating the previous
   row.
5. Activate or deactivate a policy row.
6. Add and revoke policy exceptions.
7. Write persistent `config_change` audit records for policy mutations.
8. Wire the Policies UI to live create/edit/deactivate/exception workflows.

### Out of Scope

- Runtime policy evaluation or enforcement.
- Policy DSL validation beyond required fields and JSON-object body shape.
- Approval workflow for exceptions.
- Policy resolver APIs for consumer slices.
- Template-backed policy generation.

## Requirements

### REQ-PPP-01: Policy catalog is durable

`GET /api/v1/platform/policies` must read from `PLATFORM_POLICY` and return the
existing frontend `Policy` shape.

### REQ-PPP-02: Policy filters are supported

The policy catalog must support filters by `category`, `status`, `scopeType`,
`scopeId`, `boundTo`, and `q` over key/name.

### REQ-PPP-03: Policy creation is durable

Platform Admins must be able to create a policy with version `1`, status
`active` by default, scoped to platform/application/SNOW group/workspace/project.
Only JSON object bodies are accepted.

### REQ-PPP-04: Policy edit creates a new version

Editing an existing policy must create a new row with version `current + 1` and
status `active`. The edited source row is transitioned to `inactive`.

### REQ-PPP-05: Policy activation state is durable

Platform Admins must be able to activate or deactivate an existing policy row
without deleting it.

### REQ-PPP-06: Policy exceptions are durable

Platform Admins must be able to add a time-boxed exception for an active policy.
Exceptions must remain queryable after restart. Revoking an exception sets
`revokedAt` without deleting the row.

### REQ-PPP-07: Policy mutations are audited atomically

Every successful policy create/update/activate/deactivate/exception add/revoke
operation must write a `config_change` audit record in the same transaction as
the mutation. If audit insert fails, the policy mutation must roll back.

Required actions:

- `policy.create`
- `policy.update`
- `policy.activate`
- `policy.deactivate`
- `policy.exception.add`
- `policy.exception.revoke`

### REQ-PPP-08: Seed data remains operable

Local migration data must include at least one active policy and at least one
exception so the Policies page has useful data immediately after migration.

## Acceptance Summary

- Restarting the backend does not lose policies or exceptions.
- Policy edit creates a new version and inactivates the prior version.
- Exceptions can be added and revoked without deletion.
- Policy mutations appear in Audit as `config_change` with `policy.*` actions.
- `./mvnw test` covers repository-backed reads and audited policy mutations.
