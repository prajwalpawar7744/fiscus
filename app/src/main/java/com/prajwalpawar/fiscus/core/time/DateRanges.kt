package com.prajwalpawar.fiscus.core.time

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/** The calendar month containing [today], first to last day (ADR-029). */
fun currentMonth(today: LocalDate): LocalDateRange {
    val first = LocalDate(today.year, today.month, 1)
    return first..first.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1))
}
