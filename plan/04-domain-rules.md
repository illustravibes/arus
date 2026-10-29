# Domain Rules dan Invariant

Sumber definisi lengkap: ERD bagian 3–7. Aturan berikut adalah gate wajib pada domain service dan test.

## Uang dan saldo

1. Tidak ada float/double untuk nominal.
2. `amount > 0`; `ADJUSTMENT` memakai `direction` untuk tanda, bukan nominal negatif.
3. Saldo dompet dihitung dari saldo awal dan efek type transaksi; tidak disimpan sebagai nilai turunan yang dapat drift.
4. Transaksi di masa depan tidak memengaruhi saldo saat ini.
5. Transaksi soft-deleted tidak muncul dalam saldo, laporan, atau daftar normal.

## Atomicity

- Transfer membuat `TRANSFER_OUT`, `TRANSFER_IN`, dan opsional `EXPENSE` admin dalam satu `transfer_group` dan satu database transaction.
- Resolusi rekonsiliasi beserta checkpoint dan penandaan transaksi tersimpan atomik.
- Kelak, finalisasi group order, pelunasan, write-off, netting, dan restore harus memiliki sifat yang sama.

## Referential integrity

- Σ split harus sama dengan amount transaksi untuk type yang mewajibkan split.
- Dompet dengan riwayat tidak boleh dihapus.
- System category tidak boleh dihapus.
- Edit/hapus transaksi terverifikasi wajib menandai checkpoint `needs_recheck`.
- Hapus origin debt yang telah memiliki allocation ditolak.
- Allocation debt tidak boleh melebihi principal; netting hanya antara debt berlawanan pada contact yang sama.

## Validasi UX yang harus juga ada di domain

- Dompet asal dan tujuan transfer berbeda.
- Nominal nol, ID referensi tidak ada, category yang diarsipkan untuk transaksi baru, dan kombinasi type/field yang ilegal ditolak di domain service walau UI sudah memvalidasi.
- Semua write menerima command/value object yang tervalidasi, bukan entity Room yang dimutasi langsung.

## Auditability

Pesan error harus menjelaskan invariant yang gagal tanpa memuat detail finansial pada log. Detail transaksi menampilkan hubungan yang memengaruhi angka: pasangan transfer, split, checkpoint, rule rutin, atau debt terkait.
