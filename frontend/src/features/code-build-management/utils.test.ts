import { describe, expect, it } from 'vitest';
import { filterAiReviewForRole, deriveStoryLinkStatus } from './utils';
import type { AiPrReviewPayload } from './types';

const payload: AiPrReviewPayload = {
  status: 'SUCCESS',
  summary: 'Test payload',
  headSha: 'abc1234',
  noteCounts: {
    blocker: 1,
    major: 0,
    minor: 0,
    info: 0,
  },
  notes: [
    {
      id: 'note-1',
      severity: 'BLOCKER',
      title: 'Sensitive blocker',
      body: 'Real blocker body',
      filePath: 'foo.ts',
      line: 1,
      evidence: 'test',
    },
  ],
  generatedAt: '2026-04-18T00:00:00Z',
  skillVersion: 'cb-pr-review@2.1.0',
  lineage: null,
  failureCode: null,
  stale: false,
};

describe('code-build utilities', () => {
  it('derives story link states from trailer candidates and resolver hits', () => {
    expect(deriveStoryLinkStatus([], [])).toBe('NO_STORY_ID');
    expect(deriveStoryLinkStatus(['STORY-A', 'STORY-B'], ['STORY-A'])).toBe('AMBIGUOUS');
    expect(deriveStoryLinkStatus(['STORY-A'], [])).toBe('UNKNOWN_STORY');
    expect(deriveStoryLinkStatus(['STORY-A'], ['STORY-A'])).toBe('KNOWN');
  });

  it('redacts blocker bodies for non-PM and non-Tech Lead roles', () => {
    const developerView = filterAiReviewForRole(payload, 'DEVELOPER');
    expect(developerView.notes[0].body).toContain('Visible to PM + Tech Lead');

    const pmView = filterAiReviewForRole(payload, 'PM');
    expect(pmView.notes[0].body).toBe('Real blocker body');
  });
});

