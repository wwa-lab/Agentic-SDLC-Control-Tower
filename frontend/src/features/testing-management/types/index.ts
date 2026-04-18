export * from './common';
export * from './catalog';
export * from './case';
export * from './enums';
export * from './plan';
export * from './run';
export * from './traceability';

export const TM_DEFAULT_WORKSPACE_ID = 'ws-default-001';

export const TM_DEFAULT_CATALOG_FILTER = {
  projectId: 'ALL',
  planState: 'ALL',
  coverageStatus: 'ALL',
  search: '',
} as const;

export const TM_CATALOG_CARD_KEYS = ['summary', 'grid', 'filters'] as const;
export const TM_PLAN_CARD_KEYS = ['header', 'cases', 'coverage', 'recentRuns', 'draftInbox', 'aiInsights'] as const;
export const TM_CASE_CARD_KEYS = ['detail', 'recentResults', 'revisions'] as const;
export const TM_RUN_CARD_KEYS = ['header', 'caseResults', 'coverage'] as const;
export const TM_TRACEABILITY_CARD_KEYS = ['summary', 'reqRows'] as const;

export type CatalogCardKey = (typeof TM_CATALOG_CARD_KEYS)[number];
export type PlanCardKey = (typeof TM_PLAN_CARD_KEYS)[number];
export type CaseCardKey = (typeof TM_CASE_CARD_KEYS)[number];
export type RunCardKey = (typeof TM_RUN_CARD_KEYS)[number];
export type TraceabilityCardKey = (typeof TM_TRACEABILITY_CARD_KEYS)[number];
