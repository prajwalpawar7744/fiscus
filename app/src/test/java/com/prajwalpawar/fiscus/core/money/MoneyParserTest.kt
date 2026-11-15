package com.prajwalpawar.fiscus.core.money

import com.prajwalpawar.fiscus.model.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyParserTest {

    @Test
    fun `parses rupees and paise into minor units`() {
        assertEquals(Money(1999, "INR"), parseMoneyInput("19.99", "INR"))
        assertEquals(Money(1990, "INR"), parseMoneyInput("19.9", "INR"))
        assertEquals(Money(1900, "INR"), parseMoneyInput("19", "INR"))
        assertEquals(Money(1900, "INR"), parseMoneyInput("19.", "INR"))
        assertEquals(Money(50, "INR"), parseMoneyInput(".5", "INR"))
    }

    @Test
    fun `accepts a comma as the decimal separator`() {
        assertEquals(Money(1999, "EUR"), parseMoneyInput("19,99", "EUR"))
    }

    @Test
    fun `respects currencies without minor units`() {
        assertEquals(Money(500, "JPY"), parseMoneyInput("500", "JPY"))
        assertNull(parseMoneyInput("500.5", "JPY"))
        assertEquals(Money(500, "JPY"), parseMoneyInput("500.0", "JPY"))
    }

    @Test
    fun `rejects too many decimals, zero, blank and malformed input`() {
        assertNull(parseMoneyInput("1.999", "INR"))
        assertNull(parseMoneyInput("0", "INR"))
        assertNull(parseMoneyInput("0.00", "INR"))
        assertNull(parseMoneyInput("", "INR"))
        assertNull(parseMoneyInput(".", "INR"))
        assertNull(parseMoneyInput("1.2.3", "INR"))
        assertNull(parseMoneyInput("-5", "INR"))
        assertNull(parseMoneyInput("abc", "INR"))
    }

    @Test
    fun `rejects amounts that overflow a Long`() {
        assertNull(parseMoneyInput("99999999999999999999", "INR"))
    }

    @Test
    fun `typing validation allows partial input and caps the length`() {
        assertTrue(isValidAmountInput(""))
        assertTrue(isValidAmountInput("12."))
        assertTrue(isValidAmountInput("12,5"))
        assertFalse(isValidAmountInput("12.5.1"))
        assertFalse(isValidAmountInput("1a"))
        assertFalse(isValidAmountInput("1".repeat(16)))
    }

    @Test
    fun `formats money back into editable text`() {
        assertEquals("19.99", formatMoneyInput(Money(1999, "INR")))
        assertEquals("0.05", formatMoneyInput(Money(5, "INR")))
        assertEquals("500", formatMoneyInput(Money(500, "JPY")))
    }
}
