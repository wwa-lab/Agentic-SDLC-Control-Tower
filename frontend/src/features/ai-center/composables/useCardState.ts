import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import type { CardState } from '../types';

export function useCardState<T>(
  data: MaybeRefOrGetter<T | null | undefined>,
  loading: MaybeRefOrGetter<boolean>,
  error: MaybeRefOrGetter<string | null | undefined>,
  isEmpty?: (value: T) => boolean,
) {
  return computed<CardState>(() => {
    if (toValue(loading)) {
      return 'loading';
    }

    const resolvedError = toValue(error);
    if (resolvedError) {
      return 'error';
    }

    const resolvedData = toValue(data);
    if (resolvedData == null) {
      return 'empty';
    }

    if (isEmpty?.(resolvedData)) {
      return 'empty';
    }

    return 'normal';
  });
}
