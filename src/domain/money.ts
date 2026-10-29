/** A validated, positive integer amount of Rupiah. */
export type RupiahAmount = number & { readonly __rupiahAmount: unique symbol };

export type MoneyValidationCode =
  | 'NOT_A_NUMBER'
  | 'NOT_AN_INTEGER'
  | 'NOT_POSITIVE'
  | 'UNSAFE_INTEGER';

export class MoneyValidationError extends Error {
  readonly code: MoneyValidationCode;

  constructor(code: MoneyValidationCode) {
    super(`Invalid Rupiah amount: ${code}`);
    this.name = 'MoneyValidationError';
    this.code = code;
  }
}

/**
 * Validate a financial amount without touching UI or persistence.
 * Rupiah values are represented as safe positive integer JavaScript numbers.
 */
export function createRupiahAmount(input: unknown): RupiahAmount {
  if (typeof input !== 'number' || Number.isNaN(input)) {
    throw new MoneyValidationError('NOT_A_NUMBER');
  }
  if (!Number.isFinite(input) || !Number.isSafeInteger(input)) {
    throw new MoneyValidationError(
      Number.isInteger(input) ? 'UNSAFE_INTEGER' : 'NOT_AN_INTEGER',
    );
  }
  if (input <= 0) {
    throw new MoneyValidationError('NOT_POSITIVE');
  }
  return input as RupiahAmount;
}

export function isRupiahAmount(input: unknown): input is RupiahAmount {
  return (
    typeof input === 'number' &&
    Number.isSafeInteger(input) &&
    input > 0
  );
}

export function formatRupiah(input: RupiahAmount | number): string {
  const amount = createRupiahAmount(input);
  return new Intl.NumberFormat('id-ID', {
    style: 'currency',
    currency: 'IDR',
    maximumFractionDigits: 0,
    minimumFractionDigits: 0,
  }).format(amount);
}
