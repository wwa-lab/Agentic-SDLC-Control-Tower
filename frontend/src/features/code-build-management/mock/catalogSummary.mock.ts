import type { CatalogRepoRow, CatalogSummary } from '../types';

export function buildCatalogSummary(rows: ReadonlyArray<CatalogRepoRow>, workspaceId: string): CatalogSummary {
  const counts = rows.reduce(
    (accumulator, row) => {
      switch (row.defaultBranchStatus) {
        case 'SUCCESS':
          accumulator.successRepos += 1;
          break;
        case 'FAILURE':
          accumulator.failureRepos += 1;
          break;
        case 'RUNNING':
          accumulator.runningRepos += 1;
          break;
        case 'QUEUED':
          accumulator.queuedRepos += 1;
          break;
        case 'CANCELLED':
          accumulator.cancelledRepos += 1;
          break;
        default:
          accumulator.neutralRepos += 1;
          break;
      }
      if (!row.hasRuns) {
        accumulator.reposWithoutRuns += 1;
      }
      return accumulator;
    },
    {
      successRepos: 0,
      failureRepos: 0,
      runningRepos: 0,
      queuedRepos: 0,
      cancelledRepos: 0,
      neutralRepos: 0,
      reposWithoutRuns: 0,
    },
  );

  return {
    workspaceId,
    totalRepos: rows.length,
    ...counts,
    advisory:
      counts.failureRepos > 0
        ? `${counts.failureRepos} repos need intervention before the next release train.`
        : 'Default branch health is steady across the visible workspace.',
    lastGeneratedAt: '2026-04-18T04:30:00Z',
  };
}

