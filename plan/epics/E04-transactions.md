# E04 — Transaksi, Transfer, dan Riwayat

**Fase:** 1 / MVP

## Deliverables

- S-30, S-31, S-33, S-34, S-37, S-38.
- Income, expense, transfer, optional admin fee, edit/delete/undo, future-dated.
- Riwayat Paging dan detail yang menjelaskan hubungan transfer.

## Acceptance criteria

- Transfer tidak dapat memilih dompet yang sama dan selalu transactional.
- Penghapusan normal dapat di-undo; transaksi verified memicu recheck checkpoint.
- Saldo UI selalu berasal dari query yang mematuhi rule ERD.
