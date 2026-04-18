import type {
  AutonomyLevel,
  ExecutionStatus,
  RunFilterFormState,
  RunQuery,
  SdlcStageKey,
  SkillCategory,
  SkillFilterState,
  SkillStatus,
} from './types';

export const SDLC_STAGE_ORDER: SdlcStageKey[] = [
  'requirement',
  'user-story',
  'spec',
  'architecture',
  'design',
  'tasks',
  'code',
  'test',
  'deploy',
  'incident',
  'learning',
];

export const STAGE_LABELS: Record<SdlcStageKey, string> = {
  requirement: 'Requirement',
  'user-story': 'User Story',
  spec: 'Spec',
  architecture: 'Architecture',
  design: 'Design',
  tasks: 'Tasks',
  code: 'Code',
  test: 'Test',
  deploy: 'Deploy',
  incident: 'Incident',
  learning: 'Learning',
};

export const AUTONOMY_TOOLTIPS: Record<AutonomyLevel, string> = {
  'L0-Manual': 'Human only. AI can be referenced, but no execution is initiated automatically.',
  'L1-Assist': 'AI prepares suggestions and draft actions, but a human explicitly triggers execution.',
  'L2-Auto-with-approval': 'AI can execute within guardrails, but risky actions pause for human approval.',
  'L3-Auto': 'AI can execute end-to-end within policy thresholds and audit controls.',
};

export const STATUS_COLOR_TOKENS: Record<SkillStatus | ExecutionStatus, string> = {
  active: 'var(--color-health-emerald)',
  beta: 'var(--color-approval-amber)',
  deprecated: 'var(--color-on-surface-variant)',
  running: 'var(--color-approval-amber)',
  succeeded: 'var(--color-health-emerald)',
  failed: 'var(--color-incident-crimson)',
  pending_approval: 'var(--color-approval-amber)',
  rejected: 'var(--color-incident-crimson)',
  rolled_back: 'var(--color-incident-crimson)',
};

export const SKILL_STATUS_LABELS: Record<SkillStatus, string> = {
  active: 'Active',
  beta: 'Beta',
  deprecated: 'Deprecated',
};

export const EXECUTION_STATUS_LABELS: Record<ExecutionStatus, string> = {
  running: 'Running',
  succeeded: 'Succeeded',
  failed: 'Failed',
  pending_approval: 'Pending Approval',
  rejected: 'Rejected',
  rolled_back: 'Rolled Back',
};

export const SKILL_CATEGORY_LABELS: Record<SkillCategory, string> = {
  delivery: 'Delivery',
  runtime: 'Runtime',
};

export const DEFAULT_SKILL_FILTERS: SkillFilterState = {
  category: [],
  status: [],
  autonomy: [],
  owner: [],
  search: '',
  sortBy: 'lastExecutedAt',
};

export const DEFAULT_RUN_QUERY: Required<Pick<RunQuery, 'page' | 'size'>> & Omit<RunQuery, 'page' | 'size'> = {
  skillKey: [],
  status: [],
  triggerSourcePage: undefined,
  startedAfter: undefined,
  startedBefore: undefined,
  triggeredByType: undefined,
  page: 1,
  size: 50,
};

export const DEFAULT_RUN_FILTER_FORM: RunFilterFormState = {
  skillKey: [],
  status: [],
  triggerSourcePage: '',
  startedAfter: '',
  startedBefore: '',
  triggeredByType: '',
};

export const METRIC_TITLES = [
  { key: 'aiUsageRate', label: 'AI Usage Rate' },
  { key: 'adoptionRate', label: 'Adoption Rate' },
  { key: 'autoExecSuccessRate', label: 'Auto-Exec Success Rate' },
  { key: 'timeSavedHours', label: 'Time Saved' },
  { key: 'stageCoverageCount', label: 'Stage Coverage' },
] as const;
