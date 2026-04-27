# Requirement Control Plane Spec

## Overview

Requirement Control Plane is the next-stage evolution of Requirement Management.
It reframes the page as a control surface for business source intake,
GitHub-backed SDD document review, profile-driven document chains, CLI-agent
execution requests, and freshness tracking.

The system does not make Control Tower the document repository. It treats
Jira/Confluence/uploads/KB as source references and GitHub `docs/` as the SDD
artifact source of truth.

## Source Documents

- Requirements: [requirement-control-plane-requirements.md](../01-requirements/requirement-control-plane-requirements.md)
- Stories: [requirement-control-plane-stories.md](../02-user-stories/requirement-control-plane-stories.md)
- Platform principles: [CLAUDE.md](../../CLAUDE.md)

## Functional Areas

### F-RCP-SOURCE: Source Reference Intake

- Support creation of source references for Jira, Confluence, KB, upload, and
  GitHub URLs.
- Store source metadata without copying the full source body into the
  Requirement domain.
- Allow manual refresh of source metadata.
- Show freshness status against indexed SDD documents.
- Preserve existing Normalize with AI as assisted intake, not as canonical SDD
  document generation.

### F-RCP-DOCS: GitHub SDD Document Viewing

- Index SDD documents by repo, branch/ref, path, SDD type, profile, commit SHA,
  blob SHA, status, and GitHub URL.
- Fetch latest Markdown content from GitHub on document open.
- Render Markdown inside Control Tower with version metadata.
- Provide Open in GitHub link.
- Show missing expected documents from profile definitions.

### F-RCP-REVIEW: Business Review

- Allow document-level comments.
- Allow decisions: Approved, Changes Requested, Rejected.
- Bind each review action to commit SHA and blob SHA.
- Show review history grouped by document and version.
- Mark approvals stale when a newer blob exists.

### F-RCP-PROFILE: Profile-Driven SDD

- Use active SDD profile to render document stages.
- Standard Java profile renders existing Java SDD chain.
- IBM i profile renders IBM i chain from `build-agent-skill` concepts.
- Profile defines document stages, default path patterns, skill bindings, review
  gates, traceability key rules, and optional tiering.

### F-RCP-AGENT: CLI Agent Run Request

- UI creates an agent run request and execution manifest.
- Manifest pins latest resolved source/document versions.
- Agents run outside the UI and report status through callback APIs.
- Requirement detail shows recent runs and artifacts.

### F-RCP-FRESHNESS: Freshness and Traceability

- Compute source-to-document freshness.
- Compute review-to-document freshness.
- Show common freshness states across Requirement detail.
- Connect sources, documents, reviews, agent runs, and artifact links.

## Domain Rules

### DR-RCP-01: Markdown authority

GitHub owns Markdown document bodies. Control Tower stores only index metadata
and review records.

### DR-RCP-02: Review version binding

A review decision without commit SHA and blob SHA is invalid for GitHub-backed
documents.

### DR-RCP-03: Agent manifest ownership

Agents must consume Control Tower-generated manifests. They must not infer
source versions or document versions independently.

### DR-RCP-04: Profile rendering

Requirement UI must render stages from the active profile. Hardcoded
Standard-Java-only labels are not allowed in profile-aware sections.

### DR-RCP-05: Assisted intake boundary

Normalize with AI may create structured drafts and source metadata, but it must
not be represented as the canonical SDD artifact. Canonical SDD artifacts are
GitHub documents.

## State Models

### Source Freshness

```text
---------+----------------+----------------+
| State   | Meaning        | Example        |
+---------+----------------+----------------+
| FRESH   | Source is not newer than linked doc |
| SOURCE_CHANGED | Source changed after doc generation |
| UNKNOWN | Provider cannot supply comparable version |
| ERROR   | Source metadata refresh failed |
+---------+----------------+----------------+
```

### Document Review Status

```text
UNREVIEWED -> APPROVED
UNREVIEWED -> CHANGES_REQUESTED
UNREVIEWED -> REJECTED
APPROVED -> STALE_AFTER_CHANGE
CHANGES_REQUESTED -> APPROVED
REJECTED -> APPROVED
```

### Agent Execution Status

```text
REQUESTED -> MANIFEST_READY -> RUNNING -> COMPLETED
REQUESTED -> CANCELED
MANIFEST_READY -> STALE_CONTEXT
RUNNING -> FAILED
```

## API Surface

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/v1/requirements/{id}/sources` | List source references |
| POST | `/api/v1/requirements/{id}/sources` | Add source reference |
| POST | `/api/v1/requirements/sources/{sourceId}/refresh` | Refresh source metadata |
| GET | `/api/v1/requirements/{id}/sdd-documents` | List indexed SDD docs and expected profile stages |
| GET | `/api/v1/requirements/documents/{documentId}` | Fetch latest GitHub Markdown and metadata |
| POST | `/api/v1/requirements/documents/{documentId}/reviews` | Add comment or decision |
| GET | `/api/v1/requirements/{id}/reviews` | List review history |
| POST | `/api/v1/requirements/{id}/agent-runs` | Create agent run manifest |
| GET | `/api/v1/requirements/agent-runs/{executionId}` | Get agent run status |
| POST | `/api/v1/requirements/agent-runs/{executionId}/callback` | Agent status/artifact callback |
| GET | `/api/v1/requirements/{id}/traceability` | Source/doc/review/run traceability |

## Profile Examples

### Standard Java SDD

```yaml
profileId: standard-java-sdd
stages:
  - requirement
  - user-story
  - spec
  - architecture
  - design
  - tasks
  - code
  - test
traceability:
  keyPattern: "REQ-[0-9]+"
```

### IBM i SDD

```yaml
profileId: ibm-i-sdd
stages:
  - requirement-normalizer
  - functional-spec
  - technical-design
  - program-spec
  - file-spec
  - ut-plan
  - test-scaffold
  - spec-review
  - dds-review
  - code-review
traceability:
  keyPattern: "BR-[0-9]+"
tiering:
  enabled: true
  values: [L1, L2, L3]
```

## Non-Functional Requirements

| Category | Requirement |
|---|---|
| Security | External source credentials must be isolated in integration/agent runtime |
| Audit | Source creation, review decisions, and manifests are auditable |
| Performance | Requirement detail renders without waiting for all external refreshes |
| Resilience | GitHub fetch failures are section-level errors |
| Extensibility | New source systems and profiles do not require per-profile schema changes |

