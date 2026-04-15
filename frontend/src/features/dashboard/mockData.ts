/**
 * Mock data for Dashboard view.
 * Replace with API calls when backend slice is implemented.
 */
export const DASHBOARD_SIGNALS = {
  valueStory: {
    label: 'VALUE_STORY',
    metrics: [
      { key: 'mttr', label: 'MTTR', value: '12.4m' },
      { key: 'ai_conf', label: 'AI_CONF', value: '98.2%' },
    ],
  },
  aiOperations: {
    label: 'AI_OPERATIONS',
    bars: [
      { key: 'fix_gen', label: 'FIX_GEN', percent: 80 },
      { key: 'root_cause', label: 'ROOT_CAUSE', percent: 65 },
    ],
  },
} as const;
