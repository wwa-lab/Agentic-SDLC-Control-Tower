import type { CatalogSummary, AiWorkspaceDeploymentSummary } from '../types/catalog';

export const MOCK_CATALOG_SUMMARY: CatalogSummary = {
  visibleApplications: 14,
  releasesLast7d: 28,
  deploysLast7d: 112,
  deploySuccessRate7d: 0.89,
  medianDeployFrequency: 1.6,
  changeFailureRate30d: 0.08,
  byLed: { GREEN: 9, AMBER: 3, RED: 1, UNKNOWN: 1 },
};

export const MOCK_AI_WORKSPACE_SUMMARY: AiWorkspaceDeploymentSummary = {
  status: 'SUCCESS',
  generatedAt: '2026-04-17T15:00:00Z',
  narrative:
    'The workspace is in healthy shape with 89% deploy success rate over the last 7 days. ' +
    'One active rollback on **Inventory Service** in PROD needs investigation. ' +
    'Payment Gateway staging failure is blocking the next release candidate. ' +
    'Overall deployment frequency is 1.6 deploys/day/app — within the target range.',
};
