package com.prajwalpawar.fiscus.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyTest {

    @Test
    fun `adds and subtracts in minor units`() {
        val a = Money(1_050, "INR")
        val b = Money(250, "INR")
        assertEquals(Money(1_300, "INR"), a + b)
        assertEquals(Money(800, "INR"), a - b)
        assertEquals(Money(-1_050, "INR"), -a)
    }

    @Test
    fun `reports zero and negative`() {
        assertTrue(Money.zero("INR").isZero)
        assertTrue(Money(-1, "INR").isNegative)
    }

    @Test
    fun `rejects mixing currencies`() {
        assertThrows(IllegalArgumentException::class.java) { Money(1, "INR") + Money(1, "USD") }
    }

    @Test
    fun `fails loudly on overflow instead of wrapping`() {
        assertThrows(ArithmeticException::class.java) { Money(Long.MAX_VALUE, "INR") + Money(1, "INR") }
    }
}
