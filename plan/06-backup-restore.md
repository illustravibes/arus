# Backup & Restore Plan

Backup adalah Phase 4 dan tidak menjadi dependency MVP, tetapi format dan safety gate ditetapkan lebih awal agar tidak menutup opsi recovery.

## Kontrak backup

Backup berisi database terenkripsi/logical export yang konsisten, attachment yang direferensikan, serta manifest. Manifest tidak memuat kata sandi atau key material.

```json
{
  "formatVersion": 1,
  "schemaVersion": 1,
  "createdAt": 0,
  "appVersion": "0.0.0",
  "encrypted": true,
  "checksum": "sha256:...",
  "attachmentsIncluded": true
}
```

## Aturan safety

1. Buat snapshot konsisten; jangan menyalin database aktif secara mentah tanpa mekanisme backup database yang aman.
2. Encrypt backup dengan key/password recovery yang berbeda dari key database perangkat.
3. Gunakan KDF modern dengan salt unik dan parameter yang disimpan di manifest/envelope; parameter final dipilih lewat security review.
4. Verifikasi checksum, dekripsi, dan kompatibilitas schema **sebelum** data lokal diubah.
5. Restore adalah replace-all atomik: staging → validasi → swap. Jika gagal, data lokal sebelumnya tetap dapat dibuka.
6. Backup schema lebih baru daripada aplikasi ditolak dengan instruksi update aplikasi.
7. Restore tidak pernah otomatis akibat login atau file ditemukan; pengguna harus memberi konfirmasi eksplisit.

## Storage dan scheduling

- Provider cloud dibuat sebagai adapter; Google Drive `appDataFolder` hanyalah implementasi awal yang mungkin.
- Backup otomatis memakai WorkManager dan constraint Wi-Fi/charging sesuai pilihan pengguna.
- Retention, ukuran maksimum, status terakhir, retry, dan error yang dapat ditindak pengguna harus ditampilkan.
- Attachment besar harus memiliki progress dan strategi pembatalan aman.

## Test wajib sebelum rilis backup

- Round-trip database kosong, database besar, data dengan attachment, dan tiap schema yang didukung.
- Password salah, checksum salah, arsip terpotong, storage penuh, aplikasi dihentikan saat backup/restore.
- Restore lintas perangkat serta penolakan downgrade schema.
- Tidak ada key/password/isi data pada log, notification, atau nama file cloud yang dapat dibaca publik.
