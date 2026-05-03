# Requirement Control Plane Data Flow

## Purpose

This document describes runtime data flows for Requirement Control Plane:
source intake, GitHub document rendering, business review, agent manifest
creation, manual CLI handoff, agent stage callbacks, final agent callback, and
freshness refresh.

## 1. Source Reference Intake

```mermaid
sequenceDiagram
    participant User
    participant UI as SourceReferencesPanel
    participant Store as Requirement Store
    participant API as Requirement API
    participant Source as SourceReferenceService
    participant Gateway as SourceMetadataGateway
    participant DB as DB

    User->>UI: Paste Jira or Confluence URL
    UI->>Store: addSourceReference(url)
    Store->>API: POST /requirements/{id}/sources
    API->>Source: create(reference)
    Source->>Gateway: Resolve metadata
    Gateway-->>Source: title, externalId, sourceUpdatedAt
    Source->>DB: Save source_reference
    Source-->>API: SourceReferenceDto
    API-->>Store: SourceReferenceDto
    Store-->>UI: Render source card
```

Error behavior:

- If metadata cannot be fetched, store the URL with `status=ERROR`.
- UI shows retry action.
- Requirement detail continues rendering.

## 2. Open Latest GitHub SDD Document

```mermaid
sequenceDiagram
    participant User
    participant UI as SddDocumentsPanel
    participant API as Requirement API
    participant Docs as SddDocumentService
    participant GH as GitHubDocumentGateway
    participant DB as DB

    User->>UI: Open document
    UI->>API: GET /requirements/documents/{documentId}
    API->>Docs: fetchLatest(documentId)
    Docs->>DB: Load repo/ref/path
    Docs->>GH: Fetch Markdown and blob metadata
    GH-->>Docs: markdown, commitSha, blobSha, githubUrl
    Docs->>DB: Update latest metadata
    Docs-->>API: DocumentContentDto
    API-->>UI: Markdown + version metadata
    UI->>UI: Render Markdown
```

Rules:

- Markdown body is fetched from GitHub.
- Control Tower may update index metadata after fetch.
- The fetched content is not canonical storage in Control Tower.

## 3. Business Review

```mermaid
sequenceDiagram
    participant User
    participant UI as BusinessReviewPanel
    participant API as Requirement API
    participant Review as DocumentReviewService
    participant DB as DB

    User->>UI: Comment or approve
    UI->>API: POST /requirements/documents/{documentId}/reviews
    API->>Review: createReview(request)
    Review->>Review: Validate commitSha and blobSha
    Review->>DB: Save document_review
    Review-->>API: ReviewDto
    API-->>UI: ReviewDto
    UI->>UI: Append review history
```

Rules:

- Review requests must include commit SHA and blob SHA.
- Approval becomes stale if the document blob changes later.

## 4. Agent Run Request and Manual CLI Prompt Handoff

```mermaid
sequenceDiagram
    participant Developer
    participant UI as Requirement Detail
    participant API as Requirement API
    participant AgentSvc as RequirementAgentRunService
    participant Docs as SddDocumentService
    participant Source as SourceReferenceService
    participant Profile as PipelineProfileService
    participant DB as DB

    Developer->>UI: Prepare prompt for next stage
    UI->>API: POST /requirements/{id}/agent-runs
    API->>AgentSvc: createRun(request)
    AgentSvc->>Profile: Resolve active profile
    AgentSvc->>Source: Resolve latest source refs
    AgentSvc->>Docs: Resolve latest doc refs
    AgentSvc->>DB: Save execution + manifest
    AgentSvc->>AgentSvc: Build slash-skill prompt + callback URLs
    AgentSvc-->>API: AgentRunDto(command, callbackUrl)
    API-->>UI: AgentRunDto
    UI-->>Developer: Copyable /skill-name prompt
```

The manifest pins resolved versions at creation time. In the short-term model,
the UI does not execute the CLI directly. It prepares the run and shows the
actual prompt a user should paste into their agent terminal, for example
`/ibm-i-workflow-orchestrator please help me complete Program Spec for
REQ-1024.` Callback URLs and run IDs remain in the manifest/API response for
the CLI integration layer and should not be exposed as copy text by default.

## 5. CLI Agent Stage Events

```mermaid
sequenceDiagram
    participant Developer
    participant Runner as scripts/control-tower-run
    participant API as Requirement API
    participant AgentSvc as RequirementAgentRunService
    participant DB as DB

    Developer->>Runner: Paste prepared slash-skill prompt
    Runner->>API: POST /requirements/agent-runs/{executionId}/stage-events RUNNING
    API->>AgentSvc: recordStageEvent(event)
    AgentSvc->>DB: Save event and mark run RUNNING
    Runner->>Runner: Execute configured CLI skill command
    alt Skill succeeds
        Runner->>API: POST /stage-events DONE
        API->>AgentSvc: recordStageEvent(event)
        AgentSvc->>DB: Save event and mark run COMPLETED
    else Skill fails
        Runner->>API: POST /stage-events FAILED
        API->>AgentSvc: recordStageEvent(event)
        AgentSvc->>DB: Save event and mark run FAILED
    end
```

Stage events are lightweight facts from the runner. They answer "where is this
requirement now?" without requiring the UI to infer progress from generated
files only.

Supported stage event statuses:

- STARTED
- RUNNING
- DONE
- FAILED

## 6. CLI Agent Final Callback

```mermaid
sequenceDiagram
    participant Agent as CLI Runner or Skill
    participant API as Requirement API
    participant AgentSvc as RequirementAgentRunService
    participant Docs as SddDocumentService
    participant DB as DB

    Agent->>API: POST /requirements/agent-runs/{executionId}/callback
    API->>AgentSvc: updateStatus(callback)
    AgentSvc->>DB: Save status, output summary, errors
    alt Artifacts included
        AgentSvc->>Docs: Upsert document indexes / artifact links
        Docs->>DB: Save GitHub PR and doc metadata
    end
    AgentSvc-->>API: AgentRunDto
```

Supported callback statuses:

- RUNNING
- COMPLETED
- FAILED
- STALE_CONTEXT
- CANCELED

The callback may include artifact links and may also include embedded
stageEvents when a runner batches progress updates with its final result.

## 7. Freshness Refresh

```mermaid
sequenceDiagram
    participant UI as Requirement Detail
    participant API as Requirement API
    participant Fresh as FreshnessService
    participant Source as SourceReferenceService
    participant Docs as SddDocumentService
    participant Review as DocumentReviewService

    UI->>API: GET /requirements/{id}/traceability
    API->>Fresh: buildTraceability(id)
    Fresh->>Source: Load source refs
    Fresh->>Docs: Load indexed docs
    Fresh->>Review: Load reviews
    Fresh->>Fresh: Compute freshness states
    Fresh-->>API: TraceabilityDto
    API-->>UI: TraceabilityDto
```

Freshness states:

- FRESH
- SOURCE_CHANGED
- DOCUMENT_CHANGED_AFTER_REVIEW
- MISSING_DOCUMENT
- MISSING_SOURCE
- UNKNOWN
- ERROR

## 8. Profile-Driven Document Rendering

```mermaid
sequenceDiagram
    participant UI as Requirement Detail
    participant API as Requirement API
    participant Profile as PipelineProfileService
    participant Docs as SddDocumentService

    UI->>API: GET /requirements/{id}/sdd-documents
    API->>Profile: Resolve active profile
    API->>Docs: Load indexed documents
    Docs-->>API: Existing document refs
    API->>API: Merge profile expected stages with docs
    API-->>UI: Stage list with present/missing docs
```

The UI renders missing expected documents rather than hiding gaps.
