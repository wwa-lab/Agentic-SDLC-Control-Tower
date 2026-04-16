<script setup lang="ts">
import type { RecentActivity, SectionResult } from '../types/dashboard';
import DashboardCard from './DashboardCard.vue';
import ActivityEntry from './ActivityEntry.vue';

interface Props {
  section: SectionResult<RecentActivity>;
  isLoading?: boolean;
}

defineProps<Props>();
const emit = defineEmits(['view-all']);
</script>

<template>
  <DashboardCard title="Recent Activity" :is-loading="isLoading" :error="section.error" full-width>
    <div v-if="section.data" class="activity-stream-content">
      <div class="entries-list">
        <ActivityEntry 
          v-for="entry in section.data.entries" 
          :key="entry.id" 
          :entry="entry" 
        />
      </div>
      
      <div class="stream-footer">
        <button class="view-all-btn" @click="emit('view-all')">View All Activity →</button>
      </div>
    </div>
  </DashboardCard>
</template>

<style scoped>
.activity-stream-content {
  display: flex;
  flex-direction: column;
}

.entries-list {
  display: flex;
  flex-direction: column;
}

.stream-footer {
  padding: 12px 16px;
  display: flex;
  justify-content: center;
}

.view-all-btn {
  background: none;
  border: none;
  font-family: var(--font-ui);
  font-size: 0.625rem;
  color: var(--color-on-surface-variant);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  cursor: pointer;
  transition: color 0.2s ease;
}

.view-all-btn:hover {
  color: var(--color-secondary);
}
</style>
