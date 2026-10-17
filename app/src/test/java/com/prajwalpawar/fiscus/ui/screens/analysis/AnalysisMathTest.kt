package com.prajwalpawar.fiscus.ui.screens.analysis

import com.prajwalpawar.fiscus.data.model.CategoryTotal
import com.prajwalpawar.fiscus.data.model.MonthTotals
import com.prajwalpawar.fiscus.util.Period
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class AnalysisMathTest {
    private fun cat(id: Long, total: Long) = CategoryTotal(id, "C$id", "category", total)

    @Test
    fun foldKeepsTopCategoriesAndSumsTheRest() {
        val totals = (1L..9L).map { cat(it, 1000 - it * 10) }
        val folded = foldCategories(totals, keep = 6)

        assertEquals(7, folded.size)
        assertEquals((1L..6L).toList(), folded.take(6).map { it.categoryId })
        assertEquals("Other", folded.last().name)
        assertEquals(totals.drop(6).sumOf { it.total }, folded.last().total)
        assertEquals(totals.sumOf { it.total }, folded.sumOf { it.total })
    }

    @Test
    fun foldLeavesShortListsAlone() {
        val seven = (1L..7L).map { cat(it, 100) }
        // Folding one leftover into "Other" would only rename it.
        assertEquals(seven, foldCategories(seven, keep = 6))
    }

    @Test
    fun percentChangeAgainstLastMonth() {
        assertEquals(50, percentChange(previous = 1000, current = 1500))
        assertEquals(-25, percentChange(previous = 1000, current = 750))
        assertNull(percentChange(previous = 0, current = 500))
    }

    @Test
    fun dailyAverageCountsOnlyDaysUpToToday() {
        val today = LocalDate.of(2026, 10, 10)
        // Current month: 10 days so far.
        assertEquals(100L, dailyAverage(1000, Period.month(YearMonth.of(2026, 10)), today))
        // September has 30 days.
        assertEquals(100L, dailyAverage(3000, Period.month(YearMonth.of(2026, 9)), today))
        // A single day and a 5-day range.
        assertEquals(700L, dailyAverage(700, Period(today, today), today))
        assertEquals(
            200L,
            dailyAverage(1000, Period(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 5)), today)
        )
        // Entirely in the future.
        assertEquals(0L, dailyAverage(3000, Period.month(YearMonth.of(2026, 11)), today))
    }

    @Test
    fun savingsRateCanGoNegativeAndNeedsIncome() {
        assertEquals(40, savingsRate(income = 1000, expense = 600))
        assertEquals(-20, savingsRate(income = 1000, expense = 1200))
        assertNull(savingsRate(income = 0, expense = 500))
    }

    @Test
    fun fillMonthsAddsZerosForQuietMonthsInOrder() {
        val rows = listOf(
            MonthTotals(YearMonth.of(2026, 8), 100, 50),
            MonthTotals(YearMonth.of(2026, 10), 200, 80)
        )
        val filled = fillMonths(rows, last = YearMonth.of(2026, 10), count = 6)

        assertEquals(
            (5 downTo 0).map { YearMonth.of(2026, 10).minusMonths(it.toLong()) },
            filled.map { it.month })
        assertEquals(listOf(0L, 0L, 0L, 50L, 0L, 80L), filled.map { it.expense })
    }

    @Test
    fun fillMonthsCrossesTheYear() {
        val filled = fillMonths(emptyList(), last = YearMonth.of(2027, 2), count = 6)
        assertEquals(YearMonth.of(2026, 9), filled.first().month)
        assertEquals(YearMonth.of(2027, 2), filled.last().month)
    }
}

class TrendScaleTest {
    private val oct = YearMonth.of(2026, 10)

    @Test
    fun scaleFollowsThePeriod() {
        val day = LocalDate.of(2026, 10, 18)
        assertNull(trendScale(Period(day, day)))
        assertEquals(TrendScale.MONTH_CONTEXT, trendScale(Period.month(oct)))
        assertEquals(TrendScale.DAILY, trendScale(Period(day.minusDays(30), day)))
        assertEquals(TrendScale.MONTHLY, trendScale(Period(day.minusDays(31), day)))
    }

    @Test
    fun dailyBucketsCoverEveryDayWithZerosForQuietDays() {
        val period = Period(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3))
        val rows =
            listOf(com.prajwalpawar.fiscus.data.model.DayTotals(LocalDate.of(2026, 10, 2), 0, 500))
        val buckets = dailyBuckets(rows, period)

        assertEquals(listOf(0L, 500L, 0L), buckets.map { it.expense })
        assertEquals(LocalDate.of(2026, 10, 2), buckets[1].period.start)
        assertEquals(true, buckets.all { it.isHighlighted && it.period.isSingleDay })
    }

    @Test
    fun monthlyBucketsSpanFirstToLastAndHighlightOnRequest() {
        val rows = listOf(MonthTotals(YearMonth.of(2026, 9), 0, 300))
        val buckets = monthlyBuckets(rows, YearMonth.of(2026, 8), oct) { it == oct }

        assertEquals(listOf(0L, 300L, 0L), buckets.map { it.expense })
        assertEquals(listOf(false, false, true), buckets.map { it.isHighlighted })
        assertEquals(Period.month(YearMonth.of(2026, 9)), buckets[1].period)
    }
}
