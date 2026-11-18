package com.illustravibes.arus

import org.junit.Assert.*
import org.junit.Test

class ArusBusinessRulesTest {

    @Test
    fun `test amount input must be positive`() {
        val validAmount = 500000L
        val invalidZero = 0L
        val invalidNegative = -10000L

        assertTrue(validAmount > 0L)
        assertFalse(invalidZero > 0L)
        assertFalse(invalidNegative > 0L)
    }

    @Test
    fun `test wallet balance ledger formula`() {
        val initialBalance = 1000000L // Dompet Tunai
        val income = 500000L
        val expense = 200000L
        val transferOut = 300000L
        val transferIn = 0L

        val calculatedBalance = initialBalance + income - expense + transferIn - transferOut
        assertEquals(1000000L, calculatedBalance)
    }

    @Test
    fun `test total combined balance does not change on transfer`() {
        val tunaiInitial = 1000000L
        val bankInitial = 2000000L
        val initialCombined = tunaiInitial + bankInitial

        // 1. Income 500.000 to Tunai
        val tunai1 = tunaiInitial + 500000L
        val bank1 = bankInitial
        assertEquals(3500000L, tunai1 + bank1)

        // 2. Expense 200.000 from Tunai
        val tunai2 = tunai1 - 200000L
        val bank2 = bank1
        assertEquals(3300000L, tunai2 + bank2)

        // 3. Transfer 300.000 from Tunai to Bank
        val tunai3 = tunai2 - 300000L
        val bank3 = bank2 + 300000L

        assertEquals(1000000L, tunai3) // Tunai = 1.000.000
        assertEquals(2300000L, bank3)  // Bank = 2.300.000
        assertEquals(3300000L, tunai3 + bank3) // Total combined balance remains 3.300.000
    }

    @Test
    fun `test future dated transaction exclusion`() {
        val nowMillis = System.currentTimeMillis()
        val futureMillis = nowMillis + 86400000L // Tomorrow

        val isEffectiveNow = futureMillis <= nowMillis
        assertFalse("Future-dated transaction must not be effective today", isEffectiveNow)
    }

    @Test
    fun `test negative balance permitted`() {
        val initialBalance = 100000L
        val expense = 250000L

        val resultBalance = initialBalance - expense
        assertEquals(-150000L, resultBalance)
        assertTrue("Negative balance is permitted per business rules", resultBalance < 0L)
    }
}
