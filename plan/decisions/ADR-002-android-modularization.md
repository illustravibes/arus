# ADR-002: Modularisasi Android bertahap per feature

**Status:** Accepted  
**Tanggal:** 2026-10-22

## Decision

Gunakan `:app`, sejumlah `:core:*`, dan `:feature:*` dengan dependency satu arah. Mulai dari batas feature yang bernilai; jangan memecah modul terlalu granular sebelum ada manfaat compile-time, ownership, atau isolasi dependency yang jelas.

## Consequences

- Struktur mendukung scaling feature tanpa menambah kompleksitas build berlebihan pada hari pertama.
- `:app` menjadi composition root; feature tidak bergantung satu sama lain secara langsung.
- Model lintas feature ditempatkan di `:core:model`, bukan saling mengimpor implementation.
