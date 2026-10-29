# E02 — Onboarding dan Keamanan

**Fase:** 1 / MVP

## Deliverables

- S-01 sampai S-05: welcome, wallet pertama, kategori awal, PIN/biometrik.
- Seed preset wallet/category dari asset JSON melalui onboarding.
- App lock, auto-lock, biometrik fallback PIN, dan pengaturan keamanan.

## Acceptance criteria

- Minimal satu dompet wajib sebelum masuk beranda.
- Seed tidak membuat duplikasi saat proses diulang/restart.
- PIN tidak tersimpan plaintext; lupa PIN mengikuti recovery policy.
- Deny/unavailable biometric masih memungkinkan PIN atau skip sesuai tahap onboarding.
