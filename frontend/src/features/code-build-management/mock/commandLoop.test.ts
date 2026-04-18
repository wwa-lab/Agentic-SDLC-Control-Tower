import { beforeEach, describe, expect, it } from 'vitest';
import {
  regenerateAiTriage,
  resetMockState,
  simulateWebhookRoundTrip,
} from './commandLoop';

const adminContext = {
  workspaceId: 'ws-default-001',
  role: 'PM' as const,
  autonomyLevel: 'SUPERVISED' as const,
};

describe('code build mock command loop', () => {
  beforeEach(() => {
    resetMockState();
  });

  it('surfaces evidence mismatches as row-level failures instead of command failure', async () => {
    const response = await regenerateAiTriage(
      'run-991',
      { reason: 'CB_TRIAGE_EVIDENCE_MISMATCH' },
      adminContext,
    );

    expect(response.errorCode).toBe('CB_TRIAGE_EVIDENCE_MISMATCH');
    expect(response.aiTriage.data?.rows.some(row => row.status === 'FAILED_EVIDENCE')).toBe(true);
  });

  it('rejects tampered webhook fixtures', async () => {
    await expect(simulateWebhookRoundTrip('__SIGNATURE_TRIGGER__')).rejects.toMatchObject({
      code: 'CB_WEBHOOK_SIGNATURE_INVALID',
    });
  });
});

