# ADR-001: Local-first sebagai sumber kebenaran

**Status:** Accepted  
**Tanggal:** 2026-10-22

## Context

Arus menyimpan data finansial pribadi dan harus tetap berguna tanpa koneksi. Sinkronisasi tidak termasuk MVP.

## Decision

Database lokal terenkripsi adalah sumber kebenaran. UI membaca dari database reaktif dan seluruh write penting diselesaikan lokal secara atomik. Cloud backup adalah recovery/asynchronous copy, bukan source of truth atau mekanisme sync.

## Consequences

- MVP dapat bekerja offline dan tidak membutuhkan akun.
- Conflict resolution multi-device ditunda sampai ada kebutuhan produk dan desain khusus.
- Migrasi lokal, integrity, dan backup menjadi tanggung jawab rilis yang kritis.
