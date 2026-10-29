---
title: 'Establish Expo foundation boundaries and quality baseline'
type: 'feature'
ticket: '1'
created: '2026-10-29'
status: 'built'
review: 'quick'
review_source: 'pinned'
lenses_ran: ['quick']
baseline_revision: 'a4dcda3d058a6ca6a048d9d348d90c11e6ec117b'
route: 'full'
route_source: 'auto'
risk: 'high'
review: ''
review_source: ''
lenses_ran: []
review_loop_iteration: 0
context: ['_bmad-output/initiative-arus-android-mvp/architecture-arus-android-mvp/architecture-arus-android-mvp.md', '_bmad-output/initiative-arus-android-mvp/ux-arus-android-mvp/DESIGN.md', '_bmad-output/initiative-arus-android-mvp/ux-arus-android-mvp/EXPERIENCE.md', 'plan/decisions/ADR-006-expo-supersedes-native.md']
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** The repository is still the Expo starter app. Later Arus stories do not yet have a shared design-token layer, route/persistence boundary, SQLite migration seam, or domain primitive convention, so independent implementation would drift and could introduce unsafe financial storage patterns.

**Approach:** Establish the smallest runnable Arus foundation around the existing Expo Router shell: add shared design tokens, an async SQLite provider with forward migrations, pure domain primitives for Rupiah and dates, and explicit quality scripts. Keep feature behavior out of this story while making the foundation visible and verifiable in Expo Go.

## Boundaries & Constraints

**Always:** The route layer composes providers and screens only; domain code remains React-independent; SQLite initialization enables WAL and foreign keys and uses forward-only versioned migrations; SQL values are bound; amounts are positive integer Rupiah values; Expo UI/native capability constraints from ADR-006 remain binding; `npx expo lint`, `npx tsc --noEmit`, and `npx expo-doctor` remain passing.

**Never:** Do not implement wallets, categories, transactions, transfer, onboarding, reconciliation, SQLCipher key handling, OCR, backup, or network synchronization; do not create or edit generated `android/` or `ios/` directories; do not add Drizzle or another ORM before the foundation spike decides it.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| New database | No existing database, schema version 0 | Creates the foundation schema and records the supported version | Initialization rejects if schema setup fails; no partial migration is accepted |
| Existing supported database | Version lower than current | Applies each forward migration in order and opens normally | A failed transaction rolls back the schema step |
| Future database | Version greater than app-supported version | Does not attempt to downgrade or delete data | Exposes a user-safe initialization error and preserves the database |
| Invalid amount primitive | Zero, negative, non-integer, or unsafe numeric input | Domain constructor rejects the value | Returns a typed validation error without writing to SQLite |
| App launch | Provider initialization pending or failed | Route shell shows a stable loading/error boundary rather than rendering database consumers early | Retry path can reattempt initialization without deleting the database |

</frozen-after-approval>

## Code Map

- `src/app/_layout.tsx` -- current root composition; wraps the Expo theme, splash overlay, and tab shell and is the integration point for the database provider.
- `src/app/index.tsx` and `src/app/explore.tsx` -- current Expo starter routes; retain route viability while replacing starter-only foundation presentation with Arus-neutral shell content.
- `src/components/app-tabs.tsx` and `src/components/app-tabs.web.tsx` -- current native/web tab implementations; do not couple database or domain logic to these components.
- `src/constants/theme.ts` -- existing color, spacing, font, and layout constants; extend/reuse it as the compatibility surface for the new design tokens.
- `src/hooks/use-color-scheme.web.ts` -- existing web hydration hook; already repaired for the current React hook lint rule and must remain behaviorally equivalent.
- `app.json` -- Expo config with SDK-compatible module plugins; no generated native directories are present.
- `package.json` -- SDK 58 dependency and script baseline; `lint`, `typecheck`, and `doctor` scripts are the quality contract.
- `node_modules/expo-sqlite/skills/expo-sqlite/SKILL.md` -- installed package guidance for async APIs, bound parameters, provider initialization, WAL, foreign keys, and migration transactions.
- `_bmad-output/initiative-arus-android-mvp/architecture-arus-android-mvp/architecture-arus-android-mvp.md` -- binding architecture spine for boundaries and capability tiers.

## Tasks & Acceptance

**Execution:**

- [ ] `src/design/tokens.ts` -- add typed Arus semantic colors, typography, spacing, radii, elevation, and touch-target tokens derived from the approved design spine -- gives later screens one visual source of truth.
- [ ] `src/constants/theme.ts` -- reuse or bridge existing theme exports to the Arus token layer without breaking current themed components -- preserves starter compatibility while preventing token drift.
- [ ] `src/domain/money.ts` -- add pure Rupiah amount validation/formatting helpers with positive-integer semantics and safe rejection of invalid inputs -- centralizes the financial primitive before feature stories.
- [ ] `src/domain/date.ts` -- add pure epoch-millisecond and timezone-facing helpers needed by the ERD contract without introducing device-global time calls into domain logic -- keeps date behavior testable.
- [ ] `src/db/migrations.ts` -- add the versioned foundation migration function using static DDL, `PRAGMA user_version`, WAL, foreign keys, and transaction-safe forward upgrades -- establishes the persistence seam without business tables beyond the minimum foundation metadata.
- [ ] `tests/foundation.test.cjs` and `tsconfig.foundation.json` -- exercise fresh, current, future, failed-migration, and invalid-money matrix cases against the foundation seams -- makes the safety contract executable without requiring a device.
- [ ] `src/db/database-provider.tsx` -- expose one stable `SQLiteProvider` wrapper with loading/error boundary behavior and a retry-safe initialization seam -- prevents consumers from opening competing database connections.
- [ ] `src/app/_layout.tsx` -- compose the database provider above the route shell while preserving theme and splash behavior -- makes persistence availability explicit at the root.
- [ ] `src/app/index.tsx` and `src/app/explore.tsx` -- replace Expo starter copy with a neutral Arus foundation preview and provider-state-safe content, without adding business actions -- verifies route composition in Expo Go.
- [ ] `package.json` -- retain/add `lint`, `typecheck`, and `doctor` scripts and a foundation verification command if implementation exposes one -- makes the quality gate repeatable.

**Acceptance Criteria:**

- Given a fresh install, when the app launches in Expo Go, then the Arus foundation shell renders without the Expo starter onboarding and the tab routes remain navigable.
- Given a database with a supported older version, when the provider initializes, then migrations run forward inside a transaction and the app does not delete or recreate the database.
- Given a database version newer than the supported version, when initialization runs, then the provider exposes an actionable failure state and preserves the existing file.
- Given invalid money input, when a domain helper is called, then it rejects zero, negative, fractional, and unsafe values without performing I/O.
- Given the repository changes, when `npx expo lint`, `npx tsc --noEmit`, and `npx expo-doctor` run from the repository root, then all commands pass.
- Given later feature work, when a builder imports the foundation, then route files do not contain raw SQL, database entities, or duplicated design-token literals.

## Implementation Notes

- Added `src/design/tokens.ts` and bridged the existing theme exports to Arus semantic tokens.
- Added pure `src/domain/money.ts` and `src/domain/date.ts` primitives.
- Added `src/db/migrations.ts` and `src/db/database-provider.tsx` with a metadata-only schema v1 and safe future-schema error state.
- Replaced Expo starter route content with an Arus foundation shell and Indonesian tab labels.
- Added `tests/foundation.test.cjs` plus `tsconfig.foundation.json` for executable migration and money matrix coverage.
- Added `foundation:check`, `typecheck`, and `doctor` scripts.
- Android physical/device launch remains unverified because no emulator or device is attached.

## Plan Change Log

## Review Triage Log

## Design Notes

The first implementation should favor the smallest explicit seams over a complete accounting schema. The database provider may create only foundation metadata needed to prove migration behavior; wallet/category/transaction tables belong to their owning epics. The root provider should remain stable across route changes, and initialization should never silently reset a database.

## Verification

**Commands:**

- `npx expo lint` -- expected: no ESLint errors.
- `npx tsc --noEmit` -- expected: no TypeScript errors.
- `npx expo-doctor` -- expected: all project checks pass.
- `npx expo start --android` -- expected: Android Expo Go opens the foundation shell and both routes remain navigable.

**Manual checks:**

- Open the app in Expo Go with airplane mode enabled; confirm the foundation shell does not wait for a network request.
- Relaunch after the first database initialization; confirm initialization is idempotent and no reset prompt appears.
- Confirm no financial/business data or secrets appear in logs or route params.
