import type { PipelineProfile } from '../types/requirement';

export const IBM_I_PROFILE: PipelineProfile = {
  id: 'ibm-i',
  name: 'IBM i',
  description: 'IBM i pipeline with single orchestrator skill, L1/L2/L3 tiering, shared-br traceability',
  chainNodes: [
    { id: 'req', label: 'Requirement', artifactType: 'requirement', isExecutionHub: false },
    { id: 'story', label: 'User Story', artifactType: 'user-story', isExecutionHub: false },
    { id: 'spec', label: 'Spec', artifactType: 'spec', isExecutionHub: true },
    { id: 'arch', label: 'Architecture', artifactType: 'architecture', isExecutionHub: false },
    { id: 'design', label: 'Design', artifactType: 'design', isExecutionHub: false },
    { id: 'tasks', label: 'Tasks', artifactType: 'tasks', isExecutionHub: false },
    { id: 'code', label: 'Code', artifactType: 'code', isExecutionHub: false },
    { id: 'build', label: 'Build', artifactType: 'code', isExecutionHub: false },
    { id: 'test', label: 'Test', artifactType: 'test', isExecutionHub: false },
    { id: 'deploy', label: 'Deploy', artifactType: 'deploy', isExecutionHub: false },
  ],
  skills: [
    { skillId: 'ibm-i-workflow-orchestrator', label: 'Send to Orchestrator', triggerPoint: 'requirement' },
  ],
  entryPaths: [
    { id: 'new-program', label: 'New Program', description: 'Create new RPG/COBOL program' },
    { id: 'modification', label: 'Modification', description: 'Modify existing program' },
    { id: 'conversion', label: 'Conversion', description: 'Convert from legacy format' },
  ],
  specTiering: {
    tiers: ['L1', 'L2', 'L3'],
    defaultTier: 'L2',
  },
  usesOrchestrator: true,
  traceabilityMode: 'shared-br',
};
