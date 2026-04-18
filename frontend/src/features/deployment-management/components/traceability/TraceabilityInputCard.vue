<script setup lang="ts">
import { ref, computed } from 'vue';

const props = defineProps<{
  storyIds: ReadonlyArray<string>;
  currentStoryId: string;
}>();
const emit = defineEmits<{ lookupStory: [storyId: string] }>();

const query = ref(props.currentStoryId);
const isOpen = ref(false);
const highlightIndex = ref(-1);

const filtered = computed<ReadonlyArray<string>>(() => {
  const q = query.value.trim().toLowerCase();
  if (q.length === 0) return props.storyIds;
  return props.storyIds.filter((id) => id.toLowerCase().includes(q));
});

function selectStory(storyId: string): void {
  query.value = storyId;
  isOpen.value = false;
  highlightIndex.value = -1;
  emit('lookupStory', storyId);
}

function submitQuery(): void {
  const trimmed = query.value.trim();
  if (trimmed.length === 0) return;
  isOpen.value = false;
  highlightIndex.value = -1;
  emit('lookupStory', trimmed);
}

function onKeydown(event: KeyboardEvent): void {
  if (!isOpen.value && event.key === 'ArrowDown') {
    isOpen.value = true;
    highlightIndex.value = 0;
    event.preventDefault();
    return;
  }

  if (!isOpen.value) return;

  const list = filtered.value;

  switch (event.key) {
    case 'ArrowDown':
      event.preventDefault();
      highlightIndex.value = highlightIndex.value < list.length - 1
        ? highlightIndex.value + 1
        : 0;
      break;
    case 'ArrowUp':
      event.preventDefault();
      highlightIndex.value = highlightIndex.value > 0
        ? highlightIndex.value - 1
        : list.length - 1;
      break;
    case 'Enter':
      event.preventDefault();
      if (highlightIndex.value >= 0 && highlightIndex.value < list.length) {
        selectStory(list[highlightIndex.value]);
      } else {
        submitQuery();
      }
      break;
    case 'Escape':
      isOpen.value = false;
      highlightIndex.value = -1;
      break;
  }
}

function onInput(): void {
  isOpen.value = true;
  highlightIndex.value = -1;
}

function onBlur(): void {
  // Delay to allow click on dropdown items
  setTimeout(() => { isOpen.value = false; }, 150);
}
</script>

<template>
  <div class="input-card card">
    <h3 class="card-title">Story Lookup</h3>
    <div class="input-wrapper">
      <input
        v-model="query"
        type="text"
        class="story-input"
        placeholder="Enter story ID..."
        aria-label="Story ID"
        autocomplete="off"
        @input="onInput"
        @focus="onInput"
        @blur="onBlur"
        @keydown="onKeydown"
      />
      <button class="lookup-btn" @click="submitQuery">Lookup</button>
    </div>
    <ul v-if="isOpen && filtered.length > 0" class="dropdown" role="listbox">
      <li
        v-for="(id, idx) in filtered"
        :key="id"
        class="dropdown-item"
        :class="{ highlighted: idx === highlightIndex }"
        role="option"
        :aria-selected="idx === highlightIndex"
        @mousedown.prevent="selectStory(id)"
      >
        {{ id }}
      </li>
    </ul>
  </div>
</template>

<style scoped>
.card {
  background: var(--color-surface-container-low);
  border: var(--border-ghost);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  padding: 16px;
  position: relative;
}
.card-title {
  margin: 0 0 12px;
  font-family: var(--font-ui);
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.input-wrapper {
  display: flex;
  gap: 8px;
}
.story-input {
  flex: 1;
  padding: 6px 10px;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-high);
  color: var(--color-on-surface);
  font-family: var(--font-tech);
  font-size: 0.85rem;
  outline: none;
}
.story-input::placeholder {
  color: var(--color-on-surface-variant);
  opacity: 0.6;
}
.story-input:focus {
  border-color: var(--color-secondary);
  box-shadow: 0 0 0 1px var(--color-secondary-subtle);
}
.lookup-btn {
  padding: 6px 14px;
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-high);
  color: var(--color-on-surface);
  font-family: var(--font-ui);
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}
.lookup-btn:hover {
  background: var(--color-surface-container-highest);
}
.dropdown {
  position: absolute;
  left: 16px;
  right: 16px;
  top: calc(100% - 4px);
  z-index: 10;
  margin: 0;
  padding: 4px 0;
  list-style: none;
  background: var(--color-surface-container-high);
  border: var(--border-ghost);
  border-radius: var(--radius-sm);
  box-shadow: var(--shadow-card);
  max-height: 200px;
  overflow-y: auto;
}
.dropdown-item {
  padding: 6px 10px;
  font-family: var(--font-tech);
  font-size: 0.8rem;
  color: var(--color-on-surface);
  cursor: pointer;
}
.dropdown-item:hover,
.dropdown-item.highlighted {
  background: var(--nav-hover-bg);
}
</style>
