# ADR-004: UDF pada UI dan domain command untuk write

**Status:** Accepted  
**Tanggal:** 2026-10-22

## Decision

Compose screen menerima UI state immutable dari ViewModel dan mengirim user action. ViewModel memanggil use case/domain service dengan command tervalidasi; UI tidak menulis DAO/repository secara langsung.

## Consequences

- State screen, error, dan side effect lebih mudah diuji.
- Invariant keuangan dapat dijaga di satu layer dan tidak semata mengandalkan validasi UI.
- Ada mapping tambahan antara persistence, domain, dan UI model; ini diterima demi isolasi perubahan.
