# Production Launch Checklist

Checklist ini merujuk dan melengkapi [`../08-release-readiness.md`](../08-release-readiness.md).

- [ ] Semua blocker release readiness selesai.
- [ ] Final signed artifact berasal dari commit/tag yang dapat ditelusuri.
- [ ] Version code tidak akan menabrak release sebelumnya.
- [ ] Store listing, privacy policy, support contact, dan release notes siap.
- [ ] Gradual rollout dan kriteria pause/rollback disetujui.
- [ ] Prosedur incident untuk data-loss/security issue memiliki owner dan jalur eskalasi.
- [ ] Tidak ada backup/restore feature yang dipromosikan sebelum round-trip test selesai.
