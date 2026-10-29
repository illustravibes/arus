# E05 — Rekonsiliasi Dasar

**Fase:** 1 / MVP

## Deliverables

- S-100, S-101, S-103.
- Input saldo aktual, preview selisih, adjustment/resolve, checkpoint dan recheck flag.

## Acceptance criteria

- Checkpoint dan adjustment tersimpan atomik.
- Perubahan transaksi yang verified ditandai dan dapat ditindak pengguna.
- Future-dated transaction tidak memengaruhi perhitungan rekonsiliasi saat ini.
