---
id: 1
type: story
title: "Establish security capability tiers and encrypted-storage guard"
parent: epic-onboarding-security
covers: [4]
after: [1.1]
refined: true
hitl: true
risk: high
---

# Establish security capability tiers and encrypted-storage guard

## Description

Membuat perbedaan capability Expo Go dan EAS development build dapat diperiksa, serta menyediakan guard penyimpanan yang menolak klaim atau konfigurasi cipher yang belum didukung tanpa menghapus database lokal. Pekerjaan ini mencakup langkah manual pemilik untuk memeriksa konfigurasi dan, bila tersedia, menjalankan development build Android.

## Acceptance Criteria

1. **Expo Go development mode is explicit** (epic R4)
   **Given** aplikasi dijalankan melalui Expo Go untuk pekerjaan UI atau standard SQLite
   **When** capability penyimpanan dibaca saat startup
   **Then** aplikasi menandai mode tersebut sebagai development-only unencrypted, tidak menyebutnya sebagai SQLCipher/encrypted storage, dan tetap menyediakan pesan yang dapat ditindaklanjuti untuk development build.

2. **Unsupported encryption is rejected safely** (epic R4, R5)
   **Given** konfigurasi atau metadata meminta cipher yang tidak tersedia pada binary aktif
   **When** storage guard dijalankan
   **Then** inisialisasi berhenti dengan error terstruktur yang aman untuk pengguna, tidak melakukan downgrade atau destructive reset, dan database lokal tetap dipertahankan.

3. **Native capability has a verifiable build gate** (epic R4)
   **Given** EAS development profile digunakan untuk capability native security
   **When** konfigurasi project diperiksa dan development build Android dijalankan bila device/build service tersedia
   **Then** build memakai jalur development yang terpisah dari Expo Go, dan hasil pemeriksaan menyatakan dengan jelas apakah cipher capability benar-benar tersedia atau masih deferred.

4. **Capability contract is testable without secrets** (epic R4, R5)
   **Given** capability mode, cipher version, atau initialization failure diuji
   **When** unit/contract tests dijalankan
   **Then** setiap mode valid, mode yang tidak didukung, dan preservation path memiliki test yang lulus; output test dan diagnostics tidak mengandung passphrase, key material, nominal, atau isi database.

5. **Quality gate remains green** (epic R4, R6)
   **Given** perubahan story diterapkan pada repository
   **When** `npx expo lint`, `npx tsc --noEmit`, test capability terkait, dan `npx expo-doctor` dijalankan dari repository root
   **Then** semua pemeriksaan yang tersedia lulus, sedangkan pemeriksaan development build/device dicatat sebagai passed atau deferred dengan alasan konkret.

## Boundaries

- Story ini tidak mengimplementasikan SQLCipher final, KDF/key wrapping final, PIN, biometrik, app lock, onboarding UI, atau business schema.
- Development-only unencrypted mode harus eksplisit dan tidak boleh menjadi fallback diam-diam untuk production.
- Story ini tidak boleh menghapus, membuat ulang, atau mengubah isi database pengguna ketika cipher capability tidak tersedia.
- Pemeriksaan device/build service adalah langkah manual `hitl`; ketiadaan device tidak boleh diubah menjadi klaim bahwa native encryption telah terverifikasi.

## References

- parent — _bmad-output/initiative-arus-android-mvp/tickets/epic-onboarding-security/epic-onboarding-security.md
- architecture — _bmad-output/initiative-arus-android-mvp/architecture-arus-android-mvp/architecture-arus-android-mvp.md, AD-3, AD-7, AD-8, AD-9
- ADR — plan/decisions/ADR-006-expo-supersedes-native.md, pemetaan SQLCipher/Keystore dan aturan capability tier
- security — plan/05-security-privacy.md, kontrol database dan threat model
- release — plan/08-release-readiness.md, quality/build verification

## Notes

- Risk: high — kesalahan pada capability guard dapat membuat tim mengira data terenkripsi padahal aplikasi berjalan dengan standard SQLite.
- High-risk check: pemeriksa harus membuktikan bahwa mode Expo Go tidak mengaktifkan klaim encrypted storage dan bahwa unsupported cipher tidak memicu reset database.
- Decision: 2026-10-29 — Story ini menyiapkan guard dan bukti capability; implementasi final SQLCipher/KDF tetap deferred sampai development-build spike membuktikannya.
