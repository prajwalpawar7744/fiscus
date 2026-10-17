package com.prajwalpawar.fiscus.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.util.Locale

class PeriodTest {
    private val oct = Period.month(YearMonth.of(2026, 10))
    private val today = LocalDate.of(2026, 10, 18)

    @Test
    fun wholeMonthStepsByCalendarMonths() {
        assertTrue(oct.isWholeMonth)
        assertEquals(Period.month(YearMonth.of(2026, 9)), oct.previous())
        assertEquals(Period.month(YearMonth.of(2026, 11)), oct.next())
        // February has 28 days in 2026; stepping must not drift to "28 Feb – 27 Mar".
        assertEquals(Period.month(YearMonth.of(2026, 2)), Period.month(YearMonth.of(2026, 3)).previous())
    }

    @Test
    fun rangesStepByTheirOwnLength() {
        val range = Period(LocalDate.of(2026, 10, 3), LocalDate.of(2026, 10, 17))
        assertEquals(15, range.dayCount)
        assertEquals(Period(LocalDate.of(2026, 9, 18), LocalDate.of(2026, 10, 2)), range.previous())
        assertEquals(Period(LocalDate.of(2026, 10, 18), LocalDate.of(2026, 11, 1)), range.next())

        val day = Period(today, today)
        assertTrue(day.isSingleDay)
        assertEquals(Period(today.minusDays(1), today.minusDays(1)), day.previous())
    }

    @Test
    fun forwardStopsAtToday() {
        assertFalse(oct.canGoForward(today))
        assertTrue(Period.month(YearMonth.of(2026, 9)).canGoForward(today))
        assertFalse(Period(today, today).canGoForward(today))
        assertTrue(Period(today.minusDays(1), today.minusDays(1)).canGoForward(today))
    }

    @Test
    fun dateRangeIsEndExclusiveInStoredForm() {
        val day = Period(today, today)
        val start = today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        assertEquals(DateRange(start, start + 86_400_000L), day.toDateRange())
        assertEquals(monthRange(YearMonth.of(2026, 10)), oct.toDateRange())
    }

    @Test
    fun pickerMillisRoundTrip() {
        val range = Period(LocalDate.of(2026, 10, 3), LocalDate.of(2026, 10, 17))
        val millis = range.toDateRange()
        assertEquals(range, Period.fromPickerMillis(millis.startMillis, millis.endMillis - 86_400_000L))
    }

    @Test
    fun labels() {
        val uk = Locale.UK
        assertEquals("October 2026", oct.label(uk))
        assertTrue(Period(today, today).label(uk).contains("18 Oct 2026"))
        assertEquals("3 – 17 Oct 2026", Period(LocalDate.of(2026, 10, 3), LocalDate.of(2026, 10, 17)).label(uk))
        assertTrue(Period(LocalDate.of(2026, 9, 28), LocalDate.of(2026, 10, 3)).label(uk).contains(" – "))
    }

    @Test(expected = IllegalArgumentException::class)
    fun endBeforeStartIsRejected() {
        Period(today, today.minusDays(1))
    }
}
