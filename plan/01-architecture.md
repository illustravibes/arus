# Android Architecture Plan

## Baseline

Arsitektur mengikuti praktik Android modern dan rujukan `android/skills`: Jetpack Compose, lifecycle-aware state collection, Navigation Compose dengan route bertipe, testing berlapis, permission kontekstual, dan hardening Android intent. Versi library dikunci melalui version catalog dan diperbarui terkontrol.

## Bentuk aplikasi

```text
Compose UI
  → ViewModel (UI state + intent)
  → Use case / domain service
  → Repository interface
  → Room DAO + encrypted database / platform service
```

- UI hanya merender `UiState` immutable dan mengirim event pengguna.
- ViewModel tidak menyimpan `Activity`, `View`, `Context`, maupun mutable database entity sebagai UI state.
- Domain service menegakkan invariant finansial sebelum write.
- Repository menyembunyikan Room, enkripsi, dan sumber data lain.
- Room adalah single source of truth; query `Flow` dipetakan menjadi model domain/UI.

## Struktur modul bertahap

Mulai sederhana agar build cepat; ekstrak modul saat ownership, compile time, atau dependency boundary membutuhkannya.

```text
:app                         composition root, MainActivity, navigation root
:core:model                  value object dan enum lintas domain
:core:common                 result/error, dispatcher, clock, formatter umum
:core:database               Room, migrations, SQLCipher integration, DAO
:core:security               app-lock, key material abstraction, secure settings
:core:designsystem           theme, icon wrapper, reusable Compose component
:core:testing                fixture dan test utility
:feature:onboarding
:feature:wallet
:feature:category
:feature:transaction
:feature:dashboard
:feature:reconciliation
```

Pada Phase 2+, tambahkan feature module budget, recurring, search, dan reminder; Phase 3 debt dan group-order; Phase 4 backup, export, ocr. Hindari memecah tiap class sebagai modul.

## Navigation

- Satu navigation host tingkat aplikasi; feature mengekspos destination dan callback, bukan `NavController` ke UI leaf.
- Route harus typed/serializable dan hanya memuat identifier kecil (`transactionId`, `walletId`), bukan entity/nominal/catatan sensitif.
- Deep link notifikasi divalidasi, mengarah ke destination internal eksplisit, dan aman ketika target telah dihapus/diarsipkan.
- State form penting disimpan via `SavedStateHandle`; draft kompleks fase lanjut disimpan ke database, bukan bundle.

## Persistence dan concurrency

- Room versioned sejak v1 dengan `exportSchema = true`.
- Semua enum disimpan sebagai text, bukan ordinal.
- Semua finalisasi transfer, rekonsiliasi, group order, pelunasan, dan restore dijalankan dalam transaction database.
- Query agregat saldo/laporan dilakukan di SQL, tidak dengan memuat seluruh transaksi ke memori.
- Paging menggunakan kunci stabil `(occurred_at, id)`.
- `Clock` dan timezone resolver di-inject agar perhitungan tanggal dapat diuji.

## Background work

- `WorkManager` untuk pekerjaan deferrable dan durable: recurring catch-up, reminder fallback, backup otomatis, retry upload.
- Unique work + idempotency key pada tiap pekerjaan yang dapat diulang.
- Exact alarm hanya untuk reminder yang benar-benar perlu presisi dan setelah izin/justifikasi tersedia; fallback harus jelas kepada pengguna.
- Background worker tidak mengakses UI state dan harus aman bila dijalankan lebih dari sekali.

## Build dan quality

- Kotlin DSL, Gradle Version Catalog, convention plugin saat konfigurasi berulang muncul.
- Variant minimum: `debug` dan `release`; jangan membuat flavor tanpa kebutuhan distribusi yang nyata.
- CI wajib menjalankan format/lint, unit test, instrumented migration test, dan build release reproducible.
- R8 aktif dan diuji di release; release signing secret hanya dari CI secret store atau mesin rilis yang terkontrol.

## Keputusan yang sengaja ditunda

- Minimum SDK / target SDK final ditentukan ketika bootstrap proyek dengan versi Android/AGP stabil saat itu.
- Hilt vs manual DI: default Hilt untuk graph produksi; evaluasi tetap saat bootstrap.
- Navigation 3 digunakan setelah stabil terhadap baseline proyek; bila tidak, Navigation Compose stabil digunakan dengan batas abstraksi yang sama.
