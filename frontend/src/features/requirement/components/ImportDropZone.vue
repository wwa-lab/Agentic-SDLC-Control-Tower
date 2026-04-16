<script setup lang="ts">
import { ref } from 'vue';

const emit = defineEmits<{
  fileSelected: [file: File];
}>();

const ACCEPTED = '.xlsx,.csv,.pdf,.eml,.msg,.vtt,.txt,.png,.jpg,.jpeg,.webp';
const MAX_SIZE_MB = 10;
const MAX_SIZE_BYTES = MAX_SIZE_MB * 1024 * 1024;

const isDragOver = ref(false);
const errorMsg = ref<string | null>(null);

function validateAndEmit(file: File) {
  errorMsg.value = null;
  if (file.size > MAX_SIZE_BYTES) {
    errorMsg.value = `File too large (${(file.size / 1024 / 1024).toFixed(1)} MB). Maximum is ${MAX_SIZE_MB} MB.`;
    return;
  }
  emit('fileSelected', file);
}

function handleDrop(e: DragEvent) {
  isDragOver.value = false;
  const file = e.dataTransfer?.files[0];
  if (file) validateAndEmit(file);
}

function handleFileInput(e: Event) {
  const input = e.target as HTMLInputElement;
  const file = input.files?.[0];
  if (file) validateAndEmit(file);
  input.value = '';
}
</script>

<template>
  <div class="drop-zone-wrapper">
    <div
      class="drop-zone"
      :class="{ 'drop-zone--active': isDragOver }"
      @dragover.prevent="isDragOver = true"
      @dragleave="isDragOver = false"
      @drop.prevent="handleDrop"
    >
      <span class="drop-icon">↑</span>
      <span class="drop-text">Drop a file here or click to browse</span>
      <span class="drop-formats">
        Supported: Excel, CSV, PDF, Email, Transcript, Text, Image
      </span>
      <span class="drop-limit">Max {{ MAX_SIZE_MB }} MB</span>
      <input
        type="file"
        class="file-input"
        :accept="ACCEPTED"
        @change="handleFileInput"
      />
    </div>
    <div v-if="errorMsg" class="drop-error">{{ errorMsg }}</div>
  </div>
</template>

<style scoped>
.drop-zone-wrapper {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.drop-zone {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 32px 16px;
  border: 2px dashed var(--color-outline-variant);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-low);
  cursor: pointer;
  transition: all 0.2s ease;
}

.drop-zone--active {
  border-color: var(--color-secondary);
  background: var(--color-secondary-tint);
}

.drop-zone:hover { border-color: var(--color-on-surface-variant); }

.drop-icon {
  font-size: 1.5rem;
  color: var(--color-on-surface-variant);
  opacity: 0.5;
}

.drop-text {
  font-family: var(--font-ui);
  font-size: 0.75rem;
  color: var(--color-on-surface);
}

.drop-formats, .drop-limit {
  font-family: var(--font-ui);
  font-size: 0.5625rem;
  color: var(--color-on-surface-variant);
  opacity: 0.6;
}

.file-input {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
}

.drop-error {
  font-family: var(--font-ui);
  font-size: 0.6875rem;
  color: var(--color-incident-crimson);
}
</style>
