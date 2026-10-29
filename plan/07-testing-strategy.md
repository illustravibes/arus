# Testing Strategy

## Piramida test

| Lapisan | Fokus | Contoh |
|---|---|---|
| Unit | Domain pure, formatter, ViewModel | efek type transaksi, validasi transfer, state form |
| Database/instrumented | Room, SQLCipher, migration, DAO transaction | migration seluruh versi, aggregate saldo, atomic transfer |
| UI/instrumented | Alur bernilai tinggi | onboarding, quick expense, transfer, reconcile |
| Manual exploratory | Device/permission/backup nyata | biometric, exact alarm fallback, rotation, restore lintas perangkat |

## Test wajib per perubahan

- Domain financial rule baru: unit test normal, boundary, dan invalid input.
- DAO/schema baru: test migration dan query pada database nyata.
- Operasi multi-record: test rollback saat salah satu write gagal.
- UI state baru: test loading, populated, empty, validation failure, error/retry.
- Permission/intent baru: test deny/cancel dan input hostile.

## Test fixture utama

1. Cash, bank, e-wallet, dan credit wallet.
2. Income, expense, transfer dengan/tanpa admin, adjustment plus/minus, future-dated, soft-deleted.
3. Checkpoint dengan transaksi diedit/dihapus.
4. Debt, split, recurring, dan group order untuk fase berikutnya.
5. Timezone serta tanggal 29–31 untuk recurring.

## Property / invariant tests

- Σ `transaction_split.amount == transaction.amount` bila split diwajibkan.
- Transfer sukses selalu memiliki dua sisi yang benar dan group identik; kegagalan tidak meninggalkan sisi apa pun.
- `repayment_allocation` tidak pernah melebihi principal.
- Saldo yang dihitung ulang sama setelah aplikasi dibuka kembali.
- Transaksi future-dated/deleted tidak memengaruhi saldo sekarang.

## CI quality gate

Pull request harus lulus compile, static analysis, unit test, database/migration test, dan build release. Test flaky diperlakukan sebagai defect; jangan dibypass tanpa issue dan owner. Coverage adalah sinyal, bukan pengganti test invariant.
