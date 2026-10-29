# ADR-003: Room berversi dengan enkripsi database

**Status:** Accepted  
**Tanggal:** 2026-10-22

## Decision

Gunakan Room dengan schema export dan migration forward sejak database v1. Database disimpan menggunakan SQLCipher; key material dikelola melalui abstraction yang dibungkus Android Keystore. Enum disimpan sebagai `TEXT`.

## Consequences

- Data at rest lebih terlindungi, tetapi startup, migration, recovery, dan test memerlukan perhatian tambahan.
- Destructive migration dilarang untuk data pengguna.
- Upgrade dependency SQLCipher/Room harus memiliki migration and open-database test pada release candidate.
