---
status: final
updated: 2026-10-29
scope: "Arus Android-first Expo initiative"
altitude: initiative
---

# Arus Architecture Spine

## Paradigm

Offline-first, command-driven feature slices over a single local SQLite source of truth. UI renders immutable query state and emits user intents; domain commands validate financial invariants; repositories own persistence; Expo Router owns route composition.

## Inherited invariants

- ERD and user flow define financial behavior, screen behavior, and edge cases.
- Amounts are positive integer Rupiah values; sign comes from transaction type/direction.
- Derived balances are queried, never stored as authoritative values.
- Historical entities use soft delete/archive semantics and restrictive references.
- Multi-record financial operations are atomic.

## Architecture decisions

### AD-1 — Expo is the implementation boundary

**Binds:** Expo SDK 58, React Native 0.88, Expo Router, `@expo/ui`, and Expo modules are the runtime baseline. Android is primary; universal APIs are preferred where they preserve Android behavior.  
**Prevents:** separate Kotlin/Compose and Expo implementations drifting.  
**Rule:** native folders are generated through CNG/config plugins only; no hand-maintained `android/` or `ios/` source is introduced for this initiative.

### AD-2 — UI/domain/persistence dependency direction

```text
src/app routes
  -> src/features screens/hooks
    -> src/domain commands/query models
      -> src/db repositories/SQLite
```

**Binds:** UI cannot mutate SQLite entities directly, and domain code cannot depend on React components.  
**Prevents:** form state, storage details, and domain rules becoming inseparable.

### AD-3 — SQLite owns durable state

**Binds:** one stable `SQLiteProvider`; WAL and foreign keys enabled; forward-only migrations use `PRAGMA user_version`; writes use bound parameters and awaited transactions.  
**Prevents:** divergent caches, SQL injection, partial writes, and migration resets.  
**Rule:** `execAsync` is reserved for trusted static DDL; user values use `runAsync`, prepared statements, or `db.sql`.

### AD-4 — Commands enforce financial invariants

**Binds:** every write is a validated command/value object; domain validation runs even when the UI already validates.  
**Prevents:** invalid transfers, zero/negative amounts, illegal category references, split mismatch, and inconsistent checkpoints entering the database.
**Rule:** repositories accept validated commands rather than mutable persistence entities.

### AD-5 — Query state is derived and refreshable

**Binds:** repositories expose typed query functions and feature hooks own loading/empty/error/populated states; writes invalidate or refresh affected state after commit.  
**Prevents:** stale dashboard totals and local UI state being treated as accounting truth.
**Rule:** query state is disposable presentation state; SQLite remains the source of truth.

### AD-6 — Native UI is an explicit island

**Binds:** use `@expo/ui` universal components first; use Jetpack Compose imports only in Android-isolated non-route components, always under `Host`; use RN `FlatList` for large datasets.  
**Prevents:** platform import crashes, non-virtualized history screens, and unnecessary community UI dependencies.
**Rule:** native UI imports never cross route/platform boundaries without an explicit supported fallback.

### AD-7 — Expo Go and development build are separate capability tiers

**Binds:** Expo Go verifies UI and standard SQLite; SQLCipher and any custom native capability require an EAS development build and a device check.  
**Prevents:** claiming encryption/security behavior that Expo Go cannot provide.
**Rule:** a capability is not accepted until it passes in the binary tier where it is implemented.

### AD-8 — Sensitive data has a no-leak boundary

**Binds:** financial data never appears in logs, route params, analytics, automatic screenshots, work request names, or error text; secrets use secure storage/key material APIs.  
**Prevents:** accidental leakage through debugging and navigation infrastructure.
**Rule:** diagnostics and navigation carry opaque identifiers and sanitized codes only.

### AD-9 — Verification is layered

**Binds:** each story supplies domain tests and relevant UI/database checks; every story runs `npx expo lint` and `npx tsc --noEmit`; native capability stories add EAS Android development-build verification.  
**Prevents:** relying on a single UI smoke test for accounting correctness or native security.
**Rule:** no story is done with only a visual/manual check when it changes a domain or persistence invariant.

## Seed structure

```text
src/app/          route files only
src/components/   shared visual primitives
src/design/       tokens and variants
src/db/           provider, migrations, repositories
src/domain/       money, dates, commands, invariants
src/features/     feature screens and view models/hooks
assets/data/      preset JSON
```

## Deployment envelope

- Local UI development: Expo Go through `npx expo start`.
- Native capability development: EAS `development` profile and installed dev client.
- Internal Android testing: EAS `preview` profile, APK/internal distribution.
- Production: EAS `production` profile, Android App Bundle, staged Play rollout.
- No backend is required for MVP. Network-dependent features are adapters introduced only in later epics.

## Deferred

- Drizzle ORM versus typed SQL.
- Final SQLCipher KDF/key wrapping and migration strategy.
- Background scheduling/exact alarm implementation.
- OCR engine and document capture pipeline.
- Cloud backup provider and retention policy implementation.
- Final Android minimum SDK and release signing ownership.
