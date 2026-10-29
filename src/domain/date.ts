/** Epoch milliseconds used by persisted Arus records. */
export type EpochMilliseconds = number & { readonly __epochMilliseconds: unique symbol };

export type DateValidationCode = 'INVALID_DATE' | 'UNSAFE_EPOCH';

export class DateValidationError extends Error {
  readonly code: DateValidationCode;

  constructor(code: DateValidationCode) {
    super(`Invalid epoch date: ${code}`);
    this.name = 'DateValidationError';
    this.code = code;
  }
}

function assertEpochMilliseconds(value: number): EpochMilliseconds {
  if (!Number.isFinite(value) || !Number.isSafeInteger(value)) {
    throw new DateValidationError('UNSAFE_EPOCH');
  }
  if (Number.isNaN(new Date(value).getTime())) {
    throw new DateValidationError('INVALID_DATE');
  }
  return value as EpochMilliseconds;
}

export function toEpochMilliseconds(input: Date | number): EpochMilliseconds {
  if (input instanceof Date) {
    return assertEpochMilliseconds(input.getTime());
  }
  if (typeof input !== 'number') {
    throw new DateValidationError('INVALID_DATE');
  }
  return assertEpochMilliseconds(input);
}

export function fromEpochMilliseconds(input: EpochMilliseconds | number): Date {
  return new Date(toEpochMilliseconds(input));
}

/** Format an explicitly supplied timezone; this never reads the device clock. */
export function formatDateInTimeZone(
  input: EpochMilliseconds | number,
  timeZone: string,
  locale = 'id-ID',
): string {
  return new Intl.DateTimeFormat(locale, {
    dateStyle: 'medium',
    timeZone,
  }).format(fromEpochMilliseconds(input));
}

/** Stable YYYY-MM-DD key for grouping records in an explicit timezone. */
export function dateKeyInTimeZone(
  input: EpochMilliseconds | number,
  timeZone: string,
): string {
  const parts = new Intl.DateTimeFormat('en-CA', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    timeZone,
  }).formatToParts(fromEpochMilliseconds(input));
  const values = Object.fromEntries(parts.map(({ type, value }) => [type, value]));
  return `${values.year}-${values.month}-${values.day}`;
}
