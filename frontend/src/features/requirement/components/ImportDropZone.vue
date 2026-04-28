<script setup lang="ts">
import { ref } from 'vue';

const emit = defineEmits<{
  filesSelected: [files: File[]];
}>();

const ACCEPTED = '.txt,.md,.pdf,.html,.htm,.xlsx,.xls,.docx,.csv,.zip';
const MAX_TOTAL_SIZE_MB = 100;

const isDragOver = ref(false);
const errorMsg = ref<string | null>(null);

function formatSelectedFileNames(files: File[]) {
  if (files.length === 0) return '';
  if (files.length === 1) return files[0].name;
  if (files.length === 2) return `${files[0].name}, ${files[1].name}`;
  return `${files[0].name} + ${files.length - 1} more`;
}

function validateAndEmit(fileList: FileList | File[]) {
  errorMsg.value = null;
  const files = Array.from(fileList);
  if (files.length === 0) {
    errorMsg.value = 'Select at least one file to continue.';
    return;
  }

  const totalBytes = files.reduce((sum, file) => sum + file.size, 0);
  const maxSizeBytes = MAX_TOTAL_SIZE_MB * 1024 * 1024;
  if (totalBytes > maxSizeBytes) {
    errorMsg.value = `Upload too large (${(totalBytes / 1024 / 1024).toFixed(1)} MB across ${files.length} files: ${formatSelectedFileNames(files)}). Maximum per upload is ${MAX_TOTAL_SIZE_MB} MB.`;
    return;
  }

  emit('filesSelected', files);
}

function handleDrop(e: DragEvent) {
  isDragOver.value = false;
  const files = e.dataTransfer?.files;
  if (files && files.length > 0) validateAndEmit(files);
}

function handleFileInput(e: Event) {
  const input = e.target as HTMLInputElement;
  if (input.files && input.files.length > 0) validateAndEmit(input.files);
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
      <span class="drop-text">Drop files here or click to browse</span>
      <span class="drop-formats">
        KB-compatible formats: TXT, MD, PDF, HTML, HTM, XLSX, XLS, DOCX, CSV, ZIP
      </span>
      <span class="drop-formats">
        ZIP packages are expanded during intake; inner files that still need manual review are called out in the intake report
      </span>
      <span class="drop-limit">Upload one or many files, total up to {{ MAX_TOTAL_SIZE_MB }} MB per request</span>
      <input
        type="file"
        class="file-input"
        :accept="ACCEPTED"
        multiple
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
