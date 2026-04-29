<script setup lang="ts">
import { ref, computed } from 'vue';

const emit = defineEmits<{ submit: [text: string] }>();

const text = ref('');
const charCount = computed(() => text.value.length);
const MAX_CHARS = 10000;

function handleSubmit() {
  if (text.value.trim()) {
    emit('submit', text.value.trim());
  }
}
</script>

<template>
  <div class="text-input">
    <textarea
      v-model="text"
      class="input-area"
      :maxlength="MAX_CHARS"
      placeholder="Paste requirement text, user story, meeting notes, or any source material..."
      rows="8"
    ></textarea>
    <div class="input-footer">
      <span class="char-count" :class="{ 'char-warn': charCount > MAX_CHARS * 0.9 }">
        {{ charCount.toLocaleString() }} / {{ MAX_CHARS.toLocaleString() }}
      </span>
      <button
        class="submit-btn"
        :disabled="!text.trim()"
        @click="handleSubmit"
      >
        Create Draft
      </button>
    </div>
  </div>
</template>

<style scoped>
.text-input {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-area {
  background: var(--color-surface-container-low);
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  padding: 12px;
  font-family: var(--font-ui);
  font-size: 0.75rem;
  line-height: 1.5;
  color: var(--color-on-surface);
  resize: vertical;
  min-height: 160px;
}

.input-area::placeholder {
  color: var(--color-on-surface-variant);
  opacity: 0.5;
}

.input-area:focus {
  outline: none;
  border-color: var(--color-secondary);
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.char-count {
  font-family: var(--font-tech);
  font-size: 0.5625rem;
  color: var(--color-on-surface-variant);
}

.char-warn { color: var(--color-approval-amber); }

.submit-btn {
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

.submit-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.submit-btn:hover:not(:disabled) { opacity: 0.85; }
</style>
