import type { TeamMetricItem, TeamMetrics } from '../types/metrics';

function createMetric(
  workspaceId: string,
  key: TeamMetricItem['key'],
  label: string,
  currentValue: number,
  previousValue: number,
  unit: TeamMetricItem['unit'],
  trend: TeamMetricItem['trend'],
  tooltip: string,
): TeamMetricItem {
  return {
    key,
    label,
    currentValue,
    previousValue,
    unit,
    trend,
    historyLink: `/reports/metric/${encodeURIComponent(key)}?workspaceId=${workspaceId}`,
    tooltip,
  };
}

export function metrics(workspaceId: string): TeamMetrics {
  return {
    deliveryEfficiency: [
      createMetric(workspaceId, 'delivery.cycleTime', 'Cycle Time', 4.2, 5.1, 'DAYS', 'DOWN', 'Average days from Approved to Delivered'),
      createMetric(workspaceId, 'delivery.throughput', 'Throughput', 18, 15, 'COUNT', 'UP', 'Requirements delivered per week'),
    ],
    quality: [
      createMetric(workspaceId, 'quality.defectRate', 'Defect Rate', 2.1, 2.8, 'PERCENT', 'DOWN', 'Escaped defects per release'),
      createMetric(workspaceId, 'quality.testPassRate', 'Test Pass Rate', 96, 94, 'PERCENT', 'UP', 'Automated suite pass rate'),
    ],
    stability: [
      createMetric(workspaceId, 'stability.mttr', 'MTTR', 1.8, 2.4, 'HOURS', 'DOWN', 'Mean time to recover critical incidents'),
      createMetric(workspaceId, 'stability.incidentFrequency', 'Incident Frequency', 3, 4, 'COUNT', 'DOWN', 'Open incidents in the last 30 days'),
    ],
    governanceMaturity: [
      createMetric(workspaceId, 'governance.approvalCoverage', 'Approval Coverage', 91, 88, 'PERCENT', 'UP', 'Specs with an explicit approval record'),
      createMetric(workspaceId, 'governance.auditCompleteness', 'Audit Completeness', 97, 95, 'PERCENT', 'UP', 'Required audit fields present across workflows'),
    ],
    aiParticipation: [
      createMetric(workspaceId, 'ai.participationRate', 'AI Participation', 74, 69, 'PERCENT', 'UP', 'Requirements with AI assistance in the chain'),
      createMetric(workspaceId, 'ai.acceptanceRate', 'AI Acceptance', 63, 61, 'PERCENT', 'UP', 'Accepted AI suggestions over the last 30 days'),
    ],
    lastRefreshed: '2026-04-17T01:00:00Z',
  };
}
