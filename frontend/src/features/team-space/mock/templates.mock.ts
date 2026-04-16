import type { TeamDefaultTemplates } from '../types/templates';

export function templates(workspaceId: string): TeamDefaultTemplates {
  return {
    groups: {
      PAGE: [
        {
          id: 'tpl-page-team-space',
          name: 'Team Space Layout',
          version: '1.0.0',
          kind: 'PAGE',
          lineage: { origin: 'PLATFORM', overridden: false, chain: [] },
        },
      ],
      POLICY: [
        {
          id: 'tpl-policy-approval',
          name: 'Default Approval Policy',
          version: '2.1.0',
          kind: 'POLICY',
          lineage: { origin: 'APPLICATION', overridden: false, chain: [] },
        },
      ],
      WORKFLOW: [],
      SKILL_PACK: [
        {
          id: 'tpl-skill-standard-sdd',
          name: 'Standard SDD Skill Pack',
          version: '1.2.0',
          kind: 'SKILL_PACK',
          lineage: { origin: 'PLATFORM', overridden: false, chain: [] },
        },
      ],
      AI_DEFAULT: [
        {
          id: 'tpl-ai-default',
          name: 'AI Default Config',
          version: '0.3.0',
          kind: 'AI_DEFAULT',
          lineage: { origin: 'WORKSPACE', overridden: true, chain: [] },
          detailLink: `/platform?view=config&workspaceId=${workspaceId}&section=ai`,
        },
      ],
    },
    exceptionOverrides: [
      {
        templateId: 'tpl-policy-approval',
        templateName: 'Default Approval Policy',
        overrideScope: 'PROJECT',
        overrideScopeId: 'proj-42',
        overrideScopeName: 'Gateway Migration',
        overriddenAt: '2026-03-12T00:00:00Z',
        overriddenBy: 'u-007',
      },
    ],
  };
}
