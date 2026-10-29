# Product Brief — Arus

## Problem dan nilai produk

Arus adalah aplikasi pencatat keuangan pribadi Android berbahasa Indonesia yang membantu pengguna mencatat uang masuk, uang keluar, dan perpindahan uang secara cepat—tetap bisa menjelaskan asal setiap angka melalui riwayat dan rekonsiliasi.

Nilai utama:

1. **Pencatatan cepat:** transaksi rutin dicatat dengan nominal, kategori, dan dompet tanpa alur rumit.
2. **Saldo dapat dipercaya:** saldo dihitung dari jurnal transaksi, dapat dibandingkan dengan saldo aktual, dan selisih dapat diselesaikan.
3. **Privat dan offline:** data finansial tidak bergantung pada koneksi atau akun cloud untuk penggunaan inti.
4. **Bertumbuh bersama kebutuhan:** budget, rutin, utang-piutang, group order, OCR, serta backup ditambahkan bertahap tanpa merusak data historis.

## Pengguna sasaran

- Pengguna Indonesia yang memakai kombinasi cash, bank, e-wallet, dan/atau kredit/paylater.
- Pengguna yang membutuhkan kontrol manual, bukan koneksi otomatis ke bank.
- Pengguna yang ingin membedakan pengeluaran sebenarnya dari transfer, pelunasan, pinjaman, dan uang talangan.

## Sasaran produk

| Sasaran | Ukuran keberhasilan awal |
|---|---|
| Input cepat | Pengeluaran biasa dapat disimpan dengan ≤3 interaksi setelah keypad nominal siap. |
| Akurasi saldo | Rekonsiliasi menghasilkan checkpoint dan perubahan sesudahnya selalu dapat ditelusuri. |
| Ketahanan offline | Alur MVP tidak membutuhkan jaringan. |
| Kepercayaan data | Tidak ada operasi finansial multi-record yang tersimpan setengah jadi. |
| Privasi | Data lokal, log, backup, dan layar aplikasi mengikuti kebijakan keamanan plan ini. |

## Non-goals MVP

- Sinkronisasi multi-perangkat real-time.
- Koneksi atau scraping rekening bank/e-wallet.
- Saran investasi, credit scoring, maupun nasihat finansial.
- Akuntansi bisnis / pembukuan pajak.
- OCR, cloud backup, dan export sebagai syarat penggunaan inti.

## Metrik yang diperbolehkan

Telemetry produksi hanya boleh bersifat opt-in, agregat, dan tidak memuat nominal, merchant, catatan, kontak, identitas perangkat stabil, maupun isi backup. Crash report harus disanitasi dari data transaksi.

## Risiko produk utama

| Risiko | Mitigasi |
|---|---|
| Saldo salah menghilangkan kepercayaan | Invariant domain, test property, rekonsiliasi, dan migrasi diuji. |
| Scope terlalu besar | Fase dibatasi; fitur kompleks tidak memblokir MVP. |
| Kehilangan data saat update/restore | Migrasi versioned, backup tervalidasi, restore atomik. |
| Kebocoran data finansial | Enkripsi at rest, redaksi log, dan permission kontekstual. |
