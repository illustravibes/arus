# ADR-005: WorkManager dan idempotency untuk background work

**Status:** Accepted  
**Tanggal:** 2026-10-22

## Decision

Gunakan WorkManager untuk pekerjaan durable yang tidak perlu presisi detik: recurring catch-up, reminder fallback, dan backup. Semua job memiliki unique name/idempotency strategy. Exact alarm dibatasi untuk reminder yang benar-benar presisi serta tunduk pada permission dan fallback.

## Consequences

- Worker aman terhadap retry/proses mati dan tidak menciptakan transaksi duplikat.
- Waktu notification fallback dapat meleset; UX harus mengkomunikasikan ini.
- Transaction generation tetap melewati domain service/database transaction yang sama dengan UI.
