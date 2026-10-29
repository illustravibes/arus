# Arus — Delivery Plan

Dokumen ini adalah rencana delivery **BMad-lite** untuk Arus: cukup terstruktur untuk menjaga keputusan dan kualitas produk finansial, tanpa ceremony penuh BMad.

## Status

| Area | Status | Sumber / keluaran |
|---|---|---|
| Product & UX specification | Siap ditinjau | `../arus-user-flow.md`, `../wireframes/*.dc.html` |
| Data-domain specification | Siap ditinjau | `../arus-erd.md` |
| Architecture & delivery plan | Baseline | Dokumen di direktori ini |
| Android project / kode | Belum dimulai | Phase 0 |

## Sumber kebenaran

- **Domain data dan aturan akuntansi:** [`../arus-erd.md`](../arus-erd.md)
- **Perilaku pengguna, layar, dan edge case:** [`../arus-user-flow.md`](../arus-user-flow.md)
- **Wireframe:** `../canvas.json` dan `../*.dc.html`
- **Rencana eksekusi dan quality gate:** direktori `plan/`

Jika terjadi konflik, domain rule di ERD mengalahkan UX; perubahan keduanya harus dicatat sebagai ADR atau pembaruan spesifikasi yang eksplisit.

## Urutan membaca

1. [Product brief](00-product-brief.md)
2. [MVP scope](02-mvp-scope.md)
3. [Architecture](01-architecture.md)
4. [Data model](03-data-model.md) dan [domain rules](04-domain-rules.md)
5. [Security & privacy](05-security-privacy.md)
6. [Testing strategy](07-testing-strategy.md)
7. [Release readiness](08-release-readiness.md)
8. Epic sesuai fase di [`epics/`](epics/)

## Cara memakai plan

Sebelum memulai sebuah epic, pastikan ada acceptance criteria, dependency, dan domain rule yang terkait. Sebelum merge, jalankan quality gate pada `07-testing-strategy.md`. Sebelum rilis, semua item blocker dalam `08-release-readiness.md` harus selesai.

Perubahan keputusan lintas fitur—misalnya enkripsi, cara menghitung saldo, atau modularisasi—wajib mendapat ADR baru di [`decisions/`](decisions/).

## Prinsip delivery

- Bangun **vertical slice**, bukan seluruh data layer atau seluruh UI sekaligus.
- Database adalah single source of truth dan aplikasi tetap berguna tanpa internet.
- Jangan menyimpan saldo turunan; hitung dari transaksi sesuai ERD.
- Utamakan integritas data, recovery, dan auditability dibanding kecepatan menambah fitur.
- MVP harus bisa dipakai harian sebelum automation, utang-piutang, atau OCR ditambahkan.
