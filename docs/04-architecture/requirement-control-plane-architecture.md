# Requirement Control Plane Architecture

## Overview

Requirement Control Plane extends the Requirement Management slice with a
platform-oriented architecture:

- External BAU systems provide business source references
- GitHub `docs/` provides SDD document content and version history
- Control Tower indexes metadata, renders documents, records reviews, creates
  agent manifests, and reports freshness
- CLI agents perform repo-aware and long-running work

## System Context

```mermaid
graph TD
    BA["BA / Business User"]
    DEV["Developer / Tech Lead"]
    PM["Delivery Manager"]

    CT["Control Tower Requirement Page"]
    API["Control Tower Backend"]
    DB["Control Tower DB<br/>metadata and reviews"]
    GH["GitHub<br/>repo docs and PRs"]
    JIRA["Jira"]
    CONF["Confluence"]
    KB["KB / Upload Store"]
    AGENT["CLI Agent Runtime"]
    MCP["MCP / Connectors"]

    BA --> CT
    PM --> CT
    DEV --> GH
    DEV --> AGENT

    CT --> API
    API --> DB
    API --> GH
    API --> JIRA
    API --> CONF
    API --> KB

    AGENT --> MCP
    MCP --> JIRA
    MCP --> CONF
    MCP --> GH
    AGENT --> GH
    AGENT --> API
```

## Component Architecture

```mermaid
graph TD
    subgraph Frontend["frontend/src/features/requirement"]
        Detail["RequirementDetailView"]
        SourcePanel["SourceReferencesPanel"]
        DocPanel["SddDocumentsPanel"]
        Viewer["GitHubMarkdownViewer"]
        ReviewPanel["BusinessReviewPanel"]
        AgentPanel["AgentRunsPanel"]
        TracePanel["RequirementTraceabilityPanel"]
        Store["requirementStore"]
        ApiClient["requirementApi"]
    end

    subgraph Backend["backend/domain/requirement"]
        Controller["RequirementController"]
        SourceSvc["SourceReferenceService"]
        DocSvc["SddDocumentService"]
        ReviewSvc["DocumentReviewService"]
        AgentSvc["RequirementAgentRunService"]
        FreshSvc["FreshnessService"]
    end

    subgraph Platform["backend/platform"]
        ProfileSvc["PipelineProfileService"]
        GitHubGateway["GitHubDocumentGateway"]
        SourceGateway["SourceMetadataGateway"]
    end

    Detail --> SourcePanel
    Detail --> DocPanel
    Detail --> ReviewPanel
    Detail --> AgentPanel
    Detail --> TracePanel
    DocPanel --> Viewer
    Store --> ApiClient
    ApiClient --> Controller

    Controller --> SourceSvc
    Controller --> DocSvc
    Controller --> ReviewSvc
    Controller --> AgentSvc
    Controller --> FreshSvc
    DocSvc --> GitHubGateway
    SourceSvc --> SourceGateway
    DocSvc --> ProfileSvc
```

## Backend Package Boundaries

Requirement-specific services own requirement-linked source references,
document indexes, reviews, manifests, and freshness projections. Provider-specific
logic should live behind platform gateways or agent runtime connectors.

Suggested packages:

```text
com.sdlctower.domain.requirement.source
com.sdlctower.domain.requirement.document
com.sdlctower.domain.requirement.review
com.sdlctower.domain.requirement.agent
com.sdlctower.domain.requirement.freshness
com.sdlctower.platform.github
com.sdlctower.platform.source
```

## Data Ownership

| Data | Owner | Notes |
|---|---|---|
| Jira / Confluence body | Source system | Referenced and optionally summarized, not copied as source of truth |
| SDD Markdown body | GitHub | Fetched on open |
| Source reference metadata | Control Tower DB | URL, external ID, source updated time, fetched time |
| Document index metadata | Control Tower DB | repo/path/ref/SHA/status/profile |
| Business comments and approvals | Control Tower DB | version-bound |
| Engineering review | GitHub | PR review and diff |
| Agent execution manifest | Control Tower DB | Context handoff |
| Agent outputs | GitHub plus Control Tower artifact links | PRs, docs, reports |

## Profile-Driven Rendering

```mermaid
graph TD
    Profile["Active SDD Profile"]
    Stages["Document Stage Definitions"]
    Paths["Default Path Patterns"]
    Skills["Agent Skill Bindings"]
    Gates["Review Gates"]
    UI["Requirement Detail UI"]
    Manifest["Agent Manifest"]

    Profile --> Stages
    Profile --> Paths
    Profile --> Skills
    Profile --> Gates
    Stages --> UI
    Paths --> UI
    Skills --> Manifest
    Gates --> UI
```

## Agent Boundary

Control Tower creates the manifest and records status. Agents execute outside
the web app.

```mermaid
sequenceDiagram
    participant UI as Requirement UI
    participant API as Control Tower API
    participant DB as Control Tower DB
    participant Agent as CLI Agent
    participant GH as GitHub

    UI->>API: Request agent run
    API->>DB: Create execution + manifest
    API-->>UI: Manifest ready
    Agent->>API: Fetch manifest
    Agent->>GH: Read repo and write docs/ branch
    Agent->>GH: Open PR
    Agent->>API: Callback with PR and artifacts
    API->>DB: Update execution and artifact links
    UI->>API: Refresh run status
```

## Freshness Strategy

Freshness is computed as a projection, not as a replacement for external
systems. The first implementation can compare timestamps and Git blob versions:

- `sourceUpdatedAt > documentCommitTime` means Source Changed
- `documentBlobSha != reviewedBlobSha` means Document Changed After Review
- Missing indexed document for expected profile stage means Missing Document
- Missing source for requirement means Missing Source

## Integration Risks

| Risk | Mitigation |
|---|---|
| Provider metadata differs across Jira/Confluence | Store generic metadata plus provider payload summary |
| GitHub fetch latency | Lazy fetch document content; keep metadata list fast |
| Business comments drift after doc change | Bind comments to commit/blob |
| IBM i stages do not match Java SDD | Use profile-defined stages |
| Agent executes against wrong context | Manifest pinning and stale-context callback |

