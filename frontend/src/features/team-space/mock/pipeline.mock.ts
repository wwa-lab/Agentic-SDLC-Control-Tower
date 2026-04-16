import type { RequirementPipeline } from '../types/pipeline';

export function pipeline(workspaceId: string): RequirementPipeline {
  return {
    counters: {
      requirementsInflow7d: 12,
      storiesDecomposing: 18,
      specsGenerating: 3,
      specsInReview: 5,
      specsBlocked: 2,
      specsApprovedAwaitingDownstream: 7,
    },
    blockers: [
      {
        kind: 'SPEC_BLOCKED',
        targetId: 'SPEC-0088',
        targetTitle: 'Webhook retry strategy',
        ageDays: 5,
        filterDeeplink: `/requirements?filter=blocked-specs&workspaceId=${workspaceId}`,
      },
      {
        kind: 'STORY_NO_SPEC',
        targetId: 'US-044',
        targetTitle: 'Approval fallback for manual override',
        ageDays: 4,
        filterDeeplink: `/requirements?filter=blocked-specs&workspaceId=${workspaceId}`,
      },
    ],
    chain: [
      { nodeKey: 'REQUIREMENT', health: 'GREEN', isExecutionHub: false },
      { nodeKey: 'USER_STORY', health: 'GREEN', isExecutionHub: false },
      { nodeKey: 'SPEC', health: 'YELLOW', isExecutionHub: true },
      { nodeKey: 'ARCHITECTURE', health: 'GREEN', isExecutionHub: false },
      { nodeKey: 'DESIGN', health: 'GREEN', isExecutionHub: false },
      { nodeKey: 'TASKS', health: 'GREEN', isExecutionHub: false },
      { nodeKey: 'CODE', health: 'GREEN', isExecutionHub: false },
      { nodeKey: 'TEST', health: 'YELLOW', isExecutionHub: false },
      { nodeKey: 'DEPLOY', health: 'GREEN', isExecutionHub: false },
      { nodeKey: 'INCIDENT', health: 'RED', isExecutionHub: false },
      { nodeKey: 'LEARNING', health: 'GREEN', isExecutionHub: false },
    ],
    blockerThresholdDays: 3,
  };
}
