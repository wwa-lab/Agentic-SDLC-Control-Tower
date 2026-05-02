# Platform Policy Persistence Architecture

## Context

Platform Center already has Flyway tables for policies and policy exceptions,
but the controller currently reads static catalog rows. This milestone adds
repository-backed policy services and audited write paths.

## Components

```mermaid
flowchart LR
    UI[Policies UI] --> CTRL[PlatformCenterController]
    CTRL --> AUTH[AuthService]
    CTRL --> POLICY[PlatformPolicyService]
    POLICY --> POLICY_REPO[PlatformPolicyRepository]
    POLICY --> EX_REPO[PlatformPolicyExceptionRepository]
    POLICY --> AUDIT[AuditWriter]
    AUDIT --> AUDIT_REPO[AuditRepository]
    POLICY_REPO --> DB[(H2 / Oracle)]
    EX_REPO --> DB
    AUDIT_REPO --> DB
```

## Package Layout

```text
platform/policy/
  PlatformPolicyEntity.java
  PlatformPolicyExceptionEntity.java
  PlatformPolicyRepository.java
  PlatformPolicyExceptionRepository.java
  PlatformPolicyService.java
  PolicyDto.java
  PolicyExceptionDto.java
  UpsertPolicyRequest.java
  CreatePolicyExceptionRequest.java
  PlatformPolicyException.java
```

## Data Flow: Edit Policy

```mermaid
sequenceDiagram
    participant Admin as Platform Admin
    participant API as PlatformCenterController
    participant Service as PlatformPolicyService
    participant Repo as PlatformPolicyRepository
    participant Audit as AuditWriter
    participant DB as Database

    Admin->>API: PUT /platform/policies/{id}
    API->>Service: update(id, request, actor)
    Service->>Repo: find source policy
    Service->>Repo: save source status=inactive
    Service->>Repo: insert new version status=active
    Service->>Audit: write(policy.update)
    Audit->>DB: INSERT PLATFORM_AUDIT
    Service-->>API: PolicyDto
```

The source row transition, new version insert, and audit row commit or roll back
together.

## Data Flow: Add Exception

```mermaid
sequenceDiagram
    participant Admin as Platform Admin
    participant API as PlatformCenterController
    participant Service as PlatformPolicyService
    participant ExRepo as PlatformPolicyExceptionRepository
    participant Audit as AuditWriter

    Admin->>API: POST /platform/policies/{id}/exceptions
    API->>Service: addException(id, request, actor)
    Service->>Service: validate policy active and expiry future
    Service->>ExRepo: save exception
    Service->>Audit: write(policy.exception.add)
```

## Migration Usage

This milestone reuses existing migrations:

- `V91__create_platform_policy.sql`
- `V93__seed_platform_center_data.sql`

Do not introduce duplicate policy tables.

## Security Notes

- Policy bodies are generic JSON but must not contain plaintext secrets.
- Exceptions include human-entered reasons and should be treated as audit
  evidence.
- Runtime policy enforcement remains in consumer slices.
