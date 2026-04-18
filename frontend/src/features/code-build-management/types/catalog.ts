import type { BuildHealthPoint } from './repo';
import type {
  BuildSort,
  BuildStatusFilter,
  RepoVisibility,
  RunStatus,
  VisibilityFilter,
} from './enums';

export interface CatalogSummary {
  readonly workspaceId: string;
  readonly totalRepos: number;
  readonly successRepos: number;
  readonly failureRepos: number;
  readonly runningRepos: number;
  readonly queuedRepos: number;
  readonly cancelledRepos: number;
  readonly neutralRepos: number;
  readonly reposWithoutRuns: number;
  readonly advisory: string;
  readonly lastGeneratedAt: string;
}

export interface CatalogRepoRow {
  readonly repoId: string;
  readonly owner: string;
  readonly name: string;
  readonly projectId: string;
  readonly projectName: string;
  readonly workspaceId: string;
  readonly visibility: RepoVisibility;
  readonly defaultBranch: string;
  readonly defaultBranchStatus: RunStatus;
  readonly sparkline: ReadonlyArray<BuildHealthPoint>;
  readonly lastActivityAt: string | null;
  readonly lastSyncedAt: string;
  readonly hasRuns: boolean;
  readonly githubUrl: string;
}

export interface CatalogSection {
  readonly projectId: string;
  readonly projectName: string;
  readonly workspaceId: string;
  readonly repos: ReadonlyArray<CatalogRepoRow>;
}

export interface CatalogFilter {
  readonly search: string;
  readonly buildStatus: BuildStatusFilter;
  readonly visibility: VisibilityFilter;
  readonly projectId: string | 'ALL';
  readonly sort: BuildSort;
}

export interface CatalogFilterOptions {
  readonly projects: ReadonlyArray<{ id: string; name: string }>;
  readonly statuses: ReadonlyArray<BuildStatusFilter>;
  readonly visibilities: ReadonlyArray<VisibilityFilter>;
  readonly sorts: ReadonlyArray<BuildSort>;
  readonly active: CatalogFilter;
}

