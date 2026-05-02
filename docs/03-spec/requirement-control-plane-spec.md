# Requirement Control Plane Spec

## Overview

Requirement Control Plane is the next-stage evolution of Requirement Management.
It reframes the page as a control surface for business source intake,
GitHub-backed SDD document review, profile-driven document chains, CLI-agent
execution requests, and freshness tracking.

The system does not make Control Tower the document repository. It treats
Jira/Confluence/uploads/KB as source references, central SDD repositories as the
SDD artifact source of truth, and central Knowledge Base repositories as
generated knowledge graph outputs derived from released SDD baselines.

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
- Render Jira stories as a read-only projection from Jira references or JQL
  mappings. Control Tower may cache title/status and sync metadata, but Jira
  remains the source of truth for story content and workflow state.
- Preserve existing Normalize with AI as assisted intake, not as canonical SDD
  document generation.

### F-RCP-DOCS: GitHub SDD Document Viewing

- Index SDD documents by repo, branch/ref, path, SDD type, profile, commit SHA,
  blob SHA, status, and GitHub URL.
- Resolve document context from Application, SNOW Group, central SDD repo, base
  branch, and project working branch.
- Treat Application and SNOW Group as selected filter context in Requirement
  detail. Ownership mapping is configured in Platform Center/workspace
  administration, then resolved into the SDD document index.
- Accept the currently selected SDD profile when resolving document stages so
  IBM i, Standard SDD, and future profiles render their own expected document
  chain instead of falling back to whatever profile was indexed first.
- Fetch latest Markdown content from GitHub on document open.
- Render Markdown inside Control Tower with version metadata.
- Provide Open in GitHub link.
- Show missing expected documents from profile definitions.
- Resolve known path tokens before rendering missing documents, so users see
  `docs/.../BR-123.md` when `BR-123` is known instead of only `{br-id}`.
- Display the indexed Markdown title as the document name; keep profile stage
  label as classification metadata.
- Support one-to-many stage groups where a stage such as IBM i Program Spec or
  File Spec contains multiple document instances.

### F-RCP-WORKSPACE: Project SDD Workspace

- Treat central SDD repo `main` as the released baseline.
- Create or select a project working branch from `main` for in-flight work.
- Show the selected Application, SNOW Group, source repo, central SDD repo, base
  branch, and working branch in Requirement detail.
- Keep Application and SNOW Group read-only in the Requirement SDD workspace
  panel; users change those mappings in the platform configuration surface.
- Require document review and agent manifests to use the selected working
  branch during project development.

### F-RCP-KB: Central Knowledge Base Handoff

- Treat the Central Knowledge Base repo as generated output, not as a manually
  edited SDD source.
- Expose Knowledge Base repo, main branch, preview branch, and graph manifest
  path in the SDD document index response.
- Allow released `main` SDD content to drive Knowledge Base `main` sync.
- Allow project SDD branches to drive Knowledge Base preview branches.

### F-RCP-REVIEW: Business Review

- Allow document-level comments.
- Allow decisions: Approved, Changes Requested, Rejected.
- Require a non-empty reason when the decision is Rejected.
- Bind each review action to commit SHA and blob SHA.
- Show review history grouped by document and version.
- Mark approvals stale when a newer blob exists.

### F-RCP-PROFILE: Profile-Driven SDD

- Use active SDD profile to render document stages.
- Standard SDD profile renders the real `.claude/skills` family as 10 skill
  nodes: `req-to-user-story`, `user-story-to-spec`, `spec-to-architecture`,
  `architecture-review`, `architecture-to-design`, `design-to-tasks`,
  `tasks-to-code`, `tasks-to-implementation`,
  `review-code-against-design`, and `review-doc-quality`.
- Standard SDD main Chain is the user-facing stage path:
  Requirement -> User Story -> Spec -> Architecture -> Design -> Tasks -> Code
  -> Review.
- Standard SDD supporting artifacts are shown in document and dependency maps,
  not as peer Chain stages: Data Flow and Data Model belong to
  Architecture/Design support, and API Implementation Guide belongs to Design
  support.
- IBM i profile renders IBM i chain from `build-agent-skill` concepts and
  exposes all 16 IBM i skills as separate flow nodes, not just the workflow
  orchestrator.
- The 16 IBM i flow nodes are sourced from upstream `.claude/ibm-i-*` skill
  folders: requirement normalizer, program analyzer, impact analyzer,
  functional spec, technical design, program spec, file spec, code generator,
  DDS generator, UT plan, test scaffold, compile precheck, spec reviewer, DDS
  reviewer, code reviewer, and workflow orchestrator.
- Profile defines document stages, default path patterns, skill bindings, review
  gates, traceability key rules, and optional tiering.
- Profile path patterns are templates only. Runtime document instances resolve
  token values from requirement/source/project context and agent manifests.
- Provide a Skill & Document Flow page that displays each profile skill's input
  documents, output documents, skill dependencies, document dependencies, and
  path patterns as a reusable capability map.
- Allow the Skill & Document Flow page to use profile-specific flow artifacts
  separate from Requirement Detail SDD stages, so source code, DDS source,
  generated code, reports, and manifests can be mapped without appearing as
  missing SDD documents.

### F-RCP-AGENT: CLI Agent Run Request

- UI creates an agent run request and execution manifest.
- Manifest pins latest resolved source/document versions.
- Short-term execution is a manual CLI handoff: Requirement Detail returns a
  copyable prompt that starts with the real CLI skill command, such as
  `/skill-name please help me ...`.
- Agents run outside the UI and report progress through stage-event APIs plus a
  final callback API.
- Requirement detail shows the prepared prompt and merge confirmation as the
  primary action. Execution IDs, raw statuses, stage events, final run status,
  and artifacts remain available for backend audit and diagnostics, but are not
  shown in the default user-facing action panel.
- The primary action is prioritized as a single next step: refresh changed
  sources, review changed documents, continue an in-flight CLI run, generate the
  next missing document, then refresh GitHub after merge confirmation.
- When the next step is review, `Open Document` selects the changed document and
  moves focus to Business Review. Requirement detail shows workflow progress as
  compact context by default, with the full workflow catalog collapsed.
- Business Review shows decision state, quality gate state, selected version,
  and a compact Markdown preview for the selected document so reviewers can make
  the approve/reject decision without switching context for routine checks.
- After a developer merges the generated PR, Requirement detail allows manual
  merge confirmation with a GitHub PR URL. The confirmation is recorded as a
  stage event and triggers document refresh.

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

### DR-RCP-06: Branch-aware latest

"Latest" always means latest within the selected SDD workspace branch or pinned
commit, not unconditionally the central SDD repository `main` branch.

### DR-RCP-07: Knowledge graph derivation

Knowledge graph nodes and edges must be derived from SDD documents and structured
metadata. They must not override the central SDD repository as the document
authority.

### DR-RCP-08: Document identity

An SDD document identity is `sddRepoFullName + workingBranch + path +
commitSha/blobSha`. The final file name is not sufficient to distinguish
projects or review versions.

### DR-RCP-09: Generated display names

Document display names are generated from indexed Markdown metadata. The
preferred order is frontmatter `title`, first Markdown H1, normalized path
basename, then profile stage label as a last fallback.

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
| POST | `/api/v1/requirements/documents/{documentId}/quality-gate-runs` | Run or rerun document quality gate |
| GET | `/api/v1/requirements/documents/{documentId}/quality-gate` | Get latest quality gate result |
| POST | `/api/v1/requirements/{id}/quality-gate-runs` | Run quality gates for a requirement scope |
| POST | `/api/v1/projects/{projectId}/quality-gate-runs` | Run quality gates for a project snapshot |
| POST | `/api/v1/requirements/{id}/agent-runs` | Create agent run manifest |
| GET | `/api/v1/requirements/agent-runs/{executionId}` | Get agent run status |
| POST | `/api/v1/requirements/agent-runs/{executionId}/stage-events` | Record CLI stage progress |
| POST | `/api/v1/requirements/agent-runs/{executionId}/callback` | Agent status/artifact callback |
| GET | `/api/v1/requirements/{id}/traceability` | Source/doc/review/run traceability |

## Document Quality Gate

### Scoring Contract

The quality gate uses one skill entry point:

```text
document-quality-gate(documentId, profileId, sddType)
```

The skill selects a rubric from `profileId + sddType + expectedTier +
entryPath + workspace policy + project policy`. A profile may contribute one
or more rubrics, but the UI and backend treat the result through a common
contract.

```json
{
  "executionId": "QG-123",
  "documentId": "DOC-123",
  "profileId": "ibm-i",
  "sddType": "program-spec",
  "score": 74,
  "band": "BLOCKED",
  "passed": false,
  "threshold": 80,
  "rubricVersion": "quality-gate.ibm-i.program-spec.v1",
  "commitSha": "...",
  "blobSha": "...",
  "dimensions": [
    { "key": "traceability", "score": 12, "max": 20 }
  ],
  "findings": [
    {
      "severity": "BLOCKER",
      "section": "Acceptance Criteria",
      "message": "Acceptance criteria are not testable enough."
    }
  ],
  "scoredAt": "2026-04-29T00:00:00Z"
}
```

### Bands

| Score | Band | Gate |
|---:|---|---|
| 90-100 | Excellent | Pass |
| 80-89 | Good | Pass with advisory findings |
| 0-79 | Blocked | Fail; downstream approval/generation blocked |

### Policy Scope

Quality gate policy resolves in this order:

```text
Platform default
-> Workspace / Team override
-> Project override
-> Profile + document-type rubric
```

The policy model supports multiple teams and multiple projects. Batch runs must
record workspace ID, team scope, project ID, requirement ID, document ID,
profile ID, SDD type, rubric version, commit SHA, blob SHA, trigger actor,
trigger mode, and result.

### Trigger Permissions

| Actor | Allowed triggers |
|---|---|
| BA / PM | Requirement, user-story, functional-spec, and business review gates |
| Tech Lead / Architect | Technical design, architecture, program spec, file spec gates |
| QA Lead | Test plan and test scaffold gates |
| Engineer | Implementation-facing gates for assigned work |
| Platform Admin | Any single or batch gate |
| Automation/System | After document generation, GitHub refresh, or PR update |

### Modification Permissions

| Asset | Modifier |
|---|---|
| Skill code | Platform Admin, Skill Maintainer |
| Rubric template | Platform Admin, Process Owner |
| Team policy override | Team Admin, Delivery Owner |
| Project exception | Proposed by Tech Lead; approved by Delivery Owner |
| Threshold override | Restricted; requires audit reason and effective scope |

All changes to skill code references, rubric weights, thresholds, and overrides
must create audit entries with actor, old value, new value, reason, scope, and
effective time.

## Profile Examples

### Standard SDD

```yaml
profileId: standard-sdd
stages:
  - requirement
  - user-story
  - spec
  - architecture
  - design
  - tasks
  - code
  - review
supportingArtifacts:
  architecture:
    - data-flow
    - data-model
  design:
    - api-guide
traceability:
  keyPattern: "REQ-[0-9]+"
```

### IBM i SDD

```yaml
profileId: ibm-i
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
