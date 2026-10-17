package com.prajwalpawar.fiscus.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.util.Locale

// formatMoney uses Android ICU, so its tests live in androidTest (MoneyFormatTest).
class MoneyTest {
    private val india = Locale.forLanguageTag("en-IN")

    @Test
    fun fallsBackWhenLocaleHasNoCountry() {
        assertEquals("¤", currencySymbol(Locale.ENGLISH))
        assertEquals("₹", currencySymbol(india))
    }
}

class DateRangesTest {
    private fun utcMidnight(date: LocalDate): Long =
        date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    @Test
    fun monthRangeCoversFirstDayUpToNextMonth() {
        val range = monthRange(YearMonth.of(2026, 10))

        assertEquals(utcMidnight(LocalDate.of(2026, 10, 1)), range.startMillis)
        assertEquals(utcMidnight(LocalDate.of(2026, 11, 1)), range.endMillis)
    }

    @Test
    fun decemberRollsIntoNextYear() {
        val range = monthRange(YearMonth.of(2026, 12))

        assertEquals(utcMidnight(LocalDate.of(2027, 1, 1)), range.endMillis)
    }

    @Test
    fun lastDayOfMonthIsInsideAndFirstOfNextIsNot() {
        val range = monthRange(YearMonth.of(2026, 2))
        val lastDay = utcMidnight(LocalDate.of(2026, 2, 28))
        val nextMonth = utcMidnight(LocalDate.of(2026, 3, 1))

        assert(lastDay >= range.startMillis && lastDay < range.endMillis)
        assert(nextMonth >= range.endMillis)
    }

    @Test
    fun storedDateRoundTripsToTheSameDay() {
        val day = LocalDate.of(2026, 10, 18)
        assertEquals(day, storedDateToLocalDate(utcMidnight(day)))
    }
}
