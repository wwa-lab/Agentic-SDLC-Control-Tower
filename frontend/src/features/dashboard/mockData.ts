import type { DashboardSummary } from './types/dashboard';

export const MOCK_DASHBOARD_DATA: DashboardSummary = {
  sdlcHealth: {
    data: [
      { key: 'requirement', label: 'Requirement', status: 'healthy', itemCount: 24, isHub: false, routePath: '/requirements' },
      { key: 'user-story', label: 'User Story', status: 'healthy', itemCount: 67, isHub: false, routePath: '/requirements' },
      { key: 'spec', label: 'Spec', status: 'warning', itemCount: 12, isHub: true, routePath: '/requirements' },
      { key: 'architecture', label: 'Architecture', status: 'healthy', itemCount: 8, isHub: false, routePath: '/design' },
      { key: 'design', label: 'Design', status: 'healthy', itemCount: 15, isHub: false, routePath: '/design' },
      { key: 'tasks', label: 'Tasks', status: 'healthy', itemCount: 143, isHub: false, routePath: '/project-management' },
      { key: 'code', label: 'Code', status: 'healthy', itemCount: 89, isHub: false, routePath: '/code' },
      { key: 'test', label: 'Test', status: 'warning', itemCount: 34, isHub: false, routePath: '/testing' },
      { key: 'deploy', label: 'Deploy', status: 'healthy', itemCount: 7, isHub: false, routePath: '/deployment' },
      { key: 'incident', label: 'Incident', status: 'critical', itemCount: 3, isHub: false, routePath: '/incidents' },
      { key: 'learning', label: 'Learning', status: 'inactive', itemCount: 0, isHub: false, routePath: '/ai-center' }
    ],
    error: null
  },
  deliveryMetrics: {
    data: {
      leadTime: { label: 'Lead Time', value: '4.2d', trend: 'down', trendIsPositive: true },
      deployFrequency: { label: 'Deploy Frequency', value: '3.1/wk', trend: 'up', trendIsPositive: true },
      iterationCompletion: { label: 'Iteration Completion', value: '87%', trend: 'up', trendIsPositive: true },
      bottleneckStage: 'spec'
    },
    error: null
  },
  aiParticipation: {
    data: {
      usageRate: { label: 'Usage Rate', value: '78%', trend: 'up', trendIsPositive: true },
      adoptionRate: { label: 'Adoption Rate', value: '64%', trend: 'up', trendIsPositive: true },
      autoExecSuccess: { label: 'Auto-Exec Success', value: '92%', trend: 'stable', trendIsPositive: true },
      timeSaved: { label: 'Time Saved', value: '124h', trend: 'up', trendIsPositive: true },
      stageInvolvement: [
        { stageKey: 'requirement', involved: true, actionsCount: 45 },
        { stageKey: 'user-story', involved: true, actionsCount: 128 },
        { stageKey: 'spec', involved: true, actionsCount: 342 },
        { stageKey: 'architecture', involved: true, actionsCount: 12 },
        { stageKey: 'design', involved: true, actionsCount: 56 },
        { stageKey: 'tasks', involved: true, actionsCount: 89 },
        { stageKey: 'code', involved: true, actionsCount: 567 },
        { stageKey: 'test', involved: true, actionsCount: 234 },
        { stageKey: 'deploy', involved: true, actionsCount: 45 },
        { stageKey: 'incident', involved: false, actionsCount: 0 },
        { stageKey: 'learning', involved: true, actionsCount: 23 }
      ]
    },
    error: null
  },
  qualityMetrics: {
    data: {
      buildSuccessRate: { label: 'Build Success', value: '98.4%', trend: 'up', trendIsPositive: true },
      testPassRate: { label: 'Test Pass Rate', value: '99.1%', trend: 'stable', trendIsPositive: true },
      defectDensity: { label: 'Defect Density', value: '0.42/kloc', trend: 'down', trendIsPositive: true },
      specCoverage: { label: 'Spec Coverage', value: '84%', trend: 'up', trendIsPositive: true }
    },
    error: null
  },
  stabilityMetrics: {
    data: {
      activeIncidents: 3,
      criticalIncidents: 1,
      changeFailureRate: { label: 'Change Failure', value: '2.1%', trend: 'down', trendIsPositive: true },
      mttr: { label: 'MTTR', value: '45m', trend: 'down', trendIsPositive: true },
      rollbackRate: { label: 'Rollback Rate', value: '0.5%', trend: 'stable', trendIsPositive: true }
    },
    error: null
  },
  governanceMetrics: {
    data: {
      templateReuse: { label: 'Template Reuse', value: '82%', trend: 'up', trendIsPositive: true },
      configDrift: { label: 'Config Drift', value: '3.1%', trend: 'down', trendIsPositive: true },
      auditCoverage: { label: 'Audit Coverage', value: '91%', trend: 'up', trendIsPositive: true },
      policyHitRate: { label: 'Policy Hit Rate', value: '97%', trend: 'stable', trendIsPositive: true }
    },
    error: null
  },
  recentActivity: {
    data: {
      entries: [
        { id: '1', actor: 'Gemini Agent', actorType: 'ai', action: 'Generated Spec for Feature #452', stageKey: 'spec', timestamp: new Date(Date.now() - 1000 * 60 * 5).toISOString() },
        { id: '2', actor: 'Sarah Chen', actorType: 'human', action: 'Reviewed Architecture Design', stageKey: 'architecture', timestamp: new Date(Date.now() - 1000 * 60 * 15).toISOString() },
        { id: '3', actor: 'Codex Agent', actorType: 'ai', action: 'Implemented Component: UserAuth', stageKey: 'code', timestamp: new Date(Date.now() - 1000 * 60 * 45).toISOString() },
        { id: '4', actor: 'Mike Ross', actorType: 'human', action: 'Merged PR #128: Database Migration', stageKey: 'deploy', timestamp: new Date(Date.now() - 1000 * 60 * 120).toISOString() },
        { id: '5', actor: 'Test Agent', actorType: 'ai', action: 'Executed Regression Suite: Passed', stageKey: 'test', timestamp: new Date(Date.now() - 1000 * 60 * 180).toISOString() },
        { id: '6', actor: 'Gemini Agent', actorType: 'ai', action: 'Extracted User Stories from PRD', stageKey: 'user-story', timestamp: new Date(Date.now() - 1000 * 60 * 240).toISOString() },
        { id: '7', actor: 'Alex Kim', actorType: 'human', action: 'Opened Incident: Production Latency', stageKey: 'incident', timestamp: new Date(Date.now() - 1000 * 60 * 360).toISOString() },
        { id: '8', actor: 'Gemini Agent', actorType: 'ai', action: 'Analyzed Root Cause for Latency', stageKey: 'incident', timestamp: new Date(Date.now() - 1000 * 60 * 400).toISOString() },
        { id: '9', actor: 'Rachel Zane', actorType: 'human', action: 'Updated Requirements: API v2', stageKey: 'requirement', timestamp: new Date(Date.now() - 1000 * 60 * 600).toISOString() },
        { id: '10', actor: 'Codex Agent', actorType: 'ai', action: 'Optimized SQL Query in OrderService', stageKey: 'code', timestamp: new Date(Date.now() - 1000 * 60 * 720).toISOString() }
      ],
      total: 1542
    },
    error: null
  },
  valueStory: {
    data: {
      headline: 'AI agents have reduced lead time by 35% this month while maintaining 100% audit compliance.',
      metrics: [
        { label: 'Efficiency Gain', value: '+42%', description: 'Increase in developer throughput vs last quarter' },
        { label: 'Quality Lift', value: '-28%', description: 'Reduction in post-release defects' },
        { label: 'Risk Reduction', value: 'High', description: 'Automated policy enforcement on all specs' }
      ]
    },
    error: null
  }
};
