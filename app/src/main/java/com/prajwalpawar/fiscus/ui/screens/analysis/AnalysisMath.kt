package com.prajwalpawar.fiscus.ui.screens.analysis

import com.prajwalpawar.fiscus.data.model.CategoryTotal
import com.prajwalpawar.fiscus.data.model.DayTotals
import com.prajwalpawar.fiscus.data.model.MonthTotals
import com.prajwalpawar.fiscus.util.Period
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.YearMonth

// Past this many categories the rest fold into one "Other" bar; more bars stop being comparable.
internal const val MAX_CATEGORY_BARS = 6

// Keeps the [keep] largest (input is largest first) and sums the rest into "Other".
internal fun foldCategories(totals: List<CategoryTotal>, keep: Int = MAX_CATEGORY_BARS): List<CategoryTotal> {
    // Folding a single leftover into "Other" would just rename it.
    if (totals.size <= keep + 1) return totals

    val rest = totals.drop(keep)
    return totals.take(keep) + CategoryTotal(
        categoryId = null,
        name = "Other",
        iconKey = "category",
        total = rest.sumOf { it.total }
    )
}

// Percentage change from [previous] to [current]; null when there was nothing to compare with.
internal fun percentChange(previous: Long, current: Long): Int? {
    if (previous <= 0L) return null
    return Math.round((current - previous) * 100.0 / previous).toInt()
}

// Spending per day over the period's days up to today (future days haven't happened yet).
internal fun dailyAverage(total: Long, period: Period, today: LocalDate = LocalDate.now()): Long {
    if (period.start.isAfter(today)) return 0L
    val lastCounted = if (period.end.isAfter(today)) today else period.end
    val days = ChronoUnit.DAYS.between(period.start, lastCounted) + 1
    return Math.round(total.toDouble() / days)
}

// Share of income kept, as a whole percentage (negative when spending passed income).
// Null without income, since there's nothing to keep a share of.
internal fun savingsRate(income: Long, expense: Long): Int? {
    if (income <= 0L) return null
    return Math.round((income - expense) * 100.0 / income).toInt()
}

// One entry for every month from [last] - ([count] - 1) to [last], zeros where nothing was recorded.
internal fun fillMonths(rows: List<MonthTotals>, last: YearMonth, count: Int): List<MonthTotals> {
    val byMonth = rows.associateBy { it.month }
    return (count - 1 downTo 0).map { back ->
        val month = last.minusMonths(back.toLong())
        byMonth[month] ?: MonthTotals(month, income = 0L, expense = 0L)
    }
}

// How the trend chart buckets the period.
enum class TrendScale {
    // A whole month: that month among the months before it, for context.
    MONTH_CONTEXT,
    // A short range: one column per day.
    DAILY,
    // A long range: one column per month, counting only days inside the range.
    MONTHLY
}

// Past this many days, daily columns get too thin to read or tap, so months take over.
internal const val DAILY_TREND_MAX_DAYS = 31

// Null for a single day: a one-column chart says nothing the tiles don't.
internal fun trendScale(period: Period): TrendScale? = when {
    period.isSingleDay -> null
    period.isWholeMonth -> TrendScale.MONTH_CONTEXT
    period.dayCount <= DAILY_TREND_MAX_DAYS -> TrendScale.DAILY
    else -> TrendScale.MONTHLY
}

// One trend column: the span it covers (tapping it opens that span) and its spending.
data class TrendBucket(
    val period: Period,
    val expense: Long,
    // Emphasised in the accent colour; the rest are muted context.
    val isHighlighted: Boolean
)

// A column per day of [period], zeros where nothing was spent; all are the period itself.
internal fun dailyBuckets(rows: List<DayTotals>, period: Period): List<TrendBucket> {
    val byDay = rows.associate { it.day to it.expense }
    return generateSequence(period.start) { it.plusDays(1) }
        .takeWhile { !it.isAfter(period.end) }
        .map { day -> TrendBucket(Period(day, day), byDay[day] ?: 0L, isHighlighted = true) }
        .toList()
}

// A column per month from [first] to [last], zeros where nothing was spent.
internal fun monthlyBuckets(
    rows: List<MonthTotals>,
    first: YearMonth,
    last: YearMonth,
    isHighlighted: (YearMonth) -> Boolean
): List<TrendBucket> {
    val count = (first.until(last, ChronoUnit.MONTHS) + 1).toInt()
    return fillMonths(rows, last = last, count = count).map {
        TrendBucket(Period.month(it.month), it.expense, isHighlighted(it.month))
    }
}

