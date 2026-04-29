import { computed, ref, watchEffect } from 'vue';

const STORAGE_KEY = 'sdlc-tower-font-scale';

const FONT_SCALES = [
  { id: 'compact', label: '90%', value: 90 },
  { id: 'default', label: '100%', value: 100 },
  { id: 'large', label: '112%', value: 112.5 },
  { id: 'larger', label: '125%', value: 125 },
] as const;

type FontScaleId = typeof FONT_SCALES[number]['id'];

const fontScale = ref<FontScaleId>(loadFontScale());

function loadFontScale(): FontScaleId {
  const stored = localStorage.getItem(STORAGE_KEY);
  return FONT_SCALES.some(option => option.id === stored) ? stored as FontScaleId : 'default';
}

function applyFontScale(value: FontScaleId): void {
  const option = FONT_SCALES.find(candidate => candidate.id === value) ?? FONT_SCALES[1];
  document.documentElement.style.fontSize = `${option.value}%`;
  document.documentElement.setAttribute('data-font-scale', option.id);
  localStorage.setItem(STORAGE_KEY, option.id);
}

watchEffect(() => {
  applyFontScale(fontScale.value);
});

export function useFontScale() {
  const currentFontScale = computed(() =>
    FONT_SCALES.find(option => option.id === fontScale.value) ?? FONT_SCALES[1]
  );

  const cycleFontScale = (): void => {
    const currentIndex = FONT_SCALES.findIndex(option => option.id === fontScale.value);
    const nextIndex = currentIndex < 0 ? 1 : (currentIndex + 1) % FONT_SCALES.length;
    fontScale.value = FONT_SCALES[nextIndex].id;
  };

  const setFontScale = (value: FontScaleId): void => {
    fontScale.value = value;
  };

  return {
    fontScale,
    currentFontScale,
    cycleFontScale,
    setFontScale,
    options: FONT_SCALES,
  } as const;
}
