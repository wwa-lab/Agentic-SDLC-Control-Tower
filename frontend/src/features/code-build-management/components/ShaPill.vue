<script setup lang="ts">
import { ref } from 'vue';

interface Props {
  sha: string;
  href?: string | null;
}

const props = withDefaults(defineProps<Props>(), {
  href: null,
});

const copied = ref(false);

async function copySha() {
  if (!navigator.clipboard?.writeText) {
    return;
  }
  await navigator.clipboard.writeText(props.sha);
  copied.value = true;
  window.setTimeout(() => {
    copied.value = false;
  }, 1000);
}
</script>

<template>
  <span class="sha-pill">
    <button class="sha-pill__copy" type="button" @click="copySha">{{ copied ? 'Copied' : sha.slice(0, 7) }}</button>
    <a v-if="href" class="sha-pill__link" :href="href" rel="noopener noreferrer" target="_blank">↗</a>
  </span>
</template>

<style scoped>
.sha-pill {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 7px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.04);
  border: var(--border-ghost);
}

.sha-pill__copy,
.sha-pill__link {
  border: none;
  background: transparent;
  color: var(--color-on-surface);
  font-family: var(--font-tech);
  font-size: 0.72rem;
  cursor: pointer;
  text-decoration: none;
}
</style>

