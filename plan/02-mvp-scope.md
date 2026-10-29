# MVP Scope dan Acceptance Criteria

## Tujuan MVP

Memberi pengguna aplikasi offline untuk menyiapkan dompet, mencatat transaksi inti, melihat saldo/riwayat, serta mencocokkan saldo aktual tanpa mengorbankan integritas dan keamanan data.

## In scope

| Area | Cakupan MVP |
|---|---|
| Onboarding | Sambutan, minimal satu dompet, pemilihan kategori bawaan, PIN/biometrik opsional. |
| Keamanan | App lock, auto-lock, proteksi recent-app yang dapat dipilih, data database terenkripsi. |
| Dompet | Buat, edit, urutkan, arsipkan; bank, e-wallet, cash, kredit. |
| Kategori | Pengeluaran/pemasukan datar, system category yang tidak dapat dihapus. |
| Transaksi | Income, expense, edit, hapus dengan undo, detail, tanggal masa depan. |
| Transfer | Dua sisi transfer dan biaya admin opsional, semuanya atomik. |
| Riwayat | Daftar chronologis yang dipaging dan detail transaksi. |
| Beranda | Net worth, ringkasan dompet, dan transaksi terbaru. |
| Rekonsiliasi dasar | Input saldo aktual, resolusi selisih, checkpoint. |
| Settings | Tema, format, awal periode, keamanan dasar. |

## Di luar MVP

Budget/laporan, split kategori, pencarian/filter, transaksi rutin/reminder, cek mutasi, kontak, utang-piutang, group order, OCR/lampiran, backup/restore, dan export. Enum transaksi fase lanjut tetap disediakan sejak database v1 sesuai ERD.

## Acceptance criteria lintas MVP

1. Nominal disimpan sebagai integer `Long` rupiah dan selalu positif; arah ditentukan type/direction.
2. Saldo hanya menggunakan transaksi aktif dengan `occurred_at <= now` dan rumus pada ERD.
3. Income, expense, transfer, biaya transfer, dan adjustment tidak dapat menghasilkan state parsial.
4. Edit/hapus transaksi sebelum checkpoint memperbarui tampilan; pada transaksi terverifikasi, pengguna diperingatkan dan checkpoint ditandai `needs_recheck`.
5. Dompet yang memiliki transaksi tidak dapat dihapus; hanya dapat diarsipkan.
6. Aplikasi inti dapat berjalan dalam mode pesawat setelah onboarding selesai.
7. Semua layar MVP memiliki loading, empty, validation-error, dan retry/error state yang relevan.
8. Semua operasi keuangan dapat dibatalkan aman sebelum commit; operasi hapus biasa memiliki undo selama lima detik.

## Exit criteria MVP / alpha internal

- Semua acceptance criteria di atas terotomasi atau memiliki test manual yang dapat diulang.
- Migration dari database kosong dan versi rilis sebelumnya lulus instrumented test.
- Tidak ada crash blocker, data-loss defect, atau security blocker yang diketahui.
- Checklist `checklists/pre-alpha.md` selesai.
