<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  status: string;
}>();

const variant = computed(() => {
  switch (props.status) {
    case 'published':
    case 'active':
    case 'enabled':
      return 'ok';
    case 'draft':
      return 'warning';
    case 'deprecated':
    case 'inactive':
    case 'disabled':
      return 'neutral';
    case 'error':
      return 'critical';
    default:
      return 'neutral';
  }
});
</script>

<template>
  <span class="status-badge" :class="variant" role="status">
    {{ status }}
  </span>
</template>

<style scoped>
.status-badge {
  display: inline-block;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.02em;
  border-radius: 3px;
  text-transform: capitalize;
}
.ok { background: rgba(76,175,80,0.12); color: #4caf50; border: 1px solid #4caf50; }
.warning { background: rgba(255,193,7,0.12); color: #ffc107; border: 1px dashed #ffc107; }
.neutral { background: rgba(158,158,158,0.12); color: #9e9e9e; border: 1px solid #9e9e9e; }
.critical { background: rgba(244,67,54,0.12); color: #f44336; border: 1px solid #f44336; }
</style>
