/**
 * Mock data for Incident Management view.
 * Replace with API calls when backend slice is implemented.
 */
export const MOCK_INCIDENTS = [
  {
    id: 'INC-0422',
    priority: 'P1',
    title: 'API Gateway Latency Spike (>500ms)',
    status: 'AI_INVESTIGATING',
    statusLed: 'led-crimson' as const,
  },
] as const;

export const MOCK_DIAGNOSIS_FEED = [
  { timestamp: '09:41:02', text: 'Analyzing k8s ingress logs...', type: 'normal' as const },
  { timestamp: '09:41:05', text: 'Pattern identified: SSL handshake timeout.', type: 'normal' as const },
  { timestamp: '09:41:10', text: 'SUGGESTION: Scale pod replicas to 5.', type: 'suggestion' as const },
] as const;

export const MOCK_PENDING_APPROVALS = [
  { key: 'scale-replicas', label: 'Scale Replicas (v2.4.0)' },
] as const;
