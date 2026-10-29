---
status: final
updated: 2026-10-29
product: Arus
platform: Android-first mobile
colors:
  primary: "#226B4F"
  onPrimary: "#FFFFFF"
  primaryContainer: "#A8F2C9"
  onPrimaryContainer: "#002114"
  surface: "#FAFDF9"
  surfaceContainer: "#EEF3EF"
  onSurface: "#191C1A"
  onSurfaceVariant: "#414943"
  error: "#BA1A1A"
typography:
  display: "system display"
  body: "system sans"
  label: "system sans"
rounded:
  card: 16
  control: 12
  pill: 999
spacing:
  unit: 4
  screen: 16
  section: 24
  touchTarget: 48
components:
  elevation: "tonal-first"
  controls: "Material 3 via @expo/ui when available"
---

# DESIGN.md — Arus

## Brand & Style

Arus feels calm, trustworthy, and practical. It should help users understand money without looking like a bank portal. The existing wireframes' clear hierarchy and Indonesian labels are retained, while the sketch font, black outlines, and hard shadows are replaced with Android Material 3 surfaces, semantic color, and restrained tonal elevation.

## Colors

Use semantic roles above, not hardcoded screen-specific colors. Green communicates healthy balances and completion; amber warns without implying failure; red is reserved for destructive actions, invalid amounts, and breached limits. Dark mode derives equivalent semantic roles rather than inverting colors manually.

## Typography

Use Android/system sans typography with clear numeric emphasis. Rupiah amounts use tabular-feeling readable numerals where available. Screen titles are prominent but compact; supporting explanations use `onSurfaceVariant`. Never use decorative handwriting fonts in production.

## Layout & Spacing

Screens are portrait-first with 16dp horizontal padding, 24dp section rhythm, and 48dp minimum touch targets. The home screen follows the wireframe order: net worth, wallet carousel, actions, budget/task summaries, debt summary, and recent transactions. Content scrolls behind a stable bottom navigation area with safe-area insets.

## Elevation & Depth

Prefer surface containers, dividers, and tonal contrast. Use shadow only where a floating FAB, modal sheet, or transient snackbar needs separation. Never use the wireframe's decorative offset black shadow in production.

## Shapes

Cards use a 16dp radius; text fields and standard controls use 12dp; tags and status chips use pill shapes. Destructive dialogs and confirmation surfaces remain visually distinct but not theatrical.

## Components

- Primary action: filled Material button or FAB with one clear label/icon.
- Secondary action: tonal or outlined button; never make every action equally prominent.
- Quick add: Android FAB opens a bottom sheet; short tap may directly open expense as specified by the user flow.
- Pickers/sheets/toggles/sliders/menus: `@expo/ui` first, each tree wrapped in `Host`.
- History: RN `FlatList` for scalable transaction data, with accessible day headers.
- Feedback: snackbar with five-second Undo for ordinary deletion; dialog for irreversible or security-sensitive action.
- Empty states: explain what is missing and provide one next action, not decorative illustration alone.

## Do's and Don'ts

- Do show the source/detail path for every important total.
- Do keep copy in Indonesian and use consistent Rupiah formatting.
- Do preserve strong hierarchy from the wireframes.
- Do support dark theme, large text, TalkBack, and offline states.
- Don't expose financial values in debug UI or route names.
- Don't use a dense dashboard of equal-weight cards.
- Don't hide a destructive action behind an ambiguous icon.
