<script setup lang="ts">
import { useRoute } from 'vue-router';

const route = useRoute();
</script>

<template>
  <header class="page-header">
    <div class="title-section">
      <h1 class="page-title">{{ route.meta.title }}</h1>
      <p class="page-subtitle text-label">{{ route.name?.toString().toUpperCase() }} OPERATIONAL VIEW</p>
    </div>

    <div class="action-section">
      <button class="btn-machined">
        EXPORT DATA
      </button>
      <button class="btn-machined btn-ai">
        AI SYNC
      </button>
    </div>
  </header>
</template>

<style scoped>
.page-header {
  padding: 24px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.page-title {
  font-size: 1.75rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.page-subtitle {
  margin-top: 4px;
}

.action-section {
  display: flex;
  gap: 12px;
}
</style>
