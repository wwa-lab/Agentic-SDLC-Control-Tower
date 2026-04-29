<script setup lang="ts">
import type { DocumentReview } from '../types/requirement';
import FreshnessChip from './FreshnessChip.vue';

withDefaults(defineProps<{
  reviews: ReadonlyArray<DocumentReview>;
  emptyLabel?: string;
}>(), {
  emptyLabel: 'No business reviews recorded',
});

function shortSha(value: string) {
  return value.slice(0, 10);
}
</script>

<template>
  <div class="review-list">
    <div v-if="reviews.length === 0" class="empty">{{ emptyLabel }}</div>
    <div v-for="review in reviews" v-else :key="review.id" class="review-row">
      <div class="review-main">
        <span class="decision">{{ review.decision.replaceAll('_', ' ') }}</span>
        <FreshnessChip :status="review.stale ? 'DOCUMENT_CHANGED_AFTER_REVIEW' : 'FRESH'" />
      </div>
      <p v-if="review.comment">{{ review.comment }}</p>
      <div class="review-version">
        <span>Reviewed Version</span>
        <strong>Commit {{ shortSha(review.commitSha) }} · Blob {{ shortSha(review.blobSha) }}</strong>
      </div>
    </div>
  </div>
</template>

<style scoped>
.review-list { display: flex; flex-direction: column; gap: 8px; }
.review-row { padding: 10px; border: var(--border-ghost); border-radius: var(--radius-sm); background: var(--color-surface-container); }
.review-main { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.decision { font-size: 0.6875rem; font-weight: 700; color: var(--color-on-surface); }
p { margin: 6px 0; font-size: 0.75rem; color: var(--color-on-surface); }
.review-version {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  margin-top: 6px;
  color: var(--color-on-surface-variant);
  font-size: 0.625rem;
}
.review-version span {
  flex: 0 0 auto;
  font-family: var(--font-ui);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}
.review-version strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: var(--font-tech);
  font-weight: 600;
  color: var(--color-on-surface);
}
.empty { padding: 16px 0; text-align: center; color: var(--color-on-surface-variant); font-size: 0.75rem; }
</style>
