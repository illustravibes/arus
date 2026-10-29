---
tracker_id: ""
id: 1
type: epic
title: "Expo foundation and shared contracts"
parent: initiative-arus-android-mvp
covers: ["R1", "R2", "R3", "R5", "R6", "R7"]
sources: ["_bmad-output/initiative-arus-android-mvp/initiative-arus-android-mvp.md", "plan/decisions/ADR-006-expo-supersedes-native.md", "_bmad-output/initiative-arus-android-mvp/architecture-arus-android-mvp/architecture-arus-android-mvp.md", "_bmad-output/initiative-arus-android-mvp/ux-arus-android-mvp/DESIGN.md", "_bmad-output/initiative-arus-android-mvp/ux-arus-android-mvp/EXPERIENCE.md"]
companions: []
after: []
refined: true
risk: high
---

# Expo foundation and shared contracts

## Description

Stand up the Expo implementation boundaries, shared design tokens, route shell, SQLite migration provider, domain primitives, and quality commands needed by every later epic.

## Outcome

A clean checkout can open the app, run the shared foundation checks, and demonstrate a versioned local database without introducing feature-specific accounting behavior yet.

## Requirements

1. **Foundation is runnable** (parent R7)
   **Given** a clean checkout
   **When** the foundation commands run
   **Then** Expo lint, TypeScript, Expo Doctor, and the Android Expo Go launch path complete successfully.

2. **Shared boundaries exist** (parent R5, R6)
   **Given** a feature story starts
   **When** it adds UI, domain, or persistence code
   **Then** the route, feature, domain, database, and design boundaries are available and documented.

3. **SQLite migration baseline is safe** (parent R2, R3, R6)
   **Given** a new database or supported prior schema
   **When** the provider initializes
   **Then** WAL/foreign keys and forward migration behavior are applied without destructive reset, and a failed migration does not leave a partial schema.

4. **Native capability boundary is explicit** (parent R4, R7)
   **Given** work requires SQLCipher or another custom native capability
   **When** the story is planned
   **Then** the Expo Go limitation and EAS development-build verification step are visible in the implementation contract.

## Constraints

- Do not implement business features in this epic.
- Do not create or hand-edit `android/` or `ios/` directories.
- Keep source docs in Indonesian and code identifiers in English.

## Done when

- Foundation folders and app shell are ready for feature stories.
- A versioned SQLite provider/migration seam exists and is covered by a smoke test or documented test seam.
- Design tokens and Expo UI selection rules are available to later stories.
- EAS development/preview/production configuration is present and validated syntactically.
- All baseline quality commands pass.

## Boundaries

- Wallets, categories, transactions, transfer, onboarding, and reconciliation belong to later epics.
- SQLCipher key implementation is a security-epic spike, not foundation completion.

## References

- parent — `_bmad-output/initiative-arus-android-mvp/initiative-arus-android-mvp.md`
- architecture — `_bmad-output/initiative-arus-android-mvp/architecture-arus-android-mvp/architecture-arus-android-mvp.md`
- design — `_bmad-output/initiative-arus-android-mvp/ux-arus-android-mvp/DESIGN.md`
- experience — `_bmad-output/initiative-arus-android-mvp/ux-arus-android-mvp/EXPERIENCE.md`
- ADR — `plan/decisions/ADR-006-expo-supersedes-native.md`
