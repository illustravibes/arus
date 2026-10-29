# Release Readiness

Semua item bertanda blocker harus selesai sebelum production release.

## Data & correctness — blocker

- [ ] Semua migration dari schema release terdahulu lulus pada perangkat/emulator instrumented.
- [ ] Tidak ada destructive migration, reset diam-diam, atau fallback yang menghapus data.
- [ ] Test saldo untuk semua type transaksi dan future-dated/deleted lulus.
- [ ] Transfer dan rekonsiliasi terbukti atomic termasuk rollback gagal.
- [ ] Invariant split, checkpoint, archive, dan system category diuji.
- [ ] Release build diuji membuka database yang dibuat oleh debug-to-release compatible path.

## Security & privacy — blocker

- [ ] SQLCipher/key handling dan PIN verifier direview.
- [ ] Tidak ada nominal, note, merchant, kontak, key, password, atau path backup di log/crash report.
- [ ] Permission diklaim minimal dan diminta kontekstual.
- [ ] Intent/deep link/exported component direview.
- [ ] R8 aktif di release dan smoke-tested.
- [ ] Privacy policy, data retention, dan langkah lupa PIN tersedia bagi pengguna.

## UX & accessibility — blocker

- [ ] Semua form penting tervalidasi dan menghindari kehilangan input saat rotasi/proses mati.
- [ ] Empty/error/loading state untuk fitur rilis tersedia.
- [ ] TalkBack labels, target sentuh, kontras, ukuran font besar, serta dark theme diuji.
- [ ] Offline dan jaringan buruk tidak memblokir MVP.

## Operasional

- [ ] Version code/name, signing, dan artifact provenance terdokumentasi.
- [ ] Crash monitoring tersanitasi dan proses triage tersedia.
- [ ] Rollout bertahap dan kill/rollback plan tersedia bila ada layanan remote.
- [ ] Catatan rilis menjelaskan perubahan schema/data bila ada.
- [ ] Smoke test perangkat fisik minimal mencakup Android baseline dan versi terbaru yang didukung.
