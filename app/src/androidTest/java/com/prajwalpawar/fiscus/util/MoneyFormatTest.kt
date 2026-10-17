package com.prajwalpawar.fiscus.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

// On device, because formatMoney uses Android's ICU formatter.
@RunWith(AndroidJUnit4::class)
class MoneyFormatTest {
    private val india = Locale.forLanguageTag("en-IN")
    private val us = Locale.US

    @Test
    fun formatsHundredthsWithTwoDecimals() {
        assertEquals("₹12.50", formatMoney(1250, india))
        assertEquals("$0.05", formatMoney(5, us))
        assertEquals("$1,234.00", formatMoney(123400, us))
    }

    @Test
    fun usesIndianGrouping() {
        assertEquals("₹12,34,567.89", formatMoney(123456789, india))
    }

    @Test
    fun formatsNegativeBalances() {
        assertEquals("-$12.50", formatMoney(-1250, us))
    }
}
