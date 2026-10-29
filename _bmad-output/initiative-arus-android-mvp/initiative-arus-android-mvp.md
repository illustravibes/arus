---
tracker_id: ""
id: "initiative-arus-android-mvp"
type: initiative
title: "Arus Android MVP dengan Expo"
parent: none
covers: []
sources: ["arus-user-flow.md", "arus-erd.md", "plan/00-product-brief.md", "plan/02-mvp-scope.md", "plan/01-architecture.md", "plan/03-data-model.md", "plan/04-domain-rules.md", "plan/05-security-privacy.md", "plan/06-backup-restore.md", "plan/07-testing-strategy.md", "plan/08-release-readiness.md", "package.json", "app.json"]
companions: ["plan/decisions/ADR-006-expo-supersedes-native.md", "_bmad-output/initiative-arus-android-mvp/architecture-arus-android-mvp/architecture-arus-android-mvp.md", "_bmad-output/initiative-arus-android-mvp/ux-arus-android-mvp/DESIGN.md", "_bmad-output/initiative-arus-android-mvp/ux-arus-android-mvp/EXPERIENCE.md"]
after: []
refined: false
risk: high
---

# Arus Android MVP dengan Expo

## Description

Membangun Arus sebagai aplikasi keuangan pribadi offline-first berbahasa Indonesia untuk Android menggunakan Expo SDK 58, React Native, Expo Router, `@expo/ui`, dan `expo-sqlite`. Inisiatif ini menerjemahkan rencana native Android lama ke stack Expo tanpa mengubah aturan domain, rumus saldo, atau alur pengguna yang sudah ditetapkan di ERD dan user flow.

## Outcome

Pengguna Android dapat menyiapkan dompet, mencatat transaksi inti, melihat saldo yang dapat dijelaskan, dan melakukan rekonsiliasi tanpa internet melalui aplikasi yang dapat diuji di Expo Go untuk UI serta development build untuk fitur native yang membutuhkan binary khusus.

## Requirements

1. **Pencatatan offline dan cepat** (product brief; user flow §1, §4.2–§4.4)
   **Given** pengguna telah menyelesaikan onboarding
   **When** pengguna mencatat pengeluaran, pemasukan, atau transfer
   **Then** transaksi tersimpan tanpa jaringan, default dompet/tanggal membantu input, dan alur pengeluaran biasa dapat diselesaikan maksimal tiga interaksi setelah keypad siap.

2. **Saldo dan laporan dapat dijelaskan** (ERD §1, §4, §6)
   **Given** database memiliki transaksi valid, future-dated, atau soft-deleted
   **When** aplikasi menampilkan saldo, net worth, atau riwayat
   **Then** nilai dihitung dari transaksi aktif sesuai tipe, future-dated dan deleted tidak memengaruhi saldo saat ini, dan detail dapat menelusuri asal angka.

3. **Integritas operasi multi-record** (ERD §7; domain rules)
   **Given** operasi transfer atau rekonsiliasi terdiri dari beberapa perubahan
   **When** salah satu perubahan gagal
   **Then** seluruh operasi dibatalkan tanpa state parsial, dan invariant split, referensi, checkpoint, serta archive tetap terpenuhi.

4. **Onboarding dan keamanan lokal** (user flow §4.1, §4.16; security plan)
   **Given** aplikasi baru atau terkunci
   **When** pengguna melakukan onboarding, membuka aplikasi, atau mengaktifkan PIN/biometrik
   **Then** minimal satu dompet diperlukan, seed idempotent, PIN tidak disimpan plaintext, fallback tersedia, dan data sensitif tidak bocor melalui log/screenshot/recent apps.

5. **Navigasi dan UI Android-native** (user flow §2, §6; wireframes; Expo UI docs)
   **Given** layar MVP dan fase berikutnya
   **When** pengguna bernavigasi atau mengisi form
   **Then** struktur route, bottom navigation, FAB, sheets, picker, list, form controls, loading/empty/error states, dan accessibility mengikuti kontrak UX Expo Android-first yang terdokumentasi.

6. **Database yang dapat dimigrasikan** (ERD §9; data model plan)
   **Given** database baru atau database dari schema release sebelumnya
   **When** aplikasi membuka database
   **Then** versioned migration berjalan forward tanpa destructive reset, foreign key/WAL aktif, enum aman, dan database schema yang lebih baru ditolak melalui recovery path.

7. **Kualitas dan delivery yang dapat diulang** (testing strategy; release readiness)
   **Given** perubahan kode atau release candidate
   **When** quality gate dijalankan
   **Then** lint, typecheck, unit/domain tests, database/migration tests, Expo Go smoke checks, dan EAS Android development/release build checks dapat dijalankan dengan hasil yang dapat diulang.

8. **Fitur bertahap tidak merusak MVP** (mvp scope; user flow §9; ERD §10)
   **Given** fase 2–4 menambah budget, recurring, debt, group order, OCR, backup, dan export
   **When** fitur tersebut diimplementasikan
   **Then** fitur memakai kontrak domain dan migration yang kompatibel, tidak menjadi dependency penggunaan inti MVP, dan capability yang membutuhkan custom native binary diberi gate development build/EAS.

## Constraints

- Expo SDK 58, React Native 0.88, Expo Router, `@expo/ui`, dan dependency yang kompatibel dengan SDK 58 adalah baseline saat ini.
- Expo Go tetap menjadi jalur pengujian utama untuk UI dan standard SQLite; SQLCipher tidak dianggap aktif di Expo Go dan wajib diverifikasi melalui development build.
- Folder `ios/` dan `android/` tidak dibuat atau diedit manual; konfigurasi native harus melalui app config/config plugin/CNG.
- Nominal adalah integer rupiah positif; tidak ada float/double untuk perhitungan finansial.
- Saldo turunan tidak disimpan sebagai source of truth; saldo dihitung dari jurnal transaksi.
- Data finansial, kontak, catatan, attachment, key, dan password tidak boleh masuk log, analytics, route, atau screenshot otomatis.
- Core MVP harus berguna penuh secara offline dan tidak boleh bergantung pada koneksi bank/e-wallet.
- Dokumentasi baru menggunakan Bahasa Indonesia; identifier kode tetap Bahasa Inggris.

## Done when

- MVP onboarding, wallet, category, transaction, transfer, dashboard, settings, dan reconciliation berjalan pada Android.
- Database schema/migration dan domain invariant utama memiliki test otomatis yang lulus.
- Expo Go smoke path berjalan untuk UI dan standard SQLite; development build EAS Android memverifikasi capability native/security yang tidak tersedia di Expo Go.
- Setiap epic memiliki acceptance criteria, dependency, dan verification command yang dapat dijalankan dari clean checkout.
- Tidak ada known crash blocker, data-loss defect, atau security blocker untuk alpha internal.

## Boundaries

- MVP tidak mencakup bank sync, multi-device realtime sync, investasi, credit scoring, akuntansi bisnis, OCR, cloud backup, atau export sebagai syarat penggunaan inti.
- Kotlin/Compose/Room/Hilt/WorkManager/CameraX bukan implementation stack; aturan domain yang dirujuk oleh rencana native tetap dipertahankan dan dipetakan ke Expo.
- `wireframes/` adalah referensi layout dan hierarchy; `DESIGN.md` serta `EXPERIENCE.md` adalah kontrak visual/perilaku setelah disetujui.
- Tracer path: foundation → onboarding/security → wallets/categories → transactions/transfer → dashboard → reconciliation → phase 2–4 extensions.

## References

- product — `plan/00-product-brief.md`
- user flow — `arus-user-flow.md`
- data/domain — `arus-erd.md`, `plan/03-data-model.md`, `plan/04-domain-rules.md`
- scope — `plan/02-mvp-scope.md`
- security — `plan/05-security-privacy.md`
- quality/release — `plan/07-testing-strategy.md`, `plan/08-release-readiness.md`
- wireframes — `wireframes/canvas.json` and `wireframes/*.dc.html`
- Expo baseline — `package.json`, `app.json`
- architecture — `plan/decisions/ADR-006-expo-supersedes-native.md`, `architecture-arus-android-mvp/architecture-arus-android-mvp.md`
- UX — `ux-arus-android-mvp/DESIGN.md`, `ux-arus-android-mvp/EXPERIENCE.md`

## Notes

- Decision: 2026-10-29 — Expo/React Native supersedes the previous native Kotlin implementation plan; ERD and user-flow behavior remain authoritative.
- Decision: 2026-10-29 — BMAD v7 preview minimal skills are installed at workspace scope; initiative output lives under `_bmad-output`.
- Decision: 2026-10-29 — Standard SQLite is allowed for Expo Go UI/development work, but encrypted persistence is gated on an EAS development build.
- Open question: final SQLCipher integration and key derivation implementation require a development-build spike before the security epic is accepted.
- Open question: whether Drizzle ORM adds enough value over typed SQL for this schema is deferred to the foundation spike.
