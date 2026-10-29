/**
 * Arus design tokens.
 *
 * Keep semantic roles here instead of choosing screen-specific colours in a
 * route. The values follow the approved Material 3-inspired design spine.
 */

export const arusColors = {
  light: {
    primary: '#226B4F',
    onPrimary: '#FFFFFF',
    primaryContainer: '#A8F2C9',
    onPrimaryContainer: '#002114',
    surface: '#FAFDF9',
    surfaceContainer: '#EEF3EF',
    onSurface: '#191C1A',
    onSurfaceVariant: '#414943',
    error: '#BA1A1A',
  },
  dark: {
    primary: '#8DD5AE',
    onPrimary: '#003822',
    primaryContainer: '#075238',
    onPrimaryContainer: '#A8F2C9',
    surface: '#101411',
    surfaceContainer: '#1D211E',
    onSurface: '#E1E3DF',
    onSurfaceVariant: '#BFC9C0',
    error: '#FFB4AB',
  },
} as const;

export const arusTypography = {
  display: {
    fontSize: 40,
    lineHeight: 48,
    fontWeight: '600' as const,
  },
  title: {
    fontSize: 28,
    lineHeight: 36,
    fontWeight: '600' as const,
  },
  body: {
    fontSize: 16,
    lineHeight: 24,
    fontWeight: '400' as const,
  },
  label: {
    fontSize: 14,
    lineHeight: 20,
    fontWeight: '600' as const,
  },
} as const;

export const arusSpacing = {
  unit: 4,
  screen: 16,
  section: 24,
  touchTarget: 48,
} as const;

export const arusRadii = {
  card: 16,
  control: 12,
  pill: 999,
} as const;

export const arusElevation = {
  none: 0,
  tonal: 1,
  floating: 3,
} as const;

export const arusTouchTargets = {
  minimum: arusSpacing.touchTarget,
} as const;

export const arusTokens = {
  colors: arusColors,
  typography: arusTypography,
  spacing: arusSpacing,
  radii: arusRadii,
  elevation: arusElevation,
  touchTargets: arusTouchTargets,
} as const;

export type ArusColorRole = keyof (typeof arusColors)['light'];
