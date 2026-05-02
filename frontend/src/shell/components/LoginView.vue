<script setup lang="ts">
import { computed, ref } from 'vue';
import { LogIn, UserRound, ShieldCheck } from 'lucide-vue-next';
import { useSessionStore } from '@/shell/stores/sessionStore';

const session = useSessionStore();
const staffId = ref('43910516');
const password = ref('');
const inlineError = ref<string | null>(null);

const teamBook = computed(() => session.providers.find(provider => provider.provider === 'teambook' && provider.enabled));
const canSubmit = computed(() => staffId.value.trim().length > 0 && password.value.trim().length > 0);

async function submit() {
  inlineError.value = null;
  if (!canSubmit.value) {
    inlineError.value = 'Staff ID and password are required.';
    return;
  }
  try {
    await session.login(staffId.value.trim(), password.value);
  } catch {
    inlineError.value = session.error ?? 'Login failed.';
  }
}
</script>

<template>
  <main class="login-view">
    <section class="login-panel">
      <div class="login-brand">
        <ShieldCheck :size="28" />
        <div>
          <h1>SDLC Tower</h1>
          <p>Sign in to continue</p>
        </div>
      </div>

      <button v-if="teamBook" class="sso-button" type="button" @click="session.startTeamBook(teamBook)">
        <UserRound :size="18" />
        <span>Continue with TeamBook</span>
      </button>

      <form class="login-form" @submit.prevent="submit">
        <label>
          <span>Staff ID</span>
          <input v-model="staffId" autocomplete="username" inputmode="numeric" />
        </label>
        <label>
          <span>Password</span>
          <input v-model="password" autocomplete="current-password" type="password" />
        </label>
        <p v-if="inlineError" class="login-error">{{ inlineError }}</p>
        <button class="primary-button" type="submit" :disabled="session.loading">
          <LogIn :size="17" />
          <span>Sign In</span>
        </button>
      </form>

      <button class="guest-button" type="button" :disabled="session.loading" @click="session.guest">
        Continue as Guest
      </button>
    </section>
  </main>
</template>

<style scoped>
.login-view {
  width: 100vw;
  height: 100vh;
  display: grid;
  place-items: center;
  background:
    linear-gradient(180deg, rgba(25, 34, 44, 0.92), rgba(10, 14, 18, 0.96)),
    var(--color-surface);
}

.login-panel {
  width: min(420px, calc(100vw - 40px));
  display: grid;
  gap: 18px;
  padding: 28px;
  border: var(--border-subtle);
  border-radius: var(--radius-md);
  background: var(--color-surface-container);
  box-shadow: var(--shadow-elevated);
}

.login-brand {
  display: flex;
  gap: 12px;
  align-items: center;
  color: var(--color-secondary);
}

.login-brand h1 {
  margin: 0;
  font-size: 22px;
  letter-spacing: 0;
  color: var(--color-on-surface);
}

.login-brand p {
  margin: 3px 0 0;
  color: var(--color-on-surface-variant);
  font-size: 13px;
}

.login-form {
  display: grid;
  gap: 12px;
}

label {
  display: grid;
  gap: 6px;
  color: var(--color-on-surface-variant);
  font-size: 12px;
  font-weight: 700;
}

input {
  height: 38px;
  border-radius: var(--radius-sm);
  border: var(--border-subtle);
  background: var(--color-surface-container-low);
  color: var(--color-on-surface);
  padding: 0 10px;
  font: inherit;
}

.primary-button,
.sso-button,
.guest-button {
  height: 38px;
  border-radius: var(--radius-sm);
  border: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-weight: 700;
  cursor: pointer;
}

.primary-button {
  background: var(--color-primary);
  color: var(--color-on-primary);
}

.sso-button {
  background: var(--color-secondary-container);
  color: var(--color-on-secondary-container);
}

.guest-button {
  background: transparent;
  border: var(--border-subtle);
  color: var(--color-on-surface);
}

.login-error {
  margin: 0;
  color: var(--color-critical);
  font-size: 12px;
}
</style>
