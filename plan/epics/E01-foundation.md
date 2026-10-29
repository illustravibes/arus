# E01 — Foundation

**Fase:** 0  
**Tujuan:** Membuat baseline Android yang dapat dibuild, diuji, dan aman dikembangkan.

## Deliverables

- Gradle Kotlin DSL, version catalog, debug/release, CI baseline.
- Compose design system, app shell, navigation root, dependency injection.
- Room + SQLCipher proof of open/create, schema export, test database.
- Error/result convention, `Clock`, dispatcher, logging sanitizer.
- Static analysis, unit test, instrumented test harness, release/R8 smoke build.

## Acceptance criteria

- Clean checkout dapat menjalankan lint, unit test, instrumented test, dan release build.
- Database dienkripsi dapat dibuka ulang tanpa key/data masuk log.
- Migration v1 baseline diuji.
- Tidak ada feature implementation bisnis sebelum architecture boundary minimal ini tersedia.

## Dependencies

ADR-001 sampai ADR-005 disetujui. Minimum/target SDK dan dependency versions difinalkan pada bootstrap berdasarkan versi stable saat itu.
