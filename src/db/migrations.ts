import type { SQLiteDatabase } from 'expo-sqlite';

export const CURRENT_SCHEMA_VERSION = 1;

export class UnsupportedDatabaseVersionError extends Error {
  readonly databaseVersion: number;
  readonly supportedVersion: number;

  constructor(databaseVersion: number, supportedVersion = CURRENT_SCHEMA_VERSION) {
    super('The local database was created by a newer version of Arus.');
    this.name = 'UnsupportedDatabaseVersionError';
    this.databaseVersion = databaseVersion;
    this.supportedVersion = supportedVersion;
  }
}

const FOUNDATION_MIGRATIONS = [
  {
    version: 1,
    apply: async (db: SQLiteDatabase) => {
      // This table is deliberately metadata-only. Business tables belong to
      // their feature stories and must not be introduced by the foundation.
      await db.execAsync(`
        CREATE TABLE arus_foundation_metadata (
          key TEXT PRIMARY KEY NOT NULL,
          value TEXT NOT NULL
        );
      `);
      await db.execAsync('PRAGMA user_version = 1;');
    },
  },
] as const;

/**
 * Initialize one SQLite connection and apply only forward migrations.
 * Connection pragmas are intentionally outside the migration transaction;
 * schema DDL and user_version updates are committed atomically.
 */
export async function initializeDatabase(db: SQLiteDatabase): Promise<void> {
  await db.execAsync('PRAGMA journal_mode = WAL; PRAGMA foreign_keys = ON;');

  const versionRow = await db.getFirstAsync<{ user_version: number }>('PRAGMA user_version;');
  const currentVersion = versionRow?.user_version;
  if (currentVersion === undefined || !Number.isInteger(currentVersion) || currentVersion < 0) {
    throw new Error('The local database schema version could not be read.');
  }
  if (currentVersion > CURRENT_SCHEMA_VERSION) {
    throw new UnsupportedDatabaseVersionError(currentVersion);
  }
  if (currentVersion === CURRENT_SCHEMA_VERSION) {
    return;
  }

  await db.withTransactionAsync(async () => {
    for (const migration of FOUNDATION_MIGRATIONS) {
      if (migration.version > currentVersion) {
        await migration.apply(db);
      }
    }
  });
}
