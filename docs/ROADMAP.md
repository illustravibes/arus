# Roadmap Development — Arus MVP

> Roadmap implementasi berdasarkan `PRD.md`, `BUSINESS_CONTEXT.md`, `BUSINESS_RULES.md`, `USER_FLOWS.md`, dan `TECH_STACK_AND_UI.md`. Ini urutan kerja dan gerbang penerimaan, bukan estimasi tanggal rilis.

## Kondisi awal

Proyek Android satu modul dan database Room v1 tersedia. Tahap 1 (fondasi data lokal terenkripsi) telah dibangun dan diuji di Galaxy A54 Android 17 pada 7 November 2026: tes skema/enkripsi dan Keystore lulus. Tahap 2 sedang berjalan: onboarding dompet pertama serta daftar/tambah/ubah nama dan saldo awal sudah dibuat; validasi repository diuji di perangkat. Dialog pembuatan dan keterlihatan input saat keyboard tampil sudah diperiksa manual; alur simpan sukses, edit saldo awal dengan konfirmasi, dan pengelolaan kategori masih tersisa.

## Prinsip urutan

1. Kunci aturan finansial yang masih terbuka sebelum implementasi ledger.
2. Bangun fondasi proyek dan database sebelum layar yang bergantung pada data.
3. Selesaikan alur catat transaksi dan konsistensi saldo sebelum laporan.
4. Buat backup/restore setelah format data dan skema lokal cukup stabil.
5. Tutup pekerjaan dengan verifikasi perilaku, UI, keamanan, dan operasi offline.

## Tahapan

| Tahap | Fokus | Hasil yang bisa ditinjau | Gerbang selesai |
|---|---|---|---|
| 0. Keputusan produk & setup | Tutup keputusan bisnis terbuka; siapkan proyek Android satu modul. | Keputusan tercatat; Gradle Kotlin DSL, konfigurasi Kotlin/Compose/M3, package awal, manifest minimum, struktur dasar, dan aplikasi kosong dapat dibangun. | `:app:assembleDebug` berhasil pada toolchain yang dipilih; tidak ada permission internet atau dependency di luar stack yang disetujui. |
| 1. Fondasi data & keamanan lokal | Model Room v1, repository, enkripsi DB dan pengelolaan kunci lokal. | Entity/DAO untuk dompet, kategori, transaksi/transfer; repository/Flow; database SQLCipher dengan kunci yang dikelola Keystore. | **Lulus:** skema/enkripsi dan pemulihan passphrase Keystore diuji di Galaxy A54; tidak ada migrasi database lama. |
| 2. Dompet, kategori & onboarding | Buat dompet pertama dan kelola dompet/kategori. | **Berjalan:** onboarding dompet pertama, daftar/tambah/ubah dompet, validasi saldo awal, konfirmasi perubahan saldo awal. Kategori dan verifikasi interaksi form masih perlu. | Dompet/kategori bertahan setelah restart; dompet tidak dapat diarsipkan atau dihapus bersama histori; kategori bertipe benar; perubahan saldo awal dijelaskan dan dikonfirmasi. |
| 3. Ledger transaksi & transfer | Implementasi operasi pemasukan, pengeluaran, transfer, edit/hapus. | Form transaksi; validasi nominal positif; riwayat/detail; transfer atomik; saldo dihitung dari saldo awal dan transaksi efektif. | Test aturan saldo, tanggal masa depan, edit/hapus, transfer atomik, larangan self-transfer dan kategori salah; tidak ada saldo mutable kedua. |
| 4. Beranda & navigasi utama | Integrasikan empat tab dan ringkasan yang bersumber dari repository. | Beranda saldo/dompet/aktivitas; tab Beranda, Transaksi, Laporan, Pengaturan; tombol (+) untuk tiga jenis transaksi. | Angka beranda cocok dengan ledger; navigasi/back dan empty state bekerja; input cepat tanpa membuat duplikat saat submit berulang. |
| 5. Riwayat & laporan | Lengkapi pencarian/filter dasar dan agregasi laporan/grafik. | Daftar transaksi, cari/filter, detail/edit/hapus; ringkasan periode; grafik Canvas dua seri. | Test agregasi harian/mingguan/bulanan, filter dompet, batas tanggal lokal, transaksi masa depan, dan pengecualian transfer/saldo awal. Angka grafik dan ringkasan cocok. |
| 6. Backup & restore portabel | Format backup berversi dengan SAF, KDF/password, AES-GCM dan pemulihan aman. | Ekspor file terenkripsi; restore lintas instalasi dengan password; konfirmasi konsekuensi restore. | Round-trip backup/restore berhasil; password salah, file rusak, versi tak didukung, atau operasi gagal tidak mengganti database aktif. Jangan menyatakan aman sebelum pengujian ini lulus. |
| 7. Hardening & kesiapan rilis | Audit UX, aksesibilitas, offline, integritas, build dan distribusi internal. | Perbaikan state/loading/error, format `id-ID`, TalkBack/semantics, layout layar kecil dan font besar, checklist uji manual. | Build, unit test, lint dan uji alur pada emulator/perangkat aktual dicatat; mode airplane terverifikasi; tidak ada fitur di luar MVP yang tampil seolah aktif. |

## Keputusan produk yang tercatat

- Saldo dompet **boleh negatif**. Pengeluaran/transfer tidak ditolak hanya karena saldo tidak cukup.
- Saldo awal **boleh diedit setelah transaksi tercatat**. Perubahan saldo awal mengubah hasil saldo terhitung; transaksi historis tetap utuh.

## Keputusan yang masih terbuka

Tidak ada lagi keputusan saldo/dompet yang tercatat sebagai blocker sebelum Tahap 3. Perubahan keputusan bisnis berikutnya harus diperbarui di dokumen domain dan disertai test aturan terkait.

## Catatan progres 7 November 2026

- `:app:assembleDebug` berhasil setelah onboarding dompet dan penanganan system inset ditambahkan.
- `:app:connectedDebugAndroidTest` berhasil pada Galaxy A54 (SM-A546E, Android 17): 3 tes lulus, termasuk validasi repository dompet.
- APK terbaru dipasang dan onboarding diverifikasi lewat screenshot perangkat. Ditemukan dan diperbaiki overlap status bar; UI final terlihat tanpa overlap dan ikon system bar terbaca.
- Tahap 2 belum selesai: alur penyimpanan dan edit/konfirmasi belum dijalankan manual pada perangkat, layar pengelolaan kategori belum dibuat, dan state kegagalan database belum diuji end-to-end.

## Paket pengujian lintas tahap

- **Ledger:** saldo awal, income/expense positif, nol/angka overflow, tanggal masa depan, edit/hapus, transfer atomik/self-transfer, dan konsistensi total saldo.
- **Laporan:** periode dan batas zona waktu lokal, filter dompet, periode tanpa transaksi, transfer/saldo awal dikecualikan, hasil cocok dengan data ledger.
- **Backup:** round-trip, password salah, file rusak, skema tidak dikenal, pembatalan, dan kegagalan restore tanpa merusak data aktif.
- **UI/alur:** onboarding, tambah transaksi, riwayat edit/hapus, laporan kosong/berisi, dompet/kategori, backup/restore; layar sempit dan font sistem besar.
- **Operasional:** build debug, unit test, lint, dan alur utama dalam airplane mode. Catat perintah dan hasil aktual; jangan menyimpulkan lulus bila tidak dijalankan.

## Urutan rilis internal yang disarankan

1. **Build 0 — Kerangka aplikasi:** Tahap 0–1; aplikasi terbuka dan penyimpanan lokal siap.
2. **Build 1 — Catat dan cek saldo:** Tahap 2–4; onboarding, transaksi, transfer dan beranda berfungsi.
3. **Build 2 — Telusuri dan pahami:** Tahap 5; riwayat, pencarian/filter dan laporan.
4. **Build 3 — Lindungi data:** Tahap 6; backup dan restore diuji lintas instalasi.
5. **Kandidat MVP:** Tahap 7; semua acceptance criteria PRD punya bukti uji dan tidak ada blocker integritas data.

## Yang sengaja tidak masuk roadmap MVP

OCR, anggaran, transaksi rutin/pengingat, utang-piutang, split transaksi, biaya admin, integrasi bank, akun/cloud sync, multi-currency, dan ekspor PDF/Excel tetap di luar cakupan sampai PRD diubah.

## Catatan estimasi

Roadmap ini tidak menetapkan tanggal atau durasi karena ukuran tim, pengalaman Android, target perangkat, dan kesiapan keputusan produk belum ditentukan. Setelah tim dan kapasitas tersedia, setiap tahap dapat dipecah menjadi issue kecil dengan estimasi tanpa mengubah gerbang penerimaan di atas.
