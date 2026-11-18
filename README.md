# Arus MVP

Arus adalah aplikasi pencatatan keuangan pribadi Android native, offline-first. Proyek Android satu modul (`:app`) sudah memiliki database Room v1 terenkripsi serta onboarding dan pengelolaan dasar dompet. Fitur ledger transaksi dan navigasi utama sedang dikembangkan sesuai roadmap.

## Isi repo

- [`AGENTS.md`](AGENTS.md) — aturan kerja coding agent.
- [`docs/PRD.md`](docs/PRD.md) — cakupan MVP dan acceptance criteria.
- [`docs/BUSINESS_CONTEXT.md`](docs/BUSINESS_CONTEXT.md) — masalah pengguna dan batasan bisnis.
- [`docs/BUSINESS_RULES.md`](docs/BUSINESS_RULES.md) — aturan saldo, transaksi, transfer, laporan, dan backup.
- [`docs/USER_FLOWS.md`](docs/USER_FLOWS.md) — alur pengguna dan kondisi gagal.
- [`docs/TECH_STACK_AND_UI.md`](docs/TECH_STACK_AND_UI.md) — stack, arsitektur, keamanan, dan pengujian.
- [`DESIGN.md`](DESIGN.md) — styling, token, komponen, dan layout.
- [`docs/ROADMAP.md`](docs/ROADMAP.md) — tahapan development dan gerbang penerimaan.
- `app/src/main/` — aplikasi Compose, Arus Design System, penyimpanan Room/SQLCipher, dan onboarding dompet.
- `samples/ArusHomeStylePreview.kt` — contoh visual Beranda dengan data demo, belum menjadi layar produk.

## Keputusan inti

- Kotlin + Jetpack Compose + Material 3 + Custom Arus Design System.
- Gaya visual: light blue-gray canvas, kartu putih rounded, judul bold, CTA pill; warna aksi ditentukan melalui uji kontras.
- Room + SQLCipher, database baru schema v1.
- Offline-first, tanpa backend atau login untuk MVP.
- Saldo boleh negatif; saldo awal boleh diedit setelah transaksi dengan konfirmasi; dompet tidak diarsipkan pada MVP.

## Build

Toolchain awal: Gradle Wrapper 9.4.1, Android Gradle Plugin 9.2.1, Compose BOM 2026.09.00, dan `minSdk` 26. Gunakan JDK 17.
Fondasi data memakai Room 2.8.5, KSP 2.3.13, dan `sqlcipher-android` 4.19.1.

```bash
./gradlew :app:assembleDebug
```

## Status

Pada 7 November 2026, `./gradlew :app:assembleDebug` berhasil dan `:app:connectedDebugAndroidTest` lulus dengan 3 tes pada Galaxy A54 Android 17. APK debug terbaru tersedia di `app/build/outputs/apk/debug/app-debug.apk` dan sudah terpasang di perangkat tersebut.

Onboarding dompet pertama, daftar/tambah/ubah dompet, validasi saldo awal, dan konfirmasi perubahan saldo awal sudah ada. Layar pengelolaan kategori, ledger transaksi/transfer, Beranda, navigasi empat tab, laporan, dan backup/restore belum selesai. Lihat [`docs/ROADMAP.md`](docs/ROADMAP.md) untuk progres dan gerbang berikutnya.
