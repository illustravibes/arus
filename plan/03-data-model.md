# Data Model Delivery Notes

Spesifikasi lengkap ada di [`../arus-erd.md`](../arus-erd.md). Dokumen ini menetapkan bagaimana spesifikasi itu dibawa ke implementasi yang aman dirilis.

## Baseline schema v1

MVP hanya membuat tabel yang diperlukan: `wallet`, `category`, `transaction`, `transaction_split`, `transfer_group`, `reconciliation`, dan `reconciliation_item`. Enum seluruh lifecycle tetap tersedia sejak awal, termasuk tipe debt fase lanjut, agar tidak ada migrasi ordinal/konversi type saat fitur ditambah.

## Konvensi persistence

- UUID/ULID lokal sebagai identifier yang opaque; tidak mengandalkan row id untuk referensi antar tabel.
- Nominal rupiah: `Long`, integer, positif pada kolom `amount`; overflow dan `0` ditolak di domain layer.
- Timestamp: epoch millis UTC; `timezone_id` IANA dipertahankan untuk konteks tampilan.
- Semua entity historis memiliki `created_at` dan `updated_at`; entity yang direferensikan memakai soft delete sesuai ERD.
- Foreign key eksplisit dan indeks dari ERD. `RESTRICT` menjadi default bila deletion dapat merusak sejarah.
- Category system di-seed dengan `system_code` stabil, bukan berdasarkan nama tampilan.

## Migration policy

1. Setiap schema change menaikkan version database dan menghasilkan file schema export yang dikomit.
2. Tambahkan migration forward untuk seluruh versi yang pernah dirilis; destructive migration dilarang untuk data pengguna.
3. Instrumented test harus memulai dari setiap schema release yang didukung, menjalankan migration, membuka DAO utama, lalu memvalidasi data/invariant.
4. Jika migration tidak bisa dilakukan aman, aplikasi berhenti dengan recovery path backup/export—bukan menghapus database diam-diam.

## Query correctness

- Filter `deleted_at IS NULL` dan future-dated sesuai definisi query saldo/riwayat yang relevan.
- Query saldo dan net worth memiliki test fixture untuk semua type transaksi.
- `SUM` null dikonversi pada boundary DAO/domain, bukan mengubah arti data.
- Semua paging memiliki total order deterministik.

## Data lifecycle

- Archive menyembunyikan pilihan baru tetapi mempertahankan riwayat.
- Delete transaksi adalah soft delete kecuali kebijakan retention berbeda telah disetujui dan dimigrasikan.
- Undo harus mengembalikan record yang sama bila belum ada perubahan bergantung; ia bukan membuat transaksi ekonomi kedua.
- Backup/restore menyertakan schema version, application version, checksum, dan metadata enkripsi.
