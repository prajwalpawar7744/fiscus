package com.prajwalpawar.fiscus.core.money

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

// formatMoney uses android.icu, so it's covered by the instrumented MoneyFormatterDeviceTest.
class MoneyFormatterTest {

    @Test
    fun `default currency follows the locale, with a fallback`() {
        assertEquals("INR", defaultCurrencyCode(Locale.forLanguageTag("en-IN")))
        assertEquals("USD", defaultCurrencyCode(Locale.US))
        assertEquals(FALLBACK_CURRENCY_CODE, defaultCurrencyCode(Locale.ENGLISH))
    }
}
