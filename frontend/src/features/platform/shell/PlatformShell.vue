<script setup lang="ts">
import SubSectionRail from './SubSectionRail.vue';
</script>

<template>
  <div class="platform-shell">
    <SubSectionRail />
    <div class="platform-main">
      <router-view />
    </div>
  </div>
</template>

<style scoped>
.platform-shell {
  display: flex;
  min-height: 0;
  flex: 1;
}
.platform-main {
  flex: 1;
  min-width: 0;
  max-width: 1440px;
  padding: 0 24px 24px;
}
</style>
