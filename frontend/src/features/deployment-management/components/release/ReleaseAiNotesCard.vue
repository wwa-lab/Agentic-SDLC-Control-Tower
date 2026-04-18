<script setup lang="ts">
import type { SectionResult } from '@/shared/types/section';
import type { AiReleaseNotes } from '../../types/release';
import AiRowStatusBanner from '../primitives/AiRowStatusBanner.vue';
import ReleaseNotesMarkdown from '../primitives/ReleaseNotesMarkdown.vue';

defineProps<{
  section: SectionResult<AiReleaseNotes>;
  canRegenerate: boolean;
}>();
const emit = defineEmits<{ regenerate: [] }>();
</script>

<template>
  <div class="ai-notes-card card">
    <div class="card-title">
      <span>AI Release Notes</span>
      <button
        v-if="canRegenerate"
        class="regen-btn"
        @click="emit('regenerate')"
      >Regenerate</button>
    </div>

    <div v-if="section.error" class="card-error">{{ section.error }}</div>
    <div v-else-if="!section.data" class="card-skeleton">Loading AI release notes...</div>
    <template v-else>
      <AiRowStatusBanner
        :status="section.data.status"
        :error-message="section.data.error?.message"
      />

      <div
        v-if="section.data.status === 'EVIDENCE_MISMATCH'"
        class="evidence-lock-banner"
      >
        Evidence integrity check failed. Content is locked and may be unreliable.
      </div>

      <div
        v-if="section.data.narrative"
        class="narrative-section"
        :class="{ locked: section.data.status === 'EVIDENCE_MISMATCH' }"
      >
        <ReleaseNotesMarkdown
          :markdown="section.data.narrative"
          :evidence="section.data.evidence"
        />
      </div>

      <p v-if="section.data.generatedAt" class="timestamp">
        Generated {{ new Date(section.data.generatedAt).toLocaleString() }}
        <template v-if="section.data.skillVersion">
          &middot; {{ section.data.skillVersion }}
        </template>
      </p>
    </template>
  </div>
</template>

<style scoped>
.card {
  background: var(--color-surface-container-low);
  border: var(--border-ghost);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  padding: 16px;
}
.card-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-family: var(--font-ui);
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--color-on-surface);
  text-transform: uppercase;
  letter-spacing: 0.03em;
}
.regen-btn {
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-secondary);
  background: transparent;
  color: var(--color-secondary);
  font-size: 0.7rem;
  font-weight: 600;
  cursor: pointer;
  text-transform: uppercase;
}
.regen-btn:hover { background: var(--color-secondary-tint); }

.card-error { color: var(--color-incident-crimson); font-size: 0.85rem; }
.card-skeleton { color: var(--color-on-surface-variant); font-size: 0.85rem; }

.evidence-lock-banner {
  padding: 8px 12px;
  margin-bottom: 12px;
  border-radius: var(--radius-sm);
  background: var(--color-incident-tint);
  color: var(--color-incident-crimson);
  font-family: var(--font-ui);
  font-size: 0.8rem;
  font-weight: 600;
}

.narrative-section { margin-top: 8px; }
.narrative-section.locked {
  opacity: 0.5;
  pointer-events: none;
  user-select: none;
}

.timestamp {
  margin-top: 12px;
  font-family: var(--font-ui);
  font-size: 0.7rem;
  color: var(--color-on-surface-variant);
}
</style>
