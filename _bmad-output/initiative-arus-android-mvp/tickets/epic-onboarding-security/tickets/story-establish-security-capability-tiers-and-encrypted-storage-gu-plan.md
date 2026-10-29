---
title: 'Establish security capability tiers and encrypted-storage guard'
type: 'feature'
ticket: '1'
created: '2026-10-29'
status: 'draft'
baseline_revision: ''
route: 'full'
route_source: 'auto'
risk: 'high'
review: ''
review_source: ''
lenses_ran: []
review_loop_iteration: 0
context: ['_bmad-output/initiative-arus-android-mvp/tickets/epic-onboarding-security/epic-onboarding-security.md', '_bmad-output/initiative-arus-android-mvp/architecture-arus-android-mvp/architecture-arus-android-mvp.md', 'plan/decisions/ADR-006-expo-supersedes-native.md', 'plan/05-security-privacy.md', 'plan/08-release-readiness.md']
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Arus currently opens standard SQLite directly and has no explicit runtime contract distinguishing Expo Go's unencrypted development path from a binary that contains SQLCipher. Without a guard, a missing native cipher capability could silently produce an unencrypted database while the team believes security is active.

**Approach:** Add a pure, injectable storage-capability contract and connect it to database initialization. Expo Go is allowed only through an explicit development-only unencrypted flag; encrypted mode must verify `PRAGMA cipher_version` before any encryption claim or key operation. Unsupported capability errors must be structured, sanitized, and preserve the existing database without reset. Record the capability matrix and development-build verification boundary in an ADR.

## Boundaries & Constraints

**Always:** Expo Go remains a UI/standard-SQLite tier; a development build or standalone binary is the only tier that may prove SQLCipher. Runtime decisions are testable without importing native modules. Existing migration versioning, database name, retry boundary, and future-schema preservation remain unchanged. Diagnostics contain capability codes only, never keys, passphrases, database contents, or financial values.

**Never:** Do not implement PIN, biometrics, app lock, onboarding UI, business tables, final KDF/key wrapping, backup restore, or a production unencrypted fallback. Do not silently downgrade a required encrypted mode to standard SQLite. Do not create or edit generated `android/` or `ios/` directories.

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Expo Go development | Expo Go, development build flag enabled | Explicit `development-unencrypted` capability; standard SQLite may continue for UI work | No encryption claim; user-safe development notice only |
| Expo Go without flag | Expo Go, flag disabled | Initialization is refused before database consumers render | Structured capability error; no reset or downgrade |
| Cipher required and available | Native binary, cipher probe returns a version | Encrypted capability is accepted and reported without exposing the version as sensitive data | Continue to normal migration initialization |
| Cipher required and absent | Native binary or Expo Go, cipher probe returns null | Initialization is refused | Preserve database and show actionable rebuild/development-build guidance |
| Unsupported configuration | Unknown mode or malformed capability metadata | Initialization is refused deterministically | Sanitized error code; no secret or database data in output |

</frozen-after-approval>

## Code Map

- `src/db/database-provider.tsx` — current `SQLiteProvider` wrapper, `onInit`, loading/error boundary, retry key, and user-safe database messages. Keep its retry and preservation behavior while adding the capability gate before migrations.
- `src/db/migrations.ts` — current WAL/foreign-key setup and forward-only migration seam. Reuse its typed database parameter and invoke it only after storage capability validation; do not change schema version behavior.
- `app.json` — current CNG plugin list. Keep native configuration declarative and avoid generated folders; document that SQLCipher config requires a rebuilt binary rather than changing it to an unverified production setting.
- `eas.json` — existing development APK, preview APK, and production AAB profiles. The development profile is the manual/native verification surface.
- `tests/foundation.test.cjs` and `tsconfig.foundation.json` — existing Node test harness and compilation include list. Extend the harness with capability contract cases without importing React Native or native Expo modules.
- `plan/decisions/ADR-006-expo-supersedes-native.md` and `plan/05-security-privacy.md` — source of the Expo Go limitation, SQLCipher probe rule, and no-leak constraints.

## Tasks & Acceptance

**Execution:**

- [ ] `src/security/storage-capability.ts` — define runtime tiers, explicit development-unencrypted policy, sanitized typed errors, and cipher probe contract; keep policy pure and injectable so tests do not require Expo Go or a device.
- [ ] `src/db/database-provider.tsx` — gate `initializeDatabase` with the storage capability check and map capability failures to actionable Indonesian UI without changing retry/reset behavior.
- [ ] `tests/security-capability.test.cjs`, `tsconfig.foundation.json`, and `package.json` — compile and run the happy/error/preservation matrix through the existing repeatable foundation test command.
- [ ] `plan/decisions/ADR-007-expo-security-capability-tiers.md` — record the Expo Go, development-build, and production verification matrix and the explicit deferred SQLCipher state.

**Acceptance Criteria:**

- Given Expo Go UI development with the explicit flag enabled, when capability is resolved, then the result is development-only unencrypted and never labeled encrypted.
- Given encrypted mode is required but `PRAGMA cipher_version` is absent, when initialization runs, then it fails with a sanitized actionable error before migrations and does not reset or downgrade the database.
- Given a supported cipher probe result, when initialization runs, then normal WAL/foreign-key and forward migration behavior continues unchanged.
- Given the capability matrix tests run, when diagnostics are inspected, then no test output contains a passphrase, key material, financial value, or database contents.
- Given the quality gate runs from the repository root, when lint, typecheck, foundation tests, and Expo Doctor execute, then they pass; native development-build/device verification is recorded as passed or deferred with concrete evidence.

## Implementation Notes

## Plan Change Log

## Review Triage Log

## Design Notes

The policy must be separable from detection: `Constants.appOwnership`/execution context and build configuration are adapters, while the resolver accepts plain values. SQLCipher support is binary configuration, and Expo's SDK 58 documentation states that `PRAGMA cipher_version` is the verification signal because an unsupported `PRAGMA key` can otherwise be silently ignored. The current story should therefore prove the guard and matrix, not claim final encryption.

## Verification

**Commands:**

- `npm run lint` — expected: no ESLint errors.
- `npm run typecheck` — expected: no TypeScript errors.
- `npm run foundation:test` — expected: existing migration tests and capability matrix tests pass.
- `npm run doctor` — expected: Expo Doctor reports all project checks passing.

**Manual checks:**

- Run the UI path in Expo Go with the explicit development flag and confirm the status is visibly development-only, not encrypted.
- If an Android EAS development build is available, inspect the capability result and `PRAGMA cipher_version`; otherwise record native verification as deferred and do not claim SQLCipher support.
