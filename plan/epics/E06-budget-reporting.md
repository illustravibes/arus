# E06 — Budget, Laporan, Search

**Fase:** 2

## Scope

Split transaksi, budget, laporan periodik, detail category, search/filter, dan template transfer. Semua aggregate memakai query database dan mematuhi flags `counts_in_report`/`counts_in_budget`.

## Gate

Tidak dimulai sebelum E04 telah memiliki saldo/query transaction yang tervalidasi. Boundary periode dan rollover harus diuji pada timezone serta tanggal gajian.
