const assert = require('node:assert/strict');
const test = require('node:test');

const {
  CURRENT_SCHEMA_VERSION,
  UnsupportedDatabaseVersionError,
  initializeDatabase,
} = require('../.foundation-dist/src/db/migrations.js');
const {
  MoneyValidationError,
  createRupiahAmount,
  formatRupiah,
} = require('../.foundation-dist/src/domain/money.js');

function fakeDatabase({ version = 0, failMigration = false } = {}) {
  const calls = [];
  let transactionStarted = false;
  return {
    calls,
    async execAsync(sql) {
      calls.push(['exec', sql]);
      if (failMigration && sql.includes('CREATE TABLE')) {
        throw new Error('migration failed');
      }
    },
    async getFirstAsync() {
      calls.push(['version', version]);
      return { user_version: version };
    },
    async withTransactionAsync(callback) {
      transactionStarted = true;
      calls.push(['transaction:start']);
      try {
        await callback();
        calls.push(['transaction:commit']);
      } catch (error) {
        calls.push(['transaction:rollback']);
        throw error;
      }
    },
    get transactionStarted() {
      return transactionStarted;
    },
  };
}

test('fresh database applies the foundation migration atomically', async () => {
  const db = fakeDatabase();

  await initializeDatabase(db);

  assert.equal(db.transactionStarted, true);
  assert.deepEqual(db.calls.slice(-2), [
    ['exec', 'PRAGMA user_version = 1;'],
    ['transaction:commit'],
  ]);
});

test('supported database opens without rerunning migrations', async () => {
  const db = fakeDatabase({ version: CURRENT_SCHEMA_VERSION });

  await initializeDatabase(db);

  assert.equal(db.transactionStarted, false);
  assert.equal(db.calls.some(([kind]) => kind === 'transaction:start'), false);
});

test('newer database is rejected without a transaction or reset', async () => {
  const db = fakeDatabase({ version: CURRENT_SCHEMA_VERSION + 1 });

  await assert.rejects(
    initializeDatabase(db),
    (error) =>
      error instanceof UnsupportedDatabaseVersionError &&
      error.databaseVersion === CURRENT_SCHEMA_VERSION + 1,
  );

  assert.equal(db.transactionStarted, false);
  assert.equal(db.calls.some(([kind, sql]) => kind === 'exec' && sql.includes('CREATE TABLE')), false);
});

test('failed migration rolls back and surfaces the original error', async () => {
  const db = fakeDatabase({ failMigration: true });

  await assert.rejects(initializeDatabase(db), /migration failed/);

  assert.deepEqual(db.calls.at(-1), ['transaction:rollback']);
});

test('money validation rejects invalid values without coercion', () => {
  for (const value of [0, -1, 1.5, Number.MAX_SAFE_INTEGER + 1, '100']) {
    assert.throws(() => createRupiahAmount(value), MoneyValidationError);
  }
  assert.equal(formatRupiah(createRupiahAmount(125000)), 'Rp 125.000');
});
