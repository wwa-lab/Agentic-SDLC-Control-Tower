import { describe, expect, it } from 'vitest';
import { ref } from 'vue';
import { useCardState } from '../composables/useCardState';

describe('useCardState', () => {
  it('returns loading before any data settles', () => {
    const state = useCardState(ref(null), ref(true), ref(null));
    expect(state.value).toBe('loading');
  });

  it('returns error when an error is present', () => {
    const state = useCardState(ref(null), ref(false), ref('Boom'));
    expect(state.value).toBe('error');
  });

  it('returns empty when the custom empty predicate matches', () => {
    const state = useCardState(ref([] as string[]), ref(false), ref(null), value => value.length === 0);
    expect(state.value).toBe('empty');
  });

  it('returns normal when data is available', () => {
    const state = useCardState(ref(['ready']), ref(false), ref(null), value => value.length === 0);
    expect(state.value).toBe('normal');
  });
});
