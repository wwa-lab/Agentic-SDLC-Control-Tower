<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useFontScale } from '@/shared/composables/useFontScale';
import { useTheme } from '@/shared/composables/useTheme';
import { useSessionStore } from '@/shell/stores/sessionStore';
import { getHelpLinks } from '@/shared/api/shellApi';
import ContactUsModal from './ContactUsModal.vue';
import { Search, Bell, History, Sun, Moon, Type, LifeBuoy, BookOpen, LogOut } from 'lucide-vue-next';

const emit = defineEmits<{
  search: [];
  notifications: [];
  audit: [];
}>();

const { theme, toggleTheme } = useTheme();
const { currentFontScale, cycleFontScale } = useFontScale();
const session = useSessionStore();
const contactOpen = ref(false);
const guidelineUrl = ref<string | null>(null);

onMounted(async () => {
  try {
    guidelineUrl.value = (await getHelpLinks()).userGuidelineUrl;
  } catch {
    guidelineUrl.value = null;
  }
});

function openGuideline() {
  if (guidelineUrl.value) {
    window.open(guidelineUrl.value, '_blank', 'noopener,noreferrer');
  }
}
</script>

<template>
  <div class="global-action-bar">
    <div class="theme-toggle" :class="{ dark: theme === 'dark' }" @click="toggleTheme">
      <Sun :size="12" class="toggle-icon sun-icon" />
      <Moon :size="12" class="toggle-icon moon-icon" />
      <div class="toggle-thumb"></div>
    </div>
    <button
      class="font-scale-btn"
      :class="{ 'font-scale-btn--active': currentFontScale.id !== 'default' }"
      :title="`Font size: ${currentFontScale.label}`"
      aria-label="Cycle global font size"
      @click="cycleFontScale"
    >
      <Type :size="15" class="font-scale-icon" />
      <span class="font-scale-label">Font</span>
      <span class="font-scale-value">{{ currentFontScale.label }}</span>
    </button>
    <button class="icon-btn" title="Search" @click="emit('search')"><Search :size="18" /></button>
    <button class="icon-btn" title="Notifications" @click="emit('notifications')"><Bell :size="18" /></button>
    <button class="icon-btn" title="Audit / History" @click="emit('audit')"><History :size="18" /></button>
    <button class="icon-btn" title="Contact Us" @click="contactOpen = true"><LifeBuoy :size="18" /></button>
    <button class="icon-btn" title="User Guideline" :disabled="!guidelineUrl" @click="openGuideline"><BookOpen :size="18" /></button>
    <button class="user-chip" title="Logout" @click="session.logout">
      <img v-if="session.currentUser?.avatarUrl" :src="session.currentUser.avatarUrl" alt="" />
      <span v-else>{{ session.currentUser?.displayName?.slice(0, 1) ?? 'U' }}</span>
      <LogOut :size="14" />
    </button>
    <ContactUsModal :open="contactOpen" @close="contactOpen = false" />
  </div>
</template>

<style scoped>
.global-action-bar {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  gap: 8px;
  padding-right: 24px;
}

.icon-btn {
  background: transparent;
  border: none;
  color: var(--color-on-surface-variant);
  cursor: pointer;
  padding: 6px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.icon-btn:hover {
  background: var(--nav-hover-bg);
  color: var(--color-on-surface);
}

.icon-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.user-chip {
  height: 32px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: var(--border-subtle);
  border-radius: var(--radius-sm);
  background: var(--color-surface-container-high);
  color: var(--color-on-surface);
  cursor: pointer;
  padding: 0 8px;
}

.user-chip img,
.user-chip span {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: var(--color-secondary-container);
  color: var(--color-on-secondary-container);
  font-size: 11px;
  font-weight: 800;
}

.font-scale-btn {
  min-width: 108px;
  height: 34px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: 1px solid rgba(137, 206, 255, 0.55);
  border-radius: var(--radius-sm);
  background:
    linear-gradient(180deg, rgba(137, 206, 255, 0.16), rgba(137, 206, 255, 0.06)),
    var(--color-surface-container-high);
  color: var(--color-on-surface);
  cursor: pointer;
  font-family: var(--font-ui);
  padding: 0 10px;
  box-shadow: 0 0 0 1px rgba(137, 206, 255, 0.08), 0 0 12px rgba(137, 206, 255, 0.08);
  transition: background 0.2s, border-color 0.2s, color 0.2s, box-shadow 0.2s, transform 0.2s;
}

.font-scale-btn:hover {
  background:
    linear-gradient(180deg, rgba(137, 206, 255, 0.24), rgba(137, 206, 255, 0.1)),
    var(--nav-hover-bg);
  border-color: var(--color-secondary);
  box-shadow: 0 0 0 1px rgba(137, 206, 255, 0.18), 0 0 18px rgba(137, 206, 255, 0.18);
  transform: translateY(-1px);
}

.font-scale-btn--active {
  border-color: var(--color-secondary);
  color: var(--color-secondary);
  box-shadow: 0 0 0 1px rgba(137, 206, 255, 0.18), 0 0 16px rgba(137, 206, 255, 0.18);
}

.font-scale-icon {
  flex: 0 0 auto;
}

.font-scale-label {
  font-size: 0.75rem;
  font-weight: 700;
  line-height: 1;
}

.font-scale-value {
  font-family: var(--font-tech);
  font-size: 0.625rem;
  line-height: 1;
  opacity: 0.86;
}

.theme-toggle {
  width: 48px;
  height: 24px;
  border-radius: 12px;
  background: var(--color-surface-container-high);
  cursor: pointer;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 6px;
  transition: background 0.3s;
}

.toggle-icon {
  position: relative;
  z-index: 1;
  transition: color 0.3s;
}

.sun-icon { color: var(--color-approval-amber); }
.moon-icon { color: var(--color-on-surface-variant); }
.theme-toggle.dark .sun-icon { color: var(--color-on-surface-variant); }
.theme-toggle.dark .moon-icon { color: var(--color-secondary); }

.toggle-thumb {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--color-on-surface);
  transition: transform 0.3s;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
}

.theme-toggle.dark .toggle-thumb {
  transform: translateX(24px);
}

@media (max-width: 720px) {
  .global-action-bar {
    gap: 4px;
    padding-right: 12px;
  }

  .font-scale-btn {
    min-width: 38px;
    width: 38px;
    padding: 0;
  }

  .font-scale-label,
  .font-scale-value {
    display: none;
  }

  .icon-btn {
    padding: 5px;
  }

  .theme-toggle {
    width: 42px;
  }

  .theme-toggle.dark .toggle-thumb {
    transform: translateX(18px);
  }
}

@media (max-width: 520px) {
  .global-action-bar .icon-btn[title="Audit / History"] {
    display: none;
  }
}
</style>
