# AGENTS.md — Arus Android MVP

> Instruksi kerja untuk AI coding agent pada repository **Arus rebuild**. Baca file ini **sebelum** mengubah kode. Bahasa komunikasi: Indonesia; identifier dan kode: Inggris.

## 1. Sumber kebenaran dan prioritas

1. [`docs/PRD.md`](docs/PRD.md) — **scope dan acceptance criteria** MVP: apa yang wajib/terlarang.
2. [`docs/BUSINESS_CONTEXT.md`](docs/BUSINESS_CONTEXT.md) — masalah pengguna, tujuan produk, istilah bisnis, dan prioritas.
3. [`docs/BUSINESS_RULES.md`](docs/BUSINESS_RULES.md) — **aturan domain** saldo, transfer, edit/hapus, laporan, dan backup; gunakan untuk desain data dan test.
4. [`docs/USER_FLOWS.md`](docs/USER_FLOWS.md) — **alur pengguna** termasuk kasus kosong, gagal, dan konfirmasi.
5. [`docs/TECH_STACK_AND_UI.md`](docs/TECH_STACK_AND_UI.md) — stack, arsitektur, keamanan, dan pengujian.
6. [`DESIGN.md`](DESIGN.md) — sumber styling dan komponen UI.
7. [`docs/references/ui-inspiration.png`](docs/references/ui-inspiration.png) — inspirasi visual user, **bukan** spesifikasi fitur.
8. Kode dan pengujian aktual di repo — realitas implementasi (bukan izin mengubah scope).

**Urutan keputusan agent:** pahami masalah bisnis → cek scope PRD → pastikan invariants domain → ikuti user flow → pilih implementasi teknis → styling. **Jangan menurunkan aturan bisnis dari screenshot.**

Jika ada konflik/keputusan bisnis yang belum final (misalnya saldo negatif), **jangan diam-diam mengarang aturan**. Jelaskan trade-off dan minta keputusan pemilik proyek sebelum mengunci perilaku finansial. Jangan menganggap dokumentasi Arus lama sebagai spesifikasi rebuild.

## 2. Misi

Bangun aplikasi **Android native, offline-first, sederhana** untuk dompet, transaksi, transfer, riwayat, laporan/grafik, dan backup terenkripsi. Fokus utama adalah **akurasi saldo dan kemudahan mencatat transaksi**. Hindari over-engineering.

Tidak ada pengguna/data produksi pada saat rebuild: Room boleh dimulai dari **schema v1** dan **tidak perlu migrasi dari schema 21** proyek lama. Setelah rilis ke pengguna, perubahan schema harus mempertahankan data dengan migrasi eksplisit.

## 3. Keputusan teknis yang tidak boleh diganti diam-diam

- Kotlin + **Jetpack Compose** + **Material 3 dengan Custom Arus Design System**.
- Gaya visual mengikuti **`DESIGN.md`** : bentuk rounded dan ruang lega ala Bevel, dengan detail ilustratif serta warna kategori terinspirasi Notion; jangan menyalin brand.
- MVVM + Repository; ViewModel + StateFlow; Kotlin Coroutines/Flow; manual DI.
- Room + SQLite, database terenkripsi menggunakan paket modern **`sqlcipher-android`**.
- Navigation 3 untuk navigasi Compose; Compose Canvas untuk grafik batang.
- DataStore untuk settings, Android Keystore untuk kunci lokal, SAF + backup AES-GCM portabel.
- Satu module `:app` pada MVP; minimum Android API 26.
- Tidak ada backend, login, cloud sync, analytics eksternal, atau INTERNET permission tanpa perubahan scope.

**Jangan menambahkan** Hilt, Retrofit/Ktor, Firebase, WorkManager, Paging, CameraX, ML Kit, chart library, Clean Architecture berlapis, atau multi-module tanpa bukti kebutuhan dan persetujuan.

## 4. Aturan keuangan: invariant kritis

- Simpan semua nominal IDR sebagai **`Long` dalam rupiah**, bukan floating point.
- Nominal input harus **lebih dari nol**.
- **Room/ledger adalah satu-satunya source of truth**. Saldo dihitung dari saldo awal dan transaksi efektif.
- Transaksi tanggal masa depan tidak masuk saldo saat ini atau grafik realisasi.
- Pemasukan menambah saldo, pengeluaran mengurangi saldo.
- Transfer antar dompet harus atomik; kedua sisi konsisten; tidak boleh transfer ke dompet yang sama.
- Transfer tidak dihitung pada pemasukan/pengeluaran laporan atau grafik.
- Edit/hapus transaksi harus menjaga integritas data dan memperbarui semua ringkasan secara reaktif.
- Kategori pemasukan/pengeluaran tidak boleh tertukar.
- Buat test untuk setiap aturan yang diubah **sebelum** menyatakan implementasi selesai.

## 5. Aturan desain dan UX

- **Baca `DESIGN.md`** sebelum membuat/mengubah layar atau komponen; `docs/references/ui-inspiration.png` adalah sumber inspirasi tampilan, bukan asset produksi.
- Gunakan **Material 3 + Custom Arus Theme**, jangan memakai WebView / HTML / CSS / Tailwind.
- Token utama: background `#FFFFFF` (putih murni, keputusan user 2026-11-21 — tanpa tint biru), kartu `#FFFFFF`, teks `#1A1A1A`, teks sekunder `#616161`, border `#E5E5E5`; brand/CTA primer hitam netral `#1A1A1A` (keputusan final user 2026-11-18; biru `#0009FF` ditolak).
- Palet semantik finansial memakai token di design system; pemasukan/keluar tidak dibedakan hanya lewat warna.
- Arah UI: **kartu putih rounded, ikon soft tile, judul tebal, CTA pill, list tenang, whitespace terstruktur**. Jangan tiru hero foto, banner promo, katalog kursus, atau footer web dari referensi.
- Inter, pengganti SF Pro pada referensi Bevel (fallback sans-serif); nominal tabular bila perlu. Padding layar 12dp (compact rapat, keputusan user 2026-11-21); gap antar section 16dp; spacer bawah layar tab 110dp; target sentuh minimum 48dp. Label sentence case (bukan ALL-CAPS).
- Pakai Material 3 untuk kontrol form, dialog, sheet dan semantik aksesibilitas. Komponen bersama diberi nama `Arus*` jika dipakai berulang; jangan membuat abstraksi premature.
- Empat tab: Beranda, Transaksi, Laporan, Pengaturan; tombol **(+)** untuk 3 jenis transaksi.
- Subhalaman punya back navigation dan tanpa bottom bar secara default. Satu primary CTA per layar.
- Form, grafik, dan daftar harus punya loading, empty, error, success state yang sesuai. UI bahasa Indonesia; nominal `id-ID`.
- Preview/screenshot harus diverifikasi pada ukuran layar sempit serta font scale besar. **Jangan klaim pixel-perfect atau accessibility PASS tanpa pengujian nyata.**

## 6. Cara kerja setiap task

1. **Baca konteks**: `docs/PRD.md`, `docs/BUSINESS_CONTEXT.md`, dan `docs/BUSINESS_RULES.md`. Untuk perubahan alur baca `docs/USER_FLOWS.md`; untuk UI baca `DESIGN.md`; untuk teknis baca `docs/TECH_STACK_AND_UI.md`.
2. **Inspeksi kode yang ada**; jangan membuat komponen/repository baru jika yang ada sudah cukup.
3. Buat rencana perubahan **terkecil** yang memenuhi kebutuhan.
4. Implementasikan dengan state UI satu arah dan batasan layer yang jelas; jangan akses DAO dari Composable.
5. Tambahkan/ubah tes untuk aturan finansial, termasuk negative/edge cases.
6. Jalankan build dan tes relevan **jika lingkungan memungkinkan**; catat perintah dan hasil aktual.
7. Ringkas perubahan: file diubah, perilaku, pengujian, risiko/sisa pekerjaan.

Jangan mengganti arsitektur, dependensi, aturan bisnis, atau palet brand hanya karena preferensi agent. Jika task berpotensi melebar ke luar MVP, **berhenti pada scope yang diminta** dan jelaskan trade-off.

## 7. Aturan keamanan

- Jangan menulis password/kunci secara hardcoded, commit secrets, atau log data finansial pribadi.
- Jangan menggunakan `fallbackToDestructiveMigration()` sebagai jalan pintas setelah ada data nyata.
- Backup harus **dapat dipulihkan di perangkat lain** dengan password pengguna; tidak tergantung Keystore perangkat asal.
- Restore wajib memvalidasi file/authentication/schema terlebih dahulu, meminta konfirmasi, dan tidak mengganti database aktif jika gagal.
- Jangan mengklaim SQLCipher/backup aman tanpa implementasi dan tes yang memadai.

## 8. Quality gate (Definition of Done)

Untuk perubahan non-trivial, pastikan:

- [ ] Sesuai scope PRD, BUSINESS_CONTEXT, BUSINESS_RULES, dan USER_FLOWS; tidak menambah fitur di luar MVP.
- [ ] Saldo, transfer, laporan, dan grafik konsisten pada operasi tambah/edit/hapus.
- [ ] Kode build (jika toolchain tersedia); tidak ada error baru yang disengaja.
- [ ] Unit/integration test aturan bisnis terkait sudah ditulis dan dijalankan bila memungkinkan.
- [ ] UI mengikuti `DESIGN.md`, mendukung input validation, state kosong, font besar, dan navigasi kembali.
- [ ] Tidak ada kredensial/log sensitif, atau ketergantungan internet yang tidak diperlukan.
- [ ] Laporan akhir transparan tentang **apa yang diuji dan belum diuji**.

Perintah standar (sesuaikan SDK/emulator lokal):

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
# Bila emulator tersedia:
./gradlew :app:connectedDebugAndroidTest
```

## 9. Jangan implementasikan kecuali diminta

OCR, group order, utang-piutang, rekonsiliasi/checkpoint lanjutan, split transaksi, anggaran, transaksi rutin, pengingat, biaya admin transfer, integrasi bank, cloud sync, multi-currency, ekspor PDF/Excel.

**Prinsip keputusan:** pilih solusi paling sederhana yang menjaga integritas data. *Less noise. More control.*

## 10. Keputusan design system (Bevel + Notion, 2026-11-21)

Hasil audit 587 screenshot referensi lokal dan semua keputusan user 2026-11-21; detail lengkap di `DESIGN.md` § "Lima prinsip design system". Ringkasnya:

- **Lima prinsip:** struktur ikut Bevel (satu kartu = satu blok, gap antar kartu); pengelompokan di dalam kartu ikut Notion (hairline antar baris); CTA hitam `#1A1A1A`; warna isi palet Notion; sheet ikut Notion + tombol sheet ikut Bevel.
- **Aturan pola:** satu kartu per kelompok + divider (kecuali form Catat: satu pill per field + label di atasnya, 56dp sama dengan field Catatan); chip tak terpilih latar `ArusBrandSoft` tanpa border; baris transaksi judul = catatan, subtitle = `dompet • kategori • jam` (transfer: `dompet ❯ dompet • jam`); chart = label sumbu-Y kanan langkah cantik + batang terpilih opacity penuh; filter Riwayat = tombol Filter di kanan search + sheet Filter Transaksi (Jenis/Dompet/Kategori/Tanggal, opsi pill penuh 48dp + radio kustom, subtitle hitungan live "Menampilkan N dari M transaksi" di bawah judul) + chip filter aktif di bawah search yang bisa di-close; tombol Filter hitam saat ada filter aktif; Reset di sheet menghapus semua filter; jam transaksi = jam Simpan ditekan (kecuali dipilih manual/diubah); Rasio selalu tampil (pesan bila pemasukan 0); status backup persisten; konfirmasi nominal ≥ Rp100 jt; snackbar = `ArusSnackbar` pill hitam; detail sheet = "Ubah" hitam penuh + "Hapus" teks merah (tanpa "Tutup").
- **Yang tidak berubah:** primer hitam netral `#1A1A1A`, latar putih murni `#FFFFFF`, field pill (kecuali search 12dp), padding 12dp/16dp/110dp, tanpa mode gelap, tanpa fitur baru di luar MVP.
- Jangan menambah pola sekali-pakai baru: cek komponen `Arus*` yang sudah ada dulu; kalau pola yang dibutuhkan belum ada, tambahkan sebagai komponen reusable + tampilkan di `DesignSystemShowcase`.
