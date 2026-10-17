package com.prajwalpawar.fiscus.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset

// A [start, end) range of transaction dates, in the stored form (UTC midnight millis).
data class DateRange(
    val startMillis: Long,
    val endMillis: Long
)

// Transaction dates are the local calendar day stored as UTC midnight, so a month's
// range is built the same way rather than from the device time zone.
fun monthRange(month: YearMonth = YearMonth.now()): DateRange {
    return DateRange(
        startMillis = month.atDay(1).toUtcMidnightMillis(),
        endMillis = month.plusMonths(1).atDay(1).toUtcMidnightMillis()
    )
}

// The local calendar day of a stored transaction date.
fun storedDateToLocalDate(millis: Long): LocalDate {
    return java.time.Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
}

private fun LocalDate.toUtcMidnightMillis(): Long {
    return atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}
