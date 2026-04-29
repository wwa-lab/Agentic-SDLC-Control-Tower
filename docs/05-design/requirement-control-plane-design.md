# Requirement Control Plane Design

## Purpose

This design document describes concrete implementation changes for evolving
Requirement Management into a control-plane surface for source references,
GitHub-backed SDD document viewing, business review, profile-driven stages, and
CLI-agent execution requests.

## Traceability

- Requirements: [requirement-control-plane-requirements.md](../01-requirements/requirement-control-plane-requirements.md)
- Stories: [requirement-control-plane-stories.md](../02-user-stories/requirement-control-plane-stories.md)
- Spec: [requirement-control-plane-spec.md](../03-spec/requirement-control-plane-spec.md)
- Architecture: [requirement-control-plane-architecture.md](../04-architecture/requirement-control-plane-architecture.md)
- Data Model: [requirement-control-plane-data-model.md](../04-architecture/requirement-control-plane-data-model.md)

## Frontend Structure

Add these files under `frontend/src/features/requirement/`:

```text
views/
  RequirementSkillFlowView.vue

components/
  SourceReferencesPanel.vue
  SourceReferenceCard.vue
  SddDocumentsPanel.vue
  SddDocumentStageRow.vue
  GitHubMarkdownViewer.vue
  BusinessReviewPanel.vue
  ReviewHistoryList.vue
  RequirementTraceabilityPanel.vue

types/
  controlPlane.ts

api/
  requirementControlPlaneApi.ts
```

Update:

```text
views/RequirementDetailView.vue
views/RequirementListView.vue
stores/requirementStore.ts
profiles/standardSddProfile.ts
profiles/ibmIProfile.ts
types/requirement.ts
```

## UI Layout

Requirement detail should keep the existing header and description, then add
control-plane sections:

```text
Requirement Header
Profile Strip
Source References
SDD Documents
Selected Document Viewer
Business Review
Review Readiness
Jira Stories
```

The SDD Documents section is the new primary bridge to GitHub `docs/`.
Jira Stories are a read-only projection from Jira source references. Control
Tower should show cached story title/status, source freshness, last synced time,
Open in Jira, and Refresh from Jira, but it should not become the editor or
source of truth for story content.

The legacy SDLC Chain card should not be mounted in Requirement Detail. Its
`REQ -> STORY -> SPEC -> DESIGN -> CODE -> TEST` model overlaps with the GitHub
SDD document panel and does not generalize cleanly to IBM i or other legacy
delivery profiles. A future trace view should be branch- and document-aware
instead.

The legacy Analysis Snapshot card should not be mounted in Requirement Detail.
Requirement quality analysis belongs in intake/normalize or agent output flows;
the review page should keep focus on source references, GitHub SDD documents,
business review, and readiness.

Requirement Management also exposes a dedicated Skill & Document Flow page at
`/requirements/skill-flow`. It is a profile-level capability map rather than a
single requirement review surface. The page shows skill input documents, output
documents, upstream skill dependencies, document-to-document dependencies, and
path patterns from the active SDD profile.

## Component Contracts

### SourceReferencesPanel

Props:

```ts
interface Props {
  requirementId: string;
  sources: readonly SourceReference[];
  loading: boolean;
  error: string | null;
}
```

Events:

```ts
addSource(url: string): void
refreshSource(sourceId: string): void
```

### SddDocumentsPanel

Props:

```ts
interface Props {
  requirementId: string;
  activeProfileId: string;
  stages: readonly SddDocumentIndex[];
  selectedDocumentId: string | null;
}
```

Events:

```ts
selectDocument(documentId: string): void
refreshDocuments(): void
requestAgentRun(stageKey: string): void
```

### GitHubMarkdownViewer

Props:

```ts
interface Props {
  content: DocumentContent | null;
  loading: boolean;
  error: string | null;
}
```

Displays Markdown, repo/path, commit SHA, blob SHA, fetched time, and Open in
GitHub link.

### BusinessReviewPanel

Props:

```ts
interface Props {
  documentContent: DocumentContent | null;
  reviews: readonly DocumentReview[];
}
```

Events:

```ts
comment(text: string): void
decide(decision: 'APPROVED' | 'CHANGES_REQUESTED' | 'REJECTED', text?: string): void
```

The UI must require `text` when `decision` is `REJECTED`, and the backend should
reject empty rejection reasons as a validation error. Approval can remain a
single-click action.

Agent run history is not a primary BA-facing panel. Control Tower should keep
agent run manifests and artifact links for audit, callback handling, and
developer diagnostics, but Requirement Detail should surface the business
outcome through SDD document freshness, review status, traceability, and GitHub
links instead of showing a standalone CLI execution log.

## Backend Structure

Add:

```text
domain/requirement/source/
  SourceReferenceService.java
  SourceReferenceEntity.java
  SourceReferenceRepository.java
  SourceReferenceDto.java

domain/requirement/document/
  SddDocumentService.java
  SddDocumentIndexEntity.java
  SddDocumentIndexRepository.java
  SddDocumentDto.java
  DocumentContentDto.java

domain/requirement/review/
  DocumentReviewService.java
  DocumentReviewEntity.java
  DocumentReviewRepository.java
  DocumentReviewDto.java

domain/requirement/agent/
  RequirementAgentRunService.java
  AgentRunEntity.java
  AgentRunRepository.java
  ArtifactLinkEntity.java
  ArtifactLinkRepository.java
  AgentRunDto.java
  AgentRunCallbackDto.java

domain/requirement/freshness/
  FreshnessService.java
  RequirementTraceabilityDto.java

platform/github/
  GitHubDocumentGateway.java
```

Use explicit bean names if simple class names collide with other slices.

### Provider Configuration

Requirement Control Plane uses provider interfaces so local demos can stay
deterministic while integration environments can hit real systems.

Default local mode:

```text
app.requirement-control-plane.jira.provider=stub
app.requirement-control-plane.confluence.provider=stub
app.requirement-control-plane.github.provider=stub
```

Real-provider mode is enabled per integration:

```text
REQUIREMENT_CP_JIRA_PROVIDER=real
JIRA_BASE_URL=https://your-domain.atlassian.net
JIRA_EMAIL=you@example.com
JIRA_API_TOKEN=...

REQUIREMENT_CP_CONFLUENCE_PROVIDER=real
CONFLUENCE_BASE_URL=https://your-domain.atlassian.net/wiki
CONFLUENCE_EMAIL=you@example.com
CONFLUENCE_API_TOKEN=...

REQUIREMENT_CP_GITHUB_PROVIDER=real
GITHUB_TOKEN=...
```

The Jira and Confluence source providers refresh metadata only. They do not
edit Jira issues, mutate Confluence pages, or import entire external work items
into the requirement database. The current metadata contract is:

```text
sourceType, externalId, title, url, sourceUpdatedAt, fetchedAt,
freshnessStatus, errorMessage
```

The GitHub document gateway has two implementations:

- `LocalGitHubDocumentGateway` (`provider=stub`) returns deterministic Markdown,
  commit SHA, blob SHA, and GitHub URLs for local E2E.
- `RealGitHubDocumentGateway` (`provider=real`) lists Markdown files from the
  configured SDD repo branch under `docs/` and fetches file content through the
  GitHub REST API.

Provider failures are stored on the affected source reference as
`freshnessStatus=ERROR` plus `errorMessage`, so the rest of the detail page can
continue rendering.

## Route and API Client

Existing route `/requirements/:requirementId` stays. The new sections load
additional data after the main detail load.

Suggested store actions:

```ts
fetchSourceReferences(requirementId)
addSourceReference(requirementId, url)
refreshSourceReference(sourceId)
fetchSddDocuments(requirementId)
openSddDocument(documentId)
submitDocumentReview(documentId, request)
requestAgentRun(requirementId, request)
fetchAgentRuns(requirementId)
fetchTraceability(requirementId)
```

## Visual Design

- Keep operational density consistent with the current app.
- Do not build a marketing-style document page.
- Source references render as compact rows/cards with system badges.
- Freshness states use restrained status chips.
- GitHub document viewer should feel like a readable work surface, not a modal
  buried inside a card.
- Review actions should be near document version metadata to make version-bound
  decisions obvious.

## Profile Design

Update profile types to include document stages:

```ts
interface SddDocumentStage {
  readonly key: string;
  readonly label: string;
  readonly defaultPathPattern: string;
  readonly required: boolean;
  readonly reviewGate?: string | null;
  readonly skillKey?: string | null;
}
```

At runtime, profile stages resolve into document instances. A stage can produce
multiple instances; the UI groups them under the stage label instead of assuming
one row per stage.

```ts
interface SddDocumentInstance {
  readonly documentInstanceKey: string | null;
  readonly title: string;
  readonly titleSource: 'FRONTMATTER' | 'H1' | 'PATH_BASENAME' | 'TOKEN_CONTEXT' | 'PROFILE_LABEL';
  readonly pathPattern: string;
  readonly path: string;
  readonly pathVariables: Record<string, string>;
  readonly unresolvedTokens: readonly string[];
  readonly missing: boolean;
}

interface SddDocumentStageGroup {
  readonly key: string;
  readonly label: string;
  readonly documents: readonly SddDocumentInstance[];
}
```

Profiles can optionally define executable skill/document contracts and document
dependency edges:

```ts
interface SkillDocumentContract {
  readonly skillId: string;
  readonly label: string;
  readonly description: string;
  readonly inputDocuments: readonly string[];
  readonly outputDocuments: readonly string[];
  readonly dependsOnSkills: readonly string[];
}

interface DocumentDependencyDefinition {
  readonly from: string;
  readonly to: string;
  readonly reason: string;
}
```

Profiles may also define `skillFlowDocuments` for artifacts that belong in the
Skill & Document Flow page but should not appear as SDD documents in Requirement
Detail. IBM i uses this for raw input, existing RPGLE/CLLE source, program
analysis, impact analysis, generated code, DDS source, compile precheck report,
and workflow routing manifest nodes.

If a profile does not define these contracts or flow documents, the UI falls
back to a lightweight view based on existing profile skills and sequential SDD
document stages.

Project isolation is branch-based: `central SDD repo + project branch + path`.
The final file name is generated for readability and traceability, not as the
project boundary. Missing documents should display resolved paths when token
values are known, and raw template paths only when a token is still unknown.

Standard Java profile stages:

```text
requirement, user-story, spec, architecture, design, tasks, code, test
```

IBM i profile stages:

```text
requirement-normalizer, functional-spec, technical-design, program-spec,
file-spec, ut-plan, test-scaffold, spec-review, dds-review, code-review
```

## Document Quality Gate Design

Document Quality Gate is an enhancement to the review surface. The UI triggers
or displays results from the `document-quality-gate` skill; it does not score
documents locally except for trivial display fallback states.

### User Experience

- SDD document rows show quality state: Not Scored, Scoring, Excellent, Good,
  Blocked, or Stale.
- Each row exposes `Run Gate` or `Re-run Gate` when the caller has permission.
- Business Review disables approval when the latest quality gate result is
  missing, stale, or blocked by policy.
- Blocked results show score, rubric version, blocking findings, and suggested
  remediation.
- Good results may continue but show advisory findings.
- Excellent results continue without blocking findings.

### Skill and Rubric Model

The platform exposes one quality gate skill entry point and delegates rubric
selection to policy:

```text
document-quality-gate(documentId, profileId, sddType)
```

Rubric resolution:

```text
Platform default rubric
-> Workspace / Team override
-> Project override
-> Profile rubric
-> SDD document type rubric
-> Tier / entry-path refinements
```

This keeps UI and gating logic stable while allowing Standard SDD, IBM i, and
future profiles to score documents differently.

### Multi-Team and Multi-Project Behavior

Quality gate policy is scoped. A team may inherit platform defaults or override
thresholds, required checks, and weights. Projects may apply stricter overrides
or approved exceptions. Runs may be created for:

- One selected document
- One requirement and its indexed SDD documents
- One project snapshot
- One release scope
- All stale documents visible to the caller

Each run stores workspace/team/project/requirement/document scope so audit and
dashboards can compare scores across teams and projects without mixing policy
contexts.

### Permission Model

| Action | Default role |
|---|---|
| Run business-facing document gate | BA, PM |
| Run technical document gate | Tech Lead, Architect |
| Run test document gate | QA Lead |
| Run implementation-facing assigned gate | Engineer |
| Run any gate or batch scope | Platform Admin |
| Modify skill code | Platform Admin, Skill Maintainer |
| Modify rubric template | Platform Admin, Process Owner |
| Modify team policy override | Team Admin, Delivery Owner |
| Approve project exception | Delivery Owner |

Ordinary users can trigger scoring but cannot lower standards. Any bypass,
threshold change, or project exception requires a reason and audit record.

### Data Shape

```ts
interface DocumentQualityGateResult {
  readonly executionId: string;
  readonly documentId: string;
  readonly profileId: string;
  readonly sddType: string;
  readonly score: number;
  readonly band: 'EXCELLENT' | 'GOOD' | 'BLOCKED';
  readonly passed: boolean;
  readonly threshold: number;
  readonly rubricVersion: string;
  readonly commitSha: string;
  readonly blobSha: string;
  readonly dimensions: readonly QualityDimensionScore[];
  readonly findings: readonly QualityFinding[];
  readonly triggeredBy: string;
  readonly triggeredMode: 'MANUAL' | 'AUTOMATION';
  readonly scoredAt: string;
  readonly stale: boolean;
}
```

Quality results are valid only for the matching `commitSha` and `blobSha`.
When the document changes, the result becomes stale and approval is blocked
until the gate is rerun.

## Error and Empty States

| Section | Empty | Error |
|---|---|---|
| Source References | No BAU sources linked yet | Source metadata unavailable |
| SDD Documents | No GitHub docs indexed yet | GitHub document index failed |
| Markdown Viewer | Select a document | GitHub content fetch failed |
| Business Review | No comments yet | Review save failed |
| Agent Runs | No agent runs requested | Run status unavailable |
| Traceability | Not enough links yet | Freshness computation failed |

## Migration Strategy

1. Add new tables without changing existing requirement tables.
2. Keep existing import/normalize flow.
3. Add source references and GitHub docs as new sections.
4. Gradually move Generate Stories/Spec buttons toward agent run requests.
5. Keep existing cards during transition to avoid breaking current demos.
