import type { PipelineProfile } from '../types/requirement';

export const STANDARD_SDD_PROFILE: PipelineProfile = {
  id: 'standard-sdd',
  name: 'Standard SDD',
  description: 'Spec-Driven Development pipeline with 11-node chain, 2 skills, per-layer traceability',
  chainNodes: [
    { id: 'req', label: 'Requirement', artifactType: 'requirement', isExecutionHub: false },
    { id: 'story', label: 'User Story', artifactType: 'user-story', isExecutionHub: false },
    { id: 'spec', label: 'Spec', artifactType: 'spec', isExecutionHub: true },
    { id: 'arch', label: 'Architecture', artifactType: 'architecture', isExecutionHub: false },
    { id: 'data-flow', label: 'Data Flow', artifactType: 'design', isExecutionHub: false },
    { id: 'data-model', label: 'Data Model', artifactType: 'design', isExecutionHub: false },
    { id: 'design', label: 'Design', artifactType: 'design', isExecutionHub: false },
    { id: 'api', label: 'API Guide', artifactType: 'design', isExecutionHub: false },
    { id: 'tasks', label: 'Tasks', artifactType: 'tasks', isExecutionHub: false },
    { id: 'code', label: 'Code', artifactType: 'code', isExecutionHub: false },
    { id: 'test', label: 'Test', artifactType: 'test', isExecutionHub: false },
  ],
  skills: [
    { skillId: 'req-to-user-story', label: 'Generate Stories', triggerPoint: 'requirement' },
    { skillId: 'user-story-to-spec', label: 'Generate Spec', triggerPoint: 'user-story' },
  ],
  entryPaths: [
    { id: 'standard', label: 'Standard', description: 'Single entry path through requirement capture' },
  ],
  specTiering: null,
  usesOrchestrator: false,
  traceabilityMode: 'per-layer',
};
