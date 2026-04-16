<script setup lang="ts">
import { ref, watch } from 'vue';
import type { RequirementDraft, RequirementPriority, RequirementCategory } from '../types/requirement';
import ImportInspectionCard from './ImportInspectionCard.vue';
import MissingInfoBanner from './MissingInfoBanner.vue';

interface Props {
  draft: RequirementDraft;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  confirm: [draft: RequirementDraft];
  discard: [];
}>();

// Editable local copy
const title = ref('');
const priority = ref<RequirementPriority>('Medium');
const category = ref<RequirementCategory>('Functional');
const summary = ref('');
const businessJustification = ref('');
const acceptanceCriteria = ref<string[]>([]);
const showQuestions = ref(false);

watch(() => props.draft, (d) => {
  title.value = d.title;
  priority.value = d.priority;
  category.value = d.category;
  summary.value = d.summary;
  businessJustification.value = d.businessJustification;
  acceptanceCriteria.value = [...d.acceptanceCriteria];
}, { immediate: true });

function handleConfirm() {
  emit('confirm', {
    ...props.draft,
    title: title.value,
    priority: priority.value,
    category: category.value,
    summary: summary.value,
    businessJustification: businessJustification.value,
    acceptanceCriteria: acceptanceCriteria.value,
  });
}
</script>

<template>
  <div class="normalization-result">
    <h3 class="result-title">AI Draft Review</h3>

    <div class="field-group">
      <label class="field-label">
        Title
        <span v-if="draft.aiSuggestedFields.includes('title')" class="ai-badge">AI</span>
      </label>
      <input v-model="title" class="field-input" type="text" />
    </div>

    <div class="field-row">
      <div class="field-group field-sm">
        <label class="field-label">
          Priority
          <span v-if="draft.aiSuggestedFields.includes('priority')" class="ai-badge">AI</span>
        </label>
        <select v-model="priority" class="field-select">
          <option value="Critical">Critical</option>
          <option value="High">High</option>
          <option value="Medium">Medium</option>
          <option value="Low">Low</option>
        </select>
      </div>
      <div class="field-group field-sm">
        <label class="field-label">
          Category
          <span v-if="draft.aiSuggestedFields.includes('category')" class="ai-badge">AI</span>
        </label>
        <select v-model="category" class="field-select">
          <option value="Functional">Functional</option>
          <option value="Non-Functional">Non-Functional</option>
          <option value="Technical">Technical</option>
          <option value="Business">Business</option>
        </select>
      </div>
    </div>

    <div class="field-group">
      <label class="field-label">
        Summary
        <span v-if="draft.aiSuggestedFields.includes('summary')" class="ai-badge">AI</span>
      </label>
      <textarea v-model="summary" class="field-textarea" rows="3"></textarea>
    </div>

    <div class="field-group">
      <label class="field-label">Business Justification</label>
      <textarea v-model="businessJustification" class="field-textarea" rows="2"></textarea>
    </div>

    <div class="field-group">
      <label class="field-label">Acceptance Criteria</label>
      <div class="criteria-editor">
        <div v-for="(ac, i) in acceptanceCriteria" :key="i" class="criterion-row">
          <input v-model="acceptanceCriteria[i]" class="field-input" type="text" />
        </div>
      </div>
    </div>

    <MissingInfoBanner :items="draft.missingInfo" />

    <ImportInspectionCard
      v-if="draft.importInspection"
      :inspection="draft.importInspection"
    />

    <!-- Open Questions -->
    <div v-if="draft.openQuestions.length > 0">
      <button class="toggle-questions" @click="showQuestions = !showQuestions">
        {{ showQuestions ? 'Hide' : 'Show' }} Open Questions ({{ draft.openQuestions.length }})
      </button>
      <ul v-if="showQuestions" class="questions-list">
        <li v-for="(q, i) in draft.openQuestions" :key="i">{{ q }}</li>
      </ul>
    </div>

    <!-- Actions -->
    <div class="actions">
      <button class="btn-discard" @click="emit('discard')">Discard</button>
      <button class="btn-confirm" @click="handleConfirm">Confirm &amp; Create</button>
    </div>
  </div>
</template>

<style scoped>
.normalization-result {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.result-title {
  font-family: var(--font-ui);
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-on-surface);
  margin: 0;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.field-row {
  display: flex;
  gap: 12px;
}

.field-sm { flex: 1; }

.field-label {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-on-surface-variant);
  display: flex;
  align-items: center;
  gap: 6px;
}

.ai-badge {
  font-size: 0.5rem;
  padding: 1px 4px;
  border-radius: 2px;
  background: var(--color-secondary-tint);
  color: var(--color-secondary);
  font-weight: 600;
}

.field-input, .field-select, .field-textarea {
  background: var(--color-surface-container-low);
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  padding: 6px 10px;
  font-family: var(--font-ui);
  font-size: 0.75rem;
  color: var(--color-on-surface);
}

.field-textarea {
  resize: vertical;
  line-height: 1.4;
}

.field-input:focus, .field-select:focus, .field-textarea:focus {
  outline: none;
  border-color: var(--color-secondary);
}

.criteria-editor {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.criterion-row {
  display: flex;
  gap: 4px;
}

.toggle-questions {
  background: none;
  border: none;
  color: var(--color-secondary);
  font-family: var(--font-ui);
  font-size: 0.625rem;
  cursor: pointer;
  padding: 0;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.questions-list {
  padding-left: 14px;
  margin: 6px 0 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.questions-list li {
  font-family: var(--font-ui);
  font-size: 0.6875rem;
  color: var(--color-on-surface);
  line-height: 1.3;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 8px;
  border-top: 1px solid var(--border-separator);
}

.btn-discard {
  background: none;
  border: var(--border-ghost);
  color: var(--color-on-surface-variant);
  padding: 8px 16px;
  border-radius: var(--radius-sm);
  font-family: var(--font-ui);
  font-size: 0.625rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  cursor: pointer;
}

.btn-discard:hover { color: var(--color-incident-crimson); border-color: var(--color-incident-crimson); }

.btn-confirm {
  background: linear-gradient(135deg, var(--color-secondary), #a78bfa);
  border: none;
  color: #0b1326;
  padding: 8px 20px;
  border-radius: var(--radius-sm);
  font-family: var(--font-ui);
  font-size: 0.625rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  cursor: pointer;
  transition: opacity 0.2s ease;
}

.btn-confirm:hover { opacity: 0.85; }
</style>
