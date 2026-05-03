# Platform Configuration Persistence Requirements

## Purpose

This milestone moves Platform Center Templates and Configurations from static
catalog rows to durable repository-backed governance data. It builds on the
previous Access + Audit persistence slice: Platform Admins can now read real
template/configuration state, mutate configurations, and see those changes in
the persistent audit trail.

## Source

- [platform-center-requirements.md](platform-center-requirements.md)
- [platform-center-spec.md](../03-spec/platform-center-spec.md)
- [platform-center-data-model.md](../04-architecture/platform-center-data-model.md)
- [platform-access-audit-persistence-requirements.md](platform-access-audit-persistence-requirements.md)

## Scope

### In Scope

1. Read Templates from `PLATFORM_TEMPLATE`.
2. Read Template versions from `PLATFORM_TEMPLATE_VERSION`.
3. Read Configurations from `PLATFORM_CONFIGURATION`.
4. Create and update Configuration rows through Platform Admin APIs.
5. Resolve `platformDefaultBody` and `driftFields` for configuration detail.
6. Write persistent `config_change` audit records for configuration mutations.
7. Keep all write endpoints `PLATFORM_ADMIN` gated.
8. Wire the Configuration UI to live create/update/deactivate workflows.

### Out of Scope

- Template create/edit/publish/deprecate workflows.
- Template usage/reference blocking.
- Policy persistence.
- Integration connection persistence.
- Full schema validation for configuration JSON bodies.
- Cross-module runtime consumption of configurations by other slices.

## Requirements

### REQ-PCP-01: Template catalog is durable

`GET /api/v1/platform/templates` must read from `PLATFORM_TEMPLATE` and return
rows using the existing frontend contract.

### REQ-PCP-02: Template detail is durable

`GET /api/v1/platform/templates/{id}` and
`GET /api/v1/platform/templates/{id}/versions` must read from
`PLATFORM_TEMPLATE_VERSION`. Unknown template ids must return 404.

### REQ-PCP-03: Configuration catalog is durable

`GET /api/v1/platform/configurations` must read from `PLATFORM_CONFIGURATION`
and support filtering by `kind`, `scopeType`, `scopeId`, `status`, and `q`.

### REQ-PCP-04: Configuration detail resolves defaults

`GET /api/v1/platform/configurations/{id}` must return the selected
configuration body, the platform-default body for the same key when present, and
drift fields comparing the selected body to the platform default.

### REQ-PCP-05: Configuration creation is durable

Platform Admins must be able to create a configuration row. Uniqueness remains
`config_key + scope_type + scope_id`.

### REQ-PCP-06: Configuration updates are durable

Platform Admins must be able to update body, status, kind, parent id, and scope
metadata for an existing configuration row.

### REQ-PCP-07: Configuration mutations are audited atomically

Every successful configuration create/update/deactivate operation must write a
`config_change` audit record in the same transaction as the mutation. If audit
insert fails, the configuration mutation must roll back.

Required actions:

- `configuration.create`
- `configuration.update`
- `configuration.deactivate`

### REQ-PCP-08: Seed data remains operable

Local migration data must include at least one template with a version, one
platform-default configuration, and one scoped override so detail and drift views
are meaningful immediately after migration.

## Acceptance Summary

- Restarting the backend does not lose templates, template versions, or
  configurations.
- Configuration create/update/deactivate appears in Audit as `config_change`.
- Duplicate configuration scope/key create returns `409
  CONFIGURATION_ALREADY_EXISTS`.
- Unknown template/configuration detail returns 404.
- `./mvnw test` covers repository-backed reads and audited configuration
  mutations.
