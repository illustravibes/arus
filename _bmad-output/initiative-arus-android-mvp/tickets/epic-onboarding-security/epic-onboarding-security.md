---
tracker_id: ""
id: 2
type: epic
title: "Onboarding and app security"
parent: initiative-arus-android-mvp
sources: ["_bmad-output/initiative-arus-android-mvp/initiative-arus-android-mvp.md", "plan/05-security-privacy.md", "arus-user-flow.md"]
companions: ["_bmad-output/initiative-arus-android-mvp/architecture-arus-android-mvp/architecture-arus-android-mvp.md", "_bmad-output/initiative-arus-android-mvp/ux-arus-android-mvp/DESIGN.md", "_bmad-output/initiative-arus-android-mvp/ux-arus-android-mvp/EXPERIENCE.md", "plan/decisions/ADR-006-expo-supersedes-native.md"]
after: []
refined: true
risk: high
---

# Onboarding and app security

## Description

Menyediakan jalur pengguna baru dari first launch sampai workspace lokal yang siap dipakai, lalu melindungi workspace dengan PIN, biometrik, auto-lock, dan batas kemampuan Expo Go versus EAS development build.

## Outcome

Pengguna Android dapat membuat workspace offline dengan minimal satu dompet dan kategori, kemudian membuka kembali data lokal melalui PIN atau biometrik tanpa menyimpan PIN plaintext atau mengklaim enkripsi yang belum diverifikasi pada binary yang tepat.

## Requirements

1. **Workspace pertama siap dipakai** (parent R4)
   **Given** aplikasi dijalankan untuk pertama kali
   **When** pengguna menyelesaikan onboarding
   **Then** Arus memiliki minimal satu dompet, kategori pilihan, dan state onboarding yang tersimpan secara idempotent sehingga Beranda dapat digunakan offline.

2. **Kunci aplikasi memiliki fallback aman** (parent R4)
   **Given** pengguna mengaktifkan keamanan aplikasi
   **When** aplikasi dibuka atau kembali dari kondisi terkunci
   **Then** PIN atau biometrik yang valid dapat membuka aplikasi, PIN tidak disimpan plaintext, dan PIN tetap tersedia sebagai fallback ketika biometrik gagal atau ditolak.

3. **Lifecycle dan privasi terlindungi** (parent R4)
   **Given** aplikasi berada di background, recent apps, log, atau error state
   **When** data sensitif dapat terlihat atau proses gagal
   **Then** aplikasi menerapkan auto-lock sesuai pengaturan, menyediakan proteksi tampilan yang tersedia, dan hanya mengeluarkan diagnostics tersanitasi tanpa nominal, catatan, atau secret.

4. **Capability keamanan jujur terhadap binary** (parent R6)
   **Given** fitur membutuhkan SQLCipher atau kemampuan native khusus
   **When** aplikasi dijalankan di Expo Go atau EAS development build
   **Then** Expo Go hanya mengaktifkan jalur development unencrypted yang eksplisit, sedangkan cipher guard dan implementasi native tidak dianggap aktif sebelum lolos verifikasi pada development build.

5. **Kegagalan keamanan memiliki recovery yang tidak merusak data** (parent R4, R6)
   **Given** PIN salah berulang, biometrik ditolak, atau inisialisasi security gagal
   **When** pengguna mencoba melanjutkan
   **Then** aplikasi menerapkan throttling/fallback yang dapat dijelaskan dan tidak menghapus database lokal secara diam-diam.

## Constraints

- Data finansial, PIN, key material, dan backup tidak boleh masuk log, route parameter, analytics, screenshot otomatis, atau nama work request.
- PIN disimpan sebagai salted verifier dengan parameter KDF; PIN plaintext tidak boleh ditulis ke disk.
- Expo Go adalah tier UI dan standard SQLite; SQLCipher dan custom native capability wajib diverifikasi melalui EAS development build.
- Lupa PIN tidak membuka data lokal; reset dan restore backup tetap berada di luar epic ini dan dimiliki epic OCR, backup, and export.
- Permission diminta hanya saat aksi membutuhkan dan harus memiliki fallback ketika ditolak.

## Done when

- Pengguna baru dapat menyelesaikan onboarding offline dan mencapai Beranda dengan minimal satu dompet serta kategori tanpa duplikasi seed.
- PIN, biometrik, fallback, retry/throttling, auto-lock, dan privacy hardening memiliki test domain/integration yang lulus.
- Capability matrix dan development-build check membedakan perilaku Expo Go dari native security capability secara eksplisit.
- `npx expo lint`, `npx tsc --noEmit`, test suite terkait, dan pemeriksaan Android yang sesuai berjalan tanpa known security blocker.
- Epic menghasilkan jalur yang dapat dipakai sebagai dependency untuk wallet/category dan security contract untuk backup/restore.

## Boundaries

- Bootstrap dompet pertama dan kategori pilihan dimiliki epic ini; CRUD lanjutan, archive, reorder, dan historical reference management dimiliki epic Wallets and Categories.
- Database schema bisnis wallet/category tetap dibuat oleh story bootstrap yang diperlukan, tetapi feature management lanjutan tidak masuk epic ini.
- Backup restore, SQLCipher final KDF/key wrapping, dan recovery dari backup terenkripsi tidak dianggap selesai di sini jika memerlukan native spike atau format backup yang belum disepakati.
- Tidak mencakup transaction, transfer, dashboard, reconciliation, OCR, network sync, atau cloud account.

## References

- parent — `_bmad-output/initiative-arus-android-mvp/initiative-arus-android-mvp.md`, Requirements R4 dan R6
- security — `plan/05-security-privacy.md`
- user flow — `arus-user-flow.md`, §1, §2, §3A, §8
- architecture — `_bmad-output/initiative-arus-android-mvp/architecture-arus-android-mvp/architecture-arus-android-mvp.md`, AD-3, AD-7, AD-8, AD-9
- design — `_bmad-output/initiative-arus-android-mvp/ux-arus-android-mvp/DESIGN.md` dan `EXPERIENCE.md`
- ADR — `plan/decisions/ADR-006-expo-supersedes-native.md`

## Notes

- Decision: 2026-10-29 — Onboarding owns creation of the first workspace, including the first wallet and selected preset categories; Wallets and Categories owns later CRUD and historical management.
- Decision: 2026-10-29 — The first story is the security capability-tier tracer because native encryption claims must be proven in the binary that implements them.
- Decision: 2026-10-29 — The approved seven-story breakdown is extended with a small refactor sweep before final integration verification, as required for an epic with more than three entries.
