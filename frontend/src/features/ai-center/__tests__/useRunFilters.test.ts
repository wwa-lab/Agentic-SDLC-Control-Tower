import { describe, expect, it, vi } from 'vitest';
import { useRunFilters } from '../composables/useRunFilters';

describe('useRunFilters', () => {
  it('emits a server-shaped query when applying filters', () => {
    const emitChange = vi.fn();
    const { form, applyFilters } = useRunFilters({}, emitChange);

    form.status.push('failed');
    form.skillKey.push('incident-diagnosis');
    form.triggeredByType = 'ai';
    applyFilters();

    expect(emitChange).toHaveBeenCalledWith({
      skillKey: ['incident-diagnosis'],
      status: ['failed'],
      triggerSourcePage: undefined,
      startedAfter: undefined,
      startedBefore: undefined,
      triggeredByType: 'ai',
    });
  });

  it('resets back to an empty query', () => {
    const emitChange = vi.fn();
    const { form, resetFilters } = useRunFilters({
      skillKey: ['incident-diagnosis'],
      status: ['failed'],
    }, emitChange);

    form.triggerSourcePage = '/incidents/INC-001';
    resetFilters();

    expect(emitChange).toHaveBeenCalledWith({});
    expect(form.skillKey).toEqual([]);
    expect(form.status).toEqual([]);
  });
});
