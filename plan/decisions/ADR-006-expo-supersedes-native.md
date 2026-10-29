# ADR-006 — Expo/React Native sebagai Implementation Stack

**Status:** Accepted  
**Tanggal:** 2026-10-29  
**Menggantikan:** bagian implementation stack pada ADR-002, ADR-003, dan asumsi native pada ADR-005. Aturan domain/local-first ADR-001 tetap berlaku.

## Konteks

Rencana awal Arus memakai Jetpack Compose, Room, SQLCipher, Hilt, WorkManager, dan CameraX. Repository yang benar-benar tersedia sekarang adalah aplikasi Expo SDK 58 dengan Expo Router, React Native, `@expo/ui`, dan pengujian utama melalui Expo Go. Menjaga dua stack sebagai rencana aktif akan membuat ticket dan implementasi menyimpang.

## Keputusan

Expo/React Native adalah stack implementasi resmi Arus. Android tetap menjadi platform utama, tetapi komponen universal dipilih jika tidak mengurangi kualitas Android. `@expo/ui` menjadi pilihan pertama untuk primitive native Android yang tersedia; React Native dipakai untuk layar/layout dan fallback ketika primitive yang dibutuhkan tidak tersedia atau lebih aman di JS.

## Pemetaan teknologi

| Rencana native | Arus Expo | Catatan |
|---|---|---|
| Compose + Material | Expo Router + React Native + `@expo/ui` Jetpack Compose/universal | Setiap native tree memakai `Host`; route tidak memakai platform extension. |
| Room | `expo-sqlite` dengan typed SQL dan migration `PRAGMA user_version` | Query async, WAL, foreign keys, transaksi atomik. Drizzle ditunda sampai spike. |
| SQLCipher + Keystore | `expo-sqlite` `useSQLCipher` melalui config plugin + secure key material | Tidak tersedia di Expo Go; harus diverifikasi pada EAS development build. |
| DataStore | `expo-sqlite/kv-store` atau `expo-secure-store` | Secret dan verifier tidak disimpan di key-value biasa. |
| BiometricPrompt | `expo-local-authentication` | Fallback PIN dan recovery policy tetap domain rule. |
| FLAG_SECURE | `expo-screen-capture` dan konfigurasi native yang didukung | Capability wajib diuji pada device Android. |
| WorkManager | Expo notifications/task APIs atau native module/config plugin bila diperlukan | Exact alarm dan background execution bukan dependency MVP. |
| CameraX/OCR | Expo camera/image/document capture; OCR custom native hanya setelah capability spike | OCR fase 4 dan tidak boleh memblokir MVP. |
| Gradle modules/Hilt | Feature folders, providers, repositories, domain services, typed route boundaries | Jangan membuat modul native manual sebelum ada kebutuhan nyata. |

## Aturan yang tetap mengikat

- ERD dan user flow adalah sumber kebenaran perilaku, nominal, saldo, atomicity, dan lifecycle.
- `amount` adalah integer positif; saldo dihitung dari transaksi aktif dan tanggal efektif.
- Semua write finansial melewati command/domain validation; UI validation bukan satu-satunya gate.
- Semua operasi multi-record memakai transaksi SQLite dan test rollback.
- `src/app/` hanya berisi route; domain, database, feature, dan shared UI berada di luar route.
- Native directories tidak dikomit/diedit manual; gunakan config plugin/CNG/EAS.
- Expo Go smoke test wajib untuk UI dan standard SQLite. Capability yang membutuhkan custom binary harus memiliki EAS development-build check.

## Konsekuensi

- Rencana native lama tidak lagi menjadi instruksi build, tetapi tetap menjadi sumber domain/security intent sampai ditranslasikan.
- SQLCipher, backup provider, OCR, exact alarm, dan beberapa background capability membutuhkan spike dan kemungkinan development build.
- Testing berubah dari Room/instrumented Compose menjadi domain tests, SQLite migration/query tests, React Native component/route tests, Expo Go manual smoke tests, dan EAS Android build checks.

## Deferred

- Pemilihan Drizzle vs typed SQL.
- Implementasi final KDF/key wrapping untuk SQLCipher.
- Provider backup cloud dan OCR engine.
- Background scheduling yang benar-benar presisi.
- Minimum Android SDK final selain baseline yang dipaksakan SDK 58.
