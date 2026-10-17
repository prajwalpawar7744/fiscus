package com.prajwalpawar.fiscus.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

// A span of whole local days, start and end inclusive: a calendar month, one day, or any range.
data class Period(
    val start: LocalDate,
    val end: LocalDate
) {
    init {
        require(!end.isBefore(start)) { "A period can't end before it starts." }
    }

    val isWholeMonth: Boolean
        get() = start.dayOfMonth == 1 && end == YearMonth.from(start).atEndOfMonth()

    val isSingleDay: Boolean get() = start == end

    val dayCount: Long get() = ChronoUnit.DAYS.between(start, end) + 1

    // The same-length span just before this one (the previous month for a whole month).
    fun previous(): Period {
        return if (isWholeMonth) {
            month(YearMonth.from(start).minusMonths(1))
        } else {
            Period(start.minusDays(dayCount), start.minusDays(1))
        }
    }

    // The same-length span just after this one (the next month for a whole month).
    fun next(): Period {
        return if (isWholeMonth) {
            month(YearMonth.from(start).plusMonths(1))
        } else {
            Period(end.plusDays(1), end.plusDays(dayCount))
        }
    }

    // Stepping forward is allowed while the next period starts no later than today.
    fun canGoForward(today: LocalDate = LocalDate.now()): Boolean = !next().start.isAfter(today)

    // In the stored form: UTC-midnight millis, end exclusive.
    fun toDateRange(): DateRange = DateRange(
        startMillis = start.toUtcMidnightMillis(),
        endMillis = end.plusDays(1).toUtcMidnightMillis()
    )

    // "October 2026", "18 Oct 2026", "3 – 17 Oct 2026", "28 Sept 2026 – 3 Oct 2026".
    fun label(locale: Locale = Locale.getDefault()): String {
        val medium = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)

        return when {
            isWholeMonth -> start.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale))
            isSingleDay -> start.format(medium)
            YearMonth.from(start) == YearMonth.from(end) ->
                "${start.dayOfMonth} – ${end.format(medium)}"
            else -> "${start.format(medium)} – ${end.format(medium)}"
        }
    }

    companion object {
        fun month(month: YearMonth): Period = Period(month.atDay(1), month.atEndOfMonth())

        fun currentMonth(today: LocalDate = LocalDate.now()): Period = month(YearMonth.from(today))

        // From the date picker's UTC-midnight millis.
        fun fromPickerMillis(startMillis: Long, endMillis: Long): Period =
            Period(storedDateToLocalDate(startMillis), storedDateToLocalDate(endMillis))
    }
}

private fun LocalDate.toUtcMidnightMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
