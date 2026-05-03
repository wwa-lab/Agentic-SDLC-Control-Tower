<script setup lang="ts">
import { computed, ref } from 'vue';
import { X } from 'lucide-vue-next';
import { useRoute } from 'vue-router';
import { useWorkspaceStore } from '@/shared/stores/workspaceStore';
import { useSessionStore } from '@/shell/stores/sessionStore';
import { submitSupportRequest } from '@/shared/api/shellApi';
import type { SupportRequestResult } from '@/shared/types/shell';

const props = defineProps<{ open: boolean }>();
const emit = defineEmits<{ close: [] }>();

const route = useRoute();
const workspaceStore = useWorkspaceStore();
const session = useSessionStore();

const title = ref('');
const category = ref<'access' | 'data' | 'bug' | 'question' | 'enhancement'>('question');
const description = ref('');
const submitting = ref(false);
const error = ref<string | null>(null);
const result = ref<SupportRequestResult | null>(null);

const canSubmit = computed(() =>
  title.value.trim().length > 0 && description.value.trim().length > 0 && !submitting.value
);

async function submit() {
  if (!canSubmit.value || !session.currentUser) return;
  submitting.value = true;
  error.value = null;
  result.value = null;
  try {
    result.value = await submitSupportRequest({
      title: title.value.trim(),
      category: category.value,
      description: description.value.trim(),
      route: route.fullPath,
      context: workspaceStore.context,
      reporterStaffId: session.currentUser.staffId,
      reporterMode: session.currentUser.mode,
    });
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Unable to submit support request';
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <Teleport to="body">
    <div v-if="props.open" class="modal-backdrop" @click.self="emit('close')">
      <section class="contact-modal" role="dialog" aria-modal="true" aria-label="Contact Us">
        <header>
          <h2>Contact Us</h2>
          <button class="icon-button" type="button" aria-label="Close Contact Us" @click="emit('close')">
            <X :size="18" />
          </button>
        </header>

        <form v-if="!result" class="modal-form" @submit.prevent="submit">
          <label>
            <span>Title</span>
            <input v-model="title" />
          </label>
          <label>
            <span>Category</span>
            <select v-model="category">
              <option value="question">Question</option>
              <option value="access">Access</option>
              <option value="data">Data</option>
              <option value="bug">Bug</option>
              <option value="enhancement">Enhancement</option>
            </select>
          </label>
          <label>
            <span>Description</span>
            <textarea v-model="description" rows="5"></textarea>
          </label>
          <p v-if="error" class="modal-error">{{ error }}</p>
          <button class="submit-button" type="submit" :disabled="!canSubmit">
            {{ submitting ? 'Submitting...' : 'Submit' }}
          </button>
        </form>

        <div v-else class="result-panel">
          <p class="result-title">{{ result.status === 'created' ? 'Jira story created' : 'Request queued' }}</p>
          <p class="result-meta">{{ result.jiraKey ?? result.requestId }}</p>
          <a v-if="result.jiraUrl" :href="result.jiraUrl" target="_blank" rel="noopener noreferrer">Open Jira</a>
          <button class="submit-button" type="button" @click="emit('close')">Done</button>
        </div>
      </section>
    </div>
  </Teleport>
</template>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 80;
  display: grid;
  place-items: center;
  background: rgba(0, 0, 0, 0.56);
}

.contact-modal {
  width: min(480px, calc(100vw - 40px));
  border: var(--border-subtle);
  border-radius: var(--radius-md);
  background: var(--color-surface-container);
  box-shadow: var(--shadow-elevated);
}

header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px;
  border-bottom: var(--border-ghost);
}

h2 {
  margin: 0;
  font-size: 17px;
}

.modal-form,
.result-panel {
  display: grid;
  gap: 14px;
  padding: 20px;
}

label {
  display: grid;
  gap: 6px;
  font-size: 12px;
  font-weight: 700;
  color: var(--color-on-surface-variant);
}

input,
select,
textarea {
  border-radius: var(--radius-sm);
  border: var(--border-subtle);
  background: var(--color-surface-container-low);
  color: var(--color-on-surface);
  padding: 9px 10px;
  font: inherit;
}

.icon-button {
  border: none;
  background: transparent;
  color: var(--color-on-surface-variant);
  cursor: pointer;
}

.submit-button {
  height: 38px;
  border: none;
  border-radius: var(--radius-sm);
  background: var(--color-primary);
  color: var(--color-on-primary);
  font-weight: 700;
  cursor: pointer;
}

.submit-button:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.modal-error {
  margin: 0;
  color: var(--color-critical);
}

.result-title {
  margin: 0;
  font-weight: 800;
}

.result-meta {
  margin: 0;
  font-family: var(--font-mono, monospace);
}
</style>
