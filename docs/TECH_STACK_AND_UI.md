# Arus — Tech Stack, Arsitektur, dan UI Guidelines

> Acuan implementasi rebuild Android native. Baca bersama [`PRD.md`](./PRD.md) dan [`../AGENTS.md`](../AGENTS.md).

## 1. Keputusan teknologi

| Area | Pilihan | Catatan |
|---|---|---|
| Platform | Android native | Belum menargetkan iOS/web. |
| Bahasa | Kotlin | Kode dan identifier berbahasa Inggris; copy UI berbahasa Indonesia. |
| UI framework | **Jetpack Compose** | Tanpa XML Views untuk layar baru. |
| Komponen UI | **Material 3 + Custom Arus Design System** | Gunakan perilaku komponen M3, sesuaikan token dan tampilan. |
| Arsitektur | MVVM + Repository; domain helpers seperlunya | Hindari lapisan use-case satu-per-satu yang hanya meneruskan panggilan. |
| State | ViewModel + StateFlow + `collectAsStateWithLifecycle` | UI state immutable; event mengalir ke ViewModel. |
| Async | Kotlin Coroutines + Flow | Jangan melakukan I/O pada main thread. |
| Database | Room + SQLite | Schema baru **v1**. |
| Database encryption | `sqlcipher-android` | Jangan gunakan paket lama `android-database-sqlcipher` yang deprecated. |
| Navigasi | Navigation 3 untuk Compose | Gunakan satu sistem back stack; hindari dispatch layar manual di `MainActivity`. |
| Grafik | Compose Canvas | Grafik batang sederhana; tanpa library chart eksternal di MVP. |
| Preferensi | DataStore Preferences | Jangan simpan data transaksi di DataStore. |
| Kunci lokal | Android Keystore | Kunci database dilindungi di perangkat. |
| Backup | Storage Access Framework + AES-GCM | Enkripsi portabel dengan kata sandi backup terpisah dari kunci Keystore. |
| Dependency injection | Manual constructor injection | Hilt belum perlu pada MVP. |
| Build | Gradle Kotlin DSL + KSP | Pakai versi stabil yang kompatibel; jangan menebak/pin versi tanpa validasi. |
| Minimum Android | API 26 | Konsisten dengan target produk awal; compile/target SDK mengikuti stable toolchain saat setup. |

**Catatan:** Belum ada data produksi. Jangan membawa migrasi Room v1–v21 dari aplikasi lama. Begitu app dipakai, perubahan schema berikutnya harus punya migration dan tes.

## 2. Prinsip arsitektur

```text
Compose Screen
  └── ViewModel (UiState / Events)
        └── Repository (source of truth access)
              └── Room DAO / database

Domain functions (saldo, transfer, agregasi laporan)
  └── Dipakai repository / ViewModel saat relevan
```

- **Satu modul `:app`** pada MVP; pecah modul hanya karena kebutuhan nyata.
- **Room adalah source of truth** untuk dompet, kategori, transaksi, dan laporan.
- ViewModel tidak boleh mengakses DAO langsung; Compose tidak mengakses database.
- Nominal rupiah menggunakan `Long`; format rupiah hanya pada UI.
- Jangan menyimpan `balance` terhitung sebagai sumber kebenaran terpisah dari transaksi.
- Setiap operasi transfer multi-baris dan penghapusan/edit terkait harus memakai **Room transaction**.
- Gunakan pemodelan paling sederhana yang menjaga integritas ledger. Dokumen ini **tidak mengunci bentuk tabel persis**; desain entity/DAO sebelum implementasi, lalu uji invariant.
- Jangan membuat background worker, backend, analytics, atau dependency baru tanpa kebutuhan MVP yang jelas.

## 3. Struktur direktori yang disarankan

```text
app/src/main/java/com/illustravibes/arus/
├── MainActivity.kt
├── core/
│   ├── designsystem/   # theme, tokens, components, formatting
│   ├── database/       # Room, DAO, entity, type converters
│   ├── navigation/     # routes dan back stack
│   └── security/       # SQLCipher / Keystore / crypto helpers
├── data/
│   └── repository/
├── domain/
│   └── finance/        # saldo, transfer, agregasi laporan
├── feature/
│   ├── home/
│   ├── transaction/
│   ├── wallet/
│   ├── category/
│   ├── report/
│   └── settings/
└── backup/
```

Susunan ini **rekomendasi**, bukan alasan untuk membuat banyak abstraksi/kelas kosong.

## 4. Keputusan UI — Arus Design System

**Sumber kebenaran styling:** [DESIGN.md](../DESIGN.md). Arah visual menggabungkan bentuk rounded dan ruang lega ala Bevel dengan detail ilustratif dan warna kategori yang terinspirasi Notion.

- Tetap **Jetpack Compose + Material 3 + Custom Arus Design System**; tidak berpindah ke WebView/HTML.
- Warna aksi utama dipilih berdasarkan kontras yang teruji; `#0009FF` adalah kandidat, bukan kewajiban.
- Material 3 digunakan untuk semantik dan interaksi; komponen `Arus*` dibuat bila dibutuhkan berulang. Grafik sederhana tetap memakai Compose Canvas.
- Contoh pada `samples/` merupakan titik awal teknis, bukan sumber kebenaran styling atau klaim integrasi dan pengujian.

## 5. Pola UX yang wajib

- Empat tab utama: **Beranda, Transaksi, Laporan, Pengaturan**.
- Tombol utama **(+)** membuka pilihan pemasukan/pengeluaran/transfer.
- Subhalaman menggunakan app bar dan tombol kembali; hindari bottom bar pada form/detail.
- Satu aksi primer yang jelas per layar.
- Format uang `Rp` dengan pemisah ribuan Indonesia, tanpa pecahan untuk MVP.
- Loading, kosong, berhasil, dan gagal memiliki feedback; tindakan destruktif meminta konfirmasi.
- Ukuran font sistem besar harus tetap dapat digunakan. Dukung TalkBack, contrast, focus order, dan gesture back.
- Data grafik juga tersedia sebagai teks ringkasan agar tidak bergantung pada warna/visual.

## 6. Keamanan dan backup

- Database lokal terenkripsi, kunci dilindungi Android Keystore. Jangan commit kunci, password, atau nilai sensitif.
- Backup **portabel lintas perangkat** harus dienkripsi dengan kata sandi pengguna; jangan mengenkripsi backup hanya dengan kunci Keystore perangkat asal.
- Gunakan AES-GCM dengan nonce acak unik serta derivasi kunci dari password menggunakan KDF yang sesuai, salt acak, dan parameter yang disimpan dalam header format backup berversi.
- Verifikasi format, autentikasi, versi schema, dan integritas **sebelum** mengganti data aktif. Restore gagal harus mempertahankan data lama.
- Jangan menaruh nominal, password, atau isi database pada log produksi.
- Tidak memerlukan permission `INTERNET` untuk MVP.
- Uji kompatibilitas SQLCipher/Room pada emulator/perangkat target termasuk dukungan page size native yang relevan.

## 7. Standar pengujian

**Wajib (aturan bisnis):**
- Saldo awal ± income/expense; edit/hapus; transaksi masa depan; nol transaksi.
- Transfer antar-dompet atomik, saldo total tidak berubah, larangan self-transfer.
- Aggregasi grafik vs transaksi, rentang harian/mingguan/bulanan, filter dompet.
- Validasi nominal positif, referensi dompet/kategori, dan penghapusan/arsip.
- Backup→restore roundtrip, password salah, file rusak, restore terinterupsi.

**Wajib (UI):** satu screenshot/preview atau tes alur untuk beranda, input transaksi, riwayat, laporan, dan empty states; uji pada setidaknya satu emulator Android API 26 dan perangkat/emulator Android baru jika tersedia.

Perintah umum setelah proyek disiapkan:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
# Jika emulator tersedia:
./gradlew :app:connectedDebugAndroidTest
```

Jangan mengklaim tes lulus bila belum dijalankan.

## 8. Referensi resmi

- [Android app architecture](https://developer.android.com/topic/architecture)
- [Material 3 in Compose](https://developer.android.com/develop/ui/compose/designsystems/material3)
- [Custom design systems in Compose](https://developer.android.com/develop/ui/compose/designsystems/custom)
- [Navigation 3 basics](https://developer.android.com/guide/navigation/navigation-3/basics)
- [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
- [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider)
- [SQLCipher for Android](https://github.com/sqlcipher/sqlcipher-android)
