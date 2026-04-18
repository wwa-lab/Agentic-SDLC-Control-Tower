<script setup lang="ts">
</script>

<template>
  <div class="forbidden" role="alert">
    <h2 class="text-label">ACCESS DENIED</h2>
    <p class="text-body-sm">You do not have the PLATFORM_ADMIN role required to access this section.</p>
    <router-link to="/" class="btn-machined">Return to Dashboard</router-link>
  </div>
</template>

<style scoped>
.forbidden {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 64px 24px;
  text-align: center;
}
</style>
