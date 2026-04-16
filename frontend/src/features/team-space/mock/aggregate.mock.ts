import type { TeamSpaceAggregate } from '../types/aggregate';
import { summary } from './summary.mock';
import { operatingModel } from './operatingModel.mock';
import { members } from './members.mock';
import { templates } from './templates.mock';
import { pipeline } from './pipeline.mock';
import { metrics } from './metrics.mock';
import { risks } from './risks.mock';
import { projects } from './projects.mock';

export function aggregate(workspaceId: string): TeamSpaceAggregate {
  return {
    workspaceId,
    summary: { data: summary(workspaceId), error: null },
    operatingModel: { data: operatingModel(workspaceId), error: null },
    members: { data: members(workspaceId), error: null },
    templates: { data: templates(workspaceId), error: null },
    pipeline: { data: pipeline(workspaceId), error: null },
    metrics: { data: metrics(workspaceId), error: null },
    risks: { data: risks(workspaceId), error: null },
    projects: { data: projects(workspaceId), error: null },
  };
}
