package com.prajwalpawar.fiscus.core.money

import com.prajwalpawar.fiscus.model.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class MoneyFormatterDeviceTest {

    private val india = Locale.forLanguageTag("en-IN")

    @Test
    fun formatsMinorUnitsExactly() {
        assertEquals("₹19.99", formatMoney(Money(1_999, "INR"), india))
        assertEquals("$0.01", formatMoney(Money(1, "USD"), Locale.US))
    }

    @Test
    fun usesIndianDigitGrouping() {
        assertEquals("₹1,24,580", formatMoney(Money(12_458_000, "INR"), india))
        assertEquals("₹50.02", formatMoney(Money(5_002, "INR"), india))
        assertEquals("₹50.50", formatMoney(Money(5_050, "INR"), india))
    }

    @Test
    fun formatsNegativeAmounts() {
        assertEquals("-$5", formatMoney(Money(-500, "USD"), Locale.US))
    }

    @Test
    fun respectsCurrenciesWithoutMinorUnits() {
        val formatted = formatMoney(Money(1_500, "JPY"), Locale.JAPAN)
        assertTrue(formatted, formatted.contains("1,500"))
        assertFalse(formatted, formatted.contains("."))
    }
}
