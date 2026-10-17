package com.prajwalpawar.fiscus.ui.screens.analysis

import com.prajwalpawar.fiscus.data.model.CategoryTotal
import com.prajwalpawar.fiscus.data.model.MonthTotals
import java.time.LocalDate
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

// Spending per day. The current month only counts days so far; past months count every day.
internal fun dailyAverage(total: Long, month: YearMonth, today: LocalDate = LocalDate.now()): Long {
    val days = when {
        month == YearMonth.from(today) -> today.dayOfMonth
        month.isAfter(YearMonth.from(today)) -> return 0L
        else -> month.lengthOfMonth()
    }
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
