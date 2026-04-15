import { ref, watchEffect } from 'vue';

type Theme = 'dark' | 'light';

const STORAGE_KEY = 'sdlc-tower-theme';

const theme = ref<Theme>(loadTheme());

function loadTheme(): Theme {
  const stored = localStorage.getItem(STORAGE_KEY);
  if (stored === 'light' || stored === 'dark') {
    return stored;
  }
  return 'dark';
}

function applyTheme(value: Theme): void {
  document.documentElement.setAttribute('data-theme', value);
  localStorage.setItem(STORAGE_KEY, value);
}

watchEffect(() => {
  applyTheme(theme.value);
});

export function useTheme() {
  const toggleTheme = (): void => {
    theme.value = theme.value === 'dark' ? 'light' : 'dark';
  };

  return {
    theme,
    toggleTheme,
  } as const;
}
