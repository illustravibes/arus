# Expo Implementation Plan — Arus

Dokumen ini menerjemahkan architecture plan native lama ke repository Expo yang aktif. Domain rules tetap mengikuti `arus-erd.md`; ini hanya menetapkan boundary implementasi agar story tidak memilih pola yang saling bertentangan.

## Struktur target

```text
arus/
  src/app/                 # Expo Router routes only
  src/components/          # reusable visual components
  src/design/              # tokens and component variants
  src/db/                  # SQLite provider, migrations, repositories
  src/domain/              # money, dates, commands, invariants
  src/features/            # feature UI + feature hooks/view models
  src/hooks/               # cross-feature hooks
  src/constants/           # stable app constants
  assets/data/              # wallet/category preset JSON
```

## UI and routing rules

- Route groups follow onboarding, authenticated tabs, and modal/sheet destinations.
- Route params carry opaque identifiers only, never entities or sensitive financial values.
- `@expo/ui` universal components are preferred for sheets, pickers, switches, sliders, menus, and short grouped forms.
- `FlatList` is used for transaction/history datasets; `@expo/ui` `List` is only for short fixed groups.
- Platform-specific `@expo/ui/jetpack-compose` code stays in non-route component files and is isolated from iOS/web imports.

## Persistence rules

- One stable `SQLiteProvider` owns the app database.
- Initialization enables WAL and foreign keys, then runs forward-only migrations.
- User input uses bound parameters/prepared statements; `execAsync` is only for trusted static DDL.
- All writes return domain results and refresh/invalidate UI query state after commit.
- No derived wallet balance is stored as authoritative data.

## Capability matrix

| Capability | Expo Go | EAS development build |
|---|---:|---:|
| UI/routes/@expo/ui | Yes | Yes |
| Standard expo-sqlite | Yes | Yes |
| SQLCipher | No | Required |
| Secure/local auth/screen capture | Partial/device-dependent | Required verification |
| OCR/native document processing | Not assumed | Required spike/build |
| Cloud backup provider | Not MVP | Required when implemented |

## Quality commands

```text
npx expo lint
npx tsc --noEmit
npx expo-doctor
npx expo start
```

Before security/native capability acceptance, create an EAS development build and repeat the Android smoke path on a physical device.
