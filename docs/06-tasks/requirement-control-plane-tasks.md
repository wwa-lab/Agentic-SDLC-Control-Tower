# Requirement Control Plane Tasks

## Objective

Implement the Requirement Control Plane adjustment as an additive evolution of
Requirement Management. The goal is to align the slice with the platform
principles: source references, GitHub-backed SDD docs, business review, SDD
profiles, agent manifests, and freshness.

## Traceability

- Requirements: [requirement-control-plane-requirements.md](../01-requirements/requirement-control-plane-requirements.md)
- Stories: [requirement-control-plane-stories.md](../02-user-stories/requirement-control-plane-stories.md)
- Spec: [requirement-control-plane-spec.md](../03-spec/requirement-control-plane-spec.md)
- Architecture: [requirement-control-plane-architecture.md](../04-architecture/requirement-control-plane-architecture.md)
- Data Flow: [requirement-control-plane-data-flow.md](../04-architecture/requirement-control-plane-data-flow.md)
- Data Model: [requirement-control-plane-data-model.md](../04-architecture/requirement-control-plane-data-model.md)
- Design: [requirement-control-plane-design.md](../05-design/requirement-control-plane-design.md)
- API Guide: [requirement-control-plane-API_IMPLEMENTATION_GUIDE.md](../05-design/contracts/requirement-control-plane-API_IMPLEMENTATION_GUIDE.md)

## Phase 0: Alignment and Contracts

- [ ] Confirm this SDD package is accepted as the next-stage Requirement
      Management adjustment scope
- [ ] Keep existing Requirement Management behavior during transition
- [ ] Decide initial source providers for Day 1: manual URL metadata first,
      provider fetch second
- [ ] Decide initial GitHub access path: GitHub app connector, PAT-backed
      backend gateway, or existing internal gateway
- [ ] Decide default repo/branch resolution for a requirement/project

## Phase 1: Data Model and Backend Foundations

- [ ] Add Flyway migration for `requirement_source_reference`
- [ ] Add Flyway migration for `requirement_sdd_document_index`
- [ ] Add Flyway migration for `requirement_document_review`
- [ ] Add Flyway migration for `requirement_agent_run`
- [ ] Add Flyway migration for `requirement_artifact_link`
- [ ] Create source reference entity/repository/service/DTO
- [ ] Create SDD document index entity/repository/service/DTO
- [ ] Create document review entity/repository/service/DTO
- [ ] Create agent run and artifact link entities/repositories/services/DTOs
- [ ] Add freshness service projection

## Phase 2: Source References

- [ ] Add `GET /requirements/{id}/sources`
- [ ] Add `POST /requirements/{id}/sources`
- [ ] Add `POST /requirements/sources/{sourceId}/refresh`
- [ ] Add `SourceReferencesPanel.vue`
- [ ] Add `SourceReferenceCard.vue`
- [ ] Support Jira, Confluence, GitHub, KB, upload, and generic URL source
      types in DTOs
- [ ] Add mocked source refs for DEV mode
- [ ] Add section-level error and retry states

## Phase 3: GitHub SDD Document Index and Viewer

- [ ] Add GitHub document gateway abstraction
- [ ] Add `GET /requirements/{id}/sdd-documents`
- [ ] Add `GET /requirements/documents/{documentId}`
- [ ] Add `SddDocumentsPanel.vue`
- [ ] Add `SddDocumentStageRow.vue`
- [ ] Add `GitHubMarkdownViewer.vue`
- [ ] Render profile expected stages with present/missing document state
- [ ] Show GitHub URL, repo, path, commit SHA, and blob SHA
- [ ] Ensure Markdown content is fetched from GitHub and not treated as DB
      source of truth

## Phase 4: Business Review

- [ ] Add `POST /requirements/documents/{documentId}/reviews`
- [ ] Add `GET /requirements/{id}/reviews`
- [ ] Validate commit SHA and blob SHA for review creation
- [ ] Add `BusinessReviewPanel.vue`
- [ ] Add `ReviewHistoryList.vue`
- [ ] Support COMMENT, APPROVED, CHANGES_REQUESTED, REJECTED decisions
- [ ] Mark approval stale when latest blob differs from reviewed blob
- [ ] Add tests for version-bound review behavior

## Phase 5: Profile-Driven Stage Rendering

- [ ] Extend frontend profile model with document stage definitions
- [ ] Update Standard Java profile stages and path patterns
- [ ] Rewrite IBM i profile based on `build-agent-skill` workflow:
      Requirement Normalizer, Functional Spec, Technical Design, Program Spec,
      File Spec, UT Plan, Test Scaffold, Spec Review, DDS Review, Code Review
- [ ] Include IBM i BR-xx traceability and L1/L2/L3 tiering metadata
- [ ] Make Requirement detail document stages entirely profile-driven
- [ ] Remove hardcoded Java labels from new control-plane sections

## Phase 6: Agent Run Manifest

- [ ] Add `POST /requirements/{id}/agent-runs`
- [ ] Add `GET /requirements/agent-runs/{executionId}`
- [ ] Add `POST /requirements/agent-runs/{executionId}/callback`
- [ ] Generate manifest with requirement, profile, repo, branch/ref, source
      references, document references, output expectations, and constraints
- [ ] Pin source/document versions at manifest creation time
- [ ] Add `AgentRunsPanel.vue`
- [ ] Add `AgentRunCard.vue`
- [ ] Show MANIFEST_READY, RUNNING, COMPLETED, FAILED, STALE_CONTEXT states
- [ ] Store artifact links from callbacks

## Phase 7: Freshness and Traceability

- [ ] Add `GET /requirements/{id}/traceability`
- [ ] Compute source-to-document freshness
- [ ] Compute review-to-document freshness
- [ ] Add `RequirementTraceabilityPanel.vue`
- [ ] Use common freshness states:
      FRESH, SOURCE_CHANGED, DOCUMENT_CHANGED_AFTER_REVIEW, MISSING_DOCUMENT,
      MISSING_SOURCE, UNKNOWN, ERROR
- [ ] Add visual freshness chips in source and document panels
- [ ] Add tests for stale source and stale review scenarios

## Phase 8: Integration With Existing Requirement Features

- [ ] Keep existing import/normalize flow operational
- [ ] Reposition Normalize with AI as assisted intake
- [ ] Add source reference creation from confirmed import draft
- [ ] Transition Generate Stories / Generate Spec actions toward Request Agent
      Run in the new document panel
- [ ] Keep existing linked stories/specs cards during migration
- [ ] Update Requirement detail ordering to emphasize sources and GitHub docs

## Phase 9: Verification

- [ ] Backend unit tests for services and freshness calculations
- [ ] Backend controller tests for all new endpoints
- [ ] Frontend store tests for source refs, document viewer, reviews, and agent
      runs
- [ ] UI smoke test for Requirement detail with Standard Java profile
- [ ] UI smoke test for Requirement detail with IBM i profile
- [ ] Verify GitHub fetch section-level failure does not break page
- [ ] Verify review decision is tied to commit/blob metadata
- [ ] Verify agent callback creates artifact links and updates run status

## Rollout Notes

- Roll out as additive sections first.
- Do not remove existing Requirement cards until the new source/doc/review flow
  is stable.
- Start with manual source metadata and mocked GitHub documents if integration
  credentials are not ready.
- Use feature flags if demo environments must keep the current simplified flow.

