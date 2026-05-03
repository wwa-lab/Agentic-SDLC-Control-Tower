import type {
  FreshnessStatus,
  RequirementControlPlaneListSummary,
  RequirementTraceability,
} from '../types/requirement';

export type TriageQueue = 'all' | 'stale' | 'missing' | 'errors' | 'aligned';

export function queueForStatus(status?: FreshnessStatus): TriageQueue {
  if (status === 'FRESH') return 'aligned';
  if (status === 'ERROR') return 'errors';
  if (status === 'SOURCE_CHANGED' || status === 'DOCUMENT_CHANGED_AFTER_REVIEW') return 'stale';
  if (status === 'MISSING_SOURCE' || status === 'MISSING_DOCUMENT') return 'missing';
  return 'missing';
}

export function summarizeTraceability(trace: RequirementTraceability): RequirementControlPlaneListSummary {
  const documentStages = trace.documents.stages;
  const missingDocumentCount = documentStages.filter(s => s.missing).length;
  const staleReviewCount = trace.reviews.filter(r => r.stale).length;
  const statuses = trace.freshness.map(item => item.status);

  let status: RequirementControlPlaneListSummary['status'] = 'FRESH';
  if (statuses.includes('ERROR'))                               status = 'ERROR';
  else if (statuses.includes('DOCUMENT_CHANGED_AFTER_REVIEW')) status = 'DOCUMENT_CHANGED_AFTER_REVIEW';
  else if (statuses.includes('SOURCE_CHANGED'))                status = 'SOURCE_CHANGED';
  else if (statuses.includes('MISSING_SOURCE'))                status = 'MISSING_SOURCE';
  else if (statuses.includes('MISSING_DOCUMENT'))              status = 'MISSING_DOCUMENT';
  else if (statuses.includes('UNKNOWN'))                       status = 'UNKNOWN';

  const message =
    status === 'FRESH'                          ? 'Sources, docs, and reviews are aligned'
    : status === 'DOCUMENT_CHANGED_AFTER_REVIEW' ? 'Document changed after business review'
    : status === 'SOURCE_CHANGED'               ? 'Source changed after document generation'
    : status === 'MISSING_SOURCE'               ? 'No authoritative source linked'
    : status === 'MISSING_DOCUMENT'             ? `${missingDocumentCount} expected docs missing`
    : 'Freshness needs attention';

  return {
    requirementId: trace.requirementId,
    sourceCount: trace.sources.length,
    documentCount: documentStages.filter(s => !s.missing).length,
    missingDocumentCount,
    staleReviewCount,
    artifactCount: trace.artifactLinks.length,
    status,
    message,
  };
}
