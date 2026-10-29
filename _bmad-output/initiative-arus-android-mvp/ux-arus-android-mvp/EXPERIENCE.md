---
status: final
updated: 2026-10-29
product: Arus
platform: Android-first mobile
design: ./DESIGN.md
---

# EXPERIENCE.md — Arus

## Foundation

Arus is a portrait-first, offline-first Android app for Indonesian personal finance. Expo Router provides navigation; Expo UI supplies native controls where available. The experience must remain useful in airplane mode after onboarding. Visual decisions live in `DESIGN.md`, referenced by tokens such as `{colors.primary}`, `{spacing.screen}`, and `{components.controls}`.

## Information Architecture

The five primary destinations are Beranda, Transaksi, Tambah, Anggaran, and Lainnya. Tambah is a central action rather than a content tab. Secondary destinations are reached from the relevant primary surface or the quick-add sheet. Routes carry opaque ids only.

## Voice and Tone

Use short, direct Indonesian copy. Explain what an amount means and what will happen before commit. Error copy states the failed rule and a recovery action. Avoid blame, banking jargon, and silent automation. Destructive copy names the consequence and offers Undo where possible.

## Component Patterns

- Forms focus the first meaningful input and preserve draft state during keyboard/navigation transitions.
- Sheets are used for short choices and quick actions; full routes are used for multi-step or review-heavy flows.
- Totals are tappable when a detail breakdown exists.
- Every write has an explicit save/confirm boundary. OCR, recurring confirmation, and reconciliation never write silently.
- Long lists use virtualization, stable keys, pagination or bounded loading, and clear empty/error states.

## State Patterns

Every feature defines loading, populated, empty, validation-error, persistence-error, and retry behavior as applicable. Offline is a normal state, not an error. A committed delete shows a five-second snackbar with Undo; undo restores the same record when safe. A failed atomic operation leaves no partial financial state.

## Interaction Primitives

- Tap: primary navigation and selection.
- Long press: optional shortcut only when discoverable and never the sole path.
- Swipe: secondary list action only with an accessible alternative.
- Back: respects Android predictive/back behavior and abandons or saves drafts explicitly.
- Snackbar: reversible feedback; dialog: confirmation or security decision; bottom sheet: short choice/action set.

## Accessibility Floor

All controls have TalkBack labels and state descriptions. Touch targets are at least `{spacing.touchTarget}`. Color is never the only status signal. Amounts, warnings, and errors remain understandable at large font sizes and in dark mode. Keyboard focus, screen-reader order, reduced motion, and contrast are checked on Android.

## Key Flows

### First-time setup — Sari creates a usable workspace

1. Sari opens Arus and sees a brief welcome she can skip.
2. She creates at least one wallet, chooses presets or custom values, and enters the current opening balance.
3. She chooses the categories she wants and optionally enables PIN/biometric lock.
4. The climax is reaching Beranda with a wallet and categories seeded exactly once, ready for offline use.

### Quick expense — Budi records lunch before leaving the cafe

1. Budi taps the central Add action and sees the amount keypad focused.
2. He enters the amount, taps a recent category, confirms the default wallet/date, and saves.
3. The climax is immediate confirmation and a visible Undo path without waiting for network.

### Explain a balance — Maya checks why the number changed

1. Maya taps net worth or a wallet card.
2. She opens the detail path showing contributing wallets, transactions, transfers, and liabilities.
3. The climax is being able to reconcile the displayed total to journal entries rather than trusting an opaque aggregate.

### Reconcile — Rina resolves a cash difference

1. Rina chooses a wallet and enters the real balance.
2. Arus shows app balance, real balance, difference, and candidate causes.
3. She checks or records the cause, resolves any remainder, and saves a checkpoint.
4. The climax is a durable verification point with a clear recheck warning if verified history changes later.

## Responsive and Platform

Android phone portrait is the primary surface. Small phones must preserve the quick-add path and avoid horizontal clipping. Larger screens may increase content width but must not turn the five-item navigation or transaction hierarchy into a desktop-only interaction. Expo Go is the UI test surface; EAS development builds are required for custom native/security capabilities.

## Anti-patterns

- Waiting for network for any MVP action.
- Hiding the actual wallet/date/category defaults.
- Silent OCR, recurring, restore, or reconciliation writes.
- Treating a cached total as accounting truth.
- Using a native-only import inside a route that can load on another platform.
