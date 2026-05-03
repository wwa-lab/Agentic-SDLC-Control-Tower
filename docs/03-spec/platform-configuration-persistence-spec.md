# Platform Configuration Persistence Spec

## Overview

This spec implements a focused Platform Center increment: repository-backed
Templates and repository-backed Configurations, with audited configuration
mutations. Template writes are intentionally deferred; configuration writes are
included because Configurations are the next governance surface after Access.

## Functional Rules

### FR-PCP-01: Repository-backed template catalog

`GET /api/v1/platform/templates` reads from `PLATFORM_TEMPLATE`.

Supported filters:

- `kind`
- `status`
- `q` over template key and name

The response keeps the existing `TemplateSummary` shape.

### FR-PCP-02: Repository-backed template detail

`GET /api/v1/platform/templates/{id}` returns:

- `template`: summary plus description. Description may be null while the table
  lacks a dedicated description column.
- `version`: the current version if `current_version_id` is set; otherwise the
  latest version by `version_number`.
- `inheritance`: a best-effort field-level view derived from the selected
  version body, with platform as the winning layer.

Unknown ids return `404 TEMPLATE_NOT_FOUND`.

### FR-PCP-03: Repository-backed template versions

`GET /api/v1/platform/templates/{id}/versions` returns versions for the template
ordered by `version DESC`. Unknown template ids return `404 TEMPLATE_NOT_FOUND`.

### FR-PCP-04: Repository-backed configuration catalog

`GET /api/v1/platform/configurations` reads from `PLATFORM_CONFIGURATION`.

Supported filters:

- `kind`
- `scopeType`
- `scopeId`
- `status`
- `q` over config key

Default sort is `lastModifiedAt DESC`.

### FR-PCP-05: Configuration detail

`GET /api/v1/platform/configurations/{id}` returns `ConfigurationDetail`.

`platformDefaultBody` is resolved by finding the active platform-scope row with
the same `config_key`. If the selected row itself is platform scoped, its body is
also the platform default.

`driftFields` is the list of top-level JSON fields whose selected value differs
from the platform default value. The list is empty when there is no platform
default or when values match.

### FR-PCP-06: Create configuration

`POST /api/v1/platform/configurations` validates:

- `key` is present
- `kind` is one of `page`, `field`, `component`, `flow-rule`, `view-rule`,
  `notification`, `ai-config`
- `scopeType` is one of `platform`, `application`, `snow_group`, `workspace`,
  `project`
- `platform` scope uses `scopeId = "*"`
- `status` is `active` or `inactive`
- `body` is valid JSON object data
- no row already exists for `key + scopeType + scopeId`

On success, it writes `config_change / configuration.create`.

### FR-PCP-07: Update configuration

`PUT /api/v1/platform/configurations/{id}` updates mutable fields for an
existing row. Unknown ids return `404 CONFIGURATION_NOT_FOUND`.

On success, it writes:

- `configuration.deactivate` when status transitions from `active` to
  `inactive`
- otherwise `configuration.update`

### FR-PCP-08: Atomic audit writer

Configuration mutations call `AuditWriter.write` inside the configuration
service transaction. Audit write failures roll back the configuration write.

## Error Contract

| Code | HTTP | Meaning |
|---|---:|---|
| `TEMPLATE_NOT_FOUND` | 404 | Template id does not exist |
| `CONFIGURATION_NOT_FOUND` | 404 | Configuration id does not exist |
| `CONFIGURATION_ALREADY_EXISTS` | 409 | Duplicate key/scope row |
| `INVALID_CONFIG_KIND` | 422 | Configuration kind is outside the V1 enum |
| `INVALID_CONFIG_SCOPE` | 422 | Scope type/id pair is invalid |
| `INVALID_CONFIG_STATUS` | 422 | Status is outside `active` or `inactive` |
| `INVALID_CONFIG_BODY` | 422 | Body cannot be represented as a JSON object |

## Non-Functional Rules

- All write endpoints remain `PLATFORM_ADMIN` gated.
- Template endpoints stay read-only in this milestone.
- Configuration audit payloads must not contain secrets or credential values.
- Tests must prove at least one configuration mutation writes audit.
