/**
 * Below are the colors that are used in the app. The colors are defined in the light and dark mode.
 * There are many other ways to style your app. For example, [Nativewind](https://www.nativewind.dev/), [Tamagui](https://tamagui.dev/), [unistyles](https://unistyl.es/), etc.
 */

import '@/global.css';

import { Platform } from 'react-native';

import { arusColors, arusSpacing } from '@/design/tokens';

export const Colors = {
  light: {
    text: arusColors.light.onSurface,
    background: arusColors.light.surface,
    backgroundElement: arusColors.light.surfaceContainer,
    backgroundSelected: arusColors.light.primaryContainer,
    textSecondary: arusColors.light.onSurfaceVariant,
    primary: arusColors.light.primary,
    onPrimary: arusColors.light.onPrimary,
    error: arusColors.light.error,
  },
  dark: {
    text: arusColors.dark.onSurface,
    background: arusColors.dark.surface,
    backgroundElement: arusColors.dark.surfaceContainer,
    backgroundSelected: arusColors.dark.primaryContainer,
    textSecondary: arusColors.dark.onSurfaceVariant,
    primary: arusColors.dark.primary,
    onPrimary: arusColors.dark.onPrimary,
    error: arusColors.dark.error,
  },
} as const;

export type ThemeColor = keyof typeof Colors.light & keyof typeof Colors.dark;

export const Fonts = Platform.select({
  ios: {
    /** iOS `UIFontDescriptorSystemDesignDefault` */
    sans: 'system-ui',
    /** iOS `UIFontDescriptorSystemDesignSerif` */
    serif: 'ui-serif',
    /** iOS `UIFontDescriptorSystemDesignRounded` */
    rounded: 'ui-rounded',
    /** iOS `UIFontDescriptorSystemDesignMonospaced` */
    mono: 'ui-monospace',
  },
  default: {
    sans: 'normal',
    serif: 'serif',
    rounded: 'normal',
    mono: 'monospace',
  },
  web: {
    sans: 'var(--font-display)',
    serif: 'var(--font-serif)',
    rounded: 'var(--font-rounded)',
    mono: 'var(--font-mono)',
  },
});

export const Spacing = {
  half: 2,
  one: arusSpacing.unit,
  two: arusSpacing.unit * 2,
  three: arusSpacing.screen,
  four: arusSpacing.section,
  five: arusSpacing.unit * 8,
  six: 64,
  screen: arusSpacing.screen,
  section: arusSpacing.section,
  touchTarget: arusSpacing.touchTarget,
} as const;

export const BottomTabInset = Platform.select({ ios: 50, android: 80 }) ?? 0;
export const MaxContentWidth = 800;
