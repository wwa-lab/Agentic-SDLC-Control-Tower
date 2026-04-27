# Requirement Control Plane API Implementation Guide

## Purpose

This guide defines API contracts for the Requirement Control Plane adjustment.
All responses use the shared `ApiResponse<T>` envelope unless stated otherwise.

## Base Path

```text
/api/v1/requirements
```

## 1. Source References

### GET `/api/v1/requirements/{requirementId}/sources`

Returns source references linked to a requirement.

```json
{
  "data": [
    {
      "id": "src-001",
      "requirementId": "REQ-1024",
      "sourceType": "JIRA",
      "externalId": "PAY-123",
      "title": "Payment reconciliation enhancement",
      "url": "https://jira.example.com/browse/PAY-123",
      "sourceUpdatedAt": "2026-04-27T08:12:00Z",
      "fetchedAt": "2026-04-27T09:00:00Z",
      "freshnessStatus": "FRESH",
      "errorMessage": null
    }
  ],
  "error": null
}
```

### POST `/api/v1/requirements/{requirementId}/sources`

Request:

```json
{
  "sourceType": "JIRA",
  "url": "https://jira.example.com/browse/PAY-123"
}
```

Response: `SourceReferenceDto`

### POST `/api/v1/requirements/sources/{sourceId}/refresh`

Refreshes metadata from the source provider. Response: `SourceReferenceDto`.

## 2. SDD Documents

### GET `/api/v1/requirements/{requirementId}/sdd-documents`

Returns profile stages merged with indexed GitHub documents.

```json
{
  "data": {
    "requirementId": "REQ-1024",
    "profileId": "ibm-i-sdd",
    "stages": [
      {
        "id": "doc-001",
        "sddType": "functional-spec",
        "stageLabel": "Functional Spec",
        "title": "Payment Reconciliation Functional Spec",
        "repoFullName": "wwa-lab/payment-app",
        "branchOrRef": "main",
        "path": "docs/02-functional-spec/payment-reconciliation.md",
        "latestCommitSha": "abc123",
        "latestBlobSha": "def456",
        "githubUrl": "https://github.com/wwa-lab/payment-app/blob/main/docs/02-functional-spec/payment-reconciliation.md",
        "status": "IN_REVIEW",
        "freshnessStatus": "FRESH",
        "missing": false
      },
      {
        "id": null,
        "sddType": "program-spec",
        "stageLabel": "Program Spec",
        "title": "Program Spec",
        "repoFullName": null,
        "branchOrRef": null,
        "path": "docs/04-program-spec/",
        "latestCommitSha": null,
        "latestBlobSha": null,
        "githubUrl": null,
        "status": "MISSING",
        "freshnessStatus": "MISSING_DOCUMENT",
        "missing": true
      }
    ]
  },
  "error": null
}
```

### GET `/api/v1/requirements/documents/{documentId}`

Fetches latest Markdown content from GitHub.

```json
{
  "data": {
    "document": {
      "id": "doc-001",
      "sddType": "functional-spec",
      "stageLabel": "Functional Spec",
      "title": "Payment Reconciliation Functional Spec",
      "repoFullName": "wwa-lab/payment-app",
      "branchOrRef": "main",
      "path": "docs/02-functional-spec/payment-reconciliation.md",
      "latestCommitSha": "abc123",
      "latestBlobSha": "def456",
      "githubUrl": "https://github.com/wwa-lab/payment-app/blob/main/docs/02-functional-spec/payment-reconciliation.md",
      "status": "IN_REVIEW",
      "freshnessStatus": "FRESH",
      "missing": false
    },
    "markdown": "# Functional Spec\n\n...",
    "commitSha": "abc123",
    "blobSha": "def456",
    "githubUrl": "https://github.com/wwa-lab/payment-app/blob/main/docs/02-functional-spec/payment-reconciliation.md",
    "fetchedAt": "2026-04-27T09:05:00Z"
  },
  "error": null
}
```

## 3. Reviews

### POST `/api/v1/requirements/documents/{documentId}/reviews`

Request:

```json
{
  "decision": "APPROVED",
  "comment": "Business scope is approved for the reviewed version.",
  "commitSha": "abc123",
  "blobSha": "def456",
  "anchorType": "DOCUMENT",
  "anchorValue": null
}
```

Response:

```json
{
  "data": {
    "id": "review-001",
    "documentId": "doc-001",
    "requirementId": "REQ-1024",
    "decision": "APPROVED",
    "comment": "Business scope is approved for the reviewed version.",
    "reviewerId": "u-123",
    "reviewerType": "BUSINESS",
    "commitSha": "abc123",
    "blobSha": "def456",
    "createdAt": "2026-04-27T09:10:00Z"
  },
  "error": null
}
```

### GET `/api/v1/requirements/{requirementId}/reviews`

Returns review history for all documents linked to a requirement.

## 4. Agent Runs

### POST `/api/v1/requirements/{requirementId}/agent-runs`

Creates a manifest for CLI-agent execution.

Request:

```json
{
  "skillKey": "ibm-i-workflow-orchestrator",
  "targetStage": "program-spec",
  "notes": "Generate the next IBM i artifact from current Jira and Functional Spec context."
}
```

Response:

```json
{
  "data": {
    "executionId": "exec-1024",
    "requirementId": "REQ-1024",
    "profileId": "ibm-i-sdd",
    "skillKey": "ibm-i-workflow-orchestrator",
    "status": "MANIFEST_READY",
    "manifest": {
      "executionId": "exec-1024",
      "requirementId": "REQ-1024",
      "profileId": "ibm-i-sdd",
      "repo": {
        "fullName": "wwa-lab/payment-app",
        "baseRef": "main"
      },
      "sources": [
        {
          "id": "src-001",
          "type": "JIRA",
          "url": "https://jira.example.com/browse/PAY-123",
          "externalId": "PAY-123",
          "versionKey": "updated-2026-04-27T08:12:00Z"
        }
      ],
      "documents": [
        {
          "id": "doc-001",
          "type": "functional-spec",
          "path": "docs/02-functional-spec/payment-reconciliation.md",
          "commitSha": "abc123",
          "blobSha": "def456"
        }
      ],
      "output": {
        "docsRoot": "docs/",
        "targetStage": "program-spec"
      }
    },
    "artifactLinks": [],
    "createdAt": "2026-04-27T09:12:00Z"
  },
  "error": null
}
```

### GET `/api/v1/requirements/agent-runs/{executionId}`

Returns current run status.

### POST `/api/v1/requirements/agent-runs/{executionId}/callback`

Agent callback request:

```json
{
  "status": "COMPLETED",
  "outputSummary": {
    "message": "Program Spec generated and PR opened."
  },
  "artifactLinks": [
    {
      "artifactType": "GITHUB_PR",
      "storageType": "GITHUB",
      "title": "Generate IBM i Program Spec",
      "uri": "https://github.com/wwa-lab/payment-app/pull/42",
      "repoFullName": "wwa-lab/payment-app",
      "path": null,
      "commitSha": "789abc",
      "blobSha": null,
      "status": "IN_REVIEW"
    }
  ],
  "errorMessage": null
}
```

## 5. Traceability

### GET `/api/v1/requirements/{requirementId}/traceability`

Returns sources, docs, reviews, agent runs, artifact links, and freshness states.

## Error Handling

| Status | Case | Error |
|---|---|---|
| 400 | Missing URL or invalid review version | Human-readable validation message |
| 404 | Requirement, source, document, or run not found | Resource not found message |
| 409 | Review against stale content when strict mode enabled | Version conflict |
| 502 | External source or GitHub unavailable | Provider error |
| 500 | Unexpected failure | Generic failure message |

## Testing Contracts

- Source reference creation handles provider success and provider error.
- GitHub document fetch returns markdown and SHA metadata.
- Review creation rejects missing commit/blob.
- Agent manifest includes sources, profile, documents, and output target.
- Callback updates run status and artifact links.
- Traceability includes stale review when blob changes.

