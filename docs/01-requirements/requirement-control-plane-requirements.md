# Requirement Control Plane Requirements

## Purpose

This document defines the next-stage requirements for evolving Requirement
Management from a requirement CRUD and AI-assisted drafting surface into a
platform control plane for business source intake, GitHub-backed SDD document
review, profile-driven SDD chains, CLI-agent execution requests, and freshness
tracking.

This is an additive alignment document. It does not replace the existing
Requirement Management SDD set. It defines the change set needed to align that
slice with the platform design principles in `CLAUDE.md`.

## Sources

- `CLAUDE.md` — Platform Design Principles
- `docs/01-requirements/requirement-requirements.md`
- `docs/03-spec/requirement-spec.md`
- `docs/04-architecture/requirement-architecture.md`
- `docs/04-architecture/requirement-data-model.md`
- `docs/05-design/requirement-design.md`
- IBM i skill family: `wwa-lab/build-agent-skill`

## 1. Scope

Requirement Control Plane covers:

- Business source intake from Jira, Confluence, uploads, and KB references
- Source reference registration, metadata refresh, and freshness checks
- GitHub `docs/` document discovery and rendering
- Business comments, approvals, and change requests bound to Git commit/blob
  versions
- Profile-driven SDD document chain rendering for Standard Java and IBM i
- CLI-agent execution manifest creation for long-running SDD production work
- Artifact links to GitHub documents, PRs, review reports, and generated outputs
- Requirement-to-source-to-document traceability

It does not cover:

- Replacing Jira or Confluence as BAU source systems
- Editing Markdown documents inside Control Tower
- Replacing GitHub PR review
- Running repo-aware generation, code changes, or tests synchronously from the UI
- Building a full workflow or DAG orchestration engine
- Building a universal document authoring system

## 2. Product Principles

### REQ-RCP-01: Control Tower as control plane

Requirement Management must treat Control Tower as the control plane and review
surface. It must not become the canonical store for SDD Markdown document bodies.

### REQ-RCP-02: External source systems retain authority

Jira, Confluence, uploads, and KB systems must remain source systems for business
input. GitHub must remain the source of truth for engineering-ready SDD
artifacts.

### REQ-RCP-03: GitHub `docs/` is the SDD artifact source

The UI must display SDD documents by reading current content from GitHub. The
platform may index metadata, but the Markdown body and version history belong to
GitHub.

### REQ-RCP-04: Business review is separate from engineering review

Business users must be able to review, comment, approve, or request changes in
Control Tower. Developers and technical leads continue to review GitHub PRs and
diffs in GitHub.

### REQ-RCP-05: Reviews are version-bound

Every comment, approval, and change request must be associated with the Git
commit SHA and blob SHA of the document version being reviewed.

## 3. Source Reference Requirements

### REQ-RCP-10: Source reference registration

Users must be able to register business sources against a requirement. Supported
source types for Day 1 are:

- Jira issue or epic URL
- Confluence page URL
- Uploaded source bundle or KB document reference
- Existing GitHub document or PR URL

### REQ-RCP-11: Source metadata

Each source reference must capture source type, external ID, title, URL, source
updated time when available, fetched time, version/checksum when available, and
linked requirement ID.

### REQ-RCP-12: Source freshness

The system must indicate when a source changed after an SDD document was
generated or reviewed. Freshness may be computed from `sourceUpdatedAt`,
checksum, external version, or provider-specific metadata.

### REQ-RCP-13: Source cards

Requirement detail must include a Source References section showing source
system, title, link, last fetched time, source updated time, freshness state, and
refresh action.

## 4. GitHub SDD Document Requirements

### REQ-RCP-20: Document index

The platform must index GitHub SDD documents associated with a requirement. The
index must store repo, branch/ref, path, SDD type, profile ID, latest commit SHA,
latest blob SHA, status, and GitHub URL.

### REQ-RCP-21: Latest document rendering

When a user opens a document in Control Tower, the backend must fetch the latest
Markdown content from GitHub for the indexed repo/path/ref and return the content
with version metadata.

### REQ-RCP-22: No canonical Markdown body in Control Tower

Control Tower must not treat cached Markdown content as authoritative. If content
is cached for performance later, it must be explicitly marked as cache and
validated against GitHub metadata.

### REQ-RCP-23: GitHub deep link

Every rendered document must provide a link to open the same document in GitHub.

### REQ-RCP-24: Document version display

The UI must show the current commit SHA, document path, last updated time when
available, and whether the user is viewing latest or a pinned reviewed version.

## 5. Business Review Requirements

### REQ-RCP-30: Business comment

Business users must be able to add document-level comments in Control Tower.
Day 1 comments may be document-level or heading-level; inline line comments are
deferred.

### REQ-RCP-31: Approval decision

Business users must be able to mark a document version as Approved, Changes
Requested, or Rejected.

### REQ-RCP-32: Version-bound decision

Approval decisions must store document ID, repo, path, commit SHA, blob SHA,
reviewer, decision, comment, and timestamp.

### REQ-RCP-33: Review history

Requirement detail must show review history grouped by document and version.

## 6. Profile-Driven SDD Requirements

### REQ-RCP-40: Active SDD profile

Requirement Management must render document stages from the active SDD profile,
not from a hardcoded Java-only chain.

### REQ-RCP-41: Standard Java profile

The Standard Java profile must support Requirement, User Story, Spec,
Architecture, Design, Tasks, Code, and Test document stages with the existing SDD
folder conventions.

### REQ-RCP-42: IBM i profile

The IBM i profile must support Requirement Normalizer, Functional Spec,
Technical Design, Program Spec, File Spec, UT Plan, Test Scaffold, Spec Review,
DDS Review, and Code Review stages. It must support BR-xx continuity, L1/L2/L3
tiering, fast-path enhancement work, program chain, and file chain concepts.

### REQ-RCP-43: Profile-owned document paths

Each profile must define expected document stages and default path patterns under
`docs/`. The UI must use these definitions to render expected and missing
documents.

### REQ-RCP-44: Profile-owned skill bindings

Each profile may define CLI-agent skill IDs and review gates. The UI may request
an agent run, but must not execute repo-aware skills synchronously.

## 7. Agent Execution Requirements

### REQ-RCP-50: Agent run request

Users with permission must be able to request an agent run from Requirement
detail. The request creates an execution manifest, not an immediate synchronous
generation inside the UI.

### REQ-RCP-51: Execution manifest

The manifest must include execution ID, project ID, requirement ID, active SDD
profile, repo, branch/ref, source references, known document references, output
expectations, and constraints.

### REQ-RCP-52: Latest resolved, then pinned execution

When creating a manifest, the platform must resolve the latest requested sources
and documents, pin versions in the manifest, and make the agent execute against
those versions.

### REQ-RCP-53: Agent callback

Agents must be able to report execution status, output summary, GitHub PR URL,
artifact links, and errors back to Control Tower.

### REQ-RCP-54: Execution visibility

Requirement detail must show recent agent runs, their statuses, requested skill,
source context, output artifacts, and failure reason when applicable.

## 8. Freshness and Traceability Requirements

### REQ-RCP-60: Requirement source freshness

The page must show whether business sources changed after the currently indexed
SDD documents were generated.

### REQ-RCP-61: Review freshness

The page must show whether a document changed after business approval.

### REQ-RCP-62: Traceability map

The page must connect requirement, source references, SDD documents, reviews,
agent executions, and GitHub artifacts in one traceability view.

### REQ-RCP-63: Stale state vocabulary

Freshness states must use a common vocabulary: Fresh, Source Changed, Document
Changed After Review, Missing Document, Missing Source, Unknown.

## 9. Non-Functional Requirements

### REQ-RCP-70: Permission boundaries

Business users may comment and approve in Control Tower. Engineering review
continues in GitHub. Agent run requests require a higher permission than read or
comment.

### REQ-RCP-71: Auditability

Source reference creation, document review decisions, manifest creation, and
agent callback updates must be auditable.

### REQ-RCP-72: Provider isolation

Jira, Confluence, GitHub, KB, and MCP-specific details must be isolated behind
integration gateways or agent manifests. UI components should use platform DTOs.

### REQ-RCP-73: Performance

Opening a Requirement detail page must not block on all external systems.
Document content fetches and source refreshes may be lazy or user-triggered.

### REQ-RCP-74: Extensibility

The model must support additional SDD profiles and source systems without
schema changes for every new profile-specific stage.

